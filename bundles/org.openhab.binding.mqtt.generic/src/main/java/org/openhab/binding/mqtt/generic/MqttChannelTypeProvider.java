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
package org.openhab.binding.mqtt.generic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.mqtt.generic.internal.MqttThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

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
@Component(immediate = false, service = { ChannelTypeProvider.class, ChannelGroupTypeProvider.class,
        MqttChannelTypeProvider.class })
public class MqttChannelTypeProvider implements ChannelGroupTypeProvider, ChannelTypeProvider {
    private final Map<ChannelTypeUID, ChannelType> types = new HashMap<>();
    private final Map<ChannelGroupTypeUID, ChannelGroupType> groups = new HashMap<>();

    @Override
    public @Nullable Collection<@NonNull ChannelType> getChannelTypes(@Nullable Locale locale) {
        return types.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(@NonNull ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return types.get(channelTypeUID);
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
}
