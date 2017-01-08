/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.handler;

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
 * Handler for sending Phantom Button presses to main repeater.
 *
 * @author Kyle Lehman
 */
public class PhantomHandler extends LutronHandler {

    private static final Integer ACTION_PRESS = 3;
    private static final Integer ACTION_RELEASE = 4;
    private static final Integer BUTTON_STATE = 9;

    private static final Integer BUTTON_OFF = 4;
    private static final Integer BUTTON_ON = 3;

    private Logger logger = LoggerFactory.getLogger(PhantomHandler.class);

    private int integrationId;

    public PhantomHandler(Thing thing) {
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
        /*
         * I cannot figure out the proper telnet query for a button.
         * I find the LED functionality to not return the results I am looking for.
         * queryDevice(COMPONENT_PBUTTON1, BUTTON_STATE);
         * queryDevice(COMPONENT_PBUTTON2, BUTTON_STATE);
         * queryDevice(COMPONENT_PBUTTON3, BUTTON_STATE);
         * queryDevice(COMPONENT_PBUTTON4, BUTTON_STATE);
         * queryDevice(COMPONENT_PBUTTON5, BUTTON_STATE);
         * queryDevice(COMPONENT_PBUTTON6, BUTTON_STATE);
         * queryDevice(COMPONENT_PBUTTON7, BUTTON_STATE);
         * queryDevice(COMPONENT_PBUTTON8, BUTTON_STATE);
         * queryDevice(COMPONENT_PBUTTON9, BUTTON_STATE);
         * queryDevice(COMPONENT_PBUTTON10, BUTTON_STATE);
         */
    }

    private ChannelUID channelFromComponent(int component) {
        String channel;
        // Increase this value if we add more channels
        if (component >= 1 && component <= 10) {
            channel = "pbutton" + String.valueOf(component);

        } else {
            this.logger.error("Unknown component {}", component);
            channel = null;
        }

        return channel == null ? null : new ChannelUID(getThing().getUID(), channel);

    }

    @Override
    public void handleCommand(final ChannelUID channelUID, Command command) {
        String scene;

        if (channelUID.toString().contains("pbutton")) {
            scene = channelUID.getId().toString();
            scene = scene.replace("pbutton", "");
            if (command.equals(OnOffType.ON)) {
                // Example ON
                // #DEVICE,1,3,3
                device(scene, ACTION_PRESS);
            } else if (command.equals(OnOffType.OFF)) {
                device(scene, ACTION_RELEASE);
            }
        }
    }

    @Override
    public int getIntegrationId() {
        return this.integrationId;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        /*
         * I cannot figure out the proper telnet query for a button.
         * I find the LED functionality to no return the results I am looking for.
         * if (channelUID.getId().equals(CHANNEL_PBUTTON1)) {
         * queryDevice(COMPONENT_PBUTTON1, BUTTON_STATE);
         * } else if (channelUID.getId().equals(CHANNEL_PBUTTON2)) {
         * queryDevice(COMPONENT_PBUTTON2, BUTTON_STATE);
         * } else if (channelUID.getId().equals(CHANNEL_PBUTTON3)) {
         * queryDevice(COMPONENT_PBUTTON3, BUTTON_STATE);
         * } else if (channelUID.getId().equals(CHANNEL_PBUTTON4)) {
         * queryDevice(COMPONENT_PBUTTON4, BUTTON_STATE);
         * } else if (channelUID.getId().equals(CHANNEL_PBUTTON5)) {
         * queryDevice(COMPONENT_PBUTTON5, BUTTON_STATE);
         * } else if (channelUID.getId().equals(CHANNEL_PBUTTON6)) {
         * queryDevice(COMPONENT_PBUTTON6, BUTTON_STATE);
         * } else if (channelUID.getId().equals(CHANNEL_PBUTTON7)) {
         * queryDevice(COMPONENT_PBUTTON7, BUTTON_STATE);
         * } else if (channelUID.getId().equals(CHANNEL_PBUTTON8)) {
         * queryDevice(COMPONENT_PBUTTON8, BUTTON_STATE);
         * } else if (channelUID.getId().equals(CHANNEL_PBUTTON9)) {
         * queryDevice(COMPONENT_PBUTTON9, BUTTON_STATE);
         * } else if (channelUID.getId().equals(CHANNEL_PBUTTON10)) {
         * queryDevice(COMPONENT_PBUTTON10, BUTTON_STATE);
         * }
         */
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
                if (BUTTON_ON.toString().equals(parameters[1])) {
                    updateState(channelUID, OnOffType.ON);
                } else if (BUTTON_OFF.toString().equals(parameters[1])) {
                    updateState(channelUID, OnOffType.OFF);
                }

            }
        }
    }

}
