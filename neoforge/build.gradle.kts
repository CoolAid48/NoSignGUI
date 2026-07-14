import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.gradleup.shadow")
    id("me.modmuss50.mod-publish-plugin")
    id("maven-publish")
}

val minecraftVersion = sc.current.version
val modId = property("mod.id").toString()
val modVersion = property("mod.version").toString()
val legacyNeoForge = sc.current.parsed < "1.20.2"
val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}
val publishMinecraftVersions = when (minecraftVersion) {
    "1.20.1" -> listOf("1.20.1")
    "1.20.2" -> listOf("1.20.2")
    "1.20.4" -> listOf("1.20.3", "1.20.4")
    "1.20.6" -> listOf("1.20.5", "1.20.6")
    "1.21.1" -> listOf("1.21", "1.21.1")
    "1.21.3" -> listOf("1.21.2", "1.21.3")
    "1.21.4" -> listOf("1.21.4")
    "1.21.5" -> listOf("1.21.5")
    "1.21.8" -> listOf("1.21.6", "1.21.7", "1.21.8")
    "1.21.10" -> listOf("1.21.9", "1.21.10")
    "1.21.11" -> listOf("1.21.11")
    "26.1" -> listOf("26.1")
    "26.1.1" -> listOf("26.1.1")
    "26.1.2" -> listOf("26.1.2")
    "26.2" -> listOf("26.2")
    else -> error("No NeoForge publishing versions configured for Minecraft $minecraftVersion")
}
val commonProject = requireNotNull(sc.node.sibling("common")?.project) {
    "No common Stonecutter node exists for $minecraftVersion"
}

version = "$modVersion+$minecraftVersion"
base.archivesName.set("$modId-neoforge")

architectury {
    minecraft = minecraftVersion
    platformSetupLoomIde()
    if (legacyNeoForge) forge() else neoForge()
}

if (legacyNeoForge) {
    loom {
        forge {
            mixinConfig("nosigngui.mixins.json")
        }
    }
}

val commonBundle = configurations.create("commonBundle") {
    isCanBeConsumed = false
    isCanBeResolved = true
}
val shadowBundle = configurations.create("shadowBundle") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

configurations.named("compileClasspath") { extendsFrom(commonBundle) }
configurations.named("runtimeClasspath") { extendsFrom(commonBundle) }
configurations.named(if (legacyNeoForge) "developmentForge" else "developmentNeoForge") {
    extendsFrom(commonBundle)
}

val commonNamedElements = dependencies.project(commonProject.path).apply {
    if (sc.current.parsed < "26.1") targetConfiguration = "namedElements"
    isTransitive = false
}
val commonTransform = if (legacyNeoForge) "transformProductionForge" else "transformProductionNeoForge"
val commonProduction = dependencies.project(commonProject.path).apply {
    targetConfiguration = commonTransform
    isTransitive = false
}

repositories {
    maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
}

dependencies {
    if (sc.current.parsed >= "26.1") {
        minecraft("net.minecraft:minecraft:$minecraftVersion")
    } else {
        minecraft("com.mojang:minecraft:$minecraftVersion")
        add("mappings", loom.officialMojangMappings())
    }

    if (legacyNeoForge) {
        add("forge", "net.neoforged:forge:${property("deps.neoforge_version")}")
    } else {
        add("neoForge", "net.neoforged:neoforge:${property("deps.neoforge_version")}")
    }

    add(commonBundle.name, commonNamedElements)
    add(shadowBundle.name, commonProduction)
}

java {
    withSourcesJar()
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(requiredJava.majorVersion.toInt())
}

tasks.processResources {
    val selectedMetadata = when {
        legacyNeoForge -> "mods.toml"
        sc.current.parsed < "1.20.5" -> "mods.modern.toml"
        else -> "neoforge.mods.toml"
    }
    val outputMetadata = if (sc.current.parsed < "1.20.5") "mods.toml" else "neoforge.mods.toml"
    val metadataFiles = setOf("mods.toml", "mods.modern.toml", "neoforge.mods.toml")
    val props = mapOf(
        "version" to project.version.toString(),
        "minecraft" to project.property("mod.mc_compat_neoforge").toString(),
        "neoforge" to project.property("deps.neoforge_loader_range").toString(),
        "pack" to project.property("mod.pack_format").toString(),
    )

    inputs.properties(props)
    filesMatching("META-INF/*.toml") {
        when {
            name == selectedMetadata -> {
                name = outputMetadata
                expand(props)
            }
            name in metadataFiles -> exclude()
        }
    }
    filesMatching("pack.mcmeta") { expand(props) }
}

tasks.withType<Jar>().configureEach {
    manifest.attributes["MixinConfigs"] = "nosigngui.mixins.json"
}

tasks.jar {
    archiveClassifier.set("dev")
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier.set(if (sc.current.parsed >= "26.1") "" else "dev-shadow")
}

val releaseJar = if (sc.current.parsed >= "26.1") {
    tasks.shadowJar.flatMap { it.archiveFile }
} else {
    tasks.named<RemapJarTask>("remapJar") {
        inputFile.set(tasks.shadowJar.flatMap { it.archiveFile })
        dependsOn(tasks.shadowJar)
    }.flatMap { it.archiveFile }
}

publishMods {
    file.set(releaseJar)
    changelog.set(providers.fileContents(rootProject.layout.projectDirectory.file("CHANGELOG.md")).asText)
    version.set("$modVersion+$minecraftVersion-neoforge")
    displayName.set("NoSignGUI $modVersion for NeoForge ${publishMinecraftVersions.joinToString("–")}")
    type.set(STABLE)
    modLoaders.add("neoforge")
    dryRun.set(providers.gradleProperty("publish.dryRun").map(String::toBoolean).orElse(false))

    modrinth {
        projectId.set("CBK3ZZWD")
        accessToken.set(
            providers.environmentVariable("MODRINTH_TOKEN")
                .orElse(providers.environmentVariable("MODRINTH_API_KEY"))
                .orElse(providers.gradleProperty("modrinth.token"))
        )
        minecraftVersions.addAll(publishMinecraftVersions)
        environment.set(CLIENT_ONLY)
    }

    curseforge {
        projectId.set("1207799")
        projectSlug.set("disablesigngui")
        accessToken.set(
            providers.environmentVariable("CURSEFORGE_TOKEN")
                .orElse(providers.environmentVariable("CURSEFORGE_API_KEY"))
                .orElse(providers.gradleProperty("curseforge.token"))
        )
        minecraftVersions.addAll(publishMinecraftVersions)
        javaVersions.add(requiredJava)
        client.set(true)
        server.set(false)
    }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds NeoForge $minecraftVersion and copies its final jar to the root collection directory."
    dependsOn(tasks.build)
    from(releaseJar)
    into(rootProject.layout.buildDirectory.dir("libs/$modVersion/$minecraftVersion/neoforge"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = property("mod.group").toString()
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
}
