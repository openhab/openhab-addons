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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.loxone.internal.LxServerHandlerApi;
import org.openhab.binding.loxone.internal.core.LxCategory;
import org.openhab.binding.loxone.internal.core.LxContainer;
import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;
import org.openhab.binding.loxone.internal.core.LxUuid;

/**
 * A timed switch type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a switch control is:
 * <ul>
 * <li>a virtual input of switch type
 * <li>a push button function block
 * </ul>
 *
 * @author Stephan Brunner - initial contribution
 *
 */
public class LxControlTimedSwitch extends LxControlPushbutton {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxServerHandlerApi handlerApi, LxUuid uuid, LxJsonControl json, LxContainer room,
                LxCategory category) {
            return new LxControlTimedSwitch(handlerApi, uuid, json, room, category);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    /**
     * A name by which Miniserver refers to timed switch controls
     */
    private static final String TYPE_NAME = "timedswitch";

    /**
     * deactivationDelay - countdown until the output is deactivated.
     * 0 = the output is turned off
     * -1 = the output is permanently on
     * otherwise it will count down from deactivationDelayTotal
     */
    private static final String STATE_DEACTIVATION_DELAY = "deactivationdelay";
    private final ChannelUID deactivationChannelId = getChannelId(1);

    /**
     * Create timed switch control object.
     *
     * @param handlerApi thing handler object representing the Miniserver
     * @param uuid       switch's UUID
     * @param json       JSON describing the control as received from the Miniserver
     * @param room       room to which switch belongs
     * @param category   category to which switch belongs
     */
    LxControlTimedSwitch(LxServerHandlerApi handlerApi, LxUuid uuid, LxJsonControl json, LxContainer room,
            LxCategory category) {
        super(handlerApi, uuid, json, room, category);
        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER), deactivationChannelId,
                defaultChannelLabel + " / Deactivation Delay", "Deactivation Delay", null);
    }

    @Override
    public void handleCommand(ChannelUID channelId, Command command) throws IOException {
        if (defaultChannelId.equals(channelId)) {
            super.handleCommand(channelId, command);
        }
    }

    @Override
    public State getChannelState(ChannelUID channelId) {
        if (defaultChannelId.equals(channelId)) {
            return super.getChannelState(channelId);
        } else if (deactivationChannelId.equals(channelId)) {
            Double deactivationValue = getStateDoubleValue(STATE_DEACTIVATION_DELAY);
            if (deactivationValue != null) {
                return new DecimalType(deactivationValue);
            }
        }
        return null;
    }

    /**
     * Get current value of the timed switch'es state.
     *
     * @return ON/OFF or null if not defined
     */
    @Override
    public OnOffType getState() {
        /**
         * 0 = the output is turned off
         * -1 = the output is permanently on
         * otherwise it will count down from deactivationDelayTotal
         **/
        Double value = getStateDoubleValue(STATE_DEACTIVATION_DELAY);
        if (value != null) {
            if (value == -1.0 || value > 0.0) { // mapping
                return OnOffType.ON;
            } else if (value == 0) {
                return OnOffType.OFF;
            }
        }
        return null;
    }
}
