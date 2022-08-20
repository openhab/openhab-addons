/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.tado.tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tado.internal.TadoBindingConstants;
import org.openhab.binding.tado.internal.TadoTranslationProvider;
import org.openhab.binding.tado.internal.builder.ZoneChannelBuilder;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.framework.Bundle;

/**
 * The {@link ZoneChannelBuilderTest} implements tests of the channel builder.
 *
 * @author Andrew Fiddian-Green - Initial contributions
 *
 */
@NonNullByDefault
public class ZoneChannelBuilderTest {

    private static final TadoTranslationProvider TRANSLATION_PROVIDER = new TadoTranslationProvider(mock(Bundle.class),
            new MockedTranslationProvider(), new MockedLocaleProvider());

    private final Thing zone = ThingBuilder.create(TadoBindingConstants.THING_TYPE_ZONE, "test").build();

    /**
     * Test positive functionality of a channel builder
     */
    @Test
    void testChannelBuilder() {
        // @formatter:off
        ZoneChannelBuilder channelBuilder = new ZoneChannelBuilder(zone)
                .withChannelId(TadoBindingConstants.CHANNEL_ZONE_AC_POWER)
                .withRequired(false)
                .withAcceptedItemType(CoreItemFactory.SWITCH)
                .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        assertFalse(channelBuilder.isExisting());
        assertFalse(channelBuilder.isRequired());

        Channel channel = channelBuilder.build();

        assertEquals("tado:zone:test:acPower", channel.getUID().getAsString());

        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        assertNotNull(channelTypeUID);
        assertEquals("tado:acPower", channelTypeUID.getAsString());

        assertEquals("channel-type.tado.acPower.description", channel.getDescription());
        assertEquals("channel-type.tado.acPower.label", channel.getLabel());

        assertEquals(ChannelKind.STATE, channel.getKind());
        assertEquals(CoreItemFactory.SWITCH, channel.getAcceptedItemType());
    }

    /**
     * Test channel builder failures due to missing parts
     */
    @Test
    void testChannelMissingParts() {
        ZoneChannelBuilder channelBuilder;
        boolean exceptionOccurred;

        // @formatter:off
        channelBuilder = new ZoneChannelBuilder(zone)
                .withRequired(false)
                .withAcceptedItemType(CoreItemFactory.SWITCH)
                .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        exceptionOccurred = false;
        try {
            channelBuilder.build();
        } catch (IllegalStateException e) {
            exceptionOccurred = true;
        }
        assertTrue(exceptionOccurred);

        // @formatter:off
        channelBuilder = new ZoneChannelBuilder(zone)
                .withChannelId(TadoBindingConstants.CHANNEL_ZONE_AC_POWER)
                .withAcceptedItemType(CoreItemFactory.SWITCH)
                .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        exceptionOccurred = false;
        try {
            channelBuilder.build();
        } catch (IllegalStateException e) {
            exceptionOccurred = true;
        }
        assertTrue(exceptionOccurred);

        // @formatter:off
        channelBuilder = new ZoneChannelBuilder(zone)
                .withChannelId(TadoBindingConstants.CHANNEL_ZONE_AC_POWER)
                .withRequired(false)
                .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        exceptionOccurred = false;
        try {
            channelBuilder.build();
        } catch (IllegalStateException e) {
            exceptionOccurred = true;
        }
        assertTrue(exceptionOccurred);

        // @formatter:off
        channelBuilder = new ZoneChannelBuilder(zone)
                .withChannelId(TadoBindingConstants.CHANNEL_ZONE_AC_POWER)
                .withRequired(false)
                .withAcceptedItemType(CoreItemFactory.SWITCH);
        // @formatter:on

        exceptionOccurred = false;
        try {
            channelBuilder.build();
        } catch (IllegalStateException e) {
            exceptionOccurred = true;
        }
        assertTrue(exceptionOccurred);
    }

    /**
     * @return a thing with no channels in it
     */
    private Thing emptyThing() {
        Thing thing = ThingBuilder.create(TadoBindingConstants.THING_TYPE_ZONE, "test").build();
        assertEquals(0, thing.getChannels().size());
        return thing;
    }

    /**
     * @return a thing with two channels in it
     */
    private Thing fullThing() {
        List<Channel> channels = new ArrayList<>();

        // @formatter:off
        channels.add(new ZoneChannelBuilder(zone)
                .withChannelId(TadoBindingConstants.CHANNEL_ZONE_AC_POWER)
                .withRequired(true)
                .withAcceptedItemType(CoreItemFactory.SWITCH)
                .withTranslationProvider(TRANSLATION_PROVIDER)
                .build());
        // @formatter:on

        // @formatter:off
        channels.add(new ZoneChannelBuilder(zone)
                .withChannelId(TadoBindingConstants.CHANNEL_ZONE_VERTICAL_SWING)
                .withRequired(true)
                .withAcceptedItemType(CoreItemFactory.STRING)
                .withTranslationProvider(TRANSLATION_PROVIDER)
                .build());
        // @formatter:on

        Thing thing = ThingBuilder.create(TadoBindingConstants.THING_TYPE_ZONE, "test").withChannels(channels).build();

        assertEquals(2, thing.getChannels().size());
        return thing;
    }

    /**
     * Test removing a channel from a filled channel list
     */
    @Test
    void testChannelRemoving() {
        ZoneChannelBuilder channelBuilder;
        List<Channel> channels;

        // @formatter:off
            channelBuilder = new ZoneChannelBuilder(fullThing())
                .withChannelId(TadoBindingConstants.CHANNEL_ZONE_AC_POWER)
                .withRequired(false)
                .withAcceptedItemType(CoreItemFactory.SWITCH)
                .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        assertTrue(channelBuilder.isDirty());

        channels = new ArrayList<>(fullThing().getChannels());
        assertEquals(2, channels.size());

        if (channelBuilder.isAddingRequired()) {
            fail("adding not expected");
        } else if (channelBuilder.isRemovingRequired()) {
            channels.removeIf(channelBuilder.getPredicate());
        }

        assertEquals(1, channels.size());
    }

    /**
     * Test neither adding nor removing a channel to/from a filled channel list
     */
    @Test
    void testChannelNoChange() {
        ZoneChannelBuilder channelBuilder;
        List<Channel> channels;

        // @formatter:off
        channelBuilder = new ZoneChannelBuilder(fullThing())
            .withChannelId(TadoBindingConstants.CHANNEL_ZONE_AC_POWER)
            .withRequired(true)
            .withAcceptedItemType(CoreItemFactory.SWITCH)
            .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        assertFalse(channelBuilder.isDirty());

        channels = new ArrayList<>(fullThing().getChannels());
        assertEquals(2, channels.size());

        if (channelBuilder.isAddingRequired()) {
            fail("adding not expected");
        } else if (channelBuilder.isRemovingRequired()) {
            fail("removing not expected");
        }
    }

    /**
     * Test adding a channel to an empty channel list
     */
    @Test
    void testChannelAdding() {
        ZoneChannelBuilder channelBuilder;
        List<Channel> channels;

        // @formatter:off
            channelBuilder = new ZoneChannelBuilder(emptyThing())
                .withChannelId(TadoBindingConstants.CHANNEL_ZONE_AC_POWER)
                .withRequired(true)
                .withAcceptedItemType(CoreItemFactory.SWITCH)
                .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        assertTrue(channelBuilder.isDirty());

        channels = new ArrayList<>(emptyThing().getChannels());
        assertEquals(0, channels.size());

        if (channelBuilder.isAddingRequired()) {
            channels.add(channelBuilder.build());
        } else if (channelBuilder.isRemovingRequired()) {
            fail("removing not expected");
        }

        assertEquals(1, channels.size());
    }

    /**
     * Test not adding a channel to an empty channel list
     */
    @Test
    void testChannelNoAdding() {
        ZoneChannelBuilder channelBuilder;
        List<Channel> channels;

        // @formatter:off
            channelBuilder = new ZoneChannelBuilder(emptyThing())
                .withChannelId(TadoBindingConstants.CHANNEL_ZONE_AC_POWER)
                .withRequired(false)
                .withAcceptedItemType(CoreItemFactory.SWITCH)
                .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        assertFalse(channelBuilder.isDirty());

        channels = new ArrayList<>(emptyThing().getChannels());
        assertEquals(0, channels.size());

        if (channelBuilder.isAddingRequired()) {
            fail("adding not expected");
        } else if (channelBuilder.isRemovingRequired()) {
            fail("removing not expected");
        }
    }
}
