plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    id("com.google.devtools.ksp") version "2.3.7"
    application
}

group = "edu.kit.ifv"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://packages.jetbrains.team/maven/p/kds/kotlin-ds-maven")
    maven("https://repo.osgeo.org/repository/release")
    flatDir {
        dirs("libs")
    }
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("edu.kit.ifv.mobitopp:mobitopp-reengineering:1.0.0")
    ksp("edu.kit.ifv.mobitopp:processor:1.0.0") // <-- this is required
    implementation("edu.kit.ifv.mobitopp:processor:1.0.0") // <-- this is required
    ksp("edu.kit.ifv.mobitopp:annotations:1.0.0") // <-- this is required

    implementation("edu.kit.ifv.mobitopp:kotlin-units:1.0.0")
    implementation("edu.kit.ifv.mobitopp:discrete-choice:1.1.0")
    implementation("edu.kit.ifv.mobitopp:actitoppNG:1.0.0")

    implementation("org.apache.commons:commons-compress:1.28.0")
    implementation("org.jgrapht:jgrapht-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
    implementation("me.tongfei:progressbar:0.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.0.1")
    implementation("org.yaml:snakeyaml:2.5")
    implementation("org.jetbrains.kotlinx:dataframe:1.0.0-Beta3")
    implementation("org.jetbrains.kotlinx:kandy-lets-plot:0.8.0")
    implementation("org.jetbrains.kotlinx:kandy-util:0.8.0")
    implementation("org.jetbrains.kotlinx:kandy-api:0.8.0")

//    implementation(kotlin("reflect"))

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

application {
    mainClass.set("MainShortTermKt")
}

tasks.register<JavaExec>("runLongTerm") {
    group = "application"
    description = "Runs the main entry point in MainLongTerm.kt"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("MainLongTermKt")
}

tasks.register<JavaExec>("runShortTerm") {
    group = "application"
    description = "Runs the main entry point in MainShortTerm.kt"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("MainShortTermKt")
}

tasks.withType<JavaExec>().configureEach {
    maxHeapSize = "8G"
    jvmArgs = listOf(
        "-Xmx8G"                                 // Example: Set max heap size to 6G
    )
}
