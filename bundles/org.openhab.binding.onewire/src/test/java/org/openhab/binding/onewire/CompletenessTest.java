/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.junit.Assert;
import org.junit.Test;
import org.openhab.binding.onewire.internal.OwBindingConstants;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.binding.onewire.internal.handler.AdvancedMultisensorThingHandler;
import org.openhab.binding.onewire.internal.handler.BasicMultisensorThingHandler;
import org.openhab.binding.onewire.internal.handler.BasicThingHandler;
import org.openhab.binding.onewire.internal.handler.EDSSensorThingHandler;

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

    private static final Set<OwSensorType> THINGHANDLER_SENSOR_TYPES = Collections
            .unmodifiableSet(Stream
                    .of(AdvancedMultisensorThingHandler.SUPPORTED_SENSOR_TYPES,
                            BasicMultisensorThingHandler.SUPPORTED_SENSOR_TYPES,
                            BasicThingHandler.SUPPORTED_SENSOR_TYPES, EDSSensorThingHandler.SUPPORTED_SENSOR_TYPES)
                    .flatMap(Set::stream).collect(Collectors.toSet()));

    private static final Set<ThingTypeUID> DEPRECATED_THING_TYPES = Collections.unmodifiableSet(Stream
            .of(OwBindingConstants.THING_TYPE_MS_TH, OwBindingConstants.THING_TYPE_MS_TV,
                    OwBindingConstants.THING_TYPE_COUNTER2, OwBindingConstants.THING_TYPE_DIGITALIO,
                    OwBindingConstants.THING_TYPE_DIGITALIO2, OwBindingConstants.THING_TYPE_DIGITALIO8,
                    OwBindingConstants.THING_TYPE_COUNTER2, OwBindingConstants.THING_TYPE_COUNTER,
                    OwBindingConstants.THING_TYPE_IBUTTON, OwBindingConstants.THING_TYPE_TEMPERATURE)
            .collect(Collectors.toSet()));

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
    public void allSupportedTypesInThingChannelsMap() {
        for (OwSensorType sensorType : EnumSet.allOf(OwSensorType.class)) {
            if (!OwBindingConstants.SENSOR_TYPE_CHANNEL_MAP.containsKey(sensorType)
                    && !IGNORED_SENSOR_TYPES.contains(sensorType)) {
                Assert.fail("missing channel configuration map for sensor type " + sensorType.name());
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
    public void allSensorTypesInLabelMap() {
        for (OwSensorType sensorType : EnumSet.allOf(OwSensorType.class)) {
            if (!OwBindingConstants.THING_LABEL_MAP.containsKey(sensorType)
                    && !IGNORED_SENSOR_TYPES.contains(sensorType)) {
                Assert.fail("missing label for sensor type " + sensorType.name());
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
