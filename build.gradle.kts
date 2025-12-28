plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.3.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

project.extra["paperVersion"] = "1.21.11"
project.extra["foliaVersion"] = "1.21.11"
project.extra["apiVersion"] = "1.21"

val fullVersion = "${version}-mc${project.extra["paperVersion"]}"

allprojects {
    apply(plugin = "java-library")

    val javaVersion = JavaVersion.VERSION_21
    val charset = Charsets.UTF_8

    java {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion

        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion.ordinal + 1))
        }
    }

    tasks {
        compileJava {
            options.encoding = charset.name()
            options.release.set(javaVersion.ordinal + 1)
        }

        processResources {
            filteringCharset = charset.name()
        }
    }

    dependencies {
        implementation(files(rootDir.resolve("libs/Scoreboard-5.0-SNAPSHOT.jar")))
    }
}

repositories {
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${project.extra["paperVersion"]}-R0.1-SNAPSHOT")
    implementation(projects.scoreboardFoliaPacketDisplay)
    implementation(projects.scoreboardFoliaPlaceholders)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    processResources {
        filesMatching(listOf("paper-plugin.yml")) {
            expand("projectVersion" to fullVersion, "apiVersion" to project.extra["apiVersion"].toString())
        }
    }

    shadowJar {
        minimize()
        relocate("com.github.siroshun09", "${project.group}.libs")
        archiveFileName = "Scoreboard-${fullVersion}.jar"

        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }
    }
}

runPaper.folia.registerTask {
    minecraftVersion(project.extra["foliaVersion"].toString())
}
