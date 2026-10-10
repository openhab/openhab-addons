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
package org.openhab.binding.smartthings.internal.ocf;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.StateDescriptionFragmentBuilder;

/**
 * Tests appliance description updates and lifecycle cleanup.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class ApplianceDescriptionProviderTest {
    private final EventPublisher publisher = mock(EventPublisher.class);
    private final ItemChannelLinkRegistry links = mock(ItemChannelLinkRegistry.class);
    private final ApplianceDescriptionProvider provider = new ApplianceDescriptionProvider(publisher, links);
    private final ThingUID thingUID = new ThingUID("smartthings:appliance:test");
    private final Channel channel = ChannelBuilder.create(new ChannelUID(thingUID, "temperature"), "Number:Temperature")
            .build();

    @Test
    void publishesOnlyChangedDescriptionsAndReplacesRemovedLimits() {
        var description = StateDescriptionFragmentBuilder.create().withMinimum(BigDecimal.TEN)
                .withMaximum(BigDecimal.valueOf(30)).withStep(BigDecimal.ONE).withPattern("%s °C").withReadOnly(false)
                .build();
        List<CommandOption> commands = List.of(new CommandOption("20", "20"));
        when(links.getLinkedItemNames(channel.getUID())).thenReturn(Set.of("Temperature"));
        provider.setDescription(channel.getUID(), description, commands);
        assertEquals(description.toStateDescription(), provider.getStateDescription(channel, null, null));
        assertEquals(commands, provider.getCommandDescription(channel, null, null).getCommandOptions());
        verify(publisher, times(2)).post(any());
        provider.setDescription(channel.getUID(), description, commands);
        verify(publisher, times(2)).post(any());
        var unavailable = StateDescriptionFragmentBuilder.create().withReadOnly(true).build();
        provider.setDescription(channel.getUID(), unavailable, List.of());
        assertNull(provider.getStateDescription(channel, null, null).getMinimum());
        assertTrue(provider.getStateDescription(channel, null, null).isReadOnly());
        assertTrue(provider.getCommandDescription(channel, null, null).getCommandOptions().isEmpty());
        verify(publisher, times(4)).post(any());
    }

    @Test
    void removesOnlyDescriptionsForRetiredChannelsOfTheSameThing() {
        Channel other = ChannelBuilder
                .create(new ChannelUID("smartthings:appliance:other:temperature"), "Number:Temperature").build();
        var description = StateDescriptionFragmentBuilder.create().withReadOnly(true).build();
        provider.setDescription(channel.getUID(), description, List.of());
        provider.setDescription(other.getUID(), description, List.of());
        provider.retainDescriptions(thingUID, Set.of(channel.getUID()));
        assertNotNull(provider.getStateDescription(channel, null, null));
        provider.retainDescriptions(thingUID, Set.of());
        assertNull(provider.getStateDescription(channel, null, null));
        assertNull(provider.getCommandDescription(channel, null, null));
        assertNotNull(provider.getStateDescription(other, null, null));
        assertNotNull(provider.getCommandDescription(other, null, null));
        provider.deactivate();
        assertNull(provider.getStateDescription(other, null, null));
        assertNull(provider.getCommandDescription(other, null, null));
    }
}
