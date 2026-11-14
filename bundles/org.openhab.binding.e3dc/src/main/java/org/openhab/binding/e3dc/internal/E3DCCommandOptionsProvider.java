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
package org.openhab.binding.e3dc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseDynamicCommandDescriptionProvider;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.DynamicCommandDescriptionProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Offers the energy choices of the reserve and of the manual charge, which the power plant handler computes
 * from the values the device reports.
 *
 * @author Bernd Weymann - Initial contribution
 */
@Component(service = { DynamicCommandDescriptionProvider.class, E3DCCommandOptionsProvider.class })
@NonNullByDefault
public class E3DCCommandOptionsProvider extends BaseDynamicCommandDescriptionProvider {

    @Activate
    public E3DCCommandOptionsProvider(final @Reference EventPublisher eventPublisher, //
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry, //
            final @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
    }

    /** Drops the choices of a thing that is gone, so a thing created anew does not inherit them. */
    public void removeOptions(ThingUID thingUID) {
        channelOptionsMap.keySet().removeIf(channelUID -> channelUID.getThingUID().equals(thingUID));
    }
}
