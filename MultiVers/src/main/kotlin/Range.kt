package io.github.mattshoe.shoebox

class Range(
    val start: String,
    val end: String
) {
    companion object {
        var EMPTY = Range("", "")
    }
}