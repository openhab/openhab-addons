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
package org.openhab.binding.broadlink.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
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
    private static final String TEST_MAP_FILE = "broadlink.map";
    private static final ChannelUID TEST_CHANNEL_UID = new ChannelUID("bsm:test:channel:uid");

    private BroadlinkRemoteDynamicCommandDescriptionProvider mockProvider = Mockito
            .mock(BroadlinkRemoteDynamicCommandDescriptionProvider.class);

    @Test
    public void canReadFromAMapFile() {
        BroadlinkMappingService bms = new BroadlinkMappingService(TEST_MAP_FILE, mockProvider, TEST_CHANNEL_UID);

        assertEquals("00112233", bms.lookup("TEST_COMMAND_ON"));
        assertEquals("33221100", bms.lookup("TEST_COMMAND_OFF"));
    }

    @Test
    public void notifiesTheFrameworkOfTheAvailableCommands() {
        new BroadlinkMappingService(TEST_MAP_FILE, mockProvider, TEST_CHANNEL_UID);

        List<CommandOption> expected = new ArrayList<>();
        expected.add(new CommandOption("TEST_COMMAND_ON", null));
        expected.add(new CommandOption("TEST_COMMAND_OFF", null));
        verify(mockProvider).setCommandOptions(TEST_CHANNEL_UID, expected);
    }
}
