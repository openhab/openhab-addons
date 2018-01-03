/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.jeelink.internal.ec3k.Ec3kSensorDefinition;
import org.openhab.binding.jeelink.internal.lacrosse.LaCrosseSensorDefinition;

/**
 * Base class for sensor definitions.
 *
 * @author Volker Bier - Initial contribution
 *
 * @param <R> the Reading type this sensor provides.
 */
public abstract class SensorDefinition<R extends Reading> {
    private static final HashMap<JeeLinkHandler, ArrayList<JeeLinkReadingConverter<?>>> CONVERTERS = new HashMap<>();
    private static final HashSet<SensorDefinition<?>> SENSOR_DEFS = new HashSet<>();

    final ThingTypeUID thingTypeUid;
    final String sketchName;
    final String name;

    static {
        SENSOR_DEFS.add(new LaCrosseSensorDefinition());
        SENSOR_DEFS.add(new Ec3kSensorDefinition());
    }

    public SensorDefinition(ThingTypeUID thingTypeUid, String sketchName, String name) {
        this.thingTypeUid = thingTypeUid;
        this.sketchName = sketchName;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getSketchName() {
        return sketchName;
    }

    public ThingTypeUID getThingTypeUID() {
        return thingTypeUid;
    }

    public abstract Class<R> getReadingClass();

    public abstract JeeLinkSensorHandler createHandler(Thing thing);

    public abstract JeeLinkReadingConverter<R> createConverter();

    public static ArrayList<JeeLinkReadingConverter<?>> createConverters(final JeeLinkHandler handler) {
        ArrayList<JeeLinkReadingConverter<?>> converters = CONVERTERS.get(handler);

        if (converters == null) {
            converters = new ArrayList<>();
            for (SensorDefinition<?> sensor : SENSOR_DEFS) {
                converters.add(sensor.createConverter());
            }
            CONVERTERS.put(handler, converters);
        }

        return converters;
    }

    public static ArrayList<JeeLinkReadingConverter<?>> getConverters(final JeeLinkHandler handler) {
        return CONVERTERS.get(handler);
    }

    public static void disposeConverters(final JeeLinkHandler handler) {
        CONVERTERS.remove(handler);
    }

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
}