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
package org.openhab.binding.solarmax.internal.connector;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

/**
 * The {@link SolarMaxConnectorTest} class tests request construction and response parsing of the
 * {@link SolarMaxConnector} with requests and responses recorded from a SolarMax 10MT2.
 *
 * @author Christian Schreiner - Initial contribution
 */
@NonNullByDefault
public class SolarMaxConnectorTest {

    // recorded from a SolarMax 10MT2 (device address 1)
    private static final String RESPONSE_MIXED = "{01;FB;8F|64:PAC=290;SYS=4E28,0;SAL=0;UD01=1011;UD02=F99;ID01=1E;"
            + "ID02=1B;PDC=1EC;PD01=108;PD02=E4;TNF=138C;UL1=900;TKK=1F;KDY=5;KT0=14ED8|216B}";

    private static final List<String> RECORDED_RESPONSES = List.of(RESPONSE_MIXED,
            "{01;FB;34|64:PAC=28C;SYS=4E28,0;UL1=90F;TKK=1F|0BA8}",
            "{01;FB;58|64:PAC=29A;UL1=918;UL2=902;UL3=8F4;IL1=56;IL2=56;IL3=55;SYS=4E28,0;SAL=0|1430}",
            "{01;FB;8F|64:BDN=3ABE;CAC=3FD9;IL1=61;IL2=63;IL3=5F;KDY=A;KHR=DF7A;KLD=37;KLM=11B;KLY=1823;KMT=48;"
                    + "KT0=14ED9;KYR=1782;PAC=40A;SWV=1;TKK=1F|22A6}",
            "{01;FB;82|64:TNF=1384;UL1=8FD;UL2=902;UL3=8FB;SYS=4E28,0;SAL=0;UD01=109E;UD02=10EF;ID01=3D;ID02=2C;"
                    + "PD01=202;PD02=17A;PDC=37C|1E13}");

    @Test
    public void recordedResponsesAreValid() {
        for (String response : RECORDED_RESPONSES) {
            assertTrue(SolarMaxConnector.validateResponse(response), response);
        }
    }

    @Test
    public void corruptResponsesAreInvalid() {
        // truncated, e.g. connection closed by the device while answering
        assertFalse(SolarMaxConnector.validateResponse(RESPONSE_MIXED.substring(0, 60)));
        assertFalse(SolarMaxConnector.validateResponse(""));
        // wrong checksum
        assertFalse(SolarMaxConnector.validateResponse(RESPONSE_MIXED.replace("|216B}", "|216C}")));
        // value changed in transit (checksum doesn't match any more)
        assertFalse(SolarMaxConnector.validateResponse(RESPONSE_MIXED.replace("PAC=290", "PAC=291")));
        // length field doesn't match
        assertFalse(SolarMaxConnector.validateResponse("{01;FB;35|64:PAC=28C;SYS=4E28,0;UL1=90F;TKK=1F|0BA9}"));
        // only one pipe
        assertFalse(SolarMaxConnector.validateResponse("{01;FB;0E|64:}"));
    }

    @Test
    public void responseWithSinglePipeDoesNotThrow() {
        assertTrue(SolarMaxConnector.extractValuesFromResponse("{01;FB;0E|64:PAC=290}").isEmpty());
    }

    @Test
    public void requestMatchesRecordedRequest() {
        List<SolarMaxCommandKey> commands = List.of(SolarMaxCommandKey.currentPowerGenerated,
                SolarMaxCommandKey.operatingState, SolarMaxCommandKey.acPhase1Voltage,
                SolarMaxCommandKey.heatSinkTemperature);

        String request = SolarMaxConnector.contructRequest(1, SolarMaxConnector.getCommandString(commands));

        assertEquals("{FB;01;22|64:PAC;SYS;UL1;TKK|079F}", request);
    }

    @Test
    public void requestWithFourCharacterCommandsMatchesRecordedRequest() {
        List<SolarMaxCommandKey> commands = List.of(SolarMaxCommandKey.currentPowerGenerated,
                SolarMaxCommandKey.acPhase1Voltage, SolarMaxCommandKey.acPhase2Voltage,
                SolarMaxCommandKey.acPhase3Voltage, SolarMaxCommandKey.acPhase1Current,
                SolarMaxCommandKey.acPhase2Current, SolarMaxCommandKey.acPhase3Current,
                SolarMaxCommandKey.operatingState, SolarMaxCommandKey.alarmCode);

        String request = SolarMaxConnector.contructRequest(1, SolarMaxConnector.getCommandString(commands));

        assertEquals("{FB;01;36|64:PAC;UL1;UL2;UL3;IL1;IL2;IL3;SYS;SAL|0CBD}", request);
    }

    @Test
    public void extractsThreeAndFourCharacterCommands() {
        Map<SolarMaxCommandKey, @Nullable String> values = SolarMaxConnector.extractValuesFromResponse(RESPONSE_MIXED);

        assertEquals("290", values.get(SolarMaxCommandKey.currentPowerGenerated));
        assertEquals("4E28,0", values.get(SolarMaxCommandKey.operatingState));
        assertEquals("0", values.get(SolarMaxCommandKey.alarmCode));
        assertEquals("1011", values.get(SolarMaxCommandKey.dcInput1Voltage));
        assertEquals("F99", values.get(SolarMaxCommandKey.dcInput2Voltage));
        assertEquals("1E", values.get(SolarMaxCommandKey.dcInput1Current));
        assertEquals("1B", values.get(SolarMaxCommandKey.dcInput2Current));
        assertEquals("1EC", values.get(SolarMaxCommandKey.dcPower));
        assertEquals("108", values.get(SolarMaxCommandKey.dcInput1Power));
        assertEquals("E4", values.get(SolarMaxCommandKey.dcInput2Power));
        assertEquals("14ED8", values.get(SolarMaxCommandKey.energyGeneratedTotal));
        assertFalse(values.containsKey(SolarMaxCommandKey.UNKNOWN));
    }

    @Test
    public void commandWithoutValueIsNotApplicable() {
        // MaxComm: a known command that is currently not applicable is answered with the bare command
        Map<SolarMaxCommandKey, @Nullable String> values = SolarMaxConnector
                .extractValuesFromResponse("{01;FB;1A|64:PAC=290;SAL|0000}");

        assertEquals("290", values.get(SolarMaxCommandKey.currentPowerGenerated));
        assertTrue(values.containsKey(SolarMaxCommandKey.alarmCode));
        assertNull(values.get(SolarMaxCommandKey.alarmCode));
    }

    @Test
    public void unknownCommandsAreIgnored() {
        Map<SolarMaxCommandKey, @Nullable String> values = SolarMaxConnector
                .extractValuesFromResponse("{01;FB;1A|64:XYZ=1;UD03=10;PAC=290|0000}");

        assertEquals(1, values.size());
        assertEquals("290", values.get(SolarMaxCommandKey.currentPowerGenerated));
    }
}
