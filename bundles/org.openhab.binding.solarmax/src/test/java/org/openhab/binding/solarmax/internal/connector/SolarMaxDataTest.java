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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DateTimeType;

/**
 * The {@link SolarMaxDataTest} class is used to test the {@link SolaMaxData} class.
 *
 * @author Jamie Townsend - Initial contribution
 */
@NonNullByDefault
public class SolarMaxDataTest {

    @Test
    public void dataDateTimeGetterSetterTest() throws Exception {
        // dataDateTime shouldn't be a problem, but check it anyway
        ZonedDateTime dateTimeOriginal = ZonedDateTime.now();
        ZonedDateTime dateTimeUpdated = dateTimeOriginal.plusDays(2);

        SolarMaxData solarMaxData = new SolarMaxData();

        solarMaxData.setDataDateTime(dateTimeOriginal);
        assertEquals(new DateTimeType(dateTimeOriginal), solarMaxData.getDataDateTime());

        solarMaxData.setDataDateTime(dateTimeUpdated);
        assertEquals(new DateTimeType(dateTimeUpdated), solarMaxData.getDataDateTime());
    }

    @Test
    public void valueGetterSetterTest() throws Exception {
        String startupsOriginal = "3B8B"; // 15243 in hex
        String startupsUpdated = "3B8C"; // 15244 in hex

        SolarMaxData solarMaxData = new SolarMaxData();

        Map<SolarMaxCommandKey, @Nullable String> dataOrig = new HashMap<>();
        dataOrig.put(SolarMaxCommandKey.startups, startupsOriginal);
        solarMaxData.setData(dataOrig);

        @Nullable
        Number origVersion = solarMaxData.get(SolarMaxCommandKey.startups);

        assertNotNull(origVersion);
        assertEquals(Integer.parseInt(startupsOriginal, 16), origVersion.intValue());

        Map<SolarMaxCommandKey, @Nullable String> dataUpdated = new HashMap<>();
        dataUpdated.put(SolarMaxCommandKey.startups, startupsUpdated);
        solarMaxData.setData(dataUpdated);
        Number updatedVersion = solarMaxData.get(SolarMaxCommandKey.startups);
        assertNotEquals(origVersion, updatedVersion);
    }

    @Test
    public void statusAndDcValuesFromRecordedResponse() {
        // values recorded from a SolarMax 10MT2:
        // {01;FB;8F|64:PAC=290;SYS=4E28,0;SAL=0;UD01=1011;UD02=F99;ID01=1E;ID02=1B;PDC=1EC;PD01=108;PD02=E4;...}
        Map<SolarMaxCommandKey, @Nullable String> data = new HashMap<>();
        data.put(SolarMaxCommandKey.operatingState, "4E28,0");
        data.put(SolarMaxCommandKey.alarmCode, "0");
        data.put(SolarMaxCommandKey.dcInput1Voltage, "1011");
        data.put(SolarMaxCommandKey.dcInput2Voltage, "F99");
        data.put(SolarMaxCommandKey.dcInput1Current, "1E");
        data.put(SolarMaxCommandKey.dcInput2Current, "1B");
        data.put(SolarMaxCommandKey.dcInput1Power, "108");
        data.put(SolarMaxCommandKey.dcInput2Power, "E4");
        data.put(SolarMaxCommandKey.dcPower, "1EC");

        SolarMaxData solarMaxData = new SolarMaxData();
        solarMaxData.setData(data);

        assertEquals(20008, solarMaxData.get(SolarMaxCommandKey.operatingState));
        assertEquals(0, solarMaxData.get(SolarMaxCommandKey.alarmCode));
        // decimal values have to be exact, i.e. without binary floating point artifacts
        assertEquals("411.3", solarMaxData.get(SolarMaxCommandKey.dcInput1Voltage).toString());
        assertEquals("399.3", solarMaxData.get(SolarMaxCommandKey.dcInput2Voltage).toString());
        assertEquals("0.3", solarMaxData.get(SolarMaxCommandKey.dcInput1Current).toString());
        assertEquals("0.27", solarMaxData.get(SolarMaxCommandKey.dcInput2Current).toString());
        assertEquals("132", solarMaxData.get(SolarMaxCommandKey.dcInput1Power).toString());
        assertEquals("114", solarMaxData.get(SolarMaxCommandKey.dcInput2Power).toString());
        assertEquals("246", solarMaxData.get(SolarMaxCommandKey.dcPower).toString());
    }

    @Test
    public void acValuesAreExact() {
        // 0x902 * 0.1 used to become 230.60000000000002 V, 0x5F * 0.01 became 0.9500000000000001 A
        Map<SolarMaxCommandKey, @Nullable String> data = new HashMap<>();
        data.put(SolarMaxCommandKey.acPhase1Voltage, "902");
        data.put(SolarMaxCommandKey.acPhase1Current, "5F");
        data.put(SolarMaxCommandKey.acFrequency, "1384");
        data.put(SolarMaxCommandKey.currentPowerGenerated, "40B");

        SolarMaxData solarMaxData = new SolarMaxData();
        solarMaxData.setData(data);

        assertEquals("230.6", solarMaxData.get(SolarMaxCommandKey.acPhase1Voltage).toString());
        assertEquals("0.95", solarMaxData.get(SolarMaxCommandKey.acPhase1Current).toString());
        assertEquals("49.96", solarMaxData.get(SolarMaxCommandKey.acFrequency).toString());
        assertEquals("517.5", solarMaxData.get(SolarMaxCommandKey.currentPowerGenerated).toString());
    }

    @Test
    public void operatingStateWithoutAdditionalValue() {
        Map<SolarMaxCommandKey, @Nullable String> data = new HashMap<>();
        data.put(SolarMaxCommandKey.operatingState, "4E93"); // 20115 = no mains

        SolarMaxData solarMaxData = new SolarMaxData();
        solarMaxData.setData(data);

        assertEquals(20115, solarMaxData.get(SolarMaxCommandKey.operatingState));
    }

    @Test
    public void missingOrInvalidValuesReturnNull() {
        Map<SolarMaxCommandKey, @Nullable String> data = new HashMap<>();
        data.put(SolarMaxCommandKey.alarmCode, null); // not applicable, the device returned the bare command
        data.put(SolarMaxCommandKey.dcInput1Voltage, "XYZ");
        data.put(SolarMaxCommandKey.operatingState, ",0");

        SolarMaxData solarMaxData = new SolarMaxData();
        solarMaxData.setData(data);

        assertNull(solarMaxData.get(SolarMaxCommandKey.alarmCode));
        assertNull(solarMaxData.get(SolarMaxCommandKey.dcInput1Voltage));
        assertNull(solarMaxData.get(SolarMaxCommandKey.operatingState));
        assertNull(solarMaxData.get(SolarMaxCommandKey.dcPower));
    }
}
