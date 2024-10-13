import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `maven-publish`
    id("io.izzel.taboolib") version "2.0.18"
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    kotlin("plugin.serialization") version "1.9.25"
}

taboolib {
    env {
        install(
            Bukkit,
            BukkitNMS,
            BukkitHook,
            BukkitUtil,
            BukkitNMSUtil,
            BukkitUI,
            BukkitFakeOp,
            Basic,
            MinecraftChat,
            CommandHelper
        )

        version {
            taboolib = "6.2.0-beta15"
        }
    }

    description {
        dependencies {
            name("CapybaraMachinery")
            name("FancyNpcs")
        }
    }

    relocate("kotlinx.serialization", "kotlinx.serialization163")
//    relocate("me.tofaa.entitylib", "me.xiaozhangup.domain.lib.entitylib")
}

repositories {
    mavenLocal()
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://maven.evokegames.gg/snapshots")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.fancyplugins.de/releases")
    maven("https://jitpack.io")
    maven("https://maven.nostal.ink/repository/maven-public/")
    mavenCentral()
}

dependencies {
    compileOnly("me.xiaozhangup:CapybaraMachinery:1.0.0")
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.1")
//    compileOnly("de.oliver:FancyNpcs:2.2.2")
//    compileOnly("com.github.retrooper:packetevents-spigot:2.5.0")
//    compileOnly("me.tofaa.entitylib:spigot:2.4.10-SNAPSHOT")
    compileOnly("ink.pmc.advkt:core:1.0.0-SNAPSHOT")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.register<Jar>("sourceJar") {
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
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

            from(components["kotlin"])
            artifact(tasks["sourceJar"])
            artifact("/build/libs/${rootProject.name}-${rootProject.version}-api.jar")
        }
    }
}