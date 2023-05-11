# Virtual Pipeline

## Introduction

Virtual Pipeline Jenkins plugin allows the user to define marks (using regular expressions) that can be found in the main log and them visualize them on the Project and Job Page.



Two configurations for a mark are available at the moment:
* Simple - oneline, single regex
* Advanced - start and end regex, option to match lines between

Found marks will be displayed after each Build on the Project page and the Build page with the possibility to:
* jump to the exact position in the log with highlighted marks
* request part of the log based on the marks offset
* compare the marks against previous build
* generate picture of marks as an artifact

## Getting started

The plugin can be built using the command:
```
mvn clean install
```
Created `.hpi` file in the target directory can be imported into a running Jenkins instance in the Manage Jenkins Section.

---
To use the plugin in Jenkins instance, add it to your project as a  `Virtual Pipeline` post-build step and setup the configuration according to your needs.





## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

