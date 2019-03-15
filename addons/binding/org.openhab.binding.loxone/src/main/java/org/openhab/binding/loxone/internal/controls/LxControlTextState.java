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

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.loxone.internal.LxServerHandlerApi;
import org.openhab.binding.loxone.internal.types.LxCategory;
import org.openhab.binding.loxone.internal.types.LxContainer;
import org.openhab.binding.loxone.internal.types.LxUuid;

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
        LxControl create(LxUuid uuid) {
            return new LxControlTextState(uuid);
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

    LxControlTextState(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxServerHandlerApi thingHandler, LxContainer room, LxCategory category) {
        super.initialize(thingHandler, room, category);
        ChannelUID id = addChannel("String", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_TEXT),
                defaultChannelLabel, "Text state", tags, null, this::getState);
        addChannelStateDescription(id, new StateDescription(null, null, null, null, true, null));
    }

    private State getState() {
        String value = getStateTextValue(STATE_TEXT_AND_ICON);
        if (value != null) {
            return new StringType(value);
        }
        return null;
    }
}
