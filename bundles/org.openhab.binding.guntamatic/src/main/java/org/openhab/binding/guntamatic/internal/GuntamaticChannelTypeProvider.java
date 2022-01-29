/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.guntamatic.internal;

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
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.service.component.annotations.Component;

/**
 * Provide channelTypes for Guntamatic Heating Systems
 *
 * @author Weger Michael - Initial contribution
 */
@Component(service = { ChannelTypeProvider.class, GuntamaticChannelTypeProvider.class })
@NonNullByDefault
public class GuntamaticChannelTypeProvider implements ChannelTypeProvider {
    private final Map<String, ChannelType> channelTypes = new ConcurrentHashMap<>();

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return channelTypes.get(channelTypeUID.getAsString()); // returns null if not found
    }

    public void addChannelType(ChannelTypeUID channelTypeUID, String label, String itemType, String description,
            boolean advanced, String pattern) {
        StateDescriptionFragmentBuilder stateDescriptionFragmentBuilder = StateDescriptionFragmentBuilder.create()
                .withReadOnly(true);
        if (!pattern.isEmpty()) {
            stateDescriptionFragmentBuilder.withPattern(pattern);
        }
        StateChannelTypeBuilder stateChannelTypeBuilder = ChannelTypeBuilder.state(channelTypeUID, label, itemType)
                .withDescription(description).isAdvanced(advanced)
                .withStateDescriptionFragment(stateDescriptionFragmentBuilder.build());
        channelTypes.put(channelTypeUID.getAsString(), stateChannelTypeBuilder.build());
    }
}
