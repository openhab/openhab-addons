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
package org.openhab.binding.modbus.internal;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.ModbusBindingConstants;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 * Generates ChannelTypes for the accepted item types of live poller2 channels.
 *
 * @author Sami Salonen - Initial contribution
 */
@Component(service = { ChannelTypeProvider.class, ModbusPoller2ChannelTypeProvider.class })
@NonNullByDefault
public class ModbusPoller2ChannelTypeProvider implements ChannelTypeProvider {
    private static final String SHIM_PREFIX = "poller2-shim-";

    private final Map<String, ChannelTypeUID> channelTypeUIDsByItemType = new ConcurrentHashMap<>();
    private final Map<ChannelTypeUID, ChannelType> channelTypes = new ConcurrentHashMap<>();

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return channelTypes.get(channelTypeUID);
    }

    public @Nullable ChannelTypeUID getGeneratedChannelTypeUID(@Nullable String itemType) {
        if (itemType == null || itemType.isBlank()) {
            return null;
        }
        return channelTypeUIDsByItemType.computeIfAbsent(itemType, this::createChannelType);
    }

    private ChannelTypeUID createChannelType(String itemType) {
        String id = SHIM_PREFIX
                + itemType.replace(':', '-').replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);
        ChannelTypeUID uid = new ChannelTypeUID(ModbusBindingConstants.BINDING_ID, id);
        channelTypes.putIfAbsent(uid, ChannelTypeBuilder.state(uid, itemType, itemType).build());
        return uid;
    }
}
