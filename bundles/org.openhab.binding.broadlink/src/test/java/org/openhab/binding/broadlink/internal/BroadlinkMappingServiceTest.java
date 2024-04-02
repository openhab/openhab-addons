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
package org.openhab.binding.broadlink.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.broadlink.AbstractBroadlinkTest;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.CommandOption;

/**
 * Tests the Broadlink mapping service.
 *
 * @author John Marshall - Initial contribution
 */
@NonNullByDefault
public class BroadlinkMappingServiceTest extends AbstractBroadlinkTest {
    private static final ChannelUID TEST_CHANNEL_UID = new ChannelUID("bsm:test:channel:uid");
    private static final ChannelUID TEST_CHANNEL_UID2 = new ChannelUID("bsm:test:channel:uid2");

    private BroadlinkRemoteDynamicCommandDescriptionProvider mockProvider = Mockito
            .mock(BroadlinkRemoteDynamicCommandDescriptionProvider.class);

    @Test
    public void canReadFromAMapFile() {
        BroadlinkMappingService bms = new BroadlinkMappingService(TEST_MAP_FILE_IR, TEST_MAP_FILE_RF, mockProvider,
                TEST_CHANNEL_UID, TEST_CHANNEL_UID2, storageService);

        assertEquals("00112233", bms.lookupIR("IR_TEST_COMMAND_ON"));
        assertEquals("33221100", bms.lookupIR("IR_TEST_COMMAND_OFF"));
        assertEquals(null, bms.lookupIR("IR_TEST_COMMAND_DUMMY"));
        assertEquals("00112233", bms.lookupRF("RF_TEST_COMMAND_ON"));
        assertEquals("33221100", bms.lookupRF("RF_TEST_COMMAND_OFF"));
        assertEquals(null, bms.lookupRF("RF_TEST_COMMAND_DUMMY"));
    }

    @Test
    public void canStoreOnAMapFile() {
        BroadlinkMappingService bms = new BroadlinkMappingService(TEST_MAP_FILE_IR, TEST_MAP_FILE_RF, mockProvider,
                TEST_CHANNEL_UID, TEST_CHANNEL_UID2, storageService);

        assertEquals("IR_TEST_COMMAND_UP", bms.storeIR("IR_TEST_COMMAND_UP", "44556677"));
        assertEquals(null, bms.storeIR("IR_TEST_COMMAND_ON", "77665544"));
        assertEquals("44556677", irStorage.get("IR_TEST_COMMAND_UP"));
        assertEquals("RF_TEST_COMMAND_UP", bms.storeRF("RF_TEST_COMMAND_UP", "44556677"));
        assertEquals(null, bms.storeRF("RF_TEST_COMMAND_ON", "77665544"));
        assertEquals("44556677", rfStorage.get("RF_TEST_COMMAND_UP"));
    }

    @Test
    public void canReplaceOnAMapFile() {
        BroadlinkMappingService bms = new BroadlinkMappingService(TEST_MAP_FILE_IR, TEST_MAP_FILE_RF, mockProvider,
                TEST_CHANNEL_UID, TEST_CHANNEL_UID2, storageService);

        assertEquals(null, bms.replaceIR("IR_TEST_COMMAND_UP", "55667788"));
        assertEquals("IR_TEST_COMMAND_ON", bms.replaceIR("IR_TEST_COMMAND_ON", "55667788"));
        assertEquals("55667788", irStorage.get("IR_TEST_COMMAND_ON"));
        assertEquals(null, bms.replaceRF("RF_TEST_COMMAND_UP", "55667788"));
        assertEquals("RF_TEST_COMMAND_ON", bms.replaceRF("RF_TEST_COMMAND_ON", "55667788"));
        assertEquals("55667788", rfStorage.get("RF_TEST_COMMAND_ON"));
    }

    @Test
    public void canDeleteFromAMapFile() {
        BroadlinkMappingService bms = new BroadlinkMappingService(TEST_MAP_FILE_IR, TEST_MAP_FILE_RF, mockProvider,
                TEST_CHANNEL_UID, TEST_CHANNEL_UID2, storageService);

        assertEquals("IR_TEST_COMMAND_ON", bms.deleteIR("IR_TEST_COMMAND_ON"));
        assertEquals(null, irStorage.get("IR_TEST_COMMAND_ON"));
        assertEquals(null, bms.deleteIR("IR_TEST_COMMAND_DUMMY"));
        assertEquals("RF_TEST_COMMAND_ON", bms.deleteRF("RF_TEST_COMMAND_ON"));
        assertEquals(null, rfStorage.get("RF_TEST_COMMAND_ON"));
        assertEquals(null, bms.deleteRF("RF_TEST_COMMAND_DUMMY"));
    }

    @Test
    public void notifiesTheFrameworkOfTheAvailableCommands() {
        new BroadlinkMappingService(TEST_MAP_FILE_IR, TEST_MAP_FILE_RF, mockProvider, TEST_CHANNEL_UID,
                TEST_CHANNEL_UID2, storageService);

        List<CommandOption> expected = new ArrayList<>();
        List<CommandOption> expected2 = new ArrayList<>();
        expected.add(new CommandOption("IR_TEST_COMMAND_ON", null));
        expected.add(new CommandOption("IR_TEST_COMMAND_OFF", null));
        expected2.add(new CommandOption("RF_TEST_COMMAND_ON", null));
        expected2.add(new CommandOption("RF_TEST_COMMAND_OFF", null));
        verify(mockProvider).setCommandOptions(TEST_CHANNEL_UID, expected);
        verify(mockProvider).setCommandOptions(TEST_CHANNEL_UID2, expected);
    }
}
