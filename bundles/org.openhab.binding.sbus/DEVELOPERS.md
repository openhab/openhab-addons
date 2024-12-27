# SBUS Binding Development

This document provides information for developers who want to contribute to the OpenHAB SBUS binding.

## Development Setup

1. Clone the OpenHAB addons repository:
```bash
git clone https://github.com/openhab/openhab-addons.git
cd openhab-addons
```

2. Build the binding:
```bash
cd bundles/org.openhab.binding.sbus
mvn clean install
```

## Project Structure

```
org.openhab.binding.sbus/
├── src/main/java/org/openhab/binding/sbus/
│   ├── handler/           # Thing handlers
│   │   ├── SbusRgbwHandler.java
│   │   ├── SbusSwitchHandler.java
│   │   └── SbusTemperatureHandler.java
│   └── internal/         # Internal implementation
│       └── SbusBridgeHandler.java
└── src/main/resources/
    └── OH-INF/           # OpenHAB configuration files
        ├── binding/      # Binding definitions
        ├── thing/        # Thing type definitions
        └── i18n/        # Internationalization
```

## Key Components

* `SbusBridgeHandler`: Manages the UDP connection to SBUS devices
* `SbusRgbwHandler`: Handles RGBW light control
* `SbusSwitchHandler`: Handles switch control
* `SbusTemperatureHandler`: Handles temperature sensor readings

## Testing

1. Unit Tests
   * Run unit tests with: `mvn test`
   * Add new tests in `src/test/java/`

2. Integration Testing
   * Test with real SBUS devices
   * Verify all supported channels work correctly
   * Test error handling and recovery

## Debugging

1. Enable debug logging in OpenHAB:
```
log:set DEBUG org.openhab.binding.sbus
```

2. Monitor SBUS communication:
```
openhab> sbus:monitor start
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
   * Follow OpenHAB coding guidelines
   * Add appropriate unit tests
   * Update documentation
4. Submit a pull request

### Code Style

* Follow OpenHAB's code style guidelines
* Use the provided code formatter
* Run `mvn spotless:apply` before committing

### Documentation

When adding new features:
1. Update README.md with user-facing changes
2. Update thing-types.xml for new channels/configurations
3. Add appropriate JavaDoc comments
4. Update this DEVELOPERS.md if needed

## Building from Source

```bash
cd openhab-addons/bundles/org.openhab.binding.sbus
mvn clean install
```

The built JAR will be in `target/org.openhab.binding.sbus-[version].jar`
