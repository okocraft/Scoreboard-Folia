plugins {
    id("io.papermc.paperweight.userdev") version "1.7.7"
}

dependencies {
    paperweight.paperDevBundle("${rootProject.extra["paperVersion"]}-R0.1-SNAPSHOT")
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
