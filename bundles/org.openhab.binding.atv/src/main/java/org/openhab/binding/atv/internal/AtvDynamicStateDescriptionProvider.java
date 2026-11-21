/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Supplies the runtime state options for Apple TV channels whose values are only known once connected,
 * such as the installed apps and the available user accounts. The options give the current value a
 * friendly display label; the matching sendable commands come from
 * {@link AtvDynamicCommandDescriptionProvider}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, AtvDynamicStateDescriptionProvider.class })
@NonNullByDefault
public class AtvDynamicStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {

    @Activate
    public AtvDynamicStateDescriptionProvider(final @Reference EventPublisher eventPublisher,
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry,
            final @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
    }

    /**
     * Drops the options previously supplied for a channel, so a removed Thing leaves nothing behind.
     *
     * @param channelUID channel to forget
     */
    public void removeStateOptions(ChannelUID channelUID) {
        channelOptionsMap.remove(channelUID);
    }
}
