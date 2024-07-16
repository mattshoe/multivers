package io.github.mattshoe.shoebox

class VariantRange(
    internal var range: Range
): VariantSpecifier {
    internal var exclusions = mutableListOf<String>()
    internal var tasks: MutableList<String> = mutableListOf()

    /**
     * **Define one or more Gradle tasks that will be run against this range of versions.**
     *
     * #### Priority and Order of Execution:
     * Tasks defined here will always be run ***after*** any tasks defined by [runGradleTasksOnAllVariants][MultiVersExtension.runGradleTasksOnAllVariants]
     * and ***after*** any tasks defined by outer scopes, such as [dependency.runGradleTasks][DependencyVariant.runGradleTasks]. The tasks will be run in the order they are passed.
     *
     * @param tasks The Gradle tasks you wish to run against this range of versions.
     */
    override fun runGradleTasks(vararg tasks: String) {
        this.tasks.addAll(tasks)
    }

    /**
     * **Use a regular expression to exclude versions in this [range][DependencyVariant.range].**
     *
     * The given regular expression will be evaluated against every version in this range. If the regular
     * expression matches the version string, then it will be excluded from the MultiVers process.
     *
     * @param regex One or more regular expressions to be evaluated against all available versions of this dependency.
     */
    fun exclude(regex: String) {
        exclusions.add(regex)
    }
}

