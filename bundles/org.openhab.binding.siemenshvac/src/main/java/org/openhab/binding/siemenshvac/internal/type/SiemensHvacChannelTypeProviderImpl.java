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
package org.openhab.binding.siemenshvac.internal.type;

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
 * Provides all ChannelTypes from SiemensHvac bridges.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
@Component(service = { SiemensHvacChannelTypeProvider.class, ChannelTypeProvider.class })
public class SiemensHvacChannelTypeProviderImpl implements SiemensHvacChannelTypeProvider {
    private final Map<ChannelTypeUID, ChannelType> channelTypesByUID = new HashMap<>();

    public SiemensHvacChannelTypeProviderImpl() {
    }

    @Override
    public void addChannelType(ChannelType channelType) {
        channelTypesByUID.put(channelType.getUID(), channelType);
    }

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        Collection<ChannelType> result = new ArrayList<>();

        for (ChannelTypeUID uid : channelTypesByUID.keySet()) {
            ChannelType tp = channelTypesByUID.get(uid);
            if (tp != null) {
                result.add(tp);
            }
        }
        return result;
    }

    /**
     * @see ChannelTypeRegistr#getChannelType(ChannelTypeUID, Locale)
     */
    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return channelTypesByUID.get(channelTypeUID);
    }

    @Override
    public @Nullable ChannelType getInternalChannelType(@Nullable ChannelTypeUID channelTypeUID) {
        return channelTypesByUID.get(channelTypeUID);
    }

    @Override
    public void invalidate() {
        channelTypesByUID.clear();
    }
}
