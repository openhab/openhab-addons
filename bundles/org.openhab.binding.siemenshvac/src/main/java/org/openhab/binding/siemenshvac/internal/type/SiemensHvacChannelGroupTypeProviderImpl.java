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
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeProvider;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 * Provides all ChannelGroupTypes from all SiemensHvac bridges.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
@Component(service = { SiemensHvacChannelGroupTypeProvider.class, ChannelGroupTypeProvider.class })
public class SiemensHvacChannelGroupTypeProviderImpl implements SiemensHvacChannelGroupTypeProvider {

    private final Map<ChannelGroupTypeUID, ChannelGroupType> channelGroupTypesByUID = new HashMap<>();

    @Override
    public @Nullable ChannelGroupType getInternalChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID) {
        return channelGroupTypesByUID.get(channelGroupTypeUID);
    }

    @Override
    public void addChannelGroupType(ChannelGroupType channelGroupType) {
        channelGroupTypesByUID.put(channelGroupType.getUID(), channelGroupType);
    }

    @Override
    public @Nullable ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID,
            @Nullable Locale locale) {
        return channelGroupTypesByUID.get(channelGroupTypeUID);
    }

    /**
     *
     * @see ChannelTypeRegistr#getChannelGroupTypes(Locale)
     *
     */
    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(@Nullable Locale locale) {
        Collection<ChannelGroupType> result = new ArrayList<>();
        for (ChannelGroupTypeUID uid : channelGroupTypesByUID.keySet()) {
            ChannelGroupType groupType = channelGroupTypesByUID.get(uid);
            if (groupType != null) {
                result.add(groupType);
            }
        }
        return result;
    }

    @Override
    public void invalidate() {
        channelGroupTypesByUID.clear();
    }
}
