package io.github.mattshoe.shoebox

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class VariantAggregation(
    val group: String,
    val artifactId: String,
    val versions: MutableMap<String, MutableList<String>> = mutableMapOf()
) {
    private val versionMutex = Mutex()

    val taskName by lazy { "multivers${artifactId.replaceFirstChar { it.titlecase() }}" }

    suspend fun addVersionData(version: String, tasks: List<String>) {
        versionMutex.withLock {
            if (this.versions.contains(version)) {
                this.versions[version]?.addAll(tasks)
            } else {
                this.versions[version] = tasks
            }
        }
    }

}