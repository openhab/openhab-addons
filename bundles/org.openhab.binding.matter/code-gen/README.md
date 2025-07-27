# Matter Code Generator

This system generates Java classes for Matter clusters, device types, and related functionality. It uses Handlebars templates to transform Matter.js protocol definitions into Java code suitable for serialization.

## Overview

The code generator consists of:

- `app.ts`: Main generator script that processes Matter.js definitions and generates Java code
- Template files in `src/templates/`:
  - `cluster-class.hbs`: Template for individual cluster classes
  - `base-cluster.hbs`: Template for the base cluster class
  - `cluster-constants.hbs`: Template for cluster constants
  - `cluster-registry.hbs`: Template for cluster registry
  - `device-types-class.hbs`: Template for device type definitions
  - `data-types-class.hbs`: Template for data type definitions

## Main Generator (app.ts)

The generator script:

1. Imports Matter.js protocol definitions
1. Maps Matter.js data types to Java types
1. Processes cluster inheritance and references between clusters
1. Compiles Handlebars templates
1. Generates Java code files in the `out/` directory

## Templates

### cluster-class.hbs

Generates individual cluster classes with:

- Cluster attributes
- Struct definitions  
- Enum definitions
- Command methods
- toString() implementation

### base-cluster.hbs

Generates the base cluster class with:

- Common fields and methods
- Global struct/enum definitions

### cluster-constants.hbs

Generates constants for:

- Channel names
- Channel labels
- Channel IDs
- Channel type UIDs

note this is not currently used yet in the binding

### cluster-registry.hbs

Generates a registry mapping cluster IDs to cluster classes

### device-types-class.hbs

Generates device type definitions and mappings

## Usage

1. Install dependencies:
1. Run the generator:
1. Generated Java files will be in the `out/` directory

```bash
npm install && npm run start
```

Note the the maven pom.xml will execute these steps when building the project, including linting the generated files and moving them from the out directory to the primary addon src directory.

## Handlebars Helpers

The generator includes several Handlebars helpers for string manipulation to assist in Java naming conventions:

- `asUpperCase`: Convert to uppercase
- `asLowerCase`: Convert to lowercase  
- `asUpperCamelCase`: Convert to UpperCamelCase
- `asLowerCamelCase`: Convert to lowerCamelCase
- `asTitleCase`: Convert to Title Case
- `asEnumField`: Convert to ENUM_FIELD format
- `asHex`: Convert number to hex string
- and many others
