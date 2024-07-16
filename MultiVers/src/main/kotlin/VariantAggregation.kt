package io.github.mattshoe.shoebox

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicReference

typealias VersionTaskMap = MutableMap<String, MutableList<String>>

class VariantAggregation(
    val group: String,
    val artifactId: String,
    val versions: AtomicReference<VersionTaskMap> = AtomicReference(mutableMapOf())
) {
    val name: String = buildString {
        append(sanitizeString(group))
        append(sanitizeString(artifactId))
    }

    val module: String = "$group:$artifactId"

    fun gav(version: String): String = "$module:$version"

    fun mutateVersions(mutator: VersionTaskMap.() -> Unit) {
        versions.getAndUpdate {
            it.apply(mutator)
        }
    }

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

    fun removeVersion(version: String) {
        this.versions.getAndUpdate {
            it.apply {
                remove(version)
            }
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