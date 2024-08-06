plugins {
    id("io.papermc.paperweight.userdev") version "1.7.2"
}

dependencies {
    paperweight.foliaDevBundle("${rootProject.extra["foliaVersion"]}-R0.1-SNAPSHOT")
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
