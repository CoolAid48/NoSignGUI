plugins {
    id("dev.kikugie.stonecutter")
    id("architectury-plugin") version "3.5-SNAPSHOT" apply false
    id("dev.architectury.loom") version "1.17-SNAPSHOT" apply false
    id("dev.architectury.loom-no-remap") version "1.17-SNAPSHOT" apply false
    id("com.gradleup.shadow") version "9.2.2" apply false
}

// Minecraft 26.x is distributed unobfuscated and has no separate Mojang
// mappings artifact. Loom's marker must be present before its main plugin.
gradle.beforeProject {
    if (path.matches(Regex(":(common|fabric|forge|neoforge):26\\..+"))) {
        pluginManager.apply("dev.architectury.loom-no-remap")
    }
}

stonecutter active "26.2"

stonecutter parameters {
    properties {
        tags(current.version, node.branch.id)
    }
}

tasks.register("buildAll") {
    group = "build"
    description = "Builds and collects every supported Minecraft/loader artifact."
    dependsOn(stonecutter.tasks.named("buildAndCollect") { branch.id != "common" })
}

// Preserve the order of the top-level Minecraft version tables in the
// Stonecutter properties file. Platform-specific tables such as
// [neoforge."1.20.1"] intentionally does not match this expression.
val orderedMinecraftVersions = rootProject.file("stonecutter.properties.toml")
    .readLines()
    .mapNotNull { line ->
        Regex("""^\["([^"]+)"\]$""")
            .matchEntire(line.trim())
            ?.groupValues
            ?.get(1)
    }

val publishPlatforms = listOf("fabric", "forge", "neoforge")
val platformPublishTasks = listOf("publishCurseforge", "publishModrinth")

// Gradle may otherwise schedule all Fabric publications before Forge and
// NeoForge. Order the real upload tasks so every loader/destination for one
// Minecraft version finishes before publishing the next version.
gradle.projectsEvaluated {
    val uploadsByMinecraftVersion = orderedMinecraftVersions.map { minecraftVersion ->
        publishPlatforms.flatMap { platform ->
            val platformProject = rootProject.findProject(":$platform:$minecraftVersion")
            if (platformProject == null) {
                emptyList()
            } else {
                platformPublishTasks.map { taskName ->
                    platformProject.tasks.named(taskName)
                }
            }
        }
    }

    uploadsByMinecraftVersion.zipWithNext().forEach { (previousUploads, currentUploads) ->
        currentUploads.forEach { currentUpload ->
            currentUpload.configure {
                previousUploads.forEach(::mustRunAfter)
            }
        }
    }
}

tasks.register("publishAll") {
    group = "publishing"
    description = "Publishes every artifact to Modrinth and CurseForge in Minecraft version order."
    dependsOn(stonecutter.tasks.named("publishMods") { branch.id != "common" })
}
