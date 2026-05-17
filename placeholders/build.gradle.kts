plugins {
    alias(libs.plugins.paperweight.userdev)
}

dependencies {
    paperweight.foliaDevBundle(libs.versions.folia.get())
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
