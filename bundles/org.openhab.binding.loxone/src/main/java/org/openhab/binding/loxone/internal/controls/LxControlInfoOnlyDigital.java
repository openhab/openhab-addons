/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * An InfoOnlyDigital type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, this control covers digital virtual states only. This control does not send
 * any commands to the Miniserver. It can be used to read a formatted representation of a digital virtual state.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlInfoOnlyDigital extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlInfoOnlyDigital(uuid);
        }

        @Override
        String getType() {
            return "infoonlydigital";
        }
    }

    /**
     * InfoOnlyDigital has one state that can be on/off
     */
    private static final String STATE_ACTIVE = "active";

    private LxControlInfoOnlyDigital(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_SWITCH), defaultChannelLabel,
                "Digital virtual state", tags, null, this::getChannelState);
    }

    private State getChannelState() {
        Double value = getStateDoubleValue(STATE_ACTIVE);
        if (value != null) {
            if (value == 0) {
                return OnOffType.OFF;
            } else if (value == 1.0) {
                return OnOffType.ON;
            } else {
                return UnDefType.UNDEF;
            }
        }
        return null;
    }
}
