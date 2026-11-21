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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Made-up projects of one manufacturer with one application program, written as text for a test. The devices
 * are ETS 6 devices with short ids; group addresses: {@code GA-1} 1/0/1 (1.001), {@code GA-2} 1/0/2 (none),
 * {@code GA-3} 1/0/3 (9.001).
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class ProgramFixture {

    static final String PART = "P-7C8D";
    static final String APP = "M-00C8_A-0200-10-ABCD";
    static final String H2P = "M-00C8_H-0100-1_HP-0200-10-ABCD";
    static final String PRODUCT = "M-00C8_H-0100-1_P-TEST.1";
    static final String HARDWARE_ENTRY = "M-00C8/Hardware.xml";
    static final String PROGRAM_ENTRY = "M-00C8/" + APP + ".xml";

    private static final String KNX = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<KNX xmlns=\"http://knx.org/xml/project/23\" CreatedBy=\"ETS6\" ToolVersion=\"6.4.0\">\n";

    private static final String PROJECT = KNX + "<Project Id=\"" + PART + "\">"
            + "<ProjectInformation Name=\"Proefproject\" GroupAddressStyle=\"ThreeLevel\" /></Project></KNX>\n";

    private ProgramFixture() {
    }

    /** One device with the given {@code ComObjectInstanceRef} elements, and the program with this content. */
    static TestContainer container(String instances, String programContent) {
        return container(1, instances, programContent);
    }

    /** {@code devices} devices of the same product, each with the given object instances. */
    static TestContainer container(int devices, String instances, String programContent) {
        return container(devices, instances, "", programContent, "");
    }

    /**
     * One device with these object instances and this further content (parameter values, module instances),
     * and the program with this content, followed in the file by these {@code Language} elements.
     */
    static TestContainer container(String instances, String deviceContent, String programContent, String languages) {
        return container(1, instances, deviceContent, programContent, languages);
    }

    private static TestContainer container(int devices, String instances, String deviceContent, String programContent,
            String languages) {
        return TestContainer.withMaster().put(PART + "/project.xml", PROJECT)
                .put(PART + "/0.xml", installation(devices, instances, deviceContent))
                .put(HARDWARE_ENTRY, hardware(APP)).put(PROGRAM_ENTRY, program(APP, programContent, languages));
    }

    static String installation(int devices, String instances) {
        return installation(devices, instances, "");
    }

    static String installation(int devices, String instances, String deviceContent) {
        StringBuilder xml = new StringBuilder(KNX).append("<Project Id=\"").append(PART).append("\"><Installations>")
                .append("<Installation Name=\"\"><Topology><Area Id=\"").append(PART).append("-0_A-1\" Address=\"1\">")
                .append("<Line Id=\"").append(PART).append("-0_L-1\" Address=\"1\">");
        for (int i = 1; i <= devices; i++) {
            xml.append("<DeviceInstance Id=\"").append(PART).append("-0_DI-").append(i).append("\" Address=\"")
                    .append(i).append("\" ProductRefId=\"").append(PRODUCT).append("\" Hardware2ProgramRefId=\"")
                    .append(H2P).append("\"><ComObjectInstanceRefs>").append(instances)
                    .append("</ComObjectInstanceRefs>").append(deviceContent).append("</DeviceInstance>\n");
        }
        xml.append("</Line></Area></Topology><GroupAddresses><GroupRanges>").append("<GroupRange Id=\"").append(PART)
                .append("-0_GR-1\" Name=\"Een\" RangeStart=\"2048\"").append(" RangeEnd=\"4095\"><GroupRange Id=\"")
                .append(PART).append("-0_GR-2\" Name=\"Nul\" RangeStart=\"2048\" RangeEnd=\"2303\">")
                .append("<GroupAddress Id=\"").append(PART)
                .append("-0_GA-1\" Address=\"2049\" Name=\"Schakelen\" DatapointType=\"DPST-1-1\" />")
                .append("<GroupAddress Id=\"").append(PART).append("-0_GA-2\" Address=\"2050\" Name=\"Zonder type\" />")
                .append("<GroupAddress Id=\"").append(PART)
                .append("-0_GA-3\" Address=\"2051\" Name=\"Temperatuur\" DatapointType=\"DPST-9-1\" />")
                .append("</GroupRange></GroupRange></GroupRanges></GroupAddresses>")
                .append("</Installation></Installations></Project></KNX>\n");
        return xml.toString();
    }

    /** {@code Hardware.xml} of the device's product, its {@code Hardware2Program} naming this program. */
    static String hardware(String programRefId) {
        return KNX + "<ManufacturerData><Manufacturer RefId=\"M-00C8\"><Hardware><Hardware Id=\"M-00C8_H-0100-1\""
                + " Name=\"Made-up test device\" SerialNumber=\"MADEUP-HW-SERIAL-TEST\"><Products><Product Id=\""
                + PRODUCT + "\" Text=\"Made-up test device\" OrderNumber=\"MU TEST.1\" /></Products>"
                + "<Hardware2Programs><Hardware2Program Id=\"" + H2P + "\"><ApplicationProgramRef RefId=\""
                + programRefId + "\" /></Hardware2Program></Hardware2Programs></Hardware></Hardware>"
                + "</Manufacturer></ManufacturerData></KNX>\n";
    }

    /** A program file holding one {@code ApplicationProgram} with this id and content. */
    static String program(String id, String content) {
        return program(id, content, "");
    }

    /** The same, followed by these {@code Language} elements. */
    static String program(String id, String content, String languages) {
        return KNX + "<ManufacturerData><Manufacturer RefId=\"M-00C8\"><ApplicationPrograms>"
                + "<ApplicationProgram Id=\"" + id + "\" ApplicationNumber=\"512\" ApplicationVersion=\"16\""
                + " DefaultLanguage=\"en-US\">\n" + content + "\n</ApplicationProgram></ApplicationPrograms>"
                + (languages.isEmpty() ? "" : "<Languages>" + languages + "</Languages>")
                + "</Manufacturer></ManufacturerData></KNX>\n";
    }

    /** A {@code Language} of this program with these {@code TranslationElement} elements. */
    static String language(String identifier, String elements) {
        return "<Language Identifier=\"" + identifier + "\"><TranslationUnit RefId=\"" + APP + "\">" + elements
                + "</TranslationUnit></Language>\n";
    }

    /** A {@code TranslationElement} for the element with this id, holding one translation. */
    static String translation(String elementId, String attributeName, String text) {
        return "<TranslationElement RefId=\"" + elementId + "\"><Translation AttributeName=\"" + attributeName
                + "\" Text=\"" + text + "\" /></TranslationElement>";
    }

    /** A {@code ParameterInstanceRef} of the device; {@code value} {@code null} leaves the attribute out. */
    static String parameterValue(String refId, @Nullable String value) {
        return "<ParameterInstanceRef RefId=\"" + refId + "\"" + (value == null ? "" : " Value=\"" + value + "\"")
                + " />";
    }

    /** {@code Static} with these {@code ComObject} and these {@code ComObjectRef} elements, in that order. */
    static String objectsThenReferences(String objects, String references) {
        return "<Static><ComObjectTable>" + objects + "</ComObjectTable><ComObjectRefs>" + references
                + "</ComObjectRefs></Static>";
    }

    /** A {@code ComObject} of the program with the id {@code <APP>_O-<n>} and these further attributes. */
    static String comObject(int n, String attributes) {
        return "<ComObject Id=\"" + APP + "_O-" + n + "\" Number=\"" + n + "\" " + attributes + " />\n";
    }

    /** A {@code ComObjectRef} {@code <APP>_O-<n>_R-<n>} of the object {@code <APP>_O-<n>}. */
    static String reference(int n, String attributes) {
        return "<ComObjectRef Id=\"" + APP + "_O-" + n + "_R-" + n + "\" RefId=\"" + APP + "_O-" + n + "\" "
                + attributes + " />\n";
    }

    /** An object instance {@code O-<n>_R-<n>} with these further attributes. */
    static String instance(int n, String attributes) {
        return "<ComObjectInstanceRef RefId=\"O-" + n + "_R-" + n + "\" " + attributes + " />\n";
    }
}
