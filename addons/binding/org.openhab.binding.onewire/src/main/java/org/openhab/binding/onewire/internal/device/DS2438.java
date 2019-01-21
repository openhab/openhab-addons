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

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.MILLI;
import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.Util;
import org.openhab.binding.onewire.internal.handler.OwBaseBridgeHandler;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.internal.owserver.OwserverDeviceParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DS2438} class defines an DS2438 device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2438 extends AbstractOwDevice {
    public static final Set<OwChannelConfig> CHANNELS_MS_TC = Stream
            .of(new OwChannelConfig(CHANNEL_SUPPLYVOLTAGE, CHANNEL_TYPE_UID_VOLTAGE, "Supplyvoltage"),
                    new OwChannelConfig(CHANNEL_TEMPERATURE, CHANNEL_TYPE_UID_TEMPERATURE),
                    new OwChannelConfig(CHANNEL_VOLTAGE, CHANNEL_TYPE_UID_VOLTAGE))
            .collect(Collectors.toSet());
    public static final Set<OwChannelConfig> CHANNELS_MS_TH = Stream
            .of(new OwChannelConfig(CHANNEL_SUPPLYVOLTAGE, CHANNEL_TYPE_UID_VOLTAGE, "Supplyvoltage"),
                    new OwChannelConfig(CHANNEL_TEMPERATURE, CHANNEL_TYPE_UID_TEMPERATURE),
                    new OwChannelConfig(CHANNEL_HUMIDITY, CHANNEL_TYPE_UID_HUMIDITYCONF),
                    new OwChannelConfig(CHANNEL_ABSOLUTE_HUMIDITY, CHANNEL_TYPE_UID_ABSHUMIDITY),
                    new OwChannelConfig(CHANNEL_DEWPOINT, CHANNEL_TYPE_UID_TEMPERATURE, "Dewpoint"))
            .collect(Collectors.toSet());
    public static final Set<OwChannelConfig> CHANNELS_MS_TL = Stream
            .of(new OwChannelConfig(CHANNEL_SUPPLYVOLTAGE, CHANNEL_TYPE_UID_VOLTAGE, "Supplyvoltage"),
                    new OwChannelConfig(CHANNEL_TEMPERATURE, CHANNEL_TYPE_UID_TEMPERATURE),
                    new OwChannelConfig(CHANNEL_LIGHT, CHANNEL_TYPE_UID_LIGHT))
            .collect(Collectors.toSet());
    public static final Set<OwChannelConfig> CHANNELS_MS_TV = Stream
            .of(new OwChannelConfig(CHANNEL_SUPPLYVOLTAGE, CHANNEL_TYPE_UID_VOLTAGE, "Supplyvoltage"),
                    new OwChannelConfig(CHANNEL_TEMPERATURE, CHANNEL_TYPE_UID_TEMPERATURE),
                    new OwChannelConfig(CHANNEL_VOLTAGE, CHANNEL_TYPE_UID_VOLTAGE))
            .collect(Collectors.toSet());
    public static final Set<OwChannelConfig> CHANNELS = Stream
            .of(new OwChannelConfig(CHANNEL_SUPPLYVOLTAGE, CHANNEL_TYPE_UID_VOLTAGE, "Supplyvoltage"),
                    new OwChannelConfig(CHANNEL_TEMPERATURE, CHANNEL_TYPE_UID_TEMPERATURE),
                    new OwChannelConfig(CHANNEL_VOLTAGE, CHANNEL_TYPE_UID_VOLTAGE),
                    new OwChannelConfig(CHANNEL_CURRENT, CHANNEL_TYPE_UID_CURRENT))
            .collect(Collectors.toSet());
    public static final Set<OwChannelConfig> CHANNELS_AMS = Stream
            .of(new OwChannelConfig(CHANNEL_SUPPLYVOLTAGE, CHANNEL_TYPE_UID_VOLTAGE, "Supplyvoltage"),
                    new OwChannelConfig(CHANNEL_ABSOLUTE_HUMIDITY, CHANNEL_TYPE_UID_ABSHUMIDITY),
                    new OwChannelConfig(CHANNEL_DEWPOINT, CHANNEL_TYPE_UID_TEMPERATURE, "Dewpoint"),
                    new OwChannelConfig(CHANNEL_VOLTAGE, CHANNEL_TYPE_UID_VOLTAGE),
                    new OwChannelConfig(CHANNEL_DIGITAL0, CHANNEL_TYPE_UID_DIO, "Digital I/O 0"),
                    new OwChannelConfig(CHANNEL_DIGITAL1, CHANNEL_TYPE_UID_DIO, "Digital I/O 1"))
            .collect(Collectors.toSet());
    public static final Set<OwChannelConfig> CHANNELS_AMS_S = Stream
            .of(new OwChannelConfig(CHANNEL_SUPPLYVOLTAGE, CHANNEL_TYPE_UID_VOLTAGE, "Supplyvoltage"),
                    new OwChannelConfig(CHANNEL_ABSOLUTE_HUMIDITY, CHANNEL_TYPE_UID_ABSHUMIDITY),
                    new OwChannelConfig(CHANNEL_DEWPOINT, CHANNEL_TYPE_UID_TEMPERATURE, "Dewpoint"),
                    new OwChannelConfig(CHANNEL_LIGHT, CHANNEL_TYPE_UID_LIGHT),
                    new OwChannelConfig(CHANNEL_VOLTAGE, CHANNEL_TYPE_UID_VOLTAGE),
                    new OwChannelConfig(CHANNEL_DIGITAL0, CHANNEL_TYPE_UID_DIO, "Digital I/O 0"),
                    new OwChannelConfig(CHANNEL_DIGITAL1, CHANNEL_TYPE_UID_DIO, "Digital I/O 1"))
            .collect(Collectors.toSet());
    public static final Set<OwChannelConfig> CHANNELS_BMS = Stream
            .of(new OwChannelConfig(CHANNEL_SUPPLYVOLTAGE, CHANNEL_TYPE_UID_VOLTAGE, "Supplyvoltage"),
                    new OwChannelConfig(CHANNEL_ABSOLUTE_HUMIDITY, CHANNEL_TYPE_UID_ABSHUMIDITY),
                    new OwChannelConfig(CHANNEL_DEWPOINT, CHANNEL_TYPE_UID_TEMPERATURE, "Dewpoint"))
            .collect(Collectors.toSet());
    public static final Set<OwChannelConfig> CHANNELS_BMS_S = Stream
            .of(new OwChannelConfig(CHANNEL_SUPPLYVOLTAGE, CHANNEL_TYPE_UID_VOLTAGE, "Supplyvoltage"),
                    new OwChannelConfig(CHANNEL_ABSOLUTE_HUMIDITY, CHANNEL_TYPE_UID_ABSHUMIDITY),
                    new OwChannelConfig(CHANNEL_DEWPOINT, CHANNEL_TYPE_UID_TEMPERATURE, "Dewpoint"),
                    new OwChannelConfig(CHANNEL_LIGHT, CHANNEL_TYPE_UID_LIGHT))
            .collect(Collectors.toSet());

    private final Logger logger = LoggerFactory.getLogger(DS2438.class);

    public enum LightSensorType {
        ELABNET_V1,
        ELABNET_V2,
        IBUTTONLINK
    }

    public enum CurrentSensorType {
        INTERNAL,
        IBUTTONLINK
    }

    private LightSensorType lightSensorType = LightSensorType.ELABNET_V1;
    private CurrentSensorType currentSensorType = CurrentSensorType.INTERNAL;

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

    private final OwDeviceParameterMap voltageParameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/VAD"));
        }
    };

    private final OwDeviceParameterMap currentParamater = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/vis"));
        }
    };

    private final OwDeviceParameterMap supplyVoltageParameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/VDD"));
        }
    };

    public DS2438(SensorId sensorId, OwBaseThingHandler callback) {
        super(sensorId, callback);
    }

    @Override
    public void configureChannels() {
        Thing thing = callback.getThing();

        Channel humidityChannel = thing.getChannel(CHANNEL_HUMIDITY);
        if (humidityChannel != null) {
            Configuration channelConfiguration = humidityChannel.getConfiguration();
            if (channelConfiguration.get(CONFIG_HUMIDITY) != null) {
                humidityParameter.set(THING_TYPE_OWSERVER,
                        new OwserverDeviceParameter((String) channelConfiguration.get(CONFIG_HUMIDITY)));
            } else {
                humidityParameter.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/humidity"));
            }
        }

        isConfigured = true;
    }

    @Override
    public void refresh(OwBaseBridgeHandler bridgeHandler, Boolean forcedRefresh) throws OwException {
        if (isConfigured) {
            double Vcc = 5.0;

            if (enabledChannels.contains(CHANNEL_TEMPERATURE) || enabledChannels.contains(CHANNEL_HUMIDITY)
                    || enabledChannels.contains(CHANNEL_ABSOLUTE_HUMIDITY)
                    || enabledChannels.contains(CHANNEL_DEWPOINT)) {
                QuantityType<Temperature> temperature = new QuantityType<Temperature>(
                        (DecimalType) bridgeHandler.readDecimalType(sensorId, temperatureParameter), SIUnits.CELSIUS);
                logger.trace("read temperature {} from {}", temperature, sensorId);

                if (enabledChannels.contains(CHANNEL_TEMPERATURE)) {
                    callback.postUpdate(CHANNEL_TEMPERATURE, temperature);
                }

                if (enabledChannels.contains(CHANNEL_HUMIDITY) || enabledChannels.contains(CHANNEL_ABSOLUTE_HUMIDITY)
                        || enabledChannels.contains(CHANNEL_DEWPOINT)) {
                    QuantityType<Dimensionless> humidity = new QuantityType<Dimensionless>(
                            (DecimalType) bridgeHandler.readDecimalType(sensorId, humidityParameter),
                            SmartHomeUnits.PERCENT);
                    logger.trace("read humidity {} from {}", humidity, sensorId);

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

            if (enabledChannels.contains(CHANNEL_VOLTAGE)) {
                State voltage = new QuantityType<ElectricPotential>(
                        (DecimalType) bridgeHandler.readDecimalType(sensorId, voltageParameter), SmartHomeUnits.VOLT);
                logger.trace("read voltage {} from {}", voltage, sensorId);
                callback.postUpdate(CHANNEL_VOLTAGE, voltage);
            }

            if (enabledChannels.contains(CHANNEL_CURRENT)) {
                if (currentSensorType == CurrentSensorType.IBUTTONLINK) {
                    State current = bridgeHandler.readDecimalType(sensorId, voltageParameter);
                    if (current instanceof DecimalType) {
                        double currentDouble = ((DecimalType) current).doubleValue();
                        if (currentDouble >= 0.1 || currentDouble <= 3.78) {
                            current = new QuantityType<ElectricCurrent>(currentDouble * 5.163 + 0.483,
                                    SmartHomeUnits.AMPERE);
                        }
                        callback.postUpdate(CHANNEL_CURRENT, current);
                    } else {
                        callback.postUpdate(CHANNEL_CURRENT, UnDefType.UNDEF);
                    }
                } else {
                    State current = new QuantityType<ElectricCurrent>(
                            (DecimalType) bridgeHandler.readDecimalType(sensorId, currentParamater),
                            MILLI(SmartHomeUnits.AMPERE));
                    callback.postUpdate(CHANNEL_CURRENT, current);
                }
            }

            if (enabledChannels.contains(CHANNEL_SUPPLYVOLTAGE)) {
                Vcc = ((DecimalType) bridgeHandler.readDecimalType(sensorId, supplyVoltageParameter)).doubleValue();
                State supplyVoltage = new QuantityType<ElectricPotential>(Vcc, SmartHomeUnits.VOLT);
                callback.postUpdate(CHANNEL_SUPPLYVOLTAGE, supplyVoltage);
            }

            if (enabledChannels.contains(CHANNEL_LIGHT)) {
                switch (lightSensorType) {
                    case ELABNET_V2:
                        State light = bridgeHandler.readDecimalType(sensorId, currentParamater);
                        if (light instanceof DecimalType) {
                            light = new QuantityType<Illuminance>(
                                    Math.round(Math.pow(10, ((DecimalType) light).doubleValue() / 47 * 1000)),
                                    SmartHomeUnits.LUX);
                            callback.postUpdate(CHANNEL_LIGHT, light);
                        }
                        break;
                    case ELABNET_V1:
                        light = bridgeHandler.readDecimalType(sensorId, currentParamater);
                        if (light instanceof DecimalType) {
                            light = new QuantityType<Illuminance>(Math.round(Math
                                    .exp(1.059 * Math.log(1000000 * ((DecimalType) light).doubleValue() / (4096 * 390))
                                            + 4.518)
                                    * 20000), SmartHomeUnits.LUX);
                            callback.postUpdate(CHANNEL_LIGHT, light);
                        }
                        break;
                    case IBUTTONLINK:
                        light = bridgeHandler.readDecimalType(sensorId, voltageParameter);
                        if (light instanceof DecimalType) {
                            light = new QuantityType<Illuminance>(
                                    Math.pow(10, (65 / 7.5) - (47 / 7.5) * (Vcc / ((DecimalType) light).doubleValue())),
                                    SmartHomeUnits.LUX);
                            callback.postUpdate(CHANNEL_LIGHT, light);
                        }
                }
            }
        }
    }

    /**
     * set the type of the attached light sensor
     *
     * @param lightSensorType
     */
    public void setLightSensorType(LightSensorType lightSensorType) {
        this.lightSensorType = lightSensorType;
    }

    /**
     * set the type of the attached current sensor
     *
     * @param currentSensorType
     */
    public void setCurrentSensorType(CurrentSensorType currentSensorType) {
        this.currentSensorType = currentSensorType;
    }

}
