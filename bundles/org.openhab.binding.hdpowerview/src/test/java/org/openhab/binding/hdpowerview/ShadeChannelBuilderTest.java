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
package org.openhab.binding.hdpowerview;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hdpowerview.internal.HDPowerViewTranslationProvider;
import org.openhab.binding.hdpowerview.internal.builders.ShadeChannelBuilder;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.binding.hdpowerview.providers.MockedLocaleProvider;
import org.openhab.binding.hdpowerview.providers.MockedTranslationProvider;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.framework.Bundle;

/**
 * Unit tests for {@link ShadeChannelBuilder}.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadeChannelBuilderTest {

    private static final HDPowerViewTranslationProvider TRANSLATION_PROVIDER = new HDPowerViewTranslationProvider(
            mock(Bundle.class), new MockedTranslationProvider(), new MockedLocaleProvider());

    private Thing shade = ThingBuilder.create(THING_TYPE_SHADE, "test").build();

    private enum Expect {
        DIRTY,
        CLEAN
    }

    /**
     * Test the primary position channel
     */
    @Test
    public void testPositionChannel() {
        // @formatter:off
        ShadeChannelBuilder channelBuilder = new ShadeChannelBuilder(shade)
                .withChannelTypeUID(CHANNEL_TYPE_POSITION)
                .withChannelId(CHANNEL_SHADE_POSITION)
                .withRequired(false)
                .withAcceptedItemType(CoreItemFactory.ROLLERSHUTTER)
                .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        assertFalse(channelBuilder.isExisting());
        assertFalse(channelBuilder.isRequired());

        Channel channel = channelBuilder.build();

        assertEquals("hdpowerview:shade:test:position", channel.getUID().getAsString());

        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        assertNotNull(channelTypeUID);
        assertEquals("hdpowerview:shade-position", channelTypeUID.getAsString());

        assertEquals("channel-type.hdpowerview.shade-position.description", channel.getDescription());
        assertEquals("channel-type.hdpowerview.shade-position.label", channel.getLabel());

        assertEquals(ChannelKind.STATE, channel.getKind());
        assertEquals(CoreItemFactory.ROLLERSHUTTER, channel.getAcceptedItemType());
    }

    /**
     * Test the secondary channel
     */
    @Test
    public void testSecondaryChannel() {
        // @formatter:off
        ShadeChannelBuilder channelBuilder = new ShadeChannelBuilder(shade)
                .withChannelTypeUID(CHANNEL_TYPE_SECONDARY)
                .withChannelId(CHANNEL_SHADE_SECONDARY_POSITION)
                .withRequired(false)
                .withAcceptedItemType(CoreItemFactory.ROLLERSHUTTER)
                .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        assertFalse(channelBuilder.isExisting());
        assertFalse(channelBuilder.isRequired());

        Channel channel = channelBuilder.build();

        assertEquals("hdpowerview:shade:test:secondary", channel.getUID().getAsString());

        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        assertNotNull(channelTypeUID);
        assertEquals("hdpowerview:shade-secondary", channelTypeUID.getAsString());

        assertEquals("channel-type.hdpowerview.shade-secondary.description", channel.getDescription());
        assertEquals("channel-type.hdpowerview.shade-secondary.label", channel.getLabel());

        assertEquals(ChannelKind.STATE, channel.getKind());
        assertEquals(CoreItemFactory.ROLLERSHUTTER, channel.getAcceptedItemType());
    }

    /**
     * Test the vane channel
     */
    @Test
    public void testVaneChannel() {
        // @formatter:off
        ShadeChannelBuilder channelBuilder = new ShadeChannelBuilder(shade)
                .withChannelTypeUID(CHANNEL_TYPE_VANE)
                .withChannelId(CHANNEL_SHADE_VANE)
                .withRequired(false)
                .withAcceptedItemType(CoreItemFactory.DIMMER)
                .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        assertFalse(channelBuilder.isExisting());
        assertFalse(channelBuilder.isRequired());

        Channel channel = channelBuilder.build();

        assertEquals("hdpowerview:shade:test:vane", channel.getUID().getAsString());

        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        assertNotNull(channelTypeUID);
        assertEquals("hdpowerview:shade-vane", channelTypeUID.getAsString());

        assertEquals("channel-type.hdpowerview.shade-vane.description", channel.getDescription());
        assertEquals("channel-type.hdpowerview.shade-vane.label", channel.getLabel());

        assertEquals(ChannelKind.STATE, channel.getKind());
        assertEquals(CoreItemFactory.DIMMER, channel.getAcceptedItemType());
    }

    /**
     * Test whether channels are existing or required
     */
    @Test
    public void testExistingRequired() {
        // @formatter:off
        ShadeChannelBuilder channelBuilder = new ShadeChannelBuilder(shade)
                .withChannelTypeUID(CHANNEL_TYPE_VANE)
                .withChannelId(CHANNEL_SHADE_VANE)
                .withRequired(false)
                .withAcceptedItemType(CoreItemFactory.DIMMER)
                .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        assertFalse(channelBuilder.isExisting());
        assertFalse(channelBuilder.isRequired());

        Thing shade2 = ThingBuilder.create(THING_TYPE_SHADE, "test").withChannel(channelBuilder.build()).build();
        assertEquals(1, shade2.getChannels().size());

        // @formatter:off
        channelBuilder = new ShadeChannelBuilder(shade2)
                .withChannelTypeUID(CHANNEL_TYPE_VANE)
                .withChannelId(CHANNEL_SHADE_VANE)
                .withRequired(true)
                .withAcceptedItemType(CoreItemFactory.DIMMER)
                .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        assertTrue(channelBuilder.isExisting());
        assertTrue(channelBuilder.isRequired());
    }

    /**
     * Test errors if the builder has missing elements
     */
    @Test
    public void testBuilderMissingElements() {
        ShadeChannelBuilder channelBuilder;
        boolean failed;

        Thing fullShade = fullThing();

        // @formatter:off
        // missing ChannelTypeUID element
        channelBuilder = new ShadeChannelBuilder(fullShade)
                .withChannelId(CHANNEL_SHADE_VANE)
                .withRequired(false)
                .withAcceptedItemType(CoreItemFactory.DIMMER)
                .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        failed = false;
        try {
            channelBuilder.isExisting();
            channelBuilder.isRequired();
            channelBuilder.getPredicate();
            channelBuilder.build();
        } catch (IllegalStateException e) {
            failed = true;
        }
        assertTrue(failed);

        // @formatter:off
        // missing ChannelId element
        channelBuilder = new ShadeChannelBuilder(fullShade)
                .withChannelTypeUID(CHANNEL_TYPE_VANE)
                .withRequired(false)
                .withAcceptedItemType(CoreItemFactory.DIMMER)
                .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        failed = false;
        try {
            channelBuilder.isExisting();
            channelBuilder.isRequired();
            channelBuilder.getPredicate();
            channelBuilder.build();
        } catch (IllegalStateException e) {
            failed = true;
        }
        assertTrue(failed);

        // @formatter:off
        // missing translation provider element
        channelBuilder = new ShadeChannelBuilder(fullShade)
                .withChannelTypeUID(CHANNEL_TYPE_VANE)
                .withChannelId(CHANNEL_SHADE_VANE)
                .withRequired(false)
                .withAcceptedItemType(CoreItemFactory.DIMMER);
        // @formatter:on

        failed = false;
        try {
            channelBuilder.isExisting();
            channelBuilder.isRequired();
            channelBuilder.getPredicate();
            channelBuilder.build();
        } catch (IllegalStateException e) {
            failed = true;
        }
        assertTrue(failed);

        // @formatter:off
        // missing 'required' element
        channelBuilder = new ShadeChannelBuilder(fullShade)
                .withChannelTypeUID(CHANNEL_TYPE_VANE)
                .withChannelId(CHANNEL_SHADE_VANE)
                .withAcceptedItemType(CoreItemFactory.DIMMER)
                .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        failed = false;
        try {
            channelBuilder.isExisting();
            channelBuilder.isRequired();
            channelBuilder.getPredicate();
            channelBuilder.build();
        } catch (IllegalStateException e) {
            failed = true;
        }
        assertTrue(failed);

        // @formatter:off
        // missing accepted item type
        channelBuilder = new ShadeChannelBuilder(fullShade)
                .withChannelTypeUID(CHANNEL_TYPE_VANE)
                .withChannelId(CHANNEL_SHADE_VANE)
                .withRequired(false)
                .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        failed = false;
        try {
            channelBuilder.isExisting();
            channelBuilder.isRequired();
            channelBuilder.getPredicate();
            channelBuilder.build();
        } catch (IllegalStateException e) {
            failed = true;
        }
        assertTrue(failed);

        // @formatter:off
        // all elements present
        channelBuilder = new ShadeChannelBuilder(fullShade)
                .withChannelTypeUID(CHANNEL_TYPE_VANE)
                .withChannelId(CHANNEL_SHADE_VANE)
                .withRequired(false)
                .withAcceptedItemType(CoreItemFactory.DIMMER)
                .withTranslationProvider(TRANSLATION_PROVIDER);
        // @formatter:on

        failed = false;
        try {
            channelBuilder.isExisting();
            channelBuilder.isRequired();
            channelBuilder.getPredicate();
            channelBuilder.build();
        } catch (IllegalStateException e) {
            failed = true;
        }
        assertFalse(failed);
    }

    /**
     * Test adding the required channels to an empty thing
     */
    @Test
    public void testListAddChannels() {
        testListAddRemoveChannelsHelper(emptyThing(), 0, Expect.DIRTY, 1);
        testListAddRemoveChannelsHelper(emptyThing(), 1, Expect.DIRTY, 2);
        testListAddRemoveChannelsHelper(emptyThing(), 2, Expect.DIRTY, 2);
        testListAddRemoveChannelsHelper(emptyThing(), 3, Expect.DIRTY, 2);
        testListAddRemoveChannelsHelper(emptyThing(), 4, Expect.DIRTY, 1);
        testListAddRemoveChannelsHelper(emptyThing(), 5, Expect.DIRTY, 1);
        testListAddRemoveChannelsHelper(emptyThing(), 6, Expect.DIRTY, 1);
        testListAddRemoveChannelsHelper(emptyThing(), 7, Expect.DIRTY, 2);
        testListAddRemoveChannelsHelper(emptyThing(), 8, Expect.DIRTY, 2);
        testListAddRemoveChannelsHelper(emptyThing(), 9, Expect.DIRTY, 3);
    }

    /**
     * Test removing the non required channels from a fully loaded thing
     */
    @Test
    public void testListRemoveChannels() {
        testListAddRemoveChannelsHelper(fullThing(), 0, Expect.DIRTY, 1);
        testListAddRemoveChannelsHelper(fullThing(), 1, Expect.DIRTY, 2);
        testListAddRemoveChannelsHelper(fullThing(), 2, Expect.DIRTY, 2);
        testListAddRemoveChannelsHelper(fullThing(), 3, Expect.DIRTY, 2);
        testListAddRemoveChannelsHelper(fullThing(), 4, Expect.DIRTY, 1);
        testListAddRemoveChannelsHelper(fullThing(), 5, Expect.DIRTY, 1);
        testListAddRemoveChannelsHelper(fullThing(), 6, Expect.DIRTY, 1);
        testListAddRemoveChannelsHelper(fullThing(), 7, Expect.DIRTY, 2);
        testListAddRemoveChannelsHelper(fullThing(), 8, Expect.DIRTY, 2);

        // the fullThing has all three channels so this test result won't be dirty!!
        testListAddRemoveChannelsHelper(fullThing(), 9, Expect.CLEAN, 3);
    }

    /**
     * Helper method to run tests on the given thing instance, with the given shade capabilities id, and with the given
     * expected result
     *
     * @param thing test article (may be a full or an empty thing)
     * @param capabilitiesId target capabilities
     * @param expectedDirty expected dirty outcome of tests
     * @param expectedChannelCount expected resulting number of channels
     * @return the resulting channel list
     */
    private List<Channel> testListAddRemoveChannelsHelper(Thing thing, int capabilitiesId, Expect expectedDirty,
            int expectedChannelCount) {
        ShadeCapabilitiesDatabase db = new ShadeCapabilitiesDatabase();
        Capabilities capabilities = db.getCapabilities(capabilitiesId);

        List<ShadeChannelBuilder> channelBuilders = new ArrayList<>();

        // @formatter:off
        // vane channel
        channelBuilders.add(new ShadeChannelBuilder(thing)
            .withChannelTypeUID(CHANNEL_TYPE_VANE)
            .withChannelId(CHANNEL_SHADE_VANE)
            .withRequired(capabilities.supportsTiltAnywhere() || capabilities.supportsTiltOnClosed())
            .withAcceptedItemType(CoreItemFactory.DIMMER)
            .withTranslationProvider(TRANSLATION_PROVIDER));

        // secondary channel
        channelBuilders.add(new ShadeChannelBuilder(thing)
            .withChannelTypeUID(CHANNEL_TYPE_SECONDARY)
            .withChannelId(CHANNEL_SHADE_SECONDARY_POSITION)
            .withRequired(capabilities.supportsSecondary() || capabilities.supportsSecondaryOverlapped())
            .withAcceptedItemType(CoreItemFactory.ROLLERSHUTTER)
            .withTranslationProvider(TRANSLATION_PROVIDER));

        // primary channel
        channelBuilders.add(new ShadeChannelBuilder(thing)
            .withChannelTypeUID(CHANNEL_TYPE_POSITION)
            .withChannelId(CHANNEL_SHADE_POSITION)
            .withRequired(capabilities.supportsPrimary())
            .withAcceptedItemType(CoreItemFactory.ROLLERSHUTTER)
            .withTranslationProvider(TRANSLATION_PROVIDER));
        //  @formatter:on

        boolean dirty = false;
        for (ShadeChannelBuilder channelBuilder : channelBuilders) {
            try {
                dirty |= channelBuilder.isDirty();
            } catch (IllegalStateException e) {
                fail(e.getMessage());
            }
        }

        assertEquals(expectedDirty, dirty ? Expect.DIRTY : Expect.CLEAN);

        List<Channel> channels = new ArrayList<>(thing.getChannels());

        for (ShadeChannelBuilder channelBuilder : channelBuilders) {
            if (channelBuilder.isAddingRequired()) {
                channels.add(0, channelBuilder.build());
            } else if (channelBuilder.isRemovingRequired()) {
                channels.removeIf(channelBuilder.getPredicate());
            }
        }

        assertEquals(expectedChannelCount, channels.size());

        return channels;
    }

    /**
     * @return a thing with no channels in it
     */
    private Thing emptyThing() {
        Thing thing = ThingBuilder.create(THING_TYPE_SHADE, "test").build();
        assertEquals(0, thing.getChannels().size());
        return thing;
    }

    /**
     * @return a thing with three channels in it
     */
    private Thing fullThing() {
        List<Channel> channels = testListAddRemoveChannelsHelper(emptyThing(), 9, Expect.DIRTY, 3);
        Thing thing = ThingBuilder.create(THING_TYPE_SHADE, "test").withChannels(channels).build();
        assertEquals(3, thing.getChannels().size());
        return thing;
    }
}
