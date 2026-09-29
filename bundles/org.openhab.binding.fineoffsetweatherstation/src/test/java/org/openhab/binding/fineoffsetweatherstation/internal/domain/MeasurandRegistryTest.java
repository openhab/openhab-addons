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
package org.openhab.binding.fineoffsetweatherstation.internal.domain;

import java.util.Objects;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;

/**
 * Tests for {@link MeasurandRegistry}.
 *
 * @author Andreas Berger - Initial contribution
 */
class MeasurandRegistryTest {
    private final MeasurandRegistry registry = MeasurandRegistry.standard();

    @Test
    void tcpSingleCodeResolves() {
        Assertions.assertThat(registry.tcpByCode((byte) 0x02)).isNotNull();
    }

    @Test
    void tcpMultiChannelAssignsChannelByIndex() {
        // 0x2C is soil moisture channel 1, 0x2E is channel 2
        Assertions.assertThat(registry.tcpByCode((byte) 0x2C).getDebugString()).contains("channel 1");
        Assertions.assertThat(registry.tcpByCode((byte) 0x2E).getDebugString()).contains("channel 2");
    }

    @Test
    void httpKeyedResolves() {
        Assertions.assertThat(registry.http(HttpGroup.CH_SOIL, "humidity")).isNotNull();
    }

    @Test
    void httpAlternateSharesCodeWithPrimary() {
        // 0x15 is illumination (lux) with solar-radiation (W/m²) as the dimension alternate
        HttpBinding binding = Objects.requireNonNull(registry.http(HttpGroup.COMMON_LIST, "0x15"));
        // a lux reading resolves to the primary illumination channel...
        MeasuredValue lux = Objects.requireNonNull(binding.parse("12.3 Klux", null, null, null));
        Assertions.assertThat(lux.getChannelId()).isEqualTo("illumination");
        // ...while a W/m² reading the primary can't represent falls through to the solar-radiation alternate
        MeasuredValue solar = Objects.requireNonNull(binding.parse("365.66 W/m2", null, null, null));
        Assertions.assertThat(solar.getChannelId()).isEqualTo("irradiation-solar");
    }

    @Test
    void normalizeIdKeepsHexAndDecimalDistinct() {
        Assertions.assertThat(MeasurandRegistry.normalizeId("0x03")).isNotEqualTo(MeasurandRegistry.normalizeId("3"));
    }

    @Test
    void unknownCodeReturnsNull() {
        Assertions.assertThat(registry.tcpByCode((byte) 0xEE)).isNull();
    }
}
