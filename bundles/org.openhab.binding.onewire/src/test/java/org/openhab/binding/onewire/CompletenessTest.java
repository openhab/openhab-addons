/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.onewire;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.onewire.internal.OwBindingConstants;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.binding.onewire.internal.handler.AdvancedMultisensorThingHandler;
import org.openhab.binding.onewire.internal.handler.BAE091xSensorThingHandler;
import org.openhab.binding.onewire.internal.handler.BasicMultisensorThingHandler;
import org.openhab.binding.onewire.internal.handler.BasicThingHandler;
import org.openhab.binding.onewire.internal.handler.EDSSensorThingHandler;

/**
 * Tests cases for binding completeness
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class CompletenessTest {
    // internal/temporary types, DS2409 (MicroLAN Coupler), DS2431 (EEPROM)
    private static final Set<OwSensorType> IGNORED_SENSOR_TYPES = Set.of(OwSensorType.DS2409, OwSensorType.DS2431,
            OwSensorType.EDS, OwSensorType.MS_TH_S, OwSensorType.BAE, OwSensorType.BAE0911, OwSensorType.UNKNOWN);

    private static final Set<OwSensorType> THINGHANDLER_SENSOR_TYPES = Collections.unmodifiableSet(Stream
            .of(AdvancedMultisensorThingHandler.SUPPORTED_SENSOR_TYPES,
                    BasicMultisensorThingHandler.SUPPORTED_SENSOR_TYPES, BasicThingHandler.SUPPORTED_SENSOR_TYPES,
                    EDSSensorThingHandler.SUPPORTED_SENSOR_TYPES, BAE091xSensorThingHandler.SUPPORTED_SENSOR_TYPES)
            .flatMap(Set::stream).collect(Collectors.toSet()));

    @Test
    public void allSupportedTypesInThingHandlerMap() {
        for (OwSensorType sensorType : EnumSet.allOf(OwSensorType.class)) {
            if (!OwBindingConstants.THING_TYPE_MAP.containsKey(sensorType)
                    && !IGNORED_SENSOR_TYPES.contains(sensorType)) {
                fail("missing thing type map for sensor type " + sensorType.name());
            }
        }
    }

    @Test
    public void allSupportedTypesInThingChannelsMap() {
        for (OwSensorType sensorType : EnumSet.allOf(OwSensorType.class)) {
            if (!OwBindingConstants.SENSOR_TYPE_CHANNEL_MAP.containsKey(sensorType)
                    && !IGNORED_SENSOR_TYPES.contains(sensorType)) {
                fail("missing channel configuration map for sensor type " + sensorType.name());
            }
        }
    }

    @Test
    public void allSensorsSupportedByThingHandlers() {
        for (OwSensorType sensorType : EnumSet.allOf(OwSensorType.class)) {
            if (!THINGHANDLER_SENSOR_TYPES.contains(sensorType) && !IGNORED_SENSOR_TYPES.contains(sensorType)) {
                fail("missing thing handler for sensor type " + sensorType.name());
            }
        }
    }

    @Test
    public void allSensorTypesInLabelMap() {
        for (OwSensorType sensorType : EnumSet.allOf(OwSensorType.class)) {
            if (!OwBindingConstants.THING_LABEL_MAP.containsKey(sensorType)
                    && !IGNORED_SENSOR_TYPES.contains(sensorType)) {
                fail("missing label for sensor type " + sensorType.name());
            }
        }
    }
}
