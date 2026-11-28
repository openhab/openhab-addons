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
 * Tests for the object instances, parameter values and module instances of devices, and the lookups on the
 * project that use them.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxProjectObjectsTest {

    private final KnxProjectReader reader = new KnxProjectReader();

    private KnxProject read(TestContainer container) throws IOException, KnxProjectException {
        return reader.read(container.write(), null);
    }

    private static KnxDevice device(KnxProject project, String address) {
        return project.topology().devices().stream().filter(d -> address.equals(String.valueOf(d.individualAddress())))
                .findFirst().orElseThrow();
    }

    private static KnxGroupAddress address(KnxProject project, int raw) {
        return Objects.requireNonNull(project.groupAddress(raw));
    }

    private static KnxGroupAddress address(KnxProject project, String id) {
        return Objects.requireNonNull(project.groupAddress(id));
    }

    @Test
    void ets4ConnectorsGiveTheSendingAddressFirst() throws Exception {
        KnxProject project = read(TestContainer.of("ets4", "P-0A1B"));
        KnxDevice device = device(project, "1.1.5");

        assertEquals(3, device.objects().size());
        KnxObjectInstance first = device.objects().get(0);
        assertEquals("M-00FA_A-0022-33-4455_O-0_R-1", first.refId());
        assertEquals(List.of("GA-1", "GA-2"), first.groupAddressIds());
        assertEquals("GA-1", first.sendingGroupAddressId());
        assertTrue(first.isLinked());

        KnxObjectInstance second = device.objects().get(1);
        assertEquals("M-00FA_A-0022-33-4455_O-1_R-2", second.refId());
        assertEquals(List.of("GA-2", "GA-3"), second.groupAddressIds());
        assertEquals("GA-2", second.sendingGroupAddressId());

        KnxObjectInstance unlinked = device.objects().get(2);
        assertEquals(List.of(), unlinked.groupAddressIds());
        assertNull(unlinked.sendingGroupAddressId());
        assertFalse(unlinked.isLinked());
        assertEquals(List.of(), device(project, "1.1.12").objects());
    }

    @Test
    void ets4ReceiveOnlyObjectHasNoSender() throws Exception {
        TestContainer container = TestContainer.of("ets4", "P-0A1B");
        container.put("P-0A1B/0.xml", container.text("P-0A1B/0.xml").replace(
                "<Send GroupAddressRefId=\"P-0A1B-0_GA-1\" />", "<Receive GroupAddressRefId=\"P-0A1B-0_GA-3\" />"));

        KnxObjectInstance object = device(read(container), "1.1.5").objects().get(0);

        assertEquals(List.of("GA-3", "GA-2"), object.groupAddressIds());
        assertNull(object.sendingGroupAddressId());
    }

    @Test
    void ets5LinksKeepTheirOrderAndShortIds() throws Exception {
        KnxProject project = read(TestContainer.of("ets5", "P-1C2D"));
        KnxDevice actuator = device(project, "2.3.10");

        assertEquals(4, actuator.objects().size());
        KnxObjectInstance first = actuator.objects().get(0);
        assertEquals("O-0_R-1", first.refId());
        assertEquals(List.of("GA-1", "GA-2"), first.groupAddressIds());
        assertEquals("GA-1", first.sendingGroupAddressId());
        assertEquals(List.of(new KnxDatapointType(1, 1)), first.datapointTypes());
        assertNull(first.text());
        assertNull(first.functionText());
        assertNull(first.description());
        assertNull(first.channelId());
        assertEquals(KnxObjectFlags.NONE, first.flags());

        KnxObjectInstance second = actuator.objects().get(1);
        assertEquals(List.of("GA-4", "GA-1"), second.groupAddressIds());
        assertEquals("GA-4", second.sendingGroupAddressId());
        assertEquals(List.of(new KnxDatapointType(5, 1), new KnxDatapointType(5, 4)), second.datapointTypes());

        KnxObjectInstance unlinked = actuator.objects().get(2);
        assertEquals("O-3_R-4", unlinked.refId());
        assertEquals(List.of(), unlinked.groupAddressIds());
        assertEquals(List.of(), unlinked.datapointTypes());
        assertNull(unlinked.sendingGroupAddressId());
    }

    @Test
    void ets5InstanceOverridesAreKeptAsWritten() throws Exception {
        KnxObjectInstance object = device(read(TestContainer.of("ets5", "P-1C2D")), "2.3.10").objects().get(1);

        assertEquals("Uitgang A", object.text());
        assertEquals("Schakelen", object.functionText());
        assertEquals("eerste kanaal", object.description());
        assertEquals("CH-1", object.channelId());
        assertEquals(new KnxObjectFlags("Disabled", "Disabled", "Enabled", "Disabled", "Enabled", "Enabled"),
                object.flags());
    }

    @Test
    void ets5ObjectInAModuleKeepsItsLongReferenceAndChannel() throws Exception {
        KnxObjectInstance object = device(read(TestContainer.of("ets5", "P-1C2D")), "2.3.10").objects().get(3);

        assertEquals("MD-1_M-1_MI-1_O-1-1_R-1", object.refId());
        assertEquals(List.of("GA-3"), object.groupAddressIds());
        assertEquals("MD-1_M-1_MI-1_CH-1", object.channelId());
    }

    @Test
    void ets5ParameterValuesDistinguishMissingFromEmpty() throws Exception {
        List<KnxParameterValue> parameters = device(read(TestContainer.of("ets5", "P-1C2D")), "2.3.10").parameters();

        assertEquals(List.of(new KnxParameterValue("M-0083_A-00AA-11-B2C3_P-1_R-1", "7"),
                new KnxParameterValue("M-0083_A-00AA-11-B2C3_P-2_R-2", ""),
                new KnxParameterValue("M-0083_A-00AA-11-B2C3_P-3_R-3", null),
                new KnxParameterValue("M-0083_A-00AA-11-B2C3_MD-1_M-1_MI-1_P-4_R-4", "Hal")), parameters);
        assertEquals(List.of(), device(read(TestContainer.of("ets5", "P-1C2D")), "2.3.11").parameters());
    }

    @Test
    void ets5ModuleInstancesKeepArgumentsRaw() throws Exception {
        List<KnxModuleInstance> modules = device(read(TestContainer.of("ets5", "P-1C2D")), "2.3.10").modules();

        assertEquals(3, modules.size());
        KnxModuleInstance first = modules.get(0);
        assertEquals("MD-1_M-1_MI-1", first.id());
        assertEquals("MD-1_M-1", first.refId());
        assertEquals("1x1", first.repeatIndex());
        assertEquals(List.of(new KnxModuleArgument("MD-1_A-1", "L-1"), new KnxModuleArgument("MD-1_A-2", "Hal")),
                first.arguments());
        KnxModuleInstance second = modules.get(1);
        assertEquals("2x1 3x4", second.repeatIndex());
        assertEquals(List.of(new KnxModuleArgument("MD-1_A-1", "L-1"), new KnxModuleArgument("MD-1_M-1_SM-2_A-3", "5")),
                second.arguments());
        assertNull(modules.get(2).repeatIndex());
        assertEquals(List.of(), modules.get(2).arguments());
    }

    @Test
    void ets6ObjectsFlagsAndModulesAreReadAndOtherChildrenAreNot() throws Exception {
        KnxProject project = read(TestContainer.of("ets6", "P-3E4F"));
        KnxDevice device = device(project, "1.1.1");

        // the bus interface connector, the group object tree and the channel instances are no objects
        assertEquals(2, device.objects().size());
        KnxObjectInstance plain = device.objects().get(0);
        assertEquals("Enabled", plain.flags().read());
        assertNull(plain.flags().write());
        assertEquals("CH-1", plain.channelId());
        assertEquals(List.of("GA-1", "GA-2"), plain.groupAddressIds());
        KnxObjectInstance inModule = device.objects().get(1);
        assertEquals("MD-1_M-1_MI-2_O-3-1_R-4", inModule.refId());
        assertEquals(List.of("GA-3"), inModule.groupAddressIds());
        assertEquals("MD-1_M-1_MI-2_CH-1", inModule.channelId());

        assertEquals(
                List.of(new KnxParameterValue("M-00C8_A-0101-20-0F0F_P-1_R-1", "Hall"),
                        new KnxParameterValue("M-00C8_A-0101-20-0F0F_MD-1_M-1_MI-2_P-3_R-3", "12")),
                device.parameters());
        assertEquals(List.of(new KnxModuleInstance("MD-1_M-1_MI-2", "MD-1_M-1", "1x2",
                List.of(new KnxModuleArgument("MD-1_A-1", "3")))), device.modules());
        assertEquals(List.of(), device(project, "1.1.20").objects());
    }

    @Test
    void linksAttributeWinsOverConnectors() throws Exception {
        TestContainer container = TestContainer.of("ets4", "P-0A1B");
        container.put("P-0A1B/0.xml",
                container.text("P-0A1B/0.xml").replace("RefId=\"M-00FA_A-0022-33-4455_O-0_R-1\" IsActive=\"true\"",
                        "RefId=\"M-00FA_A-0022-33-4455_O-0_R-1\" Links=\"GA-3\""));

        KnxObjectInstance object = device(read(container), "1.1.5").objects().get(0);

        assertEquals(List.of("GA-3"), object.groupAddressIds());
        assertEquals("GA-3", object.sendingGroupAddressId());
    }

    @Test
    void sameAddressInSendAndReceiveConnectorsListsTheObjectOnceInLookups() throws Exception {
        TestContainer container = TestContainer.of("ets4", "P-0A1B");
        container.put("P-0A1B/0.xml", container.text("P-0A1B/0.xml").replace(
                "<Receive GroupAddressRefId=\"P-0A1B-0_GA-2\" />", "<Receive GroupAddressRefId=\"P-0A1B-0_GA-1\" />"));
        KnxProject project = read(container);

        assertEquals(List.of("GA-1", "GA-1"), device(project, "1.1.5").objects().get(0).groupAddressIds());
        KnxGroupAddress address = address(project, 2049);
        assertEquals(1, project.objectsLinkedTo(address).size());
    }

    @Test
    void groupAddressIsFoundByRawNumberAndByEitherIdForm() throws Exception {
        KnxProject project = read(TestContainer.of("ets6", "P-3E4F"));

        KnxGroupAddress byNumber = project.groupAddress(65535);
        assertNotNull(byNumber);
        assertEquals("Laatste", byNumber.name());
        assertSame(project.groupAddresses().get(0), project.groupAddress(1));
        assertSame(project.groupAddresses().get(0), project.groupAddress("GA-1"));
        assertSame(project.groupAddresses().get(0), project.groupAddress("P-3E4F-0_GA-1"));
        assertNull(project.groupAddress(77));
        assertNull(project.groupAddress("GA-77"));
        assertNull(project.groupAddress(""));
    }

    @Test
    void deviceIsFoundByIndividualAddressAndById() throws Exception {
        KnxProject project = read(TestContainer.of("ets6", "P-3E4F"));

        KnxDevice router = project.device(new KnxIndividualAddress(1, 0, 0));
        assertNotNull(router);
        assertEquals("IP-router", router.name());
        assertSame(router, project.deviceById("P-3E4F-0_DI-3"));
        assertNull(project.device(new KnxIndividualAddress(1, 1, 99)));
        assertNull(project.deviceById("P-3E4F-0_DI-99"));
    }

    @Test
    void objectsLinkedToAGroupAddressComeInDeviceAndFileOrder() throws Exception {
        KnxProject project = read(TestContainer.of("ets4", "P-0A1B"));
        KnxGroupAddress address = address(project, 2050);

        List<KnxLinkedObject> linked = project.objectsLinkedTo(address);

        assertEquals(2, linked.size());
        assertEquals("M-00FA_A-0022-33-4455_O-0_R-1", linked.get(0).object().refId());
        assertEquals("M-00FA_A-0022-33-4455_O-1_R-2", linked.get(1).object().refId());
        assertSame(device(project, "1.1.5"), linked.get(0).device());
        KnxGroupAddress unused = new KnxGroupAddress("P-0A1B-0_GA-99", 5, "0/0/5", "x", null, List.of(), false,
                List.of());
        assertEquals(List.of(), project.objectsLinkedTo(unused));
    }

    @Test
    void linksOfAnObjectResolveToGroupAddressesInEitherForm() throws Exception {
        KnxProject ets5 = read(TestContainer.of("ets5", "P-1C2D"));
        KnxProject ets4 = read(TestContainer.of("ets4", "P-0A1B"));

        KnxObjectInstance short5 = device(ets5, "2.3.10").objects().get(0);
        assertEquals("Keuken", address(ets5, short5.groupAddressIds().get(0)).name());
        KnxObjectInstance full4 = device(ets4, "1.1.5").objects().get(0);
        assertEquals("Gang aan/uit", address(ets4, full4.groupAddressIds().get(0)).name());
        assertEquals(1, ets5.objectsLinkedTo(address(ets5, 4101)).size());
        assertEquals(2, ets5.objectsLinkedTo(address(ets5, 1)).size());
    }

    @Test
    void unknownChildrenAndAttributesOnAnObjectAreIgnored() throws Exception {
        KnxProject project = read(TestContainer.of("ets6", "P-3E4F"));

        // the fixture holds FutureChild and FutureAttribute on the first object
        assertEquals(List.of("GA-1", "GA-2"), device(project, "1.1.1").objects().get(0).groupAddressIds());
    }
}
