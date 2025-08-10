// Device and Service IDs for BLE scanning
let ALLTERCO_DEVICE_NAME_PREFIX = ["SBBT", "SBRC", "SBDW", "SBMO", "SBHT"];
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

let BTH_BUTTON_INDEX = 0x3a;  // Button object ID
let BTH_TEMP_INDEX = 0x45;  // Button object ID

// BTHome object definitions: id => {name, type, optional scale factor}
let BTH = [];
BTH[0x00] = { n: "pid", t: uint8 };                        // Packet ID
BTH[0x01] = { n: "Battery", t: uint8, u: "%" };            // Battery level in percent
BTH[0x02] = { n: "Temperatures", t: int16, f: 0.01 };      // Temperature in °C (scaled by 0.01)
BTH[0x03] = { n: "Humidity", t: uint16, f: 0.01 };         // Relative humidity % (scaled by 0.01)
BTH[0x05] = { n: "Illuminance", t: uint24, f: 0.01 };      // Light level (scaled by 0.01)
BTH[0x08] = { n: "Dewpoint", t: int16, f: 0.01 };          // Dewpoint temperature (scaled by 0.01)
BTH[0x12] = { n: "Co2", t: uint16 };                       // CO2 concentration ppm
BTH[0x14] = { n: "Moisture16", t: uint16, f: 0.01 };       // Moisture (scaled by 0.01)
BTH[0x16] = { n: "BatteryCharging", t: uint8 };            // Battery charging status (boolean)
BTH[0x1a] = { n: "Door", t: uint8 };                       // Door open/close status (boolean)
BTH[0x20] = { n: "Moisture", t: uint8 };                   // Moisture detected (boolean)
BTH[0x21] = { n: "Motion", t: uint8 };                     // Motion detected (boolean)
BTH[0x29] = { n: "Smoke", t: uint8 };                      // Smoke detected (boolean)
BTH[0x2b] = { n: "Tamper", t: uint8 };                     // Tamper detected (boolean)
BTH[0x2c] = { n: "Vibration", t: uint8 };                  // Vibration detected (boolean)
BTH[0x2d] = { n: "Window", t: uint8 };                     // Window open/close status (boolean)
BTH[0x2e] = { n: "Humidity", t: uint8 };                   // Humidity (alternative uint8 format)
BTH[0x2f] = { n: "Moisture8", t: uint8 };                  // Moisture (alternative uint8 format)
BTH[0x3a] = { n: "Button", t: uint8 };                     // Button press events
BTH[0x3c] = { n: "Dimmer", t: uint24 };                    // Dimmer event (3 bytes: event id + steps)
BTH[0x3f] = { n: "Rotation", t: int16, f: 0.1 };           // Rotation (scaled by 0.1)
BTH[0x40] = { n: "Distance_mm", t: uint16 };               // Distance in millimeters
BTH[0x41] = { n: "Distance_m", t: uint16 };                // Distance in meters
BTH[0x43] = { n: "Current", t: uint16, f: 0.1 };           // Electrical current (scaled by 0.1)
BTH[0x44] = { n: "Speed", t: uint16, f: 0.01 };            // Speed (scaled by 0.01)
BTH[0x45] = { n: "Temperature", t: int16, f: 0.1 };        // Temperature alternative format (scaled by 0.1)
BTH[0x46] = { n: "UVIndex", t: uint8 };                    // UV index
BTH[0x4f] = { n: "Water", t: uint8 };                      // Water detected (boolean)
BTH[0x50] = { n: "Timestamp", t: uint32 };                 // Timestamp (epoch time)
BTH[0x51] = { n: "Acceleration", t: uint16, f: 0.1 };      // Acceleration (scaled by 0.1)
BTH[0x5d] = { n: "Power", t: uint16, f: 0.1 };             // Electrical power (scaled by 0.1)
BTH[0x06] = { n: "Mass_kg", t: uint16, f: 0.01 };          // Mass in kilograms (scaled by 0.01)
BTH[0x07] = { n: "Mass_lb", t: uint16, f: 0.01 };          // Mass in pounds (scaled by 0.01)
BTH[0x0c] = { n: "Voltage", t: uint16, f: 0.001 };         // Voltage in Volts (scaled by 0.001)
BTH[0x54] = { n: "Raw", t: uint32 };
BTH[0x60] = { n: "Channel", t: uint16 };

// Helper function to get byte size of data type
function getByteSize(type) {
  if (type === uint8 || type === int8) return 1;
  if (type === uint16 || type === int16) return 2;
  if (type === uint24 || type === int24) return 3;
  if (type === uint32 || type === int32) return 4;
  return 255; // unknown type fallback
}

let BTHomeDecoder = {
  // Convert unsigned integer to signed based on bit size
  utoi: function (num, bitsz) {
    let mask = 1 << (bitsz - 1);
    return num & mask ? num - (1 << bitsz) : num;
  },

  // Read unsigned 8-bit integer from buffer
  getUInt8: function (buffer) {
    if (buffer.length < 1) return null;
    return buffer.at(0);
  },

  // Read signed 8-bit integer from buffer
  getInt8: function (buffer) {
    let val = this.getUInt8(buffer);
    return val !== null ? this.utoi(val, 8) : null;
  },

  // Read unsigned 16-bit little-endian integer from buffer
  getUInt16LE: function (buffer) {
    if (buffer.length < 2) return null;
    return (buffer.at(1) << 8) | buffer.at(0);
  },

  // Read signed 16-bit little-endian integer from buffer
  getInt16LE: function (buffer) {
    let val = this.getUInt16LE(buffer);
    return val !== null ? this.utoi(val, 16) : null;
  },

  // Read unsigned 24-bit little-endian integer from buffer
  getUInt24LE: function (buffer) {
    if (buffer.length < 3) return null;
    return (buffer.at(2) << 16) | (buffer.at(1) << 8) | buffer.at(0);
  },

  // Read signed 24-bit little-endian integer from buffer
  getInt24LE: function (buffer) {
    let val = this.getUInt24LE(buffer);
    return val !== null ? this.utoi(val, 24) : null;
  },

  // Read unsigned 32-bit little-endian integer from buffer
  getUInt32LE: function (buffer) {
    if (buffer.length < 4) return null;
    return (buffer.at(3) << 24) | (buffer.at(2) << 16) | (buffer.at(1) << 8) | buffer.at(0);
  },

  // Read signed 32-bit little-endian integer from buffer
  getInt32LE: function (buffer) {
    let val = this.getUInt32LE(buffer);
    return val !== null ? this.utoi(val, 32) : null;
  },

  
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
    if (typeof buffer !== "string" || buffer.length === 0) return null;

    let result = {};
    let _dib = buffer.at(0);  // Device Info Byte
    result["encryption"] = (_dib & 0x1) ? true : false;
    result["BTHome_version"] = _dib >> 5;
    if (result["encryption"]) return result; // Encrypted packets cannot be decoded
    if (result["BTHome_version"] !== 2) return null; // Only version 2 supported

    buffer = buffer.slice(1); // Remove header byte
    let bttns = [];

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

      if (_value === null) break;

      // Apply scaling factor if defined
      if (typeof _bth.f !== "undefined") _value *= _bth.f;

      // Special handling for Temperature singular/plural
      if (bthIdx === BTH_TEMP_INDEX) {
        if (result["Temperature"] !== undefined) {
          if (!Array.isArray(result["Temperature"])) {
             result["Temperatures"] = [result["Temperature"]];
              delete result["Temperature"];
            }
            result["Temperatures"].push(_value);
          } else if (result["Temperatures"] !== undefined) {
          result["Temperatures"].push(_value);
        } else {
          result["Temperature"] = _value;
        }
      }      // Handle button events separately (accumulate)
      else if (bthIdx === BTH_BUTTON_INDEX) {
        bttns.push(_value);
      } else {
        // If property already exists, convert to array or append
        if (result[_bth.n] !== undefined && Array.isArray(result[_bth.n])) {
          result[_bth.n].push(_value);
        } else if (result[_bth.n] !== undefined) {
          result[_bth.n] = [result[_bth.n], _value];
        } else {
          result[_bth.n] = _value;
        }
      }

      buffer = buffer.slice(valueSize);
    }

    if (bttns.length > 0) {
      result["Buttons"] = bttns;
    }

    return result;
  }
};

// Shelly BLU BLE data parser wrapper
let ShellyBLUParser = {
  getData: function (res) {
    // Datagramm vor Dekodierung ausgeben
      let rawData = res.service_data[BTHOME_SVC_ID_STR];
      let hexString = "";
      for (let i = 0; i < rawData.length; i++) {
        let byteHex = rawData.at(i).toString(16);
        if (byteHex.length < 2) byteHex = "0" + byteHex; // einfache Null-Vorbereitung
        hexString += byteHex + " ";
      }
    console.log("BLE Datagram:", hexString.trim());
    
    let result = BTHomeDecoder.unpack(rawData);
    if (!result) return null;
    result.addr = res.addr;
    result.rssi = res.rssi;
    result.packet = hexString;
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
        Shelly.emitEvent("oh-blu.scan_result", {"addr": res.addr, "name": res.local_name, "rssi": res.rssi, "tx_power": res.tx_power_level});
        SHELLY_BLU_CACHE[res.addr] = res.local_name;
        found = true;
      }
    }
    if (!found) {
        console.log('Unknown Device ', res.local_name);
    }
  }

  let BTHparsed = ShellyBLUParser.getData(res);
  if (BTHparsed === null) {
    console.log("Failed to parse BTH data");
    return;
  }

  if (typeof LAST_PID[res.addr] === 'undefined' || BTHparsed.pid !== LAST_PID[res.addr]) {
    Shelly.emitEvent("oh-blu.data", BTHparsed);
    LAST_PID[res.addr] = BTHparsed.pid;
  }
}

// Start BLE scanning with fallback retry
function startBLEScan() {
  let bleScanSuccess = BLE.Scanner.Start({ duration_ms: SCAN_DURATION, active: true }, scanCB);
  if (bleScanSuccess === null) {
    console.log('Unable to start OH-BLU Scanner, make sure Shelly Gateway Support is disabled in device config.');
    Timer.set(3000, false, startBLEScan);
  } else {
    console.log('Success: OH-BLU Event Gateway running');
  }
}

// Check BLE config and start scanning
let BLEConfig = Shelly.getComponentConfig('ble');
if (BLEConfig.enable === false) {
  console.log('Error: BLE not enabled, unable to start OH-BLU Scanner');
} else {
  Timer.set(1000, false, startBLEScan);
}
