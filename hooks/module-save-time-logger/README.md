# Hook example for Polarion Interceptor extension

This hook writes to log module save time.

## Build

This hook can be produced using maven:
```
mvn clean package
```

## Installation to Polarion

To install this hook to Polarion `ch.sbb.polarion.extension.interceptor-hooks.<hookname>-<version>.jar` should be copied to `<polarion_home>/polarion/extensions/ch.sbb.polarion.extension.interceptor/eclipse/plugins/hooks`.
It can be done manually or automated using maven build:
```
mvn clean install -P install-to-local-polarion
```
For automated installation with maven env variable `POLARION_HOME` should be defined and point to folder where Polarion is installed.
