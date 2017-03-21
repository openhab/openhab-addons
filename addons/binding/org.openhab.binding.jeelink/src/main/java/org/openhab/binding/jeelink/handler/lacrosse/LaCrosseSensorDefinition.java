/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.handler.lacrosse;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.jeelink.JeeLinkBindingConstants;
import org.openhab.binding.jeelink.handler.JeeLinkReadingConverter;
import org.openhab.binding.jeelink.handler.JeeLinkSensorHandler;
import org.openhab.binding.jeelink.handler.SensorDefinition;

/**
 * Sensor Defintion of a LaCrosse Temperature Sensor
 *
 * @author Volker Bier - Initial contribution
 */
public class LaCrosseSensorDefinition extends SensorDefinition<LaCrosseTemperatureReading> {

    public LaCrosseSensorDefinition() {
        super(JeeLinkBindingConstants.LACROSSE_SENSOR_THING_TYPE, "LaCrosseITPlusReader",
                "LaCrosse Temperature Sensor");
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
        return new LaCrosseTemperatureSensorHandler(thing);
    }
}
