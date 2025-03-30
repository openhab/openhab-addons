/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.emotiva.internal;

import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.BINDING_ID;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SOURCE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_ZONE2_SOURCE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.EMOTIVA_SOURCE_COMMAND_PREFIX;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;
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
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides the list of valid inputs for a source or audio mode.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Espen Fossen - Adapted to Emotiva binding
 */
@Component(service = { DynamicStateDescriptionProvider.class, EmotivaInputStateOptionProvider.class })
@NonNullByDefault
public class EmotivaInputStateOptionProvider extends BaseDynamicStateDescriptionProvider
        implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(EmotivaInputStateOptionProvider.class);

    private @Nullable EmotivaProcessorHandler handler;

    @Activate
    public EmotivaInputStateOptionProvider(final @Reference EventPublisher eventPublisher,
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry,
            final @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (EmotivaProcessorHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {
        ChannelTypeUID typeUID = channel.getChannelTypeUID();
        if (typeUID == null || !BINDING_ID.equals(typeUID.getBindingId())) {
            return null;
        }

        List<StateOption> options = new ArrayList<>();
        EmotivaProcessorHandler localHandler = handler;
        if (localHandler != null) {
            if (channel.getUID().getId().equals(CHANNEL_SOURCE)) {
                options.addAll(gatherStateOptionsForSource(channel, localHandler.getSourcesMainZone()));
            } else if (channel.getUID().getId().equals(CHANNEL_ZONE2_SOURCE)) {
                options.addAll(gatherStateOptionsForSource(channel, localHandler.getSourcesZone2()));
            } else if (channel.getUID().getId().equals(CHANNEL_MODE)) {
                EnumMap<EmotivaSubscriptionTags, String> modes = localHandler.getModes();
                Collection<EmotivaSubscriptionTags> modeKeys = modes.keySet();
                for (EmotivaSubscriptionTags modeKey : modeKeys) {
                    options.add(new StateOption(modeKey.name(), modes.get(modeKey)));
                }
                logger.trace("Updating OH channel '{}' with state options '{}'", CHANNEL_MODE, options);
            }
            setStateOptions(channel.getUID(), options);
        }
        return super.getStateDescription(channel, original, locale);
    }

    List<StateOption> gatherStateOptionsForSource(Channel channel,
            EnumMap<EmotivaControlCommands, String> sourcesWithUserLabels) {
        List<StateOption> options = new ArrayList<>();
        Collection<EmotivaControlCommands> sourceCommands = sourcesWithUserLabels.keySet();
        for (EmotivaControlCommands command : sourceCommands) {
            // If command name starts with prefix "source_", add the user provided label, else use default label from
            // command.
            if (command.name().startsWith(EMOTIVA_SOURCE_COMMAND_PREFIX)) {
                options.add(new StateOption(command.name(), sourcesWithUserLabels.get(command)));
            } else {
                options.add(new StateOption(command.name(), command.getLabel()));
            }
        }
        logger.trace("Updating OH channel '{}' with state options '{}'", channel.getUID().getId(), options);
        return options;
    }
}
