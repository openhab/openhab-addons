/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.type;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 * Implementation of {@link NeeoChannelTypeProvider} that will store {@link ChannelType} and {@link ChannelGroupType} in
 * a list.
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
@Component(immediate = true, service = { ChannelTypeProvider.class, NeeoChannelTypeProvider.class })
public class NeeoChannelTypeProviderImpl implements NeeoChannelTypeProvider {
    /** The list of {@link ChannelType } */
    private final Map<ChannelTypeUID, ChannelType> channelTypes = new ConcurrentHashMap<>();

    /** The list of {@link ChannelGroupType } */
    private final Map<ChannelGroupTypeUID, ChannelGroupType> channelGroupTypes = new ConcurrentHashMap<>();

    @Nullable
    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes.values();
    }

    @Nullable
    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return channelTypes.get(channelTypeUID);
    }

    @Nullable
    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, @Nullable Locale locale) {
        return channelGroupTypes.get(channelGroupTypeUID);
    }

    @Nullable
    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(@Nullable Locale locale) {
        return channelGroupTypes.values();
    }

    @Override
    public void addChannelTypes(List<ChannelType> channelTypes) {
        for (ChannelType ct : channelTypes) {
            this.channelTypes.put(ct.getUID(), ct);
        }
    }

    @Override
    public void addChannelGroupTypes(List<ChannelGroupType> groupTypes) {
        for (ChannelGroupType cgt : groupTypes) {
            this.channelGroupTypes.put(cgt.getUID(), cgt);
        }
    }
}
