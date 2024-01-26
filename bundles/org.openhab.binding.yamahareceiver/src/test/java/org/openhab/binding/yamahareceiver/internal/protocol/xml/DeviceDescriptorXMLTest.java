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
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Feature.*;
import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Zone.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants;

/**
 * Unit test for {@link DeviceDescriptorXML}.
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public class DeviceDescriptorXMLTest extends AbstractXMLProtocolTest {

    @Test
    public void given_RXS601D_parsesDescriptor() throws IOException {
        parsesDescriptor("RX-S601D", Arrays.asList(Main_Zone, Zone_2),
                Arrays.asList(AIRPLAY, SPOTIFY, USB, BLUETOOTH, DAB, NET_RADIO),
                new CommandsSpec(9,
                        Arrays.asList("System,Power_Control,Power", "System,Party_Mode,Mode",
                                "System,Party_Mode,Volume,Lvl", "System,Party_Mode,Volume,Mute")),
                new CommandsSpec[] {
                        // Main_Zone
                        new CommandsSpec(29, Arrays.asList("Main_Zone,Power_Control,Power", "Main_Zone,Volume,Lvl",
                                "Main_Zone,Volume,Mute", "Main_Zone,Input,Input_Sel", "Main_Zone,Input,Input_Sel_Item",
                                "Main_Zone,Scene,Scene_Sel", "Main_Zone,Scene,Scene_Sel_Item",
                                "Main_Zone,Surround,Program_Sel,Current,Straight",
                                "Main_Zone,Surround,Program_Sel,Current,Enhancer",
                                "Main_Zone,Surround,Program_Sel,Current,Sound_Program")),
                        // Zone_2
                        new CommandsSpec(20,
                                Arrays.asList("Zone_2,Power_Control,Power", "Zone_2,Volume,Lvl", "Zone_2,Volume,Mute",
                                        "Zone_2,Input,Input_Sel", "Zone_2,Input,Input_Sel_Item",
                                        "Zone_2,Scene,Scene_Sel", "Zone_2,Scene,Scene_Sel_Item")) });
    }

    @Test
    public void given_RXV3900_parsesDescriptor() throws IOException {
        parsesDescriptor("RX-V3900", Arrays.asList(Main_Zone, Zone_2, Zone_3), Arrays.asList(BLUETOOTH, TUNER, NET_USB),
                new CommandsSpec(2, Arrays.asList("System,Power_Control,Power")), new CommandsSpec[] {
                        // Main_Zone
                        new CommandsSpec(9, Arrays.asList("Main_Zone,Power_Control,Power", "Main_Zone,Vol,Lvl",
                                "Main_Zone,Vol,Mute", "Main_Zone,Input,Input_Sel", "Main_Zone,Input,Input_Sel_Item")),
                        // Zone_2
                        new CommandsSpec(9,
                                Arrays.asList("Zone_2,Power_Control,Power", "Zone_2,Vol,Lvl", "Zone_2,Vol,Mute",
                                        "Zone_2,Input,Input_Sel", "Zone_2,Input,Input_Sel_Item")),
                        // Zone_3
                        new CommandsSpec(9, Arrays.asList("Zone_3,Power_Control,Power", "Zone_3,Vol,Lvl",
                                "Zone_3,Vol,Mute", "Zone_3,Input,Input_Sel", "Zone_3,Input,Input_Sel_Item")) });
    }

    private void parsesDescriptor(String model, List<YamahaReceiverBindingConstants.Zone> zones,
            Collection<YamahaReceiverBindingConstants.Feature> features, CommandsSpec systemCommandsSpec,
            CommandsSpec[] zonesCommandsSpec) throws IOException {
        // arrange
        ctx.prepareForModel(model);

        DeviceDescriptorXML subject = new DeviceDescriptorXML();

        // act
        subject.load(con);

        // assert
        assertEquals(model, subject.getUnitName());

        assertNotNull(subject.system);
        assertCommands(subject.system, systemCommandsSpec);

        assertNotNull(subject.features);
        assertTrue(subject.features.keySet().containsAll(features), "Desired features present");

        assertNotNull(subject.zones);
        assertEquals(zones.size(), subject.zones.size(), "Number of zones match");

        for (int i = 0; i < zones.size(); i++) {
            YamahaReceiverBindingConstants.Zone zone = zones.get(i);

            assertTrue(subject.zones.containsKey(zone), "Desired zone is present");

            DeviceDescriptorXML.ZoneDescriptor zoneDesc = subject.zones.get(zone);
            CommandsSpec zoneSpec = zonesCommandsSpec[i];
            assertCommands(zoneDesc, zoneSpec);
        }
    }

    private void assertCommands(DeviceDescriptorXML.HasCommands descWithCommands, CommandsSpec spec) {
        assertNotNull(descWithCommands, "Descriptor commands are present");
        assertEquals(spec.expectedNumber, descWithCommands.commands.size(), "Expected number of commands");
        assertTrue(descWithCommands.commands.containsAll(spec.expected), "Expected commands are present");
    }

    private static class CommandsSpec {

        private final int expectedNumber;

        private final Collection<String> expected;

        private CommandsSpec(int expectedNumber, Collection<String> expected) {
            this.expectedNumber = expectedNumber;
            this.expected = expected;
        }
    }
}
