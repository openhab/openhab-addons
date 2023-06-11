/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ecobee.internal.handler;

import static org.openhab.binding.ecobee.internal.EcobeeBindingConstants.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ecobee.internal.config.EcobeeSensorConfiguration;
import org.openhab.binding.ecobee.internal.dto.thermostat.RemoteSensorCapabilityDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.RemoteSensorDTO;
import org.openhab.binding.ecobee.internal.util.StringUtils;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EcobeeSensorThingHandler} is responsible for updating the channels associated
 * with an Ecobee remote sensor.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class EcobeeSensorThingHandler extends BaseThingHandler {

    public static final String CAPABILITY_ADC = "adc";
    public static final String CAPABILITY_AIR_PRESSURE = "airPressure";
    public static final String CAPABILITY_AIR_QUALITY = "airQuality";
    public static final String CAPABILITY_AIR_QUALITY_ACCURACY = "airQualityAccuracy";
    public static final String CAPABILITY_CO2 = "co2";
    public static final String CAPABILITY_CO2_PPM = "co2PPM";
    public static final String CAPABILITY_DRY_CONTACT = "dryContact";
    public static final String CAPABILITY_HUMIDITY = "humidity";
    public static final String CAPABILITY_OCCUPANCY = "occupancy";
    public static final String CAPABILITY_TEMPERATURE = "temperature";
    public static final String CAPABILITY_VOC_PPM = "vocPPM";
    public static final String CAPABILITY_UNKNOWN = "unknown";

    private final Logger logger = LoggerFactory.getLogger(EcobeeSensorThingHandler.class);

    private @NonNullByDefault({}) String sensorId;

    private Map<String, State> stateCache = new ConcurrentHashMap<>();

    public EcobeeSensorThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        sensorId = getConfigAs(EcobeeSensorConfiguration.class).sensorId;
        logger.debug("SensorThing: Initializing sensor '{}'", sensorId);
        clearSavedState();
        updateStatus(EcobeeUtils.isBridgeOnline(getBridge()) ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
    }

    @Override
    public void dispose() {
        logger.debug("SensorThing: Disposing sensor '{}'", sensorId);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @SuppressWarnings("null")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            State state = stateCache.get(channelUID.getId());
            if (state != null) {
                updateState(channelUID.getId(), state);
            }
            return;
        }
    }

    public void updateChannels(RemoteSensorDTO sensor) {
        logger.debug("SensorThing: Updating channels for sensor '{}({})'", sensor.id, sensor.name);
        updateChannel(CH_SENSOR_ID, EcobeeUtils.undefOrString(sensor.id));
        updateChannel(CH_SENSOR_NAME, EcobeeUtils.undefOrString(sensor.name));
        updateChannel(CH_SENSOR_TYPE, EcobeeUtils.undefOrString(sensor.type));
        updateChannel(CH_SENSOR_CODE, EcobeeUtils.undefOrString(sensor.code));
        updateChannel(CH_SENSOR_IN_USE, EcobeeUtils.undefOrOnOff(sensor.inUse));
        for (RemoteSensorCapabilityDTO capability : sensor.capability) {
            updateCapabilityChannels(capability);
        }
    }

    private void updateCapabilityChannels(RemoteSensorCapabilityDTO capability) {
        ChannelUID uid = new ChannelUID(thing.getUID(), capability.type);
        Channel channel = thing.getChannel(uid);
        if (channel == null) {
            logger.debug("SensorThing: Create channel '{}'", uid);
            ThingBuilder thingBuilder;
            thingBuilder = editThing();
            channel = ChannelBuilder.create(uid, getAcceptedItemType(capability.type))
                    .withLabel("Sensor " + StringUtils.capitalizeWords(capability.type))
                    .withType(getChannelTypeUID(capability.type)).build();
            thingBuilder.withChannel(channel);
            updateThing(thingBuilder.build());
        }
        logger.trace("Capability '{}' has type '{}' with value '{}'", capability.id, capability.type, capability.value);
        updateCapabilityState(capability.type, capability.value);
    }

    // adc, co2, dryContact, humidity, temperature, occupancy, unknown.
    private String getAcceptedItemType(String capabilityType) {
        String acceptedItemType;
        switch (capabilityType) {
            case CAPABILITY_TEMPERATURE:
                acceptedItemType = "Number:Temperature";
                break;
            case CAPABILITY_HUMIDITY:
                acceptedItemType = "Number:Dimensionless";
                break;
            case CAPABILITY_OCCUPANCY:
                acceptedItemType = "Switch";
                break;
            case CAPABILITY_ADC:
            case CAPABILITY_AIR_PRESSURE:
            case CAPABILITY_AIR_QUALITY:
            case CAPABILITY_AIR_QUALITY_ACCURACY:
            case CAPABILITY_CO2:
            case CAPABILITY_CO2_PPM:
            case CAPABILITY_DRY_CONTACT:
            case CAPABILITY_UNKNOWN:
            case CAPABILITY_VOC_PPM:
            default:
                acceptedItemType = "String";
                break;
        }
        return acceptedItemType;
    }

    private ChannelTypeUID getChannelTypeUID(String capabilityType) {
        ChannelTypeUID channelTypeUID;
        switch (capabilityType) {
            case CAPABILITY_TEMPERATURE:
                channelTypeUID = CHANNELTYPEUID_TEMPERATURE;
                break;
            case CAPABILITY_HUMIDITY:
                channelTypeUID = CHANNELTYPEUID_HUMIDITY;
                break;
            case CAPABILITY_OCCUPANCY:
                channelTypeUID = CHANNELTYPEUID_OCCUPANCY;
                break;
            case CAPABILITY_ADC:
            case CAPABILITY_AIR_PRESSURE:
            case CAPABILITY_AIR_QUALITY:
            case CAPABILITY_AIR_QUALITY_ACCURACY:
            case CAPABILITY_CO2:
            case CAPABILITY_CO2_PPM:
            case CAPABILITY_DRY_CONTACT:
            case CAPABILITY_UNKNOWN:
            case CAPABILITY_VOC_PPM:
            default:
                channelTypeUID = CHANNELTYPEUID_GENERIC;
                break;
        }
        return channelTypeUID;
    }

    private void updateCapabilityState(String capabilityType, String value) {
        State state;
        switch (capabilityType) {
            case CAPABILITY_TEMPERATURE:
                try {
                    state = EcobeeUtils.undefOrTemperature(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    state = UnDefType.UNDEF;
                }
                break;
            case CAPABILITY_HUMIDITY:
                try {
                    state = EcobeeUtils.undefOrQuantity(Integer.parseInt(value), Units.PERCENT);
                } catch (NumberFormatException e) {
                    state = UnDefType.UNDEF;
                }
                break;
            case CAPABILITY_OCCUPANCY:
                state = EcobeeUtils.undefOrOnOff("true".equals(value));
                break;
            case CAPABILITY_ADC:
            case CAPABILITY_AIR_PRESSURE:
            case CAPABILITY_AIR_QUALITY:
            case CAPABILITY_AIR_QUALITY_ACCURACY:
            case CAPABILITY_CO2:
            case CAPABILITY_CO2_PPM:
            case CAPABILITY_DRY_CONTACT:
            case CAPABILITY_UNKNOWN:
            case CAPABILITY_VOC_PPM:
            default:
                state = EcobeeUtils.undefOrString(value);
                break;
        }
        updateChannel(capabilityType, state);
    }

    private void updateChannel(String channelId, State state) {
        updateState(channelId, state);
        stateCache.put(channelId, state);
    }

    private void clearSavedState() {
        stateCache.clear();
    }
}
