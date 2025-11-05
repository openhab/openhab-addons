# Ruuvi Air openHAB Integration Implementation Plan

## Executive Summary

This document outlines the implementation plan for adding Ruuvi Air support to the openHAB ecosystem via both Bluetooth Low Energy (BLE) and Ruuvi Gateway MQTT payloads. Ruuvi Air is an advanced indoor air quality monitor that extends beyond the existing RuuviTag environmental sensors with additional air quality measurements including PM2.5, CO2, VOC, NOx, and luminosity.

## 1. Maintainer Discussion & Decisions

### 1.1 Discussion with Bluetooth Maintainers (@cdjackson, @cpmeister)

**Background:**
- openHAB has support for Ruuvi Tag (BLE temperature/humidity sensor) in `bluetooth.ruuvitag`
- Ruuvi Air is a new device from the same vendor (BLE air quality sensor)
- Advertisement formats share common structure between devices

**Question 1: Separate or Combined Binding?**

> Should I create new addon for the bluetooth Ruuvi Air support? Or would it be ok to combine the new functionality with the existing bluetooth.ruuvitag binding?

**Decision: ✅ COMBINE WITH EXISTING BINDING**

**Rationale:**
- Most convenient for users
- Ruuvi users likely to have multiple devices from same vendor
- Things will be separated into their own types
- Binding name (`bluetooth.ruuvitag`) is specific, but functionality is acceptable
- **Preference: Combine and ignore naming topic**

**Question 2: Sharing Parsing Logic**

> There will be common parsing logic for Air payload in both `binding.mqtt.ruuvigateway` (BLE payloads republished via MQTT) and bluetooth parser. What would be the best way to share the code?

**Current State:**
- Both bindings rely on https://github.com/Scrin/ruuvitag-common-java
- Does not support Ruuvi Air at this moment
- Maintainer is active in Ruuvi community but moved to golang

**Options Considered:**

1. **Option 1**: Contribute to ruuvitag-common-java (fork to openhab org if maintainer unreachable), take as Maven dependency
2. **Option 2**: Make simple parsing library in openHAB org, embed at compile-time to both bindings, store JAR in binding repo
3. **Option 3**: Copy-paste parsing code to both bindings as static utility

**Decision: ✅ OPTION 2 (with fork of ruuvitag-common-java)**

**Implementation:**
- Take ruuvitag-common-java and expand it with Ruuvi Air support
- Fork to `/home/salski/src/openhab-main/git/ruuvitag-common-java` (already done)
- **Embed dependency at compile-time** using BND tool
- Reference: https://www.openhab.org/docs/developer/buildsystem.html#embedding-dependency
- No separate bundle deployment needed
- Both `bluetooth.ruuvitag` and `mqtt.ruuvigateway` embed the same parser

**Rationale:**
- Keeps shared logic in one place
- Simpler than managing external dependency
- Follows documented openHAB practice
- Both bindings use identical parser code

### 1.2 Implementation Summary

**Final Approach:**
1. ✅ **Single binding**: Extend existing `bluetooth.ruuvitag` to support both RuuviTag and Ruuvi Air
2. ✅ **Single MQTT binding**: Extend existing `mqtt.ruuvigateway` to support both devices
3. ✅ **Shared parser**: Fork and extend `ruuvitag-common-java`, embed at compile-time in both bindings
4. ✅ **Naming**: Keep `bluetooth.ruuvitag` name despite supporting Ruuvi Air

## 2. Product Overview

### 2.1 Ruuvi Air Specifications

**Sensor Capabilities:**
- **Temperature**: -20°C to +60°C (same sensor as RuuviTag)
- **Humidity**: 0-100% RH (same sensor as RuuviTag)
- **Air Pressure**: Barometric measurements (same sensor as RuuviTag)
- **PM (Particulate Matter)**: PM1.0, PM2.5, PM4.0, PM10.0 (µg/m³)
- **CO2**: 0-40,000 ppm (eCO2 equivalent), resolution 1 ppm, ±(50 ppm + 2.5%) accuracy in 400-1000 ppm range
- **VOC Index**: 0-500 (unitless, mimics human nose perception)
- **NOx Index**: 0-500 (unitless)
- **Luminosity**: Measured in Lux (logarithmic scale)
- **IAQS (Indoor Air Quality Score)**: 0-100 proprietary score based on PM2.5 and CO2

**Hardware:**
- Primary sensor module: Sensirion SEN66 (handles CO2, PM, VOC, NOx, temperature, humidity)
- Bluetooth: BLE with 1-second broadcast interval
- Storage: 10 days of history at 5-minute logging interval

### 1.2 Data Formats

Ruuvi Air uses **two BLE advertisement data formats**:

1. **Data Format 6** - Bluetooth 4.x compatible
2. **Data Format E1 (Extended v1)** - Bluetooth 5.0+ (preferred format)

**Key Characteristics:**
- Manufacturer ID: 0x0499 (transmitted as 0x9904 little-endian)
- Both formats broadcast environmental + air quality data
- BT5.0+ devices should prefer E1 format over Format 6
- Measurement sequence counter tracks packets for quality/loss detection

## 2. Existing Implementation Analysis

### 2.1 Bluetooth RuuviTag Binding (org.openhab.binding.bluetooth.ruuvitag)

**Architecture:**
```
BluetoothAdapter (Bridge)
  ↓
BeaconBluetoothHandler (base class)
  ↓
RuuviTagHandler (extends BeaconBluetoothHandler)
  ↓
AnyDataFormatParser (fi.tkgwf.ruuvi:ruuvitag-common:1.0.2)
  ↓
Channel Updates
```

**Key Components:**
- `RuuviTagHandler.java` - Main handler, processes BLE scan notifications
- `RuuviTagDiscoveryParticipant.java` - Auto-discovery by manufacturer ID 1177
- `RuuviTagHandlerFactory.java` - Creates thing handlers
- `RuuviTagBindingConstants.java` - Channel/thing type definitions

**Supported Channels (11):**
- temperature, humidity, pressure
- batteryVoltage, txPower
- accelerationx, accelerationy, accelerationz
- dataFormat, measurementSequenceNumber, movementCounter
- rssi (inherited)

**Data Parsing:**
- Uses external library `fi.tkgwf.ruuvi:ruuvitag-common:1.0.2`
- `AnyDataFormatParser` automatically detects format (3, 5, etc.)
- Supports RuuviTag Data Formats 3 and 5
- **Does NOT support Data Format 6 or E1 (Ruuvi Air formats)**

**Heartbeat Mechanism:**
- 60-second timeout if no BLE advertisements received
- Sets device OFFLINE and clears channel values

### 2.2 MQTT RuuviGateway Binding (org.openhab.binding.mqtt.ruuvigateway)

**Architecture:**
```
MQTT Broker Bridge
  ↓
AbstractMQTTThingHandler (MQTT generic binding)
  ↓
RuuviTagHandler (specific implementation)
  ↓
GatewayPayloadParser (parses JSON)
  ↓
AnyDataFormatParser (fi.tkgwf.ruuvi:ruuvitag-common:1.0.2)
  ↓
Channel Updates
```

**Key Components:**
- `handler/RuuviTagHandler.java` - MQTT message handler
- `parser/GatewayPayloadParser.java` - Parses gateway JSON payloads
- `discovery/RuuviGatewayDiscoveryService.java` - MQTT topic discovery
- `RuuviTagHandlerFactory.java` - Creates thing handlers

**Supported Channels (15):**
- All 11 from BLE binding, plus:
- rssi (gateway-to-tag signal strength)
- ts (message received timestamp)
- gwts (gateway relay timestamp)
- gwmac (gateway MAC address)

**MQTT Payload Format:**
```json
{
  "gw_mac": "DE:AD:BE:EF:00:00",
  "rssi": -82,
  "aoa": [],
  "gwts": "1659365432",
  "ts": "1659365222",
  "data": "0201061BFF99040512FC5394C37C0004FFFC040CAC364200CDCBB8334C884F",
  "coords": ""
}
```

**Data Parsing:**
- Extracts hex-encoded BLE advertisement from `data` field
- Validates manufacturer-specific data marker (0xFF at byte 4)
- Uses same `AnyDataFormatParser` as BLE binding
- **Does NOT support Data Format 6 or E1 (Ruuvi Air formats)**

**MQTT Topic Structure:**
```
ruuvi/{gateway_id}/{tag_mac_address}
```

### 2.3 Integration Tests (org.openhab.binding.mqtt.ruuvigateway.tests)

**Test Coverage:**
- Happy path with valid RuuviTag data
- Parameterized tests for invalid payloads (6+ cases)
- Status lifecycle (initialization, online, offline, timeout)
- Channel linking and state synchronization
- Discovery service tests

**Test Infrastructure:**
- Embedded Moquette MQTT broker
- OSGi test framework (JavaOSGiTest)
- JUnit 5 + Mockito
- Uses real MQTT publishing (not mocks)

## 3. Reference Implementations

### 3.1 Home Assistant

**RuuviTag BLE Integration:**
- Component: `homeassistant/components/ruuvitag_ble/`
- Maintainer: @akx
- Uses external parser library for data extraction
- Automatic discovery via Bluetooth integration
- ~1,438 active installations

**Ruuvi Gateway Integration:**
- Component: `homeassistant/components/ruuvi_gateway/`
- Added in Home Assistant 2023.2
- ~114 active installations
- MQTT-based, local communication

**Implementation Notes:**
- Python-based with standard BLE libraries
- Leverages Home Assistant's Bluetooth framework
- Entity creation per measurement channel

### 3.2 Ruuvi Official Resources

**Protocol Documentation:**
- **ARCHIVED Repository**: `github.com/ruuvi/ruuvi-sensor-protocols` (archived March 10, 2022)
  - Contains historical reference only
  - Notice: "Please go to docs.ruuvi.com for up-to-date information"
- **Current Documentation**: `docs.ruuvi.com`
  - Official documentation site with up-to-date specifications
- **Documentation Source**: `github.com/ruuvi/docs`
  - Contains markdown source files for all formats
  - Includes `/communication/bluetooth-advertisements/data-format-6.md` (20 bytes payload)
  - Includes `/communication/bluetooth-advertisements/data-format-e1.md` (40 bytes payload)
  - Both formats documented with test vectors
- Manufacturer ID: 0x0499 (0x9904 in raw data)

**Parsing Library:**
- `fi.tkgwf.ruuvi:ruuvitag-common:1.0.2` (current version in openHAB)
- Supports formats 3 and 5 only
- **Needs update/replacement for Format 6 and E1 support**

## 4. Implementation Requirements

### 4.1 New Channels for Ruuvi Air

The following channels need to be added to support Ruuvi Air's air quality measurements:

| Channel ID | Item Type | Unit | Description | Format 6 | Format E1 |
|------------|-----------|------|-------------|----------|-----------|
| `pm1` | Number:Density | µg/m³ | PM1.0 particulate matter | ❌ Not available | ✅ 0-1000, 0.1/bit |
| `pm25` | Number:Density | µg/m³ | PM2.5 particulate matter | ✅ 0-1000, 0.1/bit | ✅ 0-1000, 0.1/bit |
| `pm4` | Number:Density | µg/m³ | PM4.0 particulate matter | ❌ Not available | ✅ 0-1000, 0.1/bit |
| `pm10` | Number:Density | µg/m³ | PM10 particulate matter | ❌ Not available | ✅ 0-1000, 0.1/bit |
| `co2` | Number:Dimensionless | ppm | CO2 concentration | ✅ 0-40000, 1 ppm | ✅ 0-40000, 1 ppm |
| `vocIndex` | Number:Dimensionless | (unitless) | Volatile Organic Compounds index | ✅ 0-500 (9-bit) | ✅ 0-500 (9-bit) |
| `noxIndex` | Number:Dimensionless | (unitless) | Nitrogen oxides index | ✅ 0-500 (9-bit) | ✅ 0-500 (9-bit) |
| `luminosity` | Number:Illuminance | lux | Light intensity | ✅ 0-65535 (logarithmic, 8-bit) | ✅ 0-144284 (linear, 24-bit, 0.01 res) |

**Notes:**
- **Format 6** (BT4 compatible): 20-byte payload, PM2.5 only, logarithmic luminosity
- **Format E1** (BT5+ extended): 40-byte payload, all 4 PM sizes, linear luminosity, full MAC address
- Density unit for PM measurements: µg/m³ (micrograms per cubic meter)
- VOC average baseline is 100 (values <100 = improving, >100 = worsening)
- NOx baseline is 1 (values >1 = more nitrogen oxides than usual)
- PM measurements are cumulative: PM10 includes PM4, which includes PM2.5, which includes PM1.0
- All existing RuuviTag channels remain supported (temperature, humidity, pressure, accelerometer, battery, etc.)
- IAQS (Indoor Air Quality Score 0-100) is calculated by Ruuvi Station app, not transmitted in BLE packets

### 4.2 Data Format Support - REVISED APPROACH

**Selected: Compile-Time Embedding of Parser Dependency** ✅

**Implementation Strategy:**
- Fork https://github.com/Scrin/ruuvitag-common-java (already cloned at `/home/salski/src/openhab-main/git/ruuvitag-common-java`)
- Add Format 6 and Format E1 parser support to the fork
- Keep existing Format 3/5 support intact
- **Use BND tool to embed the dependency at compile-time** into both bindings
- Reference: https://www.openhab.org/docs/developer/buildsystem.html#embedding-dependency

**Why Compile-Time Embedding (Decision from Maintainer Discussion):**
- No separate bundle deployment needed
- Simpler dependency management for small libraries
- Both BLE and MQTT bindings embed the same parser library
- Follows documented openHAB practice: https://www.openhab.org/docs/developer/buildsystem.html#embedding-dependency
- Easier testing and deployment
- Avoids complexity of separate bundle versioning
- **Preferred by implementation team for simplicity**

**BND Configuration (pom.xml):**
```xml
<plugin>
  <groupId>biz.aQute.bnd</groupId>
  <artifactId>bnd-maven-plugin</artifactId>
  <configuration>
    <bnd><![CDATA[
      -includeresource: @ruuvitag-common-java-*.jar!/!META-INF/*
    ]]></bnd>
  </configuration>
</plugin>
```

**Test Requirements:**
- Use official Ruuvi test vectors from docs.ruuvi.com
- **Exact references to source in JavaDoc** (e.g., "Test vector from https://docs.ruuvi.com/communication/bluetooth-advertisements/data-format-6")
- Test cases: valid data, maximum values, minimum values, invalid values
- **Generate MQTT gateway test data from Bluetooth test vectors**
- Ruuvi also documents gateway MQTT format - use as reference

### 4.3 Discovery Requirements

**BLE Binding:**
- Extend `RuuviTagDiscoveryParticipant` to detect Ruuvi Air devices
- Both use same manufacturer ID (0x0499/1177)
- Differentiate by data format byte (06 or E1)
- Create appropriate thing type (`ruuviair_beacon` vs `ruuvitag_beacon`)

**MQTT Binding:**
- `RuuviGatewayDiscoveryService` should work without changes
- Discovery creates things based on MQTT topics
- Thing type determination based on first received data format

## 5. Implementation Plan - REVISED

### 5.1 Phase 1: Fork and Extend ruuvitag-common-java

**Repository:** `/home/salski/src/openhab-main/git/ruuvitag-common-java` (already cloned)

**Tasks:**
1. **Add Format 6 Parser** (Priority 1)
   - Create parser for 20-byte Format 6 payload
   - Implement all field extractions (temp, humidity, pressure, PM2.5, CO2, VOC, NOx, luminosity)
   - Handle "not available" sentinel values (0xFFFF unsigned, 0x8000 signed, etc.)
   - **Use exact test vectors from https://docs.ruuvi.com/communication/bluetooth-advertisements/data-format-6**
   - **JavaDoc must include exact source reference with URL**

2. **Add Format E1 Parser** (Future Enhancement)
   - Create parser for 40-byte Format E1 payload
   - Support all PM sizes (1.0, 2.5, 4.0, 10.0)
   - Linear luminosity parsing (24-bit, 0.01 resolution)
   - 24-bit sequence counter
   - Full 48-bit MAC address
   - **Mark as experimental pending BT5 extended advertisement support**

3. **Extend RuuviMeasurement Model**
   - Add fields for Format 6: pm25, co2, vocIndex, noxIndex, luminosity
   - Add fields for Format E1: pm1, pm4, pm10 (optional, null if Format 6)
   - Maintain backward compatibility with existing fields
   - Use `@Nullable` annotations for new fields

4. **Unit Tests with Official Test Vectors**
   - **Test vectors from https://docs.ruuvi.com/communication/bluetooth-advertisements/data-format-6**
   - Valid data: `0x06170C5668C79E007000C90501D9XXCD004C884F`
   - Maximum values: `0x067FFF9C40FFFE27109C40FAFAFEXXFF074C8F4F`
   - Minimum values: `0x0680010000000000000000000000XX00004C884F`
   - Invalid values: `0x068000FFFFFFFFFFFFFFFFFFFFFFFFXXFFFFFFFFFF`
   - JavaDoc: "Test vector from https://docs.ruuvi.com/... - Valid data case"

5. **Build JAR for Embedding**
   - Update Maven coordinates (e.g., `com.github.scrin:ruuvitag-common:1.1.0-openhab`)
   - Build JAR: `mvn clean package`
   - Install to local Maven repo: `mvn install`
   - Will be embedded at compile-time by bindings

### 5.2 Phase 2: Extend Bluetooth RuuviTag Binding (RuuviTag + Ruuvi Air)

**EXTEND EXISTING BINDING** - `org.openhab.binding.bluetooth.ruuvitag` ✅
**SINGLE BINDING FOR BOTH RUUVITAG AND RUUVI AIR** ✅

**Directory Structure:** (existing binding)
```
bundles/org.openhab.binding.bluetooth.ruuvitag/
├── src/main/java/org/openhab/binding/bluetooth/ruuvitag/internal/
│   ├── RuuviTagBindingConstants.java (UPDATE - add new channels)
│   ├── RuuviTagHandler.java (UPDATE - handle Format 6/E1)
│   ├── RuuviTagHandlerFactory.java (no changes needed)
│   └── RuuviTagDiscoveryParticipant.java (no changes needed)
├── src/main/resources/OH-INF/
│   ├── thing/ruuvitag.xml (UPDATE - add air quality channels)
│   └── i18n/bluetooth.properties (UPDATE - add translations)
└── pom.xml (UPDATE - embed ruuvitag-common-java using BND)
```

**Implementation Steps:**

1. **Update Constants (RuuviTagBindingConstants.java)**
   - Add 5 new channel IDs for Format 6 air quality: `pm25`, `co2`, `vocIndex`, `noxIndex`, `luminosity`
   - Add 3 future channels for Format E1: `pm1`, `pm4`, `pm10` (mark as experimental)
   - Keep all 11 existing RuuviTag channel IDs
   - Total: 19 channels (11 RuuviTag + 5 Ruuvi Air + 3 E1 future)

2. **Update Handler (RuuviTagHandler.java)**
   - Modify `onScanRecordReceived()` to detect format byte (0x03, 0x05, 0x06, 0xE1)
   - Use updated `AnyDataFormatParser` (supports Format 3/5/6/E1)
   - Add channel updates for new air quality measurements
   - **Check if new fields are not null before updating** (Format 3/5 won't have air quality data)
   - Maintain existing behavior for Format 3/5 (RuuviTag)
   - **Single thing type supports both RuuviTag and Ruuvi Air**

3. **Update Discovery (RuuviTagDiscoveryParticipant.java)**
   - No changes needed (same manufacturer ID 0x0499)
   - Discovery detects all Ruuvi devices (Tag and Air)
   - Thing label: "RuuviTag" or "Ruuvi Air" based on detected format

4. **Update Thing Definition (ruuvitag.xml)**
   - Add 5 new air quality channels to existing `ruuvitag_beacon` thing
   - Mark air quality channels as "advanced" (only populated for Ruuvi Air)
   - All channels remain read-only
   - Set appropriate state patterns and units
   - Add note: "Air quality channels only available for Ruuvi Air devices"

5. **Update Documentation (README.md)**
   - **Document support for both RuuviTag and Ruuvi Air**
   - Add Ruuvi Air section with supported sensors
   - Document that single binding/thing type works for both devices
   - Note: Air quality channels only populate for Format 6/E1 devices
   - Configuration examples remain same (auto-discovery)

6. **Update POM (pom.xml) - Embed Dependency**
   ```xml
   <dependency>
     <groupId>com.github.scrin</groupId>
     <artifactId>ruuvitag-common</artifactId>
     <version>1.1.0-openhab</version>
     <scope>compile</scope>
   </dependency>

   <plugin>
     <groupId>biz.aQute.bnd</groupId>
     <artifactId>bnd-maven-plugin</artifactId>
     <configuration>
       <bnd><![CDATA[
         -includeresource: @ruuvitag-common-*.jar!/!META-INF/*
       ]]></bnd>
     </configuration>
   </plugin>
   ```

### 5.3 Phase 3: Extend MQTT RuuviGateway Binding

**EXTEND EXISTING BINDING** - `org.openhab.binding.mqtt.ruuvigateway` ✅

**Directory Structure:** (existing binding)
```
bundles/org.openhab.binding.mqtt.ruuvigateway/
├── src/main/java/org/openhab/binding/mqtt/ruuvigateway/internal/
│   ├── RuuviGatewayBindingConstants.java (UPDATE - add new channels)
│   ├── RuuviTagHandlerFactory.java (no changes needed)
│   ├── handler/
│   │   └── RuuviTagHandler.java (UPDATE - handle Format 6/E1)
│   ├── parser/
│   │   └── GatewayPayloadParser.java (minimal/no changes)
│   └── discovery/
│       └── RuuviGatewayDiscoveryService.java (no changes needed)
├── src/main/resources/OH-INF/
│   ├── thing/thing-types.xml (UPDATE - add air quality channels)
│   └── i18n/mqtt.properties (UPDATE - add translations)
└── pom.xml (UPDATE - use new ruuvitag-common-java version)
```

**Implementation Steps:**

1. **Update Constants (RuuviGatewayBindingConstants.java)**
   - Add same 8 new channel IDs as BLE binding
   - Add 3 conditional channels for Format E1 (pm1, pm4, pm10)
   - Keep all 15 existing channels (11 sensor + 4 gateway metadata)

2. **Update Handler (RuuviTagHandler.java)**
   - Modify `processMessage()` to use updated parser library
   - Updated `AnyDataFormatParser` automatically handles Format 6/E1
   - Add channel updates for new air quality measurements
   - Check if new fields are present before updating channels
   - Existing Format 3/5 handling remains unchanged

3. **Gateway Payload Parser (GatewayPayloadParser.java)**
   - No changes needed (already extracts manufacturer data correctly)
   - Parser library handles format detection automatically

4. **Discovery Service (RuuviGatewayDiscoveryService.java)**
   - No changes needed (works with all formats)

5. **Update Thing Definition (thing-types.xml)**
   - Add 8 new air quality channels to existing `mqtt:ruuvitag_beacon` thing
   - Mark new channels as "advanced" (optional)
   - Total channels: 23 (15 existing + 8 new)
   - Configuration remains same

6. **Update Documentation (README.md)**
   - Add Ruuvi Air support section
   - Document new air quality channels
   - Note Gateway firmware requirements (if any)

7. **Update POM (pom.xml)**
   - Update ruuvitag-common-java dependency to new forked version

### 5.4 Phase 4: Integration Tests - WITH DOCUMENTED TEST VECTORS

**Update Existing Test Suite:**
```
itests/org.openhab.binding.mqtt.ruuvigateway.tests/
├── src/main/java/org/openhab/binding/mqtt/ruuvigateway/
│   ├── RuuviGatewayTest.java (UPDATE - add Format 6 tests)
│   ├── GatewayPayloadParserTests.java (UPDATE - add Format 6 cases)
│   ├── MqttOSGiTest.java (no changes)
│   └── ThingStatusInfoChangedSubscriber.java (no changes)
└── pom.xml (UPDATE - embed updated parser)
```

**Test Cases - Format 6 (Using Official Test Vectors):**

1. **Valid Data Test**
   - **Source**: https://docs.ruuvi.com/communication/bluetooth-advertisements/data-format-6 (Valid case)
   - **BLE Data**: `0x06170C5668C79E007000C90501D9XXCD004C884F`
   - **MQTT Payload** (generated from BLE):
   ```json
   {
     "gw_mac": "AA:BB:CC:DD:EE:FF",
     "rssi": -75,
     "gwts": "1704110000",
     "ts": "1704110001",
     "data": "0201061BFF990406170C5668C79E007000C90501D900CD004C884F",
     "coords": ""
   }
   ```
   - **Expected Values**:
     - Temperature: 29.500°C
     - Humidity: 55.300%
     - Pressure: 101102 Pa
     - PM2.5: 11.2 µg/m³
     - CO2: 201 ppm
     - VOC: 10
     - NOX: 2
     - Luminosity: 13026.67 lux

2. **Maximum Values Test**
   - **Source**: https://docs.ruuvi.com/communication/bluetooth-advertisements/data-format-6 (Maximum case)
   - **BLE Data**: `0x067FFF9C40FFFE27109C40FAFAFEXXFF074C8F4F`
   - Expected: temp=163.835°C, humidity=100%, pressure=115534 Pa, PM2.5=1000 µg/m³, etc.

3. **Minimum Values Test**
   - **Source**: https://docs.ruuvi.com/communication/bluetooth-advertisements/data-format-6 (Minimum case)
   - **BLE Data**: `0x0680010000000000000000000000XX00004C884F`
   - Expected: temp=-163.835°C, humidity=0%, pressure=50000 Pa, PM2.5=0 µg/m³, etc.

4. **Invalid Values Test**
   - **Source**: https://docs.ruuvi.com/communication/bluetooth-advertisements/data-format-6 (Invalid case)
   - **BLE Data**: `0x068000FFFFFFFFFFFFFFFFFFFFFFFFXXFFFFFFFFFF`
   - Expected: All values should be null/UNDEF (not available)

**MQTT Payload Generation:**
- **Reference**: https://docs.ruuvi.com/ruuvi-gateway-firmware/data-formats
- Gateway wraps BLE advertisement in JSON:
  - Prepend standard BLE advertisement header: `0201061BFF9904` + FORMAT_DATA
  - `0201 06` = Flags
  - `1B FF` = Manufacturer specific data (27 bytes length, 0xFF type)
  - `99 04` = Manufacturer ID (0x0499 little-endian)
  - Then append Format 6 payload (20 bytes)

**Documentation in Test Files:**
- Every test case MUST include JavaDoc with exact URL reference
- Example:
  ```java
  /**
   * Test vector from official Ruuvi documentation.
   * Source: https://docs.ruuvi.com/communication/bluetooth-advertisements/data-format-6
   * Case: Valid data
   * Raw BLE: 0x06170C5668C79E007000C90501D9XXCD004C884F
   */
  @Test
  public void testFormat6ValidData() { ... }
  ```

### 5.5 Phase 5: Unit Support in openHAB Core

**openHAB Core Repository Location:**
- Repository: `/home/salski/src/openhab-main/git/openhab-core`
- Units file: `bundles/org.openhab.core/src/main/java/org/openhab/core/library/unit/Units.java`

**Required Units Status: ✅ ALL AVAILABLE**

All required units already exist in openHAB core:

```java
// Line 104-105: Density for PM measurements
public static final Unit<Density> MICROGRAM_PER_CUBICMETRE = addUnit(
    new TransformedUnit<>(KILOGRAM_PER_CUBICMETRE,
    MultiplyConverter.ofRational(BigInteger.ONE, BigInteger.valueOf(1000000000))));

// Line 155: Illuminance for luminosity
public static final Unit<Illuminance> LUX = addUnit(tech.units.indriya.unit.Units.LUX);

// Line 110-111: Dimensionless for CO2 (ppm)
public static final Unit<Dimensionless> PARTS_PER_MILLION = addUnit(
    new TransformedUnit<>(ONE, MultiplyConverter.ofRational(BigInteger.ONE, BigInteger.valueOf(1000000))));

// Line 106: Dimensionless for VOC/NOx indices
public static final Unit<Dimensionless> ONE = addUnit(AbstractUnit.ONE);
```

**Channel Unit Mappings:**

| Channel | Item Type | Unit | Core Constant |
|---------|-----------|------|---------------|
| `pm25` | Number:Density | µg/m³ | `Units.MICROGRAM_PER_CUBICMETRE` |
| `pm1`, `pm4`, `pm10` | Number:Density | µg/m³ | `Units.MICROGRAM_PER_CUBICMETRE` |
| `co2` | Number:Dimensionless | ppm | `Units.PARTS_PER_MILLION` |
| `vocIndex` | Number:Dimensionless | unitless | `Units.ONE` |
| `noxIndex` | Number:Dimensionless | unitless | `Units.ONE` |
| `luminosity` | Number:Illuminance | lux | `Units.LUX` |

**Result: No core changes needed!** ✅

## 6. Technical Considerations

### 6.1 Backward Compatibility

**Critical Requirements:**
- Existing RuuviTag things MUST continue to work unchanged
- **Single thing type** (`ruuvitag_beacon`) supports both RuuviTag and Ruuvi Air
- New air quality channels only populate for Ruuvi Air devices (Format 6/E1)
- RuuviTag devices (Format 3/5) work exactly as before
- No breaking changes to existing users

### 6.2 Data Format Detection

**Strategy:**
- First byte of manufacturer-specific data identifies format
- Format 0x06 → Ruuvi Air (Format 6)
- Format 0xE1 → Ruuvi Air (Extended v1, preferred for BT5.0+)
- Format 0x05 → RuuviTag (Format 5, use existing parser)
- Format 0x03 → RuuviTag (Format 3, use existing parser)

**Implementation:**
```java
byte formatByte = manufacturerData[0];
switch (formatByte) {
    case 0x03:
    case 0x05:
        // Use existing AnyDataFormatParser
        break;
    case 0x06:
        // Use Format6Parser
        break;
    case (byte) 0xE1:
        // Use FormatE1Parser
        break;
    default:
        logger.debug("Unknown format: {}", formatByte);
}
```

### 6.3 Unit Handling

**New Unit Types Required:**
- **Density**: µg/m³ for particulate matter
  - Use `Units.MICROGRAM_PER_CUBICMETRE` or define if not available
  - openHAB core should support this via `javax.measure`
- **Illuminance**: lux for luminosity
  - Use `Units.LUX` from openHAB core
- **Dimensionless**: for VOC/NOx indices, CO2 ppm
  - Use `Units.ONE` or `Units.PARTS_PER_MILLION`

**Verify in openHAB Core:**
- Check `org.openhab.core.library.unit` for available units
- May need to import additional JSR-363 units

### 6.4 Performance Considerations

**BLE Binding:**
- Beacon mode (passive listening) - same as RuuviTag
- Minimal CPU overhead (only parsing when advertisement received)
- 1-second broadcast interval from Ruuvi Air (more frequent than RuuviTag's ~10 seconds)
- Selective channel updates (only linked channels)

**MQTT Binding:**
- MQTT message rate depends on gateway configuration
- Gateway may batch or rate-limit messages
- Lower CPU than BLE (no radio management)
- Network bandwidth: ~500 bytes per message

### 6.5 "Not Available" Value Handling

**Protocol Specification:**
- Unsigned values: Largest presentable number = not available
- Signed values: Smallest presentable number = not available
- Example: 16-bit unsigned PM2.5 → 0xFFFF (65535) = not available

**Implementation:**
- Check for sentinel values before updating channels
- Set channel to UNDEF if not available
- Log debug message for not-available values

### 6.6 Format 6 vs E1 Support

**Format Comparison:**

| Feature | Format 6 | Format E1 |
|---------|----------|-----------|
| BLE Version | BT4 compatible | BT5+ extended advertisements |
| Payload Size | 20 bytes | 40 bytes |
| PM Sensors | PM2.5 only | PM1.0, PM2.5, PM4.0, PM10.0 |
| Luminosity | 8-bit logarithmic (0-65535 lux) | 24-bit linear (0-144284 lux, 0.01 res) |
| Sequence Counter | 8-bit (0-255) | 24-bit (0-16777214) |
| MAC Address | 24 LSB bits only | Full 48-bit address |
| Reserved Bytes | 1 byte | 8 bytes (for future use) |

**Implementation Strategy:**
- **Start with Format 6 implementation** (BT4 compatible, primary focus)
- Format E1 uses **Bluetooth 5 Extended Advertisements** - support in openHAB/Java BLE stack is unclear
- Format 6 provides all essential air quality measurements
- Same handler can support both formats using format byte detection
- If device sends both, prefer E1 data (per specification)

**Recommendation:**
- **Phase 1**: Implement Format 6 (simpler, 20 bytes, BT4 compatible)
- **Future Enhancement**: Add Format E1 when BT5 extended advertisement support is confirmed in openHAB
- Use format byte (0x06 vs 0xE1) to route to appropriate parser
- Document Format E1 capability but mark as future work

**Extended Advertisement Support:**
- BT5 extended advertisements may require special handling in Bluetooth binding
- Need to verify support in bluez, bluegiga, and other adapters
- May require changes to core Bluetooth binding framework

## 7. Dependencies

### 7.1 Repository Locations

**openHAB Repositories:**
- **openhab-addons**: `/home/salski/src/openhab-main/git/openhab-addons` (current working directory)
- **openhab-core**: `/home/salski/src/openhab-main/git/openhab-core` (for unit definitions or core changes if needed)
- **ruuvitag-common-java**: `/home/salski/src/openhab-main/git/ruuvitag-common-java` (forked parser library)

**Key Files:**
- Units: `/home/salski/src/openhab-main/git/openhab-core/bundles/org.openhab.core/src/main/java/org/openhab/core/library/unit/Units.java`
- BLE Binding: `/home/salski/src/openhab-main/git/openhab-addons/bundles/org.openhab.binding.bluetooth.ruuvitag/`
- MQTT Binding: `/home/salski/src/openhab-main/git/openhab-addons/bundles/org.openhab.binding.mqtt.ruuvigateway/`

### 7.2 External Libraries

**Parser Library (Forked & Extended):**
- Original: `fi.tkgwf.ruuvi:ruuvitag-common:1.0.2` (from https://github.com/Scrin/ruuvitag-common-java)
- Forked: `/home/salski/src/openhab-main/git/ruuvitag-common-java`
- New version: `com.github.scrin:ruuvitag-common:1.1.0-openhab` (or similar)
- **Will be embedded at compile-time via BND** (not a separate bundle)
- Decision made in maintainer discussion: simpler than managing external dependency or creating separate bundle

### 7.3 openHAB Framework Dependencies

**Required Bindings:**
- `org.openhab.binding.bluetooth` (for BLE binding)
- `org.openhab.binding.mqtt` (for MQTT binding)
- `org.openhab.binding.mqtt.generic` (for MQTT binding)

**Required Core:**
- `org.openhab.core.thing` - Thing/handler framework
- `org.openhab.core.library.types` - QuantityType, DecimalType, DateTimeType
- `org.openhab.core.library.unit` - Units and quantities (all required units available!)
- `javax.measure` - Unit interfaces (JSR-363)

**Unit Imports in Binding Code:**
```java
import org.openhab.core.library.unit.Units;

// In handler code:
updateState(channelUID, new QuantityType<>(value, Units.MICROGRAM_PER_CUBICMETRE));
updateState(channelUID, new QuantityType<>(value, Units.LUX));
updateState(channelUID, new QuantityType<>(value, Units.PARTS_PER_MILLION));
updateState(channelUID, new QuantityType<>(value, Units.ONE));
```

### 7.4 Build Dependencies

**Maven Configuration:**

**BLE Binding POM:**
```xml
<dependencies>
  <dependency>
    <groupId>org.openhab.addons.bundles</groupId>
    <artifactId>org.openhab.binding.bluetooth</artifactId>
    <scope>provided</scope>
  </dependency>

  <!-- Embedded parser dependency -->
  <dependency>
    <groupId>com.github.scrin</groupId>
    <artifactId>ruuvitag-common</artifactId>
    <version>1.1.0-openhab</version>
    <scope>compile</scope>
  </dependency>
</dependencies>

<build>
  <plugins>
    <plugin>
      <groupId>biz.aQute.bnd</groupId>
      <artifactId>bnd-maven-plugin</artifactId>
      <configuration>
        <bnd><![CDATA[
          -includeresource: @ruuvitag-common-*.jar!/!META-INF/*
        ]]></bnd>
      </configuration>
    </plugin>
  </plugins>
</build>
```

**MQTT Binding POM:**
```xml
<dependencies>
  <dependency>
    <groupId>org.openhab.addons.bundles</groupId>
    <artifactId>org.openhab.binding.mqtt.generic</artifactId>
    <scope>provided</scope>
  </dependency>

  <!-- Embedded parser dependency (same as BLE) -->
  <dependency>
    <groupId>com.github.scrin</groupId>
    <artifactId>ruuvitag-common</artifactId>
    <version>1.1.0-openhab</version>
    <scope>compile</scope>
  </dependency>
</dependencies>

<build>
  <plugins>
    <!-- Same BND configuration as BLE binding -->
  </plugins>
</build>
```

## 8. Testing Strategy

### 8.1 Unit Tests

**Parser Tests:**
- Test Format 6 parsing with valid data
- Test boundary values (min/max for each field)
- Test "not available" sentinel values
- Test malformed data handling
- Test wrong manufacturer ID rejection

**Handler Tests:**
- Mock BLE scan notifications
- Mock MQTT message arrival
- Test channel state updates
- Test heartbeat timeout logic
- Test discovery results

### 8.2 Integration Tests

**MQTT Integration (Priority):**
- Use embedded Moquette broker (same as ruuvigateway.tests)
- Publish real Ruuvi Air JSON payloads
- Verify thing creation and status
- Verify all 24 channels update correctly
- Test timeout scenarios

**BLE Integration (Optional):**
- Requires Bluetooth test infrastructure
- May be difficult in CI/CD environment
- Consider manual testing with real hardware

### 8.3 Manual Testing

**Hardware Requirements:**
- Ruuvi Air device (1-2 units)
- Bluetooth adapter (for BLE binding testing)
- Ruuvi Gateway (for MQTT binding testing)
- MQTT broker (Mosquitto or embedded)

**Test Scenarios:**
1. BLE discovery and connection
2. All channels populating with realistic values
3. Timeout/offline detection
4. Re-connection after offline
5. MQTT topic subscription
6. Gateway payload processing
7. Multiple devices simultaneously

### 8.4 Test Data

**Need Sample Payloads:**
- Real Ruuvi Air Format 6 BLE advertisements
- Real Ruuvi Gateway JSON messages with Ruuvi Air data
- Contact Ruuvi or community for example data
- May need to purchase Ruuvi Air device for testing

**Example Test Payload (Template):**
```json
{
  "gw_mac": "DE:AD:BE:EF:00:00",
  "rssi": -75,
  "aoa": [],
  "gwts": "1704110000",
  "ts": "1704110001",
  "data": "0201061BFF99040612<FORMAT_6_DATA_HEX>",
  "coords": ""
}
```

Where `<FORMAT_6_DATA_HEX>` contains encoded Format 6 measurements.

## 9. Documentation Requirements

### 9.1 Binding README Files

**Each binding needs:**
- Product description and capabilities
- Supported thing types
- Configuration parameters
- Channel descriptions with units and ranges
- Example thing/item configurations
- Troubleshooting section
- Links to Ruuvi Air product page

### 9.2 Code Documentation

**Javadoc Requirements:**
- All public classes and methods
- Parser algorithms and data format references
- Channel ID constants
- Unit conversions

**Inline Comments:**
- Data format parsing logic
- Bit manipulation and byte order
- Sentinel value handling
- Protocol references (link to Ruuvi docs)

### 9.3 User Documentation

**openHAB Documentation Site:**
- Add Ruuvi Air binding pages
- Tutorial for setup
- Comparison with RuuviTag
- MQTT vs BLE trade-offs

## 10. Delivery Milestones

### Milestone 1: Research & Design (COMPLETE)
- ✅ Analyze existing RuuviTag bindings
- ✅ Research Ruuvi Air data formats
- ✅ Review Home Assistant implementation
- ✅ Document protocol specifications
- ✅ Create implementation plan

### Milestone 2: Fork and Extend ruuvitag-common-java
**Estimated Effort:** 3-5 days
- Fork ruuvitag-common-java repository
- Implement Format 6 parser with official test vectors
- Extend `RuuviMeasurement` model with air quality fields
- Unit tests using exact test vectors from docs.ruuvi.com
- JavaDoc with URL references to official documentation
- Build JAR for embedding

**Deliverables:**
- Working parser library with Format 6 support
- All 4 official test cases passing
- JAR ready for compile-time embedding

### Milestone 3: Extend Bluetooth RuuviTag Binding
**Estimated Effort:** 3-5 days
- Update `org.openhab.binding.bluetooth.ruuvitag` binding
- Add 5 new air quality channel definitions
- Update handler to support Format 6
- Embed parser using BND in pom.xml
- Update thing XML with new channels
- Update README with Ruuvi Air support
- **Single binding supports both RuuviTag and Ruuvi Air**

**Deliverables:**
- Extended BLE binding with air quality support
- Embedded parser dependency
- Auto-discovery works for both device types

### Milestone 4: Extend MQTT RuuviGateway Binding
**Estimated Effort:** 3-5 days
- Update `org.openhab.binding.mqtt.ruuvigateway` binding
- Add same 5 air quality channel definitions as BLE binding
- Update handler to support Format 6 via embedded parser
- Embed parser using BND in pom.xml
- Update thing XML with new channels
- Update README with Ruuvi Air support
- **Single binding supports both RuuviTag and Ruuvi Air**

**Deliverables:**
- Extended MQTT binding with air quality support
- Embedded parser dependency
- Gateway automatically handles both device types

### Milestone 5: Integration Tests with Official Test Vectors
**Estimated Effort:** 3-4 days
- Update `org.openhab.binding.mqtt.ruuvigateway.tests` bundle
- Add Format 6 MQTT integration tests using official test vectors
- Generate MQTT payloads from BLE test vectors per Ruuvi Gateway format spec
- Document exact source URLs in test JavaDoc
- All 4 test cases (valid, max, min, invalid) with expected values
- CI/CD integration

**Deliverables:**
- Comprehensive test suite with official test vectors
- MQTT test payloads generated from BLE vectors
- Complete JavaDoc references to docs.ruuvi.com

### Milestone 6: Documentation & Release
**Estimated Effort:** 2-3 days
- Complete binding README files
- Add user documentation
- Code review and cleanup
- Prepare PR for openHAB repository
- Community announcement

**Deliverables:**
- Complete documentation
- Pull request submitted

**Total Estimated Effort:** 18-26 days (3-5 weeks)

## 11. Risks and Mitigations

### Risk 1: Incomplete Format 6/E1 Specification
**Impact:** High
**Probability:** Medium
**Mitigation:**
- Focus on Format 6 (better documented)
- Acquire real Ruuvi Air device for reverse engineering
- Contact Ruuvi support for clarification
- Monitor Ruuvi GitHub for spec updates

### Risk 2: Parser Library Incompatibility
**Impact:** Medium
**Probability:** Low
**Mitigation:**
- Create custom parser from scratch
- Independent of `ruuvitag-common` library
- Can always fall back to manual implementation
- Reference Python implementations for guidance

### Risk 3: Testing Without Hardware
**Impact:** Medium
**Probability:** Medium
**Mitigation:**
- Simulate BLE advertisements using test tools
- Use MQTT for majority of testing (easier to mock)
- Request sample data from Ruuvi community
- Purchase Ruuvi Air device if budget allows

### Risk 4: Breaking Existing RuuviTag Bindings
**Impact:** High
**Probability:** Low
**Mitigation:**
- Create separate bindings (no code changes to existing)
- Comprehensive regression testing
- Maintain existing test suites
- Clear documentation on differences

### Risk 5: Unit Support in openHAB Core
**Impact:** Low
**Probability:** Low
**Mitigation:**
- Verify all required units exist in core
- Add custom units if needed
- Use dimensionless for unitless indices
- Document unit handling clearly

## 12. Future Enhancements

### Phase 2 Enhancements:
1. **Format E1 Support** - Add when specification is clearer
2. **Encrypted Format 8** - Add security features if Ruuvi Air supports it
3. **Cloud Integration** - Ruuvi Cloud API support
4. **Historical Data** - Access to Ruuvi Air's 10-day storage
5. **Calibration Settings** - If Ruuvi Air exposes calibration via GATT
6. **Firmware Update** - DFU support via Bluetooth

### Long-term Improvements:
1. **Unified Binding** - Merge RuuviTag and RuuviAir into single binding with multiple thing types
2. **Parser Library Update** - Contribute Format 6/E1 support back to `ruuvitag-common`
3. **Performance Optimization** - Caching, efficient parsing
4. **Enhanced Discovery** - RSSI-based filtering, device naming

## 13. Open Questions

### 13.1 Answered Questions ✅

1. **Should we create separate binding or combine?** ✅ ANSWERED
   - **Decision**: Combine with existing `bluetooth.ruuvitag` binding
   - Source: Maintainer discussion with @cdjackson and @cpmeister
   - Ignore naming concern, prioritize user convenience

2. **How to share parsing logic?** ✅ ANSWERED
   - **Decision**: Fork ruuvitag-common-java and embed at compile-time
   - Use BND tool as documented in openHAB build system docs
   - Both bindings embed the same parser library

3. **Is Format E1 specification available?** ✅ ANSWERED
   - Yes, fully documented at https://docs.ruuvi.com/communication/bluetooth-advertisements/data-format-e1
   - 40-byte payload with all PM sizes
   - Will be future enhancement (BT5 extended advertisement support unclear)

4. **Are official test vectors available?** ✅ ANSWERED
   - Yes, all 4 test cases documented at docs.ruuvi.com
   - Valid, maximum, minimum, invalid cases provided
   - Will be used in unit tests with exact URL references

### 13.2 Remaining Questions

1. **Hardware Availability:**
   - Can we obtain Ruuvi Air device for testing?
   - What is timeline for general availability?
   - Pre-production devices available for developers?

2. **MQTT Payload:**
   - Does Ruuvi Gateway already support Ruuvi Air?
   - What Gateway firmware version required?
   - Any payload differences for Ruuvi Air vs RuuviTag?

3. **BT5 Extended Advertisements:**
   - What is openHAB's support for BT5 extended advertisements?
   - Do bluez/bluegiga adapters support extended advertisements?
   - Any changes needed in core bluetooth binding?

4. **Community:**
   - Any existing openHAB community members with Ruuvi Air?
   - Interest level in this integration?
   - Beta testers available?

## 14. References

### Official Documentation:
- Ruuvi Air Product: https://ruuvi.com/air/
- **Current Documentation**: https://docs.ruuvi.com
- **Documentation Source**: https://github.com/ruuvi/docs
- **ARCHIVED Protocol Repo**: https://github.com/ruuvi/ruuvi-sensor-protocols (reference only, archived March 2022)
- Data Format 6: https://docs.ruuvi.com/communication/bluetooth-advertisements/data-format-6
- Data Format E1: https://docs.ruuvi.com/communication/bluetooth-advertisements/data-format-e1
- Data Format 5 (RuuviTag): https://docs.ruuvi.com/communication/bluetooth-advertisements/data-format-5-rawv2
- Bluetooth Advertisements: https://docs.ruuvi.com/communication/bluetooth-advertisements
- Ruuvi Gateway Formats: https://docs.ruuvi.com/ruuvi-gateway-firmware/data-formats

### Existing Implementations:
- Home Assistant RuuviTag BLE: https://github.com/home-assistant/core/tree/dev/homeassistant/components/ruuvitag_ble
- Home Assistant Ruuvi Gateway: https://github.com/home-assistant/core/tree/dev/homeassistant/components/ruuvi_gateway
- RuuviBridge: https://github.com/Scrin/RuuviBridge

### openHAB Code:
- Bluetooth RuuviTag: `/bundles/org.openhab.binding.bluetooth.ruuvitag/`
- MQTT RuuviGateway: `/bundles/org.openhab.binding.mqtt.ruuvigateway/`
- MQTT RuuviGateway Tests: `/itests/org.openhab.binding.mqtt.ruuvigateway.tests/`

### Technical Resources:
- Ruuvi Tag Common Parser: https://github.com/Scrin/RuuviTag-RSSI-Logger (and related forks)
- Bluetooth Advertisement Formats: https://mybeacons.info/packetFormats.html
- openHAB Binding Development: https://www.openhab.org/docs/developer/bindings/

---

**Document Version:** 1.0
**Created:** 2025-11-04
**Author:** Implementation Planning Team
**Status:** Ready for Review and Implementation
