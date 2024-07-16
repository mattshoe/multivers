package io.github.mattshoe.shoebox

import io.github.mattshoe.shoebox.util.EMPTY_STRING

class DependencyVariant(
    internal var group: String = EMPTY_STRING,
    internal var artifact: String = EMPTY_STRING
): VariantSpecifier {
    internal var tasks = mutableListOf<String>()
    internal var variantVersions = mutableListOf<VariantVersion>()
    internal var variantRanges = mutableListOf<VariantRange>()
    internal var variantMatchers = mutableListOf<VariantMatcher>()
    internal var variantExclusions = mutableListOf<Regex>()

    /**
     * **Define one or more Gradle tasks that will be run against each version of this [dependency][MultiVersExtension.dependency].**
     *
     * #### Priority and Order of Execution:
     * Tasks defined here will always be run ***after*** any tasks defined by [runGradleTasksOnAllVariants][MultiVersExtension.runGradleTasksOnAllVariants]
     * but ***before*** any other tasks nested deeper inside the dependency configuration. The tasks will be run in the order they are passed.
     *
     * @param tasks The Gradle tasks you wish to run against each version of this [dependency][MultiVersExtension.dependency].
     */
    override fun runGradleTasks(vararg tasks: String) {
        this.tasks.addAll(tasks)
    }

    /**
     * **Define one specific version which you'd like to swap out with the current version defined in your build.gradle.**
     *
     * @param value The version string as defined in the dependency's Maven coordinates.
     * @param configuration The configuration for this version's tasks.
     */
    fun version(value: String, configuration: VariantVersion.() -> Unit = {}) {
        variantVersions.add(
            VariantVersion(value).apply(configuration)
        )
    }

    /**
     * **Use one or more regular expressions to match versions of this [dependency][MultiVersExtension.dependency].**
     *
     * The given regular expressions will be evaluated against every available version in this dependency's
     * Maven repository. If the regular expression matches the version string, then it will be included in the MultiVers
     * process.
     *
     * @param regex One or more regular expressions to be evaluated against all available versions of this dependency.
     */
    fun match(vararg regex: String, configuration: VariantMatcher.() -> Unit = {}) {
        variantMatchers.add(
            VariantMatcher(regex.toList().map { Regex(it) }).apply(configuration)
        )
    }

    /**
     * **Use one or more regular expressions to exclude versions of this [dependency][MultiVersExtension.dependency].**
     *
     * The given regular expressions will be evaluated against every available version in this dependency's
     * Maven repository. If the regular expression matches the version string, then it will be excluded from
     * the MultiVers process.
     *
     * ***Any matches for this method are FINAL and will ALWAYS be excluded, regardless of whether they are included
     * by other scopes!***
     *
     * @param regex One or more regular expressions to be evaluated against all available versions of this dependency.
     */
    fun exclude(vararg regex: String) {
        variantExclusions.addAll(
            regex.toList().map { Regex(it) }
        )
    }

    /**
     * **Define a range of versions to use for the MultiVers process.**
     *
     * Any versions within the specified range will be included in the MultiVers process.
     * If you need to exclude specific versions from this range, the nested scope will allow you to execute
     * an exclude method.
     *
     * ***Note that the [end] parameter is exclusive! This will allow you to specify UP TO certain versions without
     * including them.***
     *
     * @param start The start of the range
     * @param end The exclusive end of the range.
     * @param configuration The configuration for this range's tasks.
     */
    fun range(start: String, end: String, configuration:  VariantRange.() -> Unit = {}) {
        variantRanges.add(
            VariantRange(
                Range(start, end)
            ).apply(configuration)
        )
    }

}