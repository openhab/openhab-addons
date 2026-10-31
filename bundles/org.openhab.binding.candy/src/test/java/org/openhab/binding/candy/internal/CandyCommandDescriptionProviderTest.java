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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.candy.internal.protocol.BiancaProgramCatalog.Program;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Verifies appliance-specific command choices and removal through the framework description API.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
class CandyCommandDescriptionProviderTest {
    @Test
    void exposesOnlyStartableChoicesAndRemovesThemWhenTheHandlerStops() {
        BundleContext context = mock(BundleContext.class);
        when(context.getBundle()).thenReturn(mock(Bundle.class));
        var i18n = mock(ChannelTypeI18nLocalizationService.class);
        when(i18n.createLocalizedCommandOptions(any(), anyList(), any(), any()))
                .thenAnswer(call -> call.getArgument(1));
        var provider = new CandyCommandDescriptionProvider(context, mock(EventPublisher.class),
                mock(ItemChannelLinkRegistry.class), i18n);
        ThingUID thing = new ThingUID("candy:bianca:test");
        var channel = ChannelBuilder.create(new ChannelUID(thing, "selected-program"), "String")
                .withType(new ChannelTypeUID("candy", "selected-program")).build();
        provider.setPrograms(thing, List.of(new Program("cottons", "Cottons", Map.of(), Optional.of("payload")),
                new Program("off", "Off", Map.of(), Optional.empty())));
        var description = provider.getCommandDescription(channel, null, Locale.ENGLISH);
        assertNotNull(description);
        assertEquals(1, description.getCommandOptions().size());
        assertEquals("cottons", description.getCommandOptions().getFirst().getCommand());
        provider.setPrograms(thing, List.of());
        assertNull(provider.getCommandDescription(channel, null, Locale.ENGLISH));
        provider.deactivate();
    }
}
