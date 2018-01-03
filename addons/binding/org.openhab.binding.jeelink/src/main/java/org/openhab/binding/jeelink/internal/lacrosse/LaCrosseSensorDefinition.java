/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal.lacrosse;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.jeelink.JeeLinkBindingConstants;
import org.openhab.binding.jeelink.internal.JeeLinkReadingConverter;
import org.openhab.binding.jeelink.internal.JeeLinkSensorHandler;
import org.openhab.binding.jeelink.internal.SensorDefinition;

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
