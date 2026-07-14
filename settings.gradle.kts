pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "FabricMC" }
        maven("https://maven.architectury.dev/") { name = "Architectury" }
        maven("https://maven.minecraftforge.net/") { name = "MinecraftForge" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }

    plugins {
        id("me.modmuss50.mod-publish-plugin") version "2.1.1"
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.6"
}

stonecutter {
    create(rootProject) {
        branch("common") {
            versions(
                "1.20.1", "1.20.2", "1.20.4", "1.20.6",
                "1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.8", "1.21.10", "1.21.11",
                "26.1", "26.1.1", "26.1.2", "26.2",
            )
        }
        branch("fabric") {
            versions(
                "1.20.1", "1.20.2", "1.20.4", "1.20.6",
                "1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.8", "1.21.10", "1.21.11",
                "26.1", "26.1.1", "26.1.2", "26.2",
            )
        }
        branch("forge") {
            versions(
                "1.20.1", "1.20.2", "1.20.4", "1.20.6",
                "1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.8", "1.21.10", "1.21.11",
                "26.1", "26.1.1", "26.1.2", "26.2",
            )
        }
        branch("neoforge") {
            versions(
                "1.20.1", "1.20.2", "1.20.4", "1.20.6",
                "1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.8", "1.21.10", "1.21.11",
                "26.1", "26.1.1", "26.1.2", "26.2",
            )
        }

        vcsVersion = "1.20.1"
    }
}

rootProject.name = "nosigngui"
