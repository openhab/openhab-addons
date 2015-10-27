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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.dscalarm.internal.DSCAlarmCode;
import org.openhab.binding.dscalarm.internal.DSCAlarmEvent;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage.DSCAlarmMessageInfoType;
import org.openhab.binding.dscalarm.internal.DSCAlarmProperties.LEDStateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class for handling a Zone type Thing.
 *
 * @author Russell Stephens - Initial Contribution
 */
public class KeypadThingHandler extends DSCAlarmBaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(KeypadThingHandler.class);

    /**
     * Constructor.
     *
     * @param thing
     */
    public KeypadThingHandler(Thing thing) {
        super(thing);
        setDSCAlarmThingType(DSCAlarmThingType.KEYPAD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateChannel(ChannelUID channelUID) {
        logger.debug("updateChannel(): Keypad Channel UID: {}", channelUID);

        int state;

        if (channelUID != null) {
            switch (channelUID.getId()) {
                case KEYPAD_READY_LED:
                    state = properties.getLEDState(LEDStateType.READY_LED_STATE);
                    updateState(channelUID, new DecimalType(state));
                    break;
                case KEYPAD_ARMED_LED:
                    state = properties.getLEDState(LEDStateType.ARMED_LED_STATE);
                    updateState(channelUID, new DecimalType(state));
                    break;
                case KEYPAD_MEMORY_LED:
                    state = properties.getLEDState(LEDStateType.MEMORY_LED_STATE);
                    updateState(channelUID, new DecimalType(state));
                    break;
                case KEYPAD_BYPASS_LED:
                    state = properties.getLEDState(LEDStateType.BYPASS_LED_STATE);
                    updateState(channelUID, new DecimalType(state));
                    break;
                case KEYPAD_TROUBLE_LED:
                    state = properties.getLEDState(LEDStateType.TROUBLE_LED_STATE);
                    updateState(channelUID, new DecimalType(state));
                    break;
                case KEYPAD_PROGRAM_LED:
                    state = properties.getLEDState(LEDStateType.PROGRAM_LED_STATE);
                    updateState(channelUID, new DecimalType(state));
                    break;
                case KEYPAD_FIRE_LED:
                    state = properties.getLEDState(LEDStateType.FIRE_LED_STATE);
                    updateState(channelUID, new DecimalType(state));
                    break;
                case KEYPAD_BACKLIGHT_LED:
                    state = properties.getLEDState(LEDStateType.BACKLIGHT_LED_STATE);
                    updateState(channelUID, new DecimalType(state));
                    break;
                case KEYPAD_AC_LED:
                    state = properties.getLEDState(LEDStateType.AC_LED_STATE);
                    updateState(channelUID, new DecimalType(state));
                    break;
                default:
                    logger.debug("updateChannel(): Keypad Channel not updated - {}.", channelUID);
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateProperties(ChannelUID channelUID, int state, String description) {
        logger.debug("updateProperties(): Keypad Channel UID: {}", channelUID);

        if (channelUID != null) {
            switch (channelUID.getId()) {
                case KEYPAD_READY_LED:
                    properties.setLEDState(LEDStateType.READY_LED_STATE, state);
                    break;
                case KEYPAD_ARMED_LED:
                    properties.setLEDState(LEDStateType.ARMED_LED_STATE, state);
                    break;
                case KEYPAD_MEMORY_LED:
                    properties.setLEDState(LEDStateType.MEMORY_LED_STATE, state);
                    break;
                case KEYPAD_BYPASS_LED:
                    properties.setLEDState(LEDStateType.BYPASS_LED_STATE, state);
                    break;
                case KEYPAD_TROUBLE_LED:
                    properties.setLEDState(LEDStateType.TROUBLE_LED_STATE, state);
                    break;
                case KEYPAD_PROGRAM_LED:
                    properties.setLEDState(LEDStateType.PROGRAM_LED_STATE, state);
                    break;
                case KEYPAD_FIRE_LED:
                    properties.setLEDState(LEDStateType.FIRE_LED_STATE, state);
                    break;
                case KEYPAD_BACKLIGHT_LED:
                    properties.setLEDState(LEDStateType.BACKLIGHT_LED_STATE, state);
                    break;
                case KEYPAD_AC_LED:
                    properties.setLEDState(LEDStateType.AC_LED_STATE, state);
                    break;
                default:
                    logger.debug("updateProperties(): Keypad property not updated.");
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No Commands to Handle
    }

    /**
     * Handle Keypad LED events for the EyezOn Envisalink 3/2DS DSC Alarm Interface
     *
     * @param event
     */
    private void keypadLEDStateEventHandler(EventObject event) {
        DSCAlarmEvent dscAlarmEvent = (DSCAlarmEvent) event;
        DSCAlarmMessage apiMessage = dscAlarmEvent.getDSCAlarmMessage();
        String[] channelTypes = { KEYPAD_READY_LED, KEYPAD_ARMED_LED, KEYPAD_MEMORY_LED, KEYPAD_BYPASS_LED, KEYPAD_TROUBLE_LED, KEYPAD_PROGRAM_LED, KEYPAD_FIRE_LED, KEYPAD_BACKLIGHT_LED };

        String channel;
        ChannelUID channelUID = null;
        DSCAlarmCode apiCode = DSCAlarmCode.getDSCAlarmCodeValue(apiMessage.getMessageInfo(DSCAlarmMessageInfoType.CODE));

        int bitField = Integer.decode("0x" + apiMessage.getMessageInfo(DSCAlarmMessageInfoType.DATA));
        int[] masks = { 1, 2, 4, 8, 16, 32, 64, 128 };
        int[] bits = new int[8];

        for (int i = 0; i < 8; i++) {
            bits[i] = bitField & masks[i];

            channel = channelTypes[i];

            if (channel != "") {

                channelUID = new ChannelUID(getThing().getUID(), channel);

                switch (apiCode) {
                    case KeypadLEDState: /* 510 */
                        updateProperties(channelUID, bits[i] != 0 ? 1 : 0, "");
                        break;
                    case KeypadLEDFlashState: /* 511 */
                        if (bits[i] != 0) {
                            updateProperties(channelUID, 2, "");
                        }
                        break;
                    default:
                        break;
                }

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
            if (getThing() == thing) {
                DSCAlarmEvent dscAlarmEvent = (DSCAlarmEvent) event;
                DSCAlarmMessage apiMessage = dscAlarmEvent.getDSCAlarmMessage();

                ChannelUID channelUID = null;
                DSCAlarmCode apiCode = DSCAlarmCode.getDSCAlarmCodeValue(apiMessage.getMessageInfo(DSCAlarmMessageInfoType.CODE));
                String apiData = apiMessage.getMessageInfo(DSCAlarmMessageInfoType.DATA);

                logger.debug("dscAlarmEventRecieved(): Thing - {}   Command - {}", thing.getUID(), apiCode);

                switch (apiCode) {
                    case KeypadLEDState: /* 510 */
                    case KeypadLEDFlashState: /* 511 */
                        keypadLEDStateEventHandler(event);
                        break;
                    case LEDStatus: /* 903 */
                        int aData = Integer.parseInt(apiData.substring(0, 1));
                        int state = Integer.parseInt(apiMessage.getMessageInfo(DSCAlarmMessageInfoType.DATA).substring(1));
                        switch (aData) {
                            case 1:
                                channelUID = new ChannelUID(getThing().getUID(), KEYPAD_READY_LED);
                                properties.setLEDState(LEDStateType.READY_LED_STATE, state);
                                updateChannel(channelUID);
                                break;
                            case 2:
                                channelUID = new ChannelUID(getThing().getUID(), KEYPAD_ARMED_LED);
                                properties.setLEDState(LEDStateType.ARMED_LED_STATE, state);
                                updateChannel(channelUID);
                                break;
                            case 3:
                                channelUID = new ChannelUID(getThing().getUID(), KEYPAD_MEMORY_LED);
                                properties.setLEDState(LEDStateType.MEMORY_LED_STATE, state);
                                updateChannel(channelUID);
                                break;
                            case 4:
                                channelUID = new ChannelUID(getThing().getUID(), KEYPAD_BYPASS_LED);
                                properties.setLEDState(LEDStateType.BYPASS_LED_STATE, state);
                                updateChannel(channelUID);
                                break;
                            case 5:
                                channelUID = new ChannelUID(getThing().getUID(), KEYPAD_TROUBLE_LED);
                                properties.setLEDState(LEDStateType.TROUBLE_LED_STATE, state);
                                updateChannel(channelUID);
                                break;
                            case 6:
                                channelUID = new ChannelUID(getThing().getUID(), KEYPAD_PROGRAM_LED);
                                properties.setLEDState(LEDStateType.PROGRAM_LED_STATE, state);
                                updateChannel(channelUID);
                                break;
                            case 7:
                                channelUID = new ChannelUID(getThing().getUID(), KEYPAD_FIRE_LED);
                                properties.setLEDState(LEDStateType.FIRE_LED_STATE, state);
                                updateChannel(channelUID);
                                break;
                            case 8:
                                channelUID = new ChannelUID(getThing().getUID(), KEYPAD_BACKLIGHT_LED);
                                properties.setLEDState(LEDStateType.BACKLIGHT_LED_STATE, state);
                                updateChannel(channelUID);
                                break;
                            case 9:
                                channelUID = new ChannelUID(getThing().getUID(), KEYPAD_AC_LED);
                                properties.setLEDState(LEDStateType.AC_LED_STATE, state);
                                updateChannel(channelUID);
                                break;
                            default:
                                break;
                        }
                    default:
                        break;
                }
            }
        }
    }
}
