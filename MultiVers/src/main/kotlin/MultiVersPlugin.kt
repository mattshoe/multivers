package io.github.mattshoe.shoebox

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class MultiVersPlugin : Plugin<Project> {
    companion object {
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        const val GRADLE_GROUP = "MultiVers"
    }

    private val aggregator = MultiVersVariantAggregator()
    private val variantProcessor = VersionVariantProcessor()
    private val resolver = Resolver()

    override fun apply(project: Project) {
        val extension = project.extensions.create<MultiVersExtension>("multivers")

        project.afterEvaluate {
            variantProcessor.processVariants(
                project,
                aggregator.aggregateVersionVariants(
                    project,
                    extension,
                    resolver
                )
            )
        }
    }



}



