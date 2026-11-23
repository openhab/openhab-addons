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
package org.openhab.binding.lutron.internal.hw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.openhab.binding.lutron.internal.keypadconfig.KeypadConfigHw;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.State;

/**
 * Tests for {@link KeypadChannelSupport}, which does not depend on a Lutron protocol.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class KeypadChannelSupportTest {
    private static final ThingUID THING_UID = new ThingUID("lutron:hwkeypad:test:keypad");

    private static class Host implements KeypadChannelSupport.Host {
        Thing thing = ThingBuilder.create(new ThingTypeUID("lutron", "hwkeypad"), THING_UID)
                .withConfiguration(new Configuration(Map.of())).build();
        int thingUpdates;
        final List<String> triggers = new java.util.ArrayList<>();
        final Map<ChannelUID, State> states = new java.util.HashMap<>();

        @Override
        public Thing getThing() {
            return thing;
        }

        @Override
        public ThingBuilder editThing() {
            return ThingBuilder.create(thing.getThingTypeUID(), thing.getUID()).withChannels(thing.getChannels());
        }

        @Override
        public void updateThing(Thing thing) {
            this.thing = thing;
            thingUpdates++;
        }

        @Override
        public void triggerChannel(ChannelUID channelUID, String event) {
            triggers.add(channelUID.getId() + ":" + event);
        }

        @Override
        public void updateState(ChannelUID channelUID, State state) {
            states.put(channelUID, state);
        }
    }

    private final Host host = new Host();
    private final KeypadChannelSupport support = new KeypadChannelSupport(host,
            new org.openhab.core.thing.type.ChannelTypeUID("lutron", "buttonTrigger"),
            new org.openhab.core.thing.type.ChannelTypeUID("lutron", "hwLedState"), "String");
    private final List<KeypadComponent> components = new KeypadConfigHw().getComponents(KeypadConfigHw.GENERIC);

    @BeforeEach
    public void setUp() {
        support.configure(components, List.of());
    }

    @Test
    public void createsATriggerChannelPerButtonAndAStateChannelPerLed() {
        assertEquals(48, host.thing.getChannels().size());
        Channel button = host.thing.getChannel("button5");
        assertNotNull(button);
        assertEquals(ChannelKind.TRIGGER, button.getKind());
        assertEquals("lutron:buttonTrigger", String.valueOf(button.getChannelTypeUID()));
        Channel led = host.thing.getChannel("led5");
        assertNotNull(led);
        assertEquals(ChannelKind.STATE, led.getKind());
        assertEquals("String", led.getAcceptedItemType());
    }

    @Test
    public void channelsAreNotRebuiltWhenUnchanged() {
        assertEquals(1, host.thingUpdates);

        support.configure(components, List.of());

        assertEquals(1, host.thingUpdates);
    }

    @Test
    public void extraChannelsAreKept() {
        Channel extra = org.openhab.core.thing.binding.builder.ChannelBuilder
                .create(new ChannelUID(THING_UID, "press"), "String").build();

        support.configure(components, List.of(extra));

        assertEquals(49, host.thing.getChannels().size());
        assertNotNull(host.thing.getChannel("press"));
    }

    @Test
    public void looksUpChannelsAndComponents() {
        ChannelUID channel = support.channelFor(7);
        assertNotNull(channel);
        assertEquals("button7", channel.getId());
        assertEquals(7, support.componentFor(channel));
        assertNull(support.channelFor(99));
        assertNull(support.componentFor(new ChannelUID(THING_UID, "unknown")));
    }

    @Test
    public void triggersButtonsAndUpdatesLeds() {
        assertTrue(support.trigger(3, "PRESSED"));
        assertTrue(support.update(KeypadConfigHw.ledId(3), new StringType("FLASH1")));

        assertEquals(List.of("button3:PRESSED"), host.triggers);
        assertEquals(new StringType("FLASH1"), host.states.get(new ChannelUID(THING_UID, "led3")));
    }

    @Test
    public void unknownComponentsAreReported() {
        assertEquals(false, support.trigger(99, "PRESSED"));
        assertEquals(false, support.update(99, new StringType("ON")));
        assertTrue(host.triggers.isEmpty());
    }
}
