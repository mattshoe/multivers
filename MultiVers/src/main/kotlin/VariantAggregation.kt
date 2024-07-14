package io.github.mattshoe.shoebox

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicReference

class VariantAggregation(
    val group: String,
    val artifactId: String,
    val versions: AtomicReference<MutableMap<String, MutableList<String>>> = AtomicReference(mutableMapOf())
) {
    val name: String = buildString {
        append(sanitizeString(group))
        append(sanitizeString(artifactId))
    }

    val module: String = "$group:$artifactId"

    fun gav(version: String): String = "$module:$version"

    fun addVersionData(version: String, tasks: List<String>) {
        this.versions.getAndUpdate { versionsMap ->
            if (versionsMap.contains(version)) {
                versionsMap[version]?.addAll(tasks)
            } else {
                versionsMap[version] = tasks.toMutableList()
            }

            versionsMap
        }

    }

    fun sanitizeString(text: String) = buildString {
        text
            .split(".", "-")
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.isNotBlank() }
            .forEach {
                append(it.replaceFirstChar { it.titlecaseChar() })
            }
    }

}