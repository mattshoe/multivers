package io.github.mattshoe.shoebox

import io.github.mattshoe.shoebox.util.multiversTask
import io.github.mattshoe.shoebox.util.runGradleCommand
import org.gradle.api.Project
import org.gradle.api.Task
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
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
            val topLevelVersionTasks = buildVersionSpecificTasks(project, variant, taskBuilder)
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
        variant: VariantAggregation,
        taskBuilder: MultiversTaskBuilder
    ): List<Task> {
        return variant.versions.get().map { (version, versionSpecificTasks) ->
            requiredFirstTasks.forEach {
                if (versionSpecificTasks.firstOrNull() == it)
                    versionSpecificTasks.removeFirst()
            }

            project.tasks.multiversTask("${variant.name}_v${variant.sanitizeString(version)}") {
                var startTime: Long = 0L
                doFirst {
                    startTime = System.nanoTime()
                    project.configurations.forEach { configuration ->
                        configuration.incoming.beforeResolve {
                            configuration.resolutionStrategy.force(variant.gav(version))
                        }
                    }
                }
                doLast {
                    project.rootProject.layout.buildDirectory.file("caches/modules-2").let {
                        println("Deleting: ${it.get().asFile.path}")
                        project.delete(it)
                    }
                    project.rootProject.layout.buildDirectory.file("caches/transforms-2").let {
                        println("Deleting: ${it.get().asFile.path}")
                        project.delete(it)
                    }

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
                    println("Task $name completed in ${duration}")
                }
            }
        }
    }
}