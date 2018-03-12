/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.ets.channel;

import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.knx.KNXBindingConstants;
import org.openhab.binding.knx.internal.channel.AbstractKNXChannelType;

/**
 * Helper class to find the matching {@link ETSKNXChannelType} for any given {@link ChannelTypeUID}.
 *
 * @author Karel Goderis - Initial contribution
 *
 */
@NonNullByDefault
public final class ETSChannelTypes {

    private static final Set<AbstractKNXChannelType> TYPES = Collections.unmodifiableSet(Stream.of(//
            new TypeGeneric() //
    ).collect(toSet()));

    private ETSChannelTypes() {
    }

    public static AbstractKNXChannelType getType(@Nullable ChannelTypeUID channelTypeUID)
            throws IllegalArgumentException {
        Objects.requireNonNull(channelTypeUID);
        for (AbstractKNXChannelType c : TYPES) {
            if (c.getChannelIDs().contains(KNXBindingConstants.CHANNEL_GENERIC)) {
                // always true
                return c;
            }
        }
        throw new IllegalArgumentException(channelTypeUID.getId() + " is not a valid value channel type ID");
    }
}
