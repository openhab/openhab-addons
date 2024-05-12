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
package org.openhab.binding.mercedesme.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamic provider of state options while leaving other state description fields as original.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(service = { DynamicStateDescriptionProvider.class, MercedesMeStateOptionProvider.class })
public class MercedesMeStateOptionProvider extends BaseDynamicStateDescriptionProvider {
    private final Logger logger = LoggerFactory.getLogger(MercedesMeStateOptionProvider.class);

    @Activate
    public MercedesMeStateOptionProvider(final @Reference EventPublisher eventPublisher, //
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry, //
            final @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
    }

    @Override
    public void setStateOptions(ChannelUID cuid, List<StateOption> options) {
        super.setStateOptions(cuid, options);
        logger.warn("{} state options {}", cuid.getAsString(), options);
    }

    @Override
    public void setStatePattern(ChannelUID channelUID, String pattern) {
        super.setStatePattern(channelUID, pattern);
        logger.warn("{} pattern {}", channelUID.getAsString(), pattern);
    }
}
