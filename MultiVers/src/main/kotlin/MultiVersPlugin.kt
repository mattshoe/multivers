package io.github.mattshoe.shoebox

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class MultiVersPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create<MultiVersExtension>("multiVers")

        project.afterEvaluate {
//            val extension = project.extensions.getByType(MultiVersExtension::class.java)
//            val dependencies = extension.dependencies
//            val tasksToRun = extension.tasksToRun
//            val runAllPermutations = extension.runAllPermutations
//            val specificCombinations = extension.specificCombinations
//
//            val permutations = if (runAllPermutations) {
//                dependencies.values.flatten().combinations()
//            } else {
//                specificCombinations
//            }
//
//            val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer
//
//            permutations.forEach { permutation ->
//                val configurationName = permutation.joinToString("") { "${it.dependency.name}${it.version}" }
//                if (configurationName.isNotEmpty()) {
//                    val sourceSetName = configurationName
//                    val compileTaskName = "compile$configurationName"
//                    val testTaskName = "test$configurationName"
//
//                    project.configurations.create(configurationName)
//                    permutation.forEach {
//                        project.dependencies {
//                            add(configurationName, "${it.dependency.group}:${it.dependency.name}:${it.version}")
//                        }
//                    }
//
//                    val sourceSet = sourceSets.create(sourceSetName) {
//                        compileClasspath += project.configurations[configurationName]
//                        runtimeClasspath += project.configurations[configurationName]
//                    }
//
//                    project.tasks.register<JavaCompile>(compileTaskName) {
//                        source = sourceSet.allSource
//                        classpath = sourceSet.compileClasspath
//                        destinationDir = project.file("${project.buildDir}/classes/kotlin/$sourceSetName")
//                    }
//
//                    project.tasks.register<Test>(testTaskName) {
//                        testClassesDirs = sourceSet.output.classesDirs
//                        classpath = sourceSet.runtimeClasspath
//                        dependsOn(compileTaskName)
//                    }
//
//                    tasksToRun.forEach { taskName ->
//                        project.tasks.register("${taskName}$configurationName") {
//                            dependsOn(taskName, compileTaskName)
//                        }
//                    }
//                }
//            }
        }
    }

    private fun <T> List<T>.combinations(): List<List<T>> {
        if (isEmpty()) return listOf(emptyList())
        val head = first()
        val tail = drop(1)
        return tail.combinations().flatMap { combination ->
            listOf(combination, combination + head)
        }
    }
}

open class MultiVersExtension(
    internal var variants: MutableList<Variant> = mutableListOf(),
    internal var tasks: MutableList<String> = mutableListOf("build")
) {
    fun variant(
        module: String,
        configure: Variant.() -> Unit
    ) {
        val (group, artifact) = module.split(":")
        variants.add(
            Variant(group, artifact).apply(configure)
        )
    }

    fun runGradleTasksOnAll(vararg tasks: String) {
        this.tasks.clear()
        this.tasks.addAll(tasks)
    }
}

data class VersionedDependency(
    val dependency: DependencyInfo,
    val version: String
)

data class DependencyInfo(
    val group: String,
    val name: String
)

class Variant(
    var group: String = EMPTY_STRING,
    var artifact: String = EMPTY_STRING
) {
    private var tasks: MutableList<String> = mutableListOf()
    private var variationVersions: MutableList<VariationVersion> = mutableListOf()
    private var variationRanges: MutableList<VariationRange> = mutableListOf()

    fun runGradleTasks(vararg tasks: String) {
        this.tasks.addAll(tasks)
    }

    fun version(value: String, configuration: VariationVersion.() -> Unit = {}) {
        variationVersions.add(
            VariationVersion(value).apply(configuration)
        )
    }

    fun range(start: String, end: String, configuration: VariationRange.() -> Unit = {}) {
        variationRanges.add(
            VariationRange().apply(configuration)
        )
    }

}

class VariationVersion(
    var version: String = EMPTY_STRING,
    var tasks: List<String> = emptyList()
)

class VariationRange(
    var range: Range = EMPTY_RANGE,
    var tasks: List<String> = emptyList()
)


class Range(
    val start: String,
    val end: String
)

private val EMPTY_RANGE = Range("", "")
private const val EMPTY_STRING = ""



