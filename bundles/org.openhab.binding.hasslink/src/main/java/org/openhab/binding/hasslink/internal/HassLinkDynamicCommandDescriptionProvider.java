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
package org.openhab.binding.hasslink.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseDynamicCommandDescriptionProvider;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.DynamicCommandDescriptionProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides dynamic command descriptions for HassLink channels.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@Component(service = { DynamicCommandDescriptionProvider.class, HassLinkDynamicCommandDescriptionProvider.class })
@NonNullByDefault
public class HassLinkDynamicCommandDescriptionProvider extends BaseDynamicCommandDescriptionProvider {

    @Activate
    public HassLinkDynamicCommandDescriptionProvider(final @Reference EventPublisher eventPublisher,
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry,
            final @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
    }

    /**
     * Clean up the internal map entries for a channel when it is removed.
     *
     * @param channelUID the UID of the channel that has been removed
     */
    public void removeChannel(ChannelUID channelUID) {
        channelOptionsMap.remove(channelUID);
    }

    public void removeThing(ThingUID thingUID) {
        channelOptionsMap.keySet().removeIf(channelUID -> channelUID.getThingUID().equals(thingUID));
    }
}
