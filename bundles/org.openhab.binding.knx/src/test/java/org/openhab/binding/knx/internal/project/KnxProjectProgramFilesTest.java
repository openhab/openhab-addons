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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;

/**
 * Tests for reading application program files: the overlay of the three levels, the forms of sizes and flags,
 * the bound on what is kept, and files of an unexpected shape.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxProjectProgramFilesTest {

    private static final long LARGE = 1L << 30;

    private final List<String> opened = Collections.synchronizedList(new ArrayList<>());
    private final KnxProjectReader reader = new KnxProjectReader(ReadLimits.DEFAULT, opened::add);

    private KnxProject read(TestContainer container) throws Exception {
        return reader.read(container.write(), null);
    }

    private static KnxGroupObject object(KnxProject project, String refId) {
        return project.groupObjects().stream().filter(o -> refId.equals(o.instance().refId())).findFirst()
                .orElseThrow();
    }

    private static List<String> types(KnxGroupObject object) {
        return object.datapointTypes().stream().map(KnxDatapointType::toString).toList();
    }

    private static KnxProjectException fails(KnxProjectReader reader, TestContainer container) {
        return assertThrows(KnxProjectException.class, () -> reader.read(container.write(), null));
    }

    private long opens(String entry) {
        return opened.stream().filter(entry::equals).count();
    }

    @Test
    void textFunctionTextTypeAndSizeComeFromTheFirstLevelThatHasThem() throws Exception {
        String instances = instance(1, "Links=\"GA-1\" Text=\"A instance\"")
                + instance(2, "Links=\"GA-2\" DatapointType=\"DPST-9-1\"")
                + instance(3, "Links=\"GA-3\" FunctionText=\"C instance function\"");
        String objects = comObject(1,
                "Text=\"A object\" FunctionText=\"A object function\" ObjectSize=\"1 Bit\""
                        + " DatapointType=\"DPST-7-1\"")
                + comObject(2,
                        "Text=\"B object\" FunctionText=\"B object function\" ObjectSize=\"4 Bytes\""
                                + " DatapointType=\"DPST-14-68\"")
                + comObject(3, "Text=\"C object\" FunctionText=\"C object function\" ObjectSize=\"1 Byte\""
                        + " DatapointType=\"DPST-5-1\"");
        String references = reference(1, "Text=\"A ref\" FunctionText=\"A ref function\" ObjectSize=\"2 Bytes\"")
                + reference(2, "Text=\"B ref\" DatapointType=\"DPST-5-1\"")
                + reference(3, "FunctionText=\"C ref function\" DatapointType=\"DPST-1-1\"");

        KnxProject project = read(ProgramFixture.container(instances, objectsThenReferences(objects, references)));

        KnxGroupObject a = object(project, "O-1_R-1");
        assertEquals("A instance", a.text());
        assertEquals("A ref function", a.functionText());
        assertEquals(List.of("7.001"), types(a));
        assertEquals("2 Bytes", a.sizeText());
        assertEquals(16, a.sizeBits());
        KnxGroupObject b = object(project, "O-2_R-2");
        assertEquals("B ref", b.text());
        assertEquals("B object function", b.functionText());
        assertEquals(List.of("9.001"), types(b));
        assertEquals(32, b.sizeBits());
        KnxGroupObject c = object(project, "O-3_R-3");
        assertEquals("C object", c.text());
        assertEquals("C instance function", c.functionText());
        assertEquals(List.of("1.001"), types(c));
        assertEquals("1 Byte", c.sizeText());
        assertEquals(8, c.sizeBits());
    }

    @Test
    void eachFlagComesFromItsOwnFirstLevel() throws Exception {
        String instances = instance(1, "Links=\"GA-1\" ReadFlag=\"Enabled\" TransmitFlag=\"Disabled\"")
                + instance(2, "Links=\"GA-1\" CommunicationFlag=\"Disabled\" ReadOnInitFlag=\"Enabled\"")
                + instance(3, "Links=\"GA-1\" WriteFlag=\"Disabled\" UpdateFlag=\"Enabled\"");
        String references = reference(1, "WriteFlag=\"Enabled\" UpdateFlag=\"Disabled\" TransmitFlag=\"Enabled\"")
                + reference(2, "ReadFlag=\"Enabled\" TransmitFlag=\"Disabled\"")
                + reference(3, "CommunicationFlag=\"Disabled\" ReadOnInitFlag=\"Disabled\"");
        String objects = comObject(1,
                "ReadFlag=\"Disabled\" WriteFlag=\"Disabled\" CommunicationFlag=\"Enabled\""
                        + " TransmitFlag=\"Enabled\" UpdateFlag=\"Enabled\" ReadOnInitFlag=\"Enabled\"")
                + comObject(2,
                        "ReadFlag=\"Disabled\" WriteFlag=\"Enabled\" CommunicationFlag=\"Enabled\""
                                + " TransmitFlag=\"Enabled\" UpdateFlag=\"Enabled\" ReadOnInitFlag=\"Disabled\"")
                + comObject(3, "ReadFlag=\"Enabled\" WriteFlag=\"Enabled\" CommunicationFlag=\"Enabled\""
                        + " TransmitFlag=\"Enabled\" UpdateFlag=\"Disabled\" ReadOnInitFlag=\"Enabled\"");

        KnxProject project = read(ProgramFixture.container(instances, objectsThenReferences(objects, references)));

        assertEquals(new KnxGroupObjectFlags(true, true, true, false, false, true), object(project, "O-1_R-1").flags());
        assertEquals(new KnxGroupObjectFlags(true, true, false, false, true, true), object(project, "O-2_R-2").flags());
        assertEquals(new KnxGroupObjectFlags(true, false, false, true, true, false),
                object(project, "O-3_R-3").flags());
    }

    @Test
    void flagValuesOtherThanEnabledOrDisabledCountAsNotSet() throws Exception {
        String instances = instance(1, "Links=\"GA-1\" ReadFlag=\"enabled\" WriteFlag=\"Yes\" UpdateFlag=\"Disabled\""
                + " ReadOnInitFlag=\"Enabled\"");
        String references = reference(1, "ReadFlag=\"Disabled\"");
        String objects = comObject(1,
                "ReadFlag=\"Enabled\" WriteFlag=\"Enabled\" TransmitFlag=\"Maybe\"" + " UpdateFlag=\"Enabled\"");

        KnxProject project = read(ProgramFixture.container(instances, objectsThenReferences(objects, references)));

        assertEquals(new KnxGroupObjectFlags(false, true, false, false, false, true),
                object(project, "O-1_R-1").flags());
    }

    @Test
    void emptyValuesDoNotHideTheNextLevel() throws Exception {
        String instances = instance(1, "Links=\"GA-1\" Text=\"\" FunctionText=\"\" DatapointType=\"\"");
        String references = reference(1, "Text=\"\" ObjectSize=\"\"");
        String objects = comObject(1, "Text=\"Object text\" FunctionText=\"Object function\" ObjectSize=\"1 Bit\""
                + " DatapointType=\"DPST-1-8\"");

        KnxGroupObject object = object(
                read(ProgramFixture.container(instances, objectsThenReferences(objects, references))), "O-1_R-1");

        assertEquals("Object text", object.text());
        assertEquals("Object function", object.functionText());
        assertEquals(List.of("1.008"), types(object));
        assertEquals(1, object.sizeBits());
    }

    @Test
    void objectSizesInBitsAndBytesAreParsed() throws Exception {
        List<String> sizes = List.of("1 Bit", "4 Bit", "6 Bits", "1 Byte", "14 Bytes", "LegacyVarData", "2 bytes",
                "Bit");
        StringBuilder instances = new StringBuilder();
        StringBuilder objects = new StringBuilder();
        StringBuilder references = new StringBuilder();
        for (int i = 0; i < sizes.size(); i++) {
            instances.append(instance(i, "Links=\"GA-1\""));
            objects.append(comObject(i, "ObjectSize=\"" + sizes.get(i) + "\""));
            references.append(reference(i, ""));
        }

        KnxProject project = read(ProgramFixture.container(instances.toString(),
                objectsThenReferences(objects.toString(), references.toString())));

        List<Integer> bits = new ArrayList<>();
        for (int i = 0; i < sizes.size(); i++) {
            KnxGroupObject object = object(project, "O-" + i + "_R-" + i);
            assertEquals(sizes.get(i), object.sizeText());
            Integer size = object.sizeBits();
            bits.add(size == null ? -1 : size);
        }
        assertEquals(List.of(1, 4, 6, 8, 112, -1, -1, -1), bits);
    }

    @Test
    void anObjectNumberThatIsNotANumberIsLeftOpen() throws Exception {
        String objects = "<ComObject Id=\"" + APP + "_O-1\" Number=\"one\" Text=\"One\" />";

        KnxGroupObject object = object(read(ProgramFixture.container(instance(1, "Links=\"GA-1\""),
                objectsThenReferences(objects, reference(1, "")))), "O-1_R-1");

        assertEquals(KnxObjectDefinition.FOUND, object.definition());
        assertNull(object.number());
        assertEquals("One", object.text());
    }

    @Test
    void sectionsAreFoundInAnyOrder() throws Exception {
        String instances = instance(1, "Links=\"GA-1\"")
                + "<ComObjectInstanceRef RefId=\"MD-1_M-1_MI-1_O-2-1_R-1\" Links=\"GA-2\" />";
        String content = "<Dynamic><ComObjectRefRef RefId=\"" + APP + "_O-1_R-1\" /></Dynamic>"
                + "<ModuleDefs><ModuleDef Id=\"" + APP + "_MD-1\"><Static><ComObjectRefs><ComObjectRef Id=\"" + APP
                + "_MD-1_O-2-1_R-1\" RefId=\"" + APP + "_MD-1_O-2-1\" /></ComObjectRefs><ComObjects><ComObject Id=\""
                + APP + "_MD-1_O-2-1\" Number=\"2\" Text=\"Module output\" /></ComObjects></Static>"
                + "<Arguments /></ModuleDef></ModuleDefs>" + "<Static><Options /><BusInterfaces /><ComObjectRefs>"
                + reference(1, "") + "</ComObjectRefs><ComObjectTable>" + comObject(1, "Text=\"Plain\"")
                + "</ComObjectTable><Parameters /><Code /></Static>";

        KnxProject project = read(ProgramFixture.container(instances, content));

        assertEquals("Plain", object(project, "O-1_R-1").text());
        assertEquals("Module output", object(project, "MD-1_M-1_MI-1_O-2-1_R-1").text());
        assertEquals(1, opens(PROGRAM_ENTRY), "references before their objects need one pass");
    }

    @Test
    void anEntryMissingFromTheRestOfTheFileCostsNoFurtherPass() throws Exception {
        String content = "<Static><ComObjectRefs>" + reference(1, "") + "<ComObjectRef Id=\"" + APP
                + "_O-2_R-2\" RefId=\"" + APP + "_O-99\" /></ComObjectRefs><ComObjectTable>"
                + comObject(1, "Text=\"One\"") + "</ComObjectTable></Static>";

        KnxProject project = read(
                ProgramFixture.container(instance(1, "Links=\"GA-1\"") + instance(2, "Links=\"GA-2\""), content));

        assertEquals("One", object(project, "O-1_R-1").text());
        assertEquals(KnxObjectDefinition.NOT_IN_PROGRAM, object(project, "O-2_R-2").definition());
        assertEquals(1, opens(PROGRAM_ENTRY), "no object came before the reference that names the missing one");
    }

    @Test
    void anEntryWantedAfterEntriesOfItsKindWerePassedCostsAnotherPass() throws Exception {
        String content = objectsThenReferences(comObject(1, "Text=\"One\""), reference(1, ""));

        KnxProject project = read(ProgramFixture.container(instance(1, "Links=\"GA-1\""), content));

        assertEquals("One", object(project, "O-1_R-1").text());
        assertEquals(2, opens(PROGRAM_ENTRY));
    }

    @Test
    void objectsAreOnlyTakenFromTheStaticPartsOfTheProgram() throws Exception {
        String content = "<Dynamic><Static><ComObjectRefs>" + reference(1, "Text=\"From the dialog\"")
                + "</ComObjectRefs></Static></Dynamic>" + "<Static><ComObjectTable>" + comObject(1, "Text=\"Static\"")
                + "</ComObjectTable><Other><ComObjectRef Id=\"" + APP + "_O-1_R-1\" RefId=\"" + APP
                + "_O-1\" Text=\"Misplaced\" /></Other></Static>";

        KnxGroupObject object = object(read(ProgramFixture.container(instance(1, "Links=\"GA-1\""), content)),
                "O-1_R-1");

        assertEquals(KnxObjectDefinition.NOT_IN_PROGRAM, object.definition());
    }

    @Test
    void aLargeProgramFileIsReadKeepingOnlyTheWantedEntries() throws Exception {
        StringBuilder objects = new StringBuilder();
        StringBuilder references = new StringBuilder();
        StringBuilder parameters = new StringBuilder("<Parameters>");
        for (int i = 1000; i < 31_000; i++) {
            objects.append(comObject(i, "Text=\"Unwanted\" ObjectSize=\"1 Bit\""));
            references.append(reference(i, ""));
            parameters.append("<Parameter Id=\"").append(APP).append("_P-").append(i).append("\" Value=\"0\" />");
        }
        parameters.append("</Parameters>");
        objects.append(comObject(1, "Text=\"One\"")).append(comObject(2, "Text=\"Two\""));
        references.insert(0, reference(1, "")).append(reference(2, ""));
        String content = "<Static>" + parameters + "<ComObjectTable>" + objects + "</ComObjectTable><ComObjectRefs>"
                + references + "</ComObjectRefs></Static>";
        TestContainer container = ProgramFixture
                .container(instance(1, "Links=\"GA-1\"") + instance(2, "Links=\"GA-2\""), content);
        KnxProjectReader bounded = new KnxProjectReader(new ReadLimits(100, LARGE, LARGE, 10_000, 1 << 20, 1 << 20, 4),
                opened::add);

        KnxProject project = bounded.read(container.write(), null);

        assertEquals("One", object(project, "O-1_R-1").text());
        assertEquals("Two", object(project, "O-2_R-2").text());
        assertTrue(container.text(PROGRAM_ENTRY).length() > 5_000_000);
    }

    @Test
    void moreWantedEntriesThanTheLimitAllowsIsRefused() throws Exception {
        String instances = instance(1, "Links=\"GA-1\"") + instance(2, "Links=\"GA-1\"")
                + instance(3, "Links=\"GA-1\"");
        String content = objectsThenReferences(comObject(1, "") + comObject(2, "") + comObject(3, ""),
                reference(1, "") + reference(2, "") + reference(3, ""));
        TestContainer container = ProgramFixture.container(instances, content);

        assertEquals(Reason.LIMIT_EXCEEDED,
                fails(new KnxProjectReader(new ReadLimits(100, LARGE, LARGE, 100, 1 << 20, 1 << 20, 5)), container)
                        .getReason());
        assertEquals(3, new KnxProjectReader(new ReadLimits(100, LARGE, LARGE, 100, 1 << 20, 1 << 20, 6))
                .read(container.write(), null).groupObjects().size());
    }

    @Test
    void aDocumentTypeDeclarationInAProgramFileIsRefused() {
        TestContainer container = ProgramFixture.container(instance(1, "Links=\"GA-1\""),
                objectsThenReferences(comObject(1, ""), reference(1, "")));
        container.put(PROGRAM_ENTRY, container.text(PROGRAM_ENTRY).replaceFirst("<KNX ", "<!DOCTYPE KNX>\n<KNX "));

        assertEquals(Reason.MALFORMED, fails(reader, container).getReason());
    }

    @Test
    void aDocumentTypeDeclarationInAHardwareFileIsRefused() {
        TestContainer container = ProgramFixture.container(instance(1, "Links=\"GA-1\""), "");
        container.put(HARDWARE_ENTRY, container.text(HARDWARE_ENTRY).replaceFirst("<KNX ",
                "<!DOCTYPE KNX [<!ENTITY e SYSTEM \"file:///etc/hostname\">]>\n<KNX "));

        KnxProjectException e = fails(reader, container);

        assertEquals(Reason.MALFORMED, e.getReason());
        assertTrue(String.valueOf(e.getMessage()).contains(HARDWARE_ENTRY));
    }

    @Test
    void aBrokenProgramFileIsMalformedWithoutItsContentInTheMessage() {
        TestContainer container = ProgramFixture.container(instance(1, "Links=\"GA-1\""),
                "<Static><ComObjectTable>" + comObject(1, "Text=\"MADEUP-SECRET-TEXT\"") + "</Wrong>");

        KnxProjectException e = fails(reader, container);

        assertEquals(Reason.MALFORMED, e.getReason());
        assertFalse(String.valueOf(e.getMessage()).contains("MADEUP"));
    }

    @Test
    void filesWithAnotherRootElementAreMalformed() {
        TestContainer program = ProgramFixture.container(instance(1, "Links=\"GA-1\""), "");
        program.put(PROGRAM_ENTRY, "<Other><ApplicationProgram Id=\"" + APP + "\" /></Other>");
        assertEquals(Reason.MALFORMED, fails(reader, program).getReason());

        TestContainer hardware = ProgramFixture.container(instance(1, "Links=\"GA-1\""), "");
        hardware.put(HARDWARE_ENTRY, "<Other />");
        assertEquals(Reason.MALFORMED, fails(reader, hardware).getReason());
    }

    @Test
    void manufacturerFilesCountAgainstTheTotalBytes() throws Exception {
        TestContainer container = ProgramFixture.container(instance(1, "Links=\"GA-1\""),
                "<Static><ComObjectRefs>" + reference(1, "") + "</ComObjectRefs><ComObjectTable>"
                        + comObject(1, "Text=\"One\"") + "</ComObjectTable></Static>");
        long needed = 0;
        for (String entry : List.of(PART + "/project.xml", PART + "/0.xml", HARDWARE_ENTRY, PROGRAM_ENTRY)) {
            needed += container.text(entry).getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        }

        assertEquals(Reason.LIMIT_EXCEEDED,
                fails(new KnxProjectReader(new ReadLimits(100, LARGE, needed - 1, 100, 1 << 20, 1 << 20)), container)
                        .getReason());
        assertEquals(KnxObjectDefinition.FOUND,
                new KnxProjectReader(new ReadLimits(100, LARGE, needed, 100, 1 << 20, 1 << 20))
                        .read(container.write(), null).groupObjects().get(0).definition());
    }
}
