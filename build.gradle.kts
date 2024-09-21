import io.izzel.taboolib.gradle.*

plugins {
    `java-library`
    `maven-publish`
    id("io.izzel.taboolib") version "2.0.17"
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    kotlin("plugin.serialization") version "1.8.22"
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
            taboolib = "6.2.0-beta5"
        }
    }

    description {
        dependencies {
            name("CapybaraMachinery")
            name("Adyeshach")
        }
    }

    relocate("kotlinx.serialization", "kotlinx.serialization140")
}

repositories {
    mavenLocal()
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    compileOnly("me.xiaozhangup:CapybaraMachinery:1.0.0")
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.1")
    compileOnly("ink.ptms.adyeshach:all:2.0.0-snapshot-25")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.3")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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