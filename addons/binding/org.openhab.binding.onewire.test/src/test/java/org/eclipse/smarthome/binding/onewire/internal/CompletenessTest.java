/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.onewire.internal;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.binding.onewire.internal.device.OwSensorType;
import org.eclipse.smarthome.binding.onewire.internal.handler.AdvancedMultisensorThingHandler;
import org.eclipse.smarthome.binding.onewire.internal.handler.BasicMultisensorThingHandler;
import org.eclipse.smarthome.binding.onewire.internal.handler.CounterSensorThingHandler;
import org.eclipse.smarthome.binding.onewire.internal.handler.DigitalIOThingHandler;
import org.eclipse.smarthome.binding.onewire.internal.handler.EDSSensorThingHandler;
import org.eclipse.smarthome.binding.onewire.internal.handler.IButtonThingHandler;
import org.eclipse.smarthome.binding.onewire.internal.handler.TemperatureSensorThingHandler;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests cases for binding completeness
 *
 * @author Jan N. Klug - Initial contribution
 */
public class CompletenessTest {
    // internal/temporary types, DS2409 (MicroLAN Coupler), DS2431 (EEPROM)
    private static final Set<OwSensorType> IGNORED_SENSOR_TYPES = Collections.unmodifiableSet(Stream
            .of(OwSensorType.DS2409, OwSensorType.DS2431, OwSensorType.EDS, OwSensorType.MS_TH_S, OwSensorType.UNKNOWN)
            .collect(Collectors.toSet()));

    private static final Set<OwSensorType> THINGHANDLER_SENSOR_TYPES = Collections.unmodifiableSet(Stream
            .of(AdvancedMultisensorThingHandler.SUPPORTED_SENSOR_TYPES,
                    BasicMultisensorThingHandler.SUPPORTED_SENSOR_TYPES,
                    CounterSensorThingHandler.SUPPORTED_SENSOR_TYPES, DigitalIOThingHandler.SUPPORTED_SENSOR_TYPES,
                    EDSSensorThingHandler.SUPPORTED_SENSOR_TYPES, IButtonThingHandler.SUPPORTED_SENSOR_TYPES,
                    TemperatureSensorThingHandler.SUPPORTED_SENSOR_TYPES)
            .flatMap(Set::stream).collect(Collectors.toSet()));

    private static final Set<ThingTypeUID> DEPRECATED_THING_TYPES = Collections.unmodifiableSet(Stream
            .of(OwBindingConstants.THING_TYPE_MS_TH, OwBindingConstants.THING_TYPE_MS_TV).collect(Collectors.toSet()));

    @Test
    public void allSupportedTypesInThingHandlerMap() {
        for (OwSensorType sensorType : EnumSet.allOf(OwSensorType.class)) {
            if (!OwBindingConstants.THING_TYPE_MAP.containsKey(sensorType)
                    && !IGNORED_SENSOR_TYPES.contains(sensorType)) {
                Assert.fail("missing thing type map for sensor type " + sensorType.name());
            }
        }
    }

    @Test
    public void allSensorsSupportedByThingHandlers() {
        for (OwSensorType sensorType : EnumSet.allOf(OwSensorType.class)) {
            if (!THINGHANDLER_SENSOR_TYPES.contains(sensorType) && !IGNORED_SENSOR_TYPES.contains(sensorType)) {
                Assert.fail("missing thing handler for sensor type " + sensorType.name());
            }
        }
    }

    @Test
    public void allThingTypesInLabelMap() {
        for (ThingTypeUID thingTypeUID : OwBindingConstants.SUPPORTED_THING_TYPES) {
            if (!OwBindingConstants.THING_LABEL_MAP.containsKey(thingTypeUID)
                    && !DEPRECATED_THING_TYPES.contains(thingTypeUID)
                    && !OwBindingConstants.THING_TYPE_OWSERVER.equals(thingTypeUID)) {
                Assert.fail("missing label for thing type " + thingTypeUID.getAsString());
            }
        }
    }

    @Test
    public void acceptedItemTypeMapCompleteness() {
        List<String> channels = Arrays.stream(OwBindingConstants.class.getDeclaredFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> f.getName().startsWith("CHANNEL") && !f.getName().startsWith("CHANNEL_TYPE")).map(f -> {
                    try {
                        return (String) f.get(null);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("unexpected", e);
                    }
                }).collect(Collectors.toList());

        for (String channel : channels) {
            if (!OwBindingConstants.ACCEPTED_ITEM_TYPES_MAP.containsKey(channel)) {
                Assert.fail("missing accepted item type for channel " + channel);
            }
        }
    }
}
