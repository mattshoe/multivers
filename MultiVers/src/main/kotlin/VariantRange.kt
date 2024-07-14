package io.github.mattshoe.shoebox

class VariantRange(
    var range: Range = Range.EMPTY
): VariantSpecifier {
    internal var exclusions = mutableListOf<String>()
    internal var tasks: MutableList<String> = mutableListOf()

    override fun runGradleTasks(vararg tasks: String) {
        this.tasks.addAll(tasks)
    }

    fun exclude(pattern: String) {
        exclusions.add(pattern)
    }
}