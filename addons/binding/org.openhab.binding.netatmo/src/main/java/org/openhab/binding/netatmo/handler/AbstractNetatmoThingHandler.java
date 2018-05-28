/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.*;
import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.Map;
import java.util.Optional;

import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.internal.channelhelper.BatteryHelper;
import org.openhab.binding.netatmo.internal.channelhelper.RadioHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractNetatmoThingHandler} is the abstract class that handles
 * common behaviors of all netatmo things
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public abstract class AbstractNetatmoThingHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(AbstractNetatmoThingHandler.class);

    // Units of measurement of the data delivered by the API
    public static final Unit<Temperature> API_TEMPERATURE_UNIT = SIUnits.CELSIUS;
    public static final Unit<Dimensionless> API_HUMIDITY_UNIT = SmartHomeUnits.PERCENT;
    public static final Unit<Pressure> API_PRESSURE_UNIT = HECTO(SIUnits.PASCAL);
    public static final Unit<Speed> API_WIND_SPEED_UNIT = SIUnits.KILOMETRE_PER_HOUR;
    public static final Unit<Angle> API_WIND_DIRECTION_UNIT = SmartHomeUnits.DEGREE_ANGLE;
    public static final Unit<Length> API_RAIN_UNIT = MILLI(SIUnits.METRE);
    public static final Unit<Dimensionless> API_CO2_UNIT = SmartHomeUnits.PARTS_PER_MILLION;
    public static final Unit<Dimensionless> API_NOISE_UNIT = SmartHomeUnits.DECIBEL;

    protected final MeasurableChannels measurableChannels = new MeasurableChannels();
    protected Optional<RadioHelper> radioHelper;
    protected Optional<BatteryHelper> batteryHelper;
    protected Configuration config;
    protected NetatmoBridgeHandler bridgeHandler;

    AbstractNetatmoThingHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getThing().getConfiguration();

        radioHelper = thing.getProperties().containsKey(PROPERTY_SIGNAL_LEVELS)
                ? Optional.of(new RadioHelper(thing.getProperties().get(PROPERTY_SIGNAL_LEVELS)))
                : Optional.empty();
        batteryHelper = thing.getProperties().containsKey(PROPERTY_BATTERY_LEVELS)
                ? Optional.of(new BatteryHelper(thing.getProperties().get(PROPERTY_BATTERY_LEVELS)))
                : Optional.empty();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Pending parent object initialization");
    }

    protected State getNAThingProperty(String channelId) {
        Optional<State> result;

        result = batteryHelper.flatMap(helper -> helper.getNAThingProperty(channelId));
        if (result.isPresent()) {
            return result.get();
        }
        result = radioHelper.flatMap(helper -> helper.getNAThingProperty(channelId));
        if (result.isPresent()) {
            return result.get();
        }
        result = measurableChannels.getNAThingProperty(channelId);

        return result.orElse(UnDefType.UNDEF);
    }

    protected void updateChannels() {
        getThing().getChannels().stream().filter(channel -> channel.getKind() != ChannelKind.TRIGGER)
                .forEach(channel -> {
                    String channelId = channel.getUID().getId();
                    if (isLinked(channelId)) {
                        State state = getNAThingProperty(channelId);
                        if (state != null) {
                            updateState(channel.getUID(), state);
                        }
                    }
                });
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        measurableChannels.addChannel(channelUID);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        super.channelUnlinked(channelUID);
        measurableChannels.removeChannel(channelUID);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateChannels();
        }
    }

    protected NetatmoBridgeHandler getBridgeHandler() {
        if (bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                bridgeHandler = (NetatmoBridgeHandler) bridge.getHandler();
            }
        }
        return bridgeHandler;
    }

    public boolean matchesId(String searchedId) {
        return searchedId != null && searchedId.equalsIgnoreCase(getId());
    }

    protected String getId() {
        if (config != null) {
            String equipmentId = (String) config.get(EQUIPMENT_ID);
            return equipmentId.toLowerCase();
        } else {
            return null;
        }
    }

    protected void updateProperties(Integer firmware, String modelId) {
        Map<String, String> properties = editProperties();
        if (firmware != null || modelId != null) {
            properties.put(Thing.PROPERTY_VENDOR, VENDOR);
        }
        if (firmware != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmware.toString());
        }
        if (modelId != null) {
            properties.put(Thing.PROPERTY_MODEL_ID, modelId);
        }
        updateProperties(properties);
    }
}
