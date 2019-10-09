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
import org.eclipse.smarthome.core.types.StateDescriptionFragmentBuilder;
import org.openhab.binding.loxone.internal.types.LxUuid;

/**
 * A meter type of control on Loxone Miniserver.
 * According to Loxone API documentation, a meter control covers Utility Meter functional block in Loxone Config.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlMeter extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlMeter(uuid);
        }

        @Override
        String getType() {
            return "meter";
        }
    }

    /**
     * Value for actual consumption
     */
    private static final String STATE_ACTUAL = "actual";

    /**
     * Value for total consumption
     */
    private static final String STATE_TOTAL = "total";

    /**
     * Command string used to reset the meter
     */
    private static final String CMD_RESET = "reset";

    LxControlMeter(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        ChannelUID cid = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER),
                defaultChannelLabel + " / Current", "Current meter value", tags, null,
                () -> getStateDecimalValue(STATE_ACTUAL));
        String format;
        if (details != null && details.actualFormat != null) {
            format = details.actualFormat;
        } else {
            format = "%.3f"; // Loxone default for this format
        }
        addChannelStateDescriptionFragment(cid,
                StateDescriptionFragmentBuilder.create().withPattern(format).withReadOnly(true).build());

        cid = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER),
                defaultChannelLabel + " / Total", "Total meter consumption", tags, null,
                () -> getStateDecimalValue(STATE_TOTAL));
        if (details != null && details.totalFormat != null) {
            format = details.totalFormat;
        } else {
            format = "%.1f"; // Loxone default for this format
        }
        addChannelStateDescriptionFragment(cid,
                StateDescriptionFragmentBuilder.create().withPattern(format).withReadOnly(true).build());

        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_SWITCH),
                defaultChannelLabel + " / Reset", "Reset meter", tags, this::handleResetCommands, () -> OnOffType.OFF);
    }

    private void handleResetCommands(Command command) throws IOException {
        if (command instanceof OnOffType && (OnOffType) command == OnOffType.ON) {
            sendAction(CMD_RESET);
        }
    }
}
