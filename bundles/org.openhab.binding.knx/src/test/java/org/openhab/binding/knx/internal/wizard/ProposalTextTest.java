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
package org.openhab.binding.knx.internal.wizard;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.knx.internal.wizard.WizardFixtures.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Channel;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Item;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.LocationGroup;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Read;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Thing;

/**
 * Tests for the {@code .things} and {@code .items} text of the wizard. All data is made up.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ProposalTextTest {
    private static final Pattern THING_LINE = Pattern.compile(
            "Thing knx:device:[A-Za-z0-9_-]+:[A-Za-z0-9_]+ \"(?:[^\"\\\\]|\\\\.)*\" \\(knx:(ip|serial):[A-Za-z0-9_-]+\\) \\{");

    static Channel switchChannel(String thingId, String id, String label) {
        return channel(thingId, id, label, "switch", id, "Switch", part("ga", "1.001", "31/7/1", null, Read.NONE));
    }

    @Test
    void theThingsTextOfTheSample() {
        assertEquals("""
                Thing knx:device:example:Kitchen "Kitchen" (knx:ip:example) {
                    Type switch : Ceiling_light "Ceiling light" [ ga="1.001:31/7/1+<31/7/2" ]
                    Type dimmer : Spots "Spots" [ switch="1.001:31/7/3+<31/7/4", position="5.001:31/7/5+<31/7/6", \
                increaseDecrease="3.007:31/7/7" ]
                    Type number : Temperature "Temperature" [ ga="9.001:<31/7/8" ]
                }

                Thing knx:device:example:Hall "Hall" (knx:ip:example) {
                    Type rollershutter : Blind "Blind" [ upDown="1.008:31/7/10", stopMove="1.010:31/7/11", \
                position="5.001:31/7/12+<31/7/13" ]
                }
                """, ProposalText.of(sample(true)).thingsText());
    }

    @Test
    void theItemsTextOfTheSample() {
        assertEquals("""
                Group gKitchen "Kitchen" ["Room"]

                Switch Kitchen_Ceiling_light "Ceiling light" (gKitchen) \
                { channel="knx:device:example:Kitchen:Ceiling_light" }
                Dimmer Kitchen_Spots "Spots" { channel="knx:device:example:Kitchen:Spots" }
                Number:Temperature Kitchen_Temperature "Temperature" <temperature> (gKitchen) \
                ["Measurement", "Temperature"] { channel="knx:device:example:Kitchen:Temperature" }
                Rollershutter Blind "Blind" { channel="knx:device:example:Hall:Blind" }
                """, ProposalText.of(sample(true)).itemsText());
    }

    @Test
    void withoutReadAtStartTheTextHasNoReadMark() {
        ProposalText text = ProposalText.of(sample(false));
        assertFalse(text.thingsText().contains("<"), text.thingsText());
        assertTrue(text.thingsText().contains("ga=\"1.001:31/7/1+31/7/2\""), text.thingsText());
    }

    @Test
    void quotesAndBackslashesAreEscaped() {
        String label = "Say \"hi\" \\ now";
        Channel channel = switchChannel("Kitchen", "Light", label);
        channel = withItem(channel, new Item("Light", "Switch", label, null, List.of(), List.of(), channel.uid()));
        ProposalText text = ProposalText.of(proposal(true, List.of(), thing("Kitchen", label, channel)));
        assertTrue(
                text.thingsText().startsWith(
                        "Thing knx:device:example:Kitchen \"Say \\\"hi\\\" \\\\ now\" (knx:ip:example) {\n"),
                text.thingsText());
        assertTrue(text.thingsText().contains("    Type switch : Light \"Say \\\"hi\\\" \\\\ now\" [ ga="),
                text.thingsText());
        assertEquals("Switch Light \"Say \\\"hi\\\" \\\\ now\" { channel=\"knx:device:example:Kitchen:Light\" }\n",
                text.itemsText());
    }

    @Test
    void aLineBreakInALabelDoesNotBreakTheText() {
        String label = CheckedProposal.cleanLabel("Line\nbreak\r\nhere");
        Channel channel = switchChannel("Kitchen", "Light", label);
        channel = withItem(channel, new Item("Light", "Switch", CheckedProposal.cleanItemLabel("Line\nbreak"), null,
                List.of(), List.of(), channel.uid()));
        ProposalText text = ProposalText.of(proposal(true, List
                .of(new LocationGroup("gRoom", CheckedProposal.cleanItemLabel("Ro\nom"), "Room", null, null, false)),
                thing("Kitchen", label, channel)));
        assertEquals(3, text.thingsText().split("\n").length, text.thingsText());
        assertEquals(3, text.itemsText().split("\n").length, text.itemsText());
        assertTrue(text.thingsText().contains("\"Linebreakhere\""));
        assertTrue(text.itemsText().startsWith("Group gRoom \"Room\" [\"Room\"]\n"));
    }

    @Test
    void bracketsInAnItemLabelBecomeRoundOnes() {
        String label = "Temperature [°C]";
        Channel channel = channel("Kitchen", "T", label, "number", "T", "Number:Temperature",
                part("ga", "9.001", "31/7/8", null, Read.NONE));
        channel = withItem(channel, new Item("T", "Number:Temperature", CheckedProposal.cleanItemLabel(label), null,
                List.of(), List.of(), channel.uid()));
        ProposalText text = ProposalText.of(proposal(true, List.of(), thing("Kitchen", "Kitchen", channel)));
        assertTrue(text.itemsText().startsWith("Number:Temperature T \"Temperature (°C)\" { channel="),
                text.itemsText());
        assertFalse(text.itemsText().contains("["), text.itemsText());
        assertTrue(text.thingsText().contains("\"Temperature [°C]\""), text.thingsText());
    }

    @Test
    void aSerialBridge() {
        Channel light = new Channel("knx:device:bus1:Kitchen:Light", "Light", "Light", "switch",
                List.of(part("ga", "1.001", "31/7/1", null, Read.NONE)),
                new Item("Light", "Switch", "Light", null, List.of(), List.of(), "knx:device:bus1:Kitchen:Light"));
        ProposalText text = ProposalText.of(new CheckedProposal("knx:serial:bus1",
                List.of(new Thing("knx:device:bus1:Kitchen", "Kitchen", "Kitchen", List.of(light))), List.of(), true));
        assertEquals("""
                Thing knx:device:bus1:Kitchen "Kitchen" (knx:serial:bus1) {
                    Type switch : Light "Light" [ ga="1.001:31/7/1" ]
                }
                """, text.thingsText());
        assertEquals("Switch Light \"Light\" { channel=\"knx:device:bus1:Kitchen:Light\" }\n", text.itemsText());
    }

    @Test
    void severalGroupsAndNoRoomGroups() {
        Channel light = switchChannel("Kitchen", "Light", "Light");
        light = withItem(light, new Item("Light", "Switch", "Light", "light", List.of("Switch", "Light"),
                List.of("gA", "gB"), light.uid()));
        ProposalText text = ProposalText
                .of(proposal(true,
                        List.of(new LocationGroup("gA", "A", "Room", null, null, false),
                                new LocationGroup("gB", "B", "Room", null, null, false)),
                        thing("Kitchen", "Kitchen", light)));
        assertEquals("""
                Group gA "A" ["Room"]
                Group gB "B" ["Room"]

                Switch Light "Light" <light> (gA, gB) ["Switch", "Light"] { channel="knx:device:example:Kitchen:Light" }
                """, text.itemsText());
    }

    @Test
    void noThingCarriesAConfigurationAndNothingAnAddress() {
        for (boolean readAtStart : List.of(true, false)) {
            ProposalText text = ProposalText.of(sample(readAtStart));
            for (String line : text.thingsText().split("\n")) {
                if (line.startsWith("Thing")) {
                    assertTrue(THING_LINE.matcher(line).matches(), line);
                }
            }
            assertFalse(text.thingsText().contains("address"), text.thingsText());
            assertFalse(text.itemsText().contains("address"), text.itemsText());
        }
    }

    private static CheckedProposal nested() {
        Channel light = channel("Bath", "Light", "Light", "switch", "Bath_Light", "Switch",
                part("ga", "1.001", "31/7/1", null, Read.NONE));
        light = withItem(light, item("Bath_Light", "Switch", "Light", light.uid(), null, List.of(), List.of("gBath")));
        return proposal(true,
                List.of(new LocationGroup("gHouse", "House", "Building", "house", null, false),
                        new LocationGroup("gUp", "Up", "Floor", "groundfloor", "gHouse", true),
                        new LocationGroup("gBath", "Bath", "Room", "group", "gUp", false),
                        new LocationGroup("gBoard", "Board", null, null, "gHouse", false)),
                thing("Bath", "Bath", light));
    }

    @Test
    void aLocationGroupLineHasIconParentAndTagAndComesAfterItsParent() {
        assertEquals("""
                Group gHouse "House" <house> ["Building"]
                Group gBath "Bath" <group> (gUp) ["Room"]
                Group gBoard "Board" (gHouse)

                Switch Bath_Light "Light" (gBath) { channel="knx:device:example:Bath:Light" }
                """, ProposalText.of(nested()).itemsText());
    }

    // lines for a Thing that exists

    @Test
    void theLinesForAnExistingThingAreItsChannelLinesAndItemsLinkedToTheTarget() {
        CheckedProposal sample = sample(true);
        ProposalText text = ProposalText.of(sample);
        Map<String, String> targets = Map.of(thingUID("Kitchen"), "knx:device:example:wall", thingUID("Hall"),
                "knx:device:hall");

        ProposalText.Lines lines = ProposalText.linesOf(sample, targets, uid -> Set.of());

        List<String> channelLines = text.thingsText().lines().filter(line -> line.startsWith("    Type ")).toList();
        assertEquals(4, channelLines.size());
        assertEquals(
                "// Channels of \"Kitchen\" for the Thing knx:device:example:wall: paste them before its closing brace\n"
                        + String.join("\n", channelLines.subList(0, 3)) + "\n\n"
                        + "// Channels of \"Hall\" for the Thing knx:device:hall: paste them before its closing brace\n"
                        + channelLines.get(3) + "\n",
                lines.thingsText());
        assertEquals(text.itemsText().replace("knx:device:example:Kitchen:", "knx:device:example:wall:")
                .replace("knx:device:example:Hall:", "knx:device:hall:"), lines.itemsText());
        assertEquals(Map.of(), lines.existing());
        assertEquals(0, lines.withoutTarget());
        for (String line : lines.thingsText().lines().toList()) {
            assertFalse(line.startsWith("Thing") || line.startsWith("Bridge") || line.contains("address"), line);
        }
    }

    @Test
    void aChannelIdTheTargetHasAlreadyIsMarkedAndCounted() {
        CheckedProposal sample = sample(true);
        Map<String, String> targets = Map.of(thingUID("Kitchen"), "knx:device:example:wall", thingUID("Hall"),
                "knx:device:example:wall");

        ProposalText.Lines lines = ProposalText.linesOf(sample, targets,
                uid -> "knx:device:example:wall".equals(uid) ? Set.of("Spots", "Blind", "Other") : Set.of());

        assertEquals(Map.of("knx:device:example:wall", List.of("Spots", "Blind")), lines.existing());
        List<String> things = lines.thingsText().lines().toList();
        assertEquals("    // the Thing has a channel Spots already: rename this one", things.get(2));
        assertTrue(things.get(3).startsWith("    Type dimmer : Spots "), things.get(3));
        assertEquals(2, things.stream().filter(line -> line.contains("already")).count());
    }

    @Test
    void twoThingsForOneTargetWithTheSameChannelIdCountAsOneThatExists() {
        Channel first = channel("A", "Light", "Light", "switch", "A_Light", "Switch",
                part("ga", "1.001", "31/7/1", null, Read.NONE));
        Channel second = channel("B", "Light", "Light", "switch", "B_Light", "Switch",
                part("ga", "1.001", "31/7/2", null, Read.NONE));
        CheckedProposal proposal = proposal(true, List.of(), thing("A", "A", first), thing("B", "B", second));

        ProposalText.Lines lines = ProposalText.linesOf(proposal,
                Map.of(thingUID("A"), "knx:device:example:wall", thingUID("B"), "knx:device:example:wall"),
                uid -> Set.of());

        assertEquals(Map.of("knx:device:example:wall", List.of("Light")), lines.existing());
    }

    @Test
    void aThingWithoutTargetHasACommentAndNoLines() {
        CheckedProposal sample = sample(true);

        ProposalText.Lines lines = ProposalText.linesOf(sample, Map.of(thingUID("Hall"), "knx:device:example:wall"),
                uid -> Set.of());

        assertEquals(1, lines.withoutTarget());
        assertTrue(
                lines.thingsText().startsWith("// \"Kitchen\": no existing Thing is chosen\n\n// Channels of \"Hall\""),
                lines.thingsText());
        assertFalse(lines.itemsText().contains("Kitchen_Ceiling_light"));
        assertTrue(lines.itemsText().startsWith("Group gKitchen "));
        assertTrue(lines.itemsText().contains("{ channel=\"knx:device:example:wall:Blind\" }"));
    }
}
