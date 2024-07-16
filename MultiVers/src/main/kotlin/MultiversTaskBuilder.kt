package io.github.mattshoe.shoebox

import io.github.mattshoe.shoebox.MultiVersPlugin.Companion.isWindows
import io.github.mattshoe.shoebox.util.multiversTask
import org.gradle.api.Project
import org.gradle.api.Task

class MultiversTaskBuilder(private val project: Project) {

    fun task(
        name: String = "",
        vararg dependsOn: String,
        doLast: Task.() -> Unit = {  }
    ): Task {
        return project.tasks.multiversTask(name) {
            dependsOn(*dependsOn)
            doLast {
                println("Running Task: ${this.name}")
                doLast()
            }
        }
    }

    fun chain(
        previous: Task,
        name: String,
        vararg dependsOn: String,
        doFirst: Task.() -> Unit = {},
        doLast: Task.() -> Unit = {}
    ): Task {
        return project.tasks.multiversTask(name) {
            mustRunAfter(previous.name)
            dependsOn(previous.name, *dependsOn)
            doFirst {
                doFirst()
            }
            doLast {
                println("\tRunning Task: $name \n\tExpected Previous Task: ${previous.name}")
                doLast()
            }
        }
    }
}