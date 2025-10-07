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
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.StateOption;
import org.openhab.core.util.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This class provides localized {@link StateOption}s
 *
 * @author Mike Jagdis - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = { DynamicStateDescriptionProvider.class, TuyaDynamicStateDescriptionProvider.class })
public class TuyaDynamicStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {

    private final Bundle bundle;

    @Activate
    public TuyaDynamicStateDescriptionProvider(final @Reference EventPublisher eventPublisher, //
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry, //
            final @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
        this.bundle = FrameworkUtil.getBundle(this.getClass());
    }

    public synchronized void addStateOption(ChannelUID channelUID, String option) {
        List<StateOption> newOptions;

        List<StateOption> options = channelOptionsMap.get(channelUID);
        if (options != null) {
            for (var o : options) {
                if (option.equals(o.getValue())) {
                    return;
                }
            }

            newOptions = new ArrayList<>(options.size() + 1);
            newOptions.addAll(options);
        } else {
            newOptions = new ArrayList<>(4);
        }

        newOptions.add(new StateOption(option, StringUtils.capitalizeByWhitespace(option.replaceAll("_", " "))));
        setStateOptions(channelUID, newOptions);
    }

    @Override
    protected List<StateOption> localizedStateOptions(List<StateOption> origOptions, Channel channel,
            @Nullable Locale locale) {

        ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService = this.channelTypeI18nLocalizationService;
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeI18nLocalizationService == null || channelTypeUID == null) {
            return origOptions;
        }

        // Options and commands are (somewhat) synonymous in Tuya so first translate as CommandOptions then
        // override with state translations.
        List<StateOption> options = TuyaDynamicCommandDescriptionProvider.localizedCommandOptions(bundle,
                channelTypeI18nLocalizationService,
                origOptions.stream().map(e -> new CommandOption(e.getValue(), e.getLabel())).toList(), channel, locale //
        ).stream().map(e -> new StateOption(e.getCommand().toLowerCase(), e.getLabel())).collect(Collectors.toList());

        String channelTypeId = channelTypeUID.getId();

        int i = channelTypeId.indexOf("_");
        if (i > 0 && i < channelTypeId.length()) {
            channelTypeId = channelTypeId.substring(i + 1);
        }

        ChannelTypeUID channelTypeUIDOriginal = new ChannelTypeUID(BINDING_ID, channelTypeId);
        ChannelTypeUID channelTypeUIDLower = new ChannelTypeUID(BINDING_ID, channelTypeId.toLowerCase());

        // Localize using the anonymous general channel type and lower-cased options
        // (e.g. using channel-type.tuya._.state.option.wibble = ...)
        options = channelTypeI18nLocalizationService.createLocalizedStateOptions(bundle, options,
                new ChannelTypeUID(BINDING_ID, "_"), locale);

        // Localize using lower-cased general channel types and options
        // (e.g. using channel-type.tuya.bar.state.option.wibble = ...)
        options = channelTypeI18nLocalizationService.createLocalizedStateOptions(bundle, options, channelTypeUIDLower,
                locale);

        // Localize using original-cased channel types and lower-cased options (e.g. using
        // channel-type.tuya.bAr.state.option.wibble = ...)
        options = channelTypeI18nLocalizationService.createLocalizedStateOptions(bundle, options,
                channelTypeUIDOriginal, locale);

        // Use those labels for the originally-cased states.
        for (int x = 0; x < options.size(); ++x) {
            options.set(x, new StateOption(origOptions.get(x).getValue(), options.get(x).getLabel()));
        }

        // Localize using lower-cased general channel types and original-cased options
        // (e.g. using channel-type.tuya.bar.state.option.WiBBle = ...)
        options = channelTypeI18nLocalizationService.createLocalizedStateOptions(bundle, options, channelTypeUIDLower,
                locale);

        // Localize using original-cased channel types and options (e.g. using
        // channel-type.tuya.bAr.state.option.WiBBle = ...)
        options = channelTypeI18nLocalizationService.createLocalizedStateOptions(bundle, options,
                channelTypeUIDOriginal, locale);

        // Override using product specific channel types (e.g. using
        // channel-type.tuya.productId_FOO_bAr.state.option.WiBBle = ...)
        options = channelTypeI18nLocalizationService.createLocalizedStateOptions(bundle, options, channelTypeUID,
                locale);

        return options;
    }
}
