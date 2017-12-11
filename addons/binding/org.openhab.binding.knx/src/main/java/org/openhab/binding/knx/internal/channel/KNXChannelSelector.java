/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.channel;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

@NonNullByDefault
public final class KNXChannelSelector {

    private KNXChannelSelector() {
        // prevent instantiation
    }

    private static final Set<KNXChannelType> types = new HashSet<KNXChannelType>() {
        private static final long serialVersionUID = 1L;
        {
            add(new TypeContact());
            add(new TypeDimmer());
            add(new TypeRollershutter());
            add(new TypeSetpoint());
            add(new TypeSwitch());
            add(new TypeWallButton());

            add(new TypeDateTime());
            add(new TypeNumber());
            add(new TypeString());
        }
    };

    public static KNXChannelType getValueSelectorFromChannelTypeId(@Nullable ChannelTypeUID channelTypeUID)
            throws IllegalArgumentException {
        if (channelTypeUID == null) {
            throw new IllegalArgumentException("channel type UID was null");
        }

        for (KNXChannelType c : types) {
            if (c.getChannelID().equals(channelTypeUID.getId())) {
                return c;
            }
        }
        throw new IllegalArgumentException(channelTypeUID.getId() + " is not a valid value channel type ID");

    }

}
