/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class ThingInformationExtractorTest {
    private static Stream<Arguments> extractedPropertiesContainSerialNumberAndModelIdParameterSource() {
        return Stream.of(
                Arguments.of(MieleCloudBindingConstants.THING_TYPE_HOOD, DeviceType.HOOD, "000124430018",
                        Optional.of("000124430017"), Optional.of("Ventilation Hood"), Optional.of("DA-6996"),
                        "000124430017", "Ventilation Hood DA-6996", "000124430018"),
                Arguments.of(MieleCloudBindingConstants.THING_TYPE_COFFEE_SYSTEM, DeviceType.COFFEE_SYSTEM,
                        "000124431235", Optional.of("000124431234"), Optional.of("Coffee Machine"),
                        Optional.of("CM-1234"), "000124431234", "Coffee Machine CM-1234", "000124431235"),
                Arguments.of(MieleCloudBindingConstants.THING_TYPE_HOOD, DeviceType.HOOD, "000124430018",
                        Optional.empty(), Optional.of("Ventilation Hood"), Optional.of("DA-6996"), "000124430018",
                        "Ventilation Hood DA-6996", "000124430018"),
                Arguments.of(MieleCloudBindingConstants.THING_TYPE_HOOD, DeviceType.HOOD, "000124430018",
                        Optional.empty(), Optional.empty(), Optional.of("DA-6996"), "000124430018", "DA-6996",
                        "000124430018"),
                Arguments.of(MieleCloudBindingConstants.THING_TYPE_HOOD, DeviceType.HOOD, "000124430018",
                        Optional.empty(), Optional.of("Ventilation Hood"), Optional.empty(), "000124430018",
                        "Ventilation Hood", "000124430018"),
                Arguments.of(MieleCloudBindingConstants.THING_TYPE_HOOD, DeviceType.HOOD, "000124430018",
                        Optional.empty(), Optional.empty(), Optional.empty(), "000124430018", "Unknown",
                        "000124430018"));
    }

    @ParameterizedTest
    @MethodSource("extractedPropertiesContainSerialNumberAndModelIdParameterSource")
    void extractedPropertiesContainSerialNumberAndModelId(ThingTypeUID thingTypeUid, DeviceType deviceType,
            String deviceIdentifier, Optional<String> fabNumber, Optional<String> type, Optional<String> techType,
            String expectedSerialNumber, String expectedModelId, String expectedDeviceIdentifier) {
        // given:
        var deviceState = mock(DeviceState.class);
        when(deviceState.getRawType()).thenReturn(deviceType);
        when(deviceState.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(deviceState.getFabNumber()).thenReturn(fabNumber);
        when(deviceState.getType()).thenReturn(type);
        when(deviceState.getTechType()).thenReturn(techType);

        // when:
        var properties = ThingInformationExtractor.extractProperties(thingTypeUid, deviceState);

        // then:
        assertEquals(3, properties.size());
        assertEquals(expectedSerialNumber, properties.get(Thing.PROPERTY_SERIAL_NUMBER));
        assertEquals(expectedModelId, properties.get(Thing.PROPERTY_MODEL_ID));
        assertEquals(expectedDeviceIdentifier,
                properties.get(MieleCloudBindingConstants.CONFIG_PARAM_DEVICE_IDENTIFIER));
    }

    @ParameterizedTest
    @CsvSource({ "2,2", "4,4" })
    void propertiesForHobContainPlateCount(int plateCount, String expectedPlateCountPropertyValue) {
        // given:
        var deviceState = mock(DeviceState.class);
        when(deviceState.getRawType()).thenReturn(DeviceType.HOB_INDUCTION);
        when(deviceState.getDeviceIdentifier()).thenReturn("000124430019");
        when(deviceState.getFabNumber()).thenReturn(Optional.of("000124430019"));
        when(deviceState.getType()).thenReturn(Optional.of("Induction Hob"));
        when(deviceState.getTechType()).thenReturn(Optional.of("IH-7890"));
        when(deviceState.getPlateStepCount()).thenReturn(Optional.of(plateCount));

        // when:
        var properties = ThingInformationExtractor.extractProperties(MieleCloudBindingConstants.THING_TYPE_HOB,
                deviceState);

        // then:
        assertEquals(4, properties.size());
        assertEquals("000124430019", properties.get(Thing.PROPERTY_SERIAL_NUMBER));
        assertEquals("Induction Hob IH-7890", properties.get(Thing.PROPERTY_MODEL_ID));
        assertEquals("000124430019", properties.get(MieleCloudBindingConstants.CONFIG_PARAM_DEVICE_IDENTIFIER));
        assertEquals(expectedPlateCountPropertyValue, properties.get(MieleCloudBindingConstants.PROPERTY_PLATE_COUNT));
    }
}
