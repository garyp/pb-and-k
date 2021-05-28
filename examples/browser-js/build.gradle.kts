plugins {
    kotlin("js") version "1.4.32" apply false
    id("com.google.protobuf") version "0.8.12" apply false
}

val pbandkVersion by extra("0.10.1-SNAPSHOT")

subprojects {
    repositories {
        if (System.getenv("CI") == "true") {
            mavenLocal()
        }
        mavenCentral()
    }
}
