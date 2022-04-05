# CAFEDOOD [![Build Status](https://cloud.drone.io/api/badges/Col-E/CAFED00D/status.svg)](https://cloud.drone.io/Col-E/CAFED00D) [![](https://jitpack.io/v/Col-E/CAFED00D.svg)](https://jitpack.io/#Col-E/CAFED00D)

Another class library with a focus on obfuscation support.

## Features

* Supports [Oak](https://en.wikipedia.org/wiki/Oak_(programming_language)) classes
* Drop malformed attributes from classes added by obfuscators
* Rewrite [internal jvm instructions](https://github.com/openjdk/jdk/blob/769f14db847813f5a3601e9ec21e6cadbd99ee96/src/hotspot/share/interpreter/bytecodes.cpp#L491)

## Usage

### Add dependency

Add Jitpack to your repositories
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Add CafeDude dependency _(where `VERSION` is the latest version)_
```xml
<dependency>
    <groupId>com.github.Col-E</groupId>
    <artifactId>CAFED00D</artifactId>
    <version>VERSION</version>
</dependency>
```
```groovy
implementation 'com.github.Col-E:CAFED00D:VERSION'
```

### Reading and writing classes

The default settings have anti-obfuscation measures enabled.
```java
// Reading
byte[] code = ...
ClassFileReader cr = new ClassFileReader();
// cr.setDropForwardVersioned(false) - enabled by default
// cr.setDropEofAttributes(false) - enabled by default
// cr.setDropDupeAnnotations(false) - enabled by default
ClassFile cf = cr.read(code);
// Writing
code = new ClassFileWriter().write(cf);
```

### Stripping malformed attributes and data from classes

Additional items can be removed via:
```java
// Modifies the 'cf' instance
new IllegalStrippingTransformer(cf).transform();
```