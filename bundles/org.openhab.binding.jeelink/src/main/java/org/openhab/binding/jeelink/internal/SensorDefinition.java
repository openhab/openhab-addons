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
package org.openhab.binding.jeelink.internal;

import java.util.HashSet;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.jeelink.internal.ec3k.Ec3kSensorDefinition;
import org.openhab.binding.jeelink.internal.lacrosse.LaCrosseSensorDefinition;
import org.openhab.binding.jeelink.internal.lacrosse.Tx22SensorDefinition;
import org.openhab.binding.jeelink.internal.pca301.Pca301SensorDefinition;

/**
 * Base class for sensor definitions.
 *
 * @author Volker Bier - Initial contribution
 *
 * @param <R> the Reading type this sensor provides.
 */
public abstract class SensorDefinition<R extends Reading> {
    private static final HashSet<SensorDefinition<?>> SENSOR_DEFS = new HashSet<>();

    final ThingTypeUID thingTypeUid;
    final String name;
    final String type;

    static {
        SENSOR_DEFS.add(new LaCrosseSensorDefinition());
        SENSOR_DEFS.add(new Ec3kSensorDefinition());
        SENSOR_DEFS.add(new Pca301SensorDefinition());
        SENSOR_DEFS.add(new Tx22SensorDefinition());
    }

    public SensorDefinition(ThingTypeUID thingTypeUid, String name, String type) {
        this.thingTypeUid = thingTypeUid;
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public ThingTypeUID getThingTypeUID() {
        return thingTypeUid;
    }

    public abstract Class<R> getReadingClass();

    public abstract JeeLinkSensorHandler<R> createHandler(Thing thing);

    public abstract JeeLinkReadingConverter<R> createConverter();

    public static SensorDefinition<?> getSensorDefinition(Reading reading) {
        for (SensorDefinition<?> sensor : SENSOR_DEFS) {
            if (sensor.getReadingClass().equals(reading.getClass())) {
                return sensor;
            }
        }

        return null;
    }

    public static HashSet<SensorDefinition<?>> getDefinitions() {
        return SENSOR_DEFS;
    }

    public static ThingHandler createHandler(ThingTypeUID thingTypeUid, Thing thing) {
        for (SensorDefinition<?> sensor : SENSOR_DEFS) {
            if (sensor.getThingTypeUID().equals(thingTypeUid)) {
                return sensor.createHandler(thing);
            }
        }

        return null;
    }

    public static JeeLinkReadingConverter<?> getConverter(String sensorType) {
        for (SensorDefinition<?> sensor : SENSOR_DEFS) {
            if (sensor.getSensorType().equals(sensorType)) {
                return sensor.createConverter();
            }
        }
        return null;
    }

    private String getSensorType() {
        return type;
    }
}
