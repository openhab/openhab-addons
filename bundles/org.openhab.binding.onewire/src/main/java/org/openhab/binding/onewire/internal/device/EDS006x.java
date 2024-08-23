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

import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.Util;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.openhab.binding.onewire.internal.owserver.OwserverDeviceParameter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EDS006x} class defines an EDS006x device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class EDS006x extends AbstractOwDevice {
    private final Logger logger = LoggerFactory.getLogger(EDS006x.class);
    private OwserverDeviceParameter temperatureParameter = new OwserverDeviceParameter("/temperature");
    private OwserverDeviceParameter humidityParameter = new OwserverDeviceParameter("/humidity");
    private OwserverDeviceParameter pressureParameter = new OwserverDeviceParameter("/pressure");
    private OwserverDeviceParameter lightParameter = new OwserverDeviceParameter("/light");

    public EDS006x(SensorId sensorId, OwSensorType sensorType, OwBaseThingHandler callback) {
        super(sensorId, callback);

        String sensorTypeName = sensorType.name();
        temperatureParameter = new OwserverDeviceParameter("/" + sensorTypeName + "/temperature");
        humidityParameter = new OwserverDeviceParameter("/" + sensorTypeName + "/humidity");
        pressureParameter = new OwserverDeviceParameter("/" + sensorTypeName + "/pressure");
        lightParameter = new OwserverDeviceParameter("/" + sensorTypeName + "/light");
    }

    @Override
    public void configureChannels() {
        isConfigured = true;
    }

    @Override
    public void refresh(OwserverBridgeHandler bridgeHandler, Boolean forcedRefresh) throws OwException {
        if (isConfigured) {
            logger.trace("refresh of sensor {} started", sensorId);
            if (enabledChannels.contains(CHANNEL_TEMPERATURE) || enabledChannels.contains(CHANNEL_HUMIDITY)
                    || enabledChannels.contains(CHANNEL_ABSOLUTE_HUMIDITY)
                    || enabledChannels.contains(CHANNEL_DEWPOINT)) {
                QuantityType<Temperature> temperature = new QuantityType<>(
                        (DecimalType) bridgeHandler.readDecimalType(sensorId, temperatureParameter), SIUnits.CELSIUS);

                if (enabledChannels.contains(CHANNEL_TEMPERATURE)) {
                    callback.postUpdate(CHANNEL_TEMPERATURE, temperature);
                }

                if (enabledChannels.contains(CHANNEL_HUMIDITY) || enabledChannels.contains(CHANNEL_ABSOLUTE_HUMIDITY)
                        || enabledChannels.contains(CHANNEL_DEWPOINT)) {
                    QuantityType<Dimensionless> humidity = new QuantityType<>(
                            (DecimalType) bridgeHandler.readDecimalType(sensorId, humidityParameter), Units.PERCENT);

                    if (enabledChannels.contains(CHANNEL_HUMIDITY)) {
                        callback.postUpdate(CHANNEL_HUMIDITY, humidity);
                    }

                    if (enabledChannels.contains(CHANNEL_ABSOLUTE_HUMIDITY)) {
                        callback.postUpdate(CHANNEL_ABSOLUTE_HUMIDITY,
                                Util.calculateAbsoluteHumidity(temperature, humidity));
                    }

                    if (enabledChannels.contains(CHANNEL_DEWPOINT)) {
                        callback.postUpdate(CHANNEL_DEWPOINT, Util.calculateDewpoint(temperature, humidity));
                    }
                }
            }

            if (enabledChannels.contains(CHANNEL_LIGHT)) {
                QuantityType<Illuminance> light = new QuantityType<>(
                        (DecimalType) bridgeHandler.readDecimalType(sensorId, lightParameter), Units.LUX);
                callback.postUpdate(CHANNEL_LIGHT, light);
            }

            if (enabledChannels.contains(CHANNEL_PRESSURE)) {
                QuantityType<Pressure> pressure = new QuantityType<>(
                        (DecimalType) bridgeHandler.readDecimalType(sensorId, pressureParameter),
                        MetricPrefix.HECTO(SIUnits.PASCAL));
                callback.postUpdate(CHANNEL_PRESSURE, pressure);
            }
        }
    }
}
