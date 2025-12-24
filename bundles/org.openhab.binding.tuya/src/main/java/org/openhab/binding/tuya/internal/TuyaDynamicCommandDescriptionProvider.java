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
package org.openhab.binding.tuya.internal;

import static org.openhab.binding.tuya.internal.TuyaBindingConstants.BINDING_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.BaseDynamicCommandDescriptionProvider;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.DynamicCommandDescriptionProvider;
import org.openhab.core.types.CommandOption;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This class provides the list of valid commands for dynamic channels.
 *
 * @author Cody Cutrer - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = { DynamicCommandDescriptionProvider.class, TuyaDynamicCommandDescriptionProvider.class })
public class TuyaDynamicCommandDescriptionProvider extends BaseDynamicCommandDescriptionProvider {

    private final Bundle bundle;

    @Activate
    public TuyaDynamicCommandDescriptionProvider(final @Reference EventPublisher eventPublisher, //
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry, //
            final @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
        this.bundle = FrameworkUtil.getBundle(this.getClass());
    }

    // N.B. It would be better to override getCommandDescription, parse the channel type and
    // use "range" from the schema here rather than having the Thing handler set the command
    // list to range on every channel. We don't because we want to support old pre-existing
    // Things which don't have distinct channel types and thus there is no way for us to get
    // the schema for them.

    @Override
    protected List<CommandOption> localizedCommandOptions(List<CommandOption> options, Channel channel,
            @Nullable Locale locale) {
        return TuyaDynamicCommandDescriptionProvider.localizedCommandOptions(bundle, channelTypeI18nLocalizationService,
                options, channel, locale);
    }

    public static List<CommandOption> localizedCommandOptions(Bundle bundle,
            @Nullable ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService,
            List<CommandOption> origOptions, Channel channel, @Nullable Locale locale) {
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeI18nLocalizationService == null || channelTypeUID == null) {
            return origOptions;
        }

        String channelTypeId = channelTypeUID.getId();

        int i = channelTypeId.indexOf("_");
        if (i > 0 && i < channelTypeId.length()) {
            channelTypeId = channelTypeId.substring(i + 1);
        }

        ChannelTypeUID channelTypeUIDOriginal = new ChannelTypeUID(BINDING_ID, channelTypeId);
        ChannelTypeUID channelTypeUIDLower = new ChannelTypeUID(BINDING_ID, channelTypeId.toLowerCase());

        List<CommandOption> options = new ArrayList<>(origOptions.size());
        for (CommandOption option : origOptions) {
            options.add(new CommandOption(option.getCommand().toLowerCase(), option.getLabel()));
        }

        // Localize using the anonymous general channel type and lower-cased options
        // (e.g. using channel-type.tuya._.command.option.wibble = ...)
        options = channelTypeI18nLocalizationService.createLocalizedCommandOptions(bundle, options,
                new ChannelTypeUID(BINDING_ID, "_"), locale);

        // Localize using lower-cased general channel types and options
        // (e.g. using channel-type.tuya.bar.command.option.wibble = ...)
        options = channelTypeI18nLocalizationService.createLocalizedCommandOptions(bundle, options, channelTypeUIDLower,
                locale);

        // Localize using original-cased channel types and lower-cased options (e.g. using
        // channel-type.tuya.bAr.command.option.wibble = ...)
        options = channelTypeI18nLocalizationService.createLocalizedCommandOptions(bundle, options,
                channelTypeUIDOriginal, locale);

        // Use those labels for the originally-cased commands.
        for (int x = 0; x < options.size(); ++x) {
            options.set(x, new CommandOption(origOptions.get(x).getCommand(), options.get(x).getLabel()));
        }

        // Localize using lower-cased general channel types and original-cased options
        // (e.g. using channel-type.tuya.bar.command.option.WiBBle = ...)
        options = channelTypeI18nLocalizationService.createLocalizedCommandOptions(bundle, options, channelTypeUIDLower,
                locale);

        // Localize using original-cased channel types and options (e.g. using
        // channel-type.tuya.bAr.command.option.WiBBle = ...)
        options = channelTypeI18nLocalizationService.createLocalizedCommandOptions(bundle, options,
                channelTypeUIDOriginal, locale);

        // Override using product specific channel types (e.g. using
        // channel-type.tuya.productId_FOO_bAr.command.option.WiBBle = ...)
        options = channelTypeI18nLocalizationService.createLocalizedCommandOptions(bundle, options, channelTypeUID,
                locale);

        return options;
    }
}
