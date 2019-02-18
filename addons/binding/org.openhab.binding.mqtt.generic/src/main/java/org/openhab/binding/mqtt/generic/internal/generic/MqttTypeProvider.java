/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.generic.internal.generic;


import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.openhab.binding.mqtt.generic.internal.MqttThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * If the user configures a channel and defines for example minimum/maximum values, we need a specific
 * channel type provider. This one is started on-demand only, as soon as {@link MqttThingHandlerFactory} requires it.
 *
 * It is filled with types within the different handlers ({@link HomieThingHandler, @link HomeAssistantThingHandler}
 * on-demand.
 *
 * @author David Graeff - Initial contribution
 *
 */
@NonNullByDefault
@Component(immediate = false, service = { ThingTypeProvider.class, ChannelTypeProvider.class,
        ChannelGroupTypeProvider.class, MqttTypeProvider.class })
public class MqttTypeProvider implements ThingTypeProvider, ChannelGroupTypeProvider, ChannelTypeProvider {
    private @NonNullByDefault({}) ThingTypeRegistry typeRegistry;

    private final Map<ChannelTypeUID, ChannelType> channels = new HashMap<>();
    private final Map<ChannelGroupTypeUID, ChannelGroupType> groups = new HashMap<>();
    private final Map<ThingTypeUID, ThingType> things = new HashMap<>();

    @Reference
    protected void setTypeRegistry(ThingTypeRegistry provider) {
        this.typeRegistry = provider;
    }

    protected void unsetTypeRegistry(ThingTypeRegistry provider) {
        this.typeRegistry = null;
    }

    @Override
    public @Nullable Collection<@NonNull ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channels.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(@NonNull ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return channels.get(channelTypeUID);
    }

    @Override
    public @Nullable ChannelGroupType getChannelGroupType(@NonNull ChannelGroupTypeUID channelGroupTypeUID,
            @Nullable Locale locale) {
        return groups.get(channelGroupTypeUID);
    }

    @Override
    public @Nullable Collection<@NonNull ChannelGroupType> getChannelGroupTypes(@Nullable Locale locale) {
        return groups.values();
    }

    @Override
    public Collection<ThingType> getThingTypes(@Nullable Locale locale) {
        return things.values();
    }

    @Override
    public @Nullable ThingType getThingType(ThingTypeUID thingTypeUID, @Nullable Locale locale) {
        return things.get(thingTypeUID);
    }

    public void removeChannelType(ChannelTypeUID uid) {
        channels.remove(uid);
    }

    public void removeChannelGroupType(ChannelGroupTypeUID uid) {
        groups.remove(uid);
    }

    public void removeThingType(ThingTypeUID uid) {
        things.remove(uid);
    }

    public void setChannelGroupType(ChannelGroupTypeUID uid, ChannelGroupType type) {
        groups.put(uid, type);
    }

    public void setChannelType(ChannelTypeUID uid, ChannelType type) {
        channels.put(uid, type);
    }

    public void setThingType(ThingTypeUID uid, ThingType type) {
        things.put(uid, type);
    }

    public void setThingTypeIfAbsent(ThingTypeUID uid, ThingType type) {
        things.putIfAbsent(uid, type);
    }

    public ThingTypeBuilder derive(ThingTypeUID newTypeId, ThingTypeUID baseTypeId) {
        ThingType baseType = typeRegistry.getThingType(baseTypeId);

        ThingTypeBuilder result = ThingTypeBuilder.instance(newTypeId, baseType.getLabel())
                .withChannelGroupDefinitions(baseType.getChannelGroupDefinitions())
                .withChannelDefinitions(baseType.getChannelDefinitions())
                .withExtensibleChannelTypeIds(baseType.getExtensibleChannelTypeIds())
                .withSupportedBridgeTypeUIDs(baseType.getSupportedBridgeTypeUIDs())
                .withProperties(baseType.getProperties()).isListed(baseType.isListed());

        String representationProperty = baseType.getRepresentationProperty();
        if (representationProperty != null) {
            result = result.withRepresentationProperty(representationProperty);
        }
        URI configDescriptionURI = baseType.getConfigDescriptionURI();
        if (configDescriptionURI != null) {
            result = result.withConfigDescriptionURI(configDescriptionURI);
        }
        String category = baseType.getCategory();
        if (category != null) {
            result = result.withCategory(category);
        }
        String description = baseType.getDescription();
        if (description != null) {
            result = result.withDescription(description);
        }

        return result;
    }
}
