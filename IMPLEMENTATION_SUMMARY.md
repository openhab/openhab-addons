# Ruuvi Air Implementation - Summary

## Overview
Implementation plan for adding Ruuvi Air support to existing openHAB RuuviTag bindings via BLE and MQTT Gateway.

## Maintainer Discussion Summary

**Discussion with**: @cdjackson and @cpmeister (Bluetooth maintainers)

**Key Questions & Decisions:**

1. **Q: Separate or combined binding?**
   - **A: ✅ COMBINE** - Extend existing `bluetooth.ruuvitag` to support both devices
   - Rationale: User convenience, most have multiple Ruuvi devices
   - Keep existing binding name despite specificity

2. **Q: How to share parsing logic between BLE and MQTT bindings?**
   - **A: ✅ FORK + EMBED** - Fork `ruuvitag-common-java`, extend with Format 6, embed at compile-time
   - Reference: https://www.openhab.org/docs/developer/buildsystem.html#embedding-dependency
   - Simpler than separate bundle or copy-paste approach

## Key Decisions ✅

### 1. **Single Bindings for Both Devices**
- **Bluetooth**: Extend `org.openhab.binding.bluetooth.ruuvitag` to support both RuuviTag AND Ruuvi Air
- **MQTT**: Extend `org.openhab.binding.mqtt.ruuvigateway` to support both devices
- **Rationale**: Same manufacturer ID (0x0499), different data formats

### 2. **Compile-Time Dependency Embedding**
- Fork `ruuvitag-common-java` and add Format 6 parser
- Use BND tool to **embed dependency at compile-time**
- Both bindings embed the same parser library
- **Reference**: https://www.openhab.org/docs/developer/buildsystem.html#embedding-dependency

### 3. **Format 6 First, Format E1 Later**
- **Priority**: Format 6 (BT4 compatible, 20 bytes)
- **Future**: Format E1 (BT5 extended advertisements, 40 bytes)
- **Reason**: BT5 extended advertisement support unclear in openHAB

### 4. **Official Test Vectors in Documentation**
- All test cases MUST reference exact URLs from docs.ruuvi.com
- Generate MQTT test payloads from BLE test vectors
- JavaDoc with precise source references

## Repository Locations

- **openhab-addons**: `/home/salski/src/openhab-main/git/openhab-addons`
- **openhab-core**: `/home/salski/src/openhab-main/git/openhab-core`
- **ruuvitag-common-java**: `/home/salski/src/openhab-main/git/ruuvitag-common-java`

## Units Available in openHAB Core ✅

**Location**: `/home/salski/src/openhab-main/git/openhab-core/bundles/org.openhab.core/src/main/java/org/openhab/core/library/unit/Units.java`

All required units already exist:

```java
// org.openhab.core.library.unit.Units
Units.MICROGRAM_PER_CUBICMETRE  // Line 104-105: For PM measurements (µg/m³)
Units.LUX                         // Line 155: For luminosity (lux)
Units.PARTS_PER_MILLION          // Line 110-111: For CO2 (ppm)
Units.ONE                         // Line 106: For VOC/NOx indices (dimensionless)
```

**No core changes needed!** ✅

## ruuvitag-common-java Structure

```
src/main/java/fi/tkgwf/ruuvi/common/
├── bean/
│   └── RuuviMeasurement.java          // POJO with all measurements
├── parser/
│   ├── DataFormatParser.java          // Interface: parse(byte[] data)
│   └── impl/
│       ├── AnyDataFormatParser.java   // Tries all registered parsers
│       ├── DataFormat2Parser.java     // Format 2 (URL)
│       ├── DataFormat3Parser.java     // Format 3 (RAWv1)
│       ├── DataFormat4Parser.java     // Format 4 (Eddystone)
│       ├── DataFormat5Parser.java     // Format 5 (RAWv2) ← TEMPLATE
│       └── [DataFormat6Parser.java]   // ← TO ADD
└── utils/
    └── ByteUtils.java                  // Helpers for parsing
```

### Parser Pattern (from DataFormat5Parser.java)

```java
public class DataFormat5Parser implements DataFormatParser {
    @Override
    public RuuviMeasurement parse(byte[] data) {
        // 1. Validate manufacturer ID (0x9904)
        if (data[0] != 0x99 || data[1] != 0x04) return null;

        // 2. Check format byte and length
        if (data[2] != 5 || data.length < 24) return null;

        // 3. Create RuuviMeasurement
        RuuviMeasurement m = new RuuviMeasurement();
        m.setDataFormat(5);

        // 4. Parse fields with "not available" checks
        if (!ByteUtils.isMinSignedShort(data[3], data[4])) {
            m.setTemperature((data[3] << 8 | data[4] & 0xFF) / 200.0);
        }
        // ... more fields ...

        return m;
    }
}
```

## Implementation Tasks

### Phase 1: ruuvitag-common-java Fork

1. **Extend RuuviMeasurement.java**
   ```java
   // Add new fields
   private Double pm25;           // µg/m³
   private Integer co2;           // ppm
   private Integer vocIndex;      // unitless 0-500
   private Integer noxIndex;      // unitless 0-500
   private Double luminosity;     // lux

   // For Format E1 (future)
   private Double pm1;            // µg/m³
   private Double pm4;            // µg/m³
   private Double pm10;           // µg/m³
   ```

2. **Create DataFormat6Parser.java**
   - Copy pattern from DataFormat5Parser
   - Parse 20-byte Format 6 payload
   - Handle all fields per official spec
   - Use test vectors from docs.ruuvi.com

3. **Register in AnyDataFormatParser**
   ```java
   parsers.add(new DataFormat6Parser());
   ```

4. **Unit Tests**
   - Valid data: `0x06170C5668C79E007000C90501D9XXCD004C884F`
   - Maximum values: `0x067FFF9C40FFFE27109C40FAFAFEXXFF074C8F4F`
   - Minimum values: `0x0680010000000000000000000000XX00004C884F`
   - Invalid values: `0x068000FFFFFFFFFFFFFFFFFFFFFFFFXXFFFFFFFFFF`
   - Each with JavaDoc referencing docs.ruuvi.com

5. **Build for Embedding**
   ```bash
   mvn clean package
   mvn install
   ```

### Phase 2: Bluetooth Binding

Update `org.openhab.binding.bluetooth.ruuvitag`:

1. **RuuviTagBindingConstants.java** - Add 5 channel IDs
2. **RuuviTagHandler.java** - Handle new measurements, check null
3. **ruuvitag.xml** - Add 5 channels (marked advanced)
4. **pom.xml** - Embed parser with BND:
   ```xml
   <dependency>
     <groupId>com.github.scrin</groupId>
     <artifactId>ruuvitag-common</artifactId>
     <version>1.1.0-openhab</version>
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

### Phase 3: MQTT Binding

Update `org.openhab.binding.mqtt.ruuvigateway`:

1. **RuuviGatewayBindingConstants.java** - Add same 5 channel IDs
2. **RuuviTagHandler.java** - Handle new measurements
3. **thing-types.xml** - Add 5 channels
4. **pom.xml** - Same BND embedding as BLE binding

### Phase 4: Integration Tests

Update `org.openhab.binding.mqtt.ruuvigateway.tests`:

1. Add Format 6 test cases using official vectors
2. Generate MQTT payloads from BLE vectors:
   ```
   BLE: 0x06170C5668C79E007000C90501D9XXCD004C884F
   MQTT data field: "0201061BFF990406170C5668C79E007000C90501D900CD004C884F"
   ```
3. Document exact source URLs in JavaDoc

## New Channels (7 for Format 6)

| Channel ID | Type | Unit | Description |
|------------|------|------|-------------|
| `pm25` | Number:Density | µg/m³ | PM2.5 particulate matter |
| `co2` | Number:Dimensionless | ppm | CO2 concentration |
| `vocIndex` | Number:Dimensionless | unitless | VOC index (baseline 100) |
| `noxIndex` | Number:Dimensionless | unitless | NOx index (baseline 1) |
| `luminosity` | Number:Illuminance | lux | Light intensity |
| `calibrationStatus` | String | - | **NEW** Sensor calibration state (COMPLETE, IN_PROGRESS, UNRELIABLE) |
| `airQualityIndex` | Number:Dimensionless | 0-100 | **Calculated** air quality (higher = better) |

**Air Quality Index Calculation:**
- Source: https://github.com/ruuvi/com.ruuvi.station.webui/blob/master/src/decoder/untils.js
- Combines PM2.5 (0-60 µg/m³) and CO2 (420-2300 ppm)
- Formula: `AQI = clamp(100 - √((pm25×1.6667)² + ((co2-420)×0.05319)²), 0, 100)`
- Higher values = better air quality

**Future (Format E1):**
- `pm1`, `pm4`, `pm10` (when BT5 extended advertisements supported)

## Format 6 Flags Support

**Calibration Status (Bits 0-2 of Flags Byte)**

The calibration status indicates sensor reliability:

| Value | Status | Description |
|-------|--------|-------------|
| 0 | COMPLETE | Sensor fully calibrated, values reliable |
| 1-4 | IN_PROGRESS | Sensor calibrating, values improving |
| 5-7 | UNRELIABLE | Calibration not started or incomplete |

**VOC/NOx High Bits (Bits 6-7 of Flags Byte)**

VOC and NOx indices are 9-bit values stored across two bytes:
- VOC: byte 11 (8 bits) + bit 6 of flags byte (1 bit) = 9-bit value
- NOx: byte 12 (8 bits) + bit 7 of flags byte (1 bit) = 9-bit value
- Range: 0-511 per official spec, valid range 0-500, sentinel 511

**Implementation Status:**
- ✅ VOC/NOx 9-bit extraction: Already implemented in DataFormat6Parser
- ✅ Calibration status parsing: Extracted from flags byte (bits 0-2)
- ✅ Calibration status channel: New `calibrationStatus` channel added to RuuviMeasurement
- ✅ All sentinel values respected per official specification

## Test Vector Reference

**Source**: https://docs.ruuvi.com/communication/bluetooth-advertisements/data-format-6

### Valid Data
```
BLE: 0x06170C5668C79E007000C90501D9XXCD004C884F

Expected Values:
- Temperature: 29.500°C
- Humidity: 55.300%
- Pressure: 101102 Pa
- PM2.5: 11.2 µg/m³
- CO2: 201 ppm
- VOC: 10
- NOX: 2
- Luminosity: 13026.67 lux
```

### MQTT Gateway Format
**Reference**: https://docs.ruuvi.com/ruuvi-gateway-firmware/data-formats

```
Prepend: 02 01 06 1B FF 99 04 + FORMAT_DATA
         ^^^^^^ ^^^^^ ^^^^^
         Flags  Mfr   RuuviID
```

## Timeline

- **Phase 1** (Parser): 3-5 days
- **Phase 2** (BLE): 3-5 days
- **Phase 3** (MQTT): 3-5 days
- **Phase 4** (Tests): 3-4 days
- **Total**: 12-19 days (2.5-4 weeks)

## Next Steps

1. Fork ruuvitag-common-java (already done ✅)
2. Add Format 6 support to parser
3. Update BLE binding
4. Update MQTT binding
5. Add integration tests
6. Documentation

---

**Status**: Planning Complete ✅
**Ready for Implementation**: YES
