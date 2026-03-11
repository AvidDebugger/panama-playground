plugins {
    java
    alias(libs.plugins.jmh.plugin)
}

dependencies {
    implementation(project(":ffm-examples"))
    jmh(project(":ffm-examples"))
}

val nativeLibPath = rootProject.layout.projectDirectory.dir("native/build").asFile.absolutePath

jmh {
    jvmArgs.set(listOf(
        "--enable-native-access=ALL-UNNAMED",
        "-Djava.library.path=$nativeLibPath"
    ))
    warmupIterations.set(3)
    iterations.set(3)
    fork.set(2)
}
