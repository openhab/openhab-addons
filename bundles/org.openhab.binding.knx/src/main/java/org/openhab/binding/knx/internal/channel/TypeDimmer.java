/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;

import tuwien.auto.calimero.dptxlator.DPTXlator3BitControlled;
import tuwien.auto.calimero.dptxlator.DPTXlator8BitUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;

/**
 * dimmer channel type description
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
class TypeDimmer extends KNXChannel {
    public static final Set<String> SUPPORTED_CHANNEL_TYPES = Set.of(CHANNEL_DIMMER, CHANNEL_DIMMER_CONTROL);

    TypeDimmer(Channel channel) {
        super(Set.of(SWITCH_GA, POSITION_GA, INCREASE_DECREASE_GA),
                List.of(PercentType.class, OnOffType.class, IncreaseDecreaseType.class), channel);
    }

    @Override
    protected String getDefaultDPT(String gaConfigKey) {
        if (Objects.equals(gaConfigKey, INCREASE_DECREASE_GA)) {
            return DPTXlator3BitControlled.DPT_CONTROL_DIMMING.getID();
        }
        if (Objects.equals(gaConfigKey, SWITCH_GA)) {
            return DPTXlatorBoolean.DPT_SWITCH.getID();
        }
        if (Objects.equals(gaConfigKey, POSITION_GA)) {
            return DPTXlator8BitUnsigned.DPT_SCALING.getID();
        }
        throw new IllegalArgumentException("GA configuration '" + gaConfigKey + "' is not supported");
    }
}
