# AGENTS.md - openHAB Add-ons Development Guide

## Overview

This repository contains the add-ons for the openHAB smart-home project, featuring approximately 500 different extensions located in the `bundles` folder.

**Key Resources:**
- Developer documentation: https://www.openhab.org/docs/developer/
- Add-on development guide: https://www.openhab.org/docs/developer/addons/
- Core concepts: https://www.openhab.org/docs/concepts/
- Core repository: https://github.com/openhab/openhab-core

## Project Structure

```
repo root folder
├── bundles/           # ~500 different extensions
├── itests/           # Integration tests
├── CODEOWNERS        # Maintainer assignments
└── ...
```

**Important:** This add-ons repository depends on the openhab-core repository, which defines the base system and APIs.

## Development Standards

### Java Version
- **Target:** Java 21
- Use modern language features, but stay within Java 21 bounds
- Avoid preview features or experimental APIs

### Code Style & Documentation

#### Comments and Documentation
- Add meaningful code comments where helpful
- Avoid obvious comments (e.g., `// constructor`)
- Use JavaDoc for API/class/method documentation
- Follow guidelines at: https://www.openhab.org/docs/developer/guidelines.html

#### Import Organization
- Sort imports alphabetically
- Group imports logically (standard library, third-party, openHAB)

#### Formatting
- Use `mvn spotless:apply` to fix formatting issues
- POM sections should be sorted

## Key Concepts to Understand

Before developing, familiarize yourself with these core openHAB concepts:
- [Things](https://www.openhab.org/docs/concepts/things.html) - Physical devices and services
- [Items](https://www.openhab.org/docs/concepts/items.html) - Virtual representations of device features  
- [Units of Measurement](https://www.openhab.org/docs/concepts/units-of-measurement.html) - Type-safe quantity handling

## File-Specific Guidelines

### pom.xml Files
When upgrading Maven dependencies:

1. **Check version consistency across:**
   - `features.xml` files for hardcoded version numbers
   - `*.bndrun` files for hardcoded version numbers

2. **After updates:**
   - Run `mvn spotless:apply` to fix formatting
   - Consider running full Maven build with `-DwithResolver` option

### *.bndrun Files
- Configuration files for integration tests using bndtools
- Reference documentation: https://bnd.bndtools.org/chapters/825-instructions-ref.html
- Used to define OSGi runtime configurations for testing

### AGENTS.md
There might be AGENTS.md files in subfolders. Consider them when files from that binding are open in the editor:
- bundles/org.openhab.*/AGENTS.md

### CODEOWNERS File
- Located at repository root
- Maps GitHub usernames to binding responsibilities
- Automatically updated by binding creation scripts
- Format: `path/to/binding @github-username`

## Creating New Bindings

### Prerequisites
1. Read the binding development guide: https://www.openhab.org/docs/developer/#develop-a-new-binding
2. Choose a unique binding name following naming conventions

### Naming Conventions
- **Binding Name:** Must be CamelCase matching pattern `[A-Z][A-Za-z]*`
- **GitHub Username:** Must match pattern `[a-z0-9]*` 
- **Author Name:** Can contain spaces (use quotes when scripting)

### Creation Process

Important: Run all the following commands from repo root folder.

Check the documentation for how to create new bindings:
https://www.openhab.org/docs/developer/#develop-a-new-binding

Ask the user to name the binding <bindingname> and use the supplied name. It needs to be in CamelCase and match [A-Z][A-Za-z]*.

Make sure that bindings/org.openhab.binding.<bindingname> does not exist.

Do not create folder contents on your own. Run the script create_openhab_binding_skeleton at project root directory with sh or powershell to create.
authorname needs to be in "" as it can contain spaces.
githubusername is [a-z0-9]*.
The authorname may be deduced from git config.

Run mvn spotless:apply in the new folder. Make sure you don't run this at top directory but in the binding folder to avoid long runtime.

### Important Notes
- **Never create binding folders manually** - always use the provided scripts
- The creation script will automatically update the CODEOWNERS file
- Author names containing spaces must be quoted in script parameters

## Testing

### Integration Tests
- Located in `itests/` directory
- Use bndrun configurations to define test environments
- Test against realistic OSGi runtime scenarios

### Build Validation
```bash
# Format code
mvn spotless:apply

# Run tests with dependency resolution
mvn clean install -DwithResolver

# Build and run tests for a single specific binding
mvn clean install -pl org.openhab.binding.bindingname
```

After building, the directory target inside org.openhab.binding.bindingname contains several test reports.
- target/code_analysis/report.html for results of the static code analysis
- target/site/jacoco/index.html contains code coverage (only if available for a specific binding)

## Common Pitfalls

1. **Dependency Management:** Always check `features.xml` and `*.bndrun` files when updating dependencies
2. **Manual Creation:** Don't create binding structures manually - use the skeleton scripts
3. **Import Order:** Unsorted imports will fail style checks
4. **Java Version:** Stay within Java 21 - newer features will break CI/CD

## Getting Help

- **Documentation:** https://www.openhab.org/docs/developer/
- **Community Forum:** https://community.openhab.org/
- **GitHub Issues:** Use for bug reports and feature requests
- **Code Reviews:** Required for all contributions

## Quick Reference

| Task | Command |
|------|---------|
| Format code | `mvn spotless:apply` |
| Create binding | `sh create_openhab_binding_skeleton.sh` |
| Full build | `mvn clean install` |
| Build with resolver | `mvn clean install -DwithResolver` |
| Build a specific binding | `mvn clean install -pl org.openhab.binding.bindingname` |
