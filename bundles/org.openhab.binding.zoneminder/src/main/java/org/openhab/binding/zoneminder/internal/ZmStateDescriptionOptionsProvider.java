/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.zoneminder.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link ZmStateDescriptionOptionsProvider} is used to populate custom state options
 * on the Zoneminder channel(s).
 *
 * @author Mark Hilbush - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, ZmStateDescriptionOptionsProvider.class })
@NonNullByDefault
public class ZmStateDescriptionOptionsProvider extends BaseDynamicStateDescriptionProvider {

    @Activate
    public ZmStateDescriptionOptionsProvider(final @Reference EventPublisher eventPublisher, //
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry, //
            final @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
    }
}
