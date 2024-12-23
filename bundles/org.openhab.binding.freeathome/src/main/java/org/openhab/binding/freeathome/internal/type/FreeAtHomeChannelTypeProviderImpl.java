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
package org.openhab.binding.freeathome.internal.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@Component(service = { FreeAtHomeChannelTypeProvider.class, ChannelTypeProvider.class })
@NonNullByDefault
public class FreeAtHomeChannelTypeProviderImpl implements FreeAtHomeChannelTypeProvider {

    private final Map<ChannelTypeUID, ChannelType> channelTypesByUID = new HashMap<>();

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        Collection<ChannelType> result = new ArrayList<>();

        for (ChannelTypeUID uid : channelTypesByUID.keySet()) {
            ChannelType channelType = channelTypesByUID.get(uid);

            if (channelType != null) {
                result.add(channelType);
            }
        }

        return result;
    }

    @Override
    public @Nullable ChannelType getChannelType(@Nullable ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return channelTypesByUID.get(channelTypeUID);
    }

    @Override
    public void addChannelType(@Nullable ChannelType channelType) {
        if (channelType != null) {
            channelTypesByUID.put(channelType.getUID(), channelType);
        }
    }
}
