/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.anthem.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.anthem.internal.AnthemBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;

/**
 * The {@link AnthemCommandParserTest} is responsible for testing the functionality
 * of the Anthem command parser.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class AnthemCommandParserTest {

    AnthemCommandParser parser = new AnthemCommandParser();

    @Test
    public void testInvalidCommands() {
        @Nullable
        AnthemUpdate update;

        update = parser.parseCommand("BOGUS_COMMAND;");
        assertEquals(null, update);

        update = parser.parseCommand("UNTERMINATED_COMMAND");
        assertEquals(null, update);

        update = parser.parseCommand("Z1POW0");
        assertEquals(null, update);

        update = parser.parseCommand("X");
        assertEquals(null, update);

        update = parser.parseCommand("Y;");
        assertEquals(null, update);

        update = parser.parseCommand("Z1POW67;");
        assertEquals(null, update);

        update = parser.parseCommand("POW0;");
        assertEquals(null, update);
    }

    @Test
    public void testPowerCommands() {
        @Nullable
        AnthemUpdate update;

        update = parser.parseCommand("Z1POW1;");
        assertNotEquals(null, update);
        if (update != null) {
            assertTrue(update.isStateUpdate());
            assertFalse(update.isPropertyUpdate());
            assertEquals("1", update.getStateUpdate().getGroupId());
            assertEquals(CHANNEL_POWER, update.getStateUpdate().getChannelId());
            assertEquals(OnOffType.ON, update.getStateUpdate().getState());
        }

        update = parser.parseCommand("Z2POW0;");
        assertNotEquals(null, update);
        if (update != null) {
            assertEquals("2", update.getStateUpdate().getGroupId());
            assertEquals(CHANNEL_POWER, update.getStateUpdate().getChannelId());
            assertEquals(OnOffType.OFF, update.getStateUpdate().getState());
        }
    }

    @Test
    public void testVolumeCommands() {
        @Nullable
        AnthemUpdate update;

        update = parser.parseCommand("Z1VOL55;");
        assertNotEquals(null, update);
        if (update != null) {
            assertEquals("1", update.getStateUpdate().getGroupId());
            assertEquals(CHANNEL_VOLUME_DB, update.getStateUpdate().getChannelId());
            assertEquals(new DecimalType(55), update.getStateUpdate().getState());
        }

        update = parser.parseCommand("Z2VOL99;");
        assertNotEquals(null, update);
        if (update != null) {
            assertEquals("2", update.getStateUpdate().getGroupId());
            assertEquals(CHANNEL_VOLUME_DB, update.getStateUpdate().getChannelId());
            assertEquals(new DecimalType(99), update.getStateUpdate().getState());
        }
    }

    @Test
    public void testMuteCommands() {
        @Nullable
        AnthemUpdate update;

        update = parser.parseCommand("Z1MUT1;");
        assertNotEquals(null, update);
        if (update != null) {
            assertEquals("1", update.getStateUpdate().getGroupId());
            assertEquals(CHANNEL_MUTE, update.getStateUpdate().getChannelId());
            assertEquals(OnOffType.ON, update.getStateUpdate().getState());
        }

        update = parser.parseCommand("Z2MUT0;");
        assertNotEquals(null, update);
        if (update != null) {
            assertTrue(update.isStateUpdate());
            assertEquals("2", update.getStateUpdate().getGroupId());
            assertEquals(CHANNEL_MUTE, update.getStateUpdate().getChannelId());
            assertEquals(OnOffType.OFF, update.getStateUpdate().getState());
        }
    }

    @Test
    public void testNumInputsCommand() {
        @Nullable
        AnthemUpdate update;

        update = parser.parseCommand("ICN8;");
        assertNotEquals(null, update);
        if (update != null) {
            assertTrue(update.isPropertyUpdate());
            assertEquals(PROPERTY_NUM_AVAILABLE_INPUTS, update.getPropertyUpdate().getName());
            assertEquals("8", update.getPropertyUpdate().getValue());
        }

        update = parser.parseCommand("ICN15;");
        assertNotEquals(null, update);
        if (update != null) {
            assertTrue(update.isPropertyUpdate());
            assertEquals(PROPERTY_NUM_AVAILABLE_INPUTS, update.getPropertyUpdate().getName());
            assertEquals("15", update.getPropertyUpdate().getValue());
        }
    }

    @Test
    public void testRegionProperty() {
        @Nullable
        AnthemUpdate update;

        update = parser.parseCommand("IDRUS;");
        assertNotEquals(null, update);
        if (update != null) {
            assertTrue(update.isPropertyUpdate());
            assertFalse(update.isStateUpdate());
            assertEquals(PROPERTY_REGION, update.getPropertyUpdate().getName());
            assertEquals("US", update.getPropertyUpdate().getValue());
        }
    }

    @Test
    public void testSoftwareVersionProperty() {
        @Nullable
        AnthemUpdate update;

        update = parser.parseCommand("IDS1.2.3.4;");
        assertNotEquals(null, update);
        if (update != null) {
            assertTrue(update.isPropertyUpdate());
            assertEquals(Thing.PROPERTY_FIRMWARE_VERSION, update.getPropertyUpdate().getName());
            assertEquals("1.2.3.4", update.getPropertyUpdate().getValue());
        }
    }
}
