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
package org.openhab.binding.onewire.internal.handler;

import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.DS2438Configuration;
import org.openhab.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.config.AMSHandlerConfiguration;
import org.openhab.binding.onewire.internal.device.AbstractOwDevice;
import org.openhab.binding.onewire.internal.device.DS18x20;
import org.openhab.binding.onewire.internal.device.DS2406_DS2413;
import org.openhab.binding.onewire.internal.device.DS2438;
import org.openhab.binding.onewire.internal.device.DS2438.LightSensorType;
import org.openhab.binding.onewire.internal.device.OwChannelConfig;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AdvancedMultisensorThingHandler} is responsible for handling DS2438 based multisensors (modules)
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class AdvancedMultisensorThingHandler extends OwBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_AMS, THING_TYPE_BMS);
    public static final Set<OwSensorType> SUPPORTED_SENSOR_TYPES = Set.of(OwSensorType.AMS, OwSensorType.AMS_S,
            OwSensorType.BMS, OwSensorType.BMS_S);

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
        AMSHandlerConfiguration configuration = getConfig().as(AMSHandlerConfiguration.class);
        Map<String, String> properties = editProperties();

        if (!super.configureThingHandler()) {
            return;
        }

        hwRevision = Integer.parseInt(properties.getOrDefault(PROPERTY_HW_REVISION, "0"));

        try {
            sensors.add(new DS2438(sensorId, this));
            sensors.add(new DS18x20(new SensorId(properties.get(PROPERTY_DS18B20)), this));
            if (THING_TYPE_AMS.equals(thingType)) {
                sensors.add(new DS2438(new SensorId(properties.get(PROPERTY_DS2438)), this));
                sensors.add(new DS2406_DS2413(new SensorId(properties.get(PROPERTY_DS2413)), this));
                digitalRefreshInterval = configuration.digitalRefresh * 1000;
                digitalLastRefresh = 0;
            }
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "properties invalid");
        }
        scheduler.execute(this::configureThingChannels);
    }

    @Override
    public void refresh(OwserverBridgeHandler bridgeHandler, long now) {
        try {
            if ((now >= (digitalLastRefresh + digitalRefreshInterval)) && (thingType.equals(THING_TYPE_AMS))) {
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
                    for (AbstractOwDevice sensor : sensors) {
                        sensor.refresh(bridgeHandler, forcedRefresh);
                    }
                }
            }
        } catch (OwException e) {
            logger.debug("{}: refresh exception '{}'", this.thing.getUID(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "refresh exception");
        }
    }

    @Override
    protected void configureThingChannels() {
        Configuration configuration = getConfig();
        ThingBuilder thingBuilder = editThing();

        // delete unwanted channels
        Set<String> existingChannelIds = thing.getChannels().stream().map(channel -> channel.getUID().getId())
                .collect(Collectors.toSet());
        Set<String> wantedChannelIds = SENSOR_TYPE_CHANNEL_MAP.getOrDefault(sensorType, Set.of()).stream()
                .map(channelConfig -> channelConfig.channelId).collect(Collectors.toSet());
        wantedChannelIds.add(CHANNEL_TEMPERATURE);
        wantedChannelIds.add(CHANNEL_HUMIDITY);
        existingChannelIds.stream().filter(channelId -> !wantedChannelIds.contains(channelId))
                .forEach(channelId -> removeChannelIfExisting(thingBuilder, channelId));

        // add or update wanted channels
        SENSOR_TYPE_CHANNEL_MAP.getOrDefault(sensorType, Set.of()).stream()
                .forEach(channelConfig -> addChannelIfMissingAndEnable(thingBuilder, channelConfig));

        // temperature channel
        if (configuration.containsKey(CONFIG_TEMPERATURESENSOR)
                && configuration.get(CONFIG_TEMPERATURESENSOR).equals("DS18B20")) {
            addChannelIfMissingAndEnable(thingBuilder,
                    new OwChannelConfig(CHANNEL_TEMPERATURE, CHANNEL_TYPE_UID_TEMPERATURE_POR_RES), 1);
        } else {
            addChannelIfMissingAndEnable(thingBuilder,
                    new OwChannelConfig(CHANNEL_TEMPERATURE, CHANNEL_TYPE_UID_TEMPERATURE));
        }

        // humidity channel

        addChannelIfMissingAndEnable(thingBuilder, new OwChannelConfig(CHANNEL_HUMIDITY, CHANNEL_TYPE_UID_HUMIDITY),
                new Configuration(Map.of(CONFIG_HUMIDITY, "/HIH4000/humidity")));

        // configure light channel
        if (sensorType == OwSensorType.AMS_S || sensorType == OwSensorType.BMS_S) {
            if (hwRevision <= 13) {
                ((DS2438) sensors.get(0)).setLightSensorType(LightSensorType.ELABNET_V1);
            } else {
                ((DS2438) sensors.get(0)).setLightSensorType(LightSensorType.ELABNET_V2);
            }
        }

        updateThing(thingBuilder.build());

        try {
            for (AbstractOwDevice sensor : sensors) {
                sensor.configureChannels();
            }
        } catch (OwException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        validConfig = true;
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
    }

    @Override
    public void updateSensorProperties(OwserverBridgeHandler bridgeHandler) throws OwException {
        Map<String, String> properties = editProperties();
        DS2438Configuration ds2438configuration = new DS2438Configuration(bridgeHandler, sensorId);

        sensorType = DS2438Configuration.getMultisensorType(ds2438configuration.getSensorSubType(),
                ds2438configuration.getAssociatedSensorTypes());

        properties.put(PROPERTY_MODELID, sensorType.toString());
        properties.put(PROPERTY_VENDOR, ds2438configuration.getVendor());

        properties.put(PROPERTY_PROD_DATE, ds2438configuration.getProductionDate());
        properties.put(PROPERTY_HW_REVISION, ds2438configuration.getHardwareRevision());

        switch (sensorType) {
            case BMS, BMS_S -> properties.put(PROPERTY_DS18B20,
                    ds2438configuration.getAssociatedSensorIds(OwSensorType.DS18B20).get(0).getFullPath());
            case AMS, AMS_S -> {
                properties.put(PROPERTY_DS18B20,
                        ds2438configuration.getAssociatedSensorIds(OwSensorType.DS18B20).get(0).getFullPath());
                properties.put(PROPERTY_DS2413,
                        ds2438configuration.getAssociatedSensorIds(OwSensorType.DS2413).get(0).getFullPath());
                properties.put(PROPERTY_DS2438,
                        ds2438configuration.getAssociatedSensorIds(OwSensorType.MS_TV).get(0).getFullPath());
            }
            default ->
                throw new OwException("sensorType " + sensorType.toString() + " not supported by this thing handler");
        }

        updateProperties(properties);
    }

    /**
     * used to determine the correct set of required properties
     *
     * @param thingType
     * @return
     */
    private static Set<String> getRequiredProperties(ThingTypeUID thingType) {
        return THING_TYPE_AMS.equals(thingType) ? REQUIRED_PROPERTIES_AMS : REQUIRED_PROPERTIES_BMS;
    }
}
