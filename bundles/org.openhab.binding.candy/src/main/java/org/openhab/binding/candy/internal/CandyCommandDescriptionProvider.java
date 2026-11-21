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
package org.openhab.binding.candy.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.candy.internal.protocol.BiancaProgramCatalog.Program;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseDynamicCommandDescriptionProvider;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.DynamicCommandDescriptionProvider;
import org.openhab.core.types.CommandOption;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Supplies program choices from the catalog belonging to each locally matched appliance.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(service = { DynamicCommandDescriptionProvider.class, CandyCommandDescriptionProvider.class })
public class CandyCommandDescriptionProvider extends BaseDynamicCommandDescriptionProvider {
    @Activate
    public CandyCommandDescriptionProvider(BundleContext bundleContext, @Reference EventPublisher eventPublisher,
            @Reference ItemChannelLinkRegistry itemChannelLinkRegistry,
            @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.bundleContext = bundleContext;
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
    }

    public void setPrograms(ThingUID thing, List<Program> programs) {
        List<CommandOption> options = programs.stream().filter(program -> program.startPayload().isPresent())
                .map(program -> new CommandOption(program.id(), program.label())).toList();
        for (String channel : List.of(CandyBindingConstants.CHANNEL_START_PROGRAM,
                CandyBindingConstants.CHANNEL_SELECTED_PROGRAM)) {
            ChannelUID uid = new ChannelUID(thing, channel);
            setCommandOptions(uid, options);
            if (programs.isEmpty()) {
                channelOptionsMap.remove(uid);
            }
        }
    }
}
