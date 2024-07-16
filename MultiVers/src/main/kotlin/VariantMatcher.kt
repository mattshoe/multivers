package io.github.mattshoe.shoebox

class VariantMatcher(
    internal val patterns: List<Regex>
): VariantSpecifier {
    internal val tasks = mutableListOf<String>()

    /**
     * **Define one or more Gradle tasks that will be run against any versions matched by the specified regular expressions.**
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
}