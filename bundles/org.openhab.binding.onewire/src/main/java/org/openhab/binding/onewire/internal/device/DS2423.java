/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.onewire.internal.OwBindingConstants.CHANNEL_COUNTER;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.openhab.binding.onewire.internal.owserver.OwserverDeviceParameter;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DS2423} class defines a DS2423 device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2423 extends AbstractOwDevice {
    private final Logger logger = LoggerFactory.getLogger(DS2423.class);
    private final OwserverDeviceParameter counterParameter = new OwserverDeviceParameter("/counters.ALL");

    public DS2423(SensorId sensorId, OwBaseThingHandler callback) {
        super(sensorId, callback);
    }

    @Override
    public void configureChannels() throws OwException {
        isConfigured = true;
    }

    @Override
    public void refresh(OwserverBridgeHandler bridgeHandler, Boolean forcedRefresh) throws OwException {
        if (isConfigured) {
            logger.trace("refresh of sensor {} started", sensorId);
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
