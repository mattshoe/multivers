package io.github.mattshoe.shoebox

class VariantRange(
    var range: Range = Range.EMPTY,
    var tasks: List<String> = emptyList(),
) {
    internal var exclusions = mutableListOf<String>()

    fun exclude(pattern: String) {
        exclusions.add(pattern)
    }
}