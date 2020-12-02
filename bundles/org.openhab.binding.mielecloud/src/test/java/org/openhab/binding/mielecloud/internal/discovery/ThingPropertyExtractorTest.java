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
package org.openhab.binding.mielecloud.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceType;
import org.openhab.core.thing.Thing;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class ThingPropertyExtractorTest {
    private static Stream<Arguments> extractedPropertiesContainSerialNumberAndModelIdParameterSource() {
        return Stream.of(Arguments.of(DeviceType.HOOD, "000124430018", Optional.of("000124430017"),
                Optional.of("Ventilation Hood"), Optional.of("DA-6996"), "000124430017", "Ventilation Hood DA-6996"),
                Arguments.of(DeviceType.COFFEE_SYSTEM, "000124431235", Optional.of("000124431234"),
                        Optional.of("Coffee Machine"), Optional.of("CM-1234"), "000124431234",
                        "Coffee Machine CM-1234"),
                Arguments.of(DeviceType.HOOD, "000124430018", Optional.empty(), Optional.of("Ventilation Hood"),
                        Optional.of("DA-6996"), "000124430018", "Ventilation Hood DA-6996"),
                Arguments.of(DeviceType.HOOD, "000124430018", Optional.empty(), Optional.empty(),
                        Optional.of("DA-6996"), "000124430018", "DA-6996"),
                Arguments.of(DeviceType.HOOD, "000124430018", Optional.empty(), Optional.of("Ventilation Hood"),
                        Optional.empty(), "000124430018", "Ventilation Hood"),
                Arguments.of(DeviceType.HOOD, "000124430018", Optional.empty(), Optional.empty(), Optional.empty(),
                        "000124430018", "Unknown"));
    }

    @ParameterizedTest
    @MethodSource("extractedPropertiesContainSerialNumberAndModelIdParameterSource")
    void extractedPropertiesContainSerialNumberAndModelId(DeviceType deviceType, String deviceIdentifier,
            Optional<String> fabNumber, Optional<String> type, Optional<String> techType, String expectedSerialNumber,
            String expectedModelId) {
        // given:
        var deviceState = mock(DeviceState.class);
        when(deviceState.getRawType()).thenReturn(deviceType);
        when(deviceState.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(deviceState.getFabNumber()).thenReturn(fabNumber);
        when(deviceState.getType()).thenReturn(type);
        when(deviceState.getTechType()).thenReturn(techType);

        // when:
        var properties = ThingPropertyExtractor.extractProperties(deviceState);

        // then:
        assertEquals(2, properties.size());
        assertEquals(expectedSerialNumber, properties.get(Thing.PROPERTY_SERIAL_NUMBER));
        assertEquals(expectedModelId, properties.get(Thing.PROPERTY_MODEL_ID));
    }
}
