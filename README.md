# Polarion ALM extension to execute validation during save/delete actions

This Polarion extension provides possibility to run custom Java code (via hooks), before Polarion saves/deletes a Work Item, Document or Test Run.

## Build

This extension can be produced using maven:
```
mvn clean package
```

## Installation to Polarion

To install extension to Polarion `ch.sbb.polarion.extension.interceptor-<version>.jar`
should be copied to `<polarion_home>/polarion/extensions/interceptor/eclipse/plugins`
It can be done manually or automated using maven build:
```
mvn clean install -P install-to-local-polarion
```
For automated installation with maven env variable `POLARION_HOME` should be defined and point to folder where Polarion is installed.

Changes only take effect after restart of Polarion.

## Interceptor settings/customization
Use Administration -> Interceptor -> Settings page to enable/disable or set specific properties for each existing hook.


## Hooks installation
Copy hook jar to hooks folder (`<polarion_home>/polarion/extensions/interceptor/eclipse/plugins/hooks`) and enforce hooks reloading from the settings page or restart Polarion.
