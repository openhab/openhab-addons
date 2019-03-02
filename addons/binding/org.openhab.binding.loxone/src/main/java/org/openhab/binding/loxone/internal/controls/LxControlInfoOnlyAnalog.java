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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.loxone.internal.LxServerHandlerApi;
import org.openhab.binding.loxone.internal.core.LxCategory;
import org.openhab.binding.loxone.internal.core.LxContainer;
import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;
import org.openhab.binding.loxone.internal.core.LxUuid;

/**
 * An InfoOnlyAnalog type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, this control covers analog virtual states only. This control does not send any
 * commands to the Miniserver. It can be used to read a formatted representation of an analog virtual state.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlInfoOnlyAnalog extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxServerHandlerApi handlerApi, LxUuid uuid, LxJsonControl json, LxContainer room,
                LxCategory category) {
            return new LxControlInfoOnlyAnalog(handlerApi, uuid, json, room, category);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    /**
     * A name by which Miniserver refers to analog virtual state controls
     */
    private static final String TYPE_NAME = "infoonlyanalog";
    /**
     * InfoOnlyAnalog state with current value
     */
    private static final String STATE_VALUE = "value";

    private String format;

    /**
     * Create InfoOnlyAnalog control object.
     *
     * @param handlerApi
     *                       thing handler object representing the Miniserver
     * @param uuid
     *                       control's UUID
     * @param json
     *                       JSON describing the control as received from the Miniserver
     * @param room
     *                       room to which control belongs
     * @param category
     *                       category to which control belongs
     */
    LxControlInfoOnlyAnalog(LxServerHandlerApi handlerApi, LxUuid uuid, LxJsonControl json, LxContainer room,
            LxCategory category) {
        // super constructor will call update() and populate format
        super(handlerApi, uuid, json, room, category);
        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_ANALOG), defaultChannelId,
                defaultChannelLabel, "Analog virtual state", tags);
        if (format != null) {
            addChannelStateDescription(defaultChannelId, new StateDescription(null, null, null, format, true, null));
        }
    }

    /**
     * Update Miniserver's control in runtime.
     *
     * @param json
     *                     JSON describing the control as received from the Miniserver
     * @param room
     *                     New room that this control belongs to
     * @param category
     *                     New category that this control belongs to
     */
    @Override
    public void update(LxJsonControl json, LxContainer room, LxCategory category) {
        super.update(json, room, category);
        if (json.details != null && json.details.format != null) {
            format = json.details.format;
        } else {
            format = "%.1f";
        }
    }

    @Override
    public void handleCommand(ChannelUID channelId, Command command) throws IOException {
        // no commands to handle
    }

    @Override
    public State getChannelState(ChannelUID channelId) {
        if (defaultChannelId.equals(channelId)) {
            Double value = getStateDoubleValue(STATE_VALUE);
            if (value != null) {
                return new DecimalType(value);
            }
        }
        return null;
    }
}
