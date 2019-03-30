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

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.openhab.binding.onewire.internal.owserver.OwserverDeviceParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DS18x20} class defines an DS18x20 or DS1822 device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS18x20 extends AbstractOwDevice {
    private final Logger logger = LoggerFactory.getLogger(DS18x20.class);

    private OwserverDeviceParameter temperatureParameter = new OwserverDeviceParameter("/temperature");

    private boolean ignorePOR = false;

    public DS18x20(SensorId sensorId, OwBaseThingHandler callback) {
        super(sensorId, callback);
    }

    @Override
    public void configureChannels() throws OwException {
        Thing thing = callback.getThing();
        Channel temperatureChannel = thing.getChannel(CHANNEL_TEMPERATURE);

        if (temperatureChannel != null) {
            Configuration channelConfiguration = temperatureChannel.getConfiguration();
            if (channelConfiguration.containsKey(CONFIG_RESOLUTION)) {
                temperatureParameter = new OwserverDeviceParameter(
                        "/temperature" + (String) channelConfiguration.get(CONFIG_RESOLUTION));
            } else {
                temperatureParameter = new OwserverDeviceParameter("/temperature");
            }
            if (channelConfiguration.containsKey(CONFIG_IGNORE_POR)) {
                ignorePOR = (Boolean) channelConfiguration.get(CONFIG_IGNORE_POR);
            } else {
                ignorePOR = false;
            }
        } else {
            throw new OwException(CHANNEL_TEMPERATURE + " not found");
        }

        isConfigured = true;
    }

    @Override
    public void refresh(OwserverBridgeHandler bridgeHandler, Boolean forcedRefresh) throws OwException {
        if (isConfigured && enabledChannels.contains(CHANNEL_TEMPERATURE)) {
            QuantityType<Temperature> temperature = new QuantityType<Temperature>(
                    (DecimalType) bridgeHandler.readDecimalType(sensorId, temperatureParameter), SIUnits.CELSIUS);
            logger.trace("read temperature {} from {}", temperature, sensorId);
            if (ignorePOR && (Double.compare(temperature.doubleValue(), 85.0) == 0)) {
                logger.trace("ignored POR value from sensor {}", sensorId);
            } else {
                callback.postUpdate(CHANNEL_TEMPERATURE, temperature);
            }
        }
    }
}
