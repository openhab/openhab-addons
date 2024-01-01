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
package org.openhab.binding.mqtt.generic;

import java.net.URI;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.internal.MqttThingHandlerFactory;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingTypeProvider;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeProvider;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeBuilder;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * An MQTT Extension might want to provide additional, dynamic channel types, based on auto-discovery.
 * <p>
 * Just retrieve the `MqttChannelTypeProvider` OSGi service and add (and remove) your channel types.
 * <p>
 * This provider is started on-demand only, as soon as {@link MqttThingHandlerFactory} or an extension requires it.
 *
 * @author David Graeff - Initial contribution
 *
 */
@NonNullByDefault
@Component(immediate = false, service = { ThingTypeProvider.class, ChannelTypeProvider.class,
        ChannelGroupTypeProvider.class, MqttChannelTypeProvider.class })
public class MqttChannelTypeProvider implements ThingTypeProvider, ChannelGroupTypeProvider, ChannelTypeProvider {
    private final ThingTypeRegistry typeRegistry;

    private final Map<ChannelTypeUID, ChannelType> types = new ConcurrentHashMap<>();
    private final Map<ChannelGroupTypeUID, ChannelGroupType> groups = new ConcurrentHashMap<>();
    private final Map<ThingTypeUID, ThingType> things = new ConcurrentHashMap<>();

    @Activate
    public MqttChannelTypeProvider(@Reference ThingTypeRegistry typeRegistry) {
        super();
        this.typeRegistry = typeRegistry;
    }

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return types.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return types.get(channelTypeUID);
    }

    @Override
    public @Nullable ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID,
            @Nullable Locale locale) {
        return groups.get(channelGroupTypeUID);
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(@Nullable Locale locale) {
        return groups.values();
    }

    @Override
    public Collection<ThingType> getThingTypes(@Nullable Locale locale) {
        return things.values();
    }

    public Set<ThingTypeUID> getThingTypeUIDs() {
        return things.keySet();
    }

    @Override
    public @Nullable ThingType getThingType(ThingTypeUID thingTypeUID, @Nullable Locale locale) {
        return things.get(thingTypeUID);
    }

    public void removeChannelType(ChannelTypeUID uid) {
        types.remove(uid);
    }

    public void removeChannelGroupType(ChannelGroupTypeUID uid) {
        groups.remove(uid);
    }

    public void setChannelGroupType(ChannelGroupTypeUID uid, ChannelGroupType type) {
        groups.put(uid, type);
    }

    public void setChannelType(ChannelTypeUID uid, ChannelType type) {
        types.put(uid, type);
    }

    public void removeThingType(ThingTypeUID uid) {
        things.remove(uid);
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
                .withProperties(baseType.getProperties()).isListed(false);

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
