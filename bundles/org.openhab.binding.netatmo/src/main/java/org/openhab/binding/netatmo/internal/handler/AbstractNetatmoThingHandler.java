/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.*;

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
import org.openhab.binding.netatmo.internal.channelhelper.BatteryHelper;
import org.openhab.binding.netatmo.internal.channelhelper.RadioHelper;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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
    public static final Unit<Dimensionless> API_HUMIDITY_UNIT = Units.PERCENT;
    public static final Unit<Pressure> API_PRESSURE_UNIT = HECTO(SIUnits.PASCAL);
    public static final Unit<Speed> API_WIND_SPEED_UNIT = SIUnits.KILOMETRE_PER_HOUR;
    public static final Unit<Angle> API_WIND_DIRECTION_UNIT = Units.DEGREE_ANGLE;
    public static final Unit<Length> API_RAIN_UNIT = MILLI(SIUnits.METRE);
    public static final Unit<Dimensionless> API_CO2_UNIT = Units.PARTS_PER_MILLION;
    public static final Unit<Dimensionless> API_NOISE_UNIT = Units.DECIBEL;

    private final Logger logger = LoggerFactory.getLogger(AbstractNetatmoThingHandler.class);

    protected final TimeZoneProvider timeZoneProvider;
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

                String signalLevel = thing.getProperties().get(PROPERTY_SIGNAL_LEVELS);
                radioHelper = signalLevel != null ? new RadioHelper(signalLevel) : null;
                String batteryLevel = thing.getProperties().get(PROPERTY_BATTERY_LEVELS);
                batteryHelper = batteryLevel != null ? new BatteryHelper(batteryLevel) : null;
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
        Optional<State> result = getBatteryHelper().flatMap(helper -> helper.getNAThingProperty(channelId));
        if (result.isPresent()) {
            return result.get();
        }
        result = getRadioHelper().flatMap(helper -> helper.getNAThingProperty(channelId));
        if (result.isPresent()) {
            return result.get();
        }
        return UnDefType.UNDEF;
    }

    protected void updateChannels() {
        if (thing.getStatus() != ThingStatus.ONLINE) {
            return;
        }

        updateDataChannels();

        triggerEventChannels();
    }

    private void updateDataChannels() {
        getThing().getChannels().stream()
                .filter(channel -> !ChannelKind.TRIGGER.equals(channel.getKind()) && isLinked(channel.getUID()))
                .map(channel -> channel.getUID()).forEach(this::updateChannel);
    }

    private void updateChannel(ChannelUID channelUID) {
        updateState(channelUID, getNAThingProperty(channelUID.getId()));
    }

    /**
     * Triggers all event/trigger channels
     * (when a channel is triggered, a rule can get all other information from the updated non-trigger channels)
     */
    private void triggerEventChannels() {
        getThing().getChannels().stream().filter(channel -> ChannelKind.TRIGGER.equals(channel.getKind()))
                .map(channel -> channel.getUID().getId()).forEach(this::triggerChannelIfRequired);
    }

    /**
     * Triggers the trigger channel with the given channel id when required (when an update is available)
     *
     * @param channelId channel id
     */
    protected void triggerChannelIfRequired(String channelId) {
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing '{}'", channelUID);
            updateChannel(channelUID);
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

    protected boolean isReachable() {
        return true;
    }
}
