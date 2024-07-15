package io.github.mattshoe.shoebox.util

import io.github.mattshoe.shoebox.MultiVersPlugin.Companion.GRADLE_GROUP
import io.github.mattshoe.shoebox.MultiVersPlugin.Companion.isWindows
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.apache.maven.artifact.versioning.ComparableVersion
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import kotlin.coroutines.CoroutineContext

const val EMPTY_STRING = ""

fun ComparableVersion.greaterThanEqualTo(other: ComparableVersion): Boolean {
    return compareTo(other) >= 0
}

fun ComparableVersion.lessThan(other: ComparableVersion): Boolean {
    return compareTo(other) < 0
}

fun TaskContainer.multiversTask(name: String = "", action: Task.() -> Unit): Task {

    return register("multivers${name.trim().replaceFirstChar { it.titlecaseChar() }}") {
        group = GRADLE_GROUP
        action()
    }.get()
}

fun Project.runGradleCommand(vararg commands: String) {
    exec {
        workingDir = project.rootDir
        executable =
            if (isWindows)
                "gradlew.bat"
            else
                "./gradlew"
        args = commands.toList()
    }
}