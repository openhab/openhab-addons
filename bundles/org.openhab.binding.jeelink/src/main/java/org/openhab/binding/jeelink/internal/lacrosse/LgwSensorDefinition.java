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
 * Sensor definition of a sensor directly connected to a LGW.
 *
 * @author Volker Bier - Initial contribution
 */
public class LgwSensorDefinition extends SensorDefinition<LgwReading> {

    public LgwSensorDefinition() {
        super(JeeLinkBindingConstants.LGW_SENSOR_THING_TYPE, "LGW Sensor", "LGW");
    }

    @Override
    public JeeLinkReadingConverter<LgwReading> createConverter() {
        return new LgwReadingConverter();
    }

    @Override
    public Class<LgwReading> getReadingClass() {
        return LgwReading.class;
    }

    @Override
    public JeeLinkSensorHandler<LgwReading> createHandler(Thing thing) {
        return new LgwSensorHandler(thing, type);
    }
}
