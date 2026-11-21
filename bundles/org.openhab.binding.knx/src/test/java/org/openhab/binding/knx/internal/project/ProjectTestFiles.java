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

import static org.openhab.binding.knx.internal.project.ProtectedPartWriter.DEFLATED;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Made-up ETS 6 projects as {@code .knxproj} files for tests outside the reader's package. Only individual
 * addresses {@code 7.7.x} and group addresses {@code 31/7/x}.
 * <ul>
 * <li>{@code 31/7/1} "Made-up light", 1.001, secured; {@code 31/7/2} "Made-up temperature", 9.001;
 * {@code 31/7/3} "Made-up access", 15.000 (the monitor cannot decode it); {@code 31/7/4} a name with a newline
 * and an escape character, 1.001; {@code 31/7/5} "Made-up blind", 1.008; {@code 31/7/6} "Made-up display",
 * 16.000.</li>
 * <li>{@code 7.7.1} "Made-up switch actuator" (own name), objects from a program file, one module instance,
 * parameter values; {@code 7.7.2} without own name, named by its product "Made-up push button seven", no program
 * file; one device without an address.</li>
 * <li>A building with a room, a function in the room; a second installation that is not read.</li>
 * <li>{@code MADEUP-} values in key, tool key, password and device password attributes.</li>
 * </ul>
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class ProjectTestFiles {

    /** The password of {@link #protectedProject()}. */
    public static final String PASSWORD = "made-up Seven 77";
    /** The value of the device's text parameter, which is never kept. */
    public static final String PARAMETER_VALUE = "Made-up parameter text 7";
    public static final String PROJECT_NAME = "Made-up Seven";
    public static final String DEVICE_NAME = "Made-up switch actuator";
    public static final String PRODUCT_TEXT = "Made-up push button seven";

    private static final String PART = "P-0777";
    private static final String APP = "M-00C8_A-0777-10-7777";
    private static final String HARDWARE = "M-00C8_H-0077-1";
    private static final String KNX = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<KNX xmlns=\"http://knx.org/xml/project/23\" CreatedBy=\"ETS6\" ToolVersion=\"6.4.7777.0\">\n";
    private static final String KNX_1_1 = "<?xml version=\"1.1\" encoding=\"utf-8\"?>\n"
            + "<KNX xmlns=\"http://knx.org/xml/project/23\" CreatedBy=\"ETS6\" ToolVersion=\"6.4.7777.0\">\n";

    private ProjectTestFiles() {
    }

    /** The plain project. */
    public static Path plain() {
        return plain(PROJECT_NAME);
    }

    /** The plain project under another project name. */
    public static Path plain(String projectName) {
        return container(projectXml(projectName), installationXml(groupRanges())).write();
    }

    /** The plain project with the push button's product, of the devices 2 and 3, ordered as a filter device. */
    public static Path withFilterDevices() {
        return container(projectXml(PROJECT_NAME), installationXml(groupRanges()))
                .put("M-00C8/Hardware.xml", hardwareXml().replace("OrderNumber=\"MU 7.2\"", "OrderNumber=\"dummy\""))
                .write();
    }

    /** The plain project whose group ranges are nested this deep, the address {@code 31/7/1} in the deepest. */
    public static Path nestedRanges(int depth) {
        StringBuilder ranges = new StringBuilder();
        for (int i = 1; i <= depth; i++) {
            ranges.append("<GroupRange Id=\"").append(PART).append("-0_GR-").append(i).append("\" Name=\"Level ")
                    .append(i).append("\" RangeStart=\"65280\" RangeEnd=\"65535\">");
        }
        ranges.append(address(1, "Made-up light", "DPST-1-1", ""));
        ranges.append("</GroupRange>".repeat(depth));
        return container(projectXml(PROJECT_NAME), installationXml(ranges.toString())).write();
    }

    /** The same project, protected with {@link #PASSWORD} as ETS 6 does it. */
    public static Path protectedProject() {
        byte[] inner = new ProtectedPartWriter(PASSWORD).aes("0.xml", installationXml(groupRanges()), DEFLATED, 1, 3)
                .aes("project.xml", projectXml(PROJECT_NAME), DEFLATED, 1, 3).build();
        TestContainer container = TestContainer.ofProtected(PART + ".zip", inner);
        container.put("M-00C8/Hardware.xml", hardwareXml()).put("M-00C8/" + APP + ".xml", programXml());
        return container.write();
    }

    /** A zip file that holds no project. */
    public static Path notAProject() {
        return TestContainer.withMaster().put("readme.txt", "made-up").write();
    }

    /** Every {@code MADEUP-} attribute value of the files of the project. */
    public static List<String> madeUpValues() {
        List<String> values = new ArrayList<>();
        Matcher matcher = Pattern.compile("=\"(MADEUP-[^\"]*)\"")
                .matcher(projectXml(PROJECT_NAME) + installationXml(groupRanges()) + hardwareXml() + programXml());
        while (matcher.find()) {
            values.add(matcher.group(1));
        }
        return values;
    }

    private static TestContainer container(String projectXml, String installationXml) {
        return TestContainer.withMaster().put(PART + "/project.xml", projectXml).put(PART + "/0.xml", installationXml)
                .put(PART + ".signature", "made-up signature").put("M-00C8/Hardware.xml", hardwareXml())
                .put("M-00C8/" + APP + ".xml", programXml());
    }

    private static String projectXml(String projectName) {
        return KNX + "<Project Id=\"" + PART + "\"><ProjectInformation Name=\"" + projectName
                + "\" GroupAddressStyle=\"ThreeLevel\" LastModified=\"2026-03-04T05:06:07.000Z\">"
                + "<ProjectTraces><ProjectTrace Date=\"2026-03-04T05:06:07.000Z\" UserName=\"MADEUP-USER-SEVEN\" />"
                + "</ProjectTraces><DeviceCertificates><DeviceCertificate FDSK=\"MADEUP-FDSK-SEVEN\""
                + " SerialNumber=\"MADEUP-CERT-SERIAL-SEVEN\" /></DeviceCertificates></ProjectInformation>"
                + "</Project></KNX>\n";
    }

    private static String address(int sub, String name, String type, String extra) {
        return "<GroupAddress Id=\"" + PART + "-0_GA-" + sub + "\" Address=\"" + (65280 + sub) + "\" Name=\"" + name
                + "\"" + (type.isEmpty() ? "" : " DatapointType=\"" + type + "\"") + extra + " />";
    }

    private static String groupRanges() {
        return "<GroupRange Id=\"" + PART + "-0_GR-1\" Name=\"Made-up main\" RangeStart=\"63488\" RangeEnd=\"65535\">"
                + "<GroupRange Id=\"" + PART + "-0_GR-2\" Name=\"Made-up middle\" RangeStart=\"65280\""
                + " RangeEnd=\"65535\">" //
                + address(1, "Made-up light", "DPST-1-1", " Key=\"MADEUP-GA-KEY-SEVEN\"")
                + address(2, "Made-up temperature", "DPST-9-1", " Description=\"made-up description\"")
                + address(3, "Made-up access", "DPST-15-0", "")
                + address(4, "Made-up&#10;second line&#x1B;[31m", "DPST-1-1", "")
                + address(5, "Made-up blind", "DPST-1-8", "") //
                + address(6, "Made-up display", "DPST-16-0", "") //
                + "</GroupRange></GroupRange>";
    }

    private static String installationXml(String groupRanges) {
        String device1 = "<DeviceInstance Id=\"" + PART + "-0_DI-1\" Name=\"" + DEVICE_NAME + "\" Address=\"1\""
                + " ProductRefId=\"" + HARDWARE + "_P-SEVEN.1\" Hardware2ProgramRefId=\"" + HARDWARE
                + "_HP-0777-10-7777\" SerialNumber=\"MADEUP-SERIAL-SEVEN\">"
                + "<Security ToolKey=\"MADEUP-TOOLKEY-SEVEN\" DeviceAuthenticationCode=\"MADEUP-DAC-SEVEN\""
                + " DeviceManagementPassword=\"MADEUP-DMP-SEVEN\" />" + "<BusInterfaces><BusInterface RefId=\"" + APP
                + "_BI-1\" Password=\"MADEUP-BI-PASSWORD-SEVEN\" />" + "</BusInterfaces><ComObjectInstanceRefs>"
                + "<ComObjectInstanceRef RefId=\"O-1_R-1\" Links=\"GA-1 GA-4\" />"
                + "<ComObjectInstanceRef RefId=\"O-2_R-2\" Links=\"GA-2\" />"
                + "<ComObjectInstanceRef RefId=\"MD-1_M-1_MI-1_O-3-1_R-3\" Links=\"GA-5\" />"
                + "</ComObjectInstanceRefs><ParameterInstanceRefs><ParameterInstanceRef RefId=\"" + APP
                + "_P-1_R-1\" Value=\"" + PARAMETER_VALUE + "\" /></ParameterInstanceRefs>"
                + "<ModuleInstances><ModuleInstance Id=\"MD-1_M-1_MI-1\" RefId=\"MD-1_M-1\" RepeatIndex=\"1\">"
                + "<Arguments><Argument RefId=\"MD-1_A-1\" Value=\"7\" /></Arguments></ModuleInstance>"
                + "</ModuleInstances></DeviceInstance>";
        String device2 = "<DeviceInstance Id=\"" + PART + "-0_DI-2\" Address=\"2\" ProductRefId=\"M-00C8_H-0078-1"
                + "_P-SEVEN.2\" Hardware2ProgramRefId=\"M-00C8_H-0078-1_HP-0778-10-7878\"><ComObjectInstanceRefs>"
                + "<ComObjectInstanceRef RefId=\"O-1_R-1\" Links=\"GA-1\" /></ComObjectInstanceRefs>"
                + "</DeviceInstance>";
        String device3 = "<DeviceInstance Id=\"" + PART + "-0_DI-3\" Name=\"Made-up spare\" ProductRefId=\""
                + "M-00C8_H-0078-1_P-SEVEN.2\" />";
        return KNX_1_1 + "<Project Id=\"" + PART + "\"><Installations>"
                + "<Installation Name=\"Made-up installation\" BCUKey=\"MADEUP-BCUKEY-SEVEN\"><Topology>"
                + "<Area Id=\"" + PART + "-0_A-7\" Name=\"Made-up area\" Address=\"7\">" //
                + "<Line Id=\"" + PART + "-0_L-7\" Name=\"Made-up line\" Address=\"7\">" //
                + "<Segment Id=\"" + PART + "-0_S-1\" Number=\"0\" MediumTypeRefId=\"MT-0\">" + device1 + device2
                + "</Segment></Line></Area>" //
                + "<UnassignedDevices>" + device3 + "</UnassignedDevices></Topology>" //
                + "<Locations><Space Id=\"" + PART + "-0_BP-1\" Name=\"Made-up building\" Type=\"Building\">"
                + "<Space Id=\"" + PART + "-0_BP-2\" Name=\"Made-up kitchen\" Type=\"Room\">"
                + "<DeviceInstanceRef RefId=\"" + PART + "-0_DI-1\" />" //
                + "<Function Id=\"" + PART + "-0_F-1\" Name=\"Made-up kitchen light\" Type=\"FT-1\">"
                + "<GroupAddressRef Id=\"" + PART + "-0_F-1_GA-1\" Name=\"Switch\" RefId=\"" + PART + "-0_GA-1\" />"
                + "</Function></Space></Space></Locations>" //
                + "<GroupAddresses><GroupRanges>" + groupRanges + "</GroupRanges></GroupAddresses>" + "</Installation>" //
                + "<Installation Name=\"Made-up other installation\"><Topology /></Installation>"
                + "</Installations></Project></KNX>\n";
    }

    private static String hardwareXml() {
        return KNX + "<ManufacturerData><Manufacturer RefId=\"M-00C8\"><Hardware>" //
                + "<Hardware Id=\"" + HARDWARE + "\" Name=\"Made-up switch actuator\""
                + " SerialNumber=\"MADEUP-HW-SERIAL-SEVEN\"><Products><Product Id=\"" + HARDWARE
                + "_P-SEVEN.1\" Text=\"Made-up switch actuator product\" OrderNumber=\"MU 7.1\" /></Products>"
                + "<Hardware2Programs><Hardware2Program Id=\"" + HARDWARE + "_HP-0777-10-7777\">"
                + "<ApplicationProgramRef RefId=\"" + APP + "\" /></Hardware2Program></Hardware2Programs></Hardware>"
                + "<Hardware Id=\"M-00C8_H-0078-1\" Name=\"Made-up push button\"><Products>"
                + "<Product Id=\"M-00C8_H-0078-1_P-SEVEN.2\" Text=\"" + PRODUCT_TEXT + "\" OrderNumber=\"MU 7.2\" />"
                + "</Products></Hardware></Hardware></Manufacturer></ManufacturerData></KNX>\n";
    }

    private static String programXml() {
        return KNX + "<ManufacturerData><Manufacturer RefId=\"M-00C8\"><ApplicationPrograms>" //
                + "<ApplicationProgram Id=\"" + APP + "\" ApplicationNumber=\"1911\" ApplicationVersion=\"16\""
                + " Name=\"Made-up program seven\" DefaultLanguage=\"en-US\"><Static>" //
                + "<Parameters><Parameter Id=\"" + APP + "_P-1\" Name=\"Label\" Text=\"Label\" Value=\"\" />"
                + "</Parameters><ParameterRefs><ParameterRef Id=\"" + APP + "_P-1_R-1\" RefId=\"" + APP
                + "_P-1\" /></ParameterRefs>" //
                + "<ComObjectTable>" //
                + "<ComObject Id=\"" + APP + "_O-1\" Name=\"Switch\" Text=\"Switch\" Number=\"1\""
                + " FunctionText=\"Made-up output\" ObjectSize=\"1 Bit\" ReadFlag=\"Disabled\" WriteFlag=\"Enabled\""
                + " CommunicationFlag=\"Enabled\" TransmitFlag=\"Disabled\" UpdateFlag=\"Disabled\""
                + " ReadOnInitFlag=\"Disabled\" DatapointType=\"DPST-1-1\" />" //
                + "<ComObject Id=\"" + APP + "_O-2\" Name=\"Temperature\" Text=\"Temperature\" Number=\"2\""
                + " FunctionText=\"Made-up sensor\" ObjectSize=\"2 Bytes\" ReadFlag=\"Enabled\""
                + " WriteFlag=\"Disabled\" CommunicationFlag=\"Enabled\" TransmitFlag=\"Enabled\""
                + " UpdateFlag=\"Disabled\" ReadOnInitFlag=\"Disabled\" DatapointType=\"DPST-9-1\" />"
                + "</ComObjectTable><ComObjectRefs>" //
                + "<ComObjectRef Id=\"" + APP + "_O-1_R-1\" RefId=\"" + APP + "_O-1\" />" //
                + "<ComObjectRef Id=\"" + APP + "_O-2_R-2\" RefId=\"" + APP + "_O-2\" />" //
                + "</ComObjectRefs><BusInterfaces><BusInterface Id=\"" + APP + "_BI-1\" AddressIndex=\"1\""
                + " Password=\"MADEUP-BI-PROGRAM-PASSWORD-SEVEN\" /></BusInterfaces></Static>" //
                + "<ModuleDefs><ModuleDef Id=\"" + APP + "_MD-1\" Name=\"Blind\"><Arguments><Argument Id=\"" + APP
                + "_MD-1_A-1\" Name=\"ch\" Allocates=\"1\" /></Arguments><Static><ComObjects>" //
                + "<ComObject Id=\"" + APP + "_MD-1_O-3-1\" Name=\"Blind\" Text=\"Blind {{ch}}\" Number=\"3\""
                + " FunctionText=\"Made-up blind\" ObjectSize=\"1 Bit\" ReadFlag=\"Disabled\" WriteFlag=\"Enabled\""
                + " CommunicationFlag=\"Enabled\" TransmitFlag=\"Disabled\" UpdateFlag=\"Disabled\""
                + " ReadOnInitFlag=\"Disabled\" DatapointType=\"DPST-1-8\" />" //
                + "</ComObjects><ComObjectRefs><ComObjectRef Id=\"" + APP + "_MD-1_O-3-1_R-3\" RefId=\"" + APP
                + "_MD-1_O-3-1\" /></ComObjectRefs></Static></ModuleDef></ModuleDefs>" //
                + "</ApplicationProgram></ApplicationPrograms></Manufacturer></ManufacturerData></KNX>\n";
    }
}
