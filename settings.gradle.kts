pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "Scoreboard-Folia"

sequenceOf(
    "packet-display",
    "placeholders"
).forEach {
    val prefix =rootProject.name.lowercase(java.util.Locale.ENGLISH)
    include("$prefix-$it")
    project(":$prefix-$it").projectDir = file(it)
}
