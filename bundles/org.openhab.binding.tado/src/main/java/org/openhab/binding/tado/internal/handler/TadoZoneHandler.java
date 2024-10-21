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
package org.openhab.binding.tado.internal.handler;

import static org.openhab.binding.tado.internal.api.TadoApiTypeUtils.terminationConditionTemplateToTerminationCondition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tado.internal.CapabilitiesSupport;
import org.openhab.binding.tado.internal.TadoBindingConstants;
import org.openhab.binding.tado.internal.TadoBindingConstants.FanLevel;
import org.openhab.binding.tado.internal.TadoBindingConstants.HorizontalSwing;
import org.openhab.binding.tado.internal.TadoBindingConstants.OperationMode;
import org.openhab.binding.tado.internal.TadoBindingConstants.TemperatureUnit;
import org.openhab.binding.tado.internal.TadoBindingConstants.VerticalSwing;
import org.openhab.binding.tado.internal.TadoBindingConstants.ZoneType;
import org.openhab.binding.tado.internal.TadoHvacChange;
import org.openhab.binding.tado.internal.adapter.TadoZoneStateAdapter;
import org.openhab.binding.tado.internal.api.TadoApiTypeUtils;
import org.openhab.binding.tado.internal.config.TadoZoneConfig;
import org.openhab.binding.tado.swagger.codegen.api.ApiException;
import org.openhab.binding.tado.swagger.codegen.api.GsonBuilderFactory;
import org.openhab.binding.tado.swagger.codegen.api.model.ACFanLevel;
import org.openhab.binding.tado.swagger.codegen.api.model.ACHorizontalSwing;
import org.openhab.binding.tado.swagger.codegen.api.model.ACVerticalSwing;
import org.openhab.binding.tado.swagger.codegen.api.model.AcMode;
import org.openhab.binding.tado.swagger.codegen.api.model.AcModeCapabilities;
import org.openhab.binding.tado.swagger.codegen.api.model.CoolingZoneSetting;
import org.openhab.binding.tado.swagger.codegen.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.swagger.codegen.api.model.GenericZoneSetting;
import org.openhab.binding.tado.swagger.codegen.api.model.Overlay;
import org.openhab.binding.tado.swagger.codegen.api.model.OverlayTemplate;
import org.openhab.binding.tado.swagger.codegen.api.model.OverlayTerminationCondition;
import org.openhab.binding.tado.swagger.codegen.api.model.TadoSystemType;
import org.openhab.binding.tado.swagger.codegen.api.model.Zone;
import org.openhab.binding.tado.swagger.codegen.api.model.ZoneState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link TadoZoneHandler} is responsible for handling commands of zones and update their state.
 *
 * @author Dennis Frommknecht - Initial contribution
 * @author Andrew Fiddian-Green - Added Low Battery Alarm, A/C Power and Open Window channels
 *
 */
@NonNullByDefault
public class TadoZoneHandler extends BaseHomeThingHandler {
    private Logger logger = LoggerFactory.getLogger(TadoZoneHandler.class);

    private final TadoStateDescriptionProvider stateDescriptionProvider;
    private TadoZoneConfig configuration;

    private @Nullable ScheduledFuture<?> refreshTimer;
    private @Nullable ScheduledFuture<?> scheduledHvacChange;
    private @Nullable GenericZoneCapabilities capabilities;
    private @Nullable TadoHvacChange pendingHvacChange;

    private boolean disposing = false;
    private @Nullable Gson gson;

    public TadoZoneHandler(Thing thing, TadoStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        configuration = getConfigAs(TadoZoneConfig.class);
    }

    public long getZoneId() {
        return configuration.id;
    }

    public int getFallbackTimerDuration() {
        return configuration.fallbackTimerDuration;
    }

    public ZoneType getZoneType() {
        String zoneTypeStr = thing.getProperties().get(TadoBindingConstants.PROPERTY_ZONE_TYPE);
        if (zoneTypeStr == null) {
            throw new IllegalStateException("Zone type not initialized");
        }
        return ZoneType.valueOf(zoneTypeStr);
    }

    public OverlayTerminationCondition getDefaultTerminationCondition() throws IOException, ApiException {
        OverlayTemplate overlayTemplate = getApi().showZoneDefaultOverlay(getHomeId(), getZoneId());
        logApiTransaction(overlayTemplate, false);
        return terminationConditionTemplateToTerminationCondition(overlayTemplate.getTerminationCondition());
    }

    public ZoneState getZoneState() throws IOException, ApiException {
        ZoneState zoneState = getApi().showZoneState(getHomeId(), getZoneId());
        logApiTransaction(zoneState, false);
        return zoneState;
    }

    public GenericZoneCapabilities getZoneCapabilities() {
        GenericZoneCapabilities capabilities = this.capabilities;
        if (capabilities == null) {
            throw new IllegalStateException("Zone capabilities not initialized");
        }
        return capabilities;
    }

    public TemperatureUnit getTemperatureUnit() {
        return getHomeHandler().getTemperatureUnit();
    }

    public Overlay setOverlay(Overlay overlay) throws IOException, ApiException {
        try {
            logApiTransaction(overlay, true);
            Overlay newOverlay = getApi().updateZoneOverlay(getHomeId(), getZoneId(), overlay);
            logApiTransaction(newOverlay, false);
            return newOverlay;
        } catch (ApiException e) {
            if (!logger.isTraceEnabled()) {
                logger.warn("ApiException sending JSON content:\n{}", convertToJsonString(overlay));
            }
            throw e;
        }
    }

    public void removeOverlay() throws IOException, ApiException {
        logger.debug("Removing overlay of home {} and zone {}", getHomeId(), getZoneId());
        getApi().deleteZoneOverlay(getHomeId(), getZoneId());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getId();

        if (command == RefreshType.REFRESH) {
            updateZoneState(false);
            return;
        }

        synchronized (this) {
            TadoHvacChange pendingHvacChange = this.pendingHvacChange;
            if (pendingHvacChange == null) {
                throw new IllegalStateException("Zone pendingHvacChange not initialized");
            }

            switch (id) {
                case TadoBindingConstants.CHANNEL_ZONE_HVAC_MODE:
                    pendingHvacChange.withHvacMode(((StringType) command).toFullString());
                    scheduleHvacChange();
                    break;
                case TadoBindingConstants.CHANNEL_ZONE_TARGET_TEMPERATURE:
                    if (command instanceof QuantityType<?>) {
                        @SuppressWarnings("unchecked")
                        QuantityType<Temperature> state = (QuantityType<Temperature>) command;
                        QuantityType<Temperature> stateInTargetUnit = getTemperatureUnit() == TemperatureUnit.FAHRENHEIT
                                ? state.toUnit(ImperialUnits.FAHRENHEIT)
                                : state.toUnit(SIUnits.CELSIUS);

                        if (stateInTargetUnit != null) {
                            pendingHvacChange.withTemperature(stateInTargetUnit.floatValue());
                            scheduleHvacChange();
                        }
                    }
                    break;
                case TadoBindingConstants.CHANNEL_ZONE_SWING:
                    pendingHvacChange.withSwing(((OnOffType) command) == OnOffType.ON);
                    scheduleHvacChange();
                    break;
                case TadoBindingConstants.CHANNEL_ZONE_LIGHT:
                    pendingHvacChange.withLight(((OnOffType) command) == OnOffType.ON);
                    scheduleHvacChange();
                    break;
                case TadoBindingConstants.CHANNEL_ZONE_FAN_SPEED:
                    pendingHvacChange.withFanSpeed(((StringType) command).toFullString());
                    scheduleHvacChange();
                    break;
                case TadoBindingConstants.CHANNEL_ZONE_FAN_LEVEL:
                    String fanLevelString = ((StringType) command).toFullString();
                    pendingHvacChange.withFanLevel(FanLevel.valueOf(fanLevelString.toUpperCase()));
                    scheduleHvacChange();
                    break;
                case TadoBindingConstants.CHANNEL_ZONE_HORIZONTAL_SWING:
                    String horizontalSwingString = ((StringType) command).toFullString();
                    pendingHvacChange.withHorizontalSwing(HorizontalSwing.valueOf(horizontalSwingString.toUpperCase()));
                    scheduleHvacChange();
                    break;
                case TadoBindingConstants.CHANNEL_ZONE_VERTICAL_SWING:
                    String verticalSwingString = ((StringType) command).toFullString();
                    pendingHvacChange.withVerticalSwing(VerticalSwing.valueOf(verticalSwingString.toUpperCase()));
                    scheduleHvacChange();
                    break;
                case TadoBindingConstants.CHANNEL_ZONE_OPERATION_MODE:
                    String operationMode = ((StringType) command).toFullString();
                    pendingHvacChange.withOperationMode(OperationMode.valueOf(operationMode));
                    scheduleHvacChange();
                    break;
                case TadoBindingConstants.CHANNEL_ZONE_TIMER_DURATION:
                    pendingHvacChange.activeForMinutes(((DecimalType) command).intValue());
                    scheduleHvacChange();
                    break;
            }
        }
    }

    @Override
    public void initialize() {
        disposing = false;
        configuration = getConfigAs(TadoZoneConfig.class);
        if (configuration.refreshInterval <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Refresh interval of zone "
                    + getZoneId() + " of home " + getHomeId() + " must be greater than zero");
            return;
        } else if (configuration.fallbackTimerDuration <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Fallback timer duration of zone "
                    + getZoneId() + " of home " + getHomeId() + " must be greater than zero");
            return;
        } else if (configuration.hvacChangeDebounce <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "HVAC change debounce of zone "
                    + getZoneId() + " of home " + getHomeId() + " must be greater than zero");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge != null) {
            bridgeStatusChanged(bridge.getStatusInfo());
        }
    }

    @Override
    public void dispose() {
        disposing = true;
        cancelScheduledZoneStateUpdate();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            try {
                Zone zoneDetails = getApi().showZoneDetails(getHomeId(), getZoneId());
                logApiTransaction(zoneDetails, false);

                GenericZoneCapabilities capabilities = getApi().showZoneCapabilities(getHomeId(), getZoneId());
                logApiTransaction(capabilities, false);

                if (zoneDetails == null || capabilities == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Can not access zone " + getZoneId() + " of home " + getHomeId());
                    return;
                }

                updateProperty(TadoBindingConstants.PROPERTY_ZONE_NAME, zoneDetails.getName());
                updateProperty(TadoBindingConstants.PROPERTY_ZONE_TYPE, zoneDetails.getType().name());

                this.capabilities = capabilities;

                CapabilitiesSupport capabilitiesSupport = new CapabilitiesSupport(capabilities,
                        getHomeHandler().getBatteryChecker().getZone(getZoneId()));

                updateDynamicChannels(capabilitiesSupport);
            } catch (IOException | ApiException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not connect to server due to " + e.getMessage());
                cancelScheduledZoneStateUpdate();
                return;
            }

            scheduleZoneStateUpdate();
            pendingHvacChange = new TadoHvacChange(getThing());

            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            cancelScheduledZoneStateUpdate();
        }
    }

    private void updateZoneState(boolean forceUpdate) {
        if ((thing.getStatus() != ThingStatus.ONLINE) || disposing) {
            return;
        }

        getHomeHandler().updateHomeState();

        // No update during HVAC change debounce
        ScheduledFuture<?> scheduledHvacChange = this.scheduledHvacChange;
        if (!forceUpdate && scheduledHvacChange != null && !scheduledHvacChange.isDone()) {
            return;
        }

        try {
            ZoneState zoneState = getZoneState();

            logger.debug("Updating state of home {} and zone {}", getHomeId(), getZoneId());

            TadoZoneStateAdapter state = new TadoZoneStateAdapter(zoneState, getTemperatureUnit());
            updateState(TadoBindingConstants.CHANNEL_ZONE_CURRENT_TEMPERATURE, state.getInsideTemperature());
            updateState(TadoBindingConstants.CHANNEL_ZONE_HUMIDITY, state.getHumidity());

            updateState(TadoBindingConstants.CHANNEL_ZONE_HEATING_POWER, state.getHeatingPower());
            updateState(TadoBindingConstants.CHANNEL_ZONE_AC_POWER, state.getAcPower());

            updateState(TadoBindingConstants.CHANNEL_ZONE_OPERATION_MODE, state.getOperationMode());

            updateState(TadoBindingConstants.CHANNEL_ZONE_HVAC_MODE, state.getMode());
            updateState(TadoBindingConstants.CHANNEL_ZONE_TARGET_TEMPERATURE, state.getTargetTemperature());
            updateState(TadoBindingConstants.CHANNEL_ZONE_FAN_SPEED, state.getFanSpeed());
            updateState(TadoBindingConstants.CHANNEL_ZONE_SWING, state.getSwing());
            updateState(TadoBindingConstants.CHANNEL_ZONE_LIGHT, state.getLight());
            updateState(TadoBindingConstants.CHANNEL_ZONE_FAN_LEVEL, state.getFanLevel());
            updateState(TadoBindingConstants.CHANNEL_ZONE_HORIZONTAL_SWING, state.getHorizontalSwing());
            updateState(TadoBindingConstants.CHANNEL_ZONE_VERTICAL_SWING, state.getVerticalSwing());

            updateState(TadoBindingConstants.CHANNEL_ZONE_TIMER_DURATION, state.getRemainingTimerDuration());

            updateState(TadoBindingConstants.CHANNEL_ZONE_OVERLAY_EXPIRY, state.getOverlayExpiration());

            updateState(TadoBindingConstants.CHANNEL_ZONE_OPEN_WINDOW_DETECTED, state.getOpenWindowDetected());

            updateState(TadoBindingConstants.CHANNEL_ZONE_OPEN_WINDOW_REMAINING_TIME,
                    state.getOpenWindowRemainingTime());

            updateDynamicStateDescriptions(zoneState);

            onSuccessfulOperation();
        } catch (IOException | ApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not connect to server due to " + e.getMessage());
        }

        updateState(TadoBindingConstants.CHANNEL_ZONE_BATTERY_LOW_ALARM,
                getHomeHandler().getBatteryChecker().getBatteryLowAlarm(getZoneId()));
    }

    /**
     * Update the dynamic state descriptions for any channels which support an unknown sub- range of enumerator setting
     * values, based on the list of capabilities reported by the respective zone.
     *
     * Note: currently this only applies to A/C devices that support fanLevel, horizontalSwing, or verticalSwing.
     *
     * @param zoneState the current zone Thing's state
     */
    private void updateDynamicStateDescriptions(ZoneState zoneState) {
        GenericZoneSetting setting = zoneState.getSetting();
        if (setting.getType() != TadoSystemType.AIR_CONDITIONING) {
            return;
        }

        AcMode acMode = ((CoolingZoneSetting) setting).getMode();
        AcModeCapabilities acModeCapabilities = acMode == null ? new AcModeCapabilities()
                : TadoApiTypeUtils.getModeCapabilities(acMode, capabilities);

        // update the options list of supported fan levels
        Channel channel = thing.getChannel(TadoBindingConstants.CHANNEL_ZONE_FAN_LEVEL);
        if (channel != null) {
            List<ACFanLevel> fanLevels = acModeCapabilities.getFanLevel();
            if (fanLevels != null) {
                stateDescriptionProvider.setStateOptions(channel.getUID(),
                        fanLevels.stream().map(u -> new StateOption(u.name(), u.name())).collect(Collectors.toList()));
            }
        }

        // update the options list of supported horizontal swing settings
        channel = thing.getChannel(TadoBindingConstants.CHANNEL_ZONE_HORIZONTAL_SWING);
        if (channel != null) {
            List<ACHorizontalSwing> horizontalSwings = acModeCapabilities.getHorizontalSwing();
            if (horizontalSwings != null) {
                stateDescriptionProvider.setStateOptions(channel.getUID(), horizontalSwings.stream()
                        .map(u -> new StateOption(u.name(), u.name())).collect(Collectors.toList()));
            }
        }

        // update the options list of supported vertical swing settings
        channel = thing.getChannel(TadoBindingConstants.CHANNEL_ZONE_VERTICAL_SWING);
        if (channel != null) {
            List<ACVerticalSwing> verticalSwings = acModeCapabilities.getVerticalSwing();
            if (verticalSwings != null) {
                stateDescriptionProvider.setStateOptions(channel.getUID(), verticalSwings.stream()
                        .map(u -> new StateOption(u.name(), u.name())).collect(Collectors.toList()));
            }
        }
    }

    private void scheduleZoneStateUpdate() {
        ScheduledFuture<?> refreshTimer = this.refreshTimer;
        if (refreshTimer == null || refreshTimer.isCancelled()) {
            this.refreshTimer = scheduler.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    updateZoneState(false);
                }
            }, 5, configuration.refreshInterval, TimeUnit.SECONDS);
        }
    }

    private void cancelScheduledZoneStateUpdate() {
        ScheduledFuture<?> refreshTimer = this.refreshTimer;
        if (refreshTimer != null) {
            refreshTimer.cancel(false);
        }
    }

    private void scheduleHvacChange() {
        ScheduledFuture<?> scheduledHvacChange = this.scheduledHvacChange;
        if (scheduledHvacChange != null) {
            scheduledHvacChange.cancel(false);
        }
        this.scheduledHvacChange = scheduler.schedule(() -> {
            try {
                synchronized (this) {
                    TadoHvacChange pendingHvacChange = this.pendingHvacChange;
                    this.pendingHvacChange = new TadoHvacChange(getThing());
                    if (pendingHvacChange != null) {
                        pendingHvacChange.apply();
                    }
                }
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } catch (ApiException e) {
                logger.warn("Could not apply HVAC change on home {} and zone {}: {}", getHomeId(), getZoneId(),
                        e.getMessage(), e);
            } finally {
                updateZoneState(true);
            }
        }, configuration.hvacChangeDebounce, TimeUnit.SECONDS);
    }

    /**
     * Helper method to log an API transaction on the given object.
     * If the logger level is 'debug', the transaction is simply logged.
     * If the logger level is 'trace, the object's JSON serial contents are included.
     *
     * @param object the object to be logged.
     * @param isCommand marks whether the transaction is a command to, or a response from, the server.
     */
    private void logApiTransaction(Object object, boolean isCommand) {
        if (logger.isDebugEnabled() || logger.isTraceEnabled()) {
            String logType = isCommand ? "command" : "response";
            if (logger.isTraceEnabled()) {
                logger.trace("Api {}: homeId:{}, zoneId:{}, objectId:{}, content:\n{}", logType, getHomeId(),
                        getZoneId(), object.getClass().getSimpleName(), convertToJsonString(object));
            } else if (logger.isDebugEnabled()) {
                logger.debug("Api {}: homeId:{}, zoneId:{}, objectId:{}", logType, getHomeId(), getZoneId(),
                        object.getClass().getSimpleName());
            }
        }
    }

    private synchronized String convertToJsonString(Object object) {
        Gson gson = this.gson;
        if (gson == null) {
            gson = this.gson = GsonBuilderFactory.defaultGsonBuilder().setPrettyPrinting().create();
        }
        return gson.toJson(object);
    }

    /**
     * If the given channel exists in the thing, but is NOT required in the thing, then add it to a list of channels to
     * be removed. Or if the channel does NOT exist in the thing, but is required in the thing, then log a warning.
     *
     * @param removeList the list of channels to be removed from the thing.
     * @param channelId the id of the channel to be (eventually) removed.
     * @param channelRequired true if the thing requires this channel.
     */
    private void removeListProcessChannel(List<Channel> removeList, String channelId, boolean channelRequired) {
        Channel channel = thing.getChannel(channelId);
        if (!channelRequired && channel != null) {
            removeList.add(channel);
        } else if (channelRequired && channel == null) {
            logger.warn("Thing {} does not have a '{}' channel => please reinitialize it", thing.getUID(), channelId);
        }
    }

    /**
     * Remove previously statically created channels if the device does not support them.
     *
     * @param capabilitiesSupport a CapabilitiesSupport instance which summarizes the device's capabilities.
     * @throws IllegalStateException if any of the channel builders failed.
     */
    private void updateDynamicChannels(CapabilitiesSupport capabilitiesSupport) {
        List<Channel> removeList = new ArrayList<>();

        removeListProcessChannel(removeList, TadoBindingConstants.CHANNEL_ZONE_BATTERY_LOW_ALARM,
                capabilitiesSupport.batteryLowAlarm());
        removeListProcessChannel(removeList, TadoBindingConstants.CHANNEL_ZONE_OPEN_WINDOW_DETECTED,
                capabilitiesSupport.openWindow());
        removeListProcessChannel(removeList, TadoBindingConstants.CHANNEL_ZONE_OPEN_WINDOW_REMAINING_TIME,
                capabilitiesSupport.openWindow());
        removeListProcessChannel(removeList, TadoBindingConstants.CHANNEL_ZONE_LIGHT, capabilitiesSupport.light());
        removeListProcessChannel(removeList, TadoBindingConstants.CHANNEL_ZONE_HORIZONTAL_SWING,
                capabilitiesSupport.horizontalSwing());
        removeListProcessChannel(removeList, TadoBindingConstants.CHANNEL_ZONE_VERTICAL_SWING,
                capabilitiesSupport.verticalSwing());
        removeListProcessChannel(removeList, TadoBindingConstants.CHANNEL_ZONE_SWING, capabilitiesSupport.swing());
        removeListProcessChannel(removeList, TadoBindingConstants.CHANNEL_ZONE_FAN_SPEED,
                capabilitiesSupport.fanSpeed());
        removeListProcessChannel(removeList, TadoBindingConstants.CHANNEL_ZONE_FAN_LEVEL,
                capabilitiesSupport.fanLevel());
        removeListProcessChannel(removeList, TadoBindingConstants.CHANNEL_ZONE_AC_POWER, capabilitiesSupport.acPower());
        removeListProcessChannel(removeList, TadoBindingConstants.CHANNEL_ZONE_HEATING_POWER,
                capabilitiesSupport.heatingPower());
        removeListProcessChannel(removeList, TadoBindingConstants.CHANNEL_ZONE_HUMIDITY,
                capabilitiesSupport.humidity());
        removeListProcessChannel(removeList, TadoBindingConstants.CHANNEL_ZONE_CURRENT_TEMPERATURE,
                capabilitiesSupport.currentTemperature());

        if (!removeList.isEmpty()) {
            if (logger.isDebugEnabled()) {
                StringJoiner joiner = new StringJoiner(", ");
                removeList.forEach(c -> joiner.add(c.getUID().getId()));
                logger.debug("Removing unsupported channels for {}: {}", thing.getUID(), joiner.toString());
            }
            updateThing(editThing().withoutChannels(removeList).build());
        }
    }
}
