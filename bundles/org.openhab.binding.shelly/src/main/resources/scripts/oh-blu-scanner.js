/*
 * This script uses the BLE scan functionality to pass scan results and BLU data to openHAB.
 * It gets automatically installed / updated by the binding when BLU support is enabled in the thing configuration.
 * Decoding of event data is based on the BTHome standard.
 * 
 * @author Markus Michels - Initial contribution
 * @author Udo Hartmann - Add support for decoding multi button stats
 */

let ALLTERCO_DEVICE_NAME_PREFIX = ["SBBT", "SBDW", "SBMO", "SBHT"];
let ALLTERCO_MFD_ID_STR = "0ba9";
let BTHOME_SVC_ID_STR = "fcd2";

let ALLTERCO_MFD_ID = JSON.parse("0x" + ALLTERCO_MFD_ID_STR);
let BTHOME_SVC_ID = JSON.parse("0x" + BTHOME_SVC_ID_STR);
let SCAN_DURATION = BLE.Scanner.INFINITE_SCAN;

// Cache objects for Shelly Blu devices and last packet IDs
let SHELLY_BLU_CACHE = {};
let LAST_PID = {};

// Data types enumeration
let uint8 = 0;
let int8 = 1;
let uint16 = 2;
let int16 = 3;
let uint24 = 4;
let int24 = 5;
let uint32 = 6;
let int32 = 7;

let BTH_DIMMERSTEPS_INDEX = 0x3c;   // Dimmer (Wheel) Steps object ID
let FORCE_ARRAY_VALUES = ["Temperature", "Button", "Rotation"];

// BTHome object definitions: id => {name, type, optional scale factor}
// https://bthome.io/format/
let BTH = [];
BTH[0x00] = { n: "pid", t: uint8 };                                           // Packet ID
BTH[0x01] = { n: "Battery", t: uint8, u: "%" };                               // Battery level in percent
BTH[0x02] = { n: "Temperature", t: int16, f: 0.01 };                          // Temperature in C (scaled by 0.01)
BTH[0x03] = { n: "Humidity", t: uint16, f: 0.01 };                            // Relative humidity % (scaled by 0.01)
BTH[0x04] = { n: "Pressure", t: uint24, f: 0.01 };                            // Pressure (scaled by 0.01)
BTH[0x05] = { n: "Illuminance", t: uint24, f: 0.01 };                         // Light level (scaled by 0.01)
BTH[0x06] = { n: "Mass_kg", t: uint16, f: 0.01, u: "kg" };                    // Mass in kilograms (scaled by 0.01)
BTH[0x07] = { n: "Mass_lb", t: uint16, f: 0.01, u: "lb" };                    // Mass in pounds (scaled by 0.01)
BTH[0x08] = { n: "Dewpoint", t: int16, f: 0.01 };                             // Dewpoint temperature (scaled by 0.01)
BTH[0x09] = { n: "Count", t: uint8 };                                         // Count
BTH[0x0c] = { n: "Voltage", t: uint16, f: 0.001 };                            // Voltage in Volts (scaled by 0.001)
BTH[0x0f] = { n: "GenericBoolean", t: uint8 };                                // Generic boolean false/true status (boolean)
BTH[0x10] = { n: "Power", t: uint8 };                                         // Power off/on status (boolean)
BTH[0x11] = { n: "Opening", t: uint8 };                                       // Opening closed/open status (boolean)
BTH[0x12] = { n: "Co2", t: uint16 };                                          // CO2 concentration ppm
BTH[0x13] = { n: "TVOC", t: uint16 };                                         // TVOC Air Quality ug/m3
BTH[0x14] = { n: "Moisture16", t: uint16, f: 0.01 };                          // Moisture (scaled by 0.01)
BTH[0x15] = { n: "Battery", t: uint8 };                                       // Battery level normal/low status (boolean)
BTH[0x16] = { n: "BatteryCharging", t: uint8 };                               // Battery charging status (boolean)
BTH[0x17] = { n: "CarbonMonoxide", t: uint8 };                                // Carbon Monoxide not detected/detected status (boolean)
BTH[0x18] = { n: "Cold", t: uint8 };                                          // Cold normal/cold status (boolean)
BTH[0x19] = { n: "Connectivity", t: uint8 };                                  // Connectivity disconnected/connected status (boolean)
BTH[0x1a] = { n: "Door", t: uint8 };                                          // Door open/close status (boolean)
BTH[0x1b] = { n: "GarageDoor", t: uint8 };                                    // Garage Door closed/open status (boolean)
BTH[0x1c] = { n: "Gas", t: uint8 };                                           // Gas clear/detected status (boolean)
BTH[0x1d] = { n: "Heat", t: uint8 };                                          // Heat normal/hot status (boolean)
BTH[0x1e] = { n: "Light", t: uint8 };                                         // Light no light/light detected status (boolean)
BTH[0x1f] = { n: "Lock", t: uint8 };                                          // Lock locked/unlocked status (boolean)
BTH[0x20] = { n: "Moisture", t: uint8 };                                      // Moisture dry/wet status (boolean)
BTH[0x21] = { n: "Motion", t: uint8 };                                        // Motion clear/detected status (boolean)
BTH[0x22] = { n: "Moving", t: uint8 };                                        // Moving not moving/moving status (boolean)
BTH[0x23] = { n: "Occupancy", t: uint8 };                                     // Occupancy clear/detected status (boolean)
BTH[0x24] = { n: "Plug", t: uint8 };                                          // Plug unplugged/plugged in status (boolean)
BTH[0x25] = { n: "Presence", t: uint8 };                                      // Presence away/home status (boolean)
BTH[0x26] = { n: "Problem", t: uint8 };                                       // Problem OK/problem status (boolean)
BTH[0x27] = { n: "Running", t: uint8 };                                       // Running not running/running status (boolean)
BTH[0x28] = { n: "Safety", t: uint8 };                                        // Safety unsafe/safe status (boolean)
BTH[0x29] = { n: "Smoke", t: uint8 };                                         // Smoke clear/detected status (boolean)
BTH[0x2a] = { n: "Sound", t: uint8 };                                         // Sound clear/detected status (boolean)
BTH[0x2b] = { n: "Tamper", t: uint8 };                                        // Tamper off/on status (boolean)
BTH[0x2c] = { n: "Vibration", t: uint8 };                                     // Vibration clear/detected status (boolean)
BTH[0x2d] = { n: "Window", t: uint8 };                                        // Window closed/open status (boolean)
BTH[0x2e] = { n: "Humidity", t: uint8 };                                      // Humidity (alternative uint8 format)
BTH[0x2f] = { n: "Moisture8", t: uint8 };                                     // Moisture (alternative uint8 format)
BTH[0x3a] = { n: "Button", t: uint8 };                                        // Button press events
BTH[0x3c] = { n: "Dimmer", t: uint16 };                                       // Dimmer event (2 bytes: direction up/down + steps)
BTH[0x3d] = { n: "Count", t: uint16 };                                        // Count
BTH[0x3e] = { n: "Count", t: uint32 };                                        // Count
BTH[0x3f] = { n: "Rotation", t: int16, f: 0.1 };                              // Rotation (scaled by 0.1)
BTH[0x40] = { n: "Distance_mm", t: uint16, u: "mm" };                         // Distance in millimeters
BTH[0x41] = { n: "Distance_m", t: uint16, f: 0.1, u: "m" };                   // Distance in meters (scaled by 0.1)
BTH[0x42] = { n: "Duration", t: uint24, f: 0.001, u: "s" };                   // Duration in seconds (scaled by 0.001)
BTH[0x43] = { n: "Current", t: uint16, f: 0.001 };                            // Electrical current (scaled by 0.001)
BTH[0x44] = { n: "Speed", t: uint16, f: 0.01 };                               // Speed (scaled by 0.01)
BTH[0x45] = { n: "Temperature", t: int16, f: 0.1 };                           // Temperature alternative format (scaled by 0.1)
BTH[0x46] = { n: "UVIndex", t: uint8, f: 0.1 };                               // UV index (scaled by 0.1)
BTH[0x47] = { n: "Volume", t: uint16, f: 0.1, u: "L" };                       // Volume in L (scaled by 0.1)        
BTH[0x48] = { n: "Volume", t: uint16, u: "mL" };                              // Volume in mL         
BTH[0x49] = { n: "VolumeFlowRate", t: uint16, f: 0.001, u: "m3/hr" };         // Volume Flow Rate in m3/hr (scaled by 0.001)         
BTH[0x4a] = { n: "Voltage", t: uint16, f: 0.1 };                              // Voltage in Volts (scaled by 0.1)
BTH[0x4b] = { n: "Gas", t: uint24, f: 0.001 };                                // Gas volume (scaled by 0.001)       
BTH[0x4c] = { n: "Gas", t: uint32, f: 0.001 };                                // Gas volume (scaled by 0.001)       
BTH[0x4e] = { n: "Volume", t: uint32, f: 0.001, u: "L" };                     // Volume in L (scaled by 0.001)        
BTH[0x4f] = { n: "Water", t: uint32, f: 0.001, u: "L" };                      // Water in L (scaled by 0.001)        
BTH[0x50] = { n: "Timestamp", t: uint32 };                                    // Timestamp (epoch time)
BTH[0x51] = { n: "Acceleration", t: uint16, f: 0.001 };                       // Acceleration (scaled by 0.001)          
BTH[0x53] = { n: "text", t: uint32 };                                         // text sensor with a variable length
BTH[0x54] = { n: "raw", t: uint32 };                                          // raw sensor with a variable length
BTH[0x55] = { n: "VolumeStorage", t: uint32, f: 0.001, u: "L" };              // Volume Storage in L (scaled by 0.001)
BTH[0x56] = { n: "Conductivity", t: uint16 };                                 // Conductivity      
BTH[0x57] = { n: "Temperature", t: int8 };                                    // Temperature alternative format
BTH[0x58] = { n: "Temperature", t: int8, f: 0.35 };                           // Temperature alternative format (scaled by 0.35)
BTH[0x59] = { n: "Count", t: int8 };                                          // Count
BTH[0x5a] = { n: "Count", t: int16 };                                         // Count
BTH[0x5b] = { n: "Count", t: int32 };                                         // Count
BTH[0x5d] = { n: "Current", t: int16, f: 0.001 };                             // Electrical current (scaled by 0.001)
BTH[0x5e] = { n: "Direction", t: uint16, f: 0.01 };                           // Direction (scaled by 0.01)
BTH[0x5f] = { n: "Precipitation", t: uint16, f: 0.1 };                        // Precipitation (scaled by 0.1)
BTH[0x60] = { n: "Channel", t: uint8 };                                       // Channel
BTH[0xF0] = { n: "DeviceId", t: uint16};                                      // Device type ID
BTH[0xF1] = { n: "Firmware32", t: uint32};                                    // Firmware version in 1.2.3.4 format
BTH[0xF2] = { n: "Firmware24", t: uint24};                                    // Firmware version in 1.2.3 format

// Helper function to get byte size of data type
function getByteSize(type) {                                     
  if (type === uint8 || type === int8) return 1;
  if (type === uint16 || type === int16) return 2;
  if (type === uint24 || type === int24) return 3;
  if (type === uint32 || type === int32) return 4;
  //impossible as advertisements are much smaller;
  return 255;
}

// Helper function: buffer to hex string
// padStart function not available in Shelly JS engine
function bufToHex(buffer) {
  let hex = "";
  for (let i = 0; i < buffer.length; i++) {
    let hexValue = buffer.at(i).toString(16);
    if (hexValue.length === 1) {
      hex += "0" + hexValue + " ";
    } else {
      hex += hexValue + " ";
    }
  }
  return hex.trim();
}
let BTHomeDecoder = {
  // Convert unsigned integer to signed based on bit size
  utoi: function (num, bitsz) {
    let mask = 1 << (bitsz - 1);
    return num & mask ? num - (1 << bitsz) : num;
  },
  
  getUInt8:    function (buffer) { if (buffer.length < 1) return null;  return buffer.at(0); },
  getInt8:     function (buffer) { let val = this.getUInt8(buffer);    return val !== null ? this.utoi(val, 8) : null; },
  getInt16LE:  function (buffer) { let val = this.getUInt16LE(buffer); return val !== null ? this.utoi(val, 16) : null; },
  getInt24LE:  function (buffer) { let val = this.getUInt24LE(buffer); return val !== null ? this.utoi(val, 24) : null; },
  getInt32LE:  function (buffer) { let val = this.getUInt32LE(buffer); return val !== null ? this.utoi(val, 32) : null; },
  getUInt16LE: function (buffer) { if (buffer.length < 2) return null; return (buffer.at(1) << 8) | buffer.at(0); },
  getUInt24LE: function (buffer) { if (buffer.length < 3) return null; return (buffer.at(2) << 16) | (buffer.at(1) << 8) | buffer.at(0); },
  getUInt32LE: function (buffer) { if (buffer.length < 4) return null; return (buffer.at(3) << 24) | (buffer.at(2) << 16) | (buffer.at(1) << 8) | buffer.at(0); },
  
  // Decode value buffer according to type
  getBufValue: function (type, buffer) {
    if (buffer.length < getByteSize(type)) return null;
    let res = null;
    if (type === uint8) res = this.getUInt8(buffer);
    else if (type === int8) res = this.getInt8(buffer);
    else if (type === uint16) res = this.getUInt16LE(buffer);
    else if (type === int16) res = this.getInt16LE(buffer);
    else if (type === uint24) res = this.getUInt24LE(buffer);
    else if (type === int24) res = this.getInt24LE(buffer);
    else if (type === uint32) res = this.getUInt32LE(buffer);
    else if (type === int32) res = this.getInt32LE(buffer);
    return res;
  },
  
  // Unpack BTHome payload string to object with decoded values
  unpack: function (buffer) {
    // beacons might not provide BTH service data
    if (typeof buffer !== "string" || buffer.length === 0) return null;
    
    let result = {};
    let _dib = buffer.at(0); // Device Info Byte
    result["encryption"] = _dib & 0x1 ? true : false;
    result["BTHome_version"] = _dib >> 5;
    if (result["encryption"]) return result; // Can not handle encrypted data
    if (result["BTHome_version"] !== 2) return null; // Can not handle BT version != 2    
    
    buffer = buffer.slice(1); // Remove header byte

    let dimmer = [];

    while (buffer.length > 0) {
      let bthIdx = buffer.at(0); // Object ID
      buffer = buffer.slice(1);

      let _bth = BTH[bthIdx];
      if (typeof _bth === "undefined") {
        console.log("BTH: unknown type", bthIdx);
        break;
      }

      let valueSize = getByteSize(_bth.t);
      let rawBuffer = buffer.slice(0, valueSize);
      let _value = this.getBufValue(_bth.t, rawBuffer);

      if (_value === null) {
        break;
      }

      // Apply scaling factor if defined
      if (typeof _bth.f !== "undefined") _value *= _bth.f;

      // Special handling for Dimmer-Steps-Event (0x3c)
      if (bthIdx === BTH_DIMMERSTEPS_INDEX) {
        let valueDimSize = 2; // Fixed size for dimmer steps is 2 bytes
        if (buffer.length < valueDimSize) {
          console.log("BTH: buffer too short for Dimmer event");
          continue;
        }

        // Extract the two bytes separately
        const directionByte = buffer.at(0);
        const stepsByte = buffer.at(1);

        // Create a new object with the parsed values
        let dimmerEvent = {
          direction: directionByte,
          steps: stepsByte
        };

        //dimmer.push(dimmerEvent);
        result[_bth.n] = dimmerEvent;
        
        buffer = buffer.slice(valueDimSize);
        continue;
      }
      
      // Default handling
      if (result[_bth.n] !== undefined && Array.isArray(result[_bth.n])) {
        result[_bth.n].push(_value);
      } else if (result[_bth.n] !== undefined) {
        result[_bth.n] = [result[_bth.n], _value];
      } else {
        result[_bth.n] = _value;
      }

      buffer = buffer.slice(valueSize);
    }

    // Add events as arrays to the result
    if (dimmer.length > 0) result["Dimmer"] = dimmer;

    // Special handling for values, which need to be converted to an array
    for (let i = 0; i < FORCE_ARRAY_VALUES.length; i++) {
      let key = FORCE_ARRAY_VALUES[i];
      if (typeof result[key] !== "undefined" && !Array.isArray(result[key])) {
        result[key] = [result[key]];
      }
    }

    return result;
  }
};

// Shelly BLU BLE data parser wrapper
let ShellyBLUParser = {
  getData: function (res) {
    let service_data = res.service_data[BTHOME_SVC_ID_STR];
    if (!service_data) return null;

    let hexDump = bufToHex(service_data);
    // console.log("Received BTHome RAW packet (hex):", hexDump);
    
    let result = BTHomeDecoder.unpack(service_data);
    if (!result) return null;
    result.addr = res.addr;
    result.rssi = res.rssi;
    result.packet = hexDump;
    return result;
  }
};

// BLE scan callback function
function scanCB(ev, res) {
  if (ev !== BLE.Scanner.SCAN_RESULT) return;
  if (typeof res.service_data === 'undefined' || typeof res.service_data[BTHOME_SVC_ID_STR] === 'undefined') return;

  if (typeof SHELLY_BLU_CACHE[res.addr] === 'undefined') {
    if (typeof res.local_name !== "string") return;

    let found = false;
    for (let prefix of ALLTERCO_DEVICE_NAME_PREFIX) {
      if (res.local_name.indexOf(prefix) === 0) {
        console.log('New device found: address=', res.addr, ', name=', res.local_name);
        Shelly.emitEvent("oh-blu.scan_result", {"addr":res.addr, "name":res.local_name, "rssi":res.rssi, "tx_power":res.tx_power_level});
        SHELLY_BLU_CACHE[res.addr] = res.local_name;        
        found = true
      }
    }
    if (!found) {
        console.log('Unknown device: ', res.local_name);
    }
  }
 
  let BTHparsed = ShellyBLUParser.getData(res); // skip if parsing failed
  if (BTHparsed === null) {
    console.log("Failed to parse BTH data");
    return;
  }
 
  // skip, we are deduping results
  if (typeof LAST_PID[res.addr] === 'undefined' || BTHparsed.pid !== LAST_PID[res.addr]) {
    console.log('Parsed BTH data from device ', res.local_name, ': ', JSON.stringify(BTHparsed));
    Shelly.emitEvent("oh-blu.data", BTHparsed);
    LAST_PID[res.addr] = BTHparsed.pid;
  } else {
    console.log("Drop redundant packet with pid ", BTHparsed.pid);
  }
}

// retry several times to start the scanner if script was started before
// BLE infrastructure was up in the Shelly
function startBLEScan() {
    let bleScanSuccess = BLE.Scanner.Start({ duration_ms: SCAN_DURATION, active: true }, scanCB);
    if( bleScanSuccess === null ) {
        console.log('Unable to start OH-BLU Scanner.');
        Timer.set(3000, false, startBLEScan);
    } else {
        console.log('Success: OH-BLU Event Gateway running');
    }
 }
 
let BLEConfig = Shelly.getComponentConfig('ble');
if(BLEConfig.enable === false) {
    console.log('Error: BLE not enabled, unable to start OH-BLU Scanner');
} else {
    Timer.set(1000, false, startBLEScan);
}
