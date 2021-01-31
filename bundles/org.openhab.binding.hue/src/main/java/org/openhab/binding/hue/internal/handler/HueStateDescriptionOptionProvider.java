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
package org.openhab.binding.hue.internal.handler;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Dynamic provider of state options for {@link HueBridgeHandler} while leaving other state description fields as
 * original.
 *
 * @author Hengrui Jiang - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, HueStateDescriptionOptionProvider.class })
@NonNullByDefault
public class HueStateDescriptionOptionProvider extends BaseDynamicStateDescriptionProvider {

    private final Map<ChannelUID, StateDescription> descriptions = new ConcurrentHashMap<>();

    @Activate
    public HueStateDescriptionOptionProvider(
            final @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
    }

    public void setDescription(ChannelUID channelUID, StateDescription description) {
        descriptions.put(channelUID, description);
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        StateDescription stateDescription = descriptions.get(channel.getUID());
        return stateDescription != null ? stateDescription
                : super.getStateDescription(channel, originalStateDescription, locale);
    }
}
