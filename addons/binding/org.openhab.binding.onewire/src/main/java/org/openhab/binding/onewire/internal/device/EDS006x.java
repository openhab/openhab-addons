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

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.Util;
import org.openhab.binding.onewire.internal.handler.OwBaseBridgeHandler;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.internal.owserver.OwserverDeviceParameter;

/**
 * The {@link EDS006x} class defines an EDS006x device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class EDS006x extends AbstractOwDevice {
    public static final Set<OwChannelConfig> CHANNELS_EDS0064 = Collections
            .singleton(new OwChannelConfig(CHANNEL_TEMPERATURE, CHANNEL_TYPE_UID_TEMPERATURE));
    public static final Set<OwChannelConfig> CHANNELS_EDS0065 = Stream
            .of(new OwChannelConfig(CHANNEL_TEMPERATURE, CHANNEL_TYPE_UID_TEMPERATURE),
                    new OwChannelConfig(CHANNEL_HUMIDITY, CHANNEL_TYPE_UID_HUMIDITY),
                    new OwChannelConfig(CHANNEL_ABSOLUTE_HUMIDITY, CHANNEL_TYPE_UID_ABSHUMIDITY),
                    new OwChannelConfig(CHANNEL_DEWPOINT, CHANNEL_TYPE_UID_TEMPERATURE))
            .collect(Collectors.toSet());
    public static final Set<OwChannelConfig> CHANNELS_EDS0066 = Stream
            .of(new OwChannelConfig(CHANNEL_TEMPERATURE, CHANNEL_TYPE_UID_TEMPERATURE),
                    new OwChannelConfig(CHANNEL_PRESSURE, CHANNEL_TYPE_UID_PRESSURE))
            .collect(Collectors.toSet());
    public static final Set<OwChannelConfig> CHANNELS_EDS0067 = Stream
            .of(new OwChannelConfig(CHANNEL_TEMPERATURE, CHANNEL_TYPE_UID_TEMPERATURE),
                    new OwChannelConfig(CHANNEL_LIGHT, CHANNEL_TYPE_UID_LIGHT))
            .collect(Collectors.toSet());
    public static final Set<OwChannelConfig> CHANNELS_EDS0068 = Stream
            .of(new OwChannelConfig(CHANNEL_TEMPERATURE, CHANNEL_TYPE_UID_TEMPERATURE),
                    new OwChannelConfig(CHANNEL_HUMIDITY, CHANNEL_TYPE_UID_HUMIDITY),
                    new OwChannelConfig(CHANNEL_ABSOLUTE_HUMIDITY, CHANNEL_TYPE_UID_ABSHUMIDITY),
                    new OwChannelConfig(CHANNEL_DEWPOINT, CHANNEL_TYPE_UID_TEMPERATURE),
                    new OwChannelConfig(CHANNEL_PRESSURE, CHANNEL_TYPE_UID_PRESSURE),
                    new OwChannelConfig(CHANNEL_LIGHT, CHANNEL_TYPE_UID_LIGHT))
            .collect(Collectors.toSet());

    private final OwDeviceParameterMap temperatureParameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/temperature"));
        }
    };

    private final OwDeviceParameterMap humidityParameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/humidity"));
        }
    };

    private final OwDeviceParameterMap pressureParameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/pressure"));
        }
    };

    private final OwDeviceParameterMap lightParameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/light"));
        }
    };

    public EDS006x(SensorId sensorId, OwSensorType sensorType, OwBaseThingHandler callback) {
        super(sensorId, callback);

        String sensorTypeName = sensorType.name();
        temperatureParameter.set(THING_TYPE_OWSERVER,
                new OwserverDeviceParameter("/" + sensorTypeName + "/temperature"));
        humidityParameter.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/" + sensorTypeName + "/humidity"));
        pressureParameter.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/" + sensorTypeName + "/pressure"));
        lightParameter.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/" + sensorTypeName + "/light"));
    }

    @Override
    public void configureChannels() {
        isConfigured = true;
    }

    @Override
    public void refresh(OwBaseBridgeHandler bridgeHandler, Boolean forcedRefresh) throws OwException {
        if (isConfigured) {
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
                            (DecimalType) bridgeHandler.readDecimalType(sensorId, humidityParameter),
                            SmartHomeUnits.PERCENT);

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
                        (DecimalType) bridgeHandler.readDecimalType(sensorId, lightParameter), SmartHomeUnits.LUX);
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
