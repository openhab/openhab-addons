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
package org.openhab.binding.onewire.internal.handler;

import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.onewire.internal.DS2438Configuration;
import org.openhab.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.device.DS18x20;
import org.openhab.binding.onewire.internal.device.DS2406_DS2413;
import org.openhab.binding.onewire.internal.device.DS2438;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.binding.onewire.internal.device.DS2438.LightSensorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AdvancedMultisensorThingHandler} is responsible for handling DS2438 based multisensors (modules)
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class AdvancedMultisensorThingHandler extends OwBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_AMS, THING_TYPE_BMS));
    public static final Set<OwSensorType> SUPPORTED_SENSOR_TYPES = Collections
            .unmodifiableSet(Stream.of(OwSensorType.AMS, OwSensorType.AMS_S, OwSensorType.BMS, OwSensorType.BMS_S)
                    .collect(Collectors.toSet()));

    private static final String PROPERTY_DS18B20 = "ds18b20";
    private static final String PROPERTY_DS2413 = "ds2413";
    private static final String PROPERTY_DS2438 = "ds2438";
    private static final Set<String> REQUIRED_PROPERTIES_AMS = Collections.unmodifiableSet(
            Stream.of(PROPERTY_HW_REVISION, PROPERTY_PROD_DATE, PROPERTY_DS18B20, PROPERTY_DS2438, PROPERTY_DS2413)
                    .collect(Collectors.toSet()));
    private static final Set<String> REQUIRED_PROPERTIES_BMS = Collections.unmodifiableSet(
            Stream.of(PROPERTY_HW_REVISION, PROPERTY_PROD_DATE, PROPERTY_DS18B20).collect(Collectors.toSet()));

    private final Logger logger = LoggerFactory.getLogger(AdvancedMultisensorThingHandler.class);

    private final ThingTypeUID thingType = this.thing.getThingTypeUID();
    private int hwRevision = 0;

    private int digitalRefreshInterval = 10 * 1000;
    private long digitalLastRefresh = 0;

    public AdvancedMultisensorThingHandler(Thing thing,
            OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider, SUPPORTED_SENSOR_TYPES,
                getRequiredProperties(thing.getThingTypeUID()));
    }

    @Override
    public void initialize() {
        Configuration configuration = getConfig();
        Map<String, String> properties = editProperties();

        if (!super.configure()) {
            return;
        }

        hwRevision = Integer.valueOf(properties.get(PROPERTY_HW_REVISION));

        if (configuration.containsKey(CONFIG_DIGITALREFRESH)) {
            digitalRefreshInterval = ((BigDecimal) configuration.get(CONFIG_DIGITALREFRESH)).intValue() * 1000;
        } else {
            digitalRefreshInterval = 10 * 1000;
        }
        digitalLastRefresh = 0;

        sensors.add(new DS2438(sensorId, this));
        sensors.add(new DS18x20(new SensorId(properties.get(PROPERTY_DS18B20)), this));
        if (THING_TYPE_AMS.equals(thingType)) {
            sensors.add(new DS2438(new SensorId(properties.get(PROPERTY_DS2438)), this));
            sensors.add(new DS2406_DS2413(new SensorId(properties.get(PROPERTY_DS2413)), this));
        }

        scheduler.execute(() -> {
            configureThingChannels();
        });
    }

    @Override
    public void refresh(OwBaseBridgeHandler bridgeHandler, long now) {
        try {
            if ((now >= (digitalLastRefresh + digitalRefreshInterval)) && (thingType == THING_TYPE_AMS)) {
                logger.trace("refreshing digital {}", this.thing.getUID());

                Boolean forcedRefresh = digitalLastRefresh == 0;
                digitalLastRefresh = now;

                if (!sensors.get(3).checkPresence(bridgeHandler)) {
                    return;
                }

                sensors.get(3).refresh(bridgeHandler, forcedRefresh);
            }

            if (now >= (lastRefresh + refreshInterval)) {
                if (!sensors.get(0).checkPresence(bridgeHandler)) {
                    return;
                }

                logger.trace("refreshing analog {}", this.thing.getUID());

                Boolean forcedRefresh = lastRefresh == 0;
                lastRefresh = now;

                if (thingType.equals(THING_TYPE_AMS)) {
                    for (int i = 0; i < sensors.size() - 1; i++) {
                        sensors.get(i).refresh(bridgeHandler, forcedRefresh);
                    }
                } else {
                    for (int i = 0; i < sensors.size(); i++) {
                        sensors.get(i).refresh(bridgeHandler, forcedRefresh);
                    }
                }
            }
        } catch (OwException e) {
            logger.debug("{}: refresh exception '{}'", this.thing.getUID(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "refresh exception");
        }
    }

    private void configureThingChannels() {
        Configuration configuration = getConfig();
        ThingBuilder thingBuilder = editThing();

        // temperature channel
        Channel temperatureChannel = addChannelIfMissing(thingBuilder, CHANNEL_TEMPERATURE,
                CHANNEL_TYPE_UID_TEMPERATURE);
        if (configuration.containsKey(CONFIG_TEMPERATURESENSOR)
                && configuration.get(CONFIG_TEMPERATURESENSOR).equals("DS18B20")) {
            // use DS18B20 for temperature
            if (!CHANNEL_TYPE_UID_TEMPERATURE_POR_RES.equals(temperatureChannel.getChannelTypeUID())) {
                removeChannelIfExisting(thingBuilder, CHANNEL_TEMPERATURE);
                addChannelIfMissing(thingBuilder, CHANNEL_TEMPERATURE, CHANNEL_TYPE_UID_TEMPERATURE_POR_RES,
                        temperatureChannel.getConfiguration());
            }
            sensors.get(1).enableChannel(CHANNEL_TEMPERATURE);
        } else {
            // use standard temperature channel
            if (!CHANNEL_TYPE_UID_TEMPERATURE.equals(temperatureChannel.getChannelTypeUID())) {
                removeChannelIfExisting(thingBuilder, CHANNEL_TEMPERATURE);
                addChannelIfMissing(thingBuilder, CHANNEL_TEMPERATURE, CHANNEL_TYPE_UID_TEMPERATURE,
                        temperatureChannel.getConfiguration());
            }
            sensors.get(0).enableChannel(CHANNEL_TEMPERATURE);
        }

        // always use HIH-4000 on ElabNet sensors.
        Channel humidityChannel = thing.getChannel(CHANNEL_HUMIDITY);
        if (humidityChannel != null && !humidityChannel.getConfiguration().containsKey(CONFIG_HUMIDITY)) {
            removeChannelIfExisting(thingBuilder, CHANNEL_HUMIDITY);
            addChannelIfMissing(thingBuilder, CHANNEL_HUMIDITY, CHANNEL_TYPE_UID_HUMIDITY,
                    new Configuration(new HashMap<String, Object>() {
                        {
                            put(CONFIG_HUMIDITY, "/HIH4000/humidity");
                        }
                    }));
        }

        // standard channels on all AMS/BMS
        sensors.get(0).enableChannel(CHANNEL_HUMIDITY);
        sensors.get(0).enableChannel(CHANNEL_ABSOLUTE_HUMIDITY);
        sensors.get(0).enableChannel(CHANNEL_DEWPOINT);
        sensors.get(0).enableChannel(CHANNEL_SUPPLYVOLTAGE);

        // light/current sensor
        if (configuration.containsKey(CONFIG_LIGHTSENSOR) && ((Boolean) configuration.get(CONFIG_LIGHTSENSOR))) {
            removeChannelIfExisting(thingBuilder, CHANNEL_CURRENT);
            addChannelIfMissing(thingBuilder, CHANNEL_LIGHT, CHANNEL_TYPE_UID_LIGHT);
            sensors.get(0).enableChannel(CHANNEL_LIGHT);
            if (hwRevision <= 13) {
                ((DS2438) sensors.get(0)).setLightSensorType(LightSensorType.ELABNET_V1);
            } else {
                ((DS2438) sensors.get(0)).setLightSensorType(LightSensorType.ELABNET_V2);
            }
        } else {
            removeChannelIfExisting(thingBuilder, CHANNEL_LIGHT);
            addChannelIfMissing(thingBuilder, CHANNEL_CURRENT, CHANNEL_TYPE_UID_CURRENT);
            sensors.get(0).enableChannel(CHANNEL_CURRENT);
        }

        // additional sensors on AMS
        if (THING_TYPE_AMS.equals(thingType)) {
            sensors.get(2).enableChannel(CHANNEL_VOLTAGE);

            if (configuration.containsKey(CONFIG_DIGITALREFRESH)) {
                digitalRefreshInterval = ((BigDecimal) configuration.get(CONFIG_DIGITALREFRESH)).intValue() * 1000;
            } else {
                // default 10ms
                digitalRefreshInterval = 10 * 1000;
            }
        }

        updateThing(thingBuilder.build());

        try {
            for (int i = 0; i < sensors.size(); i++) {
                sensors.get(i).configureChannels();
            }
        } catch (OwException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        validConfig = true;
        updatePresenceStatus(UnDefType.UNDEF);
    }

    @Override
    public Map<String, String> updateSensorProperties(OwBaseBridgeHandler bridgeHandler) throws OwException {
        Map<String, String> properties = new HashMap<String, String>();
        DS2438Configuration ds2438configuration = new DS2438Configuration(bridgeHandler, sensorId);

        sensorType = DS2438Configuration.getMultisensorType(ds2438configuration.getSensorSubType(),
                ds2438configuration.getAssociatedSensorTypes());

        properties.put(PROPERTY_MODELID, sensorType.toString());
        properties.put(PROPERTY_VENDOR, ds2438configuration.getVendor());

        properties.put(PROPERTY_PROD_DATE, ds2438configuration.getProductionDate());
        properties.put(PROPERTY_HW_REVISION, ds2438configuration.getHardwareRevision());

        switch (sensorType) {
            case BMS:
            case BMS_S:
                properties.put(PROPERTY_DS18B20,
                        ds2438configuration.getAssociatedSensorIds(OwSensorType.DS18B20).get(0).getFullPath());
                break;
            case AMS:
            case AMS_S:
                properties.put(PROPERTY_DS18B20,
                        ds2438configuration.getAssociatedSensorIds(OwSensorType.DS18B20).get(0).getFullPath());
                properties.put(PROPERTY_DS2413,
                        ds2438configuration.getAssociatedSensorIds(OwSensorType.DS2413).get(0).getFullPath());
                properties.put(PROPERTY_DS2438,
                        ds2438configuration.getAssociatedSensorIds(OwSensorType.MS_TV).get(0).getFullPath());

                break;
            default:
                throw new OwException("sensorType " + sensorType.toString() + " not supported by this thing handler");
        }

        return properties;
    }

    /**
     * used to determine the correct set of required properties
     *
     * @param thingType
     * @return
     */
    private static Set<String> getRequiredProperties(ThingTypeUID thingType) {
        if (THING_TYPE_AMS.equals(thingType)) {
            return REQUIRED_PROPERTIES_AMS;
        } else {
            return REQUIRED_PROPERTIES_BMS;
        }
    }
}
