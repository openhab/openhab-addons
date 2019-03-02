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
import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;
import org.openhab.binding.loxone.internal.core.LxUuid;

/**
 * An InfoOnlyDigital type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, this control covers digital virtual states only. This control does not send
 * any commands to the Miniserver. It can be used to read a formatted representation of a digital virtual state.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlInfoOnlyDigital extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxServerHandlerApi handlerApi, LxUuid uuid, LxJsonControl json, LxContainer room,
                LxCategory category) {
            return new LxControlInfoOnlyDigital(handlerApi, uuid, json, room, category);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    /**
     * A name by which Miniserver refers to digital virtual state controls
     */
    private static final String TYPE_NAME = "infoonlydigital";
    /**
     * InfoOnlyDigital has one state that can be on/off
     */
    private static final String STATE_ACTIVE = "active";

    /**
     * Create InfoOnlyDigital control object.
     *
     * @param handlerApi thing handler object representing the Miniserver
     * @param uuid       control's UUID
     * @param json       JSON describing the control as received from the Miniserver
     * @param room       room to which control belongs
     * @param category   category to which control belongs
     */
    LxControlInfoOnlyDigital(LxServerHandlerApi handlerApi, LxUuid uuid, LxJsonControl json, LxContainer room,
            LxCategory category) {
        super(handlerApi, uuid, json, room, category);
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_SWITCH), defaultChannelId,
                defaultChannelLabel, "Digital virtual state", tags);
    }

    @Override
    public void handleCommand(ChannelUID channelId, Command command) throws IOException {
        // no commands to handle
    }

    @Override
    public State getChannelState(ChannelUID channelId) {
        if (defaultChannelId.equals(channelId)) {
            Double value = getStateDoubleValue(STATE_ACTIVE);
            if (value != null) {
                if (value == 0) {
                    return OnOffType.OFF;
                } else if (value == 1.0) {
                    return OnOffType.ON;
                }
            }
        }
        return null;
    }
}
