/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.onewire.internal.device;

import static org.eclipse.smarthome.binding.onewire.internal.OwBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.SensorId;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseBridgeHandler;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseThingHandler;
import org.eclipse.smarthome.binding.onewire.internal.owserver.OwserverDeviceParameter;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DS2423} class defines an DS2423 device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2423 extends AbstractOwDevice {
    private final Logger logger = LoggerFactory.getLogger(DS2423.class);
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
            logger.trace("read array {} from {}", states, sensorId);

            if (states.size() != 2) {
                throw new OwException("Expected exactly two values, got " + String.valueOf(states.size()));
            } else {
                callback.postUpdate(CHANNEL_COUNTER + "0", states.get(0));
                callback.postUpdate(CHANNEL_COUNTER + "1", states.get(1));
            }
        }
    }
}
