# MultiVers Gradle Plugin

The MultiVers Gradle plugin is designed to simplify the process of testing your project's compatibility with multiple 
versions of its dependencies. By automating the execution of tasks across various dependency versions and ranges, 
MultiVers helps ensure that your project remains robust and adaptable to changes in its dependency ecosystem. 
This is especially valuable for maintaining stability and compatibility in projects that rely on evolving third-party 
libraries.

## Key Features

1. **Automated Multi-Version Testing**:
    - Run specified Gradle tasks across multiple versions of your dependencies automatically.
    - Ensures comprehensive validation by covering all specified versions, helping identify compatibility issues early.

2. **Flexible Version Specification**:
    - Define both specific versions and version ranges for your dependencies.
    - The end of a version range is exclusive, allowing you to easily specify ranges up to a breaking release without including the breaking change.

3. **Task Customization for Versions**:
    - Configure different tasks for different versions or ranges, providing granular control over your testing process.
    - Allows for specific tasks to be run only on certain versions, adapting to various testing needs.

4. **Exclusion Patterns**:
    - Use regex patterns to exclude specific versions from a range.
    - Helpful for skipping unstable versions, such as release candidates (RC) or snapshots.

5. **Centralized Task Execution**:
    - Specify tasks to run on all versions and ranges of dependencies, ensuring consistent testing across all configurations.
    - Simplifies the setup by reducing the need for repetitive task definitions.

## Use Cases

1. **Backward Compatibility Testing**: Ensure your project works with older versions of dependencies, preventing issues when users have not upgraded to the latest versions.
2. **Future-proofing**: Test compatibility with future or newer versions of dependencies to catch potential issues early.
3. **KSP Plugin with No Direct Dependency**: For a Kotlin Symbol Processing (KSP) plugin that generates code for libraries like Kotlin Coroutines, you can ensure compatibility with a range of coroutine library versions.
4. **Comprehensive CI/CD**: Integrate with CI/CD pipelines to automatically validate multiple dependency versions, ensuring robust software delivery.

## Getting Started

### Adding the Plugin

Add the MultiVers plugin to your project in your `build.gradle.kts` file:

```kotlin
plugins {
    id("shoebox.multivers")
}
```

### Configuration
```kotlin
multivers {
    runGradleTasksOnAllVariants("testDebugUnitTestCoverage", "check")
    
    dependency("io.some.group:SomeArtifact") {
        range("3.2.0", "4.0.0")
    }

    dependency("com.foo.bar:DerpDerp") {
        version("2.5.3")
    }

    dependency("com.group.woohoo:WhatAnArtifact") {
        runGradleTasks("assemble")
        range("3.2.0", "4.0.0") {
            exclude(".*-RC")
        }
    }

    dependency("com.flerp.meep:Yeet") {
        version("2.2.5") {
            runGradleTasks("someGradleTask")
        }
        range("1.0.0", "2.0.0") {
            exclude(".*-SNAPSHOT")
            runGradleTasks("check")
        }
    }
}
```

### Syntax

Configure the plugin using the `multivers` block in your `build.gradle.kts` file:

```kotlin
multivers {
    runGradleTasksOnAllVariants("test") // runs ./gradlew test against ALL version variants defined below.

    // Define the "group:artifiact" for which you want to vary dependencies
    dependency("com.foo.bar:DerpDerp") {
        runGradleTasks("assemble") // runs ./gradlew assemble against all com.foo.bar:DerpDerp versions
        version("2.5.3")
        range("0.0.0", "2.0.0") {
            exclude(".*-RC") // exclude any versions whose pattern matches this regex
            runGradleTasks("check") // run ./gradlew check against all versions in this range
        }
    }

    dependency("com.flerp.meep:Yeet") {
        version("0.0.3") {
            runGradleTasks("assemble") // run ./gradlew assemble against version "0.0.3" only
        }
        range("1.0.0", "2.0.0") {
            exclude(".*-SNAPSHOT")
            runGradleTasks("check")
        }
    }
}
```

### Public Methods

#### runGradleTasksOnAllVariants(vararg tasks: String)

This method defines a list of Gradle tasks that will be run against every single variation of every single dependency.
So any gradle tasks defined here will be executed by default on every single variation of every dependency.

These tasks are always executed first for each version variation.

#### runGradleTasks(vararg tasks: String)

This method works similarly to `runGradleTasksOnAllVariants`, but it defines gradle tasks in which to be run against the
most immediate scope surrounding the method. For example, invoking `runGradleTasks` directly inside the `dependency` 
lambda will run those tasks against every version for that dependency. However, specifying `runGradleTasks` inside the
`version` lambda will only run those gradle tasks against that specific version. Similarly, using `runGradleTasks` 
inside the `range` lambda will cause the specified tasks to be run against all versions in that range.

#### dependency(module: String)

This method defines a particular artifact whose versions you wish to vary. The lambda provided gives you access to 
customization functions outlined below. 

#### version(value: String)

This method is always a child of the `dependency` lambda. It defines one specific version for the dependency to run 
against. You can have any number of `version` invocations nested within a `dependency` block. It also provides a lambda 
which allows you to specify gradle tasks for that specific version.

#### range(start: String, end: String)

This method is very similar to the `version` method, except that it specifies a range of versions rather than a single 
version. You can have any number of `range` blocks inside your `dependency` block.

Please note that the "end" of the range is exclusive! This means you can specify `range("1.0.0", "2.0.0")` and version
2.0.0 will NOT be included. This allows you to easily specify versions up to but not including major releases, etc.

#### exclude(regex: String)

This method is only available inside a `range` block, and any versions in that range whose value matches the provided 
regex will be EXCLUDED from evaluation.
