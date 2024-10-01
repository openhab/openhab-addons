/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.channel;

import static org.openhab.binding.knx.internal.KNXBindingConstants.*;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;

import tuwien.auto.calimero.dptxlator.DPTXlator8BitUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;

/**
 * rollershutter channel type description
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
class TypeRollershutter extends KNXChannel {
    public static final Set<String> SUPPORTED_CHANNEL_TYPES = Set.of(CHANNEL_ROLLERSHUTTER,
            CHANNEL_ROLLERSHUTTER_CONTROL);

    TypeRollershutter(Channel channel) {
        super(List.of(UP_DOWN_GA, STOP_MOVE_GA, POSITION_GA),
                List.of(PercentType.class, UpDownType.class, StopMoveType.class), channel);
    }

    @Override
    protected String getDefaultDPT(String gaConfigKey) {
        return switch (gaConfigKey) {
            case UP_DOWN_GA -> DPTXlatorBoolean.DPT_UPDOWN.getID();
            case STOP_MOVE_GA -> DPTXlatorBoolean.DPT_START.getID();
            case POSITION_GA -> DPTXlator8BitUnsigned.DPT_SCALING.getID();
            default -> throw new IllegalArgumentException("GA configuration '" + gaConfigKey + "' is not supported");
        };
    }
}
