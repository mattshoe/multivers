package io.github.mattshoe.shoebox

import io.github.mattshoe.shoebox.util.EMPTY_STRING

class VariantVersion(
    var version: String = EMPTY_STRING,
    internal var tasks: MutableList<String> = mutableListOf()
) {
    fun runGradleTasks(vararg tasks: String) {
        this.tasks.addAll(tasks)
    }
}