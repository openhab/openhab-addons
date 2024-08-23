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
package org.openhab.binding.tr064.internal;

import static org.openhab.binding.tr064.internal.Tr064BindingConstants.CHANNEL_TYPES;

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
 * The {@link Tr064ChannelTypeProvider} is used for providing dynamic channel types
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(service = { ChannelTypeProvider.class, Tr064ChannelTypeProvider.class })
public class Tr064ChannelTypeProvider implements ChannelTypeProvider {
    private final Map<ChannelTypeUID, ChannelType> channelTypeMap = new ConcurrentHashMap<>();

    public Tr064ChannelTypeProvider() {
        CHANNEL_TYPES.forEach(channelTypeDescription -> {
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(Tr064BindingConstants.BINDING_ID,
                    channelTypeDescription.getName());
            // create state description
            StateDescriptionFragmentBuilder stateDescriptionFragmentBuilder = StateDescriptionFragmentBuilder.create()
                    .withReadOnly(channelTypeDescription.getSetAction() == null);
            if (channelTypeDescription.getItem().getStatePattern() != null) {
                stateDescriptionFragmentBuilder.withPattern(channelTypeDescription.getItem().getStatePattern());
            }

            // create channel type
            ChannelTypeBuilder<StateChannelTypeBuilder> channelTypeBuilder = ChannelTypeBuilder
                    .state(channelTypeUID, channelTypeDescription.getLabel(),
                            channelTypeDescription.getItem().getType())
                    .withStateDescriptionFragment(stateDescriptionFragmentBuilder.build())
                    .isAdvanced(channelTypeDescription.isAdvanced());
            if (channelTypeDescription.getDescription() != null) {
                channelTypeBuilder.withDescription(channelTypeDescription.getDescription());
            }

            channelTypeMap.put(channelTypeUID, channelTypeBuilder.build());
        });
    }

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypeMap.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return channelTypeMap.get(channelTypeUID);
    }
}
