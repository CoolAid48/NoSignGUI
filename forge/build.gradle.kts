import net.fabricmc.loom.LoomGradleExtension
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
val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed < "1.17" -> JavaVersion.VERSION_1_8
    else -> JavaVersion.VERSION_17
}
val publishMinecraftVersions = when (minecraftVersion) {
    "1.16.5" -> listOf("1.16.5")
    "1.20.1" -> listOf("1.20.1")
    "1.21.1" -> listOf("1.21.1")
    "1.21.11" -> listOf("1.21.11")
    "26.1" -> listOf("26.1")
    "26.1.1" -> listOf("26.1.1")
    "26.1.2" -> listOf("26.1.2")
    "26.2" -> listOf("26.2")
    else -> error("No Forge publishing versions configured for Minecraft $minecraftVersion")
}
val commonProject = requireNotNull(sc.node.sibling("common")?.project) {
    "No common Stonecutter node exists for $minecraftVersion"
}

// Modern Forge's production jar implements both login payload interfaces with
// m_295630_, while its generated Mojmap/SRG mapping leaves CustomQueryPayload
// on m_294761_. TinyRemapper cannot propagate both names to ForgePayload, so
// make that single mapping agree with Forge's production bytecode.
val forgePayloadMappingsFix = if (sc.current.parsed >= "1.20.6" && sc.current.parsed < "26.1") {
    val patchedMappings = layout.buildDirectory.file("generated/mappings/forge-$minecraftVersion.tiny")
    val sourceMappings = providers.provider<File> {
        LoomGradleExtension.get(project).mappingConfiguration.tinyMappingsWithSrg.toFile()
    }
    val prepareMappings = tasks.register("prepareForgePayloadMappings") {
        inputs.file(sourceMappings)
        outputs.file(patchedMappings)

        doLast {
            val original = "\ta\tm_294761_\twrite\tmethod_52296\twrite"
            val replacement = "\ta\tm_295630_\twrite\tmethod_52296\twrite"
            val source = sourceMappings.get().readText(Charsets.UTF_8)
            val firstMatch = source.indexOf(original)

            check(firstMatch >= 0 && source.indexOf(original, firstMatch + original.length) < 0) {
                "Expected exactly one Forge $minecraftVersion CustomQueryPayload mapping to patch"
            }

            patchedMappings.get().asFile.apply {
                parentFile.mkdirs()
                writeText(source.replace(original, replacement), Charsets.UTF_8)
            }
        }
    }

    prepareMappings to patchedMappings
} else {
    null
}

version = "$modVersion+$minecraftVersion"
base.archivesName.set("$modId-forge")

architectury {
    minecraft = minecraftVersion
    platformSetupLoomIde()
    forge()
}

loom {
    forge {
        mixinConfig("nosigngui.mixins.json")
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
configurations.named("developmentForge") { extendsFrom(commonBundle) }

val commonNamedElements = dependencies.project(commonProject.path).apply {
    if (sc.current.parsed < "26.1") targetConfiguration = "namedElements"
    isTransitive = false
}
val commonProduction = dependencies.project(commonProject.path).apply {
    targetConfiguration = "transformProductionForge"
    isTransitive = false
}

dependencies {
    if (sc.current.parsed >= "26.1") {
        minecraft("net.minecraft:minecraft:$minecraftVersion")
    } else {
        minecraft("com.mojang:minecraft:$minecraftVersion")
        add("mappings", loom.officialMojangMappings())
    }
    add("forge", "net.minecraftforge:forge:$minecraftVersion-${property("deps.forge_version")}")

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
    val props = mapOf(
        "version" to project.version.toString(),
        "minecraft" to project.property("mod.mc_compat_forgelike").toString(),
        "loader" to project.property("deps.forge_loader_range").toString(),
        "pack" to project.property("mod.pack_format").toString(),
    )
    inputs.properties(props)
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) { expand(props) }
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
        forgePayloadMappingsFix?.let { (prepareMappings, patchedMappings) ->
            dependsOn(prepareMappings)
            customMappings.from(patchedMappings)
        }
    }.flatMap { it.archiveFile }
}

publishMods {
    file.set(releaseJar)
    changelog.set(providers.fileContents(rootProject.layout.projectDirectory.file("CHANGELOG.md")).asText)
    version.set("$modVersion+$minecraftVersion-forge")
    displayName.set("NoSignGUI $modVersion for Forge ${publishMinecraftVersions.joinToString("–")}")
    type.set(STABLE)
    modLoaders.add("forge")
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
    description = "Builds Forge $minecraftVersion and copies its final jar to the root collection directory."
    dependsOn(tasks.build)
    from(releaseJar)
    into(rootProject.layout.buildDirectory.dir("libs/$modVersion/$minecraftVersion/forge"))
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
