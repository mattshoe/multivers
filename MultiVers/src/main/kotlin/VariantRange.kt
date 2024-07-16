package io.github.mattshoe.shoebox

class VariantRange(
    internal var range: Range
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

class VariantMatcher(
    internal val patterns: List<Regex>
): VariantSpecifier {
    internal val tasks = mutableListOf<String>()
    override fun runGradleTasks(vararg tasks: String) {
        this.tasks.addAll(tasks)
    }
}

class VariantExclusion(
    internal val patterns: List<Regex>
): VariantSpecifier {
    internal val tasks = mutableListOf<String>()
    override fun runGradleTasks(vararg tasks: String) {
        this.tasks.addAll(tasks)
    }
}