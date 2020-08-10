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
package org.openhab.binding.netatmo.internal.handler;

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.*;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
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
 * @author Rob Nielsen - Added day, week, and month measurements to the weather station and modules
 *
 */
@NonNullByDefault
public abstract class AbstractNetatmoThingHandler extends BaseThingHandler {
    // Units of measurement of the data delivered by the API
    public static final Unit<Temperature> API_TEMPERATURE_UNIT = SIUnits.CELSIUS;
    public static final Unit<Dimensionless> API_HUMIDITY_UNIT = SmartHomeUnits.PERCENT;
    public static final Unit<Pressure> API_PRESSURE_UNIT = HECTO(SIUnits.PASCAL);
    public static final Unit<Speed> API_WIND_SPEED_UNIT = SIUnits.KILOMETRE_PER_HOUR;
    public static final Unit<Angle> API_WIND_DIRECTION_UNIT = SmartHomeUnits.DEGREE_ANGLE;
    public static final Unit<Length> API_RAIN_UNIT = MILLI(SIUnits.METRE);
    public static final Unit<Dimensionless> API_CO2_UNIT = SmartHomeUnits.PARTS_PER_MILLION;
    public static final Unit<Dimensionless> API_NOISE_UNIT = SmartHomeUnits.DECIBEL;

    private final Logger logger = LoggerFactory.getLogger(AbstractNetatmoThingHandler.class);

    protected final TimeZoneProvider timeZoneProvider;
    protected final MeasurableChannels measurableChannels = new MeasurableChannels();
    private @Nullable RadioHelper radioHelper;
    private @Nullable BatteryHelper batteryHelper;
    protected @Nullable Configuration config;
    private @Nullable NetatmoBridgeHandler bridgeHandler;

    AbstractNetatmoThingHandler(Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        logger.debug("initializing handler for thing {}", getThing().getUID());
        Bridge bridge = getBridge();
        initializeThing(bridge != null ? bridge.getStatus() : null);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());
        initializeThing(bridgeStatusInfo.getStatus());
    }

    private void initializeThing(@Nullable ThingStatus bridgeStatus) {
        Bridge bridge = getBridge();
        BridgeHandler bridgeHandler = bridge != null ? bridge.getHandler() : null;
        if (bridgeHandler != null && bridgeStatus != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                config = getThing().getConfiguration();

                radioHelper = thing.getProperties().containsKey(PROPERTY_SIGNAL_LEVELS)
                        ? new RadioHelper(thing.getProperties().get(PROPERTY_SIGNAL_LEVELS))
                        : null;
                batteryHelper = thing.getProperties().containsKey(PROPERTY_BATTERY_LEVELS)
                        ? new BatteryHelper(thing.getProperties().get(PROPERTY_BATTERY_LEVELS))
                        : null;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Pending parent object initialization");

                initializeThing();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    protected abstract void initializeThing();

    protected State getNAThingProperty(String channelId) {
        Optional<State> result;

        result = getBatteryHelper().flatMap(helper -> helper.getNAThingProperty(channelId));
        if (result.isPresent()) {
            return result.get();
        }
        result = getRadioHelper().flatMap(helper -> helper.getNAThingProperty(channelId));
        if (result.isPresent()) {
            return result.get();
        }
        result = measurableChannels.getNAThingProperty(channelId);

        return result.orElse(UnDefType.UNDEF);
    }

    protected void updateChannels() {
        updateDataChannels();

        triggerEventChannels();
    }

    private void updateDataChannels() {
        getThing().getChannels().stream().filter(channel -> !channel.getKind().equals(ChannelKind.TRIGGER))
                .forEach(channel -> {

                    String channelId = channel.getUID().getId();
                    if (isLinked(channelId)) {
                        State state = getNAThingProperty(channelId);
                        updateState(channel.getUID(), state);
                    }
                });
    }

    /**
     * Triggers all event/trigger channels
     * (when a channel is triggered, a rule can get all other information from the updated non-trigger channels)
     */
    private void triggerEventChannels() {
        getThing().getChannels().stream().filter(channel -> channel.getKind().equals(ChannelKind.TRIGGER))
                .forEach(channel -> triggerChannelIfRequired(channel.getUID().getId()));
    }

    /**
     * Triggers the trigger channel with the given channel id when required (when an update is available)
     *
     * @param channelId channel id
     */
    protected void triggerChannelIfRequired(String channelId) {
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

    protected Optional<NetatmoBridgeHandler> getBridgeHandler() {
        if (bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                bridgeHandler = (NetatmoBridgeHandler) bridge.getHandler();
            }
        }
        NetatmoBridgeHandler handler = bridgeHandler;
        return handler != null ? Optional.of(handler) : Optional.empty();
    }

    protected Optional<AbstractNetatmoThingHandler> findNAThing(@Nullable String searchedId) {
        return getBridgeHandler().flatMap(handler -> handler.findNAThing(searchedId));
    }

    public boolean matchesId(@Nullable String searchedId) {
        return searchedId != null && searchedId.equalsIgnoreCase(getId());
    }

    protected @Nullable String getId() {
        Configuration conf = config;
        Object equipmentId = conf != null ? conf.get(EQUIPMENT_ID) : null;
        if (equipmentId instanceof String) {
            return ((String) equipmentId).toLowerCase();
        }
        return null;
    }

    protected void updateProperties(@Nullable Integer firmware, @Nullable String modelId) {
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

    protected Optional<RadioHelper> getRadioHelper() {
        RadioHelper helper = radioHelper;
        return helper != null ? Optional.of(helper) : Optional.empty();
    }

    protected Optional<BatteryHelper> getBatteryHelper() {
        BatteryHelper helper = batteryHelper;
        return helper != null ? Optional.of(helper) : Optional.empty();
    }

    public void updateMeasurements() {
    }

    public void getMeasurements(@Nullable String device, @Nullable String module, String scale, List<String> types,
            List<String> channels, Map<String, Float> channelMeasurements) {
        Optional<NetatmoBridgeHandler> handler = getBridgeHandler();
        if (!handler.isPresent() || device == null) {
            return;
        }

        if (types.size() != channels.size()) {
            throw new IllegalArgumentException("types and channels lists are different sizes.");
        }

        List<Float> measurements = handler.get().getStationMeasureResponses(device, module, scale, types);
        if (measurements.size() != types.size()) {
            throw new IllegalArgumentException("types and measurements lists are different sizes.");
        }

        int i = 0;
        for (Float measurement : measurements) {
            channelMeasurements.put(channels.get(i++), measurement);
        }
    }

    public void addMeasurement(List<String> channels, List<String> types, String channel, String type) {
        if (isLinked(channel)) {
            channels.add(channel);
            types.add(type);
        }
    }
}
