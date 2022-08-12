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

package org.openhab.binding.shelly.internal.provider;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This class provides the list of valid inputs for the input channel of a source.
 *
 * @author Markus Michels - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = { DynamicStateDescriptionProvider.class, ShellyStateDescriptionProvider.class })
public class ShellyStateDescriptionProvider extends BaseDynamicStateDescriptionProvider implements ThingHandlerService {
    private @Nullable ShellyThingInterface handler;

    @Activate
    public ShellyStateDescriptionProvider(final @Reference EventPublisher eventPublisher, //
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry, //
            final @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (ShellyThingInterface) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return (ThingHandler) handler;
    }

    @SuppressWarnings("null")
    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {
        ChannelTypeUID uid = channel.getChannelTypeUID();
        if (uid != null && handler != null) {
            setStateOptions(channel.getUID(), handler.getStateOptions(uid));
        }
        return super.getStateDescription(channel, original, locale);
    }
}
