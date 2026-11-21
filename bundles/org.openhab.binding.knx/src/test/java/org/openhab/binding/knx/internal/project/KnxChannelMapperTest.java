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
package org.openhab.binding.knx.internal.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.knx.internal.project.ChannelFixtures.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.knx.internal.project.ChannelFixtures.Obj;

/**
 * Tests for the channel text of group addresses and devices. All data is made up.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxChannelMapperTest {

    private static final int G1 = raw(1, 2, 3);
    private static final int G2 = raw(1, 2, 4);
    private static final String INPUT = "C-W--";

    private static int ga(int sub) {
        return raw(31, 7, sub);
    }

    /** Switch addresses {@code 31/7/<sub>} with the id {@code P-1_GA-<sub>}. */
    private static List<KnxGroupAddress> switches(int... subs) {
        return IntStream.of(subs).mapToObj(sub -> address(sub, ga(sub), "1.001")).toList();
    }

    private static KnxChannelSuggestion forAddress(KnxProject project, int rawAddress) {
        KnxGroupAddress address = project.groupAddress(rawAddress);
        assertNotNull(address);
        return new KnxChannelMapper(project).forGroupAddress(address);
    }

    @Test
    void typeOfTheAddressItself() {
        KnxProject project = project(List.of(address(1, G1, "9.001")), List.of());
        KnxChannelSuggestion s = forAddress(project, G1);
        assertEquals("9.001:1/2/3", s.value());
        assertEquals("ga", s.key());
        assertEquals("ga=\"9.001:1/2/3\"", s.text());
        assertEquals("number", s.channelType());
        assertNull(s.groupObject());
    }

    @Test
    void typeFromTheObjectsWhenTheAddressHasNone() {
        KnxProject project = project(List.of(address(1, G1, null)),
                List.of(object("O-1_R-1", 1, "A", null, "9.001", 1), object("O-2_R-2", 2, "B", null, "9.001", 1)));
        KnxChannelSuggestion s = forAddress(project, G1);
        assertEquals("9.001:1/2/3", s.value());
        assertEquals(List.of(), s.notes());
    }

    @Test
    void noTypeWhenTheObjectsDisagree() {
        KnxProject project = project(List.of(address(1, G1, null)),
                List.of(object("O-1_R-1", 1, "A", null, "9.001", 1), object("O-2_R-2", 2, "B", null, "9.002", 1)));
        KnxChannelSuggestion s = forAddress(project, G1);
        assertEquals("1/2/3", s.value());
        assertNull(s.channelType());
        assertEquals("ga", s.key());
        assertTrue(s.notes().contains(KnxChannelMapper.NOTE_OBJECTS_DISAGREE));
        assertFalse(s.notes().contains(KnxChannelMapper.NOTE_NO_LINKED_OBJECT));
    }

    @Test
    void objectsWithoutOwnTypeDoNotCountAsDisagreeing() {
        KnxProject project = project(List.of(address(1, G1, null)),
                List.of(object("O-1_R-1", 1, "A", null, "9.001", 1), object("O-2_R-2", 2, "B", null, null, 1)));
        assertEquals("9.001:1/2/3", forAddress(project, G1).value());
    }

    @Test
    void noTypeAndNoObject() {
        KnxProject project = project(List.of(address(1, G1, null)), List.of());
        KnxChannelSuggestion s = forAddress(project, G1);
        assertEquals("1/2/3", s.value());
        assertNull(s.channelType());
        assertEquals(List.of(KnxChannelMapper.NOTE_NO_DATA_TYPE, KnxChannelMapper.NOTE_NO_LINKED_OBJECT), s.notes());
    }

    @Test
    void noTypeButObjects() {
        KnxProject project = project(List.of(address(1, G1, null)), List.of(object("O-1_R-1", 1, "A", null, null, 1)));
        KnxChannelSuggestion s = forAddress(project, G1);
        assertEquals(List.of(KnxChannelMapper.NOTE_NO_DATA_TYPE), s.notes());
    }

    @Test
    void typeWithoutSubNumberTakesTheFullTypeTheObjectsAgreeOn() {
        KnxProject project = project(List.of(address(1, G1, "9")),
                List.of(object("O-1_R-1", 1, "A", null, "9.001", 1), object("O-2_R-2", 2, "B", null, "9.001", 1)));
        KnxChannelSuggestion s = forAddress(project, G1);
        assertEquals("9.001:1/2/3", s.value());
        assertEquals("number", s.channelType());
    }

    @Test
    void typeWithoutSubNumberAndObjectsOfAnotherMainNumber() {
        KnxProject project = project(List.of(address(1, G1, "9")),
                List.of(object("O-1_R-1", 1, "A", null, "14.001", 1)));
        KnxChannelSuggestion s = forAddress(project, G1);
        assertEquals("1/2/3", s.value());
        assertNull(s.channelType());
        assertEquals(List.of(KnxChannelMapper.NOTE_NO_SUB_NUMBER), s.notes());
    }

    @Test
    void typeWithoutSubNumberAndNoObject() {
        KnxProject project = project(List.of(address(1, G1, "9")), List.of());
        KnxChannelSuggestion s = forAddress(project, G1);
        assertEquals("1/2/3", s.value());
        assertNull(s.channelType());
        assertTrue(s.notes().contains(KnxChannelMapper.NOTE_NO_SUB_NUMBER));
    }

    @Test
    void typeWithoutSubNumberOnTheObjectsOnly() {
        KnxProject project = project(List.of(address(1, G1, null)), List.of(object("O-1_R-1", 1, "A", null, "9", 1)));
        KnxChannelSuggestion s = forAddress(project, G1);
        assertEquals("1/2/3", s.value());
        assertEquals(List.of(KnxChannelMapper.NOTE_NO_SUB_NUMBER), s.notes());
    }

    @Test
    void anActuatorInputLinkedToTwoAddressesGetsNoFurtherAddress() {
        KnxProject project = project(switches(1, 9), List.of(object("O-1", 1, "Input", null, null, 1, 9).flags(INPUT)));
        assertEquals("1.001:31/7/1", forAddress(project, ga(1)).value());
        assertEquals("1.001:31/7/9", forAddress(project, ga(9)).value());
    }

    private static Obj filterObject() {
        List<Integer> links = new ArrayList<>(List.of(20));
        for (int sub = 21; sub <= 40; sub++) {
            links.add(sub);
        }
        return new Obj("O-F", 1, "Filter", null, links, null).flags("CRWT-");
    }

    @Test
    void aFilterObjectWithTwentyFurtherLinksGetsNoFurtherAddressAndIsReadOnItsSendingAddressOnly() {
        KnxProject project = project(switches(IntStream.rangeClosed(20, 40).toArray()), List.of(filterObject()));
        assertEquals("1.001:<31/7/20", forAddress(project, ga(20)).value());
        for (int sub = 21; sub <= 40; sub++) {
            assertEquals("1.001:31/7/" + sub, forAddress(project, ga(sub)).value());
        }
    }

    @Test
    void aFilterObjectAddsNothingToTheAddressOfAnActuatorInput() {
        KnxProject project = projectOfDevices(KnxGroupAddressStyle.THREE_LEVEL,
                switches(IntStream.concat(IntStream.of(9), IntStream.rangeClosed(20, 40)).toArray()),
                List.of(List.of(filterObject()), List.of(object("O-1", 1, "Input", null, null, 21, 9).flags(INPUT))));
        assertEquals("1.001:31/7/21", forAddress(project, ga(21)).value());
    }

    @Test
    void aStatusAddressADisplayListensOnGetsNoFurtherAddress() {
        KnxProject project = projectOfDevices(KnxGroupAddressStyle.THREE_LEVEL, switches(2, 3),
                List.of(List.of(object("O-1", 1, "Status", null, null, 2).flags("CR-T-")),
                        List.of(object("O-2", 1, "Display", null, null, 2, 3).flags(INPUT))));
        assertEquals("1.001:<31/7/2", forAddress(project, ga(2)).value());
        assertEquals("1.001:31/7/3", forAddress(project, ga(3)).value());
    }

    @Test
    void noSuggestionOfTheProjectHasAFurtherAddress() {
        KnxProject project = project(switches(1, 4, 5),
                List.of(object("O-1", 1, "Input A", null, null, 5, 1, 4, 5, 1).flags(INPUT),
                        object("O-2", 2, "Input B", null, null, 4, 5, 1).flags(INPUT),
                        object("O-3", 3, "Value", null, null, 1, 4).flags("CRW--"),
                        object("O-4", 4, "Button", null, null, 4, 5).flags("C-WTU")));
        KnxChannelMapper mapper = new KnxChannelMapper(project);
        for (KnxGroupAddress address : project.groupAddresses()) {
            assertFalse(mapper.forGroupAddress(address).value().contains("+"), address.address());
        }
        for (KnxChannelSuggestion entry : mapper.forDevice(project.topology().devices().get(0))) {
            assertFalse(entry.value().contains("+"), entry.value());
        }
    }

    @Test
    void readMarkOnTheMainAddress() {
        KnxProject project = project(List.of(address(1, G1, "1.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 1).read()));
        assertEquals("1.001:<1/2/3", forAddress(project, G1).value());
    }

    @Test
    void noReadMarkWithoutTheReadFlag() {
        KnxProject project = project(List.of(address(1, G1, "1.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 1)));
        assertEquals("1.001:1/2/3", forAddress(project, G1).value());
    }

    @Test
    void noReadMarkWithReadButWithoutCommunicationFlag() {
        KnxProject project = project(List.of(address(1, G1, "1.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 1).read().noCommunication()));
        assertEquals("1.001:1/2/3", forAddress(project, G1).value());
    }

    @Test
    void readMarkOnTheSendingAddressOnlyNotOnAFurtherLink() {
        KnxProject project = project(switches(1, 9),
                List.of(object("O-1", 1, "Value", null, null, 1, 9).flags("CRW--")));
        assertEquals("1.001:<31/7/1", forAddress(project, ga(1)).value());
        assertEquals("1.001:31/7/9", forAddress(project, ga(9)).value());
    }

    @Test
    void readMarkOnAnAddressThatIsAFurtherLinkOfOneObjectAndTheSendingAddressOfAnother() {
        KnxProject project = project(switches(1, 9), List.of(object("O-1", 1, "Input", null, null, 1, 9).flags("CRW--"),
                object("O-2", 2, "Value", null, null, 9).read()));
        assertEquals("1.001:<31/7/9", forAddress(project, ga(9)).value());
    }

    @Test
    void noReadMarkOnASendingAddressWhenTheObjectLacksCommunication() {
        KnxProject project = project(switches(1, 9),
                List.of(object("O-1", 1, "Value", null, null, 1, 9).flags("-RW--")));
        assertEquals("1.001:31/7/1", forAddress(project, ga(1)).value());
        assertEquals("1.001:31/7/9", forAddress(project, ga(9)).value());
    }

    @Test
    void anObjectWithoutASendingAddressIsReadOnItsFirstLink() {
        KnxProject project = project(switches(1, 9),
                List.of(object("O-1", 1, "Value", null, null, 9, 1).flags("CRW--").receiveOnly()));
        assertEquals("1.001:<31/7/9", forAddress(project, ga(9)).value());
        assertEquals("1.001:31/7/1", forAddress(project, ga(1)).value());
    }

    @Test
    void noReadMarkOnAnAddressOnlyAFilterDeviceAnswers() {
        KnxProject project = projectOf(switches(1),
                List.of(device("", null, object("O-F", 1, "Value", null, null, 1).flags("CRWT-")).of(filterProduct())),
                List.of());
        assertEquals("1.001:31/7/1", forAddress(project, ga(1)).value());
    }

    @Test
    void readMarkWhenARealDeviceAnswersNextToAFilterDevice() {
        KnxProject project = projectOf(switches(1),
                List.of(device("", null, object("O-F", 1, "Value", null, null, 1).flags("CRWT-")).of(filterProduct()),
                        device("Sensor", null, object("O-1", 1, "Value", null, null, 1).flags("CR-T-"))),
                List.of());
        assertEquals("1.001:<31/7/1", forAddress(project, ga(1)).value());
    }

    @Test
    void aFilterDeviceStillGetsItsEntriesJustWithoutReadMark() {
        KnxProject project = projectOf(switches(1, 2), List
                .of(device("", null, object("O-F", 1, "Value", null, null, 1, 2).flags("CRWT-")).of(filterProduct())),
                List.of());
        List<KnxChannelSuggestion> entries = new KnxChannelMapper(project)
                .forDevice(project.topology().devices().get(0));
        assertEquals(List.of("1.001:31/7/1"), entries.stream().map(KnxChannelSuggestion::value).toList());
    }

    @Test
    void textWithoutADataTypeHasNoTypePrefix() {
        KnxProject project = project(List.of(address(1, G1, null), address(2, G2, null)),
                List.of(object("O-1_R-1", 1, "A", null, null, 1, 2).flags("CRW--")));
        assertEquals("<1/2/3", forAddress(project, G1).value());
        assertEquals("1/2/4", forAddress(project, G2).value());
    }

    @Test
    void addressesAreWrittenInTheStyleOfTheProject() {
        KnxProject two = project(KnxGroupAddressStyle.TWO_LEVEL,
                List.of(address(KnxGroupAddressStyle.TWO_LEVEL, 1, G1, "1.001")), List.of());
        assertEquals("1.001:1/515", forAddress(two, G1).value());
        KnxProject free = project(KnxGroupAddressStyle.FREE,
                List.of(address(KnxGroupAddressStyle.FREE, 1, G1, "1.001")), List.of());
        assertEquals("1.001:" + G1, forAddress(free, G1).value());
    }

    @Test
    void deviceEntryPerLinkedObject() {
        KnxProject project = project(List.of(address(1, G1, "1.001"), address(2, G2, "9.001")),
                List.of(object("O-1_R-1", 3, "Switch", "On/Off", null, 1), object("O-2_R-2", 4, "Unused", null, null),
                        object("O-3_R-3", 7, "Temperature", null, null, 2).read()));
        KnxDevice device = project.topology().devices().get(0);
        List<KnxChannelSuggestion> entries = new KnxChannelMapper(project).forDevice(device);
        assertEquals(2, entries.size());
        assertEquals("3 – Switch – On/Off", entries.get(0).label());
        assertEquals("ga=\"1.001:1/2/3\"", entries.get(0).text());
        assertEquals("switch", entries.get(0).channelType());
        assertEquals(project.groupObjectsOf(device).get(0), entries.get(0).groupObject());
        assertEquals("7 – Temperature", entries.get(1).label());
        assertEquals("ga=\"9.001:<1/2/4\"", entries.get(1).text());
        assertEquals("number", entries.get(1).channelType());
    }

    @Test
    void deviceEntryLabelLeavesMissingPartsOut() {
        KnxProject project = project(List.of(address(1, G1, "1.001")),
                List.of(object("O-1_R-1", null, "Switch", null, null, 1), object("O-2_R-2", 5, "", "Fn", null, 1)));
        List<KnxChannelSuggestion> entries = new KnxChannelMapper(project)
                .forDevice(project.topology().devices().get(0));
        assertEquals("Switch", entries.get(0).label());
        assertEquals("5 – Fn", entries.get(1).label());
    }

    @Test
    void deviceEntryOfAReceiveOnlyObjectUsesItsFirstLink() {
        KnxProject project = project(List.of(address(1, G1, "1.001"), address(2, G2, "1.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 2, 1).receiveOnly()));
        List<KnxChannelSuggestion> entries = new KnxChannelMapper(project)
                .forDevice(project.topology().devices().get(0));
        assertEquals(1, entries.size());
        assertEquals(G2, entries.get(0).groupAddress().rawAddress());
    }

    @Test
    void deviceEntryTextIsTheTextOfItsSendingAddress() {
        KnxProject project = project(List.of(address(1, G1, "1.001"), address(2, G2, "1.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 1, 2).flags("CRW--")));
        List<KnxChannelSuggestion> entries = new KnxChannelMapper(project)
                .forDevice(project.topology().devices().get(0));
        assertEquals("1.001:<1/2/3", entries.get(0).value());
    }

    @Test
    void deviceWithoutLinkedObjectsHasNoEntry() {
        KnxProject project = project(List.of(address(1, G1, "1.001")), List.of(object("O-1_R-1", 1, "A", null, null)));
        assertEquals(List.of(), new KnxChannelMapper(project).forDevice(project.topology().devices().get(0)));
    }

    @Test
    void objectLinkedToAnAddressOutsideTheProjectHasNoEntry() {
        KnxProject project = project(List.of(address(1, G1, "1.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 99)));
        assertEquals(List.of(), new KnxChannelMapper(project).forDevice(project.topology().devices().get(0)));
    }

    @Test
    void projectWithoutManufacturerData() {
        KnxProject project = project(List.of(address(1, G1, "9.001")),
                List.of(object("O-1_R-1", null, "", null, null, 1).definition(KnxObjectDefinition.NO_HARDWARE)));
        KnxChannelSuggestion s = forAddress(project, G1);
        assertEquals("9.001:1/2/3", s.value());
        assertEquals("number", s.channelType());
        List<KnxChannelSuggestion> entries = new KnxChannelMapper(project)
                .forDevice(project.topology().devices().get(0));
        assertEquals("ga=\"9.001:1/2/3\"", entries.get(0).text());
        assertEquals("", entries.get(0).label());
    }

    @ParameterizedTest
    @CsvSource({ "1.001,switch,ga", "1.002,switch,ga", "1.003,switch,ga", "1.008,rollershutter,upDown",
            "1.009,contact,ga", "1.019,contact,ga", "5.001,number,ga", "9.001,number,ga", "14.056,number,ga",
            "16.000,string,ga", "16.001,string,ga", "28.001,string,ga", "10.001,datetime,ga", "11.001,datetime,ga",
            "19.001,datetime,ga", "232.600,color,hsb", "3.007,dimmer,increaseDecrease" })
    void tableRows(String dpt, String channelType, String key) {
        KnxProject project = project(List.of(address(1, G1, dpt)), List.of());
        KnxChannelSuggestion s = forAddress(project, G1);
        assertEquals(channelType, s.channelType());
        assertEquals(key, s.key());
        assertEquals(key + "=\"" + dpt + ":1/2/3\"", s.text());
    }

    @ParameterizedTest
    @CsvSource({ "1.010", "1.022", "6.020", "2.001", "4.001", "20.102", "3.008", "999.999" })
    void typesWithoutASuggestionStillPrintTheText(String dpt) {
        KnxProject project = project(List.of(address(1, G1, dpt)), List.of());
        KnxChannelSuggestion s = forAddress(project, G1);
        assertNull(s.channelType());
        assertEquals("ga", s.key());
        assertEquals("ga=\"" + dpt + ":1/2/3\"", s.text());
    }

    @Test
    void suggestionListsAreNotModifiable() {
        KnxProject project = project(List.of(address(1, G1, null)), List.of());
        assertThrows(UnsupportedOperationException.class, () -> forAddress(project, G1).notes().add("x"));
    }
}
