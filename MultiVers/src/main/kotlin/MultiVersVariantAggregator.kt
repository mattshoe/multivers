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
            val allVersions = resolver.allAvailableVersions(
                project,
                dependencyVariant.group,
                dependencyVariant.artifact
            )
            val variantAggregation = VariantAggregation(
                dependencyVariant.group,
                dependencyVariant.artifact
            ).also {
                variantAggregations.add(it)
            }

            allVersions.forEach { version ->
                dependencyVariant.variantMatchers.forEach { matcher ->
                    matcher.patterns.forEach { pattern ->
                        if (version.matches(pattern)) {
                            variantAggregation.addVersionData(version, matcher.tasks)
                        }
                    }
                }

                dependencyVariant.variantRanges.forEach { rangeData ->
                    val rangeStart = ComparableVersion(rangeData.range.start)
                    val rangeEnd = ComparableVersion(rangeData.range.end)
                    val exclusions = rangeData.exclusions.map { Regex(it) }

                    if (version.isEligibleForValidation(rangeStart, rangeEnd, exclusions)) {
                        variantAggregation.addVersionData(version, extension.tasks)
                        variantAggregation.addVersionData(version, dependencyVariant.tasks)
                        variantAggregation.addVersionData(version, rangeData.tasks)
                    }
                }
            }

            dependencyVariant.variantVersions.forEach {
                variantAggregation.addVersionData(it.version, extension.tasks)
                variantAggregation.addVersionData(it.version, dependencyVariant.tasks)
                variantAggregation.addVersionData(it.version, it.tasks)
            }

            val excludedVersions = variantAggregation.versions.get()
                .keys
                .filter { version ->
                    val isExcluded = dependencyVariant.variantExclusions.any { exclusion ->
                        exclusion.patterns.any { pattern ->
                            version.matches(pattern)
                        }
                    }
                    !isExcluded
                }

            variantAggregation.mutateVersions {
                excludedVersions.forEach {
                    remove(it)
                }
            }

//            variantAggregation.mutateVersions {
//                keys.forEach { version ->
//                    val isExcluded = dependencyVariant.variantExclusions.any { exclusion ->
//                        exclusion.patterns.any { pattern ->
//                            version.matches(pattern)
//                        }
//                    }
//                    if (isExcluded) {
//                        remove(version)
//                    }
//                }
//            }
        }

        return variantAggregations
    }

    private fun String.isEligibleForValidation(
        rangeStart: ComparableVersion,
        rangeEnd: ComparableVersion,
        exclusions: List<Regex>
    ): Boolean {
        val version = ComparableVersion(this)
        return version.greaterThanEqualTo(rangeStart)
                && version.lessThan(rangeEnd)
                && exclusions.none { regex ->
                    this.matches(regex)
                }
    }
}