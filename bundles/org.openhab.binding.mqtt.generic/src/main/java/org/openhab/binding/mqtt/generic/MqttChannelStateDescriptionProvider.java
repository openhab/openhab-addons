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
package org.openhab.binding.mqtt.generic;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.internal.MqttThingHandlerFactory;
import org.openhab.binding.mqtt.generic.internal.handler.GenericMQTTThingHandler;
import org.openhab.core.i18n.I18nUtil;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.DynamicCommandDescriptionProvider;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.CommandDescription;
import org.openhab.core.types.CommandDescriptionBuilder;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.util.BundleResolver;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If the user configures a generic channel and defines for example minimum/maximum/readonly,
 * we need to dynamically override the xml default state.
 * This service is started on-demand only, as soon as {@link MqttThingHandlerFactory} requires it.
 *
 * It is filled with new state descriptions within the {@link GenericMQTTThingHandler}.
 *
 * @author David Graeff - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, DynamicCommandDescriptionProvider.class,
        MqttChannelStateDescriptionProvider.class })
@NonNullByDefault
public class MqttChannelStateDescriptionProvider
        implements DynamicStateDescriptionProvider, DynamicCommandDescriptionProvider {

    private final Map<ChannelUID, StateDescription> stateDescriptions = new ConcurrentHashMap<>();
    private final Map<ChannelUID, CommandDescription> commandDescriptions = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(MqttChannelStateDescriptionProvider.class);

    private final TranslationProvider i18nProvider;
    private final BundleResolver bundleResolver;

    @Activate
    public MqttChannelStateDescriptionProvider(@Reference TranslationProvider i18nProvider,
            @Reference BundleResolver bundleResolver) {
        this.i18nProvider = i18nProvider;
        this.bundleResolver = bundleResolver;
    }

    /**
     * Set a state description for a channel. This description will be used when preparing the channel state by
     * the framework for presentation. A previous description, if existed, will be replaced.
     *
     * @param channelUID channel UID
     * @param description state description for the channel
     */
    public void setDescription(ChannelUID channelUID, StateDescription description) {
        logger.debug("Adding state description for channel {}", channelUID);
        stateDescriptions.put(channelUID, description);
    }

    /**
     * Set a command description for a channel.
     * A previous description, if existed, will be replaced.
     *
     * @param channelUID channel UID
     * @param description command description for the channel
     */
    public void setDescription(ChannelUID channelUID, CommandDescription description) {
        logger.debug("Adding state description for channel {}", channelUID);
        commandDescriptions.put(channelUID, description);
    }

    /**
     * Clear all registered state descriptions
     */
    public void removeAllDescriptions() {
        logger.debug("Removing all descriptions");
        stateDescriptions.clear();
        commandDescriptions.clear();
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        StateDescription description = stateDescriptions.get(channel.getUID());
        if (description != null) {
            logger.trace("Providing state description for channel {}", channel.getUID());
            if (description.getOptions().stream().anyMatch(option -> I18nUtil.isConstant(option.getLabel()))) {
                StateDescriptionFragmentBuilder builder = StateDescriptionFragmentBuilder.create(description);
                builder.withOptions(description.getOptions().stream().map(option -> {
                    return new StateOption(option.getValue(), translateLabel(option.getLabel(), locale));
                }).collect(Collectors.toList()));
                description = builder.build().toStateDescription();
            }
        }
        return description;
    }

    @Override
    public @Nullable CommandDescription getCommandDescription(Channel channel,
            @Nullable CommandDescription originalCommandDescription, @Nullable Locale locale) {
        CommandDescription description = commandDescriptions.get(channel.getUID());
        if (description != null) {
            logger.trace("Providing command description for channel {}", channel.getUID());
            if (description.getCommandOptions().stream().anyMatch(option -> I18nUtil.isConstant(option.getLabel()))) {
                CommandDescriptionBuilder builder = CommandDescriptionBuilder.create();
                builder.withCommandOptions(description.getCommandOptions().stream().map(option -> {
                    return new CommandOption(option.getCommand(), translateLabel(option.getLabel(), locale));
                }).collect(Collectors.toList()));
                description = builder.build();
            }
        }
        return description;
    }

    /**
     * Removes the given channel description.
     *
     * @param channel The channel
     */
    public void remove(ChannelUID channel) {
        stateDescriptions.remove(channel);
        commandDescriptions.remove(channel);
    }

    private @Nullable String translateLabel(@Nullable String label, @Nullable Locale locale) {
        if (label == null) {
            return null;
        }
        if (!I18nUtil.isConstant(label)) {
            return label;
        }
        Bundle bundle = bundleResolver.resolveBundle(getClass());

        String translatedLabel = i18nProvider.getText(bundle, I18nUtil.stripConstant(label), null, locale);
        if (translatedLabel != null) {
            return translatedLabel;
        }
        return label;
    }
}
