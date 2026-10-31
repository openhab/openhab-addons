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
package org.openhab.persistence.victoriametrics.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.core.items.MetadataRegistry;

/**
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VictoriaMetricsMetricNameResolverTest {

    private final VictoriaMetricsMetricNameResolver instance = createInstance("");

    private static VictoriaMetricsMetricNameResolver createInstance(String prefix) {
        return new VictoriaMetricsMetricNameResolver(
                new VictoriaMetricsConfiguration(Map.of(VictoriaMetricsConfiguration.METRIC_PREFIX_PARAM, prefix)),
                new VictoriaMetricsMetadataService(mock(MetadataRegistry.class)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "WashingMachine_Door", "WashingMachineDoor", "my_lamp", "MyLamp", "_item", "a1" })
    public void itemNamesAreUsedUnchanged(String itemName) {
        assertThat(instance.getMetricName(itemName), is(itemName));
    }

    @ParameterizedTest
    @CsvSource({ "outside.temp, outside_temp", "outside-temp, outside_temp", "1stFloor, _1stFloor",
            "'living room', living_room", "Küche, K_che", "ns:metric, ns:metric" })
    public void invalidNamesAreSanitized(String name, String expected) {
        assertThat(instance.getMetricName(name), is(expected));
        assertThat(VictoriaMetricsMetricNameResolver.isValidMetricName(instance.getMetricName(name)), is(true));
    }

    @Test
    public void prefixIsPrepended() {
        VictoriaMetricsMetricNameResolver prefixed = createInstance("openhab_");
        assertThat(prefixed.getMetricName("MyLamp"), is("openhab_MyLamp"));
        // a valid prefix makes a leading digit valid
        assertThat(prefixed.getMetricName("1stFloor"), is("openhab_1stFloor"));
    }

    @Test
    public void suffixIsAppended() {
        VictoriaMetricsMetricNameResolver suffixed = new VictoriaMetricsMetricNameResolver(
                new VictoriaMetricsConfiguration(Map.of(VictoriaMetricsConfiguration.METRIC_PREFIX_PARAM, "openhab_",
                        VictoriaMetricsConfiguration.METRIC_SUFFIX_PARAM, "_value")),
                new VictoriaMetricsMetadataService(mock(MetadataRegistry.class)));
        assertThat(suffixed.getMetricName("MyLamp"), is("openhab_MyLamp_value"));
        assertThat(suffixed.getMetricName("outside.temp"), is("openhab_outside_temp_value"));
    }

    @ParameterizedTest
    @CsvSource({ "'', true", "_value, true", "1, true", "ns:x, true", ".value, false", "-x, false" })
    public void suffixValidation(String suffix, boolean valid) {
        assertThat(VictoriaMetricsMetricNameResolver.isValidMetricNameSuffix(suffix), is(valid));
    }

    @Test
    public void emptyNameIsSanitized() {
        assertThat(VictoriaMetricsMetricNameResolver.sanitize(""), is("_"));
    }

    @ParameterizedTest
    @CsvSource({ "room, room", "room-name, room_name", "1st, _1st", "ns:key, ns_key" })
    public void labelNamesAreSanitized(String name, String expected) {
        assertThat(VictoriaMetricsMetricNameResolver.sanitizeLabelName(name), is(expected));
    }
}
