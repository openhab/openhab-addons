/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.controls;

import static org.openhab.binding.loxone.internal.LxBindingConstants.*;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.loxone.internal.LxServerHandlerApi;
import org.openhab.binding.loxone.internal.core.LxCategory;
import org.openhab.binding.loxone.internal.core.LxContainer;
import org.openhab.binding.loxone.internal.core.LxUuid;

/**
 * A switch type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a switch control is:
 * <ul>
 * <li>a virtual input of switch type
 * <li>a push button function block
 * </ul>
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlSwitch extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlSwitch(uuid);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    /**
     * A name by which Miniserver refers to switch controls
     */
    static final String TYPE_NAME = "switch";

    /**
     * Switch has one state that can be on/off
     */
    private static final String STATE_ACTIVE = "active";

    /**
     * Command string used to set control's state to ON
     */
    private static final String CMD_ON = "On";
    /**
     * Command string used to set control's state to OFF
     */
    private static final String CMD_OFF = "Off";

    LxControlSwitch(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxServerHandlerApi api, LxContainer room, LxCategory category) {
        super.initialize(api, room, category);
        if (category != null && category.getType() == LxCategory.CategoryType.LIGHTS) {
            tags.add("Lighting");
        }
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_SWITCH), defaultChannelId,
                defaultChannelLabel, "Switch", tags);
    }

    @Override
    public void handleCommand(ChannelUID channelId, Command command) throws IOException {
        if (command instanceof OnOffType) {
            if ((OnOffType) command == OnOffType.ON) {
                on();
            } else {
                off();
            }
        }
    }

    @Override
    public State getChannelState(ChannelUID channelId) {
        if (defaultChannelId.equals(channelId)) {
            return getState();
        }
        return null;
    }

    /**
     * Set switch to ON.
     * <p>
     * Sends a command to operate the switch.
     * This method is separated, so {@link LxControlMood} can inherit from this class, but handle 'on' commands
     * differently.
     *
     * @throws IOException when something went wrong with communication
     */
    void on() throws IOException {
        sendAction(CMD_ON);
    }

    /**
     * Set switch to OFF.
     * <p>
     * Sends a command to operate the switch.
     * This method is separated, so {@link LxControlMood} and {@link LxControlPushbutton} can inherit from this class,
     * but handle 'off' commands differently.
     *
     * @throws IOException when something went wrong with communication
     */
    void off() throws IOException {
        sendAction(CMD_OFF);
    }

    /**
     * Get current value of the switch'es state.
     * This method is separated, so it can be overridden by {@link LxControlTimedSwitch}, which inherits from the switch
     * class, but has a different way of handling states.
     *
     * @return ON/OFF or null if undefined
     */
    OnOffType getState() {
        Double value = getStateDoubleValue(STATE_ACTIVE);
        if (value != null) {
            if (value == 1.0) {
                return OnOffType.ON;
            } else if (value == 0) {
                return OnOffType.OFF;
            }
        }
        return null;
    }
}
