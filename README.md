# Polarion ALM extension to execute validation during save/delete actions

This Polarion extension provides possibility to run custom Java code (via hooks), before Polarion saves/deletes a Work Item, Document or Test Run.

## Build

This extension can be produced using maven:
```bash
mvn clean package
```

## Installation to Polarion

To install extension to Polarion `ch.sbb.polarion.extension.interceptor-<version>.jar` should be copied to `<polarion_home>/polarion/extensions/ch.sbb.polarion.extension.interceptor/eclipse/plugins`.
It can be done manually or automated using maven build:
```bash
mvn clean install -P install-to-local-polarion
```
For automated installation with maven env variable `POLARION_HOME` should be defined and point to folder where Polarion is installed.

Changes only take effect after restart of Polarion.

## Polarion configuration

### Interceptor settings/customization
Use Administration -> Interceptor -> Settings page to enable/disable or set specific properties for each existing hook.


### Hooks installation
Copy hook jar to hooks folder (`<polarion_home>/polarion/extensions/ch.sbb.polarion.extension.interceptor/eclipse/plugins/hooks`) and enforce hooks reloading from the settings page or restart Polarion.
By default, newly added hooks are disabled and must be enabled manually.

## Upgrade

### Upgrade from version 1.x.x to 2.0.0
Version 2.0.0 requires the new hooks model therefore hooks built for 1.x.x will stop working. They must be replaced with the new ones built especially for v.2.0.0+.
Also the new version introduced some significant internal settings model changes/improvements which have made old settings data incompatible. This means that during the first run all settings will be reset to their default values (unfortunately revisions history will be lost too).
