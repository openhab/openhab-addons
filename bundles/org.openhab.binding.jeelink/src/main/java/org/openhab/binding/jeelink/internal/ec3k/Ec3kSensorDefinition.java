/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.jeelink.internal.ec3k;

import org.openhab.binding.jeelink.internal.JeeLinkBindingConstants;
import org.openhab.binding.jeelink.internal.JeeLinkReadingConverter;
import org.openhab.binding.jeelink.internal.JeeLinkSensorHandler;
import org.openhab.binding.jeelink.internal.SensorDefinition;
import org.openhab.core.thing.Thing;

/**
 * Sensor Defintion of an EC3000 Power Monitor.
 *
 * @author Volker Bier - Initial contribution
 */
public class Ec3kSensorDefinition extends SensorDefinition<Ec3kReading> {

    public Ec3kSensorDefinition() {
        super(JeeLinkBindingConstants.EC3000_SENSOR_THING_TYPE, "EnergyCount 3000 Power Monitor", "22");
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
        return new Ec3kSensorHandler(thing, type);
    }
}
