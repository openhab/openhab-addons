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
import org.openhab.core.thing.binding.BaseDynamicCommandDescriptionProvider;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.DynamicCommandDescriptionProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Supplies the runtime command options for Apple TV channels whose values are only known once connected,
 * so a UI can offer the installed apps and the available user accounts as sendable commands.
 *
 * @author Dan Cunningham - Initial contribution
 */
@Component(service = { DynamicCommandDescriptionProvider.class, AtvDynamicCommandDescriptionProvider.class })
@NonNullByDefault
public class AtvDynamicCommandDescriptionProvider extends BaseDynamicCommandDescriptionProvider {

    @Activate
    public AtvDynamicCommandDescriptionProvider(final @Reference EventPublisher eventPublisher,
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
    public void removeCommandOptions(ChannelUID channelUID) {
        channelOptionsMap.remove(channelUID);
    }
}
