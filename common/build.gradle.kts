import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("maven-publish")
}

val minecraftVersion = sc.current.version
val modId = property("mod.id").toString()
val modVersion = property("mod.version").toString()
val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}

version = "$modVersion+$minecraftVersion"
base.archivesName.set("$modId-common")

val commonPlatforms = buildList {
    if (sc.node.sibling("fabric") != null) add("fabric")
    if (sc.node.sibling("forge") != null) add("forge")
    if (sc.node.sibling("neoforge") != null) {
        add(if (sc.current.parsed >= "1.20.2") "neoforge" else "forge")
    }
}.distinct()

architectury {
    minecraft = minecraftVersion
    common(commonPlatforms)
}

dependencies {
    if (sc.current.parsed >= "26.1") {
        minecraft("net.minecraft:minecraft:$minecraftVersion")
        add("implementation", "net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    } else {
        minecraft("com.mojang:minecraft:$minecraftVersion")
        add("mappings", loom.officialMojangMappings())
        add("modImplementation", "net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    }
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
    val mixinJava = "JAVA_${requiredJava.majorVersion}"
    inputs.property("mixinJava", mixinJava)
    filesMatching("nosigngui.mixins.json") {
        expand(mapOf("java" to mixinJava))
    }
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
