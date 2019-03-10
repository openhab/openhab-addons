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

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.loxone.internal.LxServerHandler;
import org.openhab.binding.loxone.internal.types.LxCategory;
import org.openhab.binding.loxone.internal.types.LxContainer;
import org.openhab.binding.loxone.internal.types.LxUuid;

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
        LxControl create(LxUuid uuid) {
            return new LxControlInfoOnlyDigital(uuid);
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

    LxControlInfoOnlyDigital(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxServerHandler thingHandler, LxContainer room, LxCategory category) {
        super.initialize(thingHandler, room, category);
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_SWITCH), defaultChannelLabel,
                "Digital virtual state", tags, null, this::getChannelState);
    }

    private OnOffType getChannelState() {
        Double value = getStateDoubleValue(STATE_ACTIVE);
        if (value != null) {
            if (value == 0) {
                return OnOffType.OFF;
            } else if (value == 1.0) {
                return OnOffType.ON;
            }
        }
        return null;
    };
}
