/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.EnumMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that processes DSC Alarm Messages.
 *
 * @author Russell Stephens - Initial Contribution
 */
public class DSCAlarmMessage {

    private final Logger logger = LoggerFactory.getLogger(DSCAlarmMessage.class);

    private static final EnumMap<DSCAlarmCode, MessageParameters> DSCALARM_MESSAGE_PARAMETERS = new EnumMap<>(
            DSCAlarmCode.class);

    public enum DSCAlarmMessageType {
        PANEL_EVENT,
        PARTITION_EVENT,
        ZONE_EVENT,
        KEYPAD_EVENT
    }

    public enum DSCAlarmMessageInfoType {
        MESSAGE,
        NAME,
        DESCRIPTION,
        CODE,
        TIME_STAMP,
        PARTITION,
        ZONE,
        DATA,
        MODE,
        USER,
        ERROR
    }

    private DSCAlarmMessageType messageType = DSCAlarmMessageType.PANEL_EVENT;

    private String message = "";
    private String name = "";
    private String description = "";
    private String codeReceived = "";
    private String timeStamp = "";
    private String partition = "0";
    private String zone = "0";
    private String data = "";
    private String mode = "";
    private String user = "";
    private String error = "";

    /**
     * Constructor.
     *
     * @param message
     *            - the message received
     */
    public DSCAlarmMessage(String message) {
        this.message = message;
        processDSCAlarmMessage();
    }

    /**
     * Processes the incoming DSC Alarm message and extracts the information.
     */
    private void processDSCAlarmMessage() {
        DSCAlarmCode dscAlarmCode;

        if (message.length() > 3) {
            try {
                if (message.length() >= 8 && message.charAt(2) == ':' && message.charAt(5) == ':') {
                    timeStamp = message.substring(0, 8);
                    message = message.substring(9, message.length() - 2);
                } else {
                    message = message.substring(0, message.length() - 2);
                }

                codeReceived = message.substring(0, 3);

                if (message.length() >= 4) {
                    data = message.substring(3);
                }
            } catch (Exception e) {
                logger.error("processDSCAlarmMessage(): Error processing message: ({}) ", message, e);
                return;
            }

            dscAlarmCode = DSCAlarmCode.getDSCAlarmCodeValue(codeReceived);

            if (dscAlarmCode != null) {
                name = dscAlarmCode.getName();
                description = dscAlarmCode.getDescription();

                MessageParameters messageParms = DSCALARM_MESSAGE_PARAMETERS.get(dscAlarmCode);

                if (messageParms != null) {
                    boolean hasPartition = messageParms.hasPartition();
                    boolean hasZone = messageParms.hasZone();

                    if (hasPartition) {
                        partition = message.substring(3, 4);
                    }

                    if (hasZone) {
                        if (hasPartition) {
                            zone = message.substring(4);
                        } else {
                            zone = message.substring(3);
                        }
                    }

                    messageType = messageParms.getType();
                }

                switch (dscAlarmCode) {
                    case SystemError: /* 502 */
                        int systemErrorCode = 0;
                        systemErrorCode = Integer.parseInt(data);
                        switch (systemErrorCode) {
                            case 1:
                                error = "Receive Buffer Overrun";
                                break;
                            case 2:
                                error = "Receive Buffer Overflow";
                                break;
                            case 3:
                                error = "Transmit Buffer Overflow";
                                break;
                            case 10:
                                error = "Keybus Transmit Buffer Overrun";
                                break;
                            case 11:
                                error = "Keybus Transmit Time Timeout";
                                break;
                            case 12:
                                error = "Keybus Transmit Mode Timeout";
                                break;
                            case 13:
                                error = "Keybus Transmit Keystring Timeout";
                                break;
                            case 14:
                                error = "Keybus Interface Not Functioning";
                                break;
                            case 15:
                                error = "Keybus Busy - Attempting to Disarm or Arm with user code";
                                break;
                            case 16:
                                error = "Keybus Busy – Lockout";
                                break;
                            case 17:
                                error = "Keybus Busy – Installers Mode";
                                break;
                            case 18:
                                error = "Keybus Busy - General Busy";
                                break;
                            case 20:
                                error = "API Command Syntax Error";
                                break;
                            case 21:
                                error = "API Command Partition Error - Requested Partition is out of bounds";
                                break;
                            case 22:
                                error = "API Command Not Supported";
                                break;
                            case 23:
                                error = "API System Not Armed - Sent in response to a disarm command";
                                break;
                            case 24:
                                error = "API System Not Ready to Arm - System is either not-secure, in exit-delay, or already armed";
                                break;
                            case 25:
                                error = "API Command Invalid Length";
                                break;
                            case 26:
                                error = "API User Code not Required";
                                break;
                            case 27:
                                error = "API Invalid Characters in Command - No alpha characters are allowed except for checksum";
                                break;
                            case 28:
                                error = "API Virtual Keypad is Disabled";
                                break;
                            case 29:
                                error = "API Not Valid Parameter";
                                break;
                            case 30:
                                error = "API Keypad Does Not Come Out of Blank Mode";
                                break;
                            case 31:
                                error = "API IT-100 is Already in Thermostat Menu";
                                break;
                            case 32:
                                error = "API IT-100 is NOT in Thermostat Menu";
                                break;
                            case 33:
                                error = "API No Response From Thermostat or Escort Module";
                                break;
                            case 0:
                            default:
                                error = "No Error";
                                break;
                        }
                        break;

                    case PartitionArmed: /* 652 */
                        mode = message.substring(4);
                        if (mode.equals("0")) {
                            name += " (Away)";
                        } else if (mode.equals("1")) {
                            name += " (Stay)";
                        } else if (mode.equals("2")) {
                            name += " (ZEA)";
                        } else if (mode.equals("3")) {
                            name += " (ZES)";
                        }
                        messageType = DSCAlarmMessageType.PARTITION_EVENT;
                        break;
                    case UserClosing: /* 700 */
                        user = message.substring(4);
                        name = name.concat(": " + user);
                        description = codeReceived + ": Partition " + String.valueOf(partition)
                                + " has been armed by user " + user + ".";
                        messageType = DSCAlarmMessageType.PARTITION_EVENT;
                        break;
                    case UserOpening: /* 750 */
                        user = message.substring(4);
                        name = name.concat(": " + user);
                        description = codeReceived + ": Partition " + String.valueOf(partition)
                                + " has been disarmed by user " + user + ".";
                        messageType = DSCAlarmMessageType.PARTITION_EVENT;
                        break;

                    default:
                        break;
                }

                logger.debug(
                        "parseAPIMessage(): Message Received ({}) - Code: {}, Name: {}, Description: {}, Data: {}\r\n",
                        message, codeReceived, name, description, data);
            }
        } else {
            codeReceived = "-1";
            data = "";
            dscAlarmCode = DSCAlarmCode.getDSCAlarmCodeValue(codeReceived);
            name = dscAlarmCode.getName();
            description = dscAlarmCode.getDescription();
            logger.debug("parseAPIMessage(): Invalid Message Received");
        }
    }

    /**
     * Returns the DSCAlarm Message Type.
     *
     * @return messageType
     */
    public DSCAlarmMessageType getDSCAlarmMessageType() {
        return messageType;
    }

    /**
     * Returns Information from A DSC Alarm Message
     *
     * @param dscAlarmMessageInfoType
     * @return String
     */
    public String getMessageInfo(DSCAlarmMessageInfoType dscAlarmMessageInfoType) {
        String info = "";

        switch (dscAlarmMessageInfoType) {
            case MESSAGE:
                info = message;
                break;
            case NAME:
                info = name;
                break;
            case DESCRIPTION:
                info = description;
                break;
            case CODE:
                info = codeReceived;
                break;
            case TIME_STAMP:
                info = timeStamp;
                break;
            case PARTITION:
                info = partition;
                break;
            case ZONE:
                info = zone;
                break;
            case DATA:
                info = data;
                break;
            case MODE:
                info = mode;
                break;
            case USER:
                info = timeStamp;
                break;
            case ERROR:
                info = error;
                break;
            default:
                break;
        }

        return info;
    }

    /**
     * Returns a string representation of a APIMessage.
     *
     * @return APIMessage string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Code: \"");
        sb.append(codeReceived);
        sb.append("\"");

        sb.append(", Name: \"");
        sb.append(name);
        sb.append("\"");

        sb.append(", Description: \"");
        sb.append(description);
        sb.append("\"");

        if (!timeStamp.equals("")) {
            sb.append(", Time Stamp: ");
            sb.append(timeStamp);
        }

        if (!partition.equals("0")) {
            sb.append(", Partition: ");
            sb.append(partition);
        }

        if (!zone.equals("0")) {
            sb.append(", Zone: ");
            sb.append(zone);
        }

        if (!data.equals("")) {
            sb.append(", Data: ");
            sb.append(data);
        }

        if (!mode.equals("")) {
            sb.append(", Mode: ");
            sb.append(mode);
        }

        if (!user.equals("")) {
            sb.append(", user: ");
            sb.append(user);
        }

        if (!error.equals("")) {
            sb.append(", error: ");
            sb.append(error);
        }

        return sb.toString();
    }

    public static class MessageParameters {
        private boolean partition;
        private boolean zone;
        private DSCAlarmMessageType type;

        MessageParameters(DSCAlarmMessageType type, boolean partition, boolean zone) {
            this.type = type;
            this.partition = partition;
            this.zone = zone;
        }

        public DSCAlarmMessageType getType() {
            return type;
        }

        public boolean hasPartition() {
            return partition;
        }

        public boolean hasZone() {
            return zone;
        }
    }

    static {
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.CommandAcknowledge,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.CommandError,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.SystemError,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.LoginResponse,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.KeypadLEDState,
                new MessageParameters(DSCAlarmMessageType.KEYPAD_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.KeypadLEDFlashState,
                new MessageParameters(DSCAlarmMessageType.KEYPAD_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.TimeDateBroadcast,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.RingDetected,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.IndoorTemperatureBroadcast,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.OutdoorTemperatureBroadcast,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.ThermostatSetPoints,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.BroadcastLabels,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.BaudRateSet,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));

        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.ZoneAlarm,
                new MessageParameters(DSCAlarmMessageType.ZONE_EVENT, true, true));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.ZoneAlarmRestore,
                new MessageParameters(DSCAlarmMessageType.ZONE_EVENT, true, true));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.ZoneTamper,
                new MessageParameters(DSCAlarmMessageType.ZONE_EVENT, true, true));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.ZoneTamperRestore,
                new MessageParameters(DSCAlarmMessageType.ZONE_EVENT, true, true));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.ZoneFault,
                new MessageParameters(DSCAlarmMessageType.ZONE_EVENT, false, true));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.ZoneFaultRestore,
                new MessageParameters(DSCAlarmMessageType.ZONE_EVENT, false, true));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.ZoneOpen,
                new MessageParameters(DSCAlarmMessageType.ZONE_EVENT, false, true));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.ZoneRestored,
                new MessageParameters(DSCAlarmMessageType.ZONE_EVENT, false, true));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.EnvisalinkZoneTimerDump,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.DuressAlarm,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.FireKeyAlarm,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.FireKeyRestored,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.AuxiliaryKeyAlarm,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.AuxiliaryKeyRestored,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.PanicKeyAlarm,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.PanicKeyRestored,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.AuxiliaryInputAlarm,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.AuxiliaryInputAlarmRestored,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.PartitionReady,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.PartitionNotReady,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.PartitionArmed,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.PartitionReadyForceArming,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.PartitionInAlarm,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.PartitionDisarmed,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.ExitDelayInProgress,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.EntryDelayInProgress,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.KeypadLockout,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.PartitionFailedToArm,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.PGMOutputInProgress,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.ChimeEnabled,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.ChimeDisabled,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.InvalidAccessCode,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.FunctionNotAvailable,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.FailureToArm,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.PartitionBusy,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.SystemArmingInProgress,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.SystemInInstallerMode,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));

        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.UserClosing,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.SpecialClosing,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.PartialClosing,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.UserOpening,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.SpecialOpening,
                new MessageParameters(DSCAlarmMessageType.PARTITION_EVENT, true, false));

        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.PanelBatteryTrouble,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.PanelBatteryTroubleRestore,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.PanelACTrouble,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.PanelACRestore,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.SystemBellTrouble,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.SystemBellTroubleRestore,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.TLMLine1Trouble,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.TLMLine1TroubleRestore,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.TLMLine2Trouble,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.TLMLine2TroubleRestore,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.FTCTrouble,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.BufferNearFull,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.GeneralDeviceLowBattery,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, true));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.GeneralDeviceLowBatteryRestore,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, true));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.WirelessKeyLowBatteryTrouble,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, true));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.WirelessKeyLowBatteryTroubleRestore,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, true));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.HandheldKeypadLowBatteryTrouble,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, true));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.HandheldKeypadLowBatteryTroubleRestore,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, true));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.GeneralSystemTamper,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.GeneralSystemTamperRestore,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.HomeAutomationTrouble,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.HomeAutomationTroubleRestore,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.TroubleLEDOn,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.TroubleLEDOff,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, true, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.FireTroubleAlarm,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.FireTroubleAlarmRestore,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.VerboseTroubleStatus,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.KeybusFault,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.KeybusFaultRestore,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));

        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.CodeRequired,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.LCDUpdate,
                new MessageParameters(DSCAlarmMessageType.KEYPAD_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.LCDCursor,
                new MessageParameters(DSCAlarmMessageType.KEYPAD_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.LEDStatus,
                new MessageParameters(DSCAlarmMessageType.KEYPAD_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.BeepStatus,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.ToneStatus,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.BuzzerStatus,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.DoorChimeStatus,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.SoftwareVersion,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.CommandOutputPressed,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.MasterCodeRequired,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
        DSCALARM_MESSAGE_PARAMETERS.put(DSCAlarmCode.InstallersCodeRequired,
                new MessageParameters(DSCAlarmMessageType.PANEL_EVENT, false, false));
    }
}
