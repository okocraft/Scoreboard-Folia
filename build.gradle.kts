plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "net.okocraft.scoreboard"
version = "5.0-SNAPSHOT"

val mcVersion = "1.20.6"
val fullVersion = "${version}-mc${mcVersion}"

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

dependencies {
    paperweight.foliaDevBundle("$mcVersion-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("com.github.siroshun09.configapi:configapi-format-yaml:5.0.0-beta.3") {
        exclude("org.yaml", "snakeyaml")
    }
    implementation("com.github.siroshun09.messages:messages-minimessage:0.8.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

tasks {
    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()

        filesMatching(listOf("plugin.yml")) {
            expand("projectVersion" to fullVersion)
        }
    }

    shadowJar {
        minimize()
        relocate("com.github.siroshun09", "${project.group}.libs")
        archiveFileName = "Scoreboard-${fullVersion}.jar"
    }
}
