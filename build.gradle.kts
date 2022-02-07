plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("xyz.jpenilla.run-paper") version "1.0.6"

}

group = "io.sapphiremc"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies  {
    compileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.1")
    compileOnly("org.projectlombok:lombok:1.18.22")

    implementation("de.tr7zw:item-nbt-api:2.9.1")

    annotationProcessor("org.projectlombok:lombok:1.18.22")
}

tasks.processResources {
    filesMatching(listOf("plugin.yml", "config.yml")) {
        expand("version" to project.version)
    }
}

tasks.runServer {
    minecraftVersion("1.18.1")
    runDirectory.set(project.projectDir.resolve("run/"))
    if (!System.getenv("useCustomCore").isNullOrEmpty()) {
        serverJar.set(project.projectDir.resolve("run/server.jar"))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = Charsets.UTF_8.name()
    options.release.set(17)
}
tasks.withType<Javadoc> {
    options.encoding = Charsets.UTF_8.name()
}
tasks.withType<ProcessResources> {
    filteringCharset = Charsets.UTF_8.name()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    archiveClassifier.set("dev")
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("de.tr7zw.changeme", "io.sapphiremc.hideitem.shaded")
    minimize()
}