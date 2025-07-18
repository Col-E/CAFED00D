# CAFEDOOD [![](https://jitpack.io/v/Col-E/CAFED00D.svg)](https://jitpack.io/#Col-E/CAFED00D) ![](https://github.com/Col-E/CAFED00D/actions/workflows/display_test_results.yml/badge.svg)

Another class library with a focus on obfuscation support.

## Features

* Supports [Oak](https://en.wikipedia.org/wiki/Oak_(programming_language)) classes
* Drop malformed attributes from classes added by obfuscators
* Rewrite [internal jvm instructions](https://github.com/openjdk/jdk/blob/769f14db847813f5a3601e9ec21e6cadbd99ee96/src/hotspot/share/interpreter/bytecodes.cpp#L491)

## Usage

### Add dependency

Add CafeDude dependency _(where `VERSION` is the latest version)_
```xml
<dependency>
    <groupId>software.coley</groupId>
    <artifactId>cafedude-core</artifactId>
    <version>VERSION</version>
</dependency>
```
```groovy
implementation 'software.coley:cafedude-core:VERSION'
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

### Stripping reserved hotspot instructions from classes

If you encounter a class using reserved hotspot instructions create subclass of `ClassFileReader` which provides `IllegalRewritingInstructionsReader`.

An example of this can be found in [`CrasherPatchingTest.java`](core/src/test/java/software/coley/cafedude/CrasherPatchingTest.java)

### Stripping malformed attributes and data from classes

Additional items can be removed via:
```java
// Modifies the 'cf' instance
new IllegalStrippingTransformer(cf).transform();
```