plugins {
    java
    alias(libs.plugins.jmh.plugin) apply false
}

tasks.register<Exec>("buildNative") {
    group = "build"
    description = "Build the native ffmplayground library"
    workingDir = file("native")
    commandLine("sh", "-c", "mkdir -p build && cd build && cmake .. && cmake --build .")
}

allprojects {
    group = "net.szumigaj.java.panama.ffm"
    version = "1.0.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation(platform(rootProject.libs.junit.bom))
        testImplementation(rootProject.libs.junit.jupiter)
        testImplementation(rootProject.libs.assertj.core)
        testRuntimeOnly(rootProject.libs.junit.platform.launcher)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }
}

// Configure ffm-examples and ffm-benchmarks to find native library for tests
listOf(":ffm-examples", ":ffm-benchmarks").forEach { path ->
    project(path) {
        val nativeBuildDir = rootProject.layout.projectDirectory.dir("native/build")
        val libPath = nativeBuildDir.asFile.absolutePath
        tasks.withType<Test>().configureEach {
            systemProperty("java.library.path", libPath)
            environment("LD_LIBRARY_PATH", libPath)
            environment("DYLD_LIBRARY_PATH", libPath)
        }
    }
}
