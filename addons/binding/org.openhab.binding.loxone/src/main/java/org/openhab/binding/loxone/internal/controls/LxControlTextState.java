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

import org.eclipse.smarthome.core.library.types.StringType;
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
 * A Text State type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a text state represents a State functional block on the Miniserver
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlTextState extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxServerHandlerApi handlerApi, LxUuid uuid, LxJsonControl json, LxContainer room,
                LxCategory category) {
            return new LxControlTextState(handlerApi, uuid, json, room, category);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    /**
     * A name by which Miniserver refers to text state controls
     */
    private static final String TYPE_NAME = "textstate";

    /**
     * A state which will receive an update of possible Text State values)
     */
    private static final String STATE_TEXT_AND_ICON = "textandicon";

    /**
     * Create text state object.
     *
     * @param handlerApi thing handler object representing the Miniserver
     * @param uuid       controller's UUID
     * @param json       JSON describing the control as received from the Miniserver
     * @param room       room to which controller belongs
     * @param category   category to which controller belongs
     */
    LxControlTextState(LxServerHandlerApi handlerApi, LxUuid uuid, LxJsonControl json, LxContainer room,
            LxCategory category) {
        super(handlerApi, uuid, json, room, category);
        addChannel("String", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_TEXT), defaultChannelId,
                defaultChannelLabel, "Text state", tags);
    }

    @Override
    public void handleCommand(ChannelUID channelId, Command command) throws IOException {
        // no commands to handle
    }

    @Override
    public State getChannelState(ChannelUID channelId) {
        if (defaultChannelId.equals(channelId)) {
            String value = getStateTextValue(STATE_TEXT_AND_ICON);
            if (value != null) {
                return new StringType(value);
            }
        }
        return null;
    }
}
