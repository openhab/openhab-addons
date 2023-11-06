/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.dscalarm.internal;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumerator for DSCAlarm Command and Message Codes.
 *
 * @author Russell Stephens - Initial Contribution
 */
public enum DSCAlarmCode {
    // Command Codes
    Poll("000", "Poll", "000: The poll command."),
    StatusReport("001", "Status Report", "001: Request a status report command."),
    LabelsRequest("002", "Labels Request", "002: IT-100 labels request command."),
    NetworkLogin("005", "Network Login", "005: Envisalink login command."),
    DumpZoneTimers("008", "Dump Zone Timers", "008: Dump the internal Envisalink Zone Timers command."),
    SetTimeDate("010", "Set Time and Date", "010: Set the time and date command."),
    CommandOutputControl("020", "Command Output Control", "020: Activate the selected Command Output command."),
    PartitionArmControlAway("030", "Partition Arm Control - Away", "030: Arm the partition in AWAY mode."),
    PartitionArmControlStay("031", "Partition Arm Control - Stay", "031: Arm the partition in STAY mode."),
    PartitionArmControlZeroEntryDelay("032", "Partition Arm Control - Zero Entry Delay",
            "032: Arm the partition with zero entry delay."),
    PartitionArmControlWithUserCode("033", "Partition Arm Control - With User Code",
            "033: Arm the partition with user code."),
    PartitionDisarmControl("040", "Partition Disarm Control", "040: Disarm the partition with user code."),
    TimeStampControl("055", "Time Stamp Control", "055: Prepend all messages with a timestamp."),
    TimeDateBroadcastControl("056", "Time/Date Broadcast Control",
            "056: Periodically transmit system time broadcasts."),
    TemperatureBroadcastControl("057", "Temperature Broadcast Control",
            "057: Periodically transmit the interior and exterior temperatures."),
    VirtualKeypadControl("058", "Virtual Keypad Control", "058: Enable/Disable the virtual keypad command."),
    TriggerPanicAlarm("060", "Trigger Panic Alarm",
            "060: Emulates the FAP (Fire, Ambulance, Police) panic keys on a DSC keypad."),
    KeyStroke("070", "Key Stroke", "070: Send a single keystroke on Partition 1 only."),
    KeySequence("071", "Key Stroke Sequence", "071: Send a keystroke string."),
    EnterUserCodeProgramming("072", "Enter User Code Programming",
            "072: Cause the partition to enter user code (*5) programming."),
    EnterUserProgramming("073", "Enter User Programming",
            "073: Cause the partition to enter user code (*6) programming."),
    KeepAlive("074", "Keep Alive", "074: Reset the time-out timer on the panel."),
    BaudRateChange("080", "Baud Rate Change", "080: Change the baud rate on the IT-100."),
    GetTemperatureSetPoint("095", "Get Temperature Set Points", "095: Request the thermostat temperature set points."),
    TemperatureChange("096", "Temperature Change", "096: Change the thermostat temperature."),
    SaveTemperatureSetting("097", "Save Temperature Setting", "097: Save the thermostat temperature."),
    CodeSend("200", "Code Send", "200: Send a user code."),

    // Message Codes
    CommandAcknowledge("500", "Command Acknowledge", "500: A command has been received successfully."),
    CommandError("501", "Command Error", "501: A command has been received with a bad checksum."),
    SystemError("502", "System Error", "502: An error has been detected."),
    LoginResponse("505", "Login Interaction",
            "505: Login response (failed=0, success=1, time out=2, password request=3)."),
    KeypadLEDState("510", "Keypad LED State - Partition 1 Only",
            "510: A change of state in the Partition 1 keypad LEDs."),
    KeypadLEDFlashState("511", "Keypad LED Flash State - Partition 1 Only",
            "511: A change of state in the Partition 1 keypad LEDs as to whether to flash or not."),
    TimeDateBroadcast("550", "Time-Date Broadcast", "550: The current security system time."),
    RingDetected("560", "Ring Detected", "560: A ring on the telephone line."),
    IndoorTemperatureBroadcast("561", "Indoor Temperature Broadcast",
            "561: The interior temperature and the thermostat number."),
    OutdoorTemperatureBroadcast("562", "Outdoor Temperature Broadcast",
            "562: The exterior temperature and the thermostat number."),
    ThermostatSetPoints("563", "Thermostat Set Points",
            "563: Cooling and heating set points and the thermostat number."),
    BroadcastLabels("570", "Broadcast Labels", "570: Labels stored in the DSC Alarm."),
    BaudRateSet("580", "Baud Rate Set", "580: Baud Rate of the serial interface."),

    ZoneAlarm("601", "Zone Alarm", "601: A zone has gone into alarm."),
    ZoneAlarmRestore("602", "Zone Alarm Restore", "602: A zone alarm has been restored."),
    ZoneTamper("603", "Zone Tamper", "603: A zone has a tamper condition."),
    ZoneTamperRestore("604", "Zone Tamper Restored", "604: A zone tamper condition has been restored."),
    ZoneFault("605", "Zone Fault", "605: A zone has a fault condition."),
    ZoneFaultRestore("606", "Zone Fault Restored", "606: A zone fault condition has been restored."),
    ZoneOpen("609", "Zone Open", "609: General status of the zone - open."),
    ZoneRestored("610", "Zone Restored", "610: General status of the zone - restored."),
    EnvisalinkZoneTimerDump("615", "Envisalink Zone Timer Dump",
            "615: The raw zone timers used inside the Envisalink."),
    DuressAlarm("620", "Duress Alarm", "620: A duress code has been entered on a system keypad."),
    FireKeyAlarm("621", "Fire Key Alarm", "621: A Fire key alarm has been activated."),
    FireKeyRestored("622", "Fire Key Alarm Restore", "622: A Fire key alarm has been restored."),
    AuxiliaryKeyAlarm("623", "Auxiliary Key Alarm", "623: An Auxiliary key alarm has been activated."),
    AuxiliaryKeyRestored("624", "Auxiliary Key Alarm Restore", "624: An Auxiliary key alarm has been restored."),
    PanicKeyAlarm("625", "Panic Key Alarm", "625: A Panic key alarm has been activated."),
    PanicKeyRestored("626", "Panic Key Alarm Restore", "626: A Panic key alarm has been restored."),
    AuxiliaryInputAlarm("631", "2-Wire Smoke/Aux Alarm", "631: A 2-wire smoke/Auxiliary alarm has been activated."),
    AuxiliaryInputAlarmRestored("632", "2-Wire Smoke/Aux Alarm Restore",
            "632: A 2-wire smoke/Auxiliary alarm has been restored."),
    PartitionReady("650", "Partition Ready", "650: Partition can now be armed."),
    PartitionNotReady("651", "Partition Not Ready", "651: Partition can not be armed."),
    PartitionArmed("652", "Partition Armed", "652: Partition has been armed."),
    PartitionReadyForceArming("653", "Partition Ready - Force Arming Enabled",
            "653: Partition can now be armed (Force Arming Enabled)."),
    PartitionInAlarm("654", "Partition In Alarm", "654: A partition is in alarm."),
    PartitionDisarmed("655", "Partition Disarmed", "655: A partition has been disarmed."),
    ExitDelayInProgress("656", "Exit Delay in Progress", "656: A partition is in Exit Delay."),
    EntryDelayInProgress("657", "Entry Delay in Progress", "657: A partition is in Entry Delay."),
    KeypadLockout("658", "Keypad Lock-out", "658: A partition is in Keypad Lockout."),
    PartitionFailedToArm("659", "Partition Failed to Arm", "659: An attempt to arm the partition has failed."),
    PGMOutputInProgress("660", "PGM Output is in Progress", "660: *71, *72, *73, or *74 has been pressed."),
    ChimeEnabled("663", "Chime Enabled", "663: The door chime feature has been enabled."),
    ChimeDisabled("664", "Chime Disabled", "664: The door chime feature has been disabled."),
    InvalidAccessCode("670", "Invalid Access Code", "670: An access code that was entered was invalid."),
    FunctionNotAvailable("671", "Function Not Available", "671: A function that was selected is not available."),
    FailureToArm("672", "Failure to Arm", "672: An attempt was made to arm the partition and it failed."),
    PartitionBusy("673", "Partition is Busy", "673: The partition is busy."),
    SystemArmingInProgress("674", "System Arming in Progress",
            "674: This system is auto-arming and is in arm warning delay."),
    SystemInInstallerMode("680", "System in Installers Mode", "680: The whole system is in installers mode."),

    UserClosing("700", "User Closing", "700: A partition has been armed by a user."),
    SpecialClosing("701", "Special Closing",
            "701: A partition has been armed by one of the following methods: Quick Arm, Auto Arm, Keyswitch, DLS software, Wireless Key."),
    PartialClosing("702", "Partial Closing",
            "702: A partition has been armed but one or more zones have been bypassed."),
    UserOpening("750", "User Opening", "750: A partition has been disarmed by a user."),
    SpecialOpening("751", "Special Opening",
            "751: A partition has been disarmed by one of the following methods: Quick Arm, Auto Arm, Keyswitch, DLS software, Wireless Key."),

    PanelBatteryTrouble("800", "Panel Battery Trouble", "800: The panel has a low battery."),
    PanelBatteryTroubleRestore("801", "Panel Battery Trouble Restore",
            "801: The panel low battery trouble has been restored."),
    PanelACTrouble("802", "Panel AC Trouble", "802: AC power to the panel has been removed."),
    PanelACRestore("803", "Panel AC Restore", "803: AC power to the panel has been restored."),
    SystemBellTrouble("806", "System Bell Trouble",
            "806: An open circuit has been detected across the bell terminals."),
    SystemBellTroubleRestore("807", "System Bell Trouble Restore", "807: The bell trouble has been restored."),
    TLMLine1Trouble("810", "TML Line 1 Trouble", "810: The phone line is an open or shorted condition."),
    TLMLine1TroubleRestore("811", "TML Line 1 Trouble Restore",
            "811: The phone line trouble condition has been restored."),
    TLMLine2Trouble("812", "TML Line 2 Trouble", "812: The phone line is an open or shorted condition."),
    TLMLine2TroubleRestore("813", "TML Line 2 Trouble Restore",
            "813: The phone line trouble condition has been restored."),
    FTCTrouble("814", "FTC Trouble",
            "814: The panel has failed to communicate successfully to the monitoring station."),
    BufferNearFull("816", "Buffer Near Full",
            "816: The panel event buffer is 75% full from when it was last uploaded to DLS."),
    GeneralDeviceLowBattery("821", "General Device Low Battery", "821: A wireless zone has a low battery."),
    GeneralDeviceLowBatteryRestore("822", "General Device Low Battery Restore",
            "822: A wireless zone has a low battery."),
    WirelessKeyLowBatteryTrouble("825", "Wireless Key Low Battery Trouble", "825: A wireless key has a low battery."),
    WirelessKeyLowBatteryTroubleRestore("826", "Wireless Key Low Battery Trouble Restore",
            "826: A wireless key low battery condition has been restored."),
    HandheldKeypadLowBatteryTrouble("827", "Handheld Keypad Low Battery Trouble",
            "827: A handhekd keypad has a low battery."),
    HandheldKeypadLowBatteryTroubleRestore("828", "Handheld Keypad Low Battery Trouble Restore",
            "828: A handhekd keypad low battery condition has been restored."),
    GeneralSystemTamper("829", "General System Tamper", "829: A tamper has occurred with a system module."),
    GeneralSystemTamperRestore("830", "General System Tamper Restore",
            "830: A general system Tamper has been restored."),
    HomeAutomationTrouble("831", "Home Automation Trouble", "831: Escort 5580 module trouble."),
    HomeAutomationTroubleRestore("832", "Home Automation Trouble Restore",
            "832: Escort 5580 module trouble has been restored."),
    TroubleLEDOn("840", "Trouble LED ON", "840: The trouble LED on a keypad is ON."),
    TroubleLEDOff("841", "Trouble LED OFF", "841: The trouble LED on a keypad is OFF."),
    FireTroubleAlarm("842", "Fire Trouble Alarm", "842: Fire trouble alarm."),
    FireTroubleAlarmRestore("843", "Fire Trouble Alarm Restore", "843: Fire trouble alarm restored."),
    VerboseTroubleStatus("849", "Verbose Trouble Status",
            "849: a trouble appears on the system and roughly every 5 minutes until the trouble is cleared."),
    KeybusFault("896", "Keybus Fault", "896: Keybus fault condition."),
    KeybusFaultRestore("897", "Keybus Fault Restore", "897: Keybus fault has been restored."),

    CodeRequired("900", "Code Required", "900: Tells the API to enter an access code."),
    LCDUpdate("901", "LCD Update", "901: Text of the IT-100 menu has changed."),
    LCDCursor("902", "LCD Cursor", "902: Cursor position has changed."),
    LEDStatus("903", "LED Status", "903: LED Status has changed."),
    BeepStatus("904", "Beep Status", "904: Beep status sent."),
    ToneStatus("905", "Tone Status", "905: Tone status sent."),
    BuzzerStatus("906", "Buzzer Status", "906: Buzzer status sent."),
    DoorChimeStatus("907", "Door Chime Status", "907: Door Chime status sent."),
    SoftwareVersion("908", "Software Version", "908: Current software version."),
    CommandOutputPressed("912", "Command Output Pressed", "912: Tells the API to enter an access code."),
    MasterCodeRequired("921", "Master Code Required", "921: Tells the API to enter a master access code."),
    InstallersCodeRequired("922", "Installers Code Required", "922: Tells the API to enter an installers access code."),

    UnknownCode("-1", "Unknown Code", "Unknown code received.");

    private String code;
    private String name;
    private String description;

    /**
     * Lookup map to get a DSCAlarmCode value from its string code.
     */
    private static Map<String, DSCAlarmCode> codeToDSCAlarmCodeValue;

    /**
     * Constructor
     *
     * @param code
     */
    private DSCAlarmCode(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * Initialize the lookup map that gets a DSCAlarmCode value from a string code.
     */
    private static void initMapping() {
        codeToDSCAlarmCodeValue = new HashMap<>();
        for (DSCAlarmCode s : values()) {
            codeToDSCAlarmCodeValue.put(s.code, s);
        }
    }

    /**
     * The DSC Alarm command/message code string (example '005').
     */
    public String getCode() {
        return code;
    }

    /**
     * The DSC Alarm command/message name string (example 'Poll Command').
     */
    public String getName() {
        return name;
    }

    /**
     * The DSC Alarm command/message description string.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Lookup function to return the DSCAlarmCode value based on the string code. Returns 'UnknownCode' if the string
     * code is not found.
     *
     * @param code
     * @return enum value
     */
    public static DSCAlarmCode getDSCAlarmCodeValue(String code) {
        DSCAlarmCode dscAlarmCode;

        if (codeToDSCAlarmCodeValue == null) {
            initMapping();
        }

        dscAlarmCode = codeToDSCAlarmCodeValue.get(code);

        if (dscAlarmCode == null) {
            dscAlarmCode = UnknownCode;
        }

        return dscAlarmCode;
    }
}
