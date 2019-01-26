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
 * The {@link DS2408} class defines an DS2408 device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2408 extends AbstractDigitalOwDevice {
    public static final Set<OwChannelConfig> CHANNELS = Stream
            .of(new OwChannelConfig(CHANNEL_DIGITAL0, CHANNEL_TYPE_UID_DIO, "Digital I/O 0"),
                    new OwChannelConfig(CHANNEL_DIGITAL1, CHANNEL_TYPE_UID_DIO, "Digital I/O 1"),
                    new OwChannelConfig(CHANNEL_DIGITAL2, CHANNEL_TYPE_UID_DIO, "Digital I/O 2"),
                    new OwChannelConfig(CHANNEL_DIGITAL3, CHANNEL_TYPE_UID_DIO, "Digital I/O 3"),
                    new OwChannelConfig(CHANNEL_DIGITAL4, CHANNEL_TYPE_UID_DIO, "Digital I/O 4"),
                    new OwChannelConfig(CHANNEL_DIGITAL5, CHANNEL_TYPE_UID_DIO, "Digital I/O 5"),
                    new OwChannelConfig(CHANNEL_DIGITAL6, CHANNEL_TYPE_UID_DIO, "Digital I/O 6"),
                    new OwChannelConfig(CHANNEL_DIGITAL7, CHANNEL_TYPE_UID_DIO, "Digital I/O 7"))
            .collect(Collectors.toSet());

    public DS2408(SensorId sensorId, OwBaseThingHandler callback) {
        super(sensorId, callback);
    }

    @Override
    public void configureChannels() throws OwException {
        ioConfig.clear();

        for (int i = 0; i < 8; i++) {
            OwDeviceParameterMap inParam = new OwDeviceParameterMap();
            OwDeviceParameterMap outParam = new OwDeviceParameterMap();

            inParam.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("uncached/", String.format("/sensed.%d", i)));
            outParam.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter(String.format("/PIO.%d", i)));
            ioConfig.add(new DigitalIoConfig(callback.getThing(), i, inParam, outParam));
        }

        fullInParam.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("uncached/", "/sensed.BYTE"));
        fullOutParam.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/PIO.BYTE"));

        super.configureChannels();
    }
}
