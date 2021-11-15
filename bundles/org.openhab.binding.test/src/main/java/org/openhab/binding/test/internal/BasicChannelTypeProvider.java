/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.test.internal;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 * Provide channelTypes for Guntamatic Heating Systems
 *
 * @author Weger Michael - Initial contribution
 */
@Component(service = { ChannelTypeProvider.class, BasicChannelTypeProvider.class })
@NonNullByDefault
public class BasicChannelTypeProvider implements ChannelTypeProvider {
    private final Map<String, ChannelType> channelTypes = new ConcurrentHashMap<>();

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        if (channelTypes.containsKey(channelTypeUID.getAsString())) {
            return channelTypes.get(channelTypeUID.getAsString());
        }
        return null;
    }

    public void addChannelType(ChannelTypeUID channelTypeUID, String label, String itemType, String description,
            boolean advanced​) {
        // try {
        // ChannelTypeUID channelTypeUID2 = new ChannelTypeUID(BINDING_ID, "test-channel");
        ChannelTypeBuilder channelTypeBuilder = ChannelTypeBuilder.state(channelTypeUID, label, itemType)
                .isAdvanced(advanced​).withDescription(description);
        channelTypes.put(channelTypeUID.getAsString(), channelTypeBuilder.build());
        // } catch (Exception e) {
        // logger.warn("Failed creating channelType {}: {} ", channelTypeUID2, e.getMessage());
        // }
    }
}
