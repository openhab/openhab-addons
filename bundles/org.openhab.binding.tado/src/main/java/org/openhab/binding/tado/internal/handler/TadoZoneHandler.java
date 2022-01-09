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
package org.openhab.binding.tado.internal.handler;

import static org.openhab.binding.tado.internal.api.TadoApiTypeUtils.terminationConditionTemplateToTerminationCondition;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tado.internal.TadoBindingConstants;
import org.openhab.binding.tado.internal.TadoBindingConstants.OperationMode;
import org.openhab.binding.tado.internal.TadoBindingConstants.TemperatureUnit;
import org.openhab.binding.tado.internal.TadoBindingConstants.ZoneType;
import org.openhab.binding.tado.internal.TadoHvacChange;
import org.openhab.binding.tado.internal.adapter.TadoZoneStateAdapter;
import org.openhab.binding.tado.internal.api.ApiException;
import org.openhab.binding.tado.internal.api.client.HomeApi;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.Overlay;
import org.openhab.binding.tado.internal.api.model.OverlayTemplate;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationCondition;
import org.openhab.binding.tado.internal.api.model.Zone;
import org.openhab.binding.tado.internal.api.model.ZoneState;
import org.openhab.binding.tado.internal.config.TadoZoneConfig;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TadoZoneHandler} is responsible for handling commands of zones and update their state.
 *
 * @author Dennis Frommknecht - Initial contribution
 * @author Andrew Fiddian-Green - Added Low Battery Alarm, A/C Power and Open Window channels
 *
 */
public class TadoZoneHandler extends BaseHomeThingHandler {
    private Logger logger = LoggerFactory.getLogger(TadoZoneHandler.class);

    private TadoZoneConfig configuration;
    private ScheduledFuture<?> refreshTimer;
    private ScheduledFuture<?> scheduledHvacChange;
    private GenericZoneCapabilities capabilities;
    TadoHvacChange pendingHvacChange;

    public TadoZoneHandler(Thing thing) {
        super(thing);
    }

    public long getZoneId() {
        return this.configuration.id;
    }

    public int getFallbackTimerDuration() {
        return this.configuration.fallbackTimerDuration;
    }

    public @Nullable ZoneType getZoneType() {
        String zoneTypeStr = this.thing.getProperties().get(TadoBindingConstants.PROPERTY_ZONE_TYPE);
        return zoneTypeStr != null ? ZoneType.valueOf(zoneTypeStr) : null;
    }

    public OverlayTerminationCondition getDefaultTerminationCondition() throws IOException, ApiException {
        OverlayTemplate overlayTemplate = getApi().showZoneDefaultOverlay(getHomeId(), getZoneId());
        return terminationConditionTemplateToTerminationCondition(overlayTemplate.getTerminationCondition());
    }

    public ZoneState getZoneState() throws IOException, ApiException {
        HomeApi api = getApi();
        return api != null ? api.showZoneState(getHomeId(), getZoneId()) : null;
    }

    public GenericZoneCapabilities getZoneCapabilities() {
        return this.capabilities;
    }

    public TemperatureUnit getTemperatureUnit() {
        return getHomeHandler().getTemperatureUnit();
    }

    public Overlay setOverlay(Overlay overlay) throws IOException, ApiException {
        logger.debug("Setting overlay of home {} and zone {}", getHomeId(), getZoneId());
        return getApi().updateZoneOverlay(getHomeId(), getZoneId(), overlay);
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

        switch (id) {
            case TadoBindingConstants.CHANNEL_ZONE_HVAC_MODE:
                pendingHvacChange.withHvacMode(((StringType) command).toFullString());
                scheduleHvacChange();
                break;
            case TadoBindingConstants.CHANNEL_ZONE_TARGET_TEMPERATURE:
                QuantityType<Temperature> state = (QuantityType<Temperature>) command;
                QuantityType<Temperature> stateInTargetUnit = getTemperatureUnit() == TemperatureUnit.FAHRENHEIT
                        ? state.toUnit(ImperialUnits.FAHRENHEIT)
                        : state.toUnit(SIUnits.CELSIUS);

                if (stateInTargetUnit != null) {
                    pendingHvacChange.withTemperature(stateInTargetUnit.floatValue());
                    scheduleHvacChange();
                }

                break;
            case TadoBindingConstants.CHANNEL_ZONE_SWING:
                pendingHvacChange.withSwing(((OnOffType) command) == OnOffType.ON);
                scheduleHvacChange();
                break;
            case TadoBindingConstants.CHANNEL_ZONE_FAN_SPEED:
                pendingHvacChange.withFanSpeed(((StringType) command).toFullString());
                scheduleHvacChange();
                break;
            case TadoBindingConstants.CHANNEL_ZONE_OPERATION_MODE:
                String operationMode = ((StringType) command).toFullString();
                pendingHvacChange.withOperationMode(OperationMode.valueOf(operationMode));
                scheduleHvacChange();
                break;
            case TadoBindingConstants.CHANNEL_ZONE_TIMER_DURATION:
                pendingHvacChange.activeFor(((DecimalType) command).intValue());
                scheduleHvacChange();
                break;
        }
    }

    @Override
    public void initialize() {
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
        cancelScheduledZoneStateUpdate();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            try {
                Zone zoneDetails = getApi().showZoneDetails(getHomeId(), getZoneId());
                GenericZoneCapabilities capabilities = getApi().showZoneCapabilities(getHomeId(), getZoneId());

                if (zoneDetails == null || capabilities == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Can not access zone " + getZoneId() + " of home " + getHomeId());
                    return;
                }

                updateProperty(TadoBindingConstants.PROPERTY_ZONE_NAME, zoneDetails.getName());
                updateProperty(TadoBindingConstants.PROPERTY_ZONE_TYPE, zoneDetails.getType().name());
                this.capabilities = capabilities;
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
        TadoHomeHandler home = getHomeHandler();
        if (home != null) {
            home.updateHomeState();
        }

        // No update during HVAC change debounce
        if (!forceUpdate && scheduledHvacChange != null && !scheduledHvacChange.isDone()) {
            return;
        }

        try {
            ZoneState zoneState = getZoneState();

            if (zoneState == null) {
                return;
            }

            logger.debug("Updating state of home {} and zone {}", getHomeId(), getZoneId());

            TadoZoneStateAdapter state = new TadoZoneStateAdapter(zoneState, getTemperatureUnit());
            updateStateIfNotNull(TadoBindingConstants.CHANNEL_ZONE_CURRENT_TEMPERATURE, state.getInsideTemperature());
            updateStateIfNotNull(TadoBindingConstants.CHANNEL_ZONE_HUMIDITY, state.getHumidity());

            updateStateIfNotNull(TadoBindingConstants.CHANNEL_ZONE_HEATING_POWER, state.getHeatingPower());
            updateStateIfNotNull(TadoBindingConstants.CHANNEL_ZONE_AC_POWER, state.getAcPower());

            updateState(TadoBindingConstants.CHANNEL_ZONE_OPERATION_MODE, state.getOperationMode());

            updateState(TadoBindingConstants.CHANNEL_ZONE_HVAC_MODE, state.getMode());
            updateState(TadoBindingConstants.CHANNEL_ZONE_TARGET_TEMPERATURE, state.getTargetTemperature());
            updateState(TadoBindingConstants.CHANNEL_ZONE_FAN_SPEED, state.getFanSpeed());
            updateState(TadoBindingConstants.CHANNEL_ZONE_SWING, state.getSwing());

            updateState(TadoBindingConstants.CHANNEL_ZONE_TIMER_DURATION, state.getRemainingTimerDuration());

            updateState(TadoBindingConstants.CHANNEL_ZONE_OVERLAY_EXPIRY, state.getOverlayExpiration());

            updateState(TadoBindingConstants.CHANNEL_ZONE_OPEN_WINDOW_DETECTED, state.getOpenWindowDetected());

            onSuccessfulOperation();
        } catch (IOException | ApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not connect to server due to " + e.getMessage());
        }

        if (home != null) {
            updateState(TadoBindingConstants.CHANNEL_ZONE_BATTERY_LOW_ALARM, home.getBatteryLowAlarm(getZoneId()));
        }
    }

    private void scheduleZoneStateUpdate() {
        if (refreshTimer == null || refreshTimer.isCancelled()) {
            refreshTimer = scheduler.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    updateZoneState(false);
                }
            }, 5, configuration.refreshInterval, TimeUnit.SECONDS);
        }
    }

    private void cancelScheduledZoneStateUpdate() {
        if (refreshTimer != null) {
            refreshTimer.cancel(false);
        }
    }

    private void scheduleHvacChange() {
        if (scheduledHvacChange != null) {
            scheduledHvacChange.cancel(false);
        }

        scheduledHvacChange = scheduler.schedule(() -> {
            try {
                TadoHvacChange change = this.pendingHvacChange;
                this.pendingHvacChange = new TadoHvacChange(getThing());
                change.apply();
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

    private void updateStateIfNotNull(String channelID, State state) {
        if (state != null) {
            updateState(channelID, state);
        }
    }
}
