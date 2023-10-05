plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "1.5.5"
}

group = "ovh.roro.libraries"
version = "1.20.2"

repositories {
    mavenLocal()
}

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))

    withJavadocJar()
    withSourcesJar()
}

dependencies {
    paperweight.paperDevBundle("1.20.2-R0.1-SNAPSHOT")
}

// Configure reobfJar to run when invoking the build task
tasks.assemble {
    dependsOn(tasks.reobfJar)
}

tasks.compileJava {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

    // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
    // See https://openjdk.java.net/jeps/247 for more information.
    options.release.set(17)
}

tasks.javadoc {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
}

tasks.processResources {
    filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.reobfJar)
            artifact(tasks.jar) {
                classifier = "moj-mapped"
            }
            artifact(tasks["javadocJar"]) {
                classifier = "javadoc"
            }
            artifact(tasks["sourcesJar"]) {
                classifier = "sources"
            }
        }
    }

    repositories {
        maven {
            name = "roro"
            url = uri("https://repo.roro.ovh/artifactory/libraries/")

            credentials(PasswordCredentials::class)
        }
    }
}