import java.nio.file.Paths
import java.nio.file.Path
import org.gradle.internal.os.OperatingSystem

// Top-level build configuration

buildscript {
    extra["kotlin_version"] = "1.3.61"
    extra["kotlin_serialization_version"] = "0.14.0"
    extra["protobuf_version"] = "3.11.1"
    extra["spring_boot_gradle_plugin_version"] = "2.1.7.RELEASE"

    repositories { 
        google()
        mavenCentral()
        jcenter()
        maven("https://kotlin.bintray.com/kotlinx")
    }

    dependencies {
        classpath(kotlin("serialization", version = "${Versions.kotlin}"))
    }
}

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "${Versions.kotlin}"
}

allprojects {
    group = "com.github.streem.pbandk"
    version = "0.8.0"

    repositories {
        google()
        mavenCentral()
        jcenter()
        maven("https://kotlin.bintray.com/kotlinx")
    }

    //this.ext["runProtoGen"] =
}

tasks.register("generateProto") {
    dependsOn(":protoc-gen-kotlin:packagePlugin")

    doFirst {
        runProtoGen("src/commonMain/proto", "src/commonMain/kotlin", "pbandk.gen.pb", "debug", null)
    }
}

fun runProtoGen(inPath: String, outPath: String, kotlinPackage: String?, logLevel: String?, inSubPath: String?) {
    // Build CLI args
    var args = mutableListOf("protoc")
    args.add("--kotlin_out=")
    if (kotlinPackage != null) args[-1] += "kotlin_package=$kotlinPackage,"
    if (logLevel != null) args[-1] += "log=$logLevel,"
    args[-1] += "json_use_proto_names=true,"
    args[-1] += "empty_arg:" + Paths.get(outPath)
    args.add("--plugin=protoc-gen-kotlin=" +
            Paths.get(project.rootDir.toString(), "protoc-gen-kotlin/build/install/protoc-gen-kotlin/bin/protoc-gen-kotlin"))
    if (OperatingSystem.current().isWindows) args[-1] += ".bat"
    var includePath = Paths.get(inPath)
    if (!includePath.isAbsolute) includePath = Paths.get(project.projectDir.toString(), inPath)
    args.add("-I$includePath")
    var filePath = includePath
    if (inSubPath != null) filePath = includePath.resolve(inSubPath)
    args.addAll(filePath.toFile().listFiles().filter {
        it.isFile() && it.toString().endsWith(".proto")
    }.map { it.absolutePath })
    // Run itz
    exec { commandLine(args) }
}