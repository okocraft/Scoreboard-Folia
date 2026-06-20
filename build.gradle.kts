plugins {
    `java-library`
    alias(libs.plugins.jcommon)
    alias(libs.plugins.bundler)
    alias(libs.plugins.run.paper)
}

val mcVersion = libs.versions.paper.get().replaceAfter(".build", "").removeSuffix(".build")
val fullVersion = "${version}-mc${mcVersion}"

jcommon {
    javaVersion = JavaVersion.VERSION_25
}

allprojects {
    dependencies {
        implementation(files(rootDir.resolve("libs/Scoreboard-5.0.jar")))
    }
}

repositories {
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(projects.scoreboardFoliaPacketDisplay)
    implementation(projects.scoreboardFoliaPlaceholders)
}

bundler {
    replacePluginVersionForPaper(fullVersion, mcVersion)
    copyToRootBuildDirectory("Scoreboard-${fullVersion}.jar")
}

runPaper.folia.registerTask {
    minecraftVersion(mcVersion)
}
