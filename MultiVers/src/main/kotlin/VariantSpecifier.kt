package io.github.mattshoe.shoebox

interface VariantSpecifier {
    fun runGradleTasks(vararg tasks: String)
}