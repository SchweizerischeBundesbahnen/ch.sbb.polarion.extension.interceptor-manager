# Changelog

## [2.3.0](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/compare/v2.2.0...v2.3.0) (2024-07-18)


### Features

* app icon added ([#60](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/issues/60)) ([6999a3b](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/commit/6999a3ba90fb18fab7ebc855d3965d5d6e99b991))
* migration to generic v6.2.0 ([#62](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/issues/62)) ([2d35fc9](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/commit/2d35fc9c71adbc11616093be6658e82cede9c4ff))
* support for IModuleComment and IWorkRecord ([#65](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/issues/65)) ([e183022](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/commit/e18302269df6bda6e87b8f3ac3f23410efcfcfb4)), closes [#64](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/issues/64)


### Bug Fixes

* **deps:** update dependency ch.sbb.polarion.extensions:ch.sbb.polarion.extension.generic to v6.1.0 ([f327f3c](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/commit/f327f3c5d0fb0704cd621b1b9ffdab8e8a480cd7))
* **deps:** update dependency ch.sbb.polarion.extensions:ch.sbb.polarion.extension.generic to v6.2.0 ([e1aaac9](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/commit/e1aaac95955441bf598cdbacaa173601125d3684))
* **deps:** update dependency ch.sbb.polarion.extensions:ch.sbb.polarion.extension.generic to v6.3.0 ([8028b60](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/commit/8028b60d8f5dad297d885c6f7c9bc3f053c4d829))

## [2.2.0](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/compare/v2.1.0...v2.2.0) (2024-07-03)


### Features

* about page help generated from README.md ([#53](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/issues/53)) ([f8d482b](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/commit/f8d482b898ff0b59dc8a5f654fd33546b8f7c1fa))
* migrate to generic v6.0.1 ([#56](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/issues/56)) ([b5eb9dc](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/commit/b5eb9dcb31cc3cbf205326dca94dd7b54c99204f))


### Bug Fixes

* **deps:** update dependency ch.sbb.polarion.extensions:ch.sbb.polarion.extension.generic to v6.0.2 ([5fab00a](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/commit/5fab00abeb4fd130b528bed1868d59346701e725))
* display version of installed hooks ([#55](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/issues/55)) ([9deb61d](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/commit/9deb61d36ddde6e974645490176d6cc9ced4e96b))

## [2.1.0](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/compare/v2.0.0...v2.1.0) (2024-06-20)


### Features

* Hook should be disabled by default. ([#49](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/issues/49)) ([30409f7](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/commit/30409f734047245a3011798b46b48eda5e678433))
* the version of hook should not be stored in Java code, but read… ([#50](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/issues/50)) ([d4c78c0](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager/commit/d4c78c0ecd87637d006aa0f7e6daadced1807464))

## [2.0.0](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/compare/v1.1.1...v2.0.0) (2024-06-04)


### ⚠ BREAKING CHANGES

* save/delete methods proxy calls improvement, introduced separate pre&post hook methods
* save/delete methods proxy calls improvement, introduced separa… ([#39](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/issues/39))
* ch.sbb.polarion.extension.generic usage ([#36](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/issues/36))

### Features

* ch.sbb.polarion.extension.generic usage ([#36](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/issues/36)) ([ca4ca3b](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/commit/ca4ca3b418a2aadce21d2340fa651ee09703c601))
* save/delete methods proxy calls improvement, introduced separa… ([#39](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/issues/39)) ([3176de3](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/commit/3176de34ea5842b292539b9cd981f3521f7e4af8))
* save/delete methods proxy calls improvement, introduced separate pre&post hook methods ([3176de3](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/commit/3176de34ea5842b292539b9cd981f3521f7e4af8))

## [1.1.1](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/compare/v1.1.0...v1.1.1) (2024-03-05)


### Bug Fixes

* Fix settings save in nested transaction. ([#23](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/issues/23)) ([775269b](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/commit/775269b7c6ad7f05f2275289ba3809e91f698098))

## [1.1.0](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/compare/v1.0.0...v1.1.0) (2024-01-18)


### Features

* one hook can be applied for multiple object instances and example added ([#16](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/issues/16)) ([a68cb06](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/commit/a68cb06045f4fd5d9c27370b79a4f4f47dbc20da))


### Bug Fixes

* add missing deployment profiles ([#12](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/issues/12)) ([b8d7102](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/commit/b8d7102d76b49796c9ba313111afff5f928d5f87))

## 1.0.0 (2024-01-16)


### Features

* initial implementation ([#2](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/issues/2)) ([e5f0955](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor/commit/e5f095500d039356234a33ec6759b7c0ed8d1707))
