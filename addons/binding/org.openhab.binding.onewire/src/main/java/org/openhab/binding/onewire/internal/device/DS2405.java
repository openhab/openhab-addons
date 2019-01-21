/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.openhab.binding.onewire.internal.OwBindingConstants.THING_TYPE_OWSERVER;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.DigitalIoConfig;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.internal.owserver.OwserverDeviceParameter;

/**
 * The {@link DS2405} class defines an DS2405 device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2405 extends AbstractDigitalOwDevice {

    public DS2405(SensorId sensorId, OwBaseThingHandler callback) {
        super(sensorId, callback);
    }

    @Override
    public void configureChannels() throws OwException {
        OwDeviceParameterMap inParam = new OwDeviceParameterMap();
        OwDeviceParameterMap outParam = new OwDeviceParameterMap();
        inParam.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("uncached/", "/sensed"));
        outParam.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/PIO"));

        ioConfig.add(new DigitalIoConfig(callback.getThing(), 0, inParam, outParam));

        fullInParam.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("uncached/", "/sensed"));
        fullOutParam.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/PIO"));

        super.configureChannels();
    }

}
