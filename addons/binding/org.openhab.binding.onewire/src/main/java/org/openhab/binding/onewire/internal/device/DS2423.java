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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.handler.OwBaseBridgeHandler;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.internal.owserver.OwserverDeviceParameter;

/**
 * The {@link DS2423} class defines an DS2423 device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2423 extends AbstractOwDevice {
    public static final Set<OwChannelConfig> CHANNELS = Stream
            .of(new OwChannelConfig(CHANNEL_COUNTER0, CHANNEL_TYPE_UID_COUNTER, "Counter 0"),
                    new OwChannelConfig(CHANNEL_COUNTER1, CHANNEL_TYPE_UID_COUNTER, "Counter 1"))
            .collect(Collectors.toSet());

    private final OwDeviceParameterMap counterParameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/counters.ALL"));
        }
    };

    public DS2423(SensorId sensorId, OwBaseThingHandler callback) {
        super(sensorId, callback);
    }

    @Override
    public void configureChannels() throws OwException {
        isConfigured = true;
    }

    @Override
    public void refresh(OwBaseBridgeHandler bridgeHandler, Boolean forcedRefresh) throws OwException {
        if (isConfigured) {
            List<State> states = bridgeHandler.readDecimalTypeArray(sensorId, counterParameter);

            if (states.size() != 2) {
                throw new OwException("Expected exactly two values, got " + String.valueOf(states.size()));
            } else {
                callback.postUpdate(CHANNEL_COUNTER + "0", states.get(0));
                callback.postUpdate(CHANNEL_COUNTER + "1", states.get(1));
            }
        }
    }
}
