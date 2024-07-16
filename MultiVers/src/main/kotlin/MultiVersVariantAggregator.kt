package io.github.mattshoe.shoebox

import io.github.mattshoe.shoebox.util.greaterThanEqualTo
import io.github.mattshoe.shoebox.util.lessThan
import io.github.mattshoe.shoebox.util.log
import org.apache.maven.artifact.versioning.ComparableVersion
import org.gradle.api.Project

class MultiVersVariantAggregator {

    fun aggregateVersionVariants(
        project: Project,
        extension: MultiVersExtension,
        dependencyInspector: DependencyInspector
    ): List<VariantAggregation> {
        val variantAggregations = mutableListOf<VariantAggregation>()

        extension.dependencyVariants.forEach { dependencyVariant ->
            log("Aggregating Dependency -- ${dependencyVariant.group}:${dependencyVariant.artifact}")
            val variantAggregation = VariantAggregation(
                dependencyVariant.group,
                dependencyVariant.artifact
            ).also {
                variantAggregations.add(it)
            }

            dependencyInspector.allAvailableVersions(
                project,
                dependencyVariant.group,
                dependencyVariant.artifact
            ).forEach { version ->
                log("\tAggregating Version -- $version")
                dependencyVariant.variantMatchers.forEach { matcher ->
                    matcher.patterns.forEach { regex ->
                        if (version.matches(regex)) {
                            log("\tMatcher succeeded: ${regex.pattern}")
                            variantAggregation.addVersionData(version, matcher.tasks)
                        }
                    }
                }

                dependencyVariant.variantRanges.forEach { rangeData ->
                    val rangeStart = ComparableVersion(rangeData.range.start)
                    val rangeEnd = ComparableVersion(rangeData.range.end)
                    val exclusions = rangeData.exclusions.map { Regex(it) }

                    if (version.isEligibleForValidation(rangeStart, rangeEnd, exclusions)) {
                        log("Range Matched: $version")
                        variantAggregation.addVersionData(version, extension.tasks)
                        variantAggregation.addVersionData(version, dependencyVariant.tasks)
                        variantAggregation.addVersionData(version, rangeData.tasks)
                    }
                }
            }

            dependencyVariant.variantVersions.forEach {
                log("Specific version added: $it")
                variantAggregation.addVersionData(it.version, extension.tasks)
                variantAggregation.addVersionData(it.version, dependencyVariant.tasks)
                variantAggregation.addVersionData(it.version, it.tasks)
            }

            val excludedVersions = variantAggregation.versions.get()
                .keys
                .filter { version ->
                    val isExcluded = dependencyVariant.variantExclusions.any { exclusion ->
                        exclusion.patterns.any { regex ->
                            version.matches(regex).also {
                                if (it) {
                                    log("Excluding $version based on ${regex.pattern}")
                                }
                            }
                        }
                    }
                    if (isExcluded)
                        log("Version $version excluded!")

                    isExcluded
                }

            excludedVersions.forEach {
                log("Excluded version: $it")
            }

            variantAggregation.mutateVersions {
                excludedVersions.forEach {
                    remove(it)
                }
            }
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