# HypeDyn

HypeDyn (pronounced "hyped in") is a procedural hypertext fiction authoring tool for people who want to create 
text-based interactive stories that adapt to reader choice.

This project is split into several modules:

- `hypedyn-api` - The API that the other modules and plugins use to communicate
- `hypedyn-core` - The backend of the system, handling things such as data structure management, serialisation, etc.
- `hypedyn-default-story-viewer` - A plugin that is the default implementation for visualising a HypeDyn story
- `hypedyn-ui` - The UI of the system, including the main entry point into the application

## System Requirements

* Java 8u60 or later

## Installation
 
The latest stable installers can be found at http://www.narrativeandplay.org/hypedyn/.

[Gradle](http://gradle.org) is used as the build tool for this project; more information about Gradle can be 
found at its website.

To run the source version (if you cloned it from this repo), run the following command in a terminal:

```
gradlew run
```

On Windows, `gradlew` may need to be replaced with `gradlew.bat`.

To execute the tests, run `gradlew test`.

### Building packages

The Gradle JavaFX plugin used in the project will by default build the appropriate packages for the system it is 
being built on. On OS X, this will automatically build a DMG. On other platforms, this defaults to a folder with all
the necessary files.

On Windows, if Inno Setup is installed and available on the PATH, an installable exe can be created
(details [here](https://bitbucket.org/shemnon/javafx-gradle/issues/20/native-installers-not-create-on-windows) for how to correctly place the path onto the PATH variable).

On Linux, if the required build tools are present (only for RPM and DEB based distros), the appropriate package
can be built. Details [here](http://docs.oracle.com/javafx/2/deployment/self-contained-packaging.htm).


## Contributing

This project follows standard gitflow conventions, with the only deviation from standard conventions being that the
`develop` branch is named `development` instead.

## License

Copyright (C) 2015  National University of Singapore

Licensed under the GNU General Public License v3. See LICENSE for details.
