/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.jeelink.internal.emt7110;

import org.openhab.binding.jeelink.internal.JeeLinkBindingConstants;
import org.openhab.binding.jeelink.internal.JeeLinkReadingConverter;
import org.openhab.binding.jeelink.internal.JeeLinkSensorHandler;
import org.openhab.binding.jeelink.internal.SensorDefinition;
import org.openhab.core.thing.Thing;

/**
 * Handler for a EMT7110 energy Sensor thing.
 *
 * @author Timo Schober - Initial contribution
 */
public class Emt7110SensorDefinition extends SensorDefinition<Emt7110Reading> {

    public Emt7110SensorDefinition() {
        super(JeeLinkBindingConstants.EMT7110_SENSOR_THING_TYPE, "EMT7110 power monitoring wireless socket", "EMT");
    }

    @Override
    public JeeLinkReadingConverter<Emt7110Reading> createConverter() {
        return new Emt7110ReadingConverter();
    }

    @Override
    public Class<Emt7110Reading> getReadingClass() {
        return Emt7110Reading.class;
    }

    @Override
    public JeeLinkSensorHandler<Emt7110Reading> createHandler(Thing thing) {
        return new Emt7110SensorHandler(thing, type);
    }
}
