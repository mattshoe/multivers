package io.github.mattshoe.shoebox

import io.github.mattshoe.shoebox.util.*
import org.apache.maven.artifact.versioning.ComparableVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.create

class MultiVersPlugin : Plugin<Project> {
    companion object {
        const val GRADLE_GROUP = "MultiVers"
    }

    override fun apply(project: Project) {
        println("multivers applied!")

        val resolver = Resolver(project)
        val extension = project.extensions.create<MultiVersExtension>("multivers")


        project.afterEvaluate {
            println("project.afterEvaluateBlocking started")
            val variantAggregations = mutableListOf<VariantAggregation>()

            extension.dependencyVariants.forEach { dependencyVariant ->
                println("aggregating variant $dependencyVariant")
                val variantAggregation = VariantAggregation(
                    dependencyVariant.group,
                    dependencyVariant.artifact
                )
                variantAggregations.add(variantAggregation)

                println("starting versionsJob")
                dependencyVariant.variantVersions.forEach {
                    println("aggregating version: ${it.version}")
                    variantAggregation.addVersionData(it.version, extension.tasks)
                    variantAggregation.addVersionData(it.version, dependencyVariant.tasks)
                    variantAggregation.addVersionData(it.version, it.tasks)
                }

                println("aggregating ranges")
                val allVersions = resolver.allAvailableVersions(
                    dependencyVariant.group,
                    dependencyVariant.artifact
                )

                println("Found all versions:\n\t${allVersions.joinToString { it }}")

                dependencyVariant.variantRanges.forEach { rangeData ->
                    println("aggregating range: ${rangeData.range.start}-${rangeData.range.end}")
                    val rangeStart = ComparableVersion(rangeData.range.start)
                    val rangeEnd = ComparableVersion(rangeData.range.end)

                    allVersions.filter {
                        println("comparing version: $it")
                        val version = ComparableVersion(it)
                        version.greaterThanEqualTo(rangeStart) && version.lessThan(rangeEnd)
                    }.forEach {
                        println("adding version: $it")
                        variantAggregation.addVersionData(it, rangeData.tasks)
                    }
                }
            }

            val variantTasks = mutableSetOf<Task>()

            variantAggregations.forEach { variant ->
                val topLevelVariantTasks = mutableSetOf<Task>()

                println("creating tasks for ${variant.artifactId}")
                variant.versions.get().forEach { (version, tasks) ->
                    val versionTaskDescriptor = "${variant.name}_v${variant.sanitizeString(version)}"
                    println("versionTaskDescriptor = $versionTaskDescriptor")
                    val versionForceTask = project.tasks.multiversTask("${versionTaskDescriptor}_forceVersion") {
                        doFirst {
                            println("running $name doFirst")
                            project.configurations.forEach { configuration ->
                                configuration.incoming.beforeResolve {
                                    configuration.resolutionStrategy.force(variant.gav(version))
                                }
                            }
                        }
                        doLast {
                            println("running $name doLast")
                            delete(rootProject.buildDir.resolve("caches/modules-2"))
                            delete(rootProject.buildDir.resolve("caches/transforms-2"))
                        }
                    }.also {
                        println("versionForceTask = ${it.name}")
                    }

                    val cleanTask = project.tasks.multiversTask("${versionTaskDescriptor}_clean") {
                        mustRunAfter(versionForceTask.name)
                        dependsOn(versionForceTask.name, "clean")
                        doLast {
                            println("Running $name")
                        }
                    }.also {
                        println("buildTask = ${it.name}")
                    }

                    val buildTask = project.tasks.multiversTask("${versionTaskDescriptor}_build") {
                        mustRunAfter(cleanTask.name)
                        dependsOn(cleanTask.name, "build")
                        doLast {
                            println("Running $name")
                        }
                    }.also {
                        println("buildTask = ${it.name}")
                    }

                    var previousTask: Task = buildTask

                    println("All Tasks for ${versionTaskDescriptor}:\n\t${tasks.joinToString("\n\t") { it }}")

                    tasks.remove("build")
                    tasks.remove("clean")
                    tasks.map { it.trim() }.filter { it.isNotEmpty() && it.isNotBlank() }.distinct().forEach { userSpecifiedTask ->
                        val nextTask = project.tasks.multiversTask("${versionTaskDescriptor}_$userSpecifiedTask") {
                            mustRunAfter(previousTask.name)
                            dependsOn(previousTask.name, userSpecifiedTask)
                            doLast {
                                println("running $name")
                            }
                        }.also {
                            println("nextTask = ${it.name}")
                        }
                        previousTask = nextTask
                    }

                    project.tasks.multiversTask(versionTaskDescriptor) {
                        mustRunAfter(previousTask.name)
                        dependsOn(previousTask.name)
                        doLast {
                            println("running $name")
                            println("top level variant depends on ${previousTask.name}")
                        }
                    }.also {
                        println("topLevelVersionTask = ${it.name}")
                        topLevelVariantTasks.add(it)
                    }
                }

                project.tasks.multiversTask(variant.name) {
                    dependsOn(*topLevelVariantTasks.map { it.name }.toTypedArray())
                    doLast {
                        println("running $name")
                    }
                }.also {
                    variantTasks.add(it)
                    println("variantTask = ${it.name}")
                }
            }

            project.tasks.multiversTask {
                dependsOn(*variantTasks.map { it.name }.toTypedArray())
                doLast {
                    println("running $name")
                }
            }.also {
                println("multiversTask = ${it.name}")
            }

        }
    }

}



