package io.github.mattshoe.shoebox

import io.github.mattshoe.shoebox.util.EMPTY_STRING

class VariantVersion(
    var version: String = EMPTY_STRING,
    var tasks: MutableList<String> = mutableListOf()
)