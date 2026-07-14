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

stonecutter active "1.20.1"

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

tasks.register("publishAll") {
    group = "publishing"
    description = "Publishes every supported Minecraft/loader artifact to Modrinth and CurseForge."
    dependsOn(stonecutter.tasks.named("publishMods") { branch.id != "common" })
}
