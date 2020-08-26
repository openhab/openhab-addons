/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.elkm1.internal.elk;

/**
 * The elk command to the codes used.
 *
 * @author David Bennett - Initial Contribution
 */
public enum ElkCommand {
    Disarm("a0"),
    ArmAway("a1"),
    ArmToStayHome("a2"),
    ArmToStayInstant("a3"),
    ArmToNight("a4"),
    ArmToNightInstant("a5"),
    ArmToVacation("a6"),
    ArmToStepToNextAwayMode("a7"),
    ArmToStepToNextStayMode("a8"),
    ForceArmToAwayMode("a9"),
    ForceArmToStayMode("a:"),
    ArmingStatusRequest("as"),
    ArmingStatusRequestReply("AS"),
    AlarmReporting("AR"),
    AlarmReportAcknowledge("ar"),
    AlarmReportingTest("AT"),
    AlarmReportTestAcknowledge("at"),
    AlarmZoneRequest("az"),
    AlarmZoneRequestReply("AZ"),
    EntryExitData("EE"),
    ValidOrInvalidUserCode("IC"),
    OutputStatusReply("CS"),
    ControlOutputOn("cn"),
    ControlOutputOff("cf"),
    ZoneStatusRequest("zs"),
    ZoneStatusReply("ZS"),
    ZonePartition("zp"),
    ZonePartitionReply("ZP"),
    BypassedZoneState("ZB"),
    ZoneChangeUpdateReport("ZC"),
    ZoneDefintionRequest("zd"),
    ZoneDefinitionReply("ZD"),
    SystemLogUpdate("LD"),
    OutputChangeUpdate("CC"),
    TaskChangeUpdate("TC"),
    StringTextDescription("sd"),
    StringTextDescriptionReply("SD"),
    SpeakPhraseAtVoiceOutput("sp"),
    TemperatureReply("ST"),
    SpeakWordAtVoiceOutput("sw"),
    CustomValueReply("CR"),
    KeypadKeyChangeUpdate("KC"),
    ThermostatDataReply("TR"),
    PLCChangeUpdate("PC"),
    ZoneAnalogVolatageReply("ZV"),
    EthernetModuleTest("XK"),
    EthernetModuleTestAcknowledge("xk"),
    RequestVersionNumber("vn"),
    RequestVersionNumberReply("VN");

    private final String myValue;

    private ElkCommand(String value) {
        this.myValue = value;
    }

    /**
     * The string value of the command.
     */
    public String getValue() {
        return myValue;
    }

    /**
     * Get an elk command from the string value.
     */
    public static ElkCommand fromValue(String val) {
        for (ElkCommand cmd : ElkCommand.values()) {
            if (cmd.getValue().equals(val)) {
                return cmd;
            }
        }
        return null;
    }

    public String toSendString() {
        return null;
    }
}
