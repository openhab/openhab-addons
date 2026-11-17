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
import static org.openhab.binding.knx.internal.project.ProgramFixture.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for the group objects that the manufacturer files define: how the files are found, the join rules,
 * products and device names, and the definition states.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxProjectGroupObjectsTest {

    private final List<String> opened = Collections.synchronizedList(new ArrayList<>());
    private final KnxProjectReader reader = new KnxProjectReader(ReadLimits.DEFAULT, opened::add);

    private KnxProject read(TestContainer container) throws Exception {
        return reader.read(container.write(), null);
    }

    private static TestContainer generation(String fixture, String part, String folder, String program) {
        return TestContainer.of(fixture, part)
                .put(folder + "/Hardware.xml", TestContainer.resource("manufacturer/" + fixture + "/Hardware.xml"))
                .put(folder + "/" + program + ".xml",
                        TestContainer.resource("manufacturer/" + fixture + "/" + program + ".xml"));
    }

    private static TestContainer modular() {
        return generation("modular", "P-5A6B", "M-00C8", "M-00C8_A-0110-10-AAAA");
    }

    private static KnxDevice device(KnxProject project, String address) {
        return project.topology().devices().stream().filter(d -> address.equals(String.valueOf(d.individualAddress())))
                .findFirst().orElseThrow();
    }

    private static KnxGroupObject object(KnxProject project, String address, String refId) {
        return project.groupObjectsOf(device(project, address)).stream().filter(o -> refId.equals(o.instance().refId()))
                .findFirst().orElseThrow();
    }

    private static List<String> types(KnxGroupObject object) {
        return object.datapointTypes().stream().map(KnxDatapointType::toString).toList();
    }

    private long opens(String entry) {
        return opened.stream().filter(entry::equals).count();
    }

    @Test
    void ets4FullReferenceIdsAreLookedUpAsTheyAre() throws Exception {
        KnxProject project = read(generation("ets4", "P-0A1B", "M-00FA", "M-00FA_A-0022-33-4455"));
        List<KnxGroupObject> objects = project.groupObjectsOf(device(project, "1.1.5"));

        assertEquals(3, objects.size());
        KnxGroupObject first = objects.get(0);
        assertEquals(KnxObjectDefinition.FOUND, first.definition());
        assertEquals("P-0A1B-0_DI-3", first.deviceId());
        assertEquals("1.1.5", String.valueOf(first.deviceAddress()));
        assertEquals(0, first.number());
        assertEquals("Schalten", first.text());
        assertEquals("Schalten", first.name());
        assertEquals("Kanal A", first.functionText());
        assertEquals(List.of("1.001"), types(first));
        assertEquals("1 Bit", first.sizeText());
        assertEquals(1, first.sizeBits());
        assertEquals(new KnxGroupObjectFlags(false, true, true, false, false, false), first.flags());
        assertEquals(List.of("GA-1", "GA-2"), first.groupAddressIds());
        assertEquals("GA-1", first.sendingGroupAddressId());
        assertNull(first.moduleInstanceId());

        KnxGroupObject second = objects.get(1);
        assertEquals(1, second.number());
        assertEquals("Status schalten", second.text());
        assertEquals(List.of("1.001"), types(second));
        assertEquals(new KnxGroupObjectFlags(true, false, true, true, false, false), second.flags());

        KnxGroupObject unlinked = objects.get(2);
        assertEquals(KnxObjectDefinition.FOUND, unlinked.definition());
        assertFalse(unlinked.isLinked());
        assertEquals(List.of("1.003"), types(unlinked));
    }

    @Test
    void ets5ShortReferenceIdsAreLookedUpWithTheProgramId() throws Exception {
        KnxProject project = read(generation("ets5", "P-1C2D", "M-0083", "M-0083_A-00AA-11-B2C3"));
        KnxDevice device = device(project, "2.3.10");

        assertEquals(4, project.groupObjectsOf(device).size());
        KnxGroupObject plain = object(project, "2.3.10", "O-0_R-1");
        assertEquals(KnxObjectDefinition.FOUND, plain.definition());
        assertEquals(0, plain.number());
        assertEquals("Switch", plain.text());
        assertEquals("Output A, switching", plain.functionText());
        assertEquals(List.of("1.001"), types(plain));
        assertTrue(plain.flags().read());

        KnxGroupObject overridden = object(project, "2.3.10", "O-2_R-3");
        assertEquals(2, overridden.number());
        assertEquals("Uitgang A", overridden.text());
        assertEquals("Schakelen", overridden.functionText());
        assertEquals(List.of("5.001", "5.004"), types(overridden));
        assertEquals(8, overridden.sizeBits());
        assertEquals(new KnxGroupObjectFlags(false, false, true, false, true, true), overridden.flags());
        assertEquals("CH-1", overridden.channelId());

        KnxGroupObject module = object(project, "2.3.10", "MD-1_M-1_MI-1_O-1-1_R-1");
        assertEquals(KnxObjectDefinition.FOUND, module.definition());
        assertEquals(1, module.number());
        assertEquals("Temperature {{name}}", module.text());
        assertEquals("Temperature Hal", module.name());
        assertEquals(List.of("9.001"), types(module));
        assertEquals(16, module.sizeBits());
        assertEquals("MD-1_M-1_MI-1", module.moduleInstanceId());
        assertEquals("MD-1_M-1_MI-1_CH-1", module.channelId());

        assertEquals(KnxObjectDefinition.FOUND, object(project, "2.3.10", "O-3_R-4").definition());
    }

    @Test
    void ets6ObjectsAndModuleObjectsAreFound() throws Exception {
        KnxProject project = read(generation("ets6", "P-3E4F", "M-00C8", "M-00C8_A-0101-20-0F0F"));

        KnxGroupObject plain = object(project, "1.1.1", "O-1_R-1");
        assertEquals(KnxObjectDefinition.FOUND, plain.definition());
        assertEquals(1, plain.number());
        assertEquals("Switch {{0:A}}", plain.text());
        assertEquals(new KnxGroupObjectFlags(true, true, true, true, false, false), plain.flags());

        KnxGroupObject module = object(project, "1.1.1", "MD-1_M-1_MI-2_O-3-1_R-4");
        assertEquals(KnxObjectDefinition.FOUND, module.definition());
        assertEquals(3, module.number());
        assertEquals("Colour {{ch}}", module.text());
        assertEquals("3 Bytes", module.sizeText());
        assertEquals(24, module.sizeBits());
        assertEquals(List.of("232.600"), types(module));
        assertEquals("MD-1_M-1_MI-2", module.moduleInstanceId());
        assertEquals(List.of("GA-3"), module.groupAddressIds());
    }

    @Test
    void moduleAndSubmoduleObjectsAreFoundThroughTheModulePattern() throws Exception {
        KnxProject project = read(modular());

        KnxGroupObject central = object(project, "1.1.1", "O-1_R-1");
        assertEquals(KnxObjectDefinition.FOUND, central.definition());
        assertEquals("Central switch", central.text());
        assertNull(central.moduleInstanceId());

        KnxGroupObject module = object(project, "1.1.1", "MD-1_M-1_MI-1_O-2-1_R-1");
        assertEquals(KnxObjectDefinition.FOUND, module.definition());
        assertEquals(2, module.number());
        assertEquals("Output {{name}}", module.text());
        assertEquals("MD-1_M-1_MI-1", module.moduleInstanceId());
        assertEquals("GA-2", module.sendingGroupAddressId());

        KnxGroupObject sub = object(project, "1.1.1", "MD-1_M-1_MI-1_SM-1_M-1_MI-1-1-1_SM-1_O-3-1_R-1");
        assertEquals(KnxObjectDefinition.FOUND, sub.definition());
        assertEquals(3, sub.number());
        assertEquals("Logic input", sub.text());
        assertEquals(List.of("5.010"), types(sub));
        assertEquals(8, sub.sizeBits());
        assertTrue(sub.flags().update());
        assertEquals("MD-1_M-1_MI-1_SM-1_M-1_MI-1-1-1", sub.moduleInstanceId());

        KnxGroupObject second = object(project, "1.1.1", "MD-1_M-1_MI-2_O-2-1_R-1");
        assertEquals(KnxObjectDefinition.FOUND, second.definition());
        assertEquals("MD-1_M-1_MI-2", second.moduleInstanceId());
        assertEquals(2, second.number());
    }

    @Test
    void referenceOrObjectMissingFromTheProgramIsNotInProgram() throws Exception {
        KnxProject project = read(modular());

        KnxGroupObject unknown = object(project, "1.1.1", "O-9_R-9");
        assertEquals(KnxObjectDefinition.NOT_IN_PROGRAM, unknown.definition());
        assertNull(unknown.number());
        assertNull(unknown.text());
        assertEquals("", unknown.name());
        assertNull(unknown.sizeText());

        KnxGroupObject withoutObject = object(project, "1.1.1", "O-5_R-5");
        assertEquals(KnxObjectDefinition.NOT_IN_PROGRAM, withoutObject.definition());
        assertNull(withoutObject.text(), "only a found definition gives program values");
        assertEquals(List.of("GA-5"), withoutObject.groupAddressIds());
    }

    @Test
    void missingProgramFileAndMissingHardwareKeepTheInstanceValues() throws Exception {
        KnxProject project = read(modular());

        KnxGroupObject noFile = object(project, "1.1.3", "O-1_R-1");
        assertEquals(KnxObjectDefinition.NO_PROGRAM_FILE, noFile.definition());
        assertEquals("Eigen tekst", noFile.text());
        assertEquals("Eigen tekst", noFile.name());
        assertNull(noFile.number());
        assertEquals(new KnxGroupObjectFlags(false, false, false, false, true, false), noFile.flags());

        assertEquals(KnxObjectDefinition.NO_HARDWARE, object(project, "1.1.4", "O-1_R-1").definition());
        KnxGroupObject noHardware2Program = object(project, "1.1.5", "O-1_R-1");
        assertEquals(KnxObjectDefinition.NO_HARDWARE, noHardware2Program.definition());
        assertEquals(List.of("1.001"), types(noHardware2Program));
    }

    @Test
    void theInstanceTextWinsOverTheProgramText() throws Exception {
        KnxProject project = read(modular());

        KnxGroupObject object = object(project, "1.1.2", "O-1_R-1");
        assertEquals(KnxObjectDefinition.FOUND, object.definition());
        assertEquals("Centraal tweede", object.text());
        assertEquals("All channels", object.functionText());
    }

    @Test
    void groupObjectsComeInDeviceOrderThenInstanceOrder() throws Exception {
        KnxProject project = read(modular());

        List<String> order = project.groupObjects().stream().map(o -> o.deviceId() + " " + o.instance().refId())
                .toList();
        assertEquals(List.of("P-5A6B-0_DI-1 O-1_R-1", "P-5A6B-0_DI-1 MD-1_M-1_MI-1_O-2-1_R-1",
                "P-5A6B-0_DI-1 MD-1_M-1_MI-1_SM-1_M-1_MI-1-1-1_SM-1_O-3-1_R-1", "P-5A6B-0_DI-1 MD-1_M-1_MI-2_O-2-1_R-1",
                "P-5A6B-0_DI-1 O-9_R-9", "P-5A6B-0_DI-1 O-5_R-5", "P-5A6B-0_DI-2 O-1_R-1", "P-5A6B-0_DI-3 O-1_R-1",
                "P-5A6B-0_DI-4 O-1_R-1", "P-5A6B-0_DI-5 O-1_R-1"), order);
    }

    @Test
    void productsGiveTheNameOfADeviceWithoutOwnName() throws Exception {
        KnxProject project = read(modular());

        KnxProduct product = project.productOf(device(project, "1.1.1"));
        assertEquals(new KnxProduct("M-00C8_H-0010-1_P-MOD.8", "Made-up actuator 8-fold", "MU MOD.8"), product);
        assertEquals("Made-up actuator 8-fold", project.displayNameOf(device(project, "1.1.1")));
        assertEquals("Tweede actor", project.displayNameOf(device(project, "1.1.2")));
        assertEquals("Made-up device without program file", project.displayNameOf(device(project, "1.1.3")));
        assertEquals("MU NOHP.1", project.displayNameOf(device(project, "1.1.4")), "blank text: order number");
        assertNull(project.productOf(device(project, "1.1.5")));
        assertEquals("", project.displayNameOf(device(project, "1.1.5")));
        assertEquals(3, project.products().size(), "two devices share one product");
    }

    @Test
    void ets4AndEts5DeviceNames() throws Exception {
        KnxProject ets4 = read(generation("ets4", "P-0A1B", "M-00FA", "M-00FA_A-0022-33-4455"));
        assertEquals("Schakelaar gang", ets4.displayNameOf(device(ets4, "1.1.5")));
        assertEquals("Made-up push button", ets4.displayNameOf(device(ets4, "1.1.12")));

        KnxProject ets5 = read(generation("ets5", "P-1C2D", "M-0083", "M-0083_A-00AA-11-B2C3"));
        assertEquals("Made-up switch actuator 4-fold", ets5.displayNameOf(device(ets5, "2.3.10")));
        assertEquals("Thermostaat", ets5.displayNameOf(device(ets5, "2.3.11")));
    }

    @Test
    void withoutManufacturerDataTheProjectIsReadAsBefore() throws Exception {
        KnxProject project = read(TestContainer.of("ets5", "P-1C2D"));

        assertEquals(4, project.groupObjects().size());
        for (KnxGroupObject object : project.groupObjects()) {
            assertEquals(KnxObjectDefinition.NO_HARDWARE, object.definition());
            assertNull(object.number());
            assertNull(object.sizeText());
        }
        KnxGroupObject overridden = object(project, "2.3.10", "O-2_R-3");
        assertEquals("Uitgang A", overridden.text());
        assertEquals(List.of("5.001", "5.004"), types(overridden));
        assertEquals(new KnxGroupObjectFlags(false, false, true, false, true, true), overridden.flags());
        assertEquals("", object(project, "2.3.10", "O-3_R-4").name());
        assertEquals("", project.displayNameOf(device(project, "2.3.10")));
        assertTrue(project.products().isEmpty());
        assertTrue(opened.stream().noneMatch(name -> name.startsWith("M-")));

        assertTrue(read(TestContainer.of("ets6-free", "P-5A5A")).groupObjects().isEmpty());
    }

    @Test
    void anEmptyHardwareFileIsANormalCase() throws Exception {
        KnxProject project = read(TestContainer.of("ets4", "P-0A1B"));

        assertEquals(3, project.groupObjects().size());
        assertTrue(project.groupObjects().stream().allMatch(o -> o.definition() == KnxObjectDefinition.NO_HARDWARE));
        assertEquals(1, opens("M-00FA/Hardware.xml"));
    }

    @Test
    void aProgramFileUsedByTwoDevicesIsReadOnce() throws Exception {
        String references = reference(1, "");
        String objects = comObject(1, "Text=\"One\" ObjectSize=\"1 Bit\"");
        TestContainer container = ProgramFixture.container(2, instance(1, "Links=\"GA-1\""), "<Static><ComObjectRefs>"
                + references + "</ComObjectRefs><ComObjectTable>" + objects + "</ComObjectTable></Static>");

        KnxProject project = read(container);

        assertEquals(2, project.groupObjects().size());
        assertTrue(project.groupObjects().stream().allMatch(o -> o.definition() == KnxObjectDefinition.FOUND));
        assertEquals(1, opens(PROGRAM_ENTRY));
        assertEquals(1, opens(HARDWARE_ENTRY));
    }

    @Test
    void theNumberOfPassesDoesNotDependOnTheNumberOfDevices() throws Exception {
        String content = objectsThenReferences(comObject(1, "Text=\"One\""), reference(1, ""));

        read(ProgramFixture.container(1, instance(1, "Links=\"GA-1\""), content));
        long one = opens(PROGRAM_ENTRY);
        opened.clear();
        read(ProgramFixture.container(3, instance(1, "Links=\"GA-1\""), content));

        assertEquals(2, one, "objects before their references take a second pass");
        assertEquals(one, opens(PROGRAM_ENTRY));
    }

    @Test
    void entriesThatAreNeverOpened() throws Exception {
        TestContainer container = ProgramFixture.container(instance(1, "Links=\"GA-1\""),
                objectsThenReferences(comObject(1, "Text=\"One\""), reference(1, "")));
        container.put("M-00C8/Catalog.xml", "not < xml at all").put("M-00C8/Baggages.xml", "<<< broken")
                .put("M-00C8/Baggages/tool.dll", "MZ made-up binary")
                .put("M-00C8/Baggages/" + APP + ".xml", "<<< a program name in the baggage folder")
                .put("M-0002/Hardware.xml", "<<< folder of no device").put("M-00C8.signature", "made-up")
                .put(PART + "/UserFiles/notes.xml", "<<< made-up").put(PART + ".certificate", "made-up");

        KnxProject project = read(container);

        assertEquals(KnxObjectDefinition.FOUND, project.groupObjects().get(0).definition());
        assertEquals(List.of(PART + "/project.xml", PART + "/0.xml", HARDWARE_ENTRY, PROGRAM_ENTRY),
                opened.stream().distinct().toList());
    }

    @Test
    void aProgramFileOnlyInTheBaggageFolderIsNotUsed() throws Exception {
        TestContainer container = ProgramFixture.container(instance(1, "Links=\"GA-1\""), "");
        container.remove(PROGRAM_ENTRY).put("M-00C8/Baggages/" + APP + ".xml",
                program(APP, objectsThenReferences(comObject(1, "Text=\"One\""), reference(1, ""))));

        KnxProject project = read(container);

        assertEquals(KnxObjectDefinition.NO_PROGRAM_FILE, project.groupObjects().get(0).definition());
        assertEquals(0, opens("M-00C8/Baggages/" + APP + ".xml"));
    }

    @Test
    void aProgramIdThatNamesAnotherFileOfTheFolderIsNotOpened() throws Exception {
        for (String id : List.of("Catalog", "Baggages", "Hardware", "knx_master")) {
            TestContainer container = ProgramFixture.container(instance(1, "Links=\"GA-1\""), "");
            container.put(HARDWARE_ENTRY, hardware(id)).put("M-00C8/Catalog.xml", "<<< broken")
                    .put("M-00C8/Baggages.xml", "<<< broken").put("M-00C8/knx_master.xml", "<<< broken");
            opened.clear();

            KnxProject project = read(container);

            assertEquals(KnxObjectDefinition.NO_PROGRAM_FILE, project.groupObjects().get(0).definition(), id);
            assertEquals(1, opens(HARDWARE_ENTRY), id);
            assertEquals(3, opened.size(), id);
        }
    }

    @Test
    void aProgramFileIsFoundByItsFileNameInAnotherFolder() throws Exception {
        TestContainer container = ProgramFixture.container(instance(1, "Links=\"GA-1\""), "");
        container.remove(PROGRAM_ENTRY).put("M-0001/" + APP + ".xml",
                program(APP, objectsThenReferences(comObject(1, "Text=\"Elsewhere\""), reference(1, ""))));

        KnxGroupObject object = read(container).groupObjects().get(0);

        assertEquals(KnxObjectDefinition.FOUND, object.definition());
        assertEquals("Elsewhere", object.text());
    }

    @Test
    void ofTwoFilesWithTheWantedNameTheOneInTheProgramsOwnFolderIsTaken() throws Exception {
        TestContainer container = ProgramFixture.container(instance(1, "Links=\"GA-1\""), "");
        container.remove(PROGRAM_ENTRY)
                .put("M-0001/" + APP + ".xml",
                        program(APP, objectsThenReferences(comObject(1, "Text=\"Other\""), reference(1, ""))))
                .put(PROGRAM_ENTRY,
                        program(APP, objectsThenReferences(comObject(1, "Text=\"Own\""), reference(1, ""))));

        assertEquals("Own", read(container).groupObjects().get(0).text());
        assertEquals(0, opens("M-0001/" + APP + ".xml"));
    }

    @Test
    void ofTwoFilesInOtherFoldersTheFirstInTheContainerIsTaken() throws Exception {
        TestContainer container = ProgramFixture.container(instance(1, "Links=\"GA-1\""), "");
        container.remove(PROGRAM_ENTRY)
                .put("M-0002/" + APP + ".xml",
                        program(APP, objectsThenReferences(comObject(1, "Text=\"First\""), reference(1, ""))))
                .put("M-0001/" + APP + ".xml",
                        program(APP, objectsThenReferences(comObject(1, "Text=\"Second\""), reference(1, ""))));

        assertEquals("First", read(container).groupObjects().get(0).text());
    }

    @Test
    void fileNamesAreMatchedWithoutRegardToCase() throws Exception {
        TestContainer container = ProgramFixture.container(instance(1, "Links=\"GA-1\""), "");
        String hardware = container.text(HARDWARE_ENTRY);
        container.remove(PROGRAM_ENTRY).remove(HARDWARE_ENTRY).put("M-00C8/HARDWARE.XML", hardware).put(
                "M-00C8/m-00c8_a-0200-10-abcd.XML",
                program(APP, objectsThenReferences(comObject(1, "Text=\"One\""), reference(1, ""))));

        assertEquals(KnxObjectDefinition.FOUND, read(container).groupObjects().get(0).definition());
    }

    @Test
    void aFileHoldingAnotherProgramGivesNoDefinition() throws Exception {
        TestContainer container = ProgramFixture.container(instance(1, "Links=\"GA-1\""), "");
        container.put(PROGRAM_ENTRY, program("M-00C8_A-0200-10-FFFF",
                objectsThenReferences(comObject(1, "Text=\"One\""), reference(1, ""))));

        KnxGroupObject object = read(container).groupObjects().get(0);

        assertEquals(KnxObjectDefinition.NO_PROGRAM_FILE, object.definition());
        assertNull(object.text());
    }

    @Test
    void aHardware2ProgramWithoutProgramReferenceGivesNoProgramFile() throws Exception {
        TestContainer container = ProgramFixture.container(instance(1, "Links=\"GA-1\""), "");
        container.put(HARDWARE_ENTRY,
                container.text(HARDWARE_ENTRY).replaceAll("<ApplicationProgramRef [^>]*/>", "<Unknown />"));

        assertEquals(KnxObjectDefinition.NO_PROGRAM_FILE, read(container).groupObjects().get(0).definition());
    }

    @Test
    void noSecretOrPersonalValueOfTheManufacturerFilesReachesTheModel() throws Exception {
        TestContainer container = generation("ets6", "P-3E4F", "M-00C8", "M-00C8_A-0101-20-0F0F");
        List<String> madeUp = new ArrayList<>();
        Matcher matcher = Pattern.compile("=\"(MADEUP-[^\"]*)\"")
                .matcher(container.text("P-3E4F/0.xml") + container.text("P-3E4F/project.xml")
                        + container.text("M-00C8/Hardware.xml") + container.text("M-00C8/M-00C8_A-0101-20-0F0F.xml"));
        while (matcher.find()) {
            madeUp.add(matcher.group(1));
        }
        assertTrue(madeUp.containsAll(List.of("MADEUP-HW-SERIAL-LIGHT", "MADEUP-BI-ACCESS", "MADEUP-BI-TEXT",
                "MADEUP-BI-PROGRAM-PASSWORD", "MADEUP-TOOLKEY-1", "MADEUP-GA-KEY-ONE", "MADEUP-FDSK-AAAA")));

        KnxProject project = read(container);
        String model = project.toString();

        assertTrue(model.contains("Made-up light actuator"));
        assertTrue(model.contains("Switch {{0:A}}"));
        for (String value : madeUp) {
            assertFalse(model.contains(value), "found a value that must not be kept");
        }
    }

    @Test
    void theValueTypeIsTheSendingAddressTypeThenTheObjectTypeThenTheSize() throws Exception {
        KnxProject project = read(modular());

        KnxGroupObject sub = object(project, "1.1.1", "MD-1_M-1_MI-1_SM-1_M-1_MI-1-1-1_SM-1_O-3-1_R-1");
        assertEquals(new KnxObjectValueType(new KnxDatapointType(5, null), 8), project.datapointTypeOf(sub),
                "the address type wins over the object's own 5.010");
        KnxGroupObject untyped = object(project, "1.1.1", "MD-1_M-1_MI-1_O-2-1_R-1");
        KnxObjectValueType unknown = project.datapointTypeOf(untyped);
        assertFalse(unknown.hasDatapointType(), "neither GA-2 nor the object has a type");
        assertEquals(1, unknown.sizeBits());
        KnxGroupObject noHardware = object(project, "1.1.5", "O-1_R-1");
        assertEquals(new KnxObjectValueType(new KnxDatapointType(1, 1), null), project.datapointTypeOf(noHardware));
        assertEquals(new KnxObjectValueType(null, null), project.datapointTypeOf(object(project, "1.1.1", "O-9_R-9")));
    }

    @Test
    void theObjectTypeIsUsedWhenTheSendingAddressHasNone() throws Exception {
        String instances = instance(1, "Links=\"GA-2 GA-3\"") + instance(2, "Links=\"GA-3\"")
                + instance(3, "Links=\"GA-2\" DatapointType=\"DPST-20-102\"");
        String objects = comObject(1, "DatapointType=\"DPST-7-1\" ObjectSize=\"2 Bytes\"")
                + comObject(2, "DatapointType=\"DPST-14-68\" ObjectSize=\"4 Bytes\"")
                + comObject(3, "DatapointType=\"DPST-7-1\" ObjectSize=\"1 Byte\"");
        KnxProject project = read(ProgramFixture.container(instances,
                objectsThenReferences(objects, reference(1, "") + reference(2, "") + reference(3, ""))));

        assertEquals(new KnxObjectValueType(new KnxDatapointType(7, 1), 16),
                project.datapointTypeOf(project.groupObjects().get(0)), "GA-2 has no type, GA-3 is not sent on");
        assertEquals(new KnxObjectValueType(new KnxDatapointType(9, 1), 32),
                project.datapointTypeOf(project.groupObjects().get(1)));
        assertEquals(new KnxObjectValueType(new KnxDatapointType(20, 102), 8),
                project.datapointTypeOf(project.groupObjects().get(2)), "the instance type is the object's own");
    }

    @Test
    void groupObjectsLinkedToAnAddressComeInTheOrderOfTheLinkedObjects() throws Exception {
        KnxProject project = read(modular());

        for (KnxGroupAddress address : project.groupAddresses()) {
            List<KnxObjectInstance> expected = project.objectsLinkedTo(address).stream().map(KnxLinkedObject::object)
                    .toList();
            List<KnxObjectInstance> linked = project.groupObjectsLinkedTo(address).stream()
                    .map(KnxGroupObject::instance).toList();
            assertEquals(expected, linked, address.id());
        }
        KnxGroupAddress central = java.util.Objects.requireNonNull(project.groupAddress("GA-5"));
        assertEquals(List.of("P-5A6B-0_DI-1", "P-5A6B-0_DI-2", "P-5A6B-0_DI-3", "P-5A6B-0_DI-4", "P-5A6B-0_DI-5"),
                project.groupObjectsLinkedTo(central).stream().map(KnxGroupObject::deviceId).toList());
        KnxGroupAddress all = java.util.Objects.requireNonNull(project.groupAddress("GA-1"));
        assertEquals(List.of("O-1_R-1", "MD-1_M-1_MI-1_O-2-1_R-1"),
                project.groupObjectsLinkedTo(all).stream().map(o -> o.instance().refId()).toList());
    }

    @Test
    void anObjectLinkedTwiceToOneAddressIsListedOnce() throws Exception {
        KnxProject project = read(ProgramFixture.container(instance(1, "Links=\"GA-1 GA-3 GA-1\""),
                objectsThenReferences(comObject(1, ""), reference(1, ""))));
        KnxGroupAddress address = java.util.Objects.requireNonNull(project.groupAddress("GA-1"));

        assertEquals(1, project.groupObjectsLinkedTo(address).size());
        assertTrue(
                project.groupObjectsLinkedTo(java.util.Objects.requireNonNull(project.groupAddress("GA-2"))).isEmpty());
    }

    @Test
    void equalsAndHashCodeCoverTheManufacturerData() throws Exception {
        TestContainer container = modular();
        KnxProject one = read(container);
        KnxProject same = read(container);
        KnxProject without = one.withManufacturerData(List.of(), List.of());

        assertEquals(one, same);
        assertEquals(one.hashCode(), same.hashCode());
        assertNotEquals(one, without);
        assertNotEquals(one, one.withManufacturerData(one.products(), List.of()));
        assertTrue(one.toString().contains("Made-up actuator 8-fold"));
    }
}
