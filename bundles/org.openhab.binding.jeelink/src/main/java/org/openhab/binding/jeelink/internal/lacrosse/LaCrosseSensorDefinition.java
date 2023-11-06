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
 * Sensor Defintion of a LaCrosse Temperature Sensor
 *
 * @author Volker Bier - Initial contribution
 */
public class LaCrosseSensorDefinition extends SensorDefinition<LaCrosseTemperatureReading> {

    public LaCrosseSensorDefinition() {
        super(JeeLinkBindingConstants.LACROSSE_SENSOR_THING_TYPE, "LaCrosse Temperature Sensor", "9");
    }

    @Override
    public JeeLinkReadingConverter<LaCrosseTemperatureReading> createConverter() {
        return new LaCrosseTemperatureReadingConverter();
    }

    @Override
    public Class<LaCrosseTemperatureReading> getReadingClass() {
        return LaCrosseTemperatureReading.class;
    }

    @Override
    public JeeLinkSensorHandler<LaCrosseTemperatureReading> createHandler(Thing thing) {
        return new LaCrosseTemperatureSensorHandler(thing, type);
    }
}
