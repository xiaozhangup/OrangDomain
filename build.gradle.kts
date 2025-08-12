import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `maven-publish`
    id("io.izzel.taboolib") version "2.0.23"
    id("org.jetbrains.kotlin.jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
}

taboolib {
    env {
        install(
            Basic,
            Bukkit,
            BukkitHook,
            BukkitUtil,
            BukkitUI,
            MinecraftChat,
            CommandHelper
        )

        version {
            taboolib = "6.2.3-a372a91"
            coroutines = "1.10.2"
            skipKotlin = true
            skipKotlinRelocate = true
        }
    }

    description {
        dependencies {
            name("CarbKotlin")
            name("WhaleMechanism")
            name("SlimeCargoNext")
            name("Coins")
        }
    }

    relocate("com.jeff_media.customblockdata", "me.xiaozhangup.ceramic.lib.customblockdata")
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
    compileOnly("me.xiaozhangup.octopus:octopus-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("me.xiaozhangup:WhaleMechanism:1.0.1")
    compileOnly("me.xiaozhangup:SlimeCargoNext:1.0.1")
    compileOnly("net.momirealms:craft-engine-core:0.0.59")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.59")
    compileOnly("me.clip:placeholderapi:2.11.1")

    taboo("com.jeff-media:custom-block-data:2.2.5")

    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
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
        }
    }
}