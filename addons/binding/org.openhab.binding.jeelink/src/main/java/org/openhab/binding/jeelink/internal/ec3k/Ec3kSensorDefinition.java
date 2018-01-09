/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal.ec3k;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.jeelink.JeeLinkBindingConstants;
import org.openhab.binding.jeelink.internal.JeeLinkReadingConverter;
import org.openhab.binding.jeelink.internal.JeeLinkSensorHandler;
import org.openhab.binding.jeelink.internal.SensorDefinition;

/**
 * Sensor Defintion of a EC3000 Power Monitor.
 *
 * @author Volker Bier - Initial contribution
 */
public class Ec3kSensorDefinition extends SensorDefinition<Ec3kReading> {

    public Ec3kSensorDefinition() {
        super(JeeLinkBindingConstants.EC3000_SENSOR_THING_TYPE, "ec3kSerial", "EnergyCount 3000 Power Monitor");
    }

    @Override
    public JeeLinkReadingConverter<Ec3kReading> createConverter() {
        return new Ec3kReadingConverter();
    }

    @Override
    public Class<Ec3kReading> getReadingClass() {
        return Ec3kReading.class;
    }

    @Override
    public JeeLinkSensorHandler<Ec3kReading> createHandler(Thing thing) {
        return new Ec3kSensorHandler(thing);
    }

}
