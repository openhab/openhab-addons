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
package org.openhab.binding.netatmo.internal.providers;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.BINDING_ID;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.MeasureClass;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
@Component(service = { NetatmoChannelTypeProvider.class, ChannelTypeProvider.class })
public class NetatmoChannelTypeProvider implements ChannelTypeProvider {
    private final Map<ChannelTypeUID, ChannelType> channelTypes = new ConcurrentHashMap<>();

    public NetatmoChannelTypeProvider() {
        MeasureClass.asSet.forEach(measure -> {
            measure.channels.forEach((measureChannel, channelDetails) -> {
                ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, measureChannel);
                StateChannelTypeBuilder channelTypeBuilder = ChannelTypeBuilder
                        .state(channelTypeUID, measureChannel.replace("-", " "), channelDetails.itemType)
                        .withStateDescriptionFragment(StateDescriptionFragmentBuilder.create().withReadOnly(true)
                                .withPattern(channelDetails.pattern).build())
                        .withConfigDescriptionURI(channelDetails.configURI);

                channelTypes.put(channelTypeUID, channelTypeBuilder.build());
            });
        });
    }

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        if (channelTypes.containsKey(channelTypeUID)) {
            return channelTypes.get(channelTypeUID);
        } else {
            return null;
        }
    }
}
