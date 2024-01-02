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
package org.openhab.binding.onewire.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.DigitalIoConfig;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.internal.owserver.OwserverDeviceParameter;

/**
 * The {@link DS2406_DS2413} class defines a DS2406 or DS2413 device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2406_DS2413 extends AbstractDigitalOwDevice {
    public DS2406_DS2413(SensorId sensorId, OwBaseThingHandler callback) {
        super(sensorId, callback);
    }

    @Override
    public void configureChannels() throws OwException {
        ioConfig.clear();
        ioConfig.add(new DigitalIoConfig(callback.getThing(), 0, new OwserverDeviceParameter("uncached/", "/sensed.A"),
                new OwserverDeviceParameter("/PIO.A")));
        ioConfig.add(new DigitalIoConfig(callback.getThing(), 1, new OwserverDeviceParameter("uncached/", "/sensed.B"),
                new OwserverDeviceParameter("/PIO.B")));

        fullInParam = new OwserverDeviceParameter("uncached/", "/sensed.BYTE");
        fullOutParam = new OwserverDeviceParameter("/PIO.BYTE");

        super.configureChannels();
    }
}
