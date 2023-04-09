plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.5.4"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.okocraft.scoreboard"
version = "4.3-folia"

val mcVersion = "1.19.4"
val fullVersion = "${version}-mc${mcVersion}"

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

dependencies {
    paperweight.foliaDevBundle("$mcVersion-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.3")

    implementation("com.github.siroshun09.configapi:configapi-yaml:4.6.4")
    implementation("com.github.siroshun09.translationloader:translationloader:2.0.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    reobfJar {
        outputJar.set(
            project.layout.buildDirectory
                .file("libs/Scoreboard-${fullVersion}.jar")
        )
    }

    build {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()

        filesMatching(listOf("plugin.yml")) {
            expand("projectVersion" to fullVersion)
        }
    }

    shadowJar {
        minimize()
        relocate("com.github.siroshun09", "$group.libs")
    }
}
