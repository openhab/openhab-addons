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
package org.openhab.binding.jeelink.internal.revolt;

import org.openhab.binding.jeelink.internal.JeeLinkBindingConstants;
import org.openhab.binding.jeelink.internal.JeeLinkReadingConverter;
import org.openhab.binding.jeelink.internal.JeeLinkSensorHandler;
import org.openhab.binding.jeelink.internal.SensorDefinition;
import org.openhab.core.thing.Thing;

/**
 * Sensor Definition of a Revolt Energy Meter.
 *
 * @author Volker Bier - Initial contribution
 */
public class RevoltSensorDefinition extends SensorDefinition<RevoltReading> {
    public RevoltSensorDefinition() {
        super(JeeLinkBindingConstants.REVOLT_SENSOR_THING_TYPE, "Revolt Power Monitor", "r");
    }

    @Override
    public JeeLinkReadingConverter<RevoltReading> createConverter() {
        return new RevoltReadingConverter();
    }

    @Override
    public Class<RevoltReading> getReadingClass() {
        return RevoltReading.class;
    }

    @Override
    public JeeLinkSensorHandler<RevoltReading> createHandler(Thing thing) {
        return new RevoltSensorHandler(thing, type);
    }
}
