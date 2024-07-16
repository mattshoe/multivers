package io.github.mattshoe.shoebox

import io.github.mattshoe.shoebox.util.EMPTY_STRING

class DependencyVariant(
    var group: String = EMPTY_STRING,
    var artifact: String = EMPTY_STRING
): VariantSpecifier {
    internal var tasks = mutableListOf<String>()
    internal var variantVersions = mutableListOf<VariantVersion>()
    internal var variantRanges = mutableListOf<VariantRange>()
    internal var variantMatchers = mutableListOf<VariantMatcher>()
    internal var variantExclusions = mutableListOf<VariantExclusion>()

    override fun runGradleTasks(vararg tasks: String) {
        this.tasks.addAll(tasks)
    }

    fun version(value: String, configuration: VariantVersion.() -> Unit = {}) {
        variantVersions.add(
            VariantVersion(value).apply(configuration)
        )
    }

    fun match(vararg regex: String, configuration: VariantMatcher.() -> Unit = {}) {
        variantMatchers.add(
            VariantMatcher(regex.toList().map { Regex(it) }).apply(configuration)
        )
    }

    fun exclude(vararg regex: String, configuration: VariantExclusion.() -> Unit = {}) {
        variantExclusions.add(
            VariantExclusion(regex.toList().map { Regex(it) }).apply(configuration)
        )
    }

    fun range(start: String, end: String, configuration: VariantRange.() -> Unit = {}) {
        variantRanges.add(
            VariantRange(
                Range(start, end)
            ).apply(configuration)
        )
    }

}