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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;

/**
 * Tests for {@link KnxProjectReader} on made-up projects of each ETS generation.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxProjectReaderTest {

    private final KnxProjectReader reader = new KnxProjectReader();

    private KnxProject read(TestContainer container) throws IOException, KnxProjectException {
        return reader.read(container.write(), null);
    }

    private KnxProjectException fails(TestContainer container) {
        return fails(container.write());
    }

    private KnxProjectException fails(Path file) {
        return assertThrows(KnxProjectException.class, () -> reader.read(file, null));
    }

    private static KnxGroupAddress address(KnxProject project, int raw) {
        return project.groupAddresses().stream().filter(ga -> ga.rawAddress() == raw).findFirst().orElseThrow();
    }

    @Test
    void readsEts4Project() throws Exception {
        KnxProject project = read(TestContainer.of("ets4", "P-0A1B"));

        KnxProjectInfo info = project.info();
        assertEquals("P-0A1B", info.id());
        assertEquals("Testhuis Vier", info.name());
        assertEquals(KnxGroupAddressStyle.THREE_LEVEL, info.groupAddressStyle());
        assertEquals("ETS4", info.createdBy());
        assertEquals("4.2.1001.7", info.toolVersion());
        assertEquals(11, info.schemaVersion());
        assertEquals("2014-03-02T10:11:12.000Z", info.lastModified());
        assertEquals("", project.installationName());
        assertEquals(0, project.ignoredInstallations());

        assertEquals(3, project.groupAddresses().size());
        KnxGroupAddress first = project.groupAddresses().get(0);
        assertEquals("P-0A1B-0_GA-1", first.id());
        assertEquals(2049, first.rawAddress());
        assertEquals("1/0/1", first.address());
        assertEquals("Gang aan/uit", first.name());
        assertNull(first.description());
        assertEquals(List.of(), first.datapointTypes());
        assertNull(first.datapointType());
        assertFalse(first.secured());
        assertEquals(List.of("Verlichting", "Gelijkvloers"), first.rangePath());
        assertEquals(List.of("Verlichting", "Kelder"), address(project, 2304).rangePath());

        assertEquals(1, project.groupRanges().size());
        KnxGroupRange top = project.groupRanges().get(0);
        assertEquals("Verlichting", top.name());
        assertEquals(2048, top.rangeStart());
        assertEquals(4095, top.rangeEnd());
        assertEquals(List.of(), top.groupAddresses());
        assertEquals(2, top.ranges().size());
        assertEquals(2, top.ranges().get(0).groupAddresses().size());
        assertSame(first, top.ranges().get(0).groupAddresses().get(0));

        KnxTopology topology = project.topology();
        assertEquals(1, topology.areas().size());
        KnxArea area = topology.areas().get(0);
        assertEquals(1, area.address());
        assertEquals("Hoofdlijn", area.name());
        assertEquals(1, area.lines().size());
        KnxLine line = area.lines().get(0);
        assertEquals("P-0A1B-0_L-2", line.id());
        assertEquals(1, line.address());
        assertEquals("MT-0", line.mediumTypeRefId());
        assertEquals(List.of(), line.segments());
        assertEquals(2, line.devices().size());
        assertEquals(List.of(), topology.unplacedDevices());
        assertEquals(2, topology.devices().size());

        KnxDevice device = line.devices().get(0);
        assertEquals("P-0A1B-0_DI-3", device.id());
        assertEquals(new KnxIndividualAddress(1, 1, 5), device.individualAddress());
        assertEquals("1.1.5", String.valueOf(device.individualAddress()));
        assertEquals("Schakelaar gang", device.name());
        assertNull(device.description());
        assertEquals("M-00FA_H-0011-1_P-1234.5", device.productRefId());
        assertEquals("M-00FA_H-0011-1_HP-0022-33-4455", device.hardware2ProgramRefId());
        assertEquals("M-00FA", device.manufacturerRefId());
        assertEquals("MT-0", device.mediumTypeRefId());
        assertEquals("", line.devices().get(1).name());
        assertEquals(new KnxIndividualAddress(1, 1, 12), line.devices().get(1).individualAddress());
    }

    @Test
    void readsEts5Project() throws Exception {
        KnxProject project = read(TestContainer.of("ets5", "P-1C2D"));

        KnxProjectInfo info = project.info();
        assertEquals("P-1C2D", info.id());
        assertEquals("Appartement Vijf", info.name());
        assertEquals(KnxGroupAddressStyle.TWO_LEVEL, info.groupAddressStyle());
        assertEquals("5.7.2002.3", info.toolVersion());
        assertEquals(20, info.schemaVersion());
        assertEquals("Hoofdinstallatie", project.installationName());

        assertEquals(4, project.groupAddresses().size());
        assertEquals("0/1", address(project, 1).address());
        assertEquals("2/5", address(project, 4101).address());
        assertEquals("2/2047", address(project, 6143).address());

        assertEquals(List.of(new KnxDatapointType(1, 1)), address(project, 1).datapointTypes());
        assertEquals(List.of(new KnxDatapointType(5, 1), new KnxDatapointType(5, 4)),
                address(project, 2).datapointTypes());
        assertEquals(new KnxDatapointType(5, 1), address(project, 2).datapointType());
        KnxGroupAddress temperature = address(project, 4101);
        assertEquals(List.of(new KnxDatapointType(9, null)), temperature.datapointTypes());
        assertNull(temperature.datapointTypes().get(0).sub());
        assertEquals("gemeten aan de muur", temperature.description());
        assertEquals(List.of(), address(project, 6143).datapointTypes());
        assertEquals(List.of("Klimaat"), temperature.rangePath());

        assertEquals(2, project.groupRanges().size());
        assertEquals("Alle lampen", project.groupRanges().get(0).description());
        assertNull(project.groupRanges().get(1).description());

        KnxArea area = project.topology().areas().get(0);
        assertEquals(2, area.lines().size());
        KnxLine line = area.lines().get(0);
        assertEquals("MT-0", line.mediumTypeRefId());
        assertEquals(List.of(), line.segments());
        assertEquals(2, line.devices().size());
        KnxDevice actuator = line.devices().get(0);
        assertEquals("2.3.10", String.valueOf(actuator.individualAddress()));
        assertEquals("", actuator.name());
        assertEquals("Actor in de kast", actuator.description());
        assertEquals("M-0083_H-12AB-1_HP-00AA-11-B2C3", actuator.hardware2ProgramRefId());
        assertEquals("M-0083", actuator.manufacturerRefId());
        assertEquals("Thermostaat", line.devices().get(1).name());
        assertEquals("MT-2", area.lines().get(1).mediumTypeRefId());
        assertEquals(List.of(), area.lines().get(1).allDevices());
    }

    @Test
    void readsEts6Project() throws Exception {
        KnxProject project = read(TestContainer.of("ets6", "P-3E4F"));

        KnxProjectInfo info = project.info();
        assertEquals("Werkplaats Zes", info.name());
        assertEquals(KnxGroupAddressStyle.THREE_LEVEL, info.groupAddressStyle());
        assertEquals("ETS6", info.createdBy());
        assertEquals("6.4.7890.0", info.toolVersion());
        assertEquals(23, info.schemaVersion());

        assertEquals(3, project.groupAddresses().size());
        assertEquals("0/0/1", address(project, 1).address());
        assertEquals("31/7/255", address(project, 65535).address());
        assertEquals(new KnxDatapointType(232, 600), address(project, 65535).datapointType());
        assertEquals("232.600", String.valueOf(address(project, 65535).datapointType()));
        assertEquals(List.of("Top", "Einde"), address(project, 65535).rangePath());
        assertEquals(List.of("Hoofd", "Midden"), address(project, 2).rangePath());

        KnxTopology topology = project.topology();
        assertEquals(2, topology.areas().size());
        assertEquals(List.of(), topology.areas().get(1).lines());
        KnxArea area = topology.areas().get(0);
        assertEquals(2, area.lines().size());

        KnxLine first = area.lines().get(0);
        assertNull(first.mediumTypeRefId());
        assertEquals(List.of(), first.devices());
        assertEquals(2, first.segments().size());
        KnxSegment segment = first.segments().get(1);
        assertEquals("P-3E4F-0_S-2", segment.id());
        assertEquals(1, segment.number());
        assertEquals("MT-0", segment.mediumTypeRefId());
        assertEquals(2, first.allDevices().size());

        KnxDevice device = first.segments().get(0).devices().get(0);
        assertEquals("1.1.1", String.valueOf(device.individualAddress()));
        assertEquals("", device.name());
        assertEquals("MT-0", device.mediumTypeRefId());
        assertEquals("M-00C8_H-0001-2_P-ABC.12", device.productRefId());
        assertEquals("Drukknop deur", segment.devices().get(0).name());
        assertEquals("1.1.20", String.valueOf(segment.devices().get(0).individualAddress()));

        KnxDevice router = area.lines().get(1).segments().get(0).devices().get(0);
        assertEquals("1.0.0", String.valueOf(router.individualAddress()));
        assertEquals("MT-5", router.mediumTypeRefId());
        assertEquals("router in de technische ruimte", router.description());
        assertEquals(3, topology.devices().size());
    }

    @Test
    void readsFreeStyleProjectWithAddressesAtSeveralDepths() throws Exception {
        KnxProject project = read(TestContainer.of("ets6-free", "P-5A6B"));

        assertEquals(KnxGroupAddressStyle.FREE, project.info().groupAddressStyle());
        assertEquals(22, project.info().schemaVersion());
        assertEquals(2, project.groupAddresses().size());
        assertEquals("17", address(project, 17).address());
        assertEquals(List.of("Eerste"), address(project, 17).rangePath());
        KnxGroupAddress deep = address(project, 40000);
        assertEquals("40000", deep.address());
        assertEquals(List.of("Tweede", "Derde", "Vierde"), deep.rangePath());
        assertEquals(new KnxDatapointType(14, null), deep.datapointType());
        assertSame(deep, project.groupRanges().get(1).ranges().get(0).ranges().get(0).groupAddresses().get(0));

        KnxSegment segment = project.topology().areas().get(0).lines().get(0).segments().get(0);
        assertEquals("MT-5", segment.mediumTypeRefId());
        assertEquals(0, project.topology().devices().size());
    }

    @Test
    void securedFlagComesFromTheKeyAttributeAlone() throws Exception {
        KnxProject project = read(TestContainer.of("ets6", "P-3E4F"));

        assertTrue(address(project, 1).secured());
        assertFalse(address(project, 2).secured());
        assertFalse(address(project, 65535).secured());
    }

    @Test
    void noSecretOrPersonalValueReachesTheModel() throws Exception {
        TestContainer container = TestContainer.of("ets6", "P-3E4F");
        List<String> madeUp = new ArrayList<>();
        Matcher matcher = Pattern.compile("=\"(MADEUP-[^\"]*)\"")
                .matcher(container.text("P-3E4F/0.xml") + container.text("P-3E4F/project.xml"));
        while (matcher.find()) {
            madeUp.add(matcher.group(1));
        }
        assertTrue(madeUp.size() >= 14, "the fixture holds the secret attributes of the format note");

        String model = read(container).toString();

        assertTrue(model.contains("Hal schakelen"));
        for (String value : madeUp) {
            assertFalse(model.contains(value), "found a value that must not be kept");
        }
    }

    @Test
    void errorTextDoesNotCarryAttributeValues() {
        TestContainer container = TestContainer.of("ets6", "P-3E4F");
        container.put("P-3E4F/0.xml", container.text("P-3E4F/0.xml").replace("Address=\"1\" Name=\"Hal schakelen\"",
                "Address=\"MADEUP-NOT-A-NUMBER\" Name=\"Hal schakelen\""));

        KnxProjectException e = fails(container);

        assertEquals(Reason.MALFORMED, e.getReason());
        String message = String.valueOf(e.getMessage());
        assertFalse(message.contains("MADEUP"));
        assertFalse(message.contains("Hal schakelen"));
    }

    @Test
    void notAZipIsNotAProject() {
        Path file = TestContainer.write("this is just text, not an archive".getBytes(StandardCharsets.UTF_8));
        assertEquals(Reason.NOT_A_PROJECT, fails(file).getReason());
    }

    @Test
    void emptyFileIsNotAProject() {
        assertEquals(Reason.NOT_A_PROJECT, fails(TestContainer.write(new byte[0])).getReason());
    }

    @Test
    void zipWithoutProjectPartIsNotAProject() {
        assertEquals(Reason.NOT_A_PROJECT, fails(TestContainer.withMaster()).getReason());
        assertEquals(Reason.NOT_A_PROJECT,
                fails(new TestContainer().put("readme.txt", "nothing to see").put("P-0001/0.xml", "<KNX />"))
                        .getReason());
    }

    @Test
    void damagedProtectedProjectPartIsMalformed() {
        TestContainer container = TestContainer.withMaster().put("P-0A1B.zip", new byte[] { 0x50, 0x4b, 5, 6 });

        assertEquals(Reason.MALFORMED, fails(container).getReason());
        assertEquals(Reason.MALFORMED,
                assertThrows(KnxProjectException.class, () -> reader.read(container.write(), "made-up".toCharArray()))
                        .getReason());
    }

    @Test
    void moreThanOneProjectPartIsRefused() {
        TestContainer container = TestContainer.of("ets6", "P-3E4F").put("P-0A1B.zip", new byte[] { 1 });
        assertEquals(Reason.MALFORMED, fails(container).getReason());
    }

    @Test
    void projectPartWithoutInstallationFileIsMalformed() {
        TestContainer container = TestContainer.of("ets6", "P-3E4F").remove("P-3E4F/0.xml");
        assertEquals(Reason.MALFORMED, fails(container).getReason());
    }

    @Test
    void brokenInstallationXmlIsMalformed() {
        TestContainer container = TestContainer.of("ets6", "P-3E4F");
        String text = container.text("P-3E4F/0.xml");
        container.put("P-3E4F/0.xml", text.substring(0, text.indexOf("<Locations>")));

        assertEquals(Reason.MALFORMED, fails(container).getReason());
    }

    @Test
    void brokenProjectXmlIsMalformed() {
        TestContainer container = TestContainer.of("ets5", "P-1C2D");
        container.put("P-1C2D/project.xml", container.text("P-1C2D/project.xml").replace("</Project>", "</Projekt>"));

        assertEquals(Reason.MALFORMED, fails(container).getReason());
    }

    @Test
    void projectXmlWithForeignRootIsMalformed() {
        TestContainer container = TestContainer.of("ets5", "P-1C2D");
        container.put("P-1C2D/project.xml", "<?xml version=\"1.0\"?><html><body /></html>");

        assertEquals(Reason.MALFORMED, fails(container).getReason());
    }

    @Test
    void groupAddressOutOfRangeIsMalformed() {
        TestContainer container = TestContainer.of("ets5", "P-1C2D");
        container.put("P-1C2D/0.xml", container.text("P-1C2D/0.xml").replace("Address=\"6143\"", "Address=\"65536\""));

        assertEquals(Reason.MALFORMED, fails(container).getReason());
    }

    @Test
    void documentTypeDeclarationIsRefused() {
        TestContainer container = TestContainer.of("ets5", "P-1C2D");
        container.put("P-1C2D/project.xml", container.text("P-1C2D/project.xml").replace("<KNX ",
                "<!DOCTYPE KNX [<!ENTITY made SYSTEM \"file:///made/up/path\">]>\n<KNX Extra=\"&made;\" "));

        assertEquals(Reason.MALFORMED, fails(container).getReason());
    }

    @Test
    void bareDocumentTypeDeclarationIsRefusedInEveryProjectFile() {
        for (String file : new String[] { "P-1C2D/project.xml", "P-1C2D/0.xml" }) {
            TestContainer container = TestContainer.of("ets5", "P-1C2D");
            container.put(file, container.text(file).replace("<KNX ", "<!DOCTYPE KNX>\n<KNX "));

            assertEquals(Reason.MALFORMED, fails(container).getReason(), file);
        }
    }

    @Test
    void anyNamespaceNumberIsAccepted() throws Exception {
        TestContainer container = TestContainer.of("ets6", "P-3E4F");
        container.put("P-3E4F/project.xml",
                container.text("P-3E4F/project.xml").replace("xml/project/23", "xml/project/99"));
        container.put("P-3E4F/0.xml", container.text("P-3E4F/0.xml").replace("xml/project/23", "xml/project/99"));

        KnxProject project = read(container);

        assertEquals(99, project.info().schemaVersion());
        assertEquals(3, project.groupAddresses().size());
        assertEquals(3, project.topology().devices().size());
    }

    @Test
    void unknownNamespaceLeavesSchemaVersionOpen() throws Exception {
        TestContainer container = TestContainer.of("ets5", "P-1C2D");
        container.put("P-1C2D/project.xml",
                container.text("P-1C2D/project.xml").replace("http://knx.org/xml/project/20", "urn:made-up"));

        assertNull(read(container).info().schemaVersion());
    }

    @Test
    void missingGroupAddressStyleFallsBackToThreeLevel() throws Exception {
        TestContainer container = TestContainer.of("ets5", "P-1C2D");
        container.put("P-1C2D/project.xml",
                container.text("P-1C2D/project.xml").replace("GroupAddressStyle=\"TwoLevel\"", ""));

        KnxProject project = read(container);

        assertEquals(KnxGroupAddressStyle.THREE_LEVEL, project.info().groupAddressStyle());
        assertEquals("2/0/5", address(project, 4101).address());
    }

    @Test
    void onlyTheFirstInstallationIsReadAndTheRestIsCounted() throws Exception {
        TestContainer container = TestContainer.of("ets5", "P-1C2D");
        String second = "<Installation Name=\"Tweede\"><GroupAddresses><GroupRanges><GroupRange Id=\"x\" Name=\"x\">"
                + "<GroupAddress Id=\"P-1C2D-1_GA-1\" Address=\"7\" Name=\"elders\" /></GroupRange></GroupRanges>"
                + "</GroupAddresses></Installation>";
        container.put("P-1C2D/0.xml",
                container.text("P-1C2D/0.xml").replace("</Installations>", second + "</Installations>"));
        container.put("P-1C2D/1.xml", "<KNX />");
        container.put("P-1C2D/10.xml", "<KNX />");

        KnxProject project = read(container);

        assertEquals("Hoofdinstallatie", project.installationName());
        assertEquals(4, project.groupAddresses().size());
        assertEquals(3, project.ignoredInstallations());
    }

    @Test
    void lowestNumberedInstallationFileIsRead() throws Exception {
        TestContainer container = TestContainer.of("ets5", "P-1C2D");
        String installation = container.text("P-1C2D/0.xml");
        container.remove("P-1C2D/0.xml");
        container.put("P-1C2D/10.xml", "not xml at all");
        container.put("P-1C2D/2.xml", installation);

        KnxProject project = read(container);

        assertEquals(4, project.groupAddresses().size());
        assertEquals(1, project.ignoredInstallations());
    }

    @Test
    void deviceWithoutAddressOrOutsideALineIsKept() throws Exception {
        TestContainer container = TestContainer.of("ets5", "P-1C2D");
        container.put("P-1C2D/0.xml",
                container.text("P-1C2D/0.xml").replace("Name=\"Thermostaat\" Address=\"11\"", "Name=\"Thermostaat\"")
                        .replace("</Topology>", "<UnassignedDevices><DeviceInstance Id=\"P-1C2D-0_DI-9\" Name=\"Los\" "
                                + "Address=\"9\" ProductRefId=\"M-0083_H-1_P-9\" /></UnassignedDevices></Topology>"));

        KnxProject project = read(container);

        KnxLine line = project.topology().areas().get(0).lines().get(0);
        assertNull(line.devices().get(1).individualAddress());
        assertEquals("Thermostaat", line.devices().get(1).name());
        assertEquals(1, project.topology().unplacedDevices().size());
        KnxDevice loose = project.topology().unplacedDevices().get(0);
        assertEquals("Los", loose.name());
        assertNull(loose.individualAddress());
        assertNull(loose.mediumTypeRefId());
        assertEquals(3, project.topology().devices().size());
    }

    @Test
    void protectedPartNameIsMatchedWithoutRegardToCase() throws Exception {
        byte[] inner = ProtectedPartWriter.ets6("ets5", "made-up").build();
        Path file = TestContainer.ofProtected("p-0a1b.ZIP", inner).write();

        assertEquals(Reason.PASSWORD_REQUIRED, fails(file).getReason());
        KnxProject project = reader.read(file, "made-up".toCharArray());
        assertEquals("p-0a1b", project.info().id());
        assertEquals(4, project.groupAddresses().size());
    }

    @Test
    void readerCanBeUsedFromSeveralThreads() throws Exception {
        Path ets5 = TestContainer.of("ets5", "P-1C2D").write();
        Path ets6 = TestContainer.of("ets6", "P-3E4F").write();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Future<Integer>> results = new ArrayList<>();
            for (int i = 0; i < 80; i++) {
                Path file = i % 2 == 0 ? ets5 : ets6;
                results.add(executor.submit(() -> reader.read(file, null).groupAddresses().size()));
            }
            for (int i = 0; i < results.size(); i++) {
                assertEquals(i % 2 == 0 ? 4 : 3, results.get(i).get(60, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
