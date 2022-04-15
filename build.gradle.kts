plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.cadixdev.licenser") version "0.6.1"
    id("xyz.jpenilla.run-paper") version "1.0.6"
}

group = "io.sapphiremc"
version = "1.0.1"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies  {
    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.1")
    compileOnly("org.projectlombok:lombok:1.18.22")

    implementation("de.tr7zw:item-nbt-api:2.9.2")

    annotationProcessor("org.projectlombok:lombok:1.18.22")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

license {
    include("**/io/sapphiremc/hideplayers/**")

    header(project.file("HEADER"))
    newLine(false)
}

tasks {
    withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.addAll(
            listOf(
                "-parameters",
                "-nowarn",
                "-Xlint:-unchecked",
                "-Xlint:-deprecation",
                "-Xlint:-processing"
            )
        )
        options.isFork = true
    }

    processResources {
        filesMatching(listOf("plugin.yml", "config.yml")) {
            expand("version" to project.version)
        }
    }

    jar {
        include("LICENSE")
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("de.tr7zw.changeme", "io.sapphiremc.hideplayers.shaded")
        minimize()
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("1.18.2")
        runDirectory.set(project.projectDir.resolve("run/"))
        if (!System.getenv("useCustomCore").isNullOrEmpty()) {
            serverJar.set(project.projectDir.resolve("run/server.jar"))
        }
    }
}