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
package org.openhab.binding.sensibo.internal.dto;

import static org.junit.Assert.*;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Map;

import org.junit.Test;
import org.openhab.binding.sensibo.internal.dto.poddetails.AcState;
import org.openhab.binding.sensibo.internal.dto.poddetails.Measurement;
import org.openhab.binding.sensibo.internal.dto.poddetails.ModeCapability;
import org.openhab.binding.sensibo.internal.dto.poddetails.PodDetails;
import org.openhab.binding.sensibo.internal.dto.poddetails.Temperature;
import org.openhab.binding.sensibo.internal.model.SensiboSky;

/**
 * @author Arne Seime - Initial contribution
 */
public class GetPodDetailsResponseTest extends AbstractSerializationDeserializationTest {

    @Test
    public void testDeserializeWithSmartModeSetup() throws IOException {
        final PodDetails rsp = wireHelper.deSerializeResponse("/get_pod_details_response_smartmode_settings.json",
                PodDetails.class);

        assertEquals("34:15:13:AA:AA:AA", rsp.macAddress);
    }

    @Test
    public void testDeserializeNullpointerExample() throws IOException {
        final PodDetails rsp = wireHelper.deSerializeResponse("/get_pod_details_response_nullpointer.json",
                PodDetails.class);
        SensiboSky internal = new SensiboSky(rsp);

        assertEquals("50175457", internal.getSerialNumber());
    }

    @Test
    public void testDeserialize() throws IOException {
        final PodDetails rsp = wireHelper.deSerializeResponse("/get_pod_details_response.json", PodDetails.class);

        assertEquals("MA:C:AD:DR:ES:S0", rsp.macAddress);
        assertEquals("IN010056", rsp.firmwareVersion);
        assertEquals("cc3100_stm32f0", rsp.firmwareType);
        assertEquals("SERIALNUMASSTRING", rsp.serialNumber);
        assertEquals("C", rsp.temperatureUnit);
        assertEquals("skyv2", rsp.productModel);
        assertAcState(rsp.acState);
        assertMeasurement(rsp.lastMeasurement);
        assertRemoteCapabilities(rsp.getRemoteCapabilities());

    }

    private void assertRemoteCapabilities(final Map<String, ModeCapability> remoteCapabilities) {
        assertNotNull(remoteCapabilities);

        assertEquals(5, remoteCapabilities.size());
        final ModeCapability mode = remoteCapabilities.get("heat");

        assertNotNull(mode.swingModes);
        assertNotNull(mode.fanLevels);
        assertNotNull(mode.temperatures);
        final Map<String, Temperature> temperatures = mode.temperatures;
        final Temperature temperature = temperatures.get("C");
        assertNotNull(temperature);
        assertNotNull(temperature.validValues);

    }

    private void assertMeasurement(final Measurement lastMeasurement) {
        assertNotNull(lastMeasurement);
        assertNull(lastMeasurement.batteryVoltage);
        assertEquals(Double.valueOf("22.5"), lastMeasurement.temperature);
        assertEquals(Double.valueOf("24.2"), lastMeasurement.humidity);
        assertEquals(Integer.valueOf("-71"), lastMeasurement.wifiSignalStrength);
        assertEquals(ZonedDateTime.parse("2019-05-05T07:52:11Z"), lastMeasurement.measurementTimestamp.time);

    }

    private void assertAcState(final AcState acState) {
        assertNotNull(acState);

        assertTrue(acState.on);
        assertEquals("medium_high", acState.fanLevel);
        assertEquals("C", acState.temperatureUnit);
        assertEquals(21, acState.targetTemperature.intValue());
        assertEquals("heat", acState.mode);
        assertEquals("rangeFull", acState.swing);

    }
}
