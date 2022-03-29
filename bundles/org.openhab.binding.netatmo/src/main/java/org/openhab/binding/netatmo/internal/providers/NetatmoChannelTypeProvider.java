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
package org.openhab.binding.netatmo.internal.providers;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.BINDING_ID;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.osgi.service.component.annotations.Component;

/**
 * Extends the ChannelTypeProvider generating Channel Types based on {@link MeasureClass} enum.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = { NetatmoChannelTypeProvider.class, ChannelTypeProvider.class })
public class NetatmoChannelTypeProvider implements ChannelTypeProvider {
    private final Collection<ChannelType> channelTypes = new HashSet<>();

    public NetatmoChannelTypeProvider() {
        MeasureClass.AS_SET.forEach(mc -> mc.channels.forEach((measureChannel, channelDetails) -> {
            StateChannelTypeBuilder channelTypeBuilder = ChannelTypeBuilder
                    .state(new ChannelTypeUID(BINDING_ID, measureChannel), measureChannel.replace("-", " "),
                            channelDetails.itemType)
                    .withStateDescriptionFragment(channelDetails.stateDescriptionFragment)
                    .withConfigDescriptionURI(channelDetails.configURI);

            channelTypes.add(channelTypeBuilder.build());
        }));
    }

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return Collections.unmodifiableCollection(channelTypes);
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return channelTypes.stream().filter(ct -> ct.getUID().equals(channelTypeUID)).findFirst().orElse(null);
    }
}
