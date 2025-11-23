# Building Better Firefly Bushes

This document describes how to build the Better Firefly Bushes mod from source.

## Prerequisites

- **Java 21** or higher (install via [SDKMAN!](https://sdkman.io/))
- **Gradle** (included via wrapper)
- **Git** (for cloning the repository)

### Installing Java with SDKMAN!

```bash
# Install SDKMAN!
curl -s "https://get.sdkman.io" | bash

# Install Java 21
sdk install java 21.0.1-tem

# Verify installation
java -version
```

## Build Commands

### Quick Build

Builds without running tests (faster):

```bash
./gradlew build -x test
```

## Build Targets

### Standard Gradle Tasks

- `./gradlew clean` - Removes all build outputs
- `./gradlew build` - Full build including tests
- `./gradlew jar` - Creates JAR without running tests
- `./gradlew check` - Runs all verification tasks (tests + coverage)

### Custom Tasks

#### Quick Install

Install the mod to your Minecraft mods directory (--dir is required):

```bash
./gradlew installMod --dir=/path/to/minecraft/mods
```

## Troubleshooting

### Build Fails with "Execution failed for task ':test'"

This usually means tests are failing. Run tests with verbose output:

```bash
./gradlew test --info
```

### Java Versions

Use SDKMAN! to switch Java versions:

```bash
# List available Java versions
sdk list java

# Install specific version
sdk install java 21.0.1-tem

# Switch to Java 21
sdk use java 21.0.1-tem

# Verify
java -version
```

## Development Workflow

1. Make code changes
2. Run `./gradlew test` to verify tests pass
3. Run `./gradlew installMod --dir=/path/to/test/instance/mods` to test in-game
4. Run `./gradlew build` for full build with coverage verification
5. Commit changes

## IDE Setup

### IntelliJ IDEA

1. Open the project directory
2. IntelliJ should auto-detect the Gradle project
3. Wait for Gradle sync to complete
4. Run configurations will be auto-generated
