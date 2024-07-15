package io.github.mattshoe.shoebox

import io.github.mattshoe.shoebox.util.greaterThanEqualTo
import io.github.mattshoe.shoebox.util.lessThan
import org.apache.maven.artifact.versioning.ComparableVersion
import org.gradle.api.Project

class MultiVersVariantAggregator {
    fun aggregateVersionVariants(
        project: Project,
        extension: MultiVersExtension,
        resolver: Resolver
    ): List<VariantAggregation> {
        val variantAggregations = mutableListOf<VariantAggregation>()

        extension.dependencyVariants.forEach { dependencyVariant ->
            val variantAggregation = VariantAggregation(
                dependencyVariant.group,
                dependencyVariant.artifact
            )
            variantAggregations.add(variantAggregation)

            dependencyVariant.variantVersions.forEach {
                println("aggregating version: ${it.version}")
                variantAggregation.addVersionData(it.version, extension.tasks)
                variantAggregation.addVersionData(it.version, dependencyVariant.tasks)
                variantAggregation.addVersionData(it.version, it.tasks)
            }

            val allVersions = resolver.allAvailableVersions(
                project,
                dependencyVariant.group,
                dependencyVariant.artifact
            )

            dependencyVariant.variantRanges.forEach { rangeData ->
                val rangeStart = ComparableVersion(rangeData.range.start)
                val rangeEnd = ComparableVersion(rangeData.range.end)

                allVersions.filter {
                    val version = ComparableVersion(it)
                    version.greaterThanEqualTo(rangeStart) && version.lessThan(rangeEnd)
                }.forEach {
                    variantAggregation.addVersionData(it, extension.tasks)
                    variantAggregation.addVersionData(it, dependencyVariant.tasks)
                    variantAggregation.addVersionData(it, rangeData.tasks)
                }
            }
        }

        return variantAggregations
    }
}