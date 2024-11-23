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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;

import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;

/**
 * switch channel type description
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
class TypeSwitch extends KNXChannel {
    public static final Set<String> SUPPORTED_CHANNEL_TYPES = Set.of(CHANNEL_SWITCH, CHANNEL_SWITCH_CONTROL);

    TypeSwitch(Channel channel) {
        super(List.of(OnOffType.class), channel);
    }

    @Override
    protected String getDefaultDPT(String gaConfigKey) {
        return DPTXlatorBoolean.DPT_SWITCH.getID();
    }
}
