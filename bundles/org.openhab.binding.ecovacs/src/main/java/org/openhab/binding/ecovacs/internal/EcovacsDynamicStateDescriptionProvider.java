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
package org.openhab.binding.ecovacs.internal;

import static org.openhab.binding.ecovacs.internal.EcovacsBindingConstants.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateOption;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
@Component(service = { DynamicStateDescriptionProvider.class, EcovacsDynamicStateDescriptionProvider.class })
public class EcovacsDynamicStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {
    private final TranslationProvider i18nProvider;

    @Activate
    public EcovacsDynamicStateDescriptionProvider(final @Reference EventPublisher eventPublisher,
            final @Reference TranslationProvider i18nProvider,
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry,
            final @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.eventPublisher = eventPublisher;
        this.i18nProvider = i18nProvider;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
    }

    @Override
    protected List<StateOption> localizedStateOptions(List<StateOption> options, Channel channel,
            @Nullable Locale locale) {
        @Nullable
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        String channelTypeId = channelTypeUID != null ? channelTypeUID.getId() : "";
        if (CHANNEL_TYPE_ID_CLEAN_MODE.equals(channelTypeId) || CHANNEL_TYPE_ID_LAST_CLEAN_MODE.equals(channelTypeId)) {
            final Bundle bundle = bundleContext.getBundle();
            return options.stream().map(opt -> {
                String key = "ecovacs.cleaning-mode." + opt.getValue();
                String label = this.i18nProvider.getText(bundle, key, opt.getLabel(), locale);
                return new StateOption(opt.getValue(), label);
            }).collect(Collectors.toList());
        }
        return super.localizedStateOptions(options, channel, locale);
    }
}
