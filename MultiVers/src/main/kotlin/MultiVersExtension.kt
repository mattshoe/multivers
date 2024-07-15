package io.github.mattshoe.shoebox

open class MultiVersExtension(
    internal var dependencyVariants: MutableList<DependencyVariant> = mutableListOf(),
    internal var tasks: MutableList<String> = mutableListOf()
) {
    var windows = false

    fun dependency(
        module: String,
        configure: DependencyVariant.() -> Unit
    ) {
        val (group, artifact) = module.trim().split(":")
        dependency(group, artifact, configure)
    }

    fun dependency(
        group: String,
        artifactId: String,
        configure: DependencyVariant.() -> Unit
    ) {
        dependencyVariants.add(
            DependencyVariant(group, artifactId).apply(configure)
        )
    }

    fun runGradleTasksOnAllVariants(vararg tasks: String) {
        this.tasks.clear()
        this.tasks.addAll(tasks)
    }
}