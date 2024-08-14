import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.24"
    application
    id("com.google.protobuf") version "0.9.4"
}

val protobufVersion by extra("4.26.1")
val pbandkVersion by extra("0.15.0-SNAPSHOT")

repositories {
    if (System.getenv("CI") == "true") {
        mavenLocal()
    }
    mavenCentral()
}

application {
    mainClass = "pbandk.examples.addressbook.MainKt"
    applicationName = "addressbook"
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation("pro.streem.pbandk:pbandk-runtime:$pbandkVersion")
}

protobuf {
    generatedFilesBaseDir = "$projectDir/src"
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("pbandk") {
            artifact = "pro.streem.pbandk:protoc-gen-pbandk-jvm:$pbandkVersion:jvm8@jar"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach { task ->
            task.builtins {
                remove("java")
            }
            task.plugins {
                id("pbandk") {
                    option("kotlin_package=pbandk.examples.addressbook.pb")
                }
            }
        }
    }
}

tasks {
    compileJava {
        enabled = false
    }
}

// Workaround the Gradle bug resolving multi-platform dependencies.
// Fix courtesy of https://github.com/square/okio/issues/647
configurations.forEach {
    if (it.name.toLowerCase().contains("kapt") || it.name.toLowerCase().contains("proto")) {
        it.attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
    }
}
