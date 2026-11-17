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

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for the building tree, the functions and the lookups on spaces.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxProjectLocationsTest {

    private final KnxProjectReader reader = new KnxProjectReader();

    private KnxProject read(TestContainer container) throws IOException, KnxProjectException {
        return reader.read(container.write(), null);
    }

    @Test
    void ets4BuildingPartsFormTheTree() throws Exception {
        KnxProject project = read(TestContainer.of("ets4", "P-0A1B"));

        assertEquals(1, project.locations().size());
        KnxSpace house = project.locations().get(0);
        assertEquals("P-0A1B-0_BP-1", house.id());
        assertEquals("Huis", house.name());
        assertEquals("Building", house.type());
        assertEquals(List.of(), house.deviceIds());
        KnxSpace floor = house.spaces().get(0);
        assertEquals("Floor", floor.type());
        KnxSpace room = floor.spaces().get(0);
        assertEquals("Gang", room.name());
        assertEquals("Room", room.type());
        assertEquals(List.of("P-0A1B-0_DI-3"), room.deviceIds());
        assertEquals(List.of(), room.functions());
    }

    @Test
    void ets5SpacesKeepTheirAttributesAndUnknownTypes() throws Exception {
        KnxProject project = read(TestContainer.of("ets5", "P-1C2D"));

        KnxSpace building = project.locations().get(0);
        assertEquals("Appartement", building.name());
        assertEquals("", building.number());
        assertNull(building.usage());
        assertEquals(2, building.spaces().size());
        KnxSpace kitchen = building.spaces().get(0);
        assertEquals("Room", kitchen.type());
        assertEquals("SU-3", kitchen.usage());
        assertNull(kitchen.number());
        assertEquals(List.of("P-1C2D-0_DI-1", "P-1C2D-0_DI-99"), kitchen.deviceIds());
        KnxSpace attic = building.spaces().get(1);
        assertEquals("Attic", attic.type());
        assertEquals("W1", attic.number());
        assertEquals("klein", attic.description());
        assertEquals("SU-9", attic.usage());
    }

    @Test
    void ets6FunctionsKeepTheirRolesAsText() throws Exception {
        KnxProject project = read(TestContainer.of("ets6", "P-3E4F"));

        KnxSpace floor = project.locations().get(0).spaces().get(0);
        KnxSpace hall = floor.spaces().get(0);
        assertEquals("Hal", hall.name());
        assertEquals(2, hall.functions().size());
        KnxFunction light = hall.functions().get(0);
        assertEquals("P-3E4F-0_F-1", light.id());
        assertEquals("Licht hal", light.name());
        assertEquals("FT-1", light.type());
        assertEquals(List.of(
                new KnxFunctionGroupAddress("P-3E4F-0_F-1_GA-1", "Schakelen", "GA-1",
                        "9e2e5b64-0000-4000-8000-00000000a001"),
                new KnxFunctionGroupAddress("P-3E4F-0_F-1_GA-2", "Temperatuur", "GA-2",
                        "9e2e5b64-0000-4000-8000-00000000a002")),
                light.groupAddresses());
        assertEquals("Leeg ding", hall.functions().get(1).name());
        assertEquals(List.of(), hall.functions().get(1).groupAddresses());
    }

    @Test
    void functionDirectlyInAFloorBelongsToTheFloor() throws Exception {
        KnxProject project = read(TestContainer.of("ets6", "P-3E4F"));

        KnxSpace floor = project.locations().get(0).spaces().get(0);
        assertEquals(1, floor.functions().size());
        assertEquals("Licht verdieping", floor.functions().get(0).name());
        assertEquals(List.of(),
                floor.spaces().get(0).functions().stream().filter(f -> "Licht verdieping".equals(f.name())).toList());
        assertEquals(floor.functions(), project.functionsOf(floor));
    }

    @Test
    void schema22FunctionHasAWordRole() throws Exception {
        KnxProject project = read(TestContainer.of("ets6-free", "P-5A6B"));

        KnxSpace floor = project.locations().get(0).spaces().get(0);
        assertEquals("Floor", floor.type());
        assertEquals("Licht gang", floor.functions().get(0).name());
        KnxFunctionGroupAddress ref = floor.functions().get(0).groupAddresses().get(0);
        assertEquals("SwitchOnOff", ref.role());
        assertEquals("Vrij zeventien", Objects.requireNonNull(project.groupAddress(ref.groupAddressId())).name());
    }

    @Test
    void functionGroupAddressResolvesThroughTheProject() throws Exception {
        KnxProject project = read(TestContainer.of("ets6", "P-3E4F"));
        KnxFunction light = project.locations().get(0).spaces().get(0).spaces().get(0).functions().get(0);

        KnxGroupAddress address = project.groupAddress(light.groupAddresses().get(1).groupAddressId());

        assertNotNull(address);
        assertEquals("Hal temperatuur", address.name());
    }

    @Test
    void spaceOfADeviceAndDevicesInASpace() throws Exception {
        KnxProject project = read(TestContainer.of("ets5", "P-1C2D"));
        KnxDevice actuator = project.device(new KnxIndividualAddress(2, 3, 10));
        KnxDevice thermostat = project.device(new KnxIndividualAddress(2, 3, 11));
        assertNotNull(actuator);
        assertNotNull(thermostat);

        KnxSpace kitchen = project.spaceOf(actuator);

        assertNotNull(kitchen);
        assertEquals("Keuken", kitchen.name());
        assertNull(project.spaceOf(thermostat));
        // the second reference names no device of the project and is left out
        assertEquals(List.of(actuator), project.devicesIn(kitchen));
        assertEquals(List.of(), project.devicesIn(project.locations().get(0)));
    }

    @Test
    void deviceInANestedSpaceIsFound() throws Exception {
        KnxProject project = read(TestContainer.of("ets6", "P-3E4F"));
        KnxDevice button = project.device(new KnxIndividualAddress(1, 1, 20));
        assertNotNull(button);

        KnxSpace hall = project.spaceOf(button);

        assertNotNull(hall);
        assertEquals("Hal", hall.name());
        assertNull(project.spaceOf(Objects.requireNonNull(project.device(new KnxIndividualAddress(1, 1, 1)))));
    }

    @Test
    void projectWithoutLocationsHasNoSpaces() throws Exception {
        TestContainer container = TestContainer.of("ets5", "P-1C2D");
        String xml = container.text("P-1C2D/0.xml");
        container.put("P-1C2D/0.xml",
                xml.substring(0, xml.indexOf("<Locations>")) + xml.substring(xml.indexOf("</Locations>") + 12));

        KnxProject project = read(container);

        assertEquals(List.of(), project.locations());
        assertNull(project.spaceOf(project.topology().devices().get(0)));
    }

    @Test
    void spaceDepthIsNotLimitedToTheDepthsOfTheSamples() throws Exception {
        TestContainer container = TestContainer.of("ets5", "P-1C2D");
        container.put("P-1C2D/0.xml", container.text("P-1C2D/0.xml").replace("<Space Id=\"P-1C2D-0_BP-2\"",
                "<Space Id=\"x1\" Name=\"A\" Type=\"Floor\"><Space Id=\"x2\" Name=\"B\" Type=\"Floor\"><Space Id=\"x3\" Name=\"C\" Type=\"Floor\" /></Space></Space><Space Id=\"P-1C2D-0_BP-2\""));

        KnxSpace building = read(container).locations().get(0);

        assertEquals(List.of("A", "Keuken", "Wasruimte"), building.spaces().stream().map(KnxSpace::name).toList());
        assertEquals("C", building.spaces().get(0).spaces().get(0).spaces().get(0).name());
    }

    /** KNX, Project, Installations, Installation and Locations are above the first space. */
    private static final int LEVELS_ABOVE_THE_SPACES = 5;

    private static TestContainer withNestedSpaces(int levels) {
        TestContainer container = TestContainer.of("ets5", "P-1C2D");
        String xml = container.text("P-1C2D/0.xml");
        String open = "<Space Id=\"d\" Name=\"deep\" Type=\"Room\">".repeat(levels);
        String close = "</Space>".repeat(levels);
        return container.put("P-1C2D/0.xml", xml.replace("</Locations>", open + close + "</Locations>"));
    }

    @Test
    void theDeepestSpaceNestingTheDefaultDepthAllowsIsReadWithoutRecursion() throws Exception {
        int levels = ReadLimits.MAX_DEPTH - LEVELS_ABOVE_THE_SPACES;

        KnxProject project = read(withNestedSpaces(levels));
        KnxSpace space = project.locations().get(1);

        int depth = 0;
        while (!space.spaces().isEmpty()) {
            space = space.spaces().get(0);
            depth++;
        }
        assertEquals(levels - 1, depth);
        assertTrue(project.toString().length() > levels);
    }

    @Test
    void oneSpaceLevelMoreThanTheDefaultDepthAllowsIsRefused() {
        TestContainer container = withNestedSpaces(ReadLimits.MAX_DEPTH - LEVELS_ABOVE_THE_SPACES + 1);

        KnxProjectException e = assertThrows(KnxProjectException.class, () -> read(container));
        assertEquals(KnxProjectException.Reason.LIMIT_EXCEEDED, e.getReason());
    }
}
