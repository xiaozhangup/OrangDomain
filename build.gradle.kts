import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.4.3"
    id("me.xiaozhangup.sftp-uploader") version "0.1.0"
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
}


repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.momirealms.net/releases/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.evokegames.gg/snapshots")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.fancyplugins.de/releases")
    maven("https://jitpack.io")
    maven("https://maven.nostal.ink/repository/maven-public/")
}

dependencies {
    compileOnly("me.xiaozhangup.crab:CarbKotlin:2.3.20:paper") { isTransitive = false }
    compileOnly("me.xiaozhangup.octopus:octopus-api:26.2-R0.1-SNAPSHOT")
    compileOnly("me.xiaozhangup:WhaleMechanism:1.0.1:api")
    compileOnly("me.xiaozhangup:SlimeCargoNext:1.0.3:api")
    compileOnly("net.momirealms:craft-engine-core:26.6")
    compileOnly("net.momirealms:craft-engine-bukkit:26.6")
    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("com.jeff-media:custom-block-data:2.2.5")

    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

tasks.register<Jar>("sourceJar") {
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}

val apiJar = tasks.register<Jar>("apiJar") {
    archiveClassifier.set("api")
    from(sourceSets.main.get().output) { exclude("plugin.yml") }
}

publishing {
    repositories {
        mavenLocal()
    }

    publications {
        create<MavenPublication>("maven") {
            artifactId = rootProject.name
            groupId = "me.xiaozhangup"
            version = rootProject.version.toString()

            artifact(tasks.shadowJar)
            artifact(tasks.named("apiJar"))
        }
    }
}

configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

sftpUploader {
    host.set("xiaozhangup@s1.dimc.cloud")
    target.set("Minecraft")
    jars.set(
        listOf(
            layout.buildDirectory.file("libs/OrangDomain-1.0.2.jar").get().asFile.absolutePath
        )
    )
}

// Native runtime and thin compile-time API.
tasks.jar { archiveClassifier.set("plain") }
tasks.shadowJar {
    archiveClassifier.set("")
    filesMatching("META-INF/services/**") { duplicatesStrategy = DuplicatesStrategy.INCLUDE }
    mergeServiceFiles()
    exclude("META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA")
    dependencies {
        exclude(dependency("org.jetbrains.kotlin:.*:.*"))
        exclude(dependency("org.jetbrains.kotlinx:.*:.*"))
    }

    relocate("com.jeff_media.customblockdata", "me.xiaozhangup.ceramic.lib.customblockdata")
}

tasks.assemble { dependsOn(tasks.shadowJar, apiJar) }
tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("plugin.yml") { expand("version" to project.version) }
}
