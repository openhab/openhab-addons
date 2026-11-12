/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.remehaheating.internal;

import static org.openhab.binding.remehaheating.internal.RemehaHeatingBindingConstants.*;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.remehaheating.internal.api.RemehaApiClient;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * The {@link RemehaHeatingHandler} handles communication with Remeha Home heating systems.
 *
 * This handler manages the connection to the Remeha API, authenticates using OAuth2 PKCE flow,
 * and provides access to heating system data including temperatures, water pressure, and DHW controls.
 *
 * Supported features:
 * - Room and outdoor temperature monitoring
 * - Target temperature control
 * - Hot water temperature and status monitoring
 * - DHW mode control (anti-frost, schedule, continuous-comfort)
 * - Water pressure monitoring
 * - System status monitoring
 * - Heating curve monitoring (slope, base setpoint, maximum flow temperature)
 *
 * @author Michael Fraedrich - Initial contribution
 * @author Michael Fraedrich - Added heating curve and additional read-only dashboard channels
 */
@NonNullByDefault
public class RemehaHeatingHandler extends BaseThingHandler {

    private static final Instant NOT_SET_TIMESTAMP = Instant.parse("0002-01-01T00:00:00Z");

    private final Logger logger = LoggerFactory.getLogger(RemehaHeatingHandler.class);
    private final HttpClient httpClient;
    private @Nullable RemehaApiClient apiClient;
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable String cachedClimateZoneId;
    private @Nullable String cachedHotWaterZoneId;

    public RemehaHeatingHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    /**
     * Handles commands sent to the binding channels.
     *
     * Supported commands:
     * - RefreshType: Updates all channel states from API
     * - QuantityType/DecimalType on target-temperature: Sets new target room temperature
     * - StringType on dhw-mode: Changes DHW mode (anti-frost/schedule/continuous-comfort)
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateData();
        } else if (CHANNEL_TARGET_TEMPERATURE.equals(channelUID.getId())) {
            if (command instanceof QuantityType<?> qt) {
                QuantityType<?> celsius = qt.toUnit(SIUnits.CELSIUS);
                if (celsius != null) {
                    setTargetTemperature(celsius.doubleValue());
                }
            } else if (command instanceof DecimalType dt) {
                setTargetTemperature(dt.doubleValue());
            }
        } else if (CHANNEL_DHW_MODE.equals(channelUID.getId()) && command instanceof StringType) {
            setDhwMode(command.toString());
        }
    }

    /**
     * Initializes the handler by validating configuration and authenticating with Remeha API.
     * Sets up periodic data refresh job on successful authentication.
     */
    @Override
    public void initialize() {
        try {
            RemehaHeatingConfiguration config = getConfigAs(RemehaHeatingConfiguration.class);
            String email = config.email;
            String password = config.password;
            int refreshInterval = config.refreshInterval;

            if (email.isBlank() || password.isBlank()) {
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error-no-credentials");
                return;
            }

            updateStatus(ThingStatus.UNKNOWN);
            apiClient = new RemehaApiClient(httpClient);

            scheduler.execute(() -> authenticateAndStart(email, password, refreshInterval));
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid configuration", e);
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error-invalid-config");
        }
    }

    private void authenticateAndStart(String email, String password, int refreshInterval) {
        try {
            RemehaApiClient client = apiClient;
            if (client != null && client.authenticate(email, password)) {
                updateStatus(ThingStatus.ONLINE);
                startRefreshJob(refreshInterval > 0 ? refreshInterval : 60);
            } else {
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.comm-error-authentication-failed");
            }
        } catch (RuntimeException e) {
            logger.debug("Authentication error", e);
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.comm-error-authentication-error");
        }
    }

    /**
     * Cleans up resources when the handler is disposed.
     * Stops the refresh job and the HTTP client.
     */
    @Override
    public void dispose() {
        stopRefreshJob();
        apiClient = null;
        cachedClimateZoneId = null;
        cachedHotWaterZoneId = null;
        try {
            if (httpClient.isStarted()) {
                httpClient.stop();
            }
        } catch (Exception e) {
            logger.debug("Error stopping HTTP client", e);
        }
        super.dispose();
    }

    /**
     * Starts the periodic data refresh job.
     *
     * @param intervalSeconds Refresh interval in seconds
     */
    private void startRefreshJob(int intervalSeconds) {
        refreshJob = scheduler.scheduleWithFixedDelay(this::updateData, 0, intervalSeconds, TimeUnit.SECONDS);
    }

    /**
     * Stops the periodic data refresh job if running.
     */
    private void stopRefreshJob() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
            refreshJob = null;
        }
    }

    /**
     * Fetches latest data from Remeha API and updates all channel states.
     *
     * If the dashboard request fails, the handler attempts re-authentication before
     * setting the thing status to OFFLINE.
     */
    private void updateData() {
        RemehaApiClient client = apiClient;
        if (client == null) {
            return;
        }

        try {
            JsonObject dashboard = client.getDashboard();
            if (dashboard == null) {
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.comm-error-data-fetch-failed");
                return;
            }

            updateStatus(ThingStatus.ONLINE);
            updateChannelsFromDashboard(dashboard);
            updateHeatingCurve(client);
        } catch (RuntimeException e) {
            // An uncaught exception would silently cancel the scheduled refresh job
            logger.debug("Error updating data", e);
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.comm-error-data-update-failed");
        }
    }

    /**
     * Updates all channel states from the dashboard JSON data and caches zone IDs.
     *
     * @param dashboard the dashboard JSON response
     */
    void updateChannelsFromDashboard(JsonObject dashboard) {
        JsonArray appliances = dashboard.getAsJsonArray("appliances");
        if (appliances == null || appliances.isEmpty()) {
            return;
        }

        JsonObject appliance = appliances.get(0).getAsJsonObject();

        updateQuantity(appliance, "waterPressure", CHANNEL_WATER_PRESSURE, Units.BAR);
        updateString(appliance, "errorStatus", CHANNEL_STATUS);
        updateBoolean(appliance, "waterPressureOK", CHANNEL_WATER_PRESSURE_OK);

        JsonElement outdoorInfo = appliance.get("outdoorTemperatureInformation");
        if (outdoorInfo != null && outdoorInfo.isJsonObject()) {
            updateQuantity(outdoorInfo.getAsJsonObject(), "internetOutdoorTemperature", CHANNEL_OUTDOOR_TEMPERATURE,
                    SIUnits.CELSIUS);
        }

        updateString(appliance, "activeThermalMode", CHANNEL_THERMAL_MODE);
        updateString(appliance, "operatingMode", CHANNEL_OPERATING_MODE);
        updateString(appliance, "seasonalMode", CHANNEL_SEASONAL_MODE);
        JsonElement holidaySchedule = appliance.get("holidaySchedule");
        if (holidaySchedule != null && holidaySchedule.isJsonObject()) {
            updateBoolean(holidaySchedule.getAsJsonObject(), "active", CHANNEL_HOLIDAY_ACTIVE);
        } else {
            updateState(CHANNEL_HOLIDAY_ACTIVE, UnDefType.UNDEF);
        }

        // Climate zones
        JsonArray climateZones = appliance.getAsJsonArray("climateZones");
        if (climateZones != null && !climateZones.isEmpty()) {
            JsonObject zone = climateZones.get(0).getAsJsonObject();
            updateQuantity(zone, "roomTemperature", CHANNEL_ROOM_TEMPERATURE, SIUnits.CELSIUS);
            updateQuantity(zone, "setPoint", CHANNEL_TARGET_TEMPERATURE, SIUnits.CELSIUS);

            updateString(zone, "zoneMode", CHANNEL_ZONE_MODE);
            updateString(zone, "activeComfortDemand", CHANNEL_HEATING_DEMAND);
            updateQuantity(zone, "nextSetpoint", CHANNEL_NEXT_SETPOINT, SIUnits.CELSIUS);
            updateDateTime(zone, "nextSwitchTime", CHANNEL_NEXT_SWITCH_TIME);
            updateBoolean(zone, "firePlaceModeActive", CHANNEL_FIREPLACE_MODE_ACTIVE);

            // Cache climate zone ID
            JsonPrimitive climateZoneId = getPrimitive(zone, "climateZoneId");
            if (climateZoneId != null) {
                cachedClimateZoneId = climateZoneId.getAsString();
            }
        }

        // Hot water zones
        JsonArray hotWaterZones = appliance.getAsJsonArray("hotWaterZones");
        if (hotWaterZones != null && !hotWaterZones.isEmpty()) {
            JsonObject zone = hotWaterZones.get(0).getAsJsonObject();
            updateQuantity(zone, "dhwTemperature", CHANNEL_DHW_TEMPERATURE, SIUnits.CELSIUS);
            updateQuantity(zone, "targetSetpoint", CHANNEL_DHW_TARGET, SIUnits.CELSIUS);
            updateString(zone, "dhwZoneMode", CHANNEL_DHW_MODE);
            updateString(zone, "dhwStatus", CHANNEL_DHW_STATUS);

            updateDateTime(zone, "nextSwitchTime", CHANNEL_DHW_NEXT_SWITCH_TIME);
            updateBoolean(zone, "antiLegionellaEnabled", CHANNEL_DHW_ANTI_LEGIONELLA);

            // Cache hot water zone ID
            JsonPrimitive hotWaterZoneId = getPrimitive(zone, "hotWaterZoneId");
            if (hotWaterZoneId != null) {
                cachedHotWaterZoneId = hotWaterZoneId.getAsString();
            }
        }
    }

    /**
     * Not every installation provides a heating curve. If it cannot be retrieved, the channels keep their state and
     * the Thing status is not affected.
     */
    private void updateHeatingCurve(RemehaApiClient client) {
        String climateZoneId = cachedClimateZoneId;
        if (climateZoneId == null) {
            return;
        }
        JsonObject curve = client.getHeatingCurve(climateZoneId);
        if (curve == null) {
            return;
        }
        JsonPrimitive slope = getPrimitive(curve, "slope");
        updateState(CHANNEL_HEATING_CURVE_SLOPE,
                slope == null || !slope.isNumber() ? UnDefType.UNDEF : new DecimalType(slope.getAsDouble()));
        updateQuantity(curve, "baseSetpoint", CHANNEL_HEATING_CURVE_BASE_SETPOINT, SIUnits.CELSIUS);
        updateQuantity(curve, "maxFlowTemperature", CHANNEL_HEATING_CURVE_MAX_FLOW_TEMPERATURE, SIUnits.CELSIUS);
    }

    /**
     * Returns the value of the given key if it is a JSON primitive. Missing keys and JSON {@code null} values, which
     * the Remeha API uses for unavailable values (e.g. no hot water temperature on some appliances), return null.
     */
    private @Nullable JsonPrimitive getPrimitive(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsJsonPrimitive() : null;
    }

    private void updateString(JsonObject object, String key, String channelId) {
        JsonPrimitive primitive = getPrimitive(object, key);
        updateState(channelId, primitive == null ? UnDefType.UNDEF : new StringType(primitive.getAsString()));
    }

    private void updateBoolean(JsonObject object, String key, String channelId) {
        JsonPrimitive primitive = getPrimitive(object, key);
        updateState(channelId, primitive == null || !primitive.isBoolean() ? UnDefType.UNDEF
                : OnOffType.from(primitive.getAsBoolean()));
    }

    private <Q extends Quantity<Q>> void updateQuantity(JsonObject object, String key, String channelId, Unit<Q> unit) {
        JsonPrimitive primitive = getPrimitive(object, key);
        updateState(channelId, primitive == null || !primitive.isNumber() ? UnDefType.UNDEF
                : new QuantityType<>(primitive.getAsDouble(), unit));
    }

    /** The API uses {@code 0001-01-01} timestamps for "not set", which are mapped to UNDEF. */
    private void updateDateTime(JsonObject object, String key, String channelId) {
        JsonPrimitive primitive = getPrimitive(object, key);
        State state = UnDefType.UNDEF;
        if (primitive != null && primitive.isString()) {
            try {
                Instant instant = Instant.parse(primitive.getAsString());
                if (instant.isAfter(NOT_SET_TIMESTAMP)) {
                    state = new DateTimeType(instant);
                }
            } catch (DateTimeParseException e) {
                logger.debug("Cannot parse timestamp '{}' of '{}'", primitive.getAsString(), key);
            }
        }
        updateState(channelId, state);
    }

    /**
     * Sets the target room temperature via API.
     *
     * @param temperature Target temperature in Celsius
     */
    private void setTargetTemperature(double temperature) {
        RemehaApiClient client = apiClient;
        if (client == null) {
            return;
        }

        String climateZoneId = cachedClimateZoneId;
        if (climateZoneId == null) {
            logger.debug("Climate zone ID not cached, fetching from dashboard");
            JsonObject dashboard = client.getDashboard();
            if (dashboard != null) {
                updateChannelsFromDashboard(dashboard);
                climateZoneId = cachedClimateZoneId;
            }
        }
        if (climateZoneId == null) {
            logger.debug("Climate zone ID not available, cannot set temperature");
            return;
        }

        if (!client.setTemperature(climateZoneId, temperature)) {
            logger.debug("Failed to set target temperature to {}", temperature);
        }
    }

    /**
     * Sets the DHW (Domestic Hot Water) mode via API.
     *
     * @param mode DHW mode: "anti-frost", "schedule", or "continuous-comfort"
     */
    private void setDhwMode(String mode) {
        RemehaApiClient client = apiClient;
        if (client == null) {
            return;
        }

        String hotWaterZoneId = cachedHotWaterZoneId;
        if (hotWaterZoneId == null) {
            logger.debug("Hot water zone ID not cached, fetching from dashboard");
            JsonObject dashboard = client.getDashboard();
            if (dashboard != null) {
                updateChannelsFromDashboard(dashboard);
                hotWaterZoneId = cachedHotWaterZoneId;
            }
        }
        if (hotWaterZoneId == null) {
            logger.debug("Hot water zone ID not available, cannot set DHW mode");
            return;
        }

        if (!client.setDhwMode(hotWaterZoneId, mode)) {
            logger.debug("Failed to set DHW mode to {}", mode);
        }
    }
}
