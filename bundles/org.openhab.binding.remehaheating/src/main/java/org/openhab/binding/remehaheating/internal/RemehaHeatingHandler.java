/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.remehaheating.internal.api.RemehaApiClient;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
 *
 * @author Michael Fraedrich - Initial contribution
 */
@NonNullByDefault
public class RemehaHeatingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RemehaHeatingHandler.class);
    private final HttpClient httpClient;
    private @Nullable RemehaApiClient apiClient;
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable RemehaHeatingConfiguration config;

    public RemehaHeatingHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    /**
     * Handles commands sent to the binding channels.
     * 
     * Supported commands:
     * - RefreshType: Updates all channel states from API
     * - DecimalType on targetTemperature: Sets new target room temperature
     * - StringType on dhwMode: Changes DHW mode (anti-frost/schedule/continuous-comfort)
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
            config = getConfigAs(RemehaHeatingConfiguration.class);
            String email = config.email;
            String password = config.password;
            int refreshInterval = config.refreshInterval;

            if (email.isBlank() || password.isBlank()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-no-credentials");
                return;
            }

            updateStatus(ThingStatus.UNKNOWN);
            apiClient = new RemehaApiClient(httpClient);

            scheduler.execute(() -> authenticateAndStart(email, password, refreshInterval));
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid configuration", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-invalid-config");
        }
    }

    private void authenticateAndStart(String email, String password, int refreshInterval) {
        try {
            RemehaApiClient client = apiClient;
            if (client != null && client.authenticate(email, password)) {
                updateStatus(ThingStatus.ONLINE);
                startRefreshJob(refreshInterval > 0 ? refreshInterval : 60);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.comm-error-authentication-failed");
            }
        } catch (RuntimeException e) {
            logger.debug("Authentication error", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-authentication-error");
        }
    }

    /**
     * Cleans up resources when the handler is disposed.
     * Stops the refresh job.
     */
    @Override
    public void dispose() {
        stopRefreshJob();
        apiClient = null;
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
        if (refreshJob != null) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    /**
     * Fetches latest data from Remeha API and updates all channel states.
     * 
     * Updates the following channels:
     * - Room and outdoor temperatures
     * - Target temperature
     * - DHW temperature, target, mode, and status
     * - Water pressure and pressure OK status
     * - System error status
     */
    private void updateData() {
        RemehaApiClient client = apiClient;
        if (client == null) {
            return;
        }

        try {
            JsonObject dashboard = client.getDashboard();
            if (dashboard == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.comm-error-data-fetch-failed");
                return;
            }

            updateStatus(ThingStatus.ONLINE);

            JsonArray appliances = dashboard.getAsJsonArray("appliances");
            if (appliances != null && appliances.size() > 0) {
                JsonObject appliance = appliances.get(0).getAsJsonObject();

                // Update channels with proper units
                double pressure = appliance.get("waterPressure").getAsDouble();
                logger.debug("Updating water pressure: {} bar", pressure);
                updateState(CHANNEL_WATER_PRESSURE, new QuantityType<>(pressure, Units.BAR));
                updateState(CHANNEL_STATUS, new StringType(appliance.get("errorStatus").getAsString()));
                updateState(CHANNEL_WATER_PRESSURE_OK, OnOffType.from(appliance.get("waterPressureOK").getAsBoolean()));

                JsonObject outdoorInfo = appliance.getAsJsonObject("outdoorTemperatureInformation");
                if (outdoorInfo != null) {
                    double temp = outdoorInfo.get("internetOutdoorTemperature").getAsDouble();
                    updateState(CHANNEL_OUTDOOR_TEMPERATURE, new QuantityType<>(temp, SIUnits.CELSIUS));
                }

                // Climate zones
                JsonArray climateZones = appliance.getAsJsonArray("climateZones");
                if (climateZones != null && climateZones.size() > 0) {
                    JsonObject zone = climateZones.get(0).getAsJsonObject();
                    double roomTemp = zone.get("roomTemperature").getAsDouble();
                    double targetTemp = zone.get("setPoint").getAsDouble();
                    updateState(CHANNEL_ROOM_TEMPERATURE, new QuantityType<>(roomTemp, SIUnits.CELSIUS));
                    updateState(CHANNEL_TARGET_TEMPERATURE, new QuantityType<>(targetTemp, SIUnits.CELSIUS));
                }

                // Hot water zones
                JsonArray hotWaterZones = appliance.getAsJsonArray("hotWaterZones");
                if (hotWaterZones != null && hotWaterZones.size() > 0) {
                    JsonObject zone = hotWaterZones.get(0).getAsJsonObject();
                    double dhwTemp = zone.get("dhwTemperature").getAsDouble();
                    double dhwTarget = zone.get("targetSetpoint").getAsDouble();
                    String dhwMode = zone.get("dhwZoneMode").getAsString();
                    String dhwStatus = zone.get("dhwStatus").getAsString();
                    updateState(CHANNEL_DHW_TEMPERATURE, new QuantityType<>(dhwTemp, SIUnits.CELSIUS));
                    updateState(CHANNEL_DHW_TARGET, new QuantityType<>(dhwTarget, SIUnits.CELSIUS));
                    updateState(CHANNEL_DHW_MODE, new StringType(dhwMode));
                    updateState(CHANNEL_DHW_STATUS, new StringType(dhwStatus));
                }
            }
        } catch (IllegalStateException | NullPointerException e) {
            logger.debug("Error updating data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-data-update-failed");
        }
    }

    /**
     * Sets the target room temperature via API.
     * 
     * @param temperature Target temperature in Celsius
     */
    private void setTargetTemperature(double temperature) {
        RemehaApiClient client = apiClient;
        if (client != null) {
            String climateZoneId = getClimateZoneId();
            if (climateZoneId != null) {
                if (!client.setTemperature(climateZoneId, temperature)) {
                    logger.debug("Failed to set target temperature");
                }
            }
        }
    }

    /**
     * Sets the DHW (Domestic Hot Water) mode via API.
     * 
     * @param mode DHW mode: "anti-frost", "schedule", or "continuous-comfort"
     */
    private void setDhwMode(String mode) {
        RemehaApiClient client = apiClient;
        if (client != null) {
            String hotWaterZoneId = getHotWaterZoneId();
            if (hotWaterZoneId != null) {
                if (!client.setDhwMode(hotWaterZoneId, mode)) {
                    logger.debug("Failed to set DHW mode");
                }
            }
        }
    }

    /**
     * Retrieves the climate zone ID from the dashboard data.
     * Used for temperature control API calls.
     * 
     * @return Climate zone ID or null if not available
     */
    private @Nullable String getClimateZoneId() {
        RemehaApiClient client = apiClient;
        if (client == null) {
            return null;
        }

        try {
            JsonObject dashboard = client.getDashboard();
            if (dashboard != null) {
                JsonArray appliances = dashboard.getAsJsonArray("appliances");
                if (appliances != null && appliances.size() > 0) {
                    JsonObject appliance = appliances.get(0).getAsJsonObject();
                    JsonArray climateZones = appliance.getAsJsonArray("climateZones");
                    if (climateZones != null && climateZones.size() > 0) {
                        return climateZones.get(0).getAsJsonObject().get("climateZoneId").getAsString();
                    }
                }
            }
        } catch (IllegalStateException | NullPointerException e) {
            logger.debug("Error getting climate zone ID: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves the hot water zone ID from the dashboard data.
     * Used for DHW control API calls.
     * 
     * @return Hot water zone ID or null if not available
     */
    private @Nullable String getHotWaterZoneId() {
        RemehaApiClient client = apiClient;
        if (client == null) {
            return null;
        }

        try {
            JsonObject dashboard = client.getDashboard();
            if (dashboard != null) {
                JsonArray appliances = dashboard.getAsJsonArray("appliances");
                if (appliances != null && appliances.size() > 0) {
                    JsonObject appliance = appliances.get(0).getAsJsonObject();
                    JsonArray hotWaterZones = appliance.getAsJsonArray("hotWaterZones");
                    if (hotWaterZones != null && hotWaterZones.size() > 0) {
                        return hotWaterZones.get(0).getAsJsonObject().get("hotWaterZoneId").getAsString();
                    }
                }
            }
        } catch (IllegalStateException | NullPointerException e) {
            logger.debug("Error getting hot water zone ID: {}", e.getMessage());
        }
        return null;
    }
}
