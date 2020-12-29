plugins {
    kotlin("multiplatform")
    `maven-publish`
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }

        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(project(":runtime"))
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
                implementation(kotlin("stdlib-jdk8"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.12")
                implementation("com.github.tschuchortdev:kotlin-compile-testing:1.2.9")
            }
        }
    }
}

tasks {
    val generateProto by registering(KotlinProtocTask::class) {
        includeDir.set(project.file("src/commonMain/proto"))
        outputDir.set(project.file("src/commonMain/kotlin"))
        kotlinPackage.set("pbandk.gen.pb")
        logLevel.set("debug")
    }

    getByName("jvmTest").dependsOn(":protoc-gen-kotlin:protoc-gen-kotlin-jvm:installDist")
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        description = "Library for pbandk protoc plugin plugins"
        pom {
            configureForPbandk()
        }
        addBintrayRepository(project, this)
    }
}
