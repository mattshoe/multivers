package io.github.mattshoe.shoebox

import io.github.mattshoe.shoebox.util.multiversTask
import io.github.mattshoe.shoebox.util.runGradleCommand
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSetContainer
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class VersionVariantProcessor {

    companion object {
        private val requiredFirstTasks = listOf("clean", "build")
    }

    fun processVariants(
        project: Project,
        variantAggregations: List<VariantAggregation>
    ) {
        val taskBuilder = MultiversTaskBuilder(project)
        val variantTasks = mutableSetOf<Task>()

        variantAggregations.forEach { variant ->
            val topLevelVersionTasks = buildVersionSpecificTasks(project, variant)
            variantTasks.add(
                // Build this variant's top-level tasks
                taskBuilder.task(
                    variant.name,
                    *topLevelVersionTasks.map { it.name }.toTypedArray()
                )
            )
        }

        // build the multivers task to run all tasks
        taskBuilder.task(
           dependsOn = variantTasks.map { it.name }.toTypedArray()
        )
    }

    private fun buildVersionSpecificTasks(
        project: Project,
        variant: VariantAggregation
    ): List<Task> {
        return variant.versions.get().map { (version, versionSpecificTasks) ->
            val versionTaskName = "${variant.name}_v${variant.sanitizeString(version)}"
            val configurationName = "${versionTaskName}Implementation"
            requiredFirstTasks.forEach {
                if (versionSpecificTasks.firstOrNull() == it)
                    versionSpecificTasks.removeFirst()
            }

            project.tasks.multiversTask(versionTaskName) {
                var startTime: Long = 0L

                doFirst {
                    startTime = System.nanoTime()
                    val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer
                    val mainSourceSet = sourceSets.getByName("main").output.classesDirs

                    project.configurations.create(configurationName) {
                        isCanBeConsumed = false
                        isCanBeResolved = true
                        extendsFrom(
                            project.configurations.getByName("implementation"),
                            project.configurations.getByName("compileOnly"),
                            project.configurations.getByName("api")
                        )
                    }

                    project.dependencies.constraints.add(configurationName, variant.module) {
                        version {
                            strictly(version)
                        }
                    }
                }

                doLast {
                    project.runGradleCommand("clean")
                    project.runGradleCommand("build", "--refresh-dependencies")

                    versionSpecificTasks
                        .map { it.trim() }
                        .filter { it.isNotEmpty() && it.isNotBlank() }
                        .distinct()
                        .forEach {
                            project.runGradleCommand(it)
                        }

                    val duration = (System.nanoTime() - startTime).toDuration(DurationUnit.NANOSECONDS)
                    println("Task $name completed in $duration")
                }
            }
        }
    }
}