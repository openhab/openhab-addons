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
import org.eclipse.smarthome.core.library.types.PercentType;
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
 * A dimmer type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a dimmer control is:
 * <ul>
 * <li>a virtual input of dimmer type
 * </ul>
 *
 * @author Stephan Brunner - initial contribution
 *
 */
public class LxControlDimmer extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxServerHandlerApi handlerApi, LxUuid uuid, LxJsonControl json, LxContainer room,
                LxCategory category) {
            return new LxControlDimmer(handlerApi, uuid, json, room, category);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    /**
     * A name by which Miniserver refers to dimmer controls
     */
    private static final String TYPE_NAME = "dimmer";
    /**
     * States
     */
    private static final String STATE_POSITION = "position";
    private static final String STATE_MIN = "min";
    private static final String STATE_MAX = "max";

    /**
     * Command string used to set the dimmer ON
     */
    private static final String CMD_ON = "On";
    /**
     * Command string used to set the dimmer to OFF
     */
    private static final String CMD_OFF = "Off";

    /**
     * Create dimmer control object.
     *
     * @param handlerApi thing handler object representing the Miniserver
     * @param uuid       dimmer's UUID
     * @param json       JSON describing the control as received from the Miniserver
     * @param room       room to which dimmer belongs
     * @param category   category to which dimmer belongs
     */
    LxControlDimmer(LxServerHandlerApi handlerApi, LxUuid uuid, LxJsonControl json, LxContainer room,
            LxCategory category) {
        super(handlerApi, uuid, json, room, category);
        addChannel("Dimmer", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_DIMMER), defaultChannelId,
                defaultChannelLabel, "Dimmer", tags);
    }

    @Override
    public void handleCommand(ChannelUID channelId, Command command) throws IOException {
        if (command instanceof OnOffType) {
            if (command == OnOffType.ON) {
                sendAction(CMD_ON);
            } else {
                sendAction(CMD_OFF);
            }
        } else if (command instanceof PercentType) {
            PercentType percentCmd = (PercentType) command;
            setPosition(percentCmd.doubleValue());
        }
    }

    @Override
    public State getChannelState(ChannelUID channelId) {
        if (defaultChannelId.equals(channelId)) {
            Double value = mapLoxoneToOH(getStateDoubleValue(STATE_POSITION));
            if (value != null && value >= 0 && value <= 100) {
                return new PercentType(value.intValue());
            }
        }
        return null;
    }

    /**
     * Sets the current position of the dimmer
     *
     * @param position position to move to (0-100, 0 - full off, 100 - full on)
     * @throws IOException error communicating with the Miniserver
     */
    private void setPosition(Double position) throws IOException {
        Double loxonePosition = mapOHToLoxone(position);
        if (loxonePosition != null) {
            sendAction(loxonePosition.toString());
        }
    }

    private Double mapLoxoneToOH(Double loxoneValue) {
        if (loxoneValue != null) {
            // 0 means turn dimmer off, any value above zero should be mapped from min-max range
            if (Double.compare(loxoneValue, 0.0) == 0) {
                return 0.0;
            }
            Double max = getStateDoubleValue(STATE_MAX);
            Double min = getStateDoubleValue(STATE_MIN);
            if (max != null && min != null) {
                return (loxoneValue - min) * ((max - min) / 100);
            }
        }
        return null;
    }

    private Double mapOHToLoxone(Double ohValue) {
        if (ohValue != null) {
            // 0 means turn dimmer off, any value above zero should be mapped to min-max range
            if (Double.compare(ohValue, 0.0) == 0) {
                return 0.0;
            }
            Double max = getStateDoubleValue(STATE_MAX);
            Double min = getStateDoubleValue(STATE_MIN);
            if (max != null && min != null) {
                double value = min + (ohValue / ((max - min) / 100));
                return value; // no rounding to integer value is needed as loxone is accepting floating point values
            }
        }
        return null;
    }
}
