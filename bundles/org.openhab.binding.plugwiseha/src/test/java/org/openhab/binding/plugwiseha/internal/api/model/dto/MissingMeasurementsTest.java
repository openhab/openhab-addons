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
package org.openhab.binding.plugwiseha.internal.api.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.plugwiseha.internal.api.xml.PlugwiseHAXStream;

/**
 * Tests measurement availability when optional collections are absent from XML responses.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class MissingMeasurementsTest {

    @Test
    void applianceWithoutMeasurementCollectionsReturnsEmptyValues() {
        Appliance appliance = (Appliance) new PlugwiseHAXStream().fromXML("<appliance/>");

        assertAll(() -> assertTrue(appliance.getTemperature().isEmpty()),
                () -> assertTrue(appliance.getTemperatureUnit().isEmpty()),
                () -> assertTrue(appliance.getBurnerStarts().isEmpty()),
                () -> assertTrue(appliance.getRelayLockState().isEmpty()));
    }

    @Test
    void locationWithoutMeasurementCollectionsReturnsEmptyValues() {
        Location location = (Location) new PlugwiseHAXStream().fromXML("<location/>");

        assertAll(() -> assertTrue(location.getTemperature().isEmpty()),
                () -> assertTrue(location.getTemperatureUnit().isEmpty()),
                () -> assertTrue(location.getSetpointTemperature().isEmpty()),
                () -> assertTrue(location.getPreHeatState().isEmpty()),
                () -> assertTrue(location.getCoolingAllowed().isEmpty()),
                () -> assertNull(location.getRegulationControl()));
    }

    @Test
    void availableApplianceMeasurementsArePreserved() {
        Appliance appliance = (Appliance) new PlugwiseHAXStream().fromXML("""
                <appliance>
                    <point_log>
                        <type>temperature</type>
                        <unit>C</unit>
                        <measurement>21.5</measurement>
                    </point_log>
                    <cumulative_log>
                        <type>burner_starts</type>
                        <measurement>42</measurement>
                    </cumulative_log>
                    <actuator_functionality>
                        <type>relay</type>
                        <lock>true</lock>
                    </actuator_functionality>
                </appliance>
                """);

        assertAll(() -> assertEquals(21.5, appliance.getTemperature().orElseThrow()),
                () -> assertEquals("C", appliance.getTemperatureUnit().orElseThrow()),
                () -> assertEquals(42.0, appliance.getBurnerStarts().orElseThrow()),
                () -> assertEquals(true, appliance.getRelayLockState().orElseThrow()));
    }
}
