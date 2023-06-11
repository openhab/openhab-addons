/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.jeelink.internal.lacrosse;

import org.openhab.binding.jeelink.internal.JeeLinkBindingConstants;
import org.openhab.binding.jeelink.internal.JeeLinkReadingConverter;
import org.openhab.binding.jeelink.internal.JeeLinkSensorHandler;
import org.openhab.binding.jeelink.internal.SensorDefinition;
import org.openhab.core.thing.Thing;

/**
 * Sensor Defintion of a TX22 Temperature/Humidity Sensor.
 *
 * @author Volker Bier - Initial contribution
 */
public class Tx22SensorDefinition extends SensorDefinition<Tx22Reading> {

    public Tx22SensorDefinition() {
        super(JeeLinkBindingConstants.TX22_SENSOR_THING_TYPE, "TX22 Temperature/Humidity Sensor", "WS");
    }

    @Override
    public JeeLinkReadingConverter<Tx22Reading> createConverter() {
        return new Tx22ReadingConverter();
    }

    @Override
    public Class<Tx22Reading> getReadingClass() {
        return Tx22Reading.class;
    }

    @Override
    public JeeLinkSensorHandler<Tx22Reading> createHandler(Thing thing) {
        return new Tx22SensorHandler(thing, type);
    }
}
