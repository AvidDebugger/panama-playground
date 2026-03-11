plugins {
    java
}

java {
    sourceSets {
        create("integrationTest") {
            compileClasspath += sourceSets.main.get().output
            runtimeClasspath += sourceSets.main.get().output
        }
    }
}

dependencies {
    "integrationTestImplementation"(platform(libs.junit.bom))
    "integrationTestImplementation"(libs.junit.jupiter)
    "integrationTestImplementation"(libs.assertj.core)
    "integrationTestRuntimeOnly"(libs.junit.platform.launcher)
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests (require native library)"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter("test")
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    dependsOn(rootProject.tasks.named("buildNative"))
}

