/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dscalarm.handler;

import static org.openhab.binding.dscalarm.DSCAlarmBindingConstants.*;

import java.util.EventObject;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.dscalarm.internal.DSCAlarmCode;
import org.openhab.binding.dscalarm.internal.DSCAlarmEvent;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage.DSCAlarmMessageInfoType;
import org.openhab.binding.dscalarm.internal.DSCAlarmProperties.TriggerType;
import org.openhab.binding.dscalarm.internal.DSCAlarmProperties.TroubleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class for handling a Panel type Thing.
 *
 * @author Russell Stephens - Initial Contribution
 */
public class PanelThingHandler extends DSCAlarmBaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(PanelThingHandler.class);

    /**
     * Constructor.
     *
     * @param thing
     */
    public PanelThingHandler(Thing thing) {
        super(thing);
        setDSCAlarmThingType(DSCAlarmThingType.PANEL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateChannel(ChannelUID channelUID) {
        logger.debug("updateChannel(): Panel Channel UID: {}", channelUID);

        int state;
        String str = "";
        boolean trigger;
        boolean trouble;
        boolean boolState;
        OnOffType onOffType;

        if (channelUID != null) {
            switch (channelUID.getId()) {
                case PANEL_MESSAGE:
                    str = properties.getSystemMessage();
                    updateState(channelUID, new StringType(str));
                    break;
                case PANEL_SYSTEM_ERROR:
                    str = String.format("%03d", properties.getSystemErrorCode()) + ": " + properties.getSystemErrorDescription();
                    updateState(channelUID, new StringType(str));
                    break;
                case PANEL_TIME:
                    str = properties.getSystemTime();
                    updateState(channelUID, new DateTimeType(str));
                    break;
                case PANEL_TIME_STAMP:
                    boolState = properties.getSystemTimeStamp();
                    onOffType = boolState ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_TIME_BROADCAST:
                    boolState = properties.getSystemTimeBroadcast();
                    onOffType = boolState ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_COMMAND:
                    state = properties.getSystemCommand();
                    str = String.valueOf(state);
                    updateState(channelUID, new StringType(str));
                    break;
                case PANEL_TROUBLE_MESSAGE:
                    str = properties.getTroubleMessage();
                    updateState(channelUID, new StringType(str));
                    break;
                case PANEL_TROUBLE_LED:
                    boolState = properties.getTroubleLED();
                    onOffType = boolState ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_SERVICE_REQUIRED:
                    trouble = properties.getTrouble(TroubleType.SERVICE_REQUIRED);
                    onOffType = trouble ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_AC_TROUBLE:
                    trouble = properties.getTrouble(TroubleType.AC_TROUBLE);
                    onOffType = trouble ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_TELEPHONE_TROUBLE:
                    trouble = properties.getTrouble(TroubleType.TELEPHONE_LINE_TROUBLE);
                    onOffType = trouble ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_FTC_TROUBLE:
                    trouble = properties.getTrouble(TroubleType.FAILURE_TO_COMMUNICATE);
                    onOffType = trouble ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_ZONE_FAULT:
                    trouble = properties.getTrouble(TroubleType.ZONE_FAULT);
                    onOffType = trouble ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_ZONE_TAMPER:
                    trouble = properties.getTrouble(TroubleType.ZONE_TAMPER);
                    onOffType = trouble ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_ZONE_LOW_BATTERY:
                    trouble = properties.getTrouble(TroubleType.ZONE_LOW_BATTERY);
                    onOffType = trouble ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_TIME_LOSS:
                    trouble = properties.getTrouble(TroubleType.LOSS_OF_TIME);
                    onOffType = trouble ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_FIRE_KEY_ALARM:
                    trigger = properties.getTrigger(TriggerType.FIRE_KEY_ALARM);
                    onOffType = trigger ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_PANIC_KEY_ALARM:
                    trigger = properties.getTrigger(TriggerType.PANIC_KEY_ALARM);
                    onOffType = trigger ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_AUX_KEY_ALARM:
                    trigger = properties.getTrigger(TriggerType.AUX_KEY_ALARM);
                    onOffType = trigger ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PANEL_AUX_INPUT_ALARM:
                    trigger = properties.getTrigger(TriggerType.AUX_KEY_ALARM);
                    onOffType = trigger ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                default:
                    logger.debug("updateChannel(): Panel Channel not updated - {}.", channelUID);
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateProperties(ChannelUID channelUID, int state, String description) {
        logger.debug("updateProperties(): Panel Channel UID: {}", channelUID);

        boolean trouble = state != 0 ? true : false;
        boolean trigger = state != 0 ? true : false;
        boolean boolState = state != 0 ? true : false;

        if (channelUID != null) {
            switch (channelUID.getId()) {
                case PANEL_MESSAGE:
                    properties.setSystemMessage(description);
                    break;
                case PANEL_TIME:
                    properties.setSystemTime(description);
                    break;
                case PANEL_TIME_STAMP:
                    properties.setSystemTimeStamp(state == 1 ? true : false);
                    break;
                case PANEL_TIME_BROADCAST:
                    properties.setSystemTimeBroadcast(state == 1 ? true : false);
                    break;
                case PANEL_COMMAND:
                    properties.setSystemCommand(state);
                    break;
                case PANEL_TROUBLE_MESSAGE:
                    properties.setTroubleMessage(description);
                    break;
                case PANEL_TROUBLE_LED:
                    properties.setTroubleLED(boolState);
                    break;
                case PANEL_SERVICE_REQUIRED:
                    properties.setTrouble(TroubleType.SERVICE_REQUIRED, trouble);
                    break;
                case PANEL_AC_TROUBLE:
                    properties.setTrouble(TroubleType.AC_TROUBLE, trouble);
                    break;
                case PANEL_TELEPHONE_TROUBLE:
                    properties.setTrouble(TroubleType.TELEPHONE_LINE_TROUBLE, trouble);
                    break;
                case PANEL_FTC_TROUBLE:
                    properties.setTrouble(TroubleType.FAILURE_TO_COMMUNICATE, trouble);
                    break;
                case PANEL_ZONE_FAULT:
                    properties.setTrouble(TroubleType.ZONE_FAULT, trouble);
                    break;
                case PANEL_ZONE_TAMPER:
                    properties.setTrouble(TroubleType.ZONE_TAMPER, trouble);
                    break;
                case PANEL_ZONE_LOW_BATTERY:
                    properties.setTrouble(TroubleType.ZONE_LOW_BATTERY, trouble);
                    break;
                case PANEL_TIME_LOSS:
                    properties.setTrouble(TroubleType.LOSS_OF_TIME, trouble);
                    break;
                case PANEL_FIRE_KEY_ALARM:
                    properties.setTrigger(TriggerType.FIRE_KEY_ALARM, trigger);
                    break;
                case PANEL_PANIC_KEY_ALARM:
                    properties.setTrigger(TriggerType.PANIC_KEY_ALARM, trigger);
                    break;
                case PANEL_AUX_KEY_ALARM:
                    properties.setTrigger(TriggerType.AUX_KEY_ALARM, trigger);
                    break;
                case PANEL_AUX_INPUT_ALARM:
                    properties.setTrigger(TriggerType.AUX_INPUT_ALARM, trigger);
                    break;
                default:
                    logger.debug("updateProperties(): Panel property not updated.");
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (dscAlarmBridgeHandler == null) {
            logger.warn("DSC Alarm bridge handler not available. Cannot handle command without bridge.");
            return;
        }

        int cmd;

        boolean connected = dscAlarmBridgeHandler.isConnected();

        if (connected) {
            switch (channelUID.getId()) {
                case PANEL_COMMAND:
                    cmd = Integer.parseInt(command.toString());
                    switch (cmd) {
                        case 0:
                            dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.Poll);
                            break;
                        case 1:
                            dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.StatusReport);
                            break;
                        case 2:
                            dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.LabelsRequest);
                            break;
                        case 8:
                            dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.DumpZoneTimers);
                            break;
                        case 10:
                            dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.SetTimeDate);
                            break;
                        case 200:
                            dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.CodeSend, getUserCode());
                            break;
                        default:
                            break;
                    }

                    updateState(channelUID, new StringType(String.valueOf(-1)));

                    break;
                case PANEL_TIME_STAMP:
                    if (command instanceof OnOffType) {
                        cmd = command == OnOffType.ON ? 1 : 0;
                        dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.TimeStampControl, String.valueOf(cmd));
                        updateProperties(channelUID, cmd, "");
                        updateState(channelUID, (OnOffType) command);
                    }
                    break;
                case PANEL_TIME_BROADCAST:
                    if (command instanceof OnOffType) {
                        cmd = command == OnOffType.ON ? 1 : 0;
                        dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.TimeDateBroadcastControl, String.valueOf(cmd));
                        updateProperties(channelUID, cmd, "");
                        updateState(channelUID, (OnOffType) command);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Method to set Channel PANEL_MESSAGE.
     *
     * @param message
     */
    private void setPanelMessage(String message) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), PANEL_MESSAGE);
        updateProperties(channelUID, 0, message);
        updateChannel(channelUID);
    }

    /**
     * Method to set the time stamp state.
     *
     * @param timeStamp
     */
    private void setTimeStampState(String timeStamp) {
        int state = 0;
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), PANEL_TIME_STAMP);

        boolean isTimeStamp = properties.getSystemTimeStamp();

        if ((timeStamp == "" && isTimeStamp == false) || (timeStamp != "" && isTimeStamp == true)) {
            logger.debug("setTimeStampState(): Already Set!", timeStamp);
            return;
        } else if (timeStamp != "") {
            state = 1;
        }

        updateProperties(channelUID, state, "");
        updateChannel(channelUID);
    }

    /**
     * Method to set Channel PANEL_SYSTEM_ERROR.
     *
     * @param properties
     * @param dscAlarmMessage
     */
    private void panelSystemError(DSCAlarmMessage dscAlarmMessage) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), PANEL_SYSTEM_ERROR);
        properties.setSystemError(1);
        properties.setSystemErrorCode(0);
        int systemErrorCode = 0;

        if (dscAlarmMessage != null) {
            systemErrorCode = Integer.parseInt(dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DATA));
            properties.setSystemErrorCode(systemErrorCode);
        }
        switch (systemErrorCode) {
            case 1:
                properties.setSystemErrorDescription("Receive Buffer Overrun");
                break;
            case 2:
                properties.setSystemErrorDescription("Receive Buffer Overflow");
                break;
            case 3:
                properties.setSystemErrorDescription("Transmit Buffer Overflow");
                break;
            case 10:
                properties.setSystemErrorDescription("Keybus Transmit Buffer Overrun");
                break;
            case 11:
                properties.setSystemErrorDescription("Keybus Transmit Time Timeout");
                break;
            case 12:
                properties.setSystemErrorDescription("Keybus Transmit Mode Timeout");
                break;
            case 13:
                properties.setSystemErrorDescription("Keybus Transmit Keystring Timeout");
                break;
            case 14:
                properties.setSystemErrorDescription("Keybus Interface Not Functioning");
                break;
            case 15:
                properties.setSystemErrorDescription("Keybus Busy - Attempting to Disarm or Arm with user code");
                break;
            case 16:
                properties.setSystemErrorDescription("Keybus Busy – Lockout");
                break;
            case 17:
                properties.setSystemErrorDescription("Keybus Busy – Installers Mode");
                break;
            case 18:
                properties.setSystemErrorDescription("Keybus Busy - General Busy");
                break;
            case 20:
                properties.setSystemErrorDescription("API Command Syntax Error");
                break;
            case 21:
                properties.setSystemErrorDescription("API Command Partition Error - Requested Partition is out of bounds");
                break;
            case 22:
                properties.setSystemErrorDescription("API Command Not Supported");
                break;
            case 23:
                properties.setSystemErrorDescription("API System Not Armed - Sent in response to a disarm command");
                break;
            case 24:
                properties.setSystemErrorDescription("API System Not Ready to Arm - System is either not-secure, in exit-delay, or already armed");
                break;
            case 25:
                properties.setSystemErrorDescription("API Command Invalid Length");
                break;
            case 26:
                properties.setSystemErrorDescription("API User Code not Required");
                break;
            case 27:
                properties.setSystemErrorDescription("API Invalid Characters in Command - No alpha characters are allowed except for checksum");
                break;
            case 28:
                properties.setSystemErrorDescription("API Virtual Keypad is Disabled");
                break;
            case 29:
                properties.setSystemErrorDescription("API Not Valid Parameter");
                break;
            case 30:
                properties.setSystemErrorDescription("API Keypad Does Not Come Out of Blank Mode");
                break;
            case 31:
                properties.setSystemErrorDescription("API IT-100 is Already in Thermostat Menu");
                break;
            case 32:
                properties.setSystemErrorDescription("API IT-100 is NOT in Thermostat Menu");
                break;
            case 33:
                properties.setSystemErrorDescription("API No Response From Thermostat or Escort Module");
                break;
            case 0:
            default:
                properties.setSystemErrorDescription("No Error");
                break;
        }

        String errorMessage = String.format("%03d", properties.getSystemErrorCode()) + ": " + properties.getSystemErrorDescription();
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
        String[] channelTypes = { PANEL_SERVICE_REQUIRED, PANEL_AC_TROUBLE, PANEL_TELEPHONE_TROUBLE, PANEL_FTC_TROUBLE, PANEL_ZONE_FAULT, PANEL_ZONE_TAMPER, PANEL_ZONE_LOW_BATTERY, PANEL_TIME_LOSS };

        String channel;
        ChannelUID channelUID = null;

        int bitField = Integer.decode("0x" + dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DATA));
        int[] masks = { 1, 2, 4, 8, 16, 32, 64, 128 };
        int[] bits = new int[8];

        for (int i = 0; i < 8; i++) {
            bits[i] = bitField & masks[i];

            channel = channelTypes[i];

            if (channel != "") {
                channelUID = new ChannelUID(getThing().getUID(), channel);
                updateProperties(channelUID, bits[i] != 0 ? 1 : 0, "");
                updateChannel(channelUID);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dscAlarmEventReceived(EventObject event, Thing thing) {

        if (thing != null) {
            DSCAlarmEvent dscAlarmEvent = (DSCAlarmEvent) event;
            DSCAlarmMessage dscAlarmMessage = dscAlarmEvent.getDSCAlarmMessage();
            setTimeStampState(dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.TIME_STAMP));
            boolean suppressPanelMsg = false;

            if (getThing() == thing) {
                ChannelUID channelUID = null;
                DSCAlarmCode apiCode = DSCAlarmCode.getDSCAlarmCodeValue(dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.CODE));
                logger.debug("dscAlarmEventRecieved(): Thing - {}   Command - {}", thing.getUID(), apiCode);

                int state = 0;

                switch (apiCode) {
                    case CommandAcknowledge: /* 500 */
                        if (getSuppressAcknowledgementMsgs()) {
                            suppressPanelMsg = true;
                        }
                        break;
                    case SystemError: /* 502 */
                        panelSystemError(dscAlarmMessage);
                        break;
                    case TimeDateBroadcast: /* 550 */
                        channelUID = new ChannelUID(getThing().getUID(), PANEL_TIME);
                        String panelTime = dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DATA);
                        updateProperties(channelUID, state, panelTime);
                        updateChannel(channelUID);

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_TIME_BROADCAST);
                        updateProperties(channelUID, 1, "");
                        updateChannel(channelUID);

                        if (getSuppressAcknowledgementMsgs()) {
                            suppressPanelMsg = true;
                        }

                        break;
                    case FireKeyAlarm: /* 621 */
                        state = 1;
                    case FireKeyRestored: /* 622 */
                        channelUID = new ChannelUID(getThing().getUID(), PANEL_FIRE_KEY_ALARM);
                        updateProperties(channelUID, state, "");
                        updateChannel(channelUID);
                        break;
                    case AuxiliaryKeyAlarm: /* 623 */
                        state = 1;
                    case AuxiliaryKeyRestored: /* 624 */
                        channelUID = new ChannelUID(getThing().getUID(), PANEL_AUX_KEY_ALARM);
                        updateProperties(channelUID, state, "");
                        updateChannel(channelUID);
                        break;
                    case PanicKeyAlarm: /* 625 */
                        state = 1;
                    case PanicKeyRestored: /* 626 */
                        channelUID = new ChannelUID(getThing().getUID(), PANEL_PANIC_KEY_ALARM);
                        updateProperties(channelUID, state, "");
                        updateChannel(channelUID);
                        break;
                    case AuxiliaryInputAlarm: /* 631 */
                        state = 1;
                    case AuxiliaryInputAlarmRestored: /* 632 */
                        channelUID = new ChannelUID(getThing().getUID(), PANEL_AUX_INPUT_ALARM);
                        updateProperties(channelUID, state, "");
                        updateChannel(channelUID);
                        break;
                    case TroubleLEDOn: /* 840 */
                        channelUID = new ChannelUID(getThing().getUID(), PANEL_TROUBLE_LED);
                        updateProperties(channelUID, 1, "");
                        updateChannel(channelUID);
                        break;
                    case TroubleLEDOff: /* 841 */
                        channelUID = new ChannelUID(getThing().getUID(), PANEL_SERVICE_REQUIRED);
                        updateProperties(channelUID, 0, "");
                        updateChannel(channelUID);

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_AC_TROUBLE);
                        updateProperties(channelUID, 0, "");
                        updateChannel(channelUID);

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_TELEPHONE_TROUBLE);
                        updateProperties(channelUID, 0, "");
                        updateChannel(channelUID);

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_FTC_TROUBLE);
                        updateProperties(channelUID, 0, "");
                        updateChannel(channelUID);

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_ZONE_FAULT);
                        updateProperties(channelUID, 0, "");
                        updateChannel(channelUID);

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_ZONE_TAMPER);
                        updateProperties(channelUID, 0, "");
                        updateChannel(channelUID);

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_ZONE_LOW_BATTERY);
                        updateProperties(channelUID, 0, "");
                        updateChannel(channelUID);

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_TIME_LOSS);
                        updateProperties(channelUID, 0, "");
                        updateChannel(channelUID);

                        channelUID = new ChannelUID(getThing().getUID(), PANEL_TROUBLE_LED);
                        updateProperties(channelUID, 0, "");
                        updateChannel(channelUID);
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
                        updateProperties(channelUID, 0, dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DESCRIPTION));
                        updateChannel(channelUID);
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
                        updateProperties(channelUID, 0, "");
                        updateChannel(channelUID);
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

            if (!suppressPanelMsg) {
                setPanelMessage(dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DESCRIPTION));
            }
        }
    }
}
