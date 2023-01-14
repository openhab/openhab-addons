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
package org.openhab.binding.loxone.internal.controls;

import static org.openhab.binding.loxone.internal.LxBindingConstants.*;

import java.io.IOException;

import org.openhab.binding.loxone.internal.types.LxCategory;
import org.openhab.binding.loxone.internal.types.LxTags;
import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

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
class LxControlSwitch extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlSwitch(uuid);
        }

        @Override
        String getType() {
            return "switch";
        }
    }

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
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        LxCategory category = getCategory();
        if (category != null && category.getType() == LxCategory.CategoryType.LIGHTS) {
            tags.addAll(LxTags.LIGHTING);
        } else {
            tags.addAll(LxTags.SWITCHABLE);
        }
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_SWITCH), defaultChannelLabel,
                "Switch", tags, this::handleSwitchCommands, this::getSwitchState);
    }

    void handleSwitchCommands(Command command) throws IOException {
        if (command instanceof OnOffType) {
            if ((OnOffType) command == OnOffType.ON) {
                on();
            } else {
                off();
            }
        }
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
    State getSwitchState() {
        return convertSwitchState(getStateDoubleValue(STATE_ACTIVE));
    }

    /**
     * Convert double value of switch into ON/OFF state value
     *
     * @param value state value as double
     * @return state value as ON/OFF
     */
    static State convertSwitchState(Double value) {
        if (value != null) {
            if (value == 1.0) {
                return OnOffType.ON;
            } else if (value == 0.0) {
                return OnOffType.OFF;
            } else {
                return UnDefType.UNDEF;
            }
        }
        return null;
    }
}
