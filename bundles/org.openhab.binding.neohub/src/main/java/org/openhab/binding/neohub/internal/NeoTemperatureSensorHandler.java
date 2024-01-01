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
package org.openhab.binding.neohub.internal;

import static org.openhab.binding.neohub.internal.NeoHubBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Thing;

/**
 * The {@link NeoTemperatureSensorHandler} is the OpenHAB Handler for NeoTemperatureSensor devices
 * 
 * Note: inherits almost all the functionality of a {@link NeoBaseHandler}
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
@NonNullByDefault
public class NeoTemperatureSensorHandler extends NeoBaseHandler {

    public NeoTemperatureSensorHandler(Thing thing) {
        super(thing);
    }

    // =========== methods of NeoBaseHandler that are overridden ================

    @Override
    protected void toOpenHabSendChannelValues(NeoHubAbstractDeviceData.AbstractRecord deviceRecord) {
        boolean offline = deviceRecord.offline();

        toOpenHabSendValueDebounced(CHAN_TEMPERATURE_SENSOR,
                new QuantityType<>(deviceRecord.getActualTemperature(), getTemperatureUnit()), offline);

        toOpenHabSendValueDebounced(CHAN_BATTERY_LOW_ALARM, OnOffType.from(deviceRecord.isBatteryLow()), offline);
    }
}
