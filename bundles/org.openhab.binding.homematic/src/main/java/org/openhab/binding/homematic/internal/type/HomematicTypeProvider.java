/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.type;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionProvider;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.AbstractStorageBasedTypeProvider;
import org.openhab.core.thing.binding.ThingTypeProvider;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeProvider;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.ThingType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Manages custom thing types, channels, channel groups and config descriptions.
 * 
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
@Component(service = { HomematicTypeProvider.class, ThingTypeProvider.class, ChannelTypeProvider.class,
        ChannelGroupTypeProvider.class, ConfigDescriptionProvider.class })
public class HomematicTypeProvider extends AbstractStorageBasedTypeProvider implements ConfigDescriptionProvider {
    private Map<URI, ConfigDescription> configDescriptionsByURI = new HashMap<>();
    private Set<ThingTypeUID> thingTypesCreatedSinceStartup = new HashSet<>();
    private Set<ChannelTypeUID> channelTypesCreatedSinceStartup = new HashSet<>();
    private Set<ChannelGroupTypeUID> channelGroupTypesCreatedSinceStartup = new HashSet<>();

    @Activate
    public HomematicTypeProvider(@Reference StorageService storageService) {
        super(storageService);
    }

    @Override
    public void putThingType(ThingType thingType) {
        super.putThingType(thingType);
        thingTypesCreatedSinceStartup.add(thingType.getUID());
    }

    @Override
    public void putChannelType(ChannelType channelType) {
        super.putChannelType(channelType);
        channelTypesCreatedSinceStartup.add(channelType.getUID());
    }

    @Override
    public void putChannelGroupType(ChannelGroupType channelGroupType) {
        super.putChannelGroupType(channelGroupType);
        channelGroupTypesCreatedSinceStartup.add(channelGroupType.getUID());
    }

    @Override
    public Collection<ConfigDescription> getConfigDescriptions(@Nullable Locale locale) {
        return new ArrayList<>(configDescriptionsByURI.values());
    }

    @Override
    @Nullable
    public ConfigDescription getConfigDescription(URI uri, @Nullable Locale locale) {
        return configDescriptionsByURI.get(uri);
    }

    public @Nullable ThingType getThingTypeCreatedSinceStartup(ThingTypeUID thingTypeUID) {
        if (!thingTypesCreatedSinceStartup.contains(thingTypeUID)) {
            return null;
        }
        return getThingType(thingTypeUID, null);
    }

    public @Nullable ChannelType getChannelTypeCreatedSinceStartup(ChannelTypeUID channelTypeUID) {
        if (!channelTypesCreatedSinceStartup.contains(channelTypeUID)) {
            return null;
        }
        return getChannelType(channelTypeUID, null);
    }

    public @Nullable ChannelGroupType getChannelGroupTypeCreatedSinceStartup(ChannelGroupTypeUID channelGroupTypeUID) {
        if (!channelGroupTypesCreatedSinceStartup.contains(channelGroupTypeUID)) {
            return null;
        }
        return getChannelGroupType(channelGroupTypeUID, null);
    }

    public void putConfigDescription(ConfigDescription configDescription) {
        configDescriptionsByURI.put(configDescription.getUID(), configDescription);
    }
}
