package io.github.mattshoe.shoebox

import kotlinx.coroutines.*
import org.apache.maven.artifact.versioning.ComparableVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class MultiVersPlugin : Plugin<Project> {
    override fun apply(project: Project) = runBlocking {
        val resolver = Resolver(project)
        val extension = project.extensions.create<MultiVersExtension>("multivers")

        project.afterEvaluate {

            with (extension) {
                val variantAggregations = mutableListOf<VariantAggregation>()
                dependencyVariants.forEach { dependencyVariant ->
                    val variantAggregation = VariantAggregation(
                        dependencyVariant.group,
                        dependencyVariant.artifact
                    )

                    val versionsJob = launch(Dispatchers.Default) {
                        dependencyVariant.variantVersions.forEach {
                            yield()
                            variantAggregation.addVersionData(it.version, it.tasks)
                        }
                    }

                    val rangesJob = launch(Dispatchers.Default) {
                        val allVersions = resolver.allAvailableVersions(
                            dependencyVariant.group,
                            dependencyVariant.artifact
                        )

                        dependencyVariant.variantRanges.forEach { rangeData ->
                            val rangeStart = ComparableVersion(rangeData.range.start)
                            val rangeEnd = ComparableVersion(rangeData.range.end)

                            allVersions.filter {
                                yield()
                                val version = ComparableVersion(it)
                                version.greaterThanEqualTo(rangeStart) && version.lessThan(rangeEnd)
                            }.forEach {
                                yield()
                                variantAggregation.addVersionData(it, rangeData.tasks)
                            }
                        }
                    }

                    launch {
                        versionsJob.join()
                        rangesJob.join()

                        variantAggregations.forEach { variantAggregation ->
                            project.tasks.register(variantAggregation.taskName) {
                                doLast {
                                    variantAggregation.versions.forEach { (version, tasks) ->
                                        val affectedConfigurations = mutableListOf<Configuration>()

                                        project.configurations.forEach { configuration ->
                                            configuration.resolutionStrategy.eachDependency {
                                                if (requested.group == variantAggregation.group && requested.name == variantAggregation.artifactId) {
                                                    useVersion(version)
                                                    affectedConfigurations.add(configuration)
                                                }
                                            }
                                        }

                                        // Force re-resolution of necessary configurations
                                        affectedConfigurations.forEach { configuration ->
                                            configuration.resolve()
                                        }

                                        tasks.forEach {
                                            project.exec {
                                                commandLine("gradle", it)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

fun ComparableVersion.greaterThanEqualTo(other: ComparableVersion): Boolean {
    return compareTo(other) >= 0
}

fun ComparableVersion.lessThan(other: ComparableVersion): Boolean {
    return compareTo(other) < 0
}

