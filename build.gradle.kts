import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")

    group = "com.hydracloud.netty5-api"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
        options.encoding = "UTF-8"
    }

    tasks.register<Jar>("sourcesJar") {
        from(sourceSets.main.get().allJava)
        archiveClassifier.set("sources")
    }

    tasks.jar {
        manifest {
            attributes(
                "Implementation-Version" to project.version
            )
        }
    }

    tasks.named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
        manifest {
            attributes(
                "Implementation-Version" to project.version
            )
        }
    }
}