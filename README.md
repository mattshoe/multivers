# MultiVers Gradle Plugin

The MultiVers Gradle plugin is designed to allow you to test your project's compatibility with a range of 
versions of its dependencies. By automating compatibility checks across various dependency ranges, 
MultiVers helps ensure that your project remains robust and adaptable to changes in its dependency ecosystem. 
This is especially valuable for maintaining stability and compatibility in projects that rely on evolving third-party 
libraries.

## Features

1. **Automate Multi-Version Testing**:
    - Run specified Gradle tasks across any range of versions for any number of dependencies automatically.
    - Ensures comprehensive validation by covering all specified versions, helping identify compatibility issues early.

2. **Flexible DSL**:
    - Define both specific versions and version ranges for your dependencies.
    - The end of a version range is exclusive, which allowing you to easily specify ranges up to a breaking release without including the breaking change.

3. **Task Customization for Versions**:
    - Configure different tasks for different versions or ranges, providing granular control over your testing process.
    - Allows for specific tasks to be run only on certain versions, adapting to various testing needs.

4. **Exclusion Patterns**:
    - Use regex patterns to exclude specific versions from a range.
    - Helpful for skipping unstable versions, such as release candidates (RC) or snapshots.

## Use Cases

1. **Backward Compatibility Testing**: Ensure your project works with older versions of dependencies, preventing issues when users have not upgraded to the latest versions.
2. **Future-proofing**: Test compatibility with future or newer versions of dependencies to catch potential issues early.
4. **Comprehensive CI/CD**: Integrate with CI/CD pipelines to automatically validate multiple dependency versions, ensuring robust software delivery.
3. **KSP Plugin**: Some Kotlin Symbol Processing (KSP) plugins may generate code for libraries like Kotlin Coroutines or Room, but not have a direct dependency themselves. This will allow you to ensure compatibility with a range of library versions.

## Quick Start

### Add the Plugin

Add the MultiVers plugin to your project in your `build.gradle.kts` file:

```kotlin
plugins {
    id("shoebox.multivers")
}
```

### Configure your variants
```kotlin
multivers {
    runGradleTasksOnAllVariants("test", "lint")
    
    dependency("io.some.group:SomeArtifact") {
        range("3.2.0", "4.0.0")
    }

    dependency("derp.foo.bar:DerpFoo") {
        match("^1\\..*") {
            runGradleTasks("check")
        }
        exclude(".*[a-zA-z].*")
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
            runGradleTasks("yourGradleTask")
        }
    }
}
```

### Run the gradle task!

There are several ways you could choose to run MultiVers validations:

1. Run validations for all version variations at once. The command below will run all dependency/version combinations and their associated tasks:
   - `./gradlew multivers`
2. Run validations for a specific dependency. Take a look at the `"io.some.group:SomeArtifact"` dependency above:
   - `./gradlew multiversIoSomeGroupSomeArtifact`
   - Note that any dashes and underscores in the GAV are removed.
3. Run validations for a specific dependency version. Imagine we want to run validations for `"io.some.group:SomeArtifact:3.8.10"`:
   - `./gradlew multiversIoSomeGroupSomeArtifact_v3810`


## How it works

Configure the plugin using the `multivers` block in your `build.gradle.kts` file:

```kotlin
multivers {
    runGradleTasksOnAllVariants("test") // runs ./gradlew test against ALL version variants defined below.

    // Define the module whose versions you wish to vary
    dependency("com.foo.bar:DerpDerp") {
        runGradleTasks("assemble") // runs ./gradlew assemble against all com.foo.bar:DerpDerp versions
        version("2.5.3")
        version("2.5.9")
        range("0.0.0", "2.0.0") {
            exclude(".*-RC") // exclude any versions whose pattern matches this regex
            runGradleTasks("check") // run ./gradlew check against all versions in this range
        }
    }

    dependency("derp.foo.bar:DerpFoo") {
        match("^1\\..*") { // Match any version that starts with "1."
            runGradleTasks("lint")
        }
        exclude(".*[a-zA-z].*") // exclude all unstable versions (alpha, beta, RC, etc)
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

#### `runGradleTasksOnAllVariants(vararg tasks: String)`

This method defines a list of Gradle tasks that will be run against every single variation of every single dependency.
So any gradle tasks defined here will be executed by default on every single variation of every dependency.

These tasks are always executed first for each version variation.

#### `runGradleTasks(vararg tasks: String)`

This method works similarly to `runGradleTasksOnAllVariants`, but it defines gradle tasks in which to be run against the
most immediate scope surrounding the method. For example, invoking `runGradleTasks` directly inside the `dependency` 
lambda will run those tasks against every version for that dependency. However, specifying `runGradleTasks` inside the
`version` lambda will only run those gradle tasks against that specific version. Similarly, using `runGradleTasks` 
inside the `range` lambda will cause the specified tasks to be run against all versions in that range.

#### `dependency(module: String)`

This method defines a particular artifact whose versions you wish to vary. The lambda provided gives you access to 
customization functions outlined below. 

#### `range(start: String, end: String)`

This method is very similar to the `version` method, except that it specifies a range of versions rather than a single 
version. You can have any number of `range` blocks inside your `dependency` block.

Please note that the "end" of the range is exclusive! This means you can specify `range("1.0.0", "2.0.0")` and version
2.0.0 will NOT be included. This allows you to easily specify versions up to but not including major releases, etc.

#### `match(vararg regex: String)`
The given regular expressions will be evaluated against every available version in the dependency's Maven repository. 
If the regular expression matches the version string, then it will be included in the MultiVers evaluation.

#### `version(value: String)`

This method is always a child of the `dependency` lambda. It defines one specific version for the dependency to run
against. You can have any number of `version` invocations nested within a `dependency` block. It also provides a lambda
which allows you to specify gradle tasks for that specific version.

#### `exclude(regex: String)`

This method is available in 2 places:
1. Inside a `range` block, any versions in that range whose value matches the provided regex will be excluded from evaluation.
2. As a direct child of the `dependency` block, this will be a FINAL exclusion, meaning no matter whether the versions matching the exclusion pattern are matched elsewhere, they will ALWAYS be excluded from the final result.


## Contributing
Contributions are welcome! Please read the [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to contribute to this project.

### License
This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.

### Contact
For any inquiries or issues, please open an issue on GitHub or contact the project maintainer.