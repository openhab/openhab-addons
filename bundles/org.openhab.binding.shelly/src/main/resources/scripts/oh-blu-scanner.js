/*
 * This script uses the BLE scan functionality to pass scan results and BLU data to openHAB.
 * @author Markus Michels - Initial contribution
 * @author Udo Hartmann - Add support for decoding multi button inputs
 * @author Igor Jasan - Sensor parameter fixing, decoding of dimmer
 */

let ALLTERCO_DEVICE_NAME_PREFIX = ["SBBT", "SBDW", "SBMO", "SBHT", "SBDI", "SBRC", "SBWS"];
let ALLTERCO_MFD_ID_STR = "0ba9";
let BTHOME_SVC_ID_STR = "fcd2";

let ALLTERCO_MFD_ID = JSON.parse("0x" + ALLTERCO_MFD_ID_STR);
let BTHOME_SVC_ID = JSON.parse("0x" + BTHOME_SVC_ID_STR);
let SCAN_DURATION = BLE.Scanner.INFINITE_SCAN;

// Bump together with BTHomeDecoder.SCRIPT_DATA_VERSION in the binding
let EVENT_DATA_VERSION = 2;

let LVL_ERROR = 0;
let LVL_WARN = 1;
let LVL_INFO = 2;
let LVL_DEBUG = 3;
let LVL_TRACE = 4;
let LOG_LEVEL = LVL_INFO;

// LOG_LEVEL persisted in KVS survives the binding's script re-sync
Shelly.call("KVS.Get", { key: "oh-blu-scanner.log_level" }, function (res) {
  if (!res || typeof res.value !== "string") return;
  let name = res.value;
  let levels = { "ERROR": LVL_ERROR, "WARN": LVL_WARN, "INFO": LVL_INFO, "DEBUG": LVL_DEBUG, "TRACE": LVL_TRACE };
  let level = levels[name.toUpperCase()];
  if (typeof level !== "undefined") LOG_LEVEL = level;
});

// SHELLY_BLU_CACHE[addr]: device name if Shelly BLU, false if a known non-Shelly BTHome device
let SHELLY_BLU_CACHE = {};
let LAST_PID = {};

// Bounds only the negative entries (non-Shelly addresses) so an infinite scan seeing many
// rotating/random BLE addresses over time can't grow memory usage without limit. Recognized
// Shelly BLU devices are never evicted.
let NEGATIVE_CACHE_ORDER = [];
let MAX_CACHE_SIZE = 128;

function cacheSet(addr, value) {
  SHELLY_BLU_CACHE[addr] = value;
  if (value !== false) return;

  NEGATIVE_CACHE_ORDER.push(addr);
  if (NEGATIVE_CACHE_ORDER.length > MAX_CACHE_SIZE) {
    let evictAddr = NEGATIVE_CACHE_ORDER.shift();
    delete SHELLY_BLU_CACHE[evictAddr];
    delete LAST_PID[evictAddr];
  }
}

// No separators - also used as the raw payload sent to the binding
function bufToHex(buffer) {
  let hex = "";
  for (let i = 0; i < buffer.length; i++) {
    let hexValue = buffer.at(i).toString(16);
    hex += hexValue.length === 1 ? "0" + hexValue : hexValue;
  }
  return hex;
}

function scanCB(ev, res) {
  if (ev !== BLE.Scanner.SCAN_RESULT) return;
  if (typeof res.service_data === 'undefined' || typeof res.service_data[BTHOME_SVC_ID_STR] === 'undefined') return;

  let cached = SHELLY_BLU_CACHE[res.addr];
  if (cached === false) return;

  if (typeof cached === 'undefined') {
    if (typeof res.local_name !== "string") return;

    let found = false;
    for (let prefix of ALLTERCO_DEVICE_NAME_PREFIX) {
      if (res.local_name.indexOf(prefix) === 0) {
        if (LOG_LEVEL >= LVL_INFO) console.log('New device found: address=', res.addr, ', name=', res.local_name);
        Shelly.emitEvent("oh-blu.scan_result", {"addr":res.addr, "name":res.local_name, "rssi":res.rssi, "tx_power":res.tx_power_level});
        cacheSet(res.addr, res.local_name);
        found = true;
        break;
      }
    }
    if (!found) {
      if (LOG_LEVEL >= LVL_INFO) console.log('Unknown device: ', res.local_name);
      cacheSet(res.addr, false);
      return;
    }
  }

  let buffer = res.service_data[BTHOME_SVC_ID_STR];
  if (LOG_LEVEL >= LVL_TRACE) console.log('Raw BTHome packet from ', res.addr, ': ', bufToHex(buffer));

  if (typeof buffer !== "string" || buffer.length === 0) {
    if (LOG_LEVEL >= LVL_WARN) console.log("Failed to parse BTH data");
    return;
  }

  // Device Info Byte: bit 0 = encryption flag, bits 5-7 = BTHome version.
  let dib = buffer.at(0);
  if (dib & 0x1) {
    if (LOG_LEVEL >= LVL_WARN) console.log("BTH: encrypted payload, cannot decode");
    Shelly.emitEvent("oh-blu.alarm", {"addr":res.addr, "code":"BTH_ENCRYPTED"});
    return;
  }
  if ((dib >> 5) !== 2) {
    if (LOG_LEVEL >= LVL_WARN) console.log("Failed to parse BTH data");
    return;
  }

  // Packet ID (object 0x00) is always the first object when present
  let pid;
  if (buffer.length >= 3 && buffer.at(1) === 0x00) {
    pid = buffer.at(2);
  }
  if (typeof pid !== "undefined" && LAST_PID[res.addr] === pid) {
    if (LOG_LEVEL >= LVL_DEBUG) console.log("Drop redundant packet with pid ", pid);
    return;
  }
  if (typeof pid !== "undefined") LAST_PID[res.addr] = pid;

  if (LOG_LEVEL >= LVL_TRACE) console.log('Forwarding BTH data from device ', res.local_name, ', pid=', pid);
  Shelly.emitEvent("oh-blu.data", {"addr":res.addr, "rssi":res.rssi, "pid":pid, "ver":EVENT_DATA_VERSION, "raw":bufToHex(buffer.slice(1))});
}

// Retries in case script started before BLE infra was up
function startBLEScan() {
    let bleScanSuccess = BLE.Scanner.Start({ duration_ms: SCAN_DURATION, active: true }, scanCB);
    if( bleScanSuccess === null ) {
        if (LOG_LEVEL >= LVL_WARN) console.log('Unable to start OH-BLU Scanner.');
        Timer.set(3000, false, startBLEScan);
    } else {
        if (LOG_LEVEL >= LVL_INFO) console.log('Success: OH-BLU Event Gateway running');
    }
 }

let BLEConfig = Shelly.getComponentConfig('ble');
if(BLEConfig.enable === false) {
    if (LOG_LEVEL >= LVL_WARN) console.log('Error: BLE not enabled, unable to start OH-BLU Scanner');
} else {
    Timer.set(1000, false, startBLEScan);
}
