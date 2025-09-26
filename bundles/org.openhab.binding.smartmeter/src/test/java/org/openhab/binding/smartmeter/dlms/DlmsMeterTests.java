/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.smartmeter.dlms;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.smartmeter.dlms.internal.helper.DlmsChannelUtils;
import org.openhab.binding.smartmeter.dlms.internal.helper.DlmsQuantityType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescription;
import org.openmuc.jdlms.ObisCode.Medium;

/**
 * Unit tests for DLMS/COSEM meter code.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class DlmsMeterTests {

    @ParameterizedTest
    @MethodSource("testDlmsQuantityType")
    void testDlmsQuantityType(QuantityType<?> expected, String meterValue) {
        assertEquals(expected, new DlmsQuantityType<>(meterValue));
    }

    static Stream<Arguments> testDlmsQuantityType() {
        return Stream.of(
        //@formatter:off
            Arguments.of(QuantityType.valueOf("12345.678 kWh"), "1-0:1.8.0(12345.678*kWh)"), //
            Arguments.of(QuantityType.valueOf("230.0 V"), "1-0:32.7.0(230.0*V)"), //
            Arguments.of(QuantityType.valueOf("1.5 A"), "1-0:31.7.0(1.5*A)"), //
            Arguments.of(QuantityType.valueOf("0.345 kW"), "1-0:16.7.0(0.345*kW)")
        //@formatter:on
        );
    }

    @Test
    void testDlmsChannelTypeBuilder() {
        ChannelTypeUID channelTypeUID = DlmsChannelUtils.getChannelTypeUID(Medium.COLD_WATER,
                QuantityType.valueOf("1 V"));
        assertNotNull(channelTypeUID);
        assertEquals("smartmeter:cold_water-electricpotential-volt", channelTypeUID.toString());

        ChannelType channelType = DlmsChannelUtils.getChannelType(channelTypeUID, Medium.COLD_WATER,
                QuantityType.valueOf("1 V"));
        assertNotNull(channelType);
        assertEquals("water", channelType.getCategory());
        assertEquals("Number:ElectricPotential", channelType.getItemType());
        assertEquals(ChannelKind.STATE, channelType.getKind());
        assertEquals(channelTypeUID, channelType.getUID());
        assertEquals("Electric Potential [V]", channelType.getLabel());
        assertTrue(channelType.getTags().contains("Measurement"));
        assertTrue(channelType.getTags().contains("Water"));

        StateDescription stateDescription = channelType.getState();
        assertNotNull(stateDescription);
        assertTrue(stateDescription.isReadOnly());
        assertEquals("%.1f %unit%", stateDescription.getPattern());
    }
}
