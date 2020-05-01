/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.apache.commons.lang.WordUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.ecobee.internal.config.EcobeeSensorConfiguration;
import org.openhab.binding.ecobee.internal.dto.thermostat.RemoteSensorCapabilityDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.RemoteSensorDTO;
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
    public static final String CAPABILITY_CO2 = "co2";
    public static final String CAPABILITY_DRY_CONTACT = "dryContact";
    public static final String CAPABILITY_HUMIDITY = "humidity";
    public static final String CAPABILITY_OCCUPANCY = "occupancy";
    public static final String CAPABILITY_TEMPERATURE = "temperature";
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
            thingBuilder.withChannel(ChannelBuilder.create(uid, getChannelType(capability.type))
                    .withLabel("Sensor " + WordUtils.capitalize(capability.type)).build());
            updateThing(thingBuilder.build());
        }
        logger.trace("Capability '{}' has type '{}' with value '{}'", capability.id, capability.type, capability.value);
        updateCapabilityState(capability.type, capability.value);
    }

    // adc, co2, dryContact, humidity, temperature, occupancy, unknown.
    private String getChannelType(String capabilityType) {
        String type;
        switch (capabilityType) {
            case CAPABILITY_TEMPERATURE:
                type = "Number:Temperature";
                break;
            case CAPABILITY_HUMIDITY:
                type = "Number:Dimensionless";
                break;
            case CAPABILITY_OCCUPANCY:
                type = "Switch";
                break;
            case CAPABILITY_ADC:
            case CAPABILITY_CO2:
            case CAPABILITY_DRY_CONTACT:
            case CAPABILITY_UNKNOWN:
            default:
                type = "String";
                break;
        }
        return type;
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
                    state = EcobeeUtils.undefOrQuantity(Integer.parseInt(value), SmartHomeUnits.PERCENT);
                } catch (NumberFormatException e) {
                    state = UnDefType.UNDEF;
                }
                break;
            case CAPABILITY_OCCUPANCY:
                state = EcobeeUtils.undefOrOnOff("true".equals(value));
                break;
            case CAPABILITY_ADC:
            case CAPABILITY_CO2:
            case CAPABILITY_DRY_CONTACT:
            case CAPABILITY_UNKNOWN:
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
