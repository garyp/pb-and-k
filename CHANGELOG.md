# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [0.10.0] - Unreleased

[0.10.0]: https://github.com/streem/pbandk/compare/v0.9.1...HEAD

### Added

* Support for protobuf [custom options](https://developers.google.com/protocol-buffers/docs/proto#customoptions) on fields. Remaining work is tracked in [#65]. (PR [#103])
    * Protobuf messages that declare an extension range now extend from `ExtendableMessage` rather than `Message`. `ExtendableMessage` defines a `getExtension(FieldDescriptor)` method that can be used to read an extension field (described by the provided `FieldDescriptor`) from a message.
    * Protobuf extension fields are defined as Kotlin extension properties on the extended class and can be accessed like any other Kotlin property.
* `MessageDescriptor` (available via `Message.descriptor` or `Message.Companion.descriptor`) is now part of the public API. Initially only `MessageDescriptor.fields` is public, which provides access to descriptors for all of the message's fields. Additional properties of the message descriptor will be exposed in future versions. Please file an issue on GitHub if there are specific properties you would like to have access to. (PR [#103])
* `FieldDescriptor` (available via `MessageDescriptor.fields`) is now part of the public API. Initially, the field's `name` and `options` are public, allowing access to custom options defined on the field in the `.proto` file. The field's `value` accessor is also public, though this API will probably change before the final 0.10.0 release. (PR [#103])

### Changed

* Updated to Kotlin 1.4. Projects that are still on Kotlin 1.3 should be able to continue using pbandk, but this configuration is only supported on a best-effort basis (please file a GitHub issue with any problems). Projects are encouraged to update to Kotlin 1.4. (PR [#114], fixes [#86])
* **[BREAKING CHANGE]** The API and implementation of `UnknownField` changed significantly. If you access the contents of unknown fields in your code, you will need to update to the new API. The unknown field no longer provides access to a decoded version of the field's wire type. Instead it only provides access to the raw binary encoding of the field. (PR [#103])

### Fixed

* Added workaround for an error in the Kotlin/JS implementation of `protoMergeImpl()` caused by a Kotlin compiler bug (PR [#103])
* Improved the code generated for messages with a large number of fields to handle even more fields (PR [#103])
* Binary decoding of 64-bit numbers under Kotlin/JS with certain inputs that would previously cause a crash. (PR [#103])

[#65]: https://github.com/streem/pbandk/issues/65
[#86]: https://github.com/streem/pbandk/issues/86
[#103]: https://github.com/streem/pbandk/pull/103
[#114]: https://github.com/streem/pbandk/pull/114


## [0.9.1] - 2021-01-07

[0.9.1]: https://github.com/streem/pbandk/compare/v0.9.0...v0.9.1

### Fixed

* Compile error when proto contains a oneof field with same name as the enclosing message (PR [#104], fixes [#47]) (thanks @nbaztec)
* All remaining JSON conformance test failures for numeric values (PR [#105], partially fixes [#72]) (thanks @nbaztec)

[#47]: https://github.com/streem/pbandk/issues/47
[#72]: https://github.com/streem/pbandk/issues/72
[#104]: https://github.com/streem/pbandk/pull/104
[#105]: https://github.com/streem/pbandk/pull/105


## [0.9.0] - 2020-12-23

[0.9.0]: https://github.com/streem/pbandk/compare/v0.8.1...v0.9.0

### Added

* Support for Kotlin/Native (PR [#76], fixes [#19]) (thanks @sebleclerc)
* JSON encoding/decoding improvements:
    * Support for the `json_name` protobuf field option
    * Custom encoding/decoding for most of the protobuf well-known types: `Duration`, `Timestamp`, `Empty`,  `Struct`, `Value`, `NullValue`, `ListValue`, and all wrapper types
    * Added `JsonConfig` class for configuring JSON encoding/decoding at runtime. Currently supported options include `ignoreUnknownFieldsInInput`, `outputDefaultValues`, and `outputProtoFieldNames` (which match the options documented at https://developers.google.com/protocol-buffers/docs/proto3#json_options), and also `compactOutput`.
* New binary encoding and decoding overloads on Kotlin/JVM:
    * `Message.encodeToStream(java.io.OutputStream)`
    * `Message.Companion.decodeFromStream(java.io.InputStream)`
    * `Message.Companion.decodeFromByteBuffer(java.nio.ByteBuffer)`

### Changed

* **[BREAKING CHANGE]** Artifacts are now published to JCenter under new maven coordinates
    * Runtime library: `pro.streem.pbandk:pbandk-runtime-common:0.9.0`, `pro.streem.pbandk:pbandk-runtime-jvm:0.9.0`, `pro.streem.pbandk:pbandk-runtime-js:0.9.0`, `pro.streem.pbandk:pbandk-runtime-native:0.9.0`
    * Code generation plugin for the protobuf compiler: `pro.streem.pbandk:protoc-gen-kotlin-jvm:0.9.0:jvm8@jar`
    * `ServiceGenerator` library: `pro.streem.pbandk:protoc-gen-kotlin-lib-jvm:0.9.0`
* Projects that use `pbandk` can remove the `kotlinx-serialization` gradle plugin and library dependency if they don't use `kotlinx-serialization` themselves. The library is now an internal implementation detail of `pbandk`. (PR [#69], fixes [#61])
* Completely rewritten implementation of JSON encoding/decoding, fixing numerous bugs in the old version. The new implementation is much more compliant with the [official proto3 JSON spec](https://developers.google.com/protocol-buffers/docs/proto3#json). Remaining incompatibilities are tracked in [#72].
* **[BREAKING CHANGE]** Moved all of the binary and JSON encoding functionality, and most of the decoding functionality, from the generated code to the runtime library. Code generated by old versions of pbandk will not run with this new pbandk version.
* **[BREAKING CHANGE]** Changed `Message` so that it no longer extends from itself. Uses of `Message<T>` should be changed to just `Message`. (PR [#69])
* **[BREAKING CHANGE]** Added `@PbandkInternal` and `@PublicForGeneratedCode` annotations on portions of the public API that are only public for pbandk's internal use or for use from pbandk-generated code. Using these APIs outside of pbandk will generate compiler warnings and errors. If you have a need for using any of these APIs from your project, please file an issue on GitHub describing your use case.
* **[BREAKING CHANGE]** Code that calls any of pbandk's JSON encoding/decoding APIs must now opt-in to the `@ExperimentalProtoJson` annotation. See https://kotlinlang.org/docs/reference/opt-in-requirements.html#opting-in-to-using-api for detailed instructions. Once pbandk's JSON APIs are no longer considered experimental, this annotation will be removed.
* **[BREAKING CHANGE]** Update the public API to follow recent naming conventions from the Kotlin standard library: "marshal"/"unmarshal" is replaced with "encode"/"decode", encoding methods are named `encodeTo*`, and decoding methods are named `decodeFrom*`. (PRs [#69], [#90], fixes [#89])
    * The below methods have been renamed and are now defined as extension methods rather than being part of the `Message` or `Message.Companion` interface. Code that calls these methods will now need to import them first.
        * `Message.protoMarshal()` -> `Message.encodeToByteArray()`
        * `Message.Companion.protoUnmarshal(ByteArray)` -> `Message.Companion.decodeFromByteArray(ByteArray)`
        * `Message.jsonMarshal()` -> `Message.encodeToJsonString()`
        * `Message.Companion.jsonUnmarshal(String)` -> `Message.Companion.decodeFromJsonString(String)`
        * `Message.protoMarshal(pbandk.Marshaller)` -> `Message.encodeWith(pbandk.MessageEncoder)`
    * The below methods have been renamed. They will become extension methods in a future pbandk release.
        * `Message.Companion.protoUnmarshal(pbandk.Unmarshaller)` -> `Message.Companion.decodeWith(pbandk.MessageDecoder)`
    * The below methods have been renamed and are now defined as extension methods rather than being part of the `Message` or `Message.Companion` interface. Code that calls these methods will now need to import them first. The `Json` parameter from `kotlinx-serialization` has been replaced with the new `JsonConfig` type from `pbandk`.
        * `Message.jsonMarshal(kotlinx.serialization.json.Json)` -> `Message.encodeToJsonString(pbandk.json.JsonConfig)`
        * `Message.Companion.jsonUnmarshal(kotlinx.serialization.json.Json, String)` -> `Message.Companion.decodeFromJsonString(String, pbandk.json.JsonConfig)`
    * Replaced `Marshaller` and `Unmarshaller` interfaces with `MessageEncoder` and `MessageDecoder` interfaces, which are much simpler and function differently from the previous interfaces
* **[BREAKING CHANGE]** `MessageMap.entries` is now a `Set` instead of a `List`

### Removed

* **[BREAKING CHANGE]** Removed `Sizer` and `Util` from the public API
* **[BREAKING CHANGE]** Removed `UnknownField` constructors and the `UnknownField.size()` method from the public API

### Fixed

* Crash on Android when encoding `google.protobuf.Timestamp` fields to JSON (PR [#82], fixes [#46])
* `StackOverflowError` when generating code for really large `oneof` objects (PR [#52]) (thanks @tinder-aminghadersohi)
* Performance problems when compiling generated code with Kotlin 1.4 (PR [#101], fixes [#94])
* JSON decoding of unknown enum values (PR [#100])
* Map entry size computation for non-MessageMap maps (PR [#99])
* Some bugs with the handling of `packed` fields, binary encoding of enums in Kotlin/JS, and base64 encoding/decoding in Kotlin/JS (PR [#69], fixes [#23])
* Various conformance test failures (PRs [#80], [#82])

[#19]: https://github.com/streem/pbandk/issues/19
[#23]: https://github.com/streem/pbandk/issues/23
[#46]: https://github.com/streem/pbandk/issues/46
[#52]: https://github.com/streem/pbandk/pull/52
[#61]: https://github.com/streem/pbandk/issues/61
[#69]: https://github.com/streem/pbandk/pull/69
[#72]: https://github.com/streem/pbandk/issues/72
[#76]: https://github.com/streem/pbandk/pull/76
[#80]: https://github.com/streem/pbandk/pull/80
[#82]: https://github.com/streem/pbandk/pull/82
[#89]: https://github.com/streem/pbandk/issues/89
[#90]: https://github.com/streem/pbandk/pull/90
[#94]: https://github.com/streem/pbandk/issues/94
[#99]: https://github.com/streem/pbandk/pull/99
[#100]: https://github.com/streem/pbandk/pull/100
[#101]: https://github.com/streem/pbandk/pull/101


## [0.8.1] - 2020-05-22

[0.8.1]: https://github.com/streem/pbandk/compare/v0.8.0...v0.8.1

### Added

* Enable conformance tests for Kotlin/JS

### Changed

* **[BREAKING CHANGE]** Update to kotlin 1.3.72 and kotlinx.serialization 0.20.0. Projects that depend on `pbandk` must be using the same versions of kotlin and kotlinx.serialization. (fixes [#35])
* **[BREAKING CHANGE]** The `pbandk-runtime-common` maven package is now just called `pbandk-runtime`. The `protoc-gen-kotlin-jvm` maven package is now called `protoc-gen-kotlin-lib-jvm`. The `pbandk-runtime-jvm` and `pbandk-runtime-js` packages have not changed names.
* Use the new Kotlin multiplatform gradle plugin instead of the deprecated plugin (thanks @sebleclerc)
* Switch from Groovy to Kotlin for build.gradle files (thanks @sebleclerc)
* Update to gradle 6.2.2 (thanks @sebleclerc)
* Use the `maven-publish` gradle plugin instead of the `maven` plugin (thanks @sebleclerc)

### Fixed

* Name collision with nested types (fixes [#24]) (thanks @sebleclerc)
* Fix gradle configs for all examples and build them in CI so they don't break in the future (fixes [#37] and [#41])

[#24]: https://github.com/streem/pbandk/issues/24
[#35]: https://github.com/streem/pbandk/issues/35
[#37]: https://github.com/streem/pbandk/issues/37
[#41]: https://github.com/streem/pbandk/issues/41
