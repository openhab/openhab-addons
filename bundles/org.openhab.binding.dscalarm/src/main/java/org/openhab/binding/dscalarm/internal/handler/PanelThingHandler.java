/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dscalarm.internal.handler;

import static org.openhab.binding.dscalarm.internal.DSCAlarmBindingConstants.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.List;

import org.openhab.binding.dscalarm.internal.DSCAlarmCode;
import org.openhab.binding.dscalarm.internal.DSCAlarmEvent;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage.DSCAlarmMessageInfoType;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class for handling a Panel type Thing.
 *
 * @author Russell Stephens - Initial Contribution
 */
public class PanelThingHandler extends DSCAlarmBaseThingHandler {

    private static final int PANEL_COMMAND_POLL = 0;
    private static final int PANEL_COMMAND_STATUS_REPORT = 1;
    private static final int PANEL_COMMAND_LABELS_REQUEST = 2;
    private static final int PANEL_COMMAND_DUMP_ZONE_TIMERS = 8;
    private static final int PANEL_COMMAND_SET_TIME_DATE = 10;
    private static final int PANEL_COMMAND_CODE_SEND = 200;

    private final Logger logger = LoggerFactory.getLogger(PanelThingHandler.class);

    /**
     * Constructor.
     *
     * @param thing
     */
    public PanelThingHandler(Thing thing) {
        super(thing);
        setDSCAlarmThingType(DSCAlarmThingType.PANEL);
    }

    @Override
    public void updateChannel(ChannelUID channelUID, int state, String description) {
        logger.debug("updateChannel(): Panel Channel UID: {}", channelUID);

        boolean trigger;
        boolean trouble;
        boolean boolState;
        OnOffType onOffType;

        if (channelUID != null) {
            switch (channelUID.getId()) {
                case PANEL_MESSAGE:
                    updateState(channelUID, new StringType(description));
                    break;
                case PANEL_SYSTEM_ERROR:
                    updateState(channelUID, new StringType(description));
                    break;
                case PANEL_TIME:
                    Date date = null;
                    SimpleDateFormat sdfReceived = new SimpleDateFormat("hhmmMMddyy");

                    try {
                        date = sdfReceived.parse(description);
                    } catch (ParseException e) {
                        logger.warn("updateChannel(): Parse Exception occurred while trying to parse date string: {}. ",
                                e.getMessage());
                    }

                    if (date != null) {
                        SimpleDateFormat sdfUpdate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        String systemTime = sdfUpdate.format(date);
                        updateState(channelUID, new DateTimeType(systemTime));
                    }

                    break;
                case PANEL_TIME_STAMP:
                    boolState = state != 0;
                    onOffType = OnOffType.from(boolState);
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_TIME_BROADCAST:
                    boolState = state != 0;
                    onOffType = OnOffType.from(boolState);
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_COMMAND:
                    updateState(channelUID, new DecimalType(state));
                    break;
                case PANEL_TROUBLE_MESSAGE:
                    updateState(channelUID, new StringType(description));
                    break;
                case PANEL_TROUBLE_LED:
                    boolState = state != 0;
                    onOffType = OnOffType.from(boolState);
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_SERVICE_REQUIRED:
                    trouble = state != 0;
                    onOffType = OnOffType.from(trouble);
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_AC_TROUBLE:
                    trouble = state != 0;
                    onOffType = OnOffType.from(trouble);
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_TELEPHONE_TROUBLE:
                    trouble = state != 0;
                    onOffType = OnOffType.from(trouble);
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_FTC_TROUBLE:
                    trouble = state != 0;
                    onOffType = OnOffType.from(trouble);
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_ZONE_FAULT:
                    trouble = state != 0;
                    onOffType = OnOffType.from(trouble);
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_ZONE_TAMPER:
                    trouble = state != 0;
                    onOffType = OnOffType.from(trouble);
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_ZONE_LOW_BATTERY:
                    trouble = state != 0;
                    onOffType = OnOffType.from(trouble);
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_TIME_LOSS:
                    trouble = state != 0;
                    onOffType = OnOffType.from(trouble);
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_FIRE_KEY_ALARM:
                    trigger = state != 0;
                    onOffType = OnOffType.from(trigger);
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_PANIC_KEY_ALARM:
                    trigger = state != 0;
                    onOffType = OnOffType.from(trigger);
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_AUX_KEY_ALARM:
                    trigger = state != 0;
                    onOffType = OnOffType.from(trigger);
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_AUX_INPUT_ALARM:
                    trigger = state != 0;
                    onOffType = OnOffType.from(trigger);
                    updateState(channelUID, onOffType);
                    break;
                default:
                    logger.debug("updateChannel(): Panel Channel not updated - {}.", channelUID);
                    break;
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand(): Command Received - {} {}.", channelUID, command);

        if (command instanceof RefreshType) {
            return;
        }

        if (dscAlarmBridgeHandler != null && dscAlarmBridgeHandler.isConnected()) {
            int cmd;

            switch (channelUID.getId()) {
                case PANEL_COMMAND:
                    cmd = Integer.parseInt(command.toString());
                    handlePanelCommand(cmd);
                    updateState(channelUID, new StringType(String.valueOf(-1)));
                    break;
                case PANEL_TIME_STAMP:
                    if (command instanceof OnOffType onOffCommand) {
                        cmd = command == OnOffType.ON ? 1 : 0;
                        dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.TimeStampControl, String.valueOf(cmd));
                        updateState(channelUID, onOffCommand);
                    }
                    break;
                case PANEL_TIME_BROADCAST:
                    if (command instanceof OnOffType onOffCommand) {
                        cmd = command == OnOffType.ON ? 1 : 0;
                        dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.TimeDateBroadcastControl, String.valueOf(cmd));
                        updateState(channelUID, onOffCommand);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Method to handle PANEL_COMMAND
     *
     * @param cmd
     */
    private void handlePanelCommand(int cmd) {
        switch (cmd) {
            case PANEL_COMMAND_POLL:
                dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.Poll);
                break;
            case PANEL_COMMAND_STATUS_REPORT:
                dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.StatusReport);
                break;
            case PANEL_COMMAND_LABELS_REQUEST:
                dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.LabelsRequest);
                break;
            case PANEL_COMMAND_DUMP_ZONE_TIMERS:
                dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.DumpZoneTimers);
                break;
            case PANEL_COMMAND_SET_TIME_DATE:
                dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.SetTimeDate);
                break;
            case PANEL_COMMAND_CODE_SEND:
                dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.CodeSend, getUserCode());
                break;
            default:
                break;
        }
    }

    /**
     * Method to set the time stamp state.
     *
     * @param timeStamp
     */
    private void setTimeStampState(String timeStamp) {
        if (timeStamp != null) {
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), PANEL_TIME_STAMP);
            int state = timeStamp.isEmpty() ? 0 : 1;
            updateChannel(channelUID, state, "");
        }
    }

    /**
     * Method to set Channel PANEL_SYSTEM_ERROR.
     *
     * @param properties
     * @param dscAlarmMessage
     */
    private void panelSystemError(DSCAlarmMessage dscAlarmMessage) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), PANEL_SYSTEM_ERROR);
        int systemErrorCode = 0;
        String systemErrorDescription = "";

        if (dscAlarmMessage != null) {
            systemErrorCode = Integer.parseInt(dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DATA));
            switch (systemErrorCode) {
                case 1:
                    systemErrorDescription = "Receive Buffer Overrun";
                    break;
                case 2:
                    systemErrorDescription = "Receive Buffer Overflow";
                    break;
                case 3:
                    systemErrorDescription = "Transmit Buffer Overflow";
                    break;
                case 10:
                    systemErrorDescription = "Keybus Transmit Buffer Overrun";
                    break;
                case 11:
                    systemErrorDescription = "Keybus Transmit Time Timeout";
                    break;
                case 12:
                    systemErrorDescription = "Keybus Transmit Mode Timeout";
                    break;
                case 13:
                    systemErrorDescription = "Keybus Transmit Keystring Timeout";
                    break;
                case 14:
                    systemErrorDescription = "Keybus Interface Not Functioning";
                    break;
                case 15:
                    systemErrorDescription = "Keybus Busy - Attempting to Disarm or Arm with user code";
                    break;
                case 16:
                    systemErrorDescription = "Keybus Busy – Lockout";
                    break;
                case 17:
                    systemErrorDescription = "Keybus Busy – Installers Mode";
                    break;
                case 18:
                    systemErrorDescription = "Keybus Busy - General Busy";
                    break;
                case 20:
                    systemErrorDescription = "API Command Syntax Error";
                    break;
                case 21:
                    systemErrorDescription = "API Command Partition Error - Requested Partition is out of bounds";
                    break;
                case 22:
                    systemErrorDescription = "API Command Not Supported";
                    break;
                case 23:
                    systemErrorDescription = "API System Not Armed - Sent in response to a disarm command";
                    break;
                case 24:
                    systemErrorDescription = "API System Not Ready to Arm - System is either not-secure, in exit-delay, or already armed";
                    break;
                case 25:
                    systemErrorDescription = "API Command Invalid Length";
                    break;
                case 26:
                    systemErrorDescription = "API User Code not Required";
                    break;
                case 27:
                    systemErrorDescription = "API Invalid Characters in Command - No alpha characters are allowed except for checksum";
                    break;
                case 28:
                    systemErrorDescription = "API Virtual Keypad is Disabled";
                    break;
                case 29:
                    systemErrorDescription = "API Not Valid Parameter";
                    break;
                case 30:
                    systemErrorDescription = "API Keypad Does Not Come Out of Blank Mode";
                    break;
                case 31:
                    systemErrorDescription = "API IT-100 is Already in Thermostat Menu";
                    break;
                case 32:
                    systemErrorDescription = "API IT-100 is NOT in Thermostat Menu";
                    break;
                case 33:
                    systemErrorDescription = "API No Response From Thermostat or Escort Module";
                    break;
                case 0:
                default:
                    systemErrorDescription = "No Error";
                    break;
            }

        }

        String errorMessage = String.format("%03d", systemErrorCode) + ": " + systemErrorDescription;
        channelUID = new ChannelUID(getThing().getUID(), PANEL_SYSTEM_ERROR);
        updateState(channelUID, new StringType(errorMessage));
    }

    /**
     * Handle Verbose Trouble Status events for the EyezOn Envisalink 3/2DS DSC Alarm Interface.
     *
     * @param event
     */
    private void verboseTroubleStatusHandler(EventObject event) {
        DSCAlarmEvent dscAlarmEvent = (DSCAlarmEvent) event;
        DSCAlarmMessage dscAlarmMessage = dscAlarmEvent.getDSCAlarmMessage();
        String[] channelTypes = { PANEL_SERVICE_REQUIRED, PANEL_AC_TROUBLE, PANEL_TELEPHONE_TROUBLE, PANEL_FTC_TROUBLE,
                PANEL_ZONE_FAULT, PANEL_ZONE_TAMPER, PANEL_ZONE_LOW_BATTERY, PANEL_TIME_LOSS };

        ChannelUID channelUID = null;

        int bitCount = 8;
        int bitField = Integer.decode("0x" + dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DATA));
        int[] masks = { 1, 2, 4, 8, 16, 32, 64, 128 };
        int[] bits = new int[bitCount];

        for (int i = 0; i < bitCount; i++) {
            channelUID = new ChannelUID(getThing().getUID(), channelTypes[i]);
            bits[i] = bitField & masks[i];
            updateChannel(channelUID, bits[i] != 0 ? 1 : 0, "");
        }
    }

    /**
     * Restores all partitions that are in alarm after special panel alarm conditions have been restored.
     *
     * @param dscAlarmCode
     */
    private void restorePartitionsInAlarm(DSCAlarmCode dscAlarmCode) {
        logger.debug("restorePartitionsInAlarm(): DSC Alarm Code: {}!", dscAlarmCode.toString());

        ChannelUID channelUID = null;

        if (dscAlarmCode == DSCAlarmCode.FireKeyRestored || dscAlarmCode == DSCAlarmCode.AuxiliaryKeyRestored
                || dscAlarmCode == DSCAlarmCode.PanicKeyRestored
                || dscAlarmCode == DSCAlarmCode.AuxiliaryInputAlarmRestored) {
            List<Thing> things = dscAlarmBridgeHandler.getThing().getThings();
            for (Thing thg : things) {
                if (thg.getThingTypeUID().equals(PARTITION_THING_TYPE)) {
                    DSCAlarmBaseThingHandler handler = (DSCAlarmBaseThingHandler) thg.getHandler();
                    if (handler != null) {
                        channelUID = new ChannelUID(thg.getUID(), PARTITION_IN_ALARM);
                        handler.updateChannel(channelUID, 0, "");

                        logger.debug("restorePartitionsInAlarm(): Partition In Alarm Restored: {}!", thg.getUID());
                    }
                }
            }
        }
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void dscAlarmEventReceived(EventObject event, Thing thing) {
        if (thing != null) {
            DSCAlarmEvent dscAlarmEvent = (DSCAlarmEvent) event;
            DSCAlarmMessage dscAlarmMessage = dscAlarmEvent.getDSCAlarmMessage();
            String dscAlarmMessageData = dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DATA);
            setTimeStampState(dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.TIME_STAMP));

            if (getThing() == thing) {
                ChannelUID channelUID = null;
                DSCAlarmCode dscAlarmCode = DSCAlarmCode
                        .getDSCAlarmCodeValue(dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.CODE));
                logger.debug("dscAlarmEventRecieved(): Thing - {}   Command - {}", thing.getUID(), dscAlarmCode);

                int state = 0;

                switch (dscAlarmCode) {
                    case CommandAcknowledge: /* 500 */
                        break;
                    case SystemError: /* 502 */
                        int errorCode = Integer.parseInt(dscAlarmMessageData);

                        if (errorCode == 23 || errorCode == 24) {
                            List<Thing> things = dscAlarmBridgeHandler.getThing().getThings();
                            for (Thing thg : things) {
                                if (thg.getThingTypeUID().equals(PARTITION_THING_TYPE)) {
                                    DSCAlarmBaseThingHandler handler = (DSCAlarmBaseThingHandler) thg.getHandler();
                                    if (handler != null) {
                                        channelUID = new ChannelUID(thg.getUID(), PARTITION_ARM_MODE);
                                        handler.updateChannel(channelUID, 0, "");
                                    }
                                }
                            }
                        }

                        panelSystemError(dscAlarmMessage);
                        break;
                    case TimeDateBroadcast: /* 550 */
                        channelUID = new ChannelUID(getThing().getUID(), PANEL_TIME);
                        String panelTime = dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DATA);
                        updateChannel(channelUID, state, panelTime);

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_TIME_BROADCAST);
                        updateChannel(channelUID, 1, "");
                        break;
                    case FireKeyAlarm: /* 621 */
                        state = 1;
                    case FireKeyRestored: /* 622 */
                        channelUID = new ChannelUID(getThing().getUID(), PANEL_FIRE_KEY_ALARM);
                        updateChannel(channelUID, state, "");
                        restorePartitionsInAlarm(dscAlarmCode);
                        break;
                    case AuxiliaryKeyAlarm: /* 623 */
                        state = 1;
                    case AuxiliaryKeyRestored: /* 624 */
                        channelUID = new ChannelUID(getThing().getUID(), PANEL_AUX_KEY_ALARM);
                        updateChannel(channelUID, state, "");
                        restorePartitionsInAlarm(dscAlarmCode);
                        break;
                    case PanicKeyAlarm: /* 625 */
                        state = 1;
                    case PanicKeyRestored: /* 626 */
                        channelUID = new ChannelUID(getThing().getUID(), PANEL_PANIC_KEY_ALARM);
                        updateChannel(channelUID, state, "");
                        restorePartitionsInAlarm(dscAlarmCode);
                        break;
                    case AuxiliaryInputAlarm: /* 631 */
                        state = 1;
                    case AuxiliaryInputAlarmRestored: /* 632 */
                        channelUID = new ChannelUID(getThing().getUID(), PANEL_AUX_INPUT_ALARM);
                        updateChannel(channelUID, state, "");
                        restorePartitionsInAlarm(dscAlarmCode);
                        break;
                    case TroubleLEDOn: /* 840 */
                        channelUID = new ChannelUID(getThing().getUID(), PANEL_TROUBLE_LED);
                        updateChannel(channelUID, 1, "");
                        break;
                    case TroubleLEDOff: /* 841 */
                        channelUID = new ChannelUID(getThing().getUID(), PANEL_SERVICE_REQUIRED);
                        updateChannel(channelUID, 0, "");

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_AC_TROUBLE);
                        updateChannel(channelUID, 0, "");

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_TELEPHONE_TROUBLE);
                        updateChannel(channelUID, 0, "");

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_FTC_TROUBLE);
                        updateChannel(channelUID, 0, "");

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_ZONE_FAULT);
                        updateChannel(channelUID, 0, "");

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_ZONE_TAMPER);
                        updateChannel(channelUID, 0, "");

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_ZONE_LOW_BATTERY);
                        updateChannel(channelUID, 0, "");

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_TIME_LOSS);
                        updateChannel(channelUID, 0, "");

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_TROUBLE_LED);
                        updateChannel(channelUID, 0, "");
                        break;
                    case PanelBatteryTrouble: /* 800 */
                    case PanelACTrouble: /* 802 */
                    case SystemBellTrouble: /* 806 */
                    case TLMLine1Trouble: /* 810 */
                    case TLMLine2Trouble: /* 812 */
                    case FTCTrouble: /* 814 */
                    case GeneralDeviceLowBattery: /* 821 */
                    case WirelessKeyLowBatteryTrouble: /* 825 */
                    case HandheldKeypadLowBatteryTrouble: /* 827 */
                    case GeneralSystemTamper: /* 829 */
                    case HomeAutomationTrouble: /* 831 */
                    case KeybusFault: /* 896 */
                        channelUID = new ChannelUID(getThing().getUID(), PANEL_TROUBLE_MESSAGE);
                        updateChannel(channelUID, 0,
                                dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DESCRIPTION));
                        break;
                    case PanelBatteryTroubleRestore: /* 801 */
                    case PanelACRestore: /* 803 */
                    case SystemBellTroubleRestore: /* 807 */
                    case TLMLine1TroubleRestore: /* 811 */
                    case TLMLine2TroubleRestore: /* 813 */
                    case GeneralDeviceLowBatteryRestore: /* 822 */
                    case WirelessKeyLowBatteryTroubleRestore: /* 826 */
                    case HandheldKeypadLowBatteryTroubleRestore: /* 828 */
                    case GeneralSystemTamperRestore: /* 830 */
                    case HomeAutomationTroubleRestore: /* 832 */
                    case KeybusFaultRestore: /* 897 */
                        channelUID = new ChannelUID(getThing().getUID(), PANEL_TROUBLE_MESSAGE);
                        updateChannel(channelUID, 0, "");
                        break;
                    case VerboseTroubleStatus: /* 849 */
                        verboseTroubleStatusHandler(event);
                        break;
                    case CodeRequired: /* 900 */
                        dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.CodeSend, getUserCode());
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
