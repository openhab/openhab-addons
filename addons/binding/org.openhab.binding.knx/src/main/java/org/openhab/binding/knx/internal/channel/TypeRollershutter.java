/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.channel;

import static java.util.stream.Collectors.toSet;
import static org.openhab.binding.knx.KNXBindingConstants.*;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

import tuwien.auto.calimero.dptxlator.DPTXlator8BitUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;

/**
 * rollershutter channel type description
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
class TypeRollershutter extends KNXChannelType {

    TypeRollershutter() {
        super(CHANNEL_ROLLERSHUTTER, CHANNEL_ROLLERSHUTTER_CONTROL);
    }

    @Override
    protected String getDefaultDPT(String gaConfigKey) {
        if (Objects.equals(gaConfigKey, UP_DOWN_GA)) {
            return DPTXlatorBoolean.DPT_UPDOWN.getID();
        }
        if (Objects.equals(gaConfigKey, STOP_MOVE_GA)) {
            return DPTXlatorBoolean.DPT_START.getID();
        }
        if (Objects.equals(gaConfigKey, POSITION_GA)) {
            return DPTXlator8BitUnsigned.DPT_SCALING.getID();
        }
        throw new IllegalArgumentException("GA configuration '" + gaConfigKey + "' is not supported");
    }

    @Override
    protected Set<String> getAllGAKeys() {
        return Stream.of(UP_DOWN_GA, STOP_MOVE_GA, POSITION_GA).collect(toSet());
    }

}
