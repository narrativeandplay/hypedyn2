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

## Contributing

This project follows standard gitflow conventions, with the only deviation from standard conventions being that the
`develop` branch is named `development` instead.

## License
