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
package org.openhab.binding.rachio.internal.api;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.RachioBindingConstants;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudNetworkSettings;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudScheduleRule;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioCurrentScheduleResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioDeviceEvent;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioForecastEntry;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioForecastResponse;
import org.openhab.binding.rachio.internal.api.json.RachioZoneGsonDTO.RachioCloudZone;
import org.openhab.binding.rachio.internal.handler.RachioDeviceHandler;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RachioDevice} provides device level functions.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class RachioDevice extends RachioCloudDevice {
    private final Logger logger = LoggerFactory.getLogger(RachioDevice.class);

    // extensions to cloud attributes
    public String runList = "";
    public Integer runTime = 0;
    public String lastEvent = "";
    public @Nullable DateTimeType lastEventTime;
    public boolean paused = false;
    public int pauseDuration = DEFAULT_ZONE_RUNTIME_SEC;
    public int rainDelay = 0;
    private boolean sleepMode = false;

    public @Nullable ThingUID bridgeUID;
    public @Nullable ThingUID devUID;
    private HashMap<String, RachioZone> zoneList = new HashMap<String, RachioZone>();
    private @Nullable RachioDeviceHandler thingHandler = null;
    public @Nullable RachioCloudNetworkSettings network;
    public String scheduleName = "";
    public String currentScheduleId = "";
    public String currentScheduleName = "";
    public String currentScheduleType = "";
    public String currentScheduleStartTime = "";
    public String currentScheduleEndTime = "";
    public int currentScheduleDuration = 0;
    public boolean currentScheduleRunning = false;
    public String lastApiEventType = "";
    public String lastApiEventTime = "";
    public String lastApiEventSummary = "";
    public String forecastSummary = "";
    public double forecastTodayHigh = Double.NaN;
    public double forecastTodayLow = Double.NaN;
    public double forecastPrecipitation = Double.NaN;
    public double forecastPrecipitationProbability = Double.NaN;
    public double forecastWind = Double.NaN;
    public String forecastUpdated = "";
    public String lastSkipType = "";
    public String lastSkipScheduleId = "";
    public String lastSkipStartTime = "";
    public String lastSkipReason = "";
    public int activeZoneNumber = -1;
    public String activeZoneName = "";
    public String activeZoneId = "";

    @SuppressWarnings("unused")
    public RachioDevice(RachioCloudDevice device) {
        try {
            RachioApi.copyMatchingFields(device, this);
            updateRainDelayFromExpirationDate();
            logger.trace("Adding device '{}' (id='{}', model='{}', on={}, status={}, deleted={})", device.name,
                    device.id, device.model, device.on, device.status, device.deleted);
            if (!device.deleted) {
                zoneList = new HashMap<String, RachioZone>(); // discard current list
                for (int i = 0; i < device.zones.size(); i++) {
                    RachioCloudZone zone = device.zones.get(i);
                    if (true /* zone.enabled */) {
                        zoneList.put(zone.id, new RachioZone(zone, getThingID()));
                    } else {
                        logger.trace("Zone '{}.{}[{}]' is disabled, skip.", device.name, zone.name, zone.zoneNumber);
                    }
                }
            }
        } catch (RuntimeException e) {
            logger.warn("Unable to initialize device '{}': {}", device.name, e.getMessage());
        }
    }

    /**
     * Set the ThingHandler for this device
     *
     * @param deviceHandler
     */
    public void setThingHandler(RachioDeviceHandler deviceHandler) {
        thingHandler = deviceHandler;
    }

    /**
     * @return thing handler for this zone
     */
    public @Nullable RachioDeviceHandler getThingHandler() {
        return thingHandler;
    }

    /**
     * compare some specific device properties to decide if channel updates are performed
     *
     * @param cdev device properties to compare
     * @return true: no change, false: update required
     */
    public boolean compare(@Nullable RachioDevice cdev) {
        if (cdev == null || !id.equalsIgnoreCase(cdev.id) || !status.equalsIgnoreCase(cdev.status) || on != cdev.on
                || rainSensorTripped != cdev.rainSensorTripped
                || rainDelayExpirationDate != cdev.rainDelayExpirationDate) {
            logger.trace("Device data was updated");
            return false;
        }
        return true;
    }

    /**
     * Copy relevant attributes read from cloud
     *
     * @param updatedData new device settings received from cloud call
     */
    public void update(@Nullable RachioDevice updatedData) {
        if (updatedData == null || !id.equals(updatedData.id)) {
            return;
        }

        status = updatedData.status;
        on = updatedData.on;
        rainSensorTripped = updatedData.rainSensorTripped;
        rainDelayExpirationDate = updatedData.rainDelayExpirationDate;
        updateRainDelayFromExpirationDate();
    }

    /**
     * Save ThingUID (used for mapping ThingUID to internal data structure)
     *
     * @param bridgeUID
     * @param deviceUID
     */
    public void setUID(ThingUID bridgeUID, ThingUID deviceUID) {
        this.bridgeUID = bridgeUID;
        devUID = deviceUID;
    }

    /**
     * @return Device thing uid
     */
    public @Nullable ThingUID getUID() {
        return devUID;
    }

    /**
     * Fill the Thing property data
     *
     * @return A map for key/value
     */
    public Map<String, String> fillProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_VENDOR, RachioBindingConstants.BINDING_VENDOR);
        properties.put(PROPERTY_NAME, name);
        properties.put(PROPERTY_MODEL, model);
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, serialNumber);
        properties.put(Thing.PROPERTY_MAC_ADDRESS, macAddress);
        properties.put(PROPERTY_DEV_ID, id);
        properties.put(PROPERTY_DEV_LAT, new Double(latitude).toString());
        properties.put(PROPERTY_DEV_LONG, new Double(longitude).toString());
        RachioCloudNetworkSettings nw = network;
        if (nw != null) {
            properties.put(PROPERTY_IP_ADDRESS, nw.ip);
            properties.put(PROPERTY_IP_MASK, nw.ip);
            properties.put(PROPERTY_IP_GW, nw.gw);
            properties.put(PROPERTY_IP_DNS1, nw.dns1);
            properties.put(PROPERTY_IP_DNS2, nw.dns2);
            properties.put(PROPERTY_WIFI_RSSI, nw.rssi);
        }
        return properties;
    }

    /**
     * Get the thing unique id
     *
     * @return Suffix for the thing name
     */
    public String getThingID() {
        return macAddress;
    }

    /**
     * Get the thing's name
     *
     * @return Name
     */
    public String getThingName() {
        return name;
    }

    /**
     * Get controller status as OnOffType
     *
     * @return Thing status
     */
    public ThingStatus getStatus() {
        if ("ONLINE".equals(status)) {
            return ThingStatus.ONLINE;
        }
        if ("OFFLINE".equals(status)) {
            return ThingStatus.OFFLINE;
        }
        logger.debug("Device status '{}' was mapped to OFFLINE", status);
        return ThingStatus.OFFLINE;
    }

    public void setStatus(String new_status) {
        if ("ONLINE".equals(new_status) || "OFFLINE".equals(new_status)) {
            status = new_status;
            return;
        }
        if ("OFFLINE_NOTIFICATION".equals(new_status)) {
            status = "OFFLINE";
            return;
        }
        logger.debug("Device status '{}' was not set!", new_status);
    }

    /**
     * Get controller status (online/offline) as OnOffType
     *
     * @return Controller status, ON=online, OFF=offline
     */
    public OnOffType getOnline() {
        return "ONLINE".equals(status) ? OnOffType.ON : OnOffType.OFF;
    }

    /**
     * Get enabled status as OnOffType
     *
     * @return ON=enabled, OFF=disabled
     */
    public OnOffType getEnabled() {
        return on ? OnOffType.ON : OnOffType.OFF;
    }

    /**
     * Get operation mode
     *
     * @return ON=running, OFF=standby
     */
    public OnOffType getSleepMode() {
        return sleepMode ? OnOffType.ON : OnOffType.OFF;
    }

    public void setSleepMode(String subType) {
        sleepMode = subType.contains("ON") ? true : false;
    }

    public OnOffType getPaused() {
        return paused ? OnOffType.ON : OnOffType.OFF;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public int getPauseDuration() {
        return pauseDuration;
    }

    public void setPauseDuration(int duration) {
        pauseDuration = Math.max(0, Math.min(3600, duration));
    }

    /**
     * Put controller into rain delay mode
     *
     * @param newDelay Number of seconds for the Rain Delay mode
     */
    public void setRainDelayTime(int newDelay) {
        rainDelay = newDelay;
    }

    private void updateRainDelayFromExpirationDate() {
        if (rainDelayExpirationDate <= 0) {
            rainDelay = 0;
            return;
        }

        long remainingMillis = rainDelayExpirationDate - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            rainDelay = 0;
            return;
        }
        rainDelay = (int) Math.min(Integer.MAX_VALUE, (remainingMillis + 999) / 1000);
    }

    /**
     * Get the list of zones to run when starting watering on the controller
     *
     * @return Comma seperated list of zones to run
     */
    public String getRunZones() {
        return runList;
    }

    /**
     * Set the zone list for running the controller
     *
     * @param list Comma seperated list of zone IDs
     */
    public void setRunZones(String list) {
        runList = list;
    }

    /**
     * Get total run time for the controller as returned from the Cloud API
     *
     * @return Total run time for the controller
     */
    public int getRunTime() {
        return runTime;
    }

    /**
     * Set the run time for next run
     *
     * @param time Number of seconds to run the zones
     */
    public void setRunTime(int time) {
        runTime = time;
    }

    public int getMultiZoneRunTime(int defaultRuntime) {
        return runTime > 0 ? runTime : defaultRuntime;
    }

    public void setEvent(String event, DateTimeType ts) {
        lastEvent = event;
        lastEventTime = ts;
    }

    public String getEvent() {
        return lastEvent;
    }

    public @Nullable DateTimeType getEventTime() {
        return lastEventTime;
    }

    public void setNetwork(@Nullable RachioCloudNetworkSettings network) {
        this.network = network;
    }

    public void applyCurrentSchedule(RachioCurrentScheduleResponse currentSchedule) {
        currentScheduleRunning = currentSchedule.isRunning();
        if (!currentScheduleRunning) {
            currentScheduleId = "";
            currentScheduleName = "";
            currentScheduleType = "";
            currentScheduleStartTime = "";
            currentScheduleEndTime = "";
            currentScheduleDuration = 0;
            return;
        }

        String scheduleId = currentSchedule.getScheduleId();
        currentScheduleId = firstNonBlank(scheduleId, currentScheduleId);
        currentScheduleName = firstNonBlank(currentSchedule.getScheduleName(), getScheduleRuleName(currentScheduleId),
                currentScheduleName);
        currentScheduleType = firstNonBlank(currentSchedule.getScheduleType(), getScheduleRuleType(currentScheduleId),
                currentScheduleType);
        currentScheduleStartTime = firstNonBlank(currentSchedule.getStartTime(), currentScheduleStartTime);
        currentScheduleEndTime = firstNonBlank(currentSchedule.getEndTime(), currentScheduleEndTime);
        int duration = currentSchedule.getDurationSeconds();
        if (duration > 0) {
            currentScheduleDuration = duration;
        }
    }

    public void clearCurrentSchedule() {
        currentScheduleId = "";
        currentScheduleName = "";
        currentScheduleType = "";
        currentScheduleStartTime = "";
        currentScheduleEndTime = "";
        currentScheduleDuration = 0;
        currentScheduleRunning = false;
    }

    public void applyApiEvent(@Nullable RachioDeviceEvent event) {
        if (event == null) {
            lastApiEventType = "";
            lastApiEventTime = "";
            lastApiEventSummary = "";
            return;
        }
        lastApiEventType = event.getEventType();
        lastApiEventTime = event.getEventTime();
        lastApiEventSummary = event.getSummary();
    }

    public void applyWebhookEvent(RachioEventGsonDTO event) {
        lastApiEventType = firstNonBlank(event.eventType, event.subType, event.type);
        lastApiEventTime = firstNonBlank(event.timestamp, event.timeForSummary, event.endTime, event.startTime);
        lastApiEventSummary = firstNonBlank(event.summary, event.description, event.title, event.pushTitle,
                lastApiEventType);
    }

    public boolean applyForecast(RachioForecastResponse forecast) {
        return applyForecast(forecast, DEFAULT_FORECAST_UNITS, "");
    }

    public boolean applyForecast(RachioForecastResponse forecast, String forecastUnits, String retrievedAt) {
        if (!forecast.hasUsefulData()) {
            return false;
        }
        String summary = forecast.getSummary();
        if (summary.isBlank()) {
            summary = forecast.buildSummary(forecastUnits);
        }
        if (!summary.isBlank()) {
            forecastSummary = summary;
        }
        String updated = forecast.getUpdated();
        if (updated.isBlank()) {
            updated = retrievedAt;
        }
        if (!updated.isBlank()) {
            forecastUpdated = updated;
        }
        RachioForecastEntry today = forecast.getTodayForecast();
        if (today == null) {
            return true;
        }
        double high = today.getHighTemperature();
        if (!Double.isNaN(high)) {
            forecastTodayHigh = high;
        }
        double low = today.getLowTemperature();
        if (!Double.isNaN(low)) {
            forecastTodayLow = low;
        }
        if (!Double.isNaN(today.precipitation)) {
            forecastPrecipitation = today.precipitation;
        } else if (Double.compare(today.precipitationProbability, 0.0) == 0) {
            forecastPrecipitation = 0;
        }
        if (!Double.isNaN(today.precipitationProbability)) {
            forecastPrecipitationProbability = today.precipitationProbability;
        }
        double wind = today.getWind();
        if (!Double.isNaN(wind)) {
            forecastWind = wind;
        }
        return true;
    }

    public void applySkipEvent(String skipType, String scheduleId, String startTime, String reason) {
        lastSkipType = skipType;
        lastSkipScheduleId = scheduleId;
        lastSkipStartTime = startTime;
        lastSkipReason = reason;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    public String getScheduleRuleName(String scheduleId) {
        RachioCloudScheduleRule scheduleRule = getScheduleRuleById(scheduleId);
        return scheduleRule != null ? firstNonBlank(scheduleRule.name, scheduleRule.externalName) : "";
    }

    public String getScheduleRuleType(String scheduleId) {
        RachioCloudScheduleRule scheduleRule = getScheduleRuleById(scheduleId);
        if (scheduleRule == null) {
            return "";
        }
        if (scheduleRules.contains(scheduleRule)) {
            return firstNonBlank(scheduleRule.type, "FIXED");
        }
        return firstNonBlank(scheduleRule.type, "FLEX");
    }

    private @Nullable RachioCloudScheduleRule getScheduleRuleById(String scheduleId) {
        if (scheduleId.isBlank()) {
            return null;
        }
        for (RachioCloudScheduleRule scheduleRule : scheduleRules) {
            if (scheduleId.equalsIgnoreCase(scheduleRule.id)) {
                return scheduleRule;
            }
        }
        for (RachioCloudScheduleRule scheduleRule : flexScheduleRules) {
            if (scheduleId.equalsIgnoreCase(scheduleRule.id)) {
                return scheduleRule;
            }
        }
        return null;
    }

    public boolean applyActiveZoneEvent(String state, int zoneNumber, @Nullable RachioZone zone) {
        if ("ZONE_STARTED".equals(state)) {
            activeZoneNumber = zoneNumber > 0 ? zoneNumber : (zone != null ? zone.zoneNumber : -1);
            if (zone != null) {
                activeZoneName = zone.name;
                activeZoneId = zone.id;
            } else {
                activeZoneName = "";
                activeZoneId = "";
            }
            return true;
        }

        if ("ZONE_STOPPED".equals(state) || "ZONE_COMPLETED".equals(state)) {
            boolean activeZoneKnown = activeZoneNumber > 0 || !activeZoneId.isBlank();
            boolean matchesActiveZone = zone != null
                    && (zone.zoneNumber == activeZoneNumber || zone.id.equalsIgnoreCase(activeZoneId));
            matchesActiveZone |= zoneNumber > 0 && zoneNumber == activeZoneNumber;
            if (activeZoneKnown && !matchesActiveZone) {
                return false;
            }
            clearActiveZone();
            return true;
        }

        return false;
    }

    public void clearActiveZone() {
        activeZoneNumber = -1;
        activeZoneName = "";
        activeZoneId = "";
    }

    public String getAllRunZonesJson(int defaultRuntime) {
        boolean flAll = runList.isEmpty() || "ALL".equalsIgnoreCase(runList);
        int runtime = getMultiZoneRunTime(defaultRuntime);
        StringBuilder resolvedDurations = new StringBuilder();

        String list = runList + ","; // make sure last entry is terminated by ','
        String json = "{ \"zones\" : [";
        for (HashMap.Entry<String, RachioZone> ze : zoneList.entrySet()) {
            RachioZone zone = ze.getValue();
            if (flAll || (list.contains(zone.zoneNumber + ",") && (zone.getEnabled() == OnOffType.ON))) {
                if (resolvedDurations.length() > 0) {
                    resolvedDurations.append(", ");
                }
                resolvedDurations.append("zone ").append(zone.zoneNumber).append(" = ").append(runtime).append(" sec");
                if (json.contains("\"id\"")) {
                    json = json + ", ";
                }
                json = json + "{ \"id\" : \"" + zone.id + "\", \"duration\" : " + runtime + ", \"sortOrder\" : 1}";
            }
        }
        json = json + "] }";
        logger.debug("Resolved multi-zone durations: {}", resolvedDurations);
        return json;
    }

    /**
     * Get a list of all zones belonging to this controller
     *
     * @return Zone list (HashMap)
     */
    public HashMap<String, RachioZone> getZones() {
        return zoneList;
    }

    public @Nullable RachioZone getZoneByNumber(int zoneNumber) {
        for (HashMap.Entry<String, RachioZone> ze : zoneList.entrySet()) {
            RachioZone zone = ze.getValue();
            if (zone != null && zone.zoneNumber == zoneNumber) {
                return zone;
            }
        }
        return null;
    }

    public @Nullable RachioZone getZoneById(String zoneId) {
        for (HashMap.Entry<String, RachioZone> ze : zoneList.entrySet()) {
            RachioZone zone = ze.getValue();
            if (zone != null && zone.id.equals(zoneId)) {
                return zone;
            }
        }
        return null;
    }
}
