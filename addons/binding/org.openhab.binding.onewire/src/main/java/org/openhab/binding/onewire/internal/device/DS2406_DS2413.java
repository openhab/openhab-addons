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

import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.DigitalIoConfig;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.internal.owserver.OwserverDeviceParameter;

/**
 * The {@link DS2406_DS2413} class defines an DS2406 or DS2413 device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2406_DS2413 extends AbstractDigitalOwDevice {
    public static final Set<OwChannelConfig> CHANNELS = Stream
            .of(new OwChannelConfig(CHANNEL_DIGITAL0, CHANNEL_TYPE_UID_DIO, "Digital I/O 0"),
                    new OwChannelConfig(CHANNEL_DIGITAL1, CHANNEL_TYPE_UID_DIO, "Digital I/O 1"))
            .collect(Collectors.toSet());

    public DS2406_DS2413(SensorId sensorId, OwBaseThingHandler callback) {
        super(sensorId, callback);
    }

    @Override
    public void configureChannels() throws OwException {
        ioConfig.clear();
        ioConfig.add(new DigitalIoConfig(callback.getThing(), 0, new OwserverDeviceParameter("/sensed.A"),
                new OwserverDeviceParameter("/PIO.A")));
        ioConfig.add(new DigitalIoConfig(callback.getThing(), 1, new OwserverDeviceParameter("/sensed.B"),
                new OwserverDeviceParameter("/PIO.B")));

        fullInParam = new OwserverDeviceParameter("/sensed.BYTE");
        fullOutParam = new OwserverDeviceParameter("/PIO.BYTE");

        super.configureChannels();
    }

}
