plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-SNAPSHOT"
}

dependencies {
    paperweight.paperDevBundle("${rootProject.extra["paperVersion"]}-R0.1-SNAPSHOT")
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
