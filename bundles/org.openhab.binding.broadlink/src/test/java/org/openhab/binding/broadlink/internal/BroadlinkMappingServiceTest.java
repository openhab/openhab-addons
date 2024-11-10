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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.broadlink.AbstractBroadlinkTest;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants.CodeType;
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
        BroadlinkMappingService bms = new BroadlinkMappingService(mockProvider, TEST_CHANNEL_UID, TEST_CHANNEL_UID2,
                storageService);

        assertEquals("00112233", bms.lookupCode("IR_TEST_COMMAND_ON", CodeType.IR));
        assertEquals("33221100", bms.lookupCode("IR_TEST_COMMAND_OFF", CodeType.IR));
        assertEquals(null, bms.lookupCode("IR_TEST_COMMAND_DUMMY", CodeType.IR));
        assertEquals("00112233", bms.lookupCode("RF_TEST_COMMAND_ON", CodeType.RF));
        assertEquals("33221100", bms.lookupCode("RF_TEST_COMMAND_OFF", CodeType.RF));
        assertEquals(null, bms.lookupCode("RF_TEST_COMMAND_DUMMY", CodeType.RF));
    }

    @Test
    public void canStoreOnAMapFile() {
        BroadlinkMappingService bms = new BroadlinkMappingService(mockProvider, TEST_CHANNEL_UID, TEST_CHANNEL_UID2,
                storageService);

        assertEquals("IR_TEST_COMMAND_UP", bms.storeCode("IR_TEST_COMMAND_UP", "44556677", CodeType.IR));
        assertEquals(null, bms.storeCode("IR_TEST_COMMAND_ON", "77665544", CodeType.IR));
        assertEquals("44556677", irStorage.get("IR_TEST_COMMAND_UP"));
        assertEquals("RF_TEST_COMMAND_UP", bms.storeCode("RF_TEST_COMMAND_UP", "44556677", CodeType.RF));
        assertEquals(null, bms.storeCode("RF_TEST_COMMAND_ON", "77665544", CodeType.RF));
        assertEquals("44556677", rfStorage.get("RF_TEST_COMMAND_UP"));
    }

    @Test
    public void canReplaceOnAMapFile() {
        BroadlinkMappingService bms = new BroadlinkMappingService(mockProvider, TEST_CHANNEL_UID, TEST_CHANNEL_UID2,
                storageService);

        assertEquals(null, bms.replaceCode("IR_TEST_COMMAND_UP", "55667788", CodeType.IR));
        assertEquals("IR_TEST_COMMAND_ON", bms.replaceCode("IR_TEST_COMMAND_ON", "55667788", CodeType.IR));
        assertEquals("55667788", irStorage.get("IR_TEST_COMMAND_ON"));
        assertEquals(null, bms.replaceCode("RF_TEST_COMMAND_UP", "55667788", CodeType.RF));
        assertEquals("RF_TEST_COMMAND_ON", bms.replaceCode("RF_TEST_COMMAND_ON", "55667788", CodeType.RF));
        assertEquals("55667788", rfStorage.get("RF_TEST_COMMAND_ON"));
    }

    @Test
    public void canDeleteFromAMapFile() {
        BroadlinkMappingService bms = new BroadlinkMappingService(mockProvider, TEST_CHANNEL_UID, TEST_CHANNEL_UID2,
                storageService);

        assertEquals("IR_TEST_COMMAND_ON", bms.deleteCode("IR_TEST_COMMAND_ON", CodeType.IR));
        assertEquals(null, irStorage.get("IR_TEST_COMMAND_ON"));
        assertEquals(null, bms.deleteCode("IR_TEST_COMMAND_DUMMY", CodeType.IR));
        assertEquals("RF_TEST_COMMAND_ON", bms.deleteCode("RF_TEST_COMMAND_ON", CodeType.RF));
        assertEquals(null, rfStorage.get("RF_TEST_COMMAND_ON"));
        assertEquals(null, bms.deleteCode("RF_TEST_COMMAND_DUMMY", CodeType.RF));
    }

    @Test
    public void notifiesTheFrameworkOfTheAvailableCommands() {
        new BroadlinkMappingService(mockProvider, TEST_CHANNEL_UID, TEST_CHANNEL_UID2, storageService);

        ArrayList<CommandOption> expected = new ArrayList<>();
        ArrayList<CommandOption> expected2 = new ArrayList<>();
        expected.add(new CommandOption("IR_TEST_COMMAND_ON", null));
        expected.add(new CommandOption("IR_TEST_COMMAND_OFF", null));
        expected2.add(new CommandOption("RF_TEST_COMMAND_ON", null));
        expected2.add(new CommandOption("RF_TEST_COMMAND_OFF", null));
        verify(mockProvider).setCommandOptions(TEST_CHANNEL_UID, expected);
        verify(mockProvider).setCommandOptions(TEST_CHANNEL_UID2, expected2);
    }
}
