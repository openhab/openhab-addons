/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.goecharger.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * <a href="https://github.com/goecharger/go-eCharger-API-v2/blob/main/apikeys-en.md">go-e Charger API keys</a>
 *
 * @author Stefan Fussenegger - Initial contribution
 */
@NonNullByDefault
public final class GoEStatusV2ApiKeys {

    /**
     * allowChargePause (car compatiblity) (Config: bool [R/W])
     */
    public static final String ACP = "acp";

    /**
     * access_control user setting (Open=0, Wait=1) (Config: uint8 [R/W])
     */
    public static final String ACS = "acs";

    /**
     * How many ampere is the car allowed to charge now? (Status: int [R])
     */
    public static final String ACU = "acu";

    /**
     * Is the 16A adapter used? Limits the current to 16A (Status: bool [R])
     */
    public static final String ADI = "adi";

    /**
     * Is the car allowed to charge at all now? (Status: bool [R])
     */
    public static final String ALW = "alw";

    /**
     * ampere_max limit (Config: uint8 [R/W])
     */
    public static final String AMA = "ama";

    /**
     * requestedCurrent in Ampere, used for display on LED ring and logic calculations (Config: uint8 [R/W])
     */
    public static final String AMP = "amp";

    /**
     * temperatureCurrentLimit (Status: int [R])
     */
    public static final String AMT = "amt";

    /**
     * automatic stop remain in aWATTar (Config: bool [R/W])
     */
    public static final String ARA = "ara";

    /**
     * automatic stop energy in Wh (Config: double [R/W])
     */
    public static final String ATE = "ate";

    /**
     * nextTripPlanData (debug) (Status: optional&lt;object&gt; [R])
     */
    public static final String ATP = "atp";

    /**
     * automatic stop time in seconds since day begin, calculation: (hours*3600)+(minutes*60)+(seconds) (Config: seconds
     * [R/W])
     */
    public static final String ATT = "att";

    /**
     * Stromnetz average frequency (~50Hz) + (Status: TYPE [R])
     */
    public static final String AVGFHZ = "avgfhz";

    /**
     * awattar country (Austria=0, Germany=1) (Config: uint8 [R/W])
     */
    public static final String AWC = "awc";

    /**
     * awattar current price (Status: optional&lt;object&gt; [R])
     */
    public static final String AWCP = "awcp";

    /**
     * useAwattar (Config: bool [R/W])
     */
    public static final String AWE = "awe";

    /**
     * awattarMaxPrice in ct (Config: float [R/W])
     */
    public static final String AWP = "awp";

    /**
     * awattar price list, timestamps are measured in unix-time, seconds since 1970 + (Status: TYPE [W])
     */
    public static final String AWPL = "awpl";

    /**
     * Button allow Current change (0=AlwaysLock, 1=LockWhenCarIsConnected, 2=LockWhenCarIsCharging, 3=NeverLock)
     * (Config: uint8_t [R/W])
     */
    public static final String BAC = "bac";

    /**
     * Button Allow WiFi AP reset (0=AlwaysLock, 1=LockWhenCarIsConnected, 2=LockWhenCarIsCharging, 3=NeverLock) +
     * (Config: TYPE [R/W])
     */
    public static final String BAR = "bar";

    /**
     * carState, null if internal error (Unknown/Error=0, Idle=1, Charging=2, WaitCar=3, Complete=4, Error=5) (Status:
     * optional&lt;uint8&gt; [R])
     */
    public static final String CAR = "car";

    /**
     * array of user data (name, energy used, activation state) (Config: array [R/W])
     */
    public static final String CARDS = "cards";

    /**
     * cable_current_limit in A (Status: optional&lt;int&gt; [R])
     */
    public static final String CBL = "cbl";

    /**
     * Connected controller data + (Status: TYPE [R])
     */
    public static final String CCD = "ccd";

    /**
     * color_charging, format: #RRGGBB (Config: string [R/W])
     */
    public static final String CCH = "cch";

    /**
     * car consumption (only stored for app) (Config: double [R/W])
     */
    public static final String CCO = "cco";

    /**
     * chargectrl recommended version (Constant: string [R])
     */
    public static final String CCRV = "ccrv";

    /**
     * charge controller update progress (null if no update is in progress) (Status: optional&lt;object&gt; [R])
     */
    public static final String CCU = "ccu";

    /**
     * Currently connected WiFi (Status: optional&lt;object&gt; [R])
     */
    public static final String CCW = "ccw";

    /**
     * charging duration info (null=no charging in progress, type=0 counter going up, type=1 duration in ms) (Status:
     * object [R])
     */
    public static final String CDI = "cdi";

    /**
     * color_finished, format: #RRGGBB (Config: string [R/W])
     */
    public static final String CFI = "cfi";

    /**
     * color_idle, format: #RRGGBB (Config: string [R/W])
     */
    public static final String CID = "cid";

    /**
     * Cloud last error + (Status: TYPE [R])
     */
    public static final String CLE = "cle";

    /**
     * Cloud last error (age) + (Status: TYPE [R])
     */
    public static final String CLEA = "clea";

    /**
     * Current limits list + (Status: TYPE [R])
     */
    public static final String CLL = "cll";

    /**
     * current limit presets, max. 5 entries (Config: array [R/W])
     */
    public static final String CLP = "clp";

    /**
     * controllerMdnsMaxResults + (Config: TYPE [R])
     */
    public static final String CMMR = "cmmr";

    /**
     * controllerMdnsProto + (Config: TYPE [R])
     */
    public static final String CMP = "cmp";

    /**
     * controllerMdnsService + (Config: TYPE [R])
     */
    public static final String CMS = "cms";

    /**
     * controllerMdnsScanEnabled, set to false to completely disable any MDNS searches (debugging) + (Config: TYPE [R])
     */
    public static final String CMSE = "cmse";

    /**
     * controller scan active + (Status: TYPE [R])
     */
    public static final String CSA = "csa";

    /**
     * car type, free text string (max. 64 characters) + (Config: TYPE [R/W])
     */
    public static final String CT = "ct";

    /**
     * Controllers search result + (Status: TYPE [R])
     */
    public static final String CTRLS = "ctrls";

    /**
     * Cable unlock status (Unknown=0, Unlocked=1, UnlockFailed=2, Locked=3, LockFailed=4, LockUnlockPowerout=5)
     * (Status: uint8 [R])
     */
    public static final String CUS = "cus";

    /**
     * color_waitcar, format: #RRGGBB (Config: string [R/W])
     */
    public static final String CWC = "cwc";

    /**
     * cloud websocket enabled" (Config: bool [R/W])
     */
    public static final String CWE = "cwe";

    /**
     * grafana token from cloud for app + (Status: TYPE [R])
     */
    public static final String DATA = "data";

    /**
     * set this to 0-9 to clear card (erases card name, energy and rfid id) (Other: uint8 [W])
     */
    public static final String DEL = "del";

    /**
     * deltaA (Other: float [R])
     */
    public static final String DELTAA = "deltaa";

    /**
     * deltaP (Status: float [R])
     */
    public static final String DELTAP = "deltap";

    /**
     * set this to 0-9 to delete sta config (erases ssid, key, ...) (Other: uint8 [W])
     */
    public static final String DELW = "delw";

    /**
     * digital Input 1-phase + (Config: TYPE [R/W])
     */
    public static final String DI1 = "di1";

    /**
     * digital Input Enabled + (Config: TYPE [R/W])
     */
    public static final String DIE = "die";

    /**
     * digital Input Inverted + (Config: TYPE [R/W])
     */
    public static final String DII = "dii";

    /**
     * download link for app csv export + (Status: TYPE [R])
     */
    public static final String DLL = "dll";

    /**
     * DNS server (Status: object [R])
     */
    public static final String DNS = "dns";

    /**
     * inverter data source + (Status: TYPE [R])
     */
    public static final String DSRC = "dsrc";

    /**
     * charging energy limit, measured in Wh, null means disabled, not the next-trip energy (Config:
     * optional&lt;double&gt; [R/W])
     */
    public static final String DWO = "dwo";

    /**
     * error, null if internal error (None = 0, FiAc = 1, FiDc = 2, Phase = 3, Overvolt = 4, Overamp = 5, Diode = 6,
     * PpInvalid = 7, GndInvalid = 8, ContactorStuck = 9, ContactorMiss = 10, FiUnknown = 11, Unknown = 12, Overtemp =
     * 13, NoComm = 14, StatusLockStuckOpen = 15, StatusLockStuckLocked = 16, Reserved20 = 20, Reserved21 = 21,
     * Reserved22 = 22, Reserved23 = 23, Reserved24 = 24) (Status: optional&lt;uint8&gt; [R])
     */
    public static final String ERR = "err";

    /**
     * energy set kwh (only stored for app) (Config: bool [R/W])
     */
    public static final String ESK = "esk";

    /**
     * energy_total, measured in Wh (Status: uint64 [R])
     */
    public static final String ETO = "eto";

    /**
     * effectiveRoundingMode (Status: uint8 [R])
     */
    public static final String FERM = "ferm";

    /**
     * lock feedback (NoProblem=0, ProblemLock=1, ProblemUnlock=2) (Status: uint8 [R])
     */
    public static final String FFB = "ffb";

    /**
     * Stromnetz frequency (~50Hz) or 0 if unknown (Status: optional&lt;float&gt; [R])
     */
    public static final String FHZ = "fhz";

    /**
     * minChargeTime in milliseconds (Config: milliseconds [R/W])
     */
    public static final String FMT = "fmt";

    /**
     * friendlyName (Config: string [R/W])
     */
    public static final String FNA = "fna";

    /**
     * forceState (Neutral=0, Off=1, On=2) (Config: uint8 [R/W])
     */
    public static final String FRC = "frc";

    /**
     * roundingMode PreferPowerFromGrid=0, Default=1, PreferPowerToGrid=2 (Config: uint8 [R])
     */
    public static final String FRM = "frm";

    /**
     * force_single_phase, das Rechenergebnis der Ladelogik, ob gerade single phase benötigt wird oder nicht. (Status:
     * bool [R])
     */
    public static final String FSP = "fsp";

    /**
     * force single phase toggle wished since (Status: optional&lt;milliseconds&gt; [R])
     */
    public static final String FSPTWS = "fsptws";

    /**
     * startingPower in watts (Config: float [R/W])
     */
    public static final String FST = "fst";

    /**
     * usePvSurplus (Config: bool [R/W])
     */
    public static final String FUP = "fup";

    /**
     * firmware from CarControl (Constant: string [R])
     */
    public static final String FWC = "fwc";

    /**
     * FW_VERSION (Constant: string [R])
     */
    public static final String FWV = "fwv";

    /**
     * zeroFeedin (Config: bool [R/W])
     */
    public static final String FZF = "fzf";

    /**
     * gridMonitoringTimeReconnection in seconds + (Config: TYPE [R/W])
     */
    public static final String GMTR = "gmtr";

    /**
     * gridMonitoring last failure + (Status: TYPE [R/W])
     */
    public static final String GSA = "gsa";

    /**
     * httpApiEnabled (allows /api/status and /api/set requests) + (Config: TYPE [R/W])
     */
    public static final String HAI = "hai";

    /**
     * httpLegacyApiEnabled (allows /status and /mqtt requests) + (Config: TYPE [R/W])
     */
    public static final String HLA = "hla";

    /**
     * hostname used on STA interface (Status: optional&lt;string&gt; [R])
     */
    public static final String HOST = "host";

    /**
     * httpStaAuthentication (Config: bool [R/W])
     */
    public static final String HSA = "hsa";

    /**
     * Inverter data override (Config: optional&lt;object&gt; [R])
     */
    public static final String IDO = "ido";

    /**
     * PvSurPlus information. e.g.: {"pGrid": 1000., "pPv": 1400., "pAkku": 2000.} pGrid < 0 ==> feed grid, pAkku < 0
     * ==> load battery, pPv > 0 ==> PV production, pPv < 0 ==> standby. Needed all 5 seconds. Can be read back within
     * 10 seconds after set. pPv und pAkku are optional (Other: bool [R/W])
     */
    public static final String IDS = "ids";

    /**
     * age of inverter data (Status: milliseconds [R])
     */
    public static final String INVA = "inva";

    /**
     * limit adapter 1-phase (in A) + (Config: TYPE [R/W])
     */
    public static final String LA1 = "la1";

    /**
     * limit adapter 3-phase (in A) + (Config: TYPE [R/W])
     */
    public static final String LA3 = "la3";

    /**
     * lastButtonHoldLong + (Config: TYPE [R])
     */
    public static final String LBL = "lbl";

    /**
     * lastButtonPress in milliseconds (Status: milliseconds [R])
     */
    public static final String LBP = "lbp";

    /**
     * led_bright, 0-255 (Config: uint8 [R/W])
     */
    public static final String LBR = "lbr";

    /**
     * lastCarStateChangedFromCharging (in ms) (Status: optional&lt;milliseconds&gt; [R])
     */
    public static final String LCCFC = "lccfc";

    /**
     * lastCarStateChangedFromIdle (in ms) (Status: optional&lt;milliseconds&gt; [R])
     */
    public static final String LCCFI = "lccfi";

    /**
     * lastCarStateChangedToCharging (in ms) (Status: optional&lt;milliseconds&gt; [R])
     */
    public static final String LCCTC = "lcctc";

    /**
     * Effective lock setting, as sent to Charge Ctrl (Normal=0, AutoUnlock=1, AlwaysLock=2, ForceUnlock=3) (Status:
     * uint8 [R])
     */
    public static final String LCK = "lck";

    /**
     * last controller scan timestamp in milliseconds since boot time + (Status: TYPE [R])
     */
    public static final String LCS = "lcs";

    /**
     * internal infos about currently running led animation (Status: object [R])
     */
    public static final String LED = "led";

    /**
     * last force single phase toggle (Status: optional&lt;milliseconds&gt; [R])
     */
    public static final String LFSPT = "lfspt";

    /**
     * logic mode (Default=3, Awattar=4, AutomaticStop=5) (Config: uint8 [R/W])
     */
    public static final String LMO = "lmo";

    /**
     * last model status change (Status: milliseconds [R])
     */
    public static final String LMSC = "lmsc";

    /**
     * load balancing ampere (Status: optional&lt;uint8&gt; [R])
     */
    public static final String LOA = "loa";

    /**
     * local time (Status: string [R])
     */
    public static final String LOC = "loc";

    /**
     * Load balancing enabled (Config: bool [R/W])
     */
    public static final String LOE = "loe";

    /**
     * load_fallback (Config: uint8 [R/W])
     */
    public static final String LOF = "lof";

    /**
     * load_group_id (Config: string [R/W])
     */
    public static final String LOG = "log";

    /**
     * load_priority (Config: uint16 [R/W])
     */
    public static final String LOP = "lop";

    /**
     * load balancing protected + (Config: TYPE [R/W])
     */
    public static final String LOPR = "lopr";

    /**
     * load balancing total amp (Config: uint32 [R/W])
     */
    public static final String LOT = "lot";

    /**
     * load balancing type (Static=0, Dynamic=1) (Config: uint8 [R/W])
     */
    public static final String LOTY = "loty";

    /**
     * last pv surplus calculation (Status: milliseconds [R])
     */
    public static final String LPSC = "lpsc";

    /**
     * last rfid card index + (Status: TYPE [R])
     */
    public static final String LRC = "lrc";

    /**
     * last rfid id (only available when sendRfid) + (Status: TYPE [R])
     */
    public static final String LRI = "lri";

    /**
     * set this to 0-9 to learn last read card id (Other: uint8 [W])
     */
    public static final String LRN = "lrn";

    /**
     * last rfid read (milliseconds since boot) + (Status: TYPE [R])
     */
    public static final String LRR = "lrr";

    /**
     * led_save_energy (Config: bool [R/W])
     */
    public static final String LSE = "lse";

    /**
     * local time offset in milliseconds, tab + rbt + lto = local time + (Status: TYPE [R])
     */
    public static final String LTO = "lto";

    /**
     * last wifi connect failed (milliseconds since boot) + (Status: TYPE [R])
     */
    public static final String LWF = "lwf";

    /**
     * load_mapping (uint8_t[3]) (Config: array [R/W])
     */
    public static final String MAP = "map";

    /**
     * minChargingCurrent (Config: uint8 [R/W])
     */
    public static final String MCA = "mca";

    /**
     * MQTT connected + (Status: TYPE [R])
     */
    public static final String MCC = "mcc";

    /**
     * MQTT connected (age) + (Status: TYPE [R])
     */
    public static final String MCCA = "mcca";

    /**
     * MQTT enabled + (Config: TYPE [R/W])
     */
    public static final String MCE = "mce";

    /**
     * minimumChargingInterval in milliseconds (0 means disabled) (Config: milliseconds [R/W])
     */
    public static final String MCI = "mci";

    /**
     * minChargePauseDuration in milliseconds (0 means disabled) (Config: milliseconds [R/W])
     */
    public static final String MCPD = "mcpd";

    /**
     * minChargePauseEndsAt (set to null to abort current minChargePauseDuration) (Status: optional&lt;milliseconds&gt;
     * [R/W])
     */
    public static final String MCPEA = "mcpea";

    /**
     * MQTT readonly (don't allow api writes from mqtt broker) + (Config: TYPE [R/W])
     */
    public static final String MCR = "mcr";

    /**
     * MQTT started + (Status: TYPE [R])
     */
    public static final String MCS = "mcs";

    /**
     * MQTT broker url + (Config: TYPE [R/W])
     */
    public static final String MCU = "mcu";

    /**
     * modbus slave enabled + (Config: TYPE [R/W])
     */
    public static final String MEN = "men";

    /**
     * MQTT enable homeassistant discovery + (Config: TYPE [R/W])
     */
    public static final String MHE = "mhe";

    /**
     * MQTT homeassistant topic prefix (set to null to reset back to the default) + (Config: TYPE [R/W])
     */
    public static final String MHT = "mht";

    /**
     * MQTT last error + (Status: TYPE [R])
     */
    public static final String MLR = "mlr";

    /**
     * MQTT last error (age) + (Status: TYPE [R])
     */
    public static final String MLRA = "mlra";

    /**
     * maximumMeasuredChargingPower (debug) (Status: float [R])
     */
    public static final String MMP = "mmp";

    /**
     * Reason why we allow charging or not right now (NotChargingBecauseNoChargeCtrlData=0,
     * NotChargingBecauseOvertemperature=1, NotChargingBecauseAccessControlWait=2, ChargingBecauseForceStateOn=3,
     * NotChargingBecauseForceStateOff=4, NotChargingBecauseScheduler=5, NotChargingBecauseEnergyLimit=6,
     * ChargingBecauseAwattarPriceLow=7, ChargingBecauseAutomaticStopTestLadung=8,
     * ChargingBecauseAutomaticStopNotEnoughTime=9, ChargingBecauseAutomaticStop=10,
     * ChargingBecauseAutomaticStopNoClock=11, ChargingBecausePvSurplus=12, ChargingBecauseFallbackGoEDefault=13,
     * ChargingBecauseFallbackGoEScheduler=14, ChargingBecauseFallbackDefault=15,
     * NotChargingBecauseFallbackGoEAwattar=16, NotChargingBecauseFallbackAwattar=17,
     * NotChargingBecauseFallbackAutomaticStop=18, ChargingBecauseCarCompatibilityKeepAlive=19,
     * ChargingBecauseChargePauseNotAllowed=20, NotChargingBecauseSimulateUnplugging=22,
     * NotChargingBecausePhaseSwitch=23, NotChargingBecauseMinPauseDuration=24, NotChargingBecauseError=26,
     * NotChargingBecauseLoadManagementDoesntWant=27, NotChargingBecauseOcppDoesntWant=28,
     * NotChargingBecauseReconnectDelay=29, NotChargingBecauseAdapterBlocking=30,
     * NotChargingBecauseUnderfrequencyControl=31, NotChargingBecauseUnbalancedLoad=32,
     * ChargingBecauseDischargingPvBattery=33, NotChargingBecauseGridMonitoring=34, NotChargingBecauseOcppFallback=35)
     * (Status: uint8 [R])
     */
    public static final String MODELSTATUS = "modelStatus";

    /**
     * min phase toggle wait time (in milliseconds) (Config: milliseconds [R/W])
     */
    public static final String MPTWT = "mptwt";

    /**
     * min phase wish switch time (in milliseconds) (Config: milliseconds [R/W])
     */
    public static final String MPWST = "mpwst";

    /**
     * MQTT skipCertCommonNameCheck + (Config: TYPE [R/W])
     */
    public static final String MQCN = "mqcn";

    /**
     * MQTT useGlobalCaStore + (Config: TYPE [R/W])
     */
    public static final String MQG = "mqg";

    /**
     * MQTT skipServerVerification + (Config: TYPE [R/W])
     */
    public static final String MQSS = "mqss";

    /**
     * modbus slave swap bytes + (Config: TYPE [R/W])
     */
    public static final String MSB = "msb";

    /**
     * modbus slave port (requires off/on toggle) + (Config: TYPE [R/W])
     */
    public static final String MSP = "msp";

    /**
     * modbus slave swap registers + (Config: TYPE [R/W])
     */
    public static final String MSR = "msr";

    /**
     * MQTT topic prefix (set to null to reset back to the default) + (Config: TYPE [R/W])
     */
    public static final String MTP = "mtp";

    /**
     * Default route (Status: string [R])
     */
    public static final String NIF = "nif";

    /**
     * norway_mode / ground check enabled when norway mode is disabled (inverted) (Config: bool [R/W])
     */
    public static final String NMO = "nmo";

    /**
     * energy array, U (L1, L2, L3, N), I (L1, L2, L3), P (L1, L2, L3, N, Total), pf (L1, L2, L3, N) (Status: array [R])
     */
    public static final String NRG = "nrg";

    /**
     * OCPP connected and accepted (Status: bool [R])
     */
    public static final String OCPPA = "ocppa";

    /**
     * OCPP connected and accepted (timestamp in milliseconds since reboot) Subtract from reboot time (rbt) to get
     * number of milliseconds since connected (Status: null or milliseconds [R])
     */
    public static final String OCPPAA = "ocppaa";

    /**
     * OCPP clock aligned data interval (can also be read/written with `GetConfiguration` and `ChangeConfiguration`)
     * (Config: seconds [R/W])
     */
    public static final String OCPPAI = "ocppai";

    /**
     * OCPP AllowOfflineTxForUnknownId + (Status: TYPE [R/W])
     */
    public static final String OCPPAO = "ocppao";

    /**
     * OCPP connected (Status: bool [R])
     */
    public static final String OCPPC = "ocppc";

    /**
     * OCPP connected (timestamp in milliseconds since reboot) Subtract from reboot time (rbt) to get number of
     * milliseconds since connected (Status: null or milliseconds [R])
     */
    public static final String OCPPCA = "ocppca";

    /**
     * OCPP client cert (Config: string [R/W])
     */
    public static final String OCPPCC = "ocppcc";

    /**
     * OCPP client key (Config: string [R/W])
     */
    public static final String OCPPCK = "ocppck";

    /**
     * OCPP LocalAuthListEnabled + (Status: TYPE [R/W])
     */
    public static final String OCPPCM = "ocppcm";

    /**
     * OCPP skipCertCommonNameCheck (Config: bool [R/W])
     */
    public static final String OCPPCN = "ocppcn";

    /**
     * OCPP connector status (0=Available, 1=Preparing, 2=Charging, 3=SuspendedEVSE, 4=SuspendedEV, 5=Finishing,
     * 6=Reserved, 7=Unavailable, 8=Faulted) + (Status: TYPE [R])
     */
    public static final String OCPPCS = "ocppcs";

    /**
     * OCPP dummy card id (used when no card has been used and charging is already allowed / starting) (Config: string
     * [R/W])
     */
    public static final String OCPPD = "ocppd";

    /**
     * OCPP enabled (Config: bool [R/W])
     */
    public static final String OCPPE = "ocppe";

    /**
     * OCPP fallback current + (Config: TYPE [R/W])
     */
    public static final String OCPPF = "ocppf";

    /**
     * OCPP use global CA Store (Config: bool [R/W])
     */
    public static final String OCPPG = "ocppg";

    /**
     * OCPP heartbeat interval (can also be read/written with `GetConfiguration` and `ChangeConfiguration`) (Config:
     * seconds [R/W])
     */
    public static final String OCPPH = "ocpph";

    /**
     * OCPP meter values sample interval (can also be read/written with `GetConfiguration` and `ChangeConfiguration`)
     * (Config: seconds [R/W])
     */
    public static final String OCPPI = "ocppi";

    /**
     * OCPP LocalAuthListEnabled + (Status: TYPE [R/W])
     */
    public static final String OCPPLA = "ocppla";

    /**
     * OCPP last error (Status: string or null [R])
     */
    public static final String OCPPLE = "ocpple";

    /**
     * OCPP last error (timestamp in milliseconds since reboot) Subtract from reboot time (rbt) to get number of
     * milliseconds since connected (Status: null or milliseconds [R])
     */
    public static final String OCPPLEA = "ocpplea";

    /**
     * OCPP LocalAuthorizeOffline + (Status: TYPE [R/W])
     */
    public static final String OCPPLO = "ocpplo";

    /**
     * OCPP rotate phases on charger (Config: bool [R/W])
     */
    public static final String OCPPR = "ocppr";

    /**
     * OCPP remote logging (usually only enabled by go-e support to allow debugging) (Config: bool [R/W])
     */
    public static final String OCPPRL = "ocpprl";

    /**
     * OCPP started (Status: bool [R])
     */
    public static final String OCPPS = "ocpps";

    /**
     * OCPP server cert (Config: string [R/W])
     */
    public static final String OCPPSC = "ocppsc";

    /**
     * OCPP skipServerVerification (Config: bool [R/W])
     */
    public static final String OCPPSS = "ocppss";

    /**
     * OCPP transaction id + (Status: TYPE [R/W])
     */
    public static final String OCPPTI = "ocppti";

    /**
     * OCPP server url (Config: string [R/W])
     */
    public static final String OCPPU = "ocppu";

    /**
     * firmware update trigger (must specify a branch from ocu) (Other: string [W])
     */
    public static final String OCT = "oct";

    /**
     * list of available firmware branches (Status: array [R])
     */
    public static final String OCU = "ocu";

    /**
     * OEM manufacturer (Constant: string [R])
     */
    public static final String OEM = "oem";

    /**
     * pAkku in W (Status: optional&lt;float&gt; [R])
     */
    public static final String PAKKU = "pakku";

    /**
     * controllerCloudKey + (Config: TYPE [R])
     */
    public static final String PCO = "pco";

    /**
     * protect Digital Input + (Config: TYPE [R/W])
     */
    public static final String PDI = "pdi";

    /**
     * protect Grid Requirements + (Config: TYPE [R/W])
     */
    public static final String PGR = "pgr";

    /**
     * pGrid in W (Status: optional&lt;float&gt; [R])
     */
    public static final String PGRID = "pgrid";

    /**
     * pGridTarget in W (Config: float [R/W])
     */
    public static final String PGT = "pgt";

    /**
     * phases (Status: optional&lt;array&gt; [R])
     */
    public static final String PHA = "pha";

    /**
     * numberOfPhases (Status: uint8 [R])
     */
    public static final String PNP = "pnp";

    /**
     * prioOffset in W (Config: float [R/W])
     */
    public static final String PO = "po";

    /**
     * pPv in W (Status: optional&lt;float&gt; [R])
     */
    public static final String PPV = "ppv";

    /**
     * phaseSwitchHysteresis in W (Config: float [R/W])
     */
    public static final String PSH = "psh";

    /**
     * phaseSwitchMode (Auto=0, Force_1=1, Force_3=2) (Config: uint8 [R/W])
     */
    public static final String PSM = "psm";

    /**
     * forceSinglePhaseDuration (in milliseconds) (Config: milliseconds [R/W])
     */
    public static final String PSMD = "psmd";

    /**
     * averagePAkku (Status: float [R])
     */
    public static final String PVOPT_AVERAGEPAKKU = "pvopt_averagePAkku";

    /**
     * averagePGrid (Status: float [R])
     */
    public static final String PVOPT_AVERAGEPGRID = "pvopt_averagePGrid";

    /**
     * averagePPv (Status: float [R])
     */
    public static final String PVOPT_AVERAGEPPV = "pvopt_averagePPv";

    /**
     * phase wish mode for debugging / only for pv optimizing / used for timers later (Force_3=0, Wish_1=1, Wish_3=2)
     * (Status: uint8 [R])
     */
    public static final String PWM = "pwm";

    /**
     * reboot_counter (Status: uint32 [R])
     */
    public static final String RBC = "rbc";

    /**
     * time since boot in milliseconds (Status: milliseconds [R])
     */
    public static final String RBT = "rbt";

    /**
     * randomDelayStartFlexibleTariffCharging in seconds + (Config: TYPE [R/W])
     */
    public static final String RDBF = "rdbf";

    /**
     * randomDelayStartFlexibleTariffChargingEndsAt (set to null to abort current
     * randomDelayStartFlexibleTariffCharging) + (Config: TYPE [R/W])
     */
    public static final String RDBFE = "rdbfe";

    /**
     * randomDelayStartScheduledCharging in seconds + (Config: TYPE [R/W])
     */
    public static final String RDBS = "rdbs";

    /**
     * randomDelayStartScheduledChargingEndsAt (set to null to abort current randomDelayStartScheduledCharging) +
     * (Config: TYPE [R/W])
     */
    public static final String RDBSE = "rdbse";

    /**
     * send rfid serial to cloud/api/mqtt (enable lri api key to show rfid numbers) + (Config: TYPE [R/W])
     */
    public static final String RDE = "rde";

    /**
     * randomDelayStopFlexibleTariffCharging in seconds + (Config: TYPE [R/W])
     */
    public static final String RDEF = "rdef";

    /**
     * randomDelayStopFlexibleTariffChargingEndsAt (set to null to abort current randomDelayStopFlexibleTariffCharging)
     * + (Config: TYPE [R/W])
     */
    public static final String RDEFE = "rdefe";

    /**
     * randomDelayStopScheduledCharging in seconds + (Config: TYPE [R/W])
     */
    public static final String RDES = "rdes";

    /**
     * randomDelayStopScheduledChargingEndsAt (set to null to abort current randomDelayStopScheduledCharging) + (Config:
     * TYPE [R/W])
     */
    public static final String RDESE = "rdese";

    /**
     * randomDelayWhenPluggingCar in seconds + (Config: TYPE [R/W])
     */
    public static final String RDPL = "rdpl";

    /**
     * randomDelayWhenPluggingCarEndsAt (set to null to abort current randomDelayWhenPluggingCar) + (Config: TYPE [R/W])
     */
    public static final String RDPLE = "rdple";

    /**
     * randomDelayReconnection in seconds + (Config: TYPE [R/W])
     */
    public static final String RDRE = "rdre";

    /**
     * randomDelayReconnectionEndsAt (set to null to abort current randomDelayReconnection) + (Config: TYPE [R/W])
     */
    public static final String RDREE = "rdree";

    /**
     * Relay Feedback (Status: int [R])
     */
    public static final String RFB = "rfb";

    /**
     * reconnectionMaximumFrequency in Hz + (Config: TYPE [R/W])
     */
    public static final String RMAF = "rmaf";

    /**
     * reconnectionMaximumVoltage in Volt + (Config: TYPE [R/W])
     */
    public static final String RMAV = "rmav";

    /**
     * reconnectionMinimumFrequency in Hz + (Config: TYPE [R/W])
     */
    public static final String RMIF = "rmif";

    /**
     * reconnectionMinimumVoltage in Volt + (Config: TYPE [R/W])
     */
    public static final String RMIV = "rmiv";

    /**
     * rampup started at + (Status: TYPE [R/W])
     */
    public static final String RSA = "rsa";

    /**
     * rampupAtStartAndReconnectionEnabled + (Config: TYPE [R/W])
     */
    public static final String RSRE = "rsre";

    /**
     * rampupAtStartAndReconnectionRate in %/s + (Config: TYPE [R/W])
     */
    public static final String RSRR = "rsrr";

    /**
     * RSSI signal strength (Status: optional&lt;int8&gt; [R])
     */
    public static final String RSSI = "rssi";

    /**
     * Reboot charger (Other: any [W])
     */
    public static final String RST = "rst";

    /**
     * wifi scan age (Status: milliseconds [R])
     */
    public static final String SCAA = "scaa";

    /**
     * wifi scan result (encryptionType: OPEN=0, WEP=1, WPA_PSK=2, WPA2_PSK=3, WPA_WPA2_PSK=4, WPA2_ENTERPRISE=5,
     * WPA3_PSK=6, WPA2_WPA3_PSK=7) (Status: array [R])
     */
    public static final String SCAN = "scan";

    /**
     * scheduler_saturday, control enum values: Disabled=0, Inside=1, Outside=2 (Config: object [R/W])
     */
    public static final String SCH_SATUR = "sch_satur";

    /**
     * scheduler_sunday, control enum values: Disabled=0, Inside=1, Outside=2 (Config: object [R/W])
     */
    public static final String SCH_SUND = "sch_sund";

    /**
     * scheduler_weekday, control enum values: Disabled=0, Inside=1, Outside=2 (Config: object [R/W])
     */
    public static final String SCH_WEEK = "sch_week";

    /**
     * Button Allow Force change (0=AlwaysLock, 1=LockWhenCarIsConnected, 2=LockWhenCarIsCharging, 3=NeverLock) (Config:
     * uint8_t [R/W])
     */
    public static final String SDP = "sdp";

    /**
     * stopHysteresis in W (Config: float [R/W])
     */
    public static final String SH = "sh";

    /**
     * smart meter data + (Status: TYPE [R])
     */
    public static final String SMD = "smd";

    /**
     * threePhaseSwitchLevel (Config: float [R/W])
     */
    public static final String SPL3 = "spl3";

    /**
     * serial number (Constant: string [R])
     */
    public static final String SSE = "sse";

    /**
     * simulateUnpluggingShort (Config: bool [R/W])
     */
    public static final String SU = "su";

    /**
     * simulateUnpluggingAlways (Config: bool [R/W])
     */
    public static final String SUA = "sua";

    /**
     * simulate unpluging duration (in milliseconds) (Config: milliseconds [R/W])
     */
    public static final String SUMD = "sumd";

    /**
     * led strip T0H + (Config: TYPE [R/W])
     */
    public static final String T0H = "t0h";

    /**
     * led strip T0L + (Config: TYPE [R/W])
     */
    public static final String T0L = "t0l";

    /**
     * led strip T1H + (Config: TYPE [R/W])
     */
    public static final String T1H = "t1h";

    /**
     * led strip T1L + (Config: TYPE [R/W])
     */
    public static final String T1L = "t1l";

    /**
     * time at boot in utc in milliseconds, add rbt to get to current utc time + (Status: TYPE [R])
     */
    public static final String TAB = "tab";

    /**
     * temporary current limit (does not change the user current limit, will be reset after 10min if not updated
     * regulary) + (Config: TYPE [R/W])
     */
    public static final String TCL = "tcl";

    /**
     * timezone daylight saving mode, None=0, EuropeanSummerTime=1, UsDaylightTime=2 (Config: uint8 [R/W])
     */
    public static final String TDS = "tds";

    /**
     * testLadungFinished (debug) (Status: bool [R])
     */
    public static final String TLF = "tlf";

    /**
     * testLadungStarted (debug) (Status: bool [R])
     */
    public static final String TLS = "tls";

    /**
     * temperature sensors (Status: array [R])
     */
    public static final String TMA = "tma";

    /**
     * timezone offset in minutes (Config: minutes [R/W])
     */
    public static final String TOF = "tof";

    /**
     * 30 seconds total power average (used to get better next-trip predictions) (Status: float [R])
     */
    public static final String TPA = "tpa";

    /**
     * transaction, null when no transaction, 0 when without card, otherwise cardIndex + 1 (1: 0. card, 2: 1. card, ...)
     * (Status: optional&lt;uint8&gt; [R/W])
     */
    public static final String TRX = "trx";

    /**
     * time server enabled (NTP) (Config: bool [R/W])
     */
    public static final String TSE = "tse";

    /**
     * transaction start rfidid (only available when sendRfid) + (Status: TYPE [R])
     */
    public static final String TSI = "tsi";

    /**
     * time server sync status (RESET=0, COMPLETED=1, IN_PROGRESS=2) (Config: uint8 [R])
     */
    public static final String TSSS = "tsss";

    /**
     * Devicetype (Constant: string [R])
     */
    public static final String TYP = "typ";

    /**
     * timezone type, freetext string for app selection + (Config: TYPE [R/W])
     */
    public static final String TZT = "tzt";

    /**
     * Underfrequency Control activation threshold + (Config: TYPE [R/W])
     */
    public static final String UFA = "ufa";

    /**
     * Underfrequency Control enabled + (Config: TYPE [R/W])
     */
    public static final String UFE = "ufe";

    /**
     * Underfrequency Control mode (TypeNominal=0, TypeActual=1) + (Config: TYPE [R/W])
     */
    public static final String UFM = "ufm";

    /**
     * Underfrequency Control stop frequency + (Config: TYPE [R/W])
     */
    public static final String UFS = "ufs";

    /**
     * unlock_power_outage (Config: bool [R/W])
     */
    public static final String UPO = "upo";

    /**
     * unlock_setting (Normal=0, AutoUnlock=1, AlwaysLock=2) (Config: uint8 [R/W])
     */
    public static final String UST = "ust";

    /**
     * utc time (Status: string [R/W])
     */
    public static final String UTC = "utc";

    /**
     * variant: max Ampere value of unit (11: 11kW/16A, 22: 22kW/32A) (Constant: uint8 [R])
     */
    public static final String VAR = "var";

    /**
     * WiFi Bandwidth (for both AP and STA) WIFI_BW_HT20=1, WIFI_BW_HT40=2 + (Config: TYPE [R])
     */
    public static final String WBW = "wbw";

    /**
     * WiFi current mac address (Status: string [R])
     */
    public static final String WCB = "wcb";

    /**
     * disable AccessPoint when cloud is connected + (Config: TYPE [R/W])
     */
    public static final String WDA = "wda";

    /**
     * WiFi failed mac addresses (Status: array [R])
     */
    public static final String WFB = "wfb";

    /**
     * energy in Wh since car connected (Status: double [R])
     */
    public static final String WH = "wh";

    /**
     * wifi configurations with ssids and keys, if you only want to change the second entry, send an array with 1 empty
     * and 1 filled wifi config object: `[{}, {"ssid":"","key":""}]` (Config: array [R/W])
     */
    public static final String WIFIS = "wifis";

    /**
     * WiFi planned mac addresses (Status: array [R])
     */
    public static final String WPB = "wpb";

    /**
     * WiFi STA error count (Status: uint8 [R])
     */
    public static final String WSC = "wsc";

    /**
     * WiFi STA error messages log + (Status: TYPE [R])
     */
    public static final String WSL = "wsl";

    /**
     * WiFi STA error message (Status: string [R])
     */
    public static final String WSM = "wsm";

    /**
     * WiFi state machine state (None=0, Scanning=1, Connecting=2, Connected=3) (Status: uint8 [R])
     */
    public static final String WSMS = "wsms";

    /**
     * WiFi STA status (IDLE_STATUS=0, NO_SSID_AVAIL=1, SCAN_COMPLETED=2, CONNECTED=3, CONNECT_FAILED=4,
     * CONNECTION_LOST=5, DISCONNECTED=6, CONNECTING=8, DISCONNECTING=9, NO_SHIELD=10 (for compatibility with WiFi
     * Shield library)) (Status: uint8 [R])
     */
    public static final String WST = "wst";

    /**
     * zeroFeedinOffset in W (Config: float [R/W])
     */
    public static final String ZFO = "zfo";

    private GoEStatusV2ApiKeys() {
    }
}
