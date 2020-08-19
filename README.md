# Streamy
[![TravisCI](https://travis-ci.com/amannocci/streamy.svg?branch=master)](https://travis-ci.com/github/amannocci/streamy)
[![codecov](https://codecov.io/gh/amannocci/streamy/branch/master/graph/badge.svg)](https://codecov.io/gh/amannocci/streamy)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Famannocci%2Fstreamy.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Famannocci%2Fstreamy?ref=badge_shield)

*Streamy is a tool for managing events and logs. You can use it to collect logs, parse them, and store them for later use.
It is fully free and fully open source. The license is MIT, meaning you are pretty much free to use it however you want in whatever way.*
* [Source](https://github.com/amannocci/streamy)
* [Documentation](https://amannocci.github.io/streamy-docs/)
* [Issues](https://github.com/amannocci/streamy/issues)
* [Contact](mailto:adrien.mannocci@gmail.com)

## Prerequisites
* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) for build
* [Java 11](http://www.oracle.com/technetwork/java/javase/downloads/index.html) for runtime
* [Sbt](http://www.scala-sbt.org/)
* [Bats](https://github.com/sstephenson/bats)

## Features
* Coming soon

## Setup
The following steps will ensure your project is cloned properly.
1. git clone https://github.com/amannocci/streamy
2. cd streamy && ./scripts/workflow.sh setup

## Build
* To build you have to use the workflow script.

```bash
./scripts/workflow.sh build
```

* It will compile project code with the current environment.

## Test
* To test `streamy` you have to use the workflow script.
* Tests are based on sbt and testcontainers capabilities.

```bash
./scripts/workflow.sh test
```

## Package
* This project contains best effort debian packaging support.
* To package `streamy` you have to use the workflow script.

```bash
./scripts/workflow.sh package
```

## Release (or prepare)
* To release or prepare a release you have to use the workflow script.

```bash
./scripts/workflow.sh release
```

## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Famannocci%2Fstreamy.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Famannocci%2Fstreamy?ref=badge_large)