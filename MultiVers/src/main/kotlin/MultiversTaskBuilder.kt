package io.github.mattshoe.shoebox

import io.github.mattshoe.shoebox.MultiVersPlugin.Companion.isWindows
import io.github.mattshoe.shoebox.util.multiversTask
import org.gradle.api.Project
import org.gradle.api.Task

class MultiversTaskBuilder(private val project: Project) {

    fun forceVersionTask(
        name: String,
        gav: String
    ): Task {
        return project.tasks.multiversTask(name) {
            doFirst {
                project.configurations.forEach { configuration ->
                    configuration.incoming.beforeResolve {
                        configuration.resolutionStrategy.force(gav)
                    }
                }
            }
            doLast {
                project.rootProject.layout.buildDirectory.file("caches/modules-2").let {
                    println("Deleting: ${it.get().asFile.path}")
                    project.delete(it)
                }
                project.rootProject.layout.buildDirectory.file("caches/transforms-2").let {
                    println("Deleting: ${it.get().asFile.path}")
                    project.delete(it)
                }


            }
        }
    }

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
            dependsOn(previous.name)
            doFirst {
                doFirst()
            }
            doLast {
                project.exec {
                    workingDir = project.rootDir
                    executable =
                        if (isWindows)
                            "gradlew.bat"
                         else
                             "./gradlew"
                    args = dependsOn.toList()
                }
                println("\tRunning Task: $name \n\tExpected Previous Task: ${previous.name}")
                doLast()
            }
        }
    }
}