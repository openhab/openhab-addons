# AGENTS.md - openHAB KNX Add-on Development Guide

## KNX Binding Specifics

You are now a **KNX expert** working on the openHAB KNX binding - connecting openHAB with **KNX installations** (European standard for home and building automation).

### KNX Protocol Overview

- **Standard**: ISO/IEC 14543-3 for home and building automation
- **Reference**: [KNX Wikipedia](https://en.wikipedia.org/wiki/KNX)
- **Applications**: Lighting, HVAC, security, energy management
- **Architecture**: Decentralized bus-based communication
- KNX specification v3.0 is now available for free from [knx.org](https://my.knx.org/de/shop/knx-specifications)

## Technical Foundation

### Calimero Library Dependencies

| Component | Repository | Purpose |
|-----------|------------|---------|
| **Calimero Core** | [calimero-core](https://github.com/calimero-project/calimero-core) | Core KNX protocol implementation |
| **Calimero Device** | [calimero-device](https://github.com/calimero-project/calimero-device) | KNX device abstraction layer |
| **Documentation** | [calimero-project.github.io](https://calimero-project.github.io/) | API docs and guides |

**Key Features**: Pure Java KNX stack, supports KNXnet/IP, KNX RF, KNX PL110

### KNX Data Point Types (DPTs)

Purpose: To enable interworking between different KNX devices, the data representation has been defined in the KNX specification as Datapoint Types. DPTs ensure consistent data interpretation across all KNX devices regardless of manufacturer.

**Official DPT Specification**: [KNX DPT Reference PDF](https://support.knx.org/hc/en-us/article_attachments/15392631105682)

**Common DPT Categories:**

- **DPT 1.x**: Boolean (switches, binary sensors)
- **DPT 5.x**: 8-bit unsigned (dimming, blinds)
- **DPT 9.x**: 16-bit float (temperature, humidity)
- **DPT 14.x**: 32-bit float (energy, power)

## KNX Security Support

- **KNX IP Secure**: Fully supported for both secure interfaces and secure routers
- **KNX Data Secure**: Read-only support - can decode secure data but cannot send Data Secure telegrams

## KNX-Specific Development

### Testing & Debugging

#### KNX-Specific Testing

- **Mock Infrastructure**: Use Calimero test utilities for unit tests
- **Hardware Testing**: Requires KNX IP Interface and ETS software
- **DPT Validation**: Test all supported data type conversions

#### Code Coverage

Results available in `target/site/jacoco/index.html` after running tests (as per root AGENTS.md).

#### Debug Logging

```shell
# KNX-specific debug logging, set to DEBUG or TRACE
log:set DEBUG tuwien.auto.calimero
log:set DEBUG org.openhab.binding.knx
```

## Common KNX Issues

### Troubleshooting

- **Gateway Discovery**: KNXnet/IP multicast issues in containerized environments
- **Group Address Conflicts**: Validate ETS project configuration
- **DPT Mismatches**: Ensure correct openHAB ↔ KNX data type mapping

### Hardware Requirements

- **KNX IP Interface**: Required for real KNX network testing
- **ETS Software**: KNX Engineering Tool Software for device configuration
- **Test Devices**: Various KNX actuators/sensors for comprehensive testing

## KNX Resources

- **KNX Association**: [knx.org](https://www.knx.org) - Standards and certification
- **ETS Software**: Essential for KNX project management
- **Calimero Issues**: [GitHub Issues](https://github.com/calimero-project/calimero-core/issues) for library bugs
- **openHAB KNX Add-on Issues**: [GitHub Issues](https://github.com/openhab/openhab-addons/issues?q=is%3Aissue%20is%3Aopen%20in%3Atitle%20knx). This can also be fetched by running gh issue list --repo openhab/openhab-addons --search "in:title knx"
