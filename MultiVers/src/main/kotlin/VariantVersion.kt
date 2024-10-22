package io.github.mattshoe.shoebox

import io.github.mattshoe.shoebox.util.EMPTY_STRING

class VariantVersion(
    var version: String,
    internal var tasks: MutableList<String> = mutableListOf()
): VariantSpecifier {

    /**
     * **Define one or more Gradle tasks that will be run against this specific version.**
     *
     * #### Priority and Order of Execution:
     * Tasks defined here will always be run ***after*** any tasks defined by [runGradleTasksOnAllVariants][MultiVersExtension.runGradleTasksOnAllVariants]
     * and ***after*** any tasks defined by outer scopes, such as [dependency.runGradleTasks][DependencyVariant.runGradleTasks]. The tasks will be run in the order they are passed.
     *
     * @param tasks The Gradle tasks you wish to run against this version.
     */
    override fun runGradleTasks(vararg tasks: String) {
        this.tasks.addAll(tasks)
    }
}