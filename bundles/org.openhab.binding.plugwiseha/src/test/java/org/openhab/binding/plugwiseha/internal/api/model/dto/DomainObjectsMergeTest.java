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

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.plugwiseha.internal.api.xml.PlugwiseHAXStream;

/**
 * Tests measurement updates whose timestamps differ from their parent objects.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class DomainObjectsMergeTest {

    private static final String OLDER = "2026-01-01T00:00:00Z";
    private static final String CURRENT = "2026-01-01T01:00:00Z";
    private static final String NEWER = "2026-01-01T02:00:00Z";

    @ParameterizedTest
    @ValueSource(strings = { OLDER, CURRENT })
    void applianceMeasurementsUpdateWithoutNewerParentMetadata(String parentDate) {
        Appliances appliances = new Appliances();
        appliances.put("appliance", appliance(CURRENT, applianceMeasurements(CURRENT, 20, 10, false)));

        appliances.merge(Map.of("appliance", appliance(parentDate, applianceMeasurements(NEWER, 25, 11, true))));

        Appliance merged = requireNonNull(appliances.get("appliance"));
        assertEquals("Appliance " + CURRENT, merged.getName());
        assertApplianceMeasurements(merged, 25, 11, true);
    }

    @Test
    void newerApplianceMetadataPreservesMoreRecentMeasurements() {
        Appliances appliances = new Appliances();
        appliances.put("appliance", appliance(CURRENT, applianceMeasurements(NEWER, 25, 11, true)));

        appliances.merge(Map.of("appliance", appliance(NEWER, applianceMeasurements(OLDER, 20, 10, false))));

        Appliance merged = requireNonNull(appliances.get("appliance"));
        assertEquals("Appliance " + NEWER, merged.getName());
        assertApplianceMeasurements(merged, 25, 11, true);
    }

    @Test
    void staleMeasurementsDoNotRollBackCachedValues() {
        Appliances appliances = new Appliances();
        appliances.put("appliance", appliance(CURRENT, applianceMeasurements(NEWER, 25, 11, true)));
        Locations locations = new Locations();
        locations.put("location", location(CURRENT, locationMeasurements(NEWER, 25, true)));

        appliances.merge(Map.of("appliance", appliance(CURRENT, applianceMeasurements(OLDER, 20, 10, false))));
        locations.merge(Map.of("location", location(CURRENT, locationMeasurements(OLDER, 20, false))));

        assertApplianceMeasurements(requireNonNull(appliances.get("appliance")), 25, 11, true);
        assertLocationMeasurements(requireNonNull(locations.get("location")), 25, true);
    }

    @Test
    void partialApplianceUpdateRetainsOmittedMeasurements() {
        Appliances appliances = new Appliances();
        appliances.put("appliance", appliance(CURRENT, applianceMeasurements(CURRENT, 20, 10, true)));
        String temperatureOnly = """
                <point_log>
                    <type>temperature</type><unit>C</unit><measurement>25</measurement>
                    <measurement_date>%s</measurement_date>
                </point_log>
                """.formatted(NEWER);

        appliances.merge(Map.of("appliance", appliance(CURRENT, temperatureOnly)));

        assertApplianceMeasurements(requireNonNull(appliances.get("appliance")), 25, 10, true);
    }

    @ParameterizedTest
    @ValueSource(strings = { OLDER, CURRENT })
    void locationMeasurementsUpdateWithoutNewerParentMetadata(String parentDate) {
        Locations locations = new Locations();
        locations.put("location", location(CURRENT, locationMeasurements(CURRENT, 20, false)));

        locations.merge(Map.of("location", location(parentDate, locationMeasurements(NEWER, 25, true))));

        Location merged = requireNonNull(locations.get("location"));
        assertEquals("Location " + CURRENT, merged.getName());
        assertLocationMeasurements(merged, 25, true);
    }

    @Test
    void newerLocationMetadataPreservesMoreRecentMeasurements() {
        Locations locations = new Locations();
        locations.put("location", location(CURRENT, locationMeasurements(NEWER, 25, true)));

        locations.merge(Map.of("location", location(NEWER, locationMeasurements(OLDER, 20, false))));

        Location merged = requireNonNull(locations.get("location"));
        assertEquals("Location " + NEWER, merged.getName());
        assertLocationMeasurements(merged, 25, true);
    }

    @ParameterizedTest
    @ValueSource(strings = { "updated_date", "modified_date" })
    void olderActuatorStateDoesNotOverwriteNewerState(String timestampElement) {
        String original = """
                <actuator_functionality>
                    <type>relay</type><lock>true</lock><%1$s>%2$s</%1$s>
                </actuator_functionality>
                """.formatted(timestampElement, NEWER);
        String stale = """
                <actuator_functionality>
                    <type>relay</type><lock>false</lock><%1$s>%2$s</%1$s>
                </actuator_functionality>
                """.formatted(timestampElement, OLDER);
        ActuatorFunctionalities functionalities = appliance(CURRENT, original).getActuatorFunctionalities();

        functionalities.merge(appliance(CURRENT, stale).getActuatorFunctionalities());

        assertTrue(functionalities.getRelayLockState().orElseThrow());
    }

    private Appliance appliance(String parentDate, String measurements) {
        return (Appliance) new PlugwiseHAXStream().fromXML("""
                <appliance id="appliance">
                    <name>Appliance %1$s</name><modified_date>%1$s</modified_date>
                    %2$s
                </appliance>
                """.formatted(parentDate, measurements));
    }

    private Location location(String parentDate, String measurements) {
        return (Location) new PlugwiseHAXStream().fromXML("""
                <location id="location">
                    <name>Location %1$s</name><modified_date>%1$s</modified_date>
                    %2$s
                </location>
                """.formatted(parentDate, measurements));
    }

    private String applianceMeasurements(String date, double temperature, int burnerStarts, boolean locked) {
        return """
                <point_log>
                    <type>temperature</type><unit>C</unit><measurement>%2$s</measurement>
                    <measurement_date>%1$s</measurement_date>
                </point_log>
                <cumulative_log>
                    <type>burner_starts</type><measurement>%3$s</measurement><updated_date>%1$s</updated_date>
                </cumulative_log>
                <actuator_functionality>
                    <type>relay</type><lock>%4$s</lock><updated_date>%1$s</updated_date>
                </actuator_functionality>
                """.formatted(date, temperature, burnerStarts, locked);
    }

    private String locationMeasurements(String date, double temperature, boolean enabled) {
        return """
                <point_log>
                    <type>temperature</type><unit>C</unit><measurement>%2$s</measurement>
                    <measurement_date>%1$s</measurement_date>
                </point_log>
                <actuator_functionality>
                    <type>thermostat</type><updated_date>%1$s</updated_date>
                    <preheating_allowed>%3$s</preheating_allowed><cooling_allowed>%3$s</cooling_allowed>
                    <regulation_control>%4$s</regulation_control>
                </actuator_functionality>
                """.formatted(date, temperature, enabled, enabled ? "active" : "off");
    }

    private void assertApplianceMeasurements(Appliance appliance, double temperature, int burnerStarts,
            boolean locked) {
        assertAll(() -> assertEquals(temperature, appliance.getTemperature().orElseThrow()),
                () -> assertEquals((double) burnerStarts, appliance.getBurnerStarts().orElseThrow()),
                () -> assertEquals(locked, appliance.getRelayLockState().orElseThrow()));
    }

    private void assertLocationMeasurements(Location location, double temperature, boolean enabled) {
        assertAll(() -> assertEquals(temperature, location.getTemperature().orElseThrow()),
                () -> assertEquals(enabled, location.getPreHeatState().orElseThrow()),
                () -> assertEquals(enabled, location.getCoolingAllowed().orElseThrow()),
                () -> assertEquals(enabled ? "active" : "off", location.getRegulationControl()));
    }
}
