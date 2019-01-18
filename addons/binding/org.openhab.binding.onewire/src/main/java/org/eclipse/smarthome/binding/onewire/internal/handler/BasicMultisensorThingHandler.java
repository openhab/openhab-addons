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
package org.eclipse.smarthome.binding.onewire.internal.handler;

import static org.eclipse.smarthome.binding.onewire.internal.OwBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.DS2438Configuration;
import org.eclipse.smarthome.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.device.DS1923;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2438;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2438.CurrentSensorType;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2438.LightSensorType;
import org.eclipse.smarthome.binding.onewire.internal.device.OwSensorType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * The {@link BasicMultisensorThingHandler} is responsible for handling DS2438/DS1923 based multisensors (single
 * sensors)
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class BasicMultisensorThingHandler extends OwBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_MS_TX, THING_TYPE_MS_TH, THING_TYPE_MS_TV).collect(Collectors.toSet()));
    public static final Set<OwSensorType> SUPPORTED_SENSOR_TYPES = Collections
            .unmodifiableSet(Stream.of(OwSensorType.MS_TH, OwSensorType.MS_TC, OwSensorType.MS_TL, OwSensorType.MS_TV,
                    OwSensorType.DS1923, OwSensorType.DS2438).collect(Collectors.toSet()));

    public BasicMultisensorThingHandler(Thing thing,
            OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider, SUPPORTED_SENSOR_TYPES);
    }

    @Override
    public void initialize() {
        if (!thing.getThingTypeUID().equals(THING_TYPE_MS_TX)) {
            changeThingType(THING_TYPE_MS_TX, getConfig());
        }

        if (!super.configure()) {
            return;
        }

        // add sensors
        if (sensorType == OwSensorType.DS1923) {
            sensors.add(new DS1923(sensorId, this));
        } else {
            sensors.add(new DS2438(sensorId, this));
        }

        scheduler.execute(() -> {
            configureThingChannels();
        });
    }

    private void configureThingChannels() {
        ThingBuilder thingBuilder = editThing();

        // temperature channel (present on all devices)
        sensors.get(0).enableChannel(CHANNEL_TEMPERATURE);

        // supply voltage (all sensors, except DS1923)
        if (sensorType == OwSensorType.DS1923) {
            removeChannelIfExisting(thingBuilder, CHANNEL_SUPPLYVOLTAGE);
        } else {
            addChannelIfMissing(thingBuilder, CHANNEL_SUPPLYVOLTAGE, CHANNEL_TYPE_UID_VOLTAGE, "Supply Voltage");
            sensors.get(0).enableChannel(CHANNEL_SUPPLYVOLTAGE);
        }

        // analog channel
        switch (sensorType) {
            case DS2438:
                addChannelIfMissing(thingBuilder, CHANNEL_VOLTAGE, CHANNEL_TYPE_UID_VOLTAGE);
                addChannelIfMissing(thingBuilder, CHANNEL_CURRENT, CHANNEL_TYPE_UID_CURRENT);
                removeChannelIfExisting(thingBuilder, CHANNEL_HUMIDITY);
                removeChannelIfExisting(thingBuilder, CHANNEL_ABSOLUTE_HUMIDITY);
                removeChannelIfExisting(thingBuilder, CHANNEL_DEWPOINT);
                removeChannelIfExisting(thingBuilder, CHANNEL_LIGHT);
                ((DS2438) sensors.get(0)).setCurrentSensorType(CurrentSensorType.INTERNAL);
                sensors.get(0).enableChannel(CHANNEL_VOLTAGE);
                sensors.get(0).enableChannel(CHANNEL_CURRENT);
                break;
            case DS1923:
                // DS1923 has fixed humidity sensor on-board
                addChannelIfMissing(thingBuilder, CHANNEL_HUMIDITY, CHANNEL_TYPE_UID_HUMIDITY);
                addChannelIfMissing(thingBuilder, CHANNEL_ABSOLUTE_HUMIDITY, CHANNEL_TYPE_UID_ABSHUMIDITY);
                addChannelIfMissing(thingBuilder, CHANNEL_DEWPOINT, CHANNEL_TYPE_UID_DEWPOINT);
                removeChannelIfExisting(thingBuilder, CHANNEL_LIGHT);
                removeChannelIfExisting(thingBuilder, CHANNEL_CURRENT);
                removeChannelIfExisting(thingBuilder, CHANNEL_VOLTAGE);
                sensors.get(0).enableChannel(CHANNEL_HUMIDITY);
                sensors.get(0).enableChannel(CHANNEL_ABSOLUTE_HUMIDITY);
                sensors.get(0).enableChannel(CHANNEL_DEWPOINT);

                break;
            case MS_TC:
                addChannelIfMissing(thingBuilder, CHANNEL_CURRENT, CHANNEL_TYPE_UID_CURRENT);
                removeChannelIfExisting(thingBuilder, CHANNEL_HUMIDITY);
                removeChannelIfExisting(thingBuilder, CHANNEL_ABSOLUTE_HUMIDITY);
                removeChannelIfExisting(thingBuilder, CHANNEL_DEWPOINT);
                removeChannelIfExisting(thingBuilder, CHANNEL_LIGHT);
                removeChannelIfExisting(thingBuilder, CHANNEL_VOLTAGE);
                ((DS2438) sensors.get(0)).setCurrentSensorType(CurrentSensorType.IBUTTONLINK);
                sensors.get(0).enableChannel(CHANNEL_CURRENT);
                break;
            case MS_TH:
                // DS2438 can have different sensors
                addChannelIfMissing(thingBuilder, CHANNEL_HUMIDITY, CHANNEL_TYPE_UID_HUMIDITYCONF);
                addChannelIfMissing(thingBuilder, CHANNEL_ABSOLUTE_HUMIDITY, CHANNEL_TYPE_UID_ABSHUMIDITY);
                addChannelIfMissing(thingBuilder, CHANNEL_DEWPOINT, CHANNEL_TYPE_UID_DEWPOINT);
                removeChannelIfExisting(thingBuilder, CHANNEL_LIGHT);
                removeChannelIfExisting(thingBuilder, CHANNEL_VOLTAGE);
                removeChannelIfExisting(thingBuilder, CHANNEL_CURRENT);
                sensors.get(0).enableChannel(CHANNEL_HUMIDITY);
                sensors.get(0).enableChannel(CHANNEL_ABSOLUTE_HUMIDITY);
                sensors.get(0).enableChannel(CHANNEL_DEWPOINT);
                break;
            case MS_TL:
                addChannelIfMissing(thingBuilder, CHANNEL_LIGHT, CHANNEL_TYPE_UID_LIGHT);
                removeChannelIfExisting(thingBuilder, CHANNEL_HUMIDITY);
                removeChannelIfExisting(thingBuilder, CHANNEL_ABSOLUTE_HUMIDITY);
                removeChannelIfExisting(thingBuilder, CHANNEL_DEWPOINT);
                removeChannelIfExisting(thingBuilder, CHANNEL_CURRENT);
                removeChannelIfExisting(thingBuilder, CHANNEL_VOLTAGE);
                sensors.get(0).enableChannel(CHANNEL_LIGHT);
                ((DS2438) sensors.get(0)).setLightSensorType(LightSensorType.IBUTTONLINK);
                break;
            default:
                // use voltage channel as default
                addChannelIfMissing(thingBuilder, CHANNEL_VOLTAGE, CHANNEL_TYPE_UID_VOLTAGE);
                removeChannelIfExisting(thingBuilder, CHANNEL_HUMIDITY);
                removeChannelIfExisting(thingBuilder, CHANNEL_ABSOLUTE_HUMIDITY);
                removeChannelIfExisting(thingBuilder, CHANNEL_DEWPOINT);
                removeChannelIfExisting(thingBuilder, CHANNEL_CURRENT);
                removeChannelIfExisting(thingBuilder, CHANNEL_LIGHT);
                sensors.get(0).enableChannel(CHANNEL_VOLTAGE);
        }

        updateThing(thingBuilder.build());

        try {
            sensors.get(0).configureChannels();
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
        sensorType = bridgeHandler.getType(sensorId);

        if (sensorType == OwSensorType.DS1923) {
            properties.put(PROPERTY_MODELID, sensorType.toString());
            properties.put(PROPERTY_VENDOR, "Dallas/Maxim");
        } else {
            DS2438Configuration ds2438configuration = new DS2438Configuration(bridgeHandler, sensorId);

            sensorType = ds2438configuration.getSensorSubType();
            properties.put(PROPERTY_MODELID, sensorType.toString());

            String vendor = ds2438configuration.getVendor();
            properties.put(PROPERTY_VENDOR, vendor);
        }

        return properties;
    }
}
