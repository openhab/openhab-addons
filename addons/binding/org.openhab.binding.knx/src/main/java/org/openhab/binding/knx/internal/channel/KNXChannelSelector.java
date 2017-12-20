/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.channel;

import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

@NonNullByDefault
public final class KNXChannelSelector {

    private static final Set<KNXChannelType> TYPES = Collections.unmodifiableSet(Stream.of(//
            new TypeContact(), //
            new TypeDimmer(), //
            new TypeRollershutter(), //
            new TypeSetpoint(), //
            new TypeSwitch(), //
            new TypeWallButton(), //
            new TypeDateTime(), //
            new TypeNumber(), //
            new TypeString()).collect(toSet()));

    private KNXChannelSelector() {
        // prevent instantiation
    }

    public static KNXChannelType getValueSelectorFromChannelTypeId(@Nullable ChannelTypeUID channelTypeUID)
            throws IllegalArgumentException {
        if (channelTypeUID == null) {
            throw new IllegalArgumentException("channel type UID was null");
        }

        for (KNXChannelType c : TYPES) {
            if (c.getChannelID().equals(channelTypeUID.getId())) {
                return c;
            }
        }
        throw new IllegalArgumentException(channelTypeUID.getId() + " is not a valid value channel type ID");

    }

}
