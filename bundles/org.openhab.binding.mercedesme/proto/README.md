# How to protoc

- Check [mvn repository](https://mvnrepository.com/artifact/com.google.protobuf/protobuf-java-util) which version to use
- Download correct protoc compiler from [maven central](https://repo1.maven.org/maven2/com/google/protobuf/protoc/) into `PROTOC_DIR`
- Call in mercedesme binding directory `PROTOC_DIR\protoc -I=proto --java_out=gen proto/*.proto`
- Move generated sources including subdirs from `gen` to `3rdparty\java`
- Adapt `pom.xml` with version of step 1
