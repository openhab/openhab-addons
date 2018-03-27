/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.handler;

import static org.openhab.binding.lutron.LutronBindingConstants.*;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lutron.internal.protocol.LutronCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with a keypad.
 *
 * @author Allan Tong - Initial contribution
 */
public class KeypadHandler extends LutronHandler {
    private static final int COMPONENT_BUTTON1 = 1;
    private static final int COMPONENT_BUTTON2 = 2;
    private static final int COMPONENT_BUTTON3 = 3;
    private static final int COMPONENT_BUTTON4 = 4;
    private static final int COMPONENT_BUTTON5 = 5;
    private static final int COMPONENT_BUTTON6 = 6;
    private static final int COMPONENT_BUTTON7 = 7;
    private static final int COMPONENT_BUTTONTOPLOWER = 16;
    private static final int COMPONENT_BUTTONTOPRAISE = 17;
    private static final int COMPONENT_BUTTONBOTTOMLOWER = 18;
    private static final int COMPONENT_BUTTONBOTTOMRAISE = 19;
    private static final int COMPONENT_LED1 = 81;
    private static final int COMPONENT_LED2 = 82;
    private static final int COMPONENT_LED3 = 83;
    private static final int COMPONENT_LED4 = 84;
    private static final int COMPONENT_LED5 = 85;
    private static final int COMPONENT_LED6 = 86;
    private static final int COMPONENT_LED7 = 87;

    private static final Integer ACTION_PRESS = 3;
    private static final Integer ACTION_RELEASE = 4;
    private static final Integer LED_STATE = 9;

    private static final Integer LED_OFF = 0;
    private static final Integer LED_ON = 1;

    private Logger logger = LoggerFactory.getLogger(KeypadHandler.class);

    private int integrationId;

    public KeypadHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Number id = (Number) getThing().getConfiguration().get("integrationId");

        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");

            return;
        }

        this.integrationId = id.intValue();

        updateStatus(ThingStatus.ONLINE);

        queryDevice(COMPONENT_LED1, LED_STATE);
        queryDevice(COMPONENT_LED2, LED_STATE);
        queryDevice(COMPONENT_LED3, LED_STATE);
        queryDevice(COMPONENT_LED4, LED_STATE);
        queryDevice(COMPONENT_LED5, LED_STATE);
        queryDevice(COMPONENT_LED6, LED_STATE);
        queryDevice(COMPONENT_LED7, LED_STATE);
    }

    private ChannelUID channelFromComponent(int component) {
        String channel;

        switch (component) {
            case COMPONENT_BUTTON1:
                channel = CHANNEL_BUTTON1;
                break;

            case COMPONENT_BUTTON2:
                channel = CHANNEL_BUTTON2;
                break;

            case COMPONENT_BUTTON3:
                channel = CHANNEL_BUTTON3;
                break;

            case COMPONENT_BUTTON4:
                channel = CHANNEL_BUTTON4;
                break;

            case COMPONENT_BUTTON5:
                channel = CHANNEL_BUTTON5;
                break;

            case COMPONENT_BUTTON6:
                channel = CHANNEL_BUTTON6;
                break;

            case COMPONENT_BUTTON7:
                channel = CHANNEL_BUTTON7;
                break;

            case COMPONENT_BUTTONTOPRAISE:
                channel = CHANNEL_BUTTONTOPRAISE;
                break;

            case COMPONENT_BUTTONTOPLOWER:
                channel = CHANNEL_BUTTONTOPLOWER;
                break;

            case COMPONENT_BUTTONBOTTOMRAISE:
                channel = CHANNEL_BUTTONBOTTOMRAISE;
                break;

            case COMPONENT_BUTTONBOTTOMLOWER:
                channel = CHANNEL_BUTTONBOTTOMLOWER;
                break;

            case COMPONENT_LED1:
                channel = CHANNEL_LED1;
                break;

            case COMPONENT_LED2:
                channel = CHANNEL_LED2;
                break;

            case COMPONENT_LED3:
                channel = CHANNEL_LED3;
                break;

            case COMPONENT_LED4:
                channel = CHANNEL_LED4;
                break;

            case COMPONENT_LED5:
                channel = CHANNEL_LED5;
                break;

            case COMPONENT_LED6:
                channel = CHANNEL_LED6;
                break;

            case COMPONENT_LED7:
                channel = CHANNEL_LED7;
                break;

            default:
                this.logger.error("Unknown component {}", component);
                channel = null;
                break;
        }

        return channel == null ? null : new ChannelUID(getThing().getUID(), channel);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, Command command) {
    }

    @Override
    public int getIntegrationId() {
        return this.integrationId;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_LED1)) {
            queryDevice(COMPONENT_LED1, LED_STATE);
        } else if (channelUID.getId().equals(CHANNEL_LED2)) {
            queryDevice(COMPONENT_LED2, LED_STATE);
        } else if (channelUID.getId().equals(CHANNEL_LED3)) {
            queryDevice(COMPONENT_LED3, LED_STATE);
        } else if (channelUID.getId().equals(CHANNEL_LED4)) {
            queryDevice(COMPONENT_LED4, LED_STATE);
        } else if (channelUID.getId().equals(CHANNEL_LED5)) {
            queryDevice(COMPONENT_LED5, LED_STATE);
        } else if (channelUID.getId().equals(CHANNEL_LED6)) {
            queryDevice(COMPONENT_LED6, LED_STATE);
        } else if (channelUID.getId().equals(CHANNEL_LED7)) {
            queryDevice(COMPONENT_LED7, LED_STATE);
        }
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        if (type == LutronCommandType.DEVICE && parameters.length >= 2) {
            int component;

            try {
                component = Integer.parseInt(parameters[0]);
            } catch (NumberFormatException e) {
                this.logger.error("Invalid component {} in keypad update event message", parameters[0]);

                return;
            }

            ChannelUID channelUID = channelFromComponent(component);

            if (channelUID != null) {
                if (LED_STATE.toString().equals(parameters[1]) && parameters.length >= 3) {
                    if (LED_ON.toString().equals(parameters[2])) {
                        updateState(channelUID, OnOffType.ON);
                    } else if (LED_OFF.toString().equals(parameters[2])) {
                        updateState(channelUID, OnOffType.OFF);
                    }
                } else if (ACTION_PRESS.toString().equals(parameters[1])) {
                    postCommand(channelUID, OnOffType.ON);
                } else if (ACTION_RELEASE.toString().equals(parameters[1])) {
                    postCommand(channelUID, OnOffType.OFF);
                }
            }
        }
    }

}
