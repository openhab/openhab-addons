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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.project.ChannelFixtures.Obj;
import org.openhab.binding.knx.internal.project.KnxProjectCheck.Difference;
import org.openhab.binding.knx.internal.project.KnxProjectCheck.Kind;

/**
 * Tests for the comparison of configured channel addresses with the project. All data is made up.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxProjectCheckTest {

    private static final int G1 = raw(1, 2, 3);
    private static final int G2 = raw(1, 2, 4);
    private static final int F1 = raw(31, 7, 1);
    private static final int F2 = raw(31, 7, 2);

    private static KnxConfiguredAddress configured(int rawAddress, @Nullable String dpt, boolean read) {
        return new KnxConfiguredAddress(rawAddress, dpt, read, true, "thing:channel");
    }

    private static List<Difference> check(KnxProject project, KnxConfiguredAddress... addresses) {
        return new KnxProjectCheck(project).check(List.of(addresses));
    }

    @Test
    void addressNotInProject() {
        KnxProject project = project(List.of(address(1, G1, "1.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 1)));
        List<Difference> found = check(project, configured(G2, "1.001", true));
        assertEquals(1, found.size());
        Difference d = found.get(0);
        assertEquals(Kind.ADDRESS_NOT_IN_PROJECT, d.kind());
        assertEquals(G2, d.configured().rawAddress());
        assertNull(d.projectAddress());
        assertNull(d.projectType());
        assertFalse(d.noteOnly());
    }

    @Test
    void addressWithoutObject() {
        KnxProject project = project(List.of(address(1, G1, "1.001")), List.of());
        List<Difference> found = check(project, configured(G1, "1.001", false));
        assertEquals(1, found.size());
        assertEquals(Kind.ADDRESS_WITHOUT_OBJECT, found.get(0).kind());
        KnxGroupAddress inProject = found.get(0).projectAddress();
        assertNotNull(inProject);
        assertEquals(G1, inProject.rawAddress());
    }

    @Test
    void matchingAddressGivesNothing() {
        KnxProject project = project(List.of(address(1, G1, "9.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 1).read()));
        assertEquals(List.of(), check(project, configured(G1, "9.001", true)));
    }

    @Test
    void mainNumberDiffers() {
        KnxProject project = project(List.of(address(1, G1, "9.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 1)));
        List<Difference> found = check(project, configured(G1, "1.001", false));
        assertEquals(1, found.size());
        assertEquals(Kind.DATAPOINT_TYPE_DIFFERS, found.get(0).kind());
        assertFalse(found.get(0).noteOnly());
        assertEquals(new KnxDatapointType(9, 1), found.get(0).projectType());
    }

    @Test
    void onlyTheSubNumberDiffersIsANoteOnly() {
        KnxProject project = project(List.of(address(1, G1, "9.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 1)));
        List<Difference> found = check(project, configured(G1, "9.002", false));
        assertEquals(1, found.size());
        assertEquals(Kind.DATAPOINT_TYPE_DIFFERS, found.get(0).kind());
        assertTrue(found.get(0).noteOnly());
    }

    @Test
    void noTypeOnOneSideIsNoDifference() {
        KnxProject project = project(List.of(address(1, G1, "9.001"), address(2, G2, null)),
                List.of(object("O-1_R-1", 1, "A", null, null, 1, 2), object("O-2_R-2", 2, "B", null, null, 2)));
        assertEquals(List.of(), check(project, configured(G1, null, false), configured(G2, "9.001", false)));
    }

    @Test
    void typeWithoutSubNumberInTheProjectWithTheSameMainIsNoDifference() {
        KnxProject project = project(List.of(address(1, G1, "9")), List.of(object("O-1_R-1", 1, "A", null, null, 1)));
        assertEquals(List.of(), check(project, configured(G1, "9.001", false)));
        assertEquals(1, check(project, configured(G1, "14.001", false)).size());
    }

    @Test
    void projectTypeFollowsTheRuleOfTheChannelText() {
        // the address has no type, its objects agree on one
        KnxProject project = project(List.of(address(1, G1, null)),
                List.of(object("O-1_R-1", 1, "A", null, "9.001", 1), object("O-2_R-2", 2, "B", null, "9.001", 1)));
        List<Difference> found = check(project, configured(G1, "1.001", false));
        assertEquals(1, found.size());
        assertEquals(new KnxDatapointType(9, 1), found.get(0).projectType());
    }

    @Test
    void configuredTypeThatDoesNotParseCountsAsNone() {
        KnxProject project = project(List.of(address(1, G1, "9.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 1)));
        assertEquals(List.of(), check(project, configured(G1, "nonsense", false)));
        assertEquals(List.of(), check(project, configured(G1, "", false)));
    }

    @Test
    void readWithoutReadFlag() {
        KnxProject project = project(List.of(address(1, G1, "1.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 1)));
        List<Difference> found = check(project, configured(G1, "1.001", true));
        assertEquals(1, found.size());
        assertEquals(Kind.READ_WITHOUT_READ_FLAG, found.get(0).kind());
    }

    @Test
    void readWithReadButWithoutCommunicationFlag() {
        KnxProject project = project(List.of(address(1, G1, "1.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 1).read().noCommunication()));
        assertEquals(Kind.READ_WITHOUT_READ_FLAG, check(project, configured(G1, "1.001", true)).get(0).kind());
    }

    @Test
    void readIsFineWhenAnyLinkedObjectCanRead() {
        KnxProject project = project(List.of(address(1, G1, "1.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 1), object("O-2_R-2", 2, "B", null, null, 1).read()));
        assertEquals(List.of(), check(project, configured(G1, "1.001", true)));
    }

    @Test
    void noReadFlagDifferenceWhenReadIsNotConfigured() {
        KnxProject project = project(List.of(address(1, G1, "1.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 1)));
        assertEquals(List.of(), check(project, configured(G1, "1.001", false)));
    }

    @Test
    void noReadFlagDifferenceWhenTheFlagsAreUnknown() {
        KnxProject project = project(List.of(address(1, G1, "1.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 1).definition(KnxObjectDefinition.NO_PROGRAM_FILE)));
        assertEquals(List.of(), check(project, configured(G1, "1.001", true)));
    }

    @Test
    void noReadFlagDifferenceWhileAnObjectOfUnknownDefinitionMayAnswer() {
        KnxProject project = project(List.of(address(1, G1, "1.001")), List.of(object("O-1_R-1", 1, "A", null, null, 1),
                object("O-2_R-2", 2, "B", null, null, 1).definition(KnxObjectDefinition.NO_PROGRAM_FILE)));
        assertEquals(List.of(), check(project, configured(G1, "1.001", true)));
    }

    @Test
    void readWithoutReadFlagWhenEveryLinkedObjectIsKnownAndNoneCanRead() {
        KnxProject project = project(List.of(address(1, G1, "1.001")), List.of(object("O-1_R-1", 1, "A", null, null, 1),
                object("O-2_R-2", 2, "B", null, null, 1).read().noCommunication()));
        List<Difference> found = check(project, configured(G1, "1.001", true));
        assertEquals(1, found.size());
        assertEquals(Kind.READ_WITHOUT_READ_FLAG, found.get(0).kind());
    }

    @Test
    void readOnAFurtherLinkOfAReadableObjectIsReported() {
        KnxProject project = project(List.of(address(1, F1, "1.001"), address(2, F2, "1.001")),
                List.of(object("O-1", 1, "Value", null, null, 1, 2).flags("CRW--")));
        assertEquals(List.of(), check(project, configured(F1, "1.001", true)));
        List<Difference> found = check(project, configured(F2, "1.001", true));
        assertEquals(List.of(Kind.READ_WITHOUT_READ_FLAG), found.stream().map(Difference::kind).toList());
        assertEquals(F2, found.get(0).configured().rawAddress());
    }

    @Test
    void readOnAnAddressOnlyAFilterDeviceAnswersIsReported() {
        Obj value = object("O-F", 1, "Value", null, null, 1).flags("CRWT-");
        KnxProject filterOnly = projectOf(List.of(address(1, F1, "1.001")),
                List.of(device("", null, value).of(filterProduct())), List.of());
        assertEquals(List.of(Kind.READ_WITHOUT_READ_FLAG),
                check(filterOnly, configured(F1, "1.001", true)).stream().map(Difference::kind).toList());

        KnxProject withSensor = projectOf(List.of(address(1, F1, "1.001")),
                List.of(device("", null, value).of(filterProduct()),
                        device("Sensor", null, object("O-1", 1, "Value", null, null, 1).flags("CR-T-"))),
                List.of());
        assertEquals(List.of(), check(withSensor, configured(F1, "1.001", true)));
    }

    @Test
    void noReadFlagDifferenceOnAFurtherLinkWhileAnObjectOfUnknownDefinitionMayAnswer() {
        KnxProject project = project(List.of(address(1, F1, "1.001"), address(2, F2, "1.001")),
                List.of(object("O-1", 1, "Value", null, null, 1, 2).flags("CRW--"),
                        object("O-2", 2, "Other", null, null, 2).definition(KnxObjectDefinition.NO_PROGRAM_FILE)));
        assertEquals(List.of(), check(project, configured(F2, "1.001", true)));
    }

    @Test
    void oneDifferencePerCause() {
        KnxProject project = project(List.of(address(1, G1, "9.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 1)));
        List<Difference> found = check(project, configured(G1, "1.001", true));
        assertEquals(List.of(Kind.DATAPOINT_TYPE_DIFFERS, Kind.READ_WITHOUT_READ_FLAG),
                found.stream().map(Difference::kind).toList());
        // an address that is not in the project gives that one difference and nothing else
        assertEquals(1, check(project, configured(G2, "1.001", true)).size());
        // no object: no read flag difference, but the type still can differ
        KnxProject without = project(List.of(address(1, G1, "9.001")), List.of());
        assertEquals(List.of(Kind.ADDRESS_WITHOUT_OBJECT, Kind.DATAPOINT_TYPE_DIFFERS),
                check(without, configured(G1, "1.001", true)).stream().map(Difference::kind).toList());
    }

    @Test
    void severalAddressesKeepTheirOrder() {
        KnxProject project = project(List.of(address(1, G1, "1.001")),
                List.of(object("O-1_R-1", 1, "A", null, null, 1)));
        List<Difference> found = check(project, configured(G2, null, false), configured(G1, "9.001", false));
        assertEquals(List.of(Kind.ADDRESS_NOT_IN_PROJECT, Kind.DATAPOINT_TYPE_DIFFERS),
                found.stream().map(Difference::kind).toList());
    }
}
