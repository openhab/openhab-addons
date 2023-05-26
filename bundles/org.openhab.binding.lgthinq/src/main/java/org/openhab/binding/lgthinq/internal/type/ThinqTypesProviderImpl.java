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
package org.openhab.binding.lgthinq.internal.type;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingTypeProvider;
import org.openhab.core.thing.type.*;
import org.osgi.service.component.annotations.Component;

/**
 * Provider class to provide model types for custom things (not in XML).
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
@Component(service = { ThinqChannelTypeProvider.class, ChannelTypeProvider.class, ChannelGroupTypeProvider.class,
        ThinqChannelGroupTypeProvider.class, ThinqConfigDescriptionProvider.class, ConfigDescriptionProvider.class,
        ThinqThingTypeProvider.class, ThingTypeProvider.class })
public class ThinqTypesProviderImpl implements ThinqChannelTypeProvider, ThinqChannelGroupTypeProvider,
        ThinqConfigDescriptionProvider, ThinqThingTypeProvider {

    private final Map<ThingTypeUID, ThingType> thingTypesByUID = new ConcurrentHashMap<>();
    private final Map<ChannelTypeUID, ChannelType> channelTypesByUID = new ConcurrentHashMap<>();
    private final Map<ChannelGroupTypeUID, ChannelGroupType> channelGroupTypesByUID = new ConcurrentHashMap<>();

    private final Map<URI, ConfigDescription> configDescriptionsByURI = new ConcurrentHashMap<>();

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable final Locale locale) {
        return Collections.unmodifiableCollection(channelTypesByUID.values());
    }

    @Override
    public @Nullable ChannelType getChannelType(final ChannelTypeUID channelTypeUID, @Nullable final Locale locale) {
        return channelTypesByUID.get(channelTypeUID);
    }

    /**
     * Add a channel type for a user configured channel.
     *
     * @param channelType channelType
     */
    @Override
    public void addChannelType(final ChannelType channelType) {
        channelTypesByUID.put(channelType.getUID(), channelType);
    }

    @Override
    @Nullable
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, @Nullable Locale locale) {
        return channelGroupTypesByUID.get(channelGroupTypeUID);
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(@Nullable Locale locale) {
        return Collections.unmodifiableCollection(channelGroupTypesByUID.values());
    }

    @Override
    public void addChannelGroupType(ChannelGroupType channelGroupType) {
        channelGroupTypesByUID.put(channelGroupType.getUID(), channelGroupType);
    }

    @Override
    public void removeChannelGroupType(ChannelGroupType channelGroupType) {
        channelGroupTypesByUID.remove(channelGroupType.getUID());
    }

    @Override
    public List<ChannelGroupType> internalGroupTypes() {
        return new ArrayList<>(channelGroupTypesByUID.values());
    }

    @Override
    public void addConfigDescription(ConfigDescription configDescription) {
        configDescriptionsByURI.put(configDescription.getUID(), configDescription);
    }

    @Override
    public Collection<ConfigDescription> getConfigDescriptions(@Nullable Locale locale) {
        return Collections.unmodifiableCollection(configDescriptionsByURI.values());
    }

    @Override
    public @Nullable ConfigDescription getConfigDescription(URI uri, @Nullable Locale locale) {
        return configDescriptionsByURI.get(uri);
    }

    @Override
    public void addThingType(ThingType thingType) {
        thingTypesByUID.put(thingType.getUID(), thingType);
    }

    @Override
    public Collection<ThingType> getThingTypes(@Nullable Locale locale) {
        return Collections.unmodifiableCollection(thingTypesByUID.values());
    }

    @Override
    public @Nullable ThingType getThingType(ThingTypeUID thingTypeUID, @Nullable Locale locale) {
        return thingTypesByUID.get(thingTypeUID);
    }
}
