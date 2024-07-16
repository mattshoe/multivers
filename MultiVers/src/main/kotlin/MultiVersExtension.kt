package io.github.mattshoe.shoebox

open class MultiVersExtension(
    internal var dependencyVariants: MutableList<DependencyVariant> = mutableListOf(),
    internal var tasks: MutableList<String> = mutableListOf()
) {
    var windows = false

    /**
     * **Define a dependency that you'd like to swap out with a range of different versions.**
     *
     * The [module] argument should be specified as `"groupId:artifactId"`.
     *
     * For more information on maven coordinates, please refer to
     * [Maven Coordinates](https://maven.apache.org/pom.html#maven-coordinates)
     *
     * @param module The dependency's Group and ArtifactId, in the form "group:artifactId"
     * @param configuration A lambda which provides methods to define the version matching.
     */
    fun dependency(
        module: String,
        configuration: DependencyVariant.() -> Unit
    ) {
        val (group, artifact) = module.trim().split(":")
        dependency(group, artifact, configuration)
    }

    /**
     * **Define a dependency that you'd like to swap out with a range of different versions.**
     *
     * For more information on maven coordinates, please refer to
     * [Maven Coordinates](https://maven.apache.org/pom.html#maven-coordinates)
     *
     * @param groupId The dependency's `groupId` of its Maven coordinates.
     * @param artifactId The dependency's `artifactId` of its Maven coordinates.
     * @param configuration A lambda which provides methods to define the version matching.
     */
    fun dependency(
        groupId: String,
        artifactId: String,
        configuration: DependencyVariant.() -> Unit
    ) {
        dependencyVariants.add(
            DependencyVariant(groupId, artifactId).apply(configuration)
        )
    }

    /**
     * **Define one or more Gradle tasks that will be run against EVERY different version of EVERY dependency.**
     *
     *  #### Priority and Order of Execution:
     * Tasks defined here will always be run ***before*** any other tasks nested deeper inside the dependency configuration.
     * The tasks will be run in the order they are passed.
     *
     * @param tasks The Gradle tasks you wish to run against every single version of every single dependency.
     */
    fun runGradleTasksOnAllVariants(vararg tasks: String) {
        this.tasks.clear()
        this.tasks.addAll(tasks)
    }
}