import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile

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
    else -> error("No Fabric publishing versions configured for Minecraft $minecraftVersion")
}
val commonProject = requireNotNull(sc.node.sibling("common")?.project) {
    "No common Stonecutter node exists for $minecraftVersion"
}

version = "$modVersion+$minecraftVersion"
base.archivesName.set("$modId-fabric")

architectury {
    minecraft = minecraftVersion
    platformSetupLoomIde()
    fabric()
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
configurations.named("developmentFabric") { extendsFrom(commonBundle) }

val commonNamedElements = dependencies.project(commonProject.path).apply {
    if (sc.current.parsed < "26.1") targetConfiguration = "namedElements"
    isTransitive = false
}
val commonProduction = dependencies.project(commonProject.path).apply {
    targetConfiguration = "transformProductionFabric"
    isTransitive = false
}

dependencies {
    if (sc.current.parsed >= "26.1") {
        minecraft("net.minecraft:minecraft:$minecraftVersion")
        add("implementation", "net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
        add("implementation", "net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    } else {
        minecraft("com.mojang:minecraft:$minecraftVersion")
        add("mappings", loom.officialMojangMappings())
        add("modImplementation", "net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
        add("modImplementation", "net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
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
    val props = mapOf(
        "version" to project.version.toString(),
        "minecraft" to project.property("mod.mc_compat_fabric").toString(),
        "loader" to project.property("deps.fabric_loader").toString(),
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }
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
    version.set("$modVersion+$minecraftVersion-fabric")
    displayName.set("NoSignGUI $modVersion for Fabric ${publishMinecraftVersions.joinToString("–")}")
    type.set(STABLE)
    modLoaders.add("fabric")
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
        requires("fabric-api")
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
        requires("fabric-api")
    }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds Fabric $minecraftVersion and copies its final jar to the root collection directory."
    dependsOn(tasks.build)
    from(releaseJar)
    into(rootProject.layout.buildDirectory.dir("libs/$modVersion/$minecraftVersion/fabric"))
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
