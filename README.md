[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager&metric=bugs)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager&metric=coverage)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.interceptor-manager)

# Polarion ALM extension to execute validation during save/delete actions

This Polarion extension provides possibility to run custom Java code (via hooks), before Polarion saves/deletes a Work Item, Document, Plan or Test Run.

> [!IMPORTANT]
> Starting from version 4.0.0 only latest version of Polarion is supported.
> Right now it is Polarion 2506.

## Quick start

The latest version of the extension can be downloaded from the [releases page](../../releases/latest) and installed to Polarion instance without necessity to be compiled from the sources.
The extension should be copied to `<polarion_home>/polarion/extensions/ch.sbb.polarion.extension.intercaptor-manager/eclipse/plugins` and changes will take effect after Polarion restart.
> [!IMPORTANT]
> Don't forget to clear `<polarion_home>/data/workspace/.config` folder after extension installation/update to make it work properly.

## Build

This extension can be produced using maven:

```bash
mvn clean package
```

## Installation to Polarion

To install extension to Polarion `ch.sbb.polarion.extension.interceptor-manager-<version>.jar`
should be copied to `<polarion_home>/polarion/extensions/ch.sbb.polarion.extension.interceptor-manager/eclipse/plugins`.
It can be done manually or automated using maven build:

```bash
mvn clean install -P install-to-local-polarion
```

For automated installation with maven env variable `POLARION_HOME` should be defined and point to folder where Polarion is installed.

Changes only take effect after restart of Polarion.

## Polarion configuration

### Interceptor Manager settings/customization

Use Administration -> Interceptor Manager -> Settings page to enable/disable or set specific properties for each existing hook.

### Hooks installation

#### Standalone jar hooks

Copy hook jar to hooks folder (`<polarion_home>/polarion/extensions/ch.sbb.polarion.extension.interceptor-manager/eclipse/plugins/hooks`) and enforce hooks reloading from the settings page or restart Polarion.
By default, newly added hooks are disabled and must be enabled manually.

#### Hooks from Different Polarion Extension

Hooks can be located in another Polarion extension. In this case, the following two approaches to register the hooks are supported:

- OSGi Services, illustrated in example: [Delete Non-resolved Module Comments as OSGi Service](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager.hook-samples/tree/main/hook-samples-osgi)
- Google Guice, illustrated in example: [Delete Non-resolved Module Comments as Guice Module](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager.hook-samples/tree/main/hook-samples-guice)

### REST API

This extension provides REST API. OpenAPI Specification can be obtained [here](docs/openapi.json).

## Upgrade

### Upgrade from version 2.x.x to 3.0.0

Version 3.0.0 has received significant change - project+package rename. Verify you're using the new subfolder name in the `extensions` folder.
Also, this change prevents previous hooks versions from working. They must be replaced with the new ones built especially for v.3.0.0+. Note that doing this will reset hooks settings tho their default values, so do not forget to backup your
actual settings to restore them after update.

### Upgrade from version 1.x.x to 2.0.0

Version 2.0.0 requires the new hooks model therefore hooks built for 1.x.x will stop working. They must be replaced with the new ones built especially for v.2.0.0+.
Also the new version introduced some significant internal settings model changes/improvements which have made old settings data incompatible. This means that during the first run all settings will be reset to their default values (
unfortunately revisions history will be lost too).
