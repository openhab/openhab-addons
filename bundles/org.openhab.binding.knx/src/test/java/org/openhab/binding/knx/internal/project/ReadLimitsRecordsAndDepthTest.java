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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.function.BiFunction;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;

/**
 * Tests the limits on the records one read creates and on the nesting depth of the XML files, in every file
 * and pass of a read.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ReadLimitsRecordsAndDepthTest {

    private static final long LARGE = 1L << 30;
    private static final int DEPTH = 30;

    private static ReadLimits records(int maxRecords) {
        return new ReadLimits(100, LARGE, LARGE, 10_000, 1 << 20, 1 << 20, ReadLimits.MAX_PROGRAM_ENTRIES, maxRecords,
                ReadLimits.MAX_DEPTH);
    }

    private static ReadLimits depth(int maxDepth) {
        return new ReadLimits(100, LARGE, LARGE, 10_000, 1 << 20, 1 << 20, ReadLimits.MAX_PROGRAM_ENTRIES,
                ReadLimits.MAX_RECORDS, maxDepth);
    }

    private static boolean reads(ReadLimits limits, TestContainer container) {
        Path file = container.write();
        try {
            new KnxProjectReader(limits).read(file, null);
            return true;
        } catch (KnxProjectException e) {
            assertEquals(Reason.LIMIT_EXCEEDED, e.getReason(), String.valueOf(e.getMessage()));
            return false;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The smallest record limit a read of the container passes. */
    private static int needed(TestContainer container) {
        int low = 1;
        int high = 100_000;
        assertTrue(reads(records(high), container));
        while (low < high) {
            int middle = (low + high) / 2;
            if (reads(records(middle), container)) {
                high = middle;
            } else {
                low = middle + 1;
            }
        }
        return low;
    }

    private static String chain(int levels) {
        return "<x>".repeat(levels) + "</x>".repeat(levels);
    }

    private static TestContainer program(String instances, String deviceContent, String content) {
        return ProgramFixture.container(instances, deviceContent, content, "");
    }

    private static String replaceLast(String text, String what, String with) {
        int at = text.lastIndexOf(what);
        assertTrue(at >= 0, what);
        return text.substring(0, at) + with + text.substring(at + what.length());
    }

    private static TestContainer plain() {
        return ProgramFixture.container(instance(1, "Links=\"GA-1\""),
                objectsThenReferences(comObject(1, ""), reference(1, "")));
    }

    /**
     * Puts a chain of elements into the entry so that the deepest element has the depth limit, which is read, and
     * one level deeper, which is refused.
     *
     * @param hangsFrom the depth of the element the chain is put into
     */
    private static void assertDepthLimit(String entry, int hangsFrom, BiFunction<String, String, String> insert) {
        int levels = DEPTH - hangsFrom;
        TestContainer atLimit = plain();
        atLimit.put(entry, insert.apply(atLimit.text(entry), chain(levels)));
        assertTrue(reads(depth(DEPTH), atLimit), entry + " at the limit");

        TestContainer over = plain();
        over.put(entry, insert.apply(over.text(entry), chain(levels + 1)));
        KnxProjectException e = assertThrows(KnxProjectException.class,
                () -> new KnxProjectReader(depth(DEPTH)).read(over.write(), null));
        assertEquals(Reason.LIMIT_EXCEEDED, e.getReason());
        String message = String.valueOf(e.getMessage());
        assertTrue(message.contains("nests elements deeper than " + DEPTH + " levels"), message);
    }

    @Test
    void depthInProjectXml() {
        assertDepthLimit(PART + "/project.xml", 2,
                (text, chain) -> replaceLast(text, "</Project>", chain + "</Project>"));
    }

    @Test
    void depthInTheInstallationFile() {
        assertDepthLimit(PART + "/0.xml", 4,
                (text, chain) -> text.replace("<Installation Name=\"\">", "<Installation Name=\"\">" + chain));
    }

    @Test
    void depthInASkippedSecondInstallation() {
        assertDepthLimit(PART + "/0.xml", 4, (text, chain) -> replaceLast(text, "</Installations>",
                "<Installation Name=\"second\">" + chain + "</Installation></Installations>"));
    }

    @Test
    void depthInTheHardwareFile() {
        assertDepthLimit(HARDWARE_ENTRY, 1, (text, chain) -> replaceLast(text, "</KNX>", chain + "</KNX>"));
    }

    @Test
    void depthInAProgramFile() {
        assertDepthLimit(PROGRAM_ENTRY, 1, (text, chain) -> replaceLast(text, "</KNX>", chain + "</KNX>"));
    }

    @Test
    void depthInASkippedPartOfAProgramFile() {
        assertDepthLimit(PROGRAM_ENTRY, 6, (text, chain) -> text.replace("</ApplicationProgram>",
                "<Dynamic>" + chain + "</Dynamic></ApplicationProgram>"));
    }

    @Test
    void theDefaultDepthIsReadAndOneMoreIsRefused() {
        int levels = ReadLimits.MAX_DEPTH - 2;
        TestContainer atLimit = plain();
        atLimit.put(PART + "/project.xml",
                replaceLast(atLimit.text(PART + "/project.xml"), "</Project>", chain(levels) + "</Project>"));
        assertTrue(reads(ReadLimits.DEFAULT, atLimit));
        TestContainer over = plain();
        over.put(PART + "/project.xml",
                replaceLast(over.text(PART + "/project.xml"), "</Project>", chain(levels + 1) + "</Project>"));
        assertFalse(reads(ReadLimits.DEFAULT, over));
    }

    @Test
    void recordsOfTheInstallationFile() {
        TestContainer base = plain();
        int baseline = needed(base);
        TestContainer more = plain();
        StringBuilder addresses = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            addresses.append("<GroupAddress Id=\"").append(PART).append("-0_GA-").append(100 + i)
                    .append("\" Address=\"").append(3000 + i).append("\" Name=\"More\" />");
        }
        more.put(PART + "/0.xml", more.text(PART + "/0.xml").replace("</GroupRange></GroupRange>",
                addresses + "</GroupRange></GroupRange>"));

        assertEquals(baseline + 50, needed(more));
    }

    private static String types(int count) {
        StringBuilder text = new StringBuilder();
        for (int i = 1; i <= count; i++) {
            text.append(i > 1 ? " " : "").append("DPST-5-").append(i);
        }
        return text.toString();
    }

    @Test
    void datapointTypesOfAGroupAddressAreRecords() {
        int baseline = needed(plain());
        TestContainer more = plain();
        more.put(PART + "/0.xml",
                more.text(PART + "/0.xml").replace("</GroupRange></GroupRange>",
                        "<GroupAddress Id=\"" + PART + "-0_GA-100\" Address=\"3000\" Name=\"More\" DatapointType=\""
                                + types(30) + "\" /></GroupRange></GroupRange>"));

        // the address and its thirty types
        assertEquals(baseline + 1 + 30, needed(more));
    }

    @Test
    void datapointTypesOfAnInstanceAndItsLinksAreRecords() {
        int baseline = needed(plain());
        TestContainer typed = ProgramFixture.container(
                instance(1, "Links=\"GA-1\" DatapointType=\"" + types(25) + "\""),
                objectsThenReferences(comObject(1, ""), reference(1, "")));
        assertEquals(baseline + 25, needed(typed));

        StringBuilder ids = new StringBuilder();
        for (int i = 1; i <= 20; i++) {
            ids.append(i > 1 ? " " : "").append("GA-").append(i);
        }
        TestContainer linked = ProgramFixture.container(instance(1, "Links=\"" + ids + "\""),
                objectsThenReferences(comObject(1, ""), reference(1, "")));
        assertEquals(baseline + 19, needed(linked));
    }

    @Test
    void aProgramLevelTypeListIsCountedOnceNoMatterHowManyInstancesUseIt() {
        String content = objectsThenReferences(comObject(1, "DatapointType=\"" + types(30) + "\""), reference(1, ""));
        String untyped = objectsThenReferences(comObject(1, ""), reference(1, ""));
        int without = needed(ProgramFixture.container(5, instance(1, "Links=\"GA-1\""), untyped));
        int with = needed(ProgramFixture.container(5, instance(1, "Links=\"GA-1\""), content));

        assertEquals(without + 30, with);

        String onReference = objectsThenReferences(comObject(1, ""),
                reference(1, "DatapointType=\"" + types(20) + "\""));
        assertEquals(without + 20, needed(ProgramFixture.container(5, instance(1, "Links=\"GA-1\""), onReference)));
    }

    @Test
    void tooManyDatapointTypesInOneAttributeIsRefused() {
        TestContainer container = plain();
        container.put(PART + "/0.xml",
                container.text(PART + "/0.xml").replace("</GroupRange></GroupRange>",
                        "<GroupAddress Id=\"" + PART + "-0_GA-100\" Address=\"3000\" Name=\"More\" DatapointType=\""
                                + types(2_000) + "\" /></GroupRange></GroupRange>"));

        assertFalse(reads(records(1_000), container));
    }

    @Test
    void recordsOfTheDevices() {
        int baseline = needed(plain());
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            values.append(parameterValue("P-" + i + "_R-" + i, "1"));
        }
        TestContainer more = program(instance(1, "Links=\"GA-1\""),
                "<ParameterInstanceRefs>" + values + "</ParameterInstanceRefs>",
                objectsThenReferences(comObject(1, ""), reference(1, "")));

        assertEquals(baseline + 40, needed(more));
    }

    @Test
    void recordsKeptFromAProgramFile() {
        TestContainer none = program(instance(1, "Links=\"GA-1\"") + instance(2, "Links=\"GA-1\""), "", "<Static />");
        TestContainer found = program(instance(1, "Links=\"GA-1\"") + instance(2, "Links=\"GA-1\""), "",
                objectsThenReferences(comObject(1, "") + comObject(2, ""), reference(1, "") + reference(2, "")));

        // a reference and an object for each of the two instances
        assertEquals(needed(none) + 4, needed(found));
    }

    @Test
    void recordsKeptFromTheHardwareFile() {
        TestContainer with = plain();
        TestContainer without = plain();
        without.put(HARDWARE_ENTRY, "<KNX><ManufacturerData /></KNX>");

        // a product and a Hardware2Program; the program file is not found without the latter, which adds its entries
        TestContainer withoutEntries = program(instance(1, "Links=\"GA-1\""), "", "<Static />");
        assertEquals(needed(without) + 2, needed(withoutEntries));
        assertTrue(needed(with) > needed(withoutEntries));
    }

    @Test
    void tooManyRecordsIsRefusedWithTheLimitNamed() {
        TestContainer container = plain();
        int needed = needed(container);
        KnxProjectException e = assertThrows(KnxProjectException.class,
                () -> new KnxProjectReader(records(needed - 1)).read(container.write(), null));
        assertEquals(Reason.LIMIT_EXCEEDED, e.getReason());
        String message = String.valueOf(e.getMessage());
        assertTrue(message.contains("more than " + (needed - 1) + " records"), message);
    }

    @Test
    void theDefaultsKeepRoomForARealProject() {
        assertTrue(ReadLimits.DEFAULT.maxRecords() >= 100_000);
        assertTrue(ReadLimits.DEFAULT.maxDepth() >= 64);
        ReadLimits six = new ReadLimits(1, 2, 3, 4, 5, 6);
        assertEquals(ReadLimits.MAX_RECORDS, six.maxRecords());
        assertEquals(ReadLimits.MAX_DEPTH, six.maxDepth());
        ReadLimits seven = new ReadLimits(1, 2, 3, 4, 5, 6, 7);
        assertEquals(7, seven.maxProgramEntries());
        assertEquals(ReadLimits.MAX_RECORDS, seven.maxRecords());
    }
}
