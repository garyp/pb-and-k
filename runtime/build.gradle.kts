import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    `maven-publish`
    // Fetch/sync remote proto files prior to code gen.
    id("com.tinder.gitquery")
}

val protoDir = "src/commonMain/proto"

gitQuery {
    configFile = "gitquery-proto.yml"
    outputDir = protoDir
    repoDir = "tmp/.gitquery"
}

kotlin {

    jvm {
        withJava()
    }

    js {
        useCommonJs()
        browser {}
        nodejs {}
    }

    iosArm64()
    iosX64()

    // For ARM, should be changed to iosArm32 or iosArm64
    // For Linux, should be changed to e.g. linuxX64
    // For MacOS, should be changed to e.g. macosX64
    // For Windows, should be changed to e.g. mingwX64
    macosX64()
    linuxX64()

    targets.withType<KotlinNativeTarget> {
        val main by compilations.getting {
            defaultSourceSet {
                kotlin.srcDir("src/nativeMain/kotlin")
            }
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:${Versions.kotlinSerialization}")
            }
        }
    }


    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")

            languageSettings.useExperimentalAnnotation("pbandk.ExperimentalProtoJson")
            languageSettings.useExperimentalAnnotation("pbandk.PbandkInternal")
            languageSettings.useExperimentalAnnotation("pbandk.PublicForGeneratedCode")
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }

        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:${Versions.kotlinSerialization}")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Versions.kotlinSerialization}")
                api("com.google.protobuf:protobuf-java:${Versions.protobufJava}")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.12")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:${Versions.kotlinSerialization}")
                implementation(npm("protobufjs", "^${Versions.protobufJs}"))
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

tasks {
    val generateWellKnownTypes by registering(KotlinProtocTask::class) {
        includeDir.set(project.file("$protoDir/src"))
        outputDir.set(project.file("src/commonMain/kotlin"))
        kotlinPackage.set("pbandk.wkt")
        logLevel.set("debug")
        protoFileSubdir("google/protobuf")
        dependsOn(gitQuery)
    }

    val generateKotlinTestTypes by registering(KotlinProtocTask::class) {
        includeDir.set(project.file("src/commonTest/proto"))
        outputDir.set(project.file("src/commonTest/kotlin"))
        kotlinPackage.set("pbandk.testpb")
        logLevel.set("debug")
        protoFileSubdir("pbandk/testpb")
    }

    val generateJavaTestTypes by registering(ProtocTask::class) {
        includeDir.set(project.file("src/commonTest/proto"))
        outputDir.set(project.file("src/jvmTest/java"))
        plugin.set("java")
        protoFileSubdir("pbandk/testpb")
    }

    val generateTestTypes by registering {
        dependsOn(generateKotlinTestTypes, generateJavaTestTypes)
    }

    // DCE is now enable by default in Kotlin 1.3.7x
    // and it doesn't work well with commonJS modules
    // Use of commonJs could be removed since default module is now UMD
    // but would require some code change
    val processDceJsKotlinJs by getting {
        enabled = false
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        artifactId = "pbandk-$artifactId"
        description = "Library for pbandk runtime protobuf code"
        pom {
            configureForPbandk()
        }
        addBintrayRepository(project, this)
    }
}