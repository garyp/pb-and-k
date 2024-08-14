import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    `maven-publish`
    signing
}

description = "Kotlin code generator for Protocol Buffers and library for writing code generator plugins."

kotlin {
    explicitApi()

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":pbandk-runtime"))
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        jvmMain {
            dependencies {
                implementation("com.google.protobuf:protobuf-java:${Versions.protobufJava}")
            }
        }

        jvmTest {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
            }
        }
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(Versions.jvmTarget))
}
tasks.withType<JavaCompile> {
    targetCompatibility = Versions.jvmTarget
}

val extractWellKnownTypeProtos = rootProject.tasks.named<Sync>("extractWellKnownTypeProtos")

tasks {
    val generateProtocProtos by registering(KotlinProtocTask::class) {
        includeDir.set(layout.dir(extractWellKnownTypeProtos.map { it.destinationDir }))
        outputDir.set(project.file("src/commonMain/kotlin"))
        kotlinPackage.set("pbandk.gen.pb")
        logLevel.set("debug")
        protoFileSubdir("google/protobuf/compiler")
    }

    val generateProtos by registering {
        dependsOn(generateProtocProtos)
    }

    val generateTestProtoDescriptor by registering(DescriptorProtocTask::class) {
        includeDir.set(project.file("src/jvmTest/resources/protos"))
        outputDir.set(layout.buildDirectory.dir(name))
    }

    getByName("jvmTest").dependsOn(generateTestProtoDescriptor)
}

// Stub javadoc artifact to satisfy Maven Central requirements. It's only required for the jvm target.
// TODO: replace this with a real javadoc jar generated with Dokka
val jvmJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        if (artifactId == "protoc-gen-pbandk-lib-jvm") {
            artifact(jvmJavadocJar)
        }
        configurePbandkPom(project.description!!)
    }
}