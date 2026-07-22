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
import static org.openhab.binding.rachio.internal.RachioUtils.*;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.RachioBindingConstants;
import org.openhab.binding.rachio.internal.api.json.RachioApiGsonDTO.RachioApiLegacyWebHookEventType;
import org.openhab.binding.rachio.internal.api.json.RachioApiGsonDTO.RachioApiWebHookEntry;
import org.openhab.binding.rachio.internal.api.json.RachioApiGsonDTO.RachioApiWebHookResourceId;
import org.openhab.binding.rachio.internal.api.json.RachioApiGsonDTO.RachioApiWebhookEventTypesResponse;
import org.openhab.binding.rachio.internal.api.json.RachioApiGsonDTO.RachioCloudPersonId;
import org.openhab.binding.rachio.internal.api.json.RachioApiGsonDTO.RachioCloudStatus;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.api.json.RachioPropertyGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioPropertyGsonDTO.RachioProperty;
import org.openhab.binding.rachio.internal.api.json.RachioPropertyGsonDTO.RachioPropertyEntityLookupResponse;
import org.openhab.binding.rachio.internal.api.json.RachioPropertyGsonDTO.RachioPropertyListResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioBaseStation;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioBaseStationListResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioPlannedRunSkipOverrideRequest;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioProgramSkipOverrideRequest;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValve;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveDayViewsRequest;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveDayViewsResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveDefaultRuntimeRequest;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveListResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveProgram;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveProgramListResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveStartWateringRequest;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveStopWateringRequest;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioCurrentScheduleResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioDeviceEventListResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioFlexScheduleRuleResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioForecastResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioMoistureLevelRequest;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioMoisturePercentRequest;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioScheduleRuleCommandRequest;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioScheduleRuleResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioSeasonalAdjustmentRequest;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookResourceType;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookTarget;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.Priority;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RateLimitThrottleException;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link RachioApi} implements the interface to the Rachio cloud service (using http).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class RachioApi {
    private final Logger logger = LoggerFactory.getLogger(RachioApi.class);
    private static final String MD5_HASH_ALGORITHM = "MD5";
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final int WEBHOOK_SIGNATURE_LENGTH_BYTES = 32;
    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    protected String apikey = "";
    protected String personId = "";
    protected String userName = "";
    protected String fullName = "";
    protected String email = "";
    protected @Nullable ThingUID bridgeUID = null;

    protected RachioApiResult lastApiResult = new RachioApiResult();
    private static final Map<String, ClientRateLimitManager> RATE_LIMIT_MANAGERS = new ConcurrentHashMap<>();
    private ClientRateLimitManager rateLimitManager = new ClientRateLimitManager(10, Duration.ofSeconds(30));

    private HashMap<String, RachioDevice> deviceList = new HashMap<String, RachioDevice>();
    private RachioHttp httpApi = new RachioHttp("");

    public RachioApi(String personId) {
        this.personId = personId;
    }

    public RachioApiResult getLastApiResult() {
        return lastApiResult;
    }

    protected void setApiResult(RachioApiResult result) {
        lastApiResult = result;
    }

    private void throttleIfNeeded(Priority priority, RequestPurpose requestPurpose) throws RachioApiException {
        try {
            rateLimitManager.tryThrottle(priority, requestPurpose);
        } catch (RateLimitThrottleException e) {
            throw new RachioApiThrottledException(e, lastApiResult);
        }
    }

    private void updateRateLimit(@Nullable RachioApiResult result) {
        if (result == null) {
            return;
        }
        int rateRemaining = result.hasKnownRateRemaining() ? result.rateRemaining : -1;
        rateLimitManager.updateRateLimit(result.rateLimit, rateRemaining, result.rateReset);
    }

    private RachioApiResult recordApiResult(RachioApiResult result) {
        updateRateLimit(result);
        lastApiResult = result;
        return result;
    }

    private void recordApiException(RachioApiException e) {
        RachioApiResult result = e.getApiResult();
        updateRateLimit(result);
        lastApiResult = result;
    }

    private Priority readPriority(RequestPurpose requestPurpose) {
        return requestPurpose == RequestPurpose.INITIALIZATION ? Priority.MEDIUM : Priority.LOW;
    }

    private RachioApiResult httpGet(String url, @Nullable String params, Priority priority) throws RachioApiException {
        return httpGet(url, params, priority, RequestPurpose.BACKGROUND_REFRESH);
    }

    private RachioApiResult httpGet(String url, @Nullable String params, Priority priority,
            RequestPurpose requestPurpose) throws RachioApiException {
        throttleIfNeeded(priority, requestPurpose);
        try {
            return recordApiResult(httpApi.httpGet(url, params));
        } catch (RachioApiException e) {
            recordApiException(e);
            throw e;
        }
    }

    private RachioApiResult httpPut(String url, String data, Priority priority) throws RachioApiException {
        throttleIfNeeded(priority, RequestPurpose.USER_COMMAND);
        try {
            return recordApiResult(httpApi.httpPut(url, data));
        } catch (RachioApiException e) {
            recordApiException(e);
            throw e;
        }
    }

    private RachioApiResult httpPost(String url, String data, Priority priority) throws RachioApiException {
        return httpPost(url, data, priority, RequestPurpose.BACKGROUND_REFRESH);
    }

    private RachioApiResult httpPost(String url, String data, Priority priority, RequestPurpose requestPurpose)
            throws RachioApiException {
        throttleIfNeeded(priority, requestPurpose);
        try {
            return recordApiResult(httpApi.httpPost(url, data));
        } catch (RachioApiException e) {
            recordApiException(e);
            throw e;
        }
    }

    private RachioApiResult httpDelete(String url, @Nullable String params, Priority priority)
            throws RachioApiException {
        return httpDelete(url, params, priority, RequestPurpose.USER_COMMAND);
    }

    private RachioApiResult httpDelete(String url, @Nullable String params, Priority priority,
            RequestPurpose requestPurpose) throws RachioApiException {
        throttleIfNeeded(priority, requestPurpose);
        try {
            return recordApiResult(httpApi.httpDelete(url, params));
        } catch (RachioApiException e) {
            recordApiException(e);
            throw e;
        }
    }

    public String getPersonId() {
        return personId;
    }

    public String getExternalId() {
        ThingUID uid = bridgeUID;
        if (apikey.isEmpty() || uid == null) {
            return "";
        }
        String apikeyHash = getMD5Hash(apikey);
        String rawValue = "OH_RACHIO_EXTERNALID_" + uid + "_" + apikeyHash;
        return getMD5Hash(rawValue);
    }

    public List<String> getLegacyExternalIds() {
        if (apikey.isEmpty()) {
            return List.of();
        }
        List<String> legacyIds = new ArrayList<>();
        String apikeyHash = getMD5Hash(apikey);
        for (int legacySalt = 1; legacySalt <= 50; legacySalt++) {
            String hash = "OH_" + apikeyHash + "_" + legacySalt;
            legacyIds.add(getMD5Hash(hash));
        }
        return legacyIds;
    }

    public void initialize(String apikey, ThingUID bridgeUID) throws RachioApiException {
        initialize(apikey, bridgeUID, Priority.MEDIUM, RequestPurpose.INITIALIZATION);
    }

    public void initialize(String apikey, ThingUID bridgeUID, Priority priority) throws RachioApiException {
        initialize(apikey, bridgeUID, priority, RequestPurpose.BACKGROUND_REFRESH);
    }

    public void initialize(String apikey, ThingUID bridgeUID, Priority priority, RequestPurpose requestPurpose)
            throws RachioApiException {
        this.apikey = apikey;
        this.bridgeUID = bridgeUID;
        this.rateLimitManager = Objects.requireNonNull(RATE_LIMIT_MANAGERS.computeIfAbsent(getMD5Hash(apikey),
                key -> new ClientRateLimitManager(10, Duration.ofSeconds(30))));
        httpApi = new RachioHttp(this.apikey);
        if (!initializePersonId(priority, requestPurpose) || !initializeDevices(bridgeUID, priority, requestPurpose)
                || !initializeZones()) {
            throw new RachioApiException("API initialization failed!");
        }
    }

    public HashMap<String, RachioDevice> getDevices() {
        return deviceList;
    }

    public @Nullable RachioDevice bindDeviceByRachioId(ThingUID bridgeUID, ThingUID thingUID, String deviceId) {
        RachioDevice dev = getDeviceByRachioId(deviceId);
        if (dev != null) {
            dev.setUID(bridgeUID, thingUID);
            logger.debug("Mapped requested device UID '{}' to Rachio device '{}' using configured deviceId '{}'",
                    thingUID, dev.name, deviceId);
        } else {
            logger.debug("Unable to map requested device UID '{}' using configured deviceId '{}'", thingUID, deviceId);
        }
        return dev;
    }

    public @Nullable RachioDevice getDeviceByRachioId(@Nullable String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return null;
        }

        RachioDevice device = deviceList.get(deviceId);
        if (device != null) {
            return device;
        }

        for (RachioDevice dev : deviceList.values()) {
            if (matchesIdentifierValue(deviceId, dev.id)) {
                return dev;
            }
        }
        return null;
    }

    public @Nullable RachioZone getZoneByRachioId(@Nullable String zoneId) {
        if (zoneId == null || zoneId.isBlank()) {
            return null;
        }

        for (RachioDevice dev : deviceList.values()) {
            for (RachioZone zone : dev.getZones().values()) {
                if (matchesIdentifierValue(zoneId, zone.id)) {
                    return zone;
                }
            }
        }
        return null;
    }

    public @Nullable RachioDevice getDeviceByZoneRachioId(@Nullable String zoneId) {
        if (zoneId == null || zoneId.isBlank()) {
            return null;
        }

        for (RachioDevice dev : deviceList.values()) {
            for (RachioZone zone : dev.getZones().values()) {
                if (matchesIdentifierValue(zoneId, zone.id)) {
                    return dev;
                }
            }
        }
        return null;
    }

    public @Nullable RachioDevice getDevByUID(@Nullable ThingUID bridgeUID, @Nullable ThingUID thingUID) {
        return getDevByUID(bridgeUID, thingUID, Collections.emptyMap(), Collections.emptyMap());
    }

    public @Nullable RachioDevice getDevByUID(@Nullable ThingUID bridgeUID, @Nullable ThingUID thingUID,
            Map<String, String> properties) {
        return getDevByUID(bridgeUID, thingUID, Collections.emptyMap(), properties);
    }

    public @Nullable RachioDevice getDevByUID(@Nullable ThingUID bridgeUID, @Nullable ThingUID thingUID,
            Map<String, @Nullable Object> configuration, Map<String, String> properties) {
        if (bridgeUID == null || thingUID == null) {
            logger.debug("getDevByUID: Unable map UID to device, bridgeUID={}, deviceUID={}", bridgeUID, thingUID);
            return null;
        }

        String configuredDeviceId = getConfigurationString(configuration, PROPERTY_DEV_ID);
        if (!configuredDeviceId.isBlank()) {
            RachioDevice dev = getDeviceByRachioId(configuredDeviceId);
            if (dev != null) {
                dev.setUID(bridgeUID, thingUID);
                logger.debug(
                        "getDevByUID: mapped requested device UID '{}' to Rachio device '{}' using configured deviceId '{}'",
                        thingUID, dev.name, configuredDeviceId);
                return dev;
            }

            logger.debug("getDevByUID: Unable map UID '{}' to device using configured deviceId '{}'", thingUID,
                    configuredDeviceId);
            return null;
        }

        for (HashMap.Entry<String, RachioDevice> entry : deviceList.entrySet()) {
            RachioDevice dev = entry.getValue();
            String matchedProperty = getMatchingDeviceProperty(dev, properties);
            if (matchedProperty != null) {
                dev.setUID(bridgeUID, thingUID);
                logger.debug(
                        "getDevByUID: mapped requested device UID '{}' to Rachio device '{}' using Thing property '{}'",
                        thingUID, dev.name, matchedProperty);
                return dev;
            }
        }

        for (HashMap.Entry<String, RachioDevice> entry : deviceList.entrySet()) {
            RachioDevice dev = entry.getValue();
            ThingUID expectedUID = buildExpectedThingUID(THING_TYPE_DEVICE, bridgeUID, dev.getThingID());
            logger.trace("getDevByUID: requested bridge={}, requested device={}, cached bridge={}, cached device={}, "
                    + "candidate device={}", bridgeUID, thingUID, dev.bridgeUID, dev.devUID, expectedUID);
            if (expectedUID != null && matchesThingUID(expectedUID, thingUID)) {
                dev.setUID(bridgeUID, thingUID);
                logger.trace("Device '{}' found by canonical UID '{}'.", dev.name, expectedUID);
                return dev;
            }
        }

        logger.debug("getDevByUID: Unable map UID to device, bridgeUID={}, deviceUID={}", bridgeUID, thingUID);
        return null;
    }

    public @Nullable RachioZone getZoneByUID(@Nullable ThingUID bridgeUID, @Nullable ThingUID zoneUID) {
        return getZoneByUID(bridgeUID, zoneUID, Collections.emptyMap(), Collections.emptyMap());
    }

    public @Nullable RachioZone getZoneByUID(@Nullable ThingUID bridgeUID, @Nullable ThingUID zoneUID,
            Map<String, String> properties) {
        return getZoneByUID(bridgeUID, zoneUID, Collections.emptyMap(), properties);
    }

    public @Nullable RachioZone getZoneByUID(@Nullable ThingUID bridgeUID, @Nullable ThingUID zoneUID,
            Map<String, @Nullable Object> configuration, Map<String, String> properties) {
        if (bridgeUID == null || zoneUID == null) {
            logger.debug("getZoneByUID: Unable map UID to zone, bridgeUID={}, zoneUID={}", bridgeUID, zoneUID);
            return null;
        }

        String configuredZoneId = getConfigurationString(configuration, PROPERTY_ZONE_ID);
        if (!configuredZoneId.isBlank()) {
            RachioDevice dev = getDeviceByZoneRachioId(configuredZoneId);
            RachioZone zone = getZoneByRachioId(configuredZoneId);
            if (dev != null && zone != null) {
                bindZoneUIDs(dev, zone, bridgeUID, zoneUID);
                logger.debug(
                        "getZoneByUID: mapped requested zone UID '{}' to Rachio zone '{}' using configured zoneId '{}'",
                        zoneUID, zone.name, configuredZoneId);
                return zone;
            }

            logger.debug("getZoneByUID: Unable map UID '{}' to zone using configured zoneId '{}'", zoneUID,
                    configuredZoneId);
            return null;
        }

        for (HashMap.Entry<String, RachioDevice> de : deviceList.entrySet()) {
            RachioDevice dev = de.getValue();
            ThingUID expectedDevUID = buildExpectedThingUID(THING_TYPE_DEVICE, bridgeUID, dev.getThingID());
            if (expectedDevUID == null) {
                logger.trace("getZoneByUID: Skip device '{}' because no valid device Thing UID can be built", dev.name);
                continue;
            }

            HashMap<String, RachioZone> zoneList = dev.getZones();
            for (HashMap.Entry<String, RachioZone> ze : zoneList.entrySet()) {
                RachioZone zone = ze.getValue();
                String matchedProperty = getMatchingZoneProperty(zone, properties);
                if (matchedProperty != null) {
                    bindZoneUIDs(dev, zone, bridgeUID, zoneUID);
                    logger.debug(
                            "getZoneByUID: mapped requested zone UID '{}' to Rachio zone '{}' using Thing property '{}'",
                            zoneUID, zone.name, matchedProperty);
                    return zone;
                }
            }
        }

        for (HashMap.Entry<String, RachioDevice> de : deviceList.entrySet()) {
            RachioDevice dev = de.getValue();
            ThingUID expectedDevUID = buildExpectedThingUID(THING_TYPE_DEVICE, bridgeUID, dev.getThingID());
            if (expectedDevUID == null) {
                logger.trace("getZoneByUID: Skip device '{}' because no valid device Thing UID can be built", dev.name);
                continue;
            }

            HashMap<String, RachioZone> zoneList = dev.getZones();
            for (HashMap.Entry<String, RachioZone> ze : zoneList.entrySet()) {
                RachioZone zone = ze.getValue();
                ThingUID expectedZoneUID = buildExpectedThingUID(THING_TYPE_ZONE, bridgeUID, zone.getThingID());
                logger.trace(
                        "getZoneByUID: requested bridge={}, requested zone={}, cached device={}, cached zone={}, "
                                + "candidate device={}, candidate zone={}",
                        bridgeUID, zoneUID, zone.getDevUID(), zone.getUID(), expectedDevUID, expectedZoneUID);
                if (expectedZoneUID != null && matchesThingUID(expectedZoneUID, zoneUID)) {
                    bindZoneUIDs(dev, zone, bridgeUID, zoneUID);
                    logger.trace("Zone '{}' found by canonical UID '{}'.", zone.name, expectedZoneUID);
                    return zone;
                }
            }
        }

        logger.debug("getZoneByUID: Unable map UID to zone, bridgeUID={}, zoneUID={}", bridgeUID, zoneUID);
        return null;
    }

    private void bindZoneUIDs(RachioDevice dev, RachioZone zone, ThingUID bridgeUID, ThingUID zoneUID) {
        ThingUID deviceUID = dev.getUID();
        if (deviceUID == null) {
            deviceUID = buildExpectedThingUID(THING_TYPE_DEVICE, bridgeUID, dev.getThingID());
            if (deviceUID != null) {
                dev.setUID(bridgeUID, deviceUID);
            }
        }
        zone.setUID(deviceUID, zoneUID);
    }

    private @Nullable String getMatchingDeviceProperty(RachioDevice dev, Map<String, String> properties) {
        if (matchesProperty(properties, PROPERTY_DEV_ID, dev.id)) {
            return PROPERTY_DEV_ID;
        }
        if (matchesProperty(properties, Thing.PROPERTY_MAC_ADDRESS, dev.macAddress)) {
            return Thing.PROPERTY_MAC_ADDRESS;
        }
        if (matchesProperty(properties, Thing.PROPERTY_SERIAL_NUMBER, dev.serialNumber)) {
            return Thing.PROPERTY_SERIAL_NUMBER;
        }
        return null;
    }

    private @Nullable String getMatchingZoneProperty(RachioZone zone, Map<String, String> properties) {
        if (matchesProperty(properties, PROPERTY_ZONE_ID, zone.id)) {
            return PROPERTY_ZONE_ID;
        }
        return null;
    }

    private String getConfigurationString(Map<String, @Nullable Object> configuration, String parameterName) {
        Object configValue = getConfigurationValue(configuration, parameterName);
        return configValue != null ? configValue.toString().trim() : "";
    }

    private @Nullable Object getConfigurationValue(Map<String, @Nullable Object> configuration, String parameterName) {
        Object value = configuration.get(parameterName);
        if (value != null) {
            return value;
        }

        for (Map.Entry<String, @Nullable Object> entry : configuration.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(parameterName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private boolean matchesProperty(Map<String, String> properties, String propertyName,
            @Nullable String expectedValue) {
        String actualValue = properties.get(propertyName);
        return matchesIdentifierValue(actualValue, expectedValue);
    }

    private boolean matchesIdentifierValue(@Nullable String actualValue, @Nullable String expectedValue) {
        return actualValue != null && !actualValue.isBlank() && expectedValue != null && !expectedValue.isBlank()
                && actualValue.equalsIgnoreCase(expectedValue);
    }

    private @Nullable ThingUID buildExpectedThingUID(ThingTypeUID thingTypeUID, ThingUID bridgeUID,
            @Nullable String thingId) {
        if (thingId == null || thingId.isBlank()) {
            return null;
        }
        try {
            return new ThingUID(thingTypeUID, bridgeUID, thingId);
        } catch (IllegalArgumentException e) {
            logger.trace("Unable to build Rachio Thing UID for thing type '{}', bridge '{}', thing id '{}'",
                    thingTypeUID, bridgeUID, thingId);
            return null;
        }
    }

    private boolean matchesThingUID(ThingUID expectedUID, ThingUID requestedUID) {
        return expectedUID.equals(requestedUID)
                || expectedUID.getAsString().equalsIgnoreCase(requestedUID.getAsString());
    }

    private Boolean initializePersonId(Priority priority, RequestPurpose requestPurpose) throws RachioApiException {
        if (!personId.isEmpty()) {
            logger.trace("Using cached Rachio person ID.");
            return true;
        }

        lastApiResult = httpGet(APIURL_BASE + APIURL_GET_PERSON, null, priority, requestPurpose);
        Gson gson = new Gson();
        RachioCloudPersonId pid = Objects
                .requireNonNull(gson.fromJson(lastApiResult.resultString, RachioCloudPersonId.class));
        personId = pid.id;
        logger.debug("Using Rachio person ID from API response.");
        if (lastApiResult.isRateLimitCritical()) {
            String errorMessage = MessageFormat.format(
                    "Rachio Cloud API Rate Limit is critical ({0} of {1}), reset at {2}", lastApiResult.rateRemaining,
                    lastApiResult.rateLimit, lastApiResult.rateReset);
            throw new RachioApiException(errorMessage, lastApiResult);
        }
        return true;
    }

    public String getUserInfo() {
        return !userName.isEmpty() ? fullName + "(" + userName + ", " + email + ")" : "";
    }

    public void stopWatering(String deviceId) throws RachioApiException {
        logger.debug("Stop watering for device '{}'", deviceId);
        httpPut(APIURL_BASE + APIURL_DEV_PUT_STOP, "{ \"id\" : \"" + deviceId + "\" }", Priority.HIGH);
    }

    public void enableDevice(String deviceId) throws RachioApiException {
        logger.debug("Enable device '{}'.", deviceId);
        httpPut(APIURL_BASE + APIURL_DEV_PUT_ON, "{ \"id\" : \"" + deviceId + "\" }", Priority.HIGH);
    }

    public void disableDevice(String deviceId) throws RachioApiException {
        logger.debug("Disable device '{}'.", deviceId);
        httpPut(APIURL_BASE + APIURL_DEV_PUT_OFF, "{ \"id\" : \"" + deviceId + "\" }", Priority.HIGH);
    }

    public void rainDelay(String deviceId, Integer delay) throws RachioApiException {
        logger.debug("Start rain delay for device '{}'.", deviceId);
        httpPut(APIURL_BASE + APIURL_DEV_PUT_RAIN_DELAY,
                "{ \"id\" : \"" + deviceId + "\", \"duration\" : " + delay + " }", Priority.HIGH);
    }

    public void pauseZoneRun(String deviceId, int duration) throws RachioApiException {
        logger.debug("Pause active zone run for device '{}' for {} sec.", deviceId, duration);
        httpPut(APIURL_BASE + APIURL_DEV_PUT_PAUSE_ZONE_RUN,
                "{ \"id\" : \"" + deviceId + "\", \"duration\" : " + duration + " }", Priority.HIGH);
    }

    public void resumeZoneRun(String deviceId) throws RachioApiException {
        logger.debug("Resume active zone run for device '{}'.", deviceId);
        httpPut(APIURL_BASE + APIURL_DEV_PUT_RESUME_ZONE_RUN, "{ \"id\" : \"" + deviceId + "\" }", Priority.HIGH);
    }

    public void runMultipleZones(String zoneListJson) throws RachioApiException {
        int zoneCount = countStartMultipleZoneEntries(zoneListJson);
        if (zoneCount >= 0) {
            logger.debug("Start multiple zones: zoneCount={}.", zoneCount);
        } else {
            logger.debug("Start multiple zones: zoneCount=unknown, requestLength={}.", zoneListJson.length());
        }
        httpPut(APIURL_BASE + APIURL_ZONE_PUT_MULTIPLE_START, zoneListJson, Priority.HIGH);
    }

    private int countStartMultipleZoneEntries(String zoneListJson) {
        try {
            JsonElement root = JsonParser.parseString(zoneListJson);
            if (root.isJsonObject()) {
                JsonElement zones = root.getAsJsonObject().get("zones");
                if (zones != null && zones.isJsonArray()) {
                    return zones.getAsJsonArray().size();
                }
            }
        } catch (RuntimeException e) {
            logger.trace("Unable to summarize multi-zone start request JSON: {}", e.getMessage());
        }
        return -1;
    }

    public void runZone(String zoneId, int duration) throws RachioApiException {
        logger.debug("Start zone '{}' for {} sec.", zoneId, duration);
        httpPut(APIURL_BASE + APIURL_ZONE_PUT_START, "{ \"id\" : \"" + zoneId + "\", \"duration\" : " + duration + " }",
                Priority.HIGH);
    }

    public void enableZone(String zoneId) throws RachioApiException {
        logger.debug("Enable zone '{}'.", zoneId);
        httpPut(APIURL_BASE + APIURL_ZONE_PUT_ENABLE, "{ \"id\" : \"" + zoneId + "\" }", Priority.HIGH);
    }

    public void disableZone(String zoneId) throws RachioApiException {
        logger.debug("Disable zone '{}'.", zoneId);
        httpPut(APIURL_BASE + APIURL_ZONE_PUT_DISABLE, "{ \"id\" : \"" + zoneId + "\" }", Priority.HIGH);
    }

    public RachioCurrentScheduleResponse getCurrentSchedule(String deviceId) throws RachioApiException {
        return getCurrentSchedule(deviceId, RequestPurpose.BACKGROUND_REFRESH);
    }

    public RachioCurrentScheduleResponse getCurrentSchedule(String deviceId, RequestPurpose requestPurpose)
            throws RachioApiException {
        logger.debug("Load current schedule for device '{}'.", deviceId);
        String json = httpGet(
                APIURL_BASE + APIURL_GET_DEVICE + "/" + deviceId + "/" + APIURL_GET_DEVICE_CURRENT_SCHEDULE, null,
                Priority.MEDIUM, requestPurpose).resultString;
        RachioCurrentScheduleResponse response = new Gson().fromJson(json, RachioCurrentScheduleResponse.class);
        return response != null ? response : new RachioCurrentScheduleResponse();
    }

    public RachioDeviceEventListResponse getDeviceEvents(String deviceId, long startTime, long endTime)
            throws RachioApiException {
        logger.debug("Load device events for device '{}' from {} to {}.", deviceId, startTime, endTime);
        String params = "startTime=" + startTime + "&endTime=" + endTime;
        String json = httpGet(APIURL_BASE + APIURL_GET_DEVICE + "/" + deviceId + "/" + APIURL_GET_DEVICE_EVENT, params,
                Priority.LOW).resultString;
        RachioDeviceEventListResponse response = RachioDeviceEventListResponse.fromJson(json);
        logger.debug("Loaded {} device events for device '{}'.", response.events.size(), deviceId);
        return response;
    }

    public RachioForecastResponse getDeviceForecast(String deviceId, String units) throws RachioApiException {
        String normalizedUnits = "US".equalsIgnoreCase(units) ? "US" : "METRIC";
        logger.debug("Load forecast for device '{}' using {} units.", deviceId, normalizedUnits);
        String json = httpGet(APIURL_BASE + APIURL_GET_DEVICE + "/" + deviceId + "/" + APIURL_GET_DEVICE_FORECAST,
                "units=" + urlEncode(normalizedUnits), Priority.LOW).resultString;
        return RachioForecastResponse.fromJson(json);
    }

    public List<RachioProperty> listProperties(String userId) throws RachioApiException {
        logger.debug("Load Rachio properties for user '{}'.", userId);
        String json = httpGet(APIURL_CLOUD_REST_BASE + PROPERTY_LIST + urlEncode(userId), null,
                Priority.LOW).resultString;
        RachioPropertyListResponse response = RachioPropertyListResponse.fromJson(json);
        logger.debug("Loaded {} Rachio properties for user '{}'.", response.properties.size(), userId);
        return response.properties;
    }

    public RachioProperty getProperty(String propertyId) throws RachioApiException {
        logger.debug("Load Rachio property '{}'.", propertyId);
        String json = httpGet(APIURL_CLOUD_REST_BASE + PROPERTY_GET + urlEncode(propertyId), null,
                Priority.LOW).resultString;
        RachioProperty property = RachioPropertyGsonDTO.parseProperty(json);
        return property != null ? property : new RachioProperty();
    }

    public Optional<RachioProperty> findPropertyByEntity(String entityId, String entityType) throws RachioApiException {
        String query = buildPropertyEntityQuery(entityId, entityType);
        logger.debug("Find Rachio property by entity type '{}'.", entityType);
        String json = httpGet(APIURL_CLOUD_REST_BASE + PROPERTY_FIND_BY_ENTITY, query, Priority.LOW).resultString;
        RachioPropertyEntityLookupResponse response = RachioPropertyEntityLookupResponse.fromJson(json);
        return Optional.ofNullable(response.getProperty());
    }

    public Optional<RachioProperty> findPropertyForLocation(String locationId) throws RachioApiException {
        return findPropertyByEntity(locationId, "locationId");
    }

    public Optional<RachioProperty> findPropertyForBaseStation(String baseStationId) throws RachioApiException {
        return findPropertyByEntity(baseStationId, "baseStationId");
    }

    public Optional<RachioProperty> findPropertyForLightingArea(String lightingAreaId) throws RachioApiException {
        return findPropertyByEntity(lightingAreaId, "lightingAreaId");
    }

    public List<RachioBaseStation> listBaseStations(String userId) throws RachioApiException {
        logger.debug("Load Rachio Smart Hose Timer base stations for user '{}'.", userId);
        String json = httpGet(APIURL_CLOUD_REST_BASE + VALVE_LIST_BASE_STATIONS + urlEncode(userId), null,
                Priority.LOW).resultString;
        RachioBaseStationListResponse response = RachioBaseStationListResponse.fromJson(json);
        logger.debug("Loaded {} Rachio Smart Hose Timer base stations for user '{}'.", response.baseStations.size(),
                userId);
        return response.baseStations;
    }

    public RachioBaseStation getBaseStation(String baseStationId) throws RachioApiException {
        return getBaseStation(baseStationId, RequestPurpose.BACKGROUND_REFRESH);
    }

    public RachioBaseStation getBaseStation(String baseStationId, RequestPurpose requestPurpose)
            throws RachioApiException {
        logger.debug("Load Rachio Smart Hose Timer base station '{}'.", baseStationId);
        String json = httpGet(APIURL_CLOUD_REST_BASE + VALVE_GET_BASE_STATION + urlEncode(baseStationId), null,
                readPriority(requestPurpose), requestPurpose).resultString;
        return RachioSmartHoseTimerGsonDTO.parseBaseStation(json);
    }

    public List<RachioValve> listValves(String baseStationId) throws RachioApiException {
        logger.debug("Load Rachio Smart Hose Timer valves for base station '{}'.", baseStationId);
        String json = httpGet(APIURL_CLOUD_REST_BASE + VALVE_LIST_VALVES + urlEncode(baseStationId), null,
                Priority.LOW).resultString;
        RachioValveListResponse response = RachioValveListResponse.fromJson(json);
        logger.debug("Loaded {} Rachio Smart Hose Timer valves for base station '{}'.", response.valves.size(),
                baseStationId);
        return response.valves;
    }

    public RachioValve getValve(String valveId) throws RachioApiException {
        return getValve(valveId, RequestPurpose.BACKGROUND_REFRESH);
    }

    public RachioValve getValve(String valveId, RequestPurpose requestPurpose) throws RachioApiException {
        logger.debug("Load Rachio Smart Hose Timer valve '{}'.", valveId);
        String json = httpGet(APIURL_CLOUD_REST_BASE + VALVE_GET_VALVE + urlEncode(valveId), null,
                readPriority(requestPurpose), requestPurpose).resultString;
        return RachioSmartHoseTimerGsonDTO.parseValve(json);
    }

    public void setValveDefaultRuntime(String valveId, int defaultRuntimeSeconds) throws RachioApiException {
        if (defaultRuntimeSeconds <= 0) {
            throw new RachioApiException("Valve default runtime must be greater than 0 seconds.");
        }
        logger.debug("Set Smart Hose Timer valve '{}' default runtime to {} sec.", valveId, defaultRuntimeSeconds);
        httpPut(APIURL_CLOUD_REST_BASE + VALVE_SET_DEFAULT_RUNTIME,
                buildValveDefaultRuntimePayload(valveId, defaultRuntimeSeconds), Priority.HIGH);
    }

    public void startValveWatering(String valveId, int durationSeconds) throws RachioApiException {
        if (durationSeconds <= 0) {
            throw new RachioApiException("Valve watering duration must be greater than 0 seconds.");
        }
        logger.debug("Start Smart Hose Timer valve '{}' for {} sec.", valveId, durationSeconds);
        httpPut(APIURL_CLOUD_REST_BASE + VALVE_START_WATERING, buildValveStartWateringPayload(valveId, durationSeconds),
                Priority.HIGH);
    }

    public void stopValveWatering(String valveId) throws RachioApiException {
        logger.debug("Stop Smart Hose Timer valve '{}'.", valveId);
        httpPut(APIURL_CLOUD_REST_BASE + VALVE_STOP_WATERING, buildValveStopWateringPayload(valveId), Priority.HIGH);
    }

    public List<RachioValveProgram> listValveProgramsV2ByBaseStation(String baseStationId) throws RachioApiException {
        logger.debug("Load Smart Hose Timer programs for base station '{}'.", baseStationId);
        String json = httpGet(APIURL_CLOUD_REST_BASE + PROGRAM_LIST_PROGRAMS_V2,
                PROGRAM_QUERY_BASE_STATION_ID + "=" + urlEncode(baseStationId), Priority.LOW).resultString;
        RachioValveProgramListResponse response = RachioValveProgramListResponse.fromJson(json);
        logger.debug("Loaded {} Smart Hose Timer programs for base station '{}'.", response.programs.size(),
                baseStationId);
        return response.programs;
    }

    public List<RachioValveProgram> listValveProgramsV2ByValve(String valveId) throws RachioApiException {
        logger.debug("Load Smart Hose Timer programs for valve '{}'.", valveId);
        String json = httpGet(APIURL_CLOUD_REST_BASE + PROGRAM_LIST_PROGRAMS_V2,
                PROGRAM_QUERY_VALVE_ID + "=" + urlEncode(valveId), Priority.LOW).resultString;
        RachioValveProgramListResponse response = RachioValveProgramListResponse.fromJson(json);
        logger.debug("Loaded {} Smart Hose Timer programs for valve '{}'.", response.programs.size(), valveId);
        return response.programs;
    }

    public List<RachioValveProgram> listValvePrograms(String entityId) throws RachioApiException {
        logger.debug("Load legacy Smart Hose Timer programs for entity '{}'.", entityId);
        String json = httpGet(APIURL_CLOUD_REST_BASE + PROGRAM_LIST_PROGRAMS + urlEncode(entityId), null,
                Priority.LOW).resultString;
        RachioValveProgramListResponse response = RachioValveProgramListResponse.fromJson(json);
        return response.programs;
    }

    public RachioValveProgram getValveProgramV2(String programId) throws RachioApiException {
        return getValveProgramV2(programId, RequestPurpose.BACKGROUND_REFRESH);
    }

    public RachioValveProgram getValveProgramV2(String programId, RequestPurpose requestPurpose)
            throws RachioApiException {
        logger.debug("Load Smart Hose Timer Program V2 '{}'.", programId);
        String json = httpGet(APIURL_CLOUD_REST_BASE + PROGRAM_GET_PROGRAM_V2 + urlEncode(programId), null,
                readPriority(requestPurpose), requestPurpose).resultString;
        return RachioSmartHoseTimerGsonDTO.parseValveProgram(json);
    }

    public RachioValveProgram getValveProgram(String programId) throws RachioApiException {
        return getValveProgram(programId, RequestPurpose.BACKGROUND_REFRESH);
    }

    public RachioValveProgram getValveProgram(String programId, RequestPurpose requestPurpose)
            throws RachioApiException {
        logger.debug("Load legacy Smart Hose Timer Program '{}'.", programId);
        String json = httpGet(APIURL_CLOUD_REST_BASE + PROGRAM_GET_PROGRAM + urlEncode(programId), null,
                readPriority(requestPurpose), requestPurpose).resultString;
        return RachioSmartHoseTimerGsonDTO.parseValveProgram(json);
    }

    public RachioValveProgram createValveProgramV2(RachioValveProgram program) throws RachioApiException {
        logger.debug("Create Smart Hose Timer Program V2 '{}'.", program.getThingName());
        String json = httpPost(APIURL_CLOUD_REST_BASE + PROGRAM_CREATE_PROGRAM_V2, new Gson().toJson(program),
                Priority.HIGH).resultString;
        return RachioSmartHoseTimerGsonDTO.parseValveProgram(json);
    }

    public RachioValveProgram updateValveProgramV2(RachioValveProgram program) throws RachioApiException {
        logger.debug("Update Smart Hose Timer Program V2 '{}'.", program.id);
        String json = httpPut(APIURL_CLOUD_REST_BASE + PROGRAM_UPDATE_PROGRAM_V2, new Gson().toJson(program),
                Priority.HIGH).resultString;
        return RachioSmartHoseTimerGsonDTO.parseValveProgram(json);
    }

    public void deleteValveProgram(String programId) throws RachioApiException {
        logger.debug("Delete Smart Hose Timer Program '{}'.", programId);
        httpDelete(APIURL_CLOUD_REST_BASE + PROGRAM_DELETE_PROGRAM + urlEncode(programId), null, Priority.HIGH);
    }

    public RachioValveDayViewsResponse getValveDayViews(String valveId, LocalDate start, LocalDate end)
            throws RachioApiException {
        logger.debug("Load Smart Hose Timer summary for valve '{}' from {} to {}.", valveId, start, end);
        String json = httpPost(APIURL_CLOUD_REST_BASE + SUMMARY_GET_VALVE_DAY_VIEWS,
                buildValveDayViewsPayload(valveId, start, end), Priority.LOW).resultString;
        return RachioValveDayViewsResponse.fromJson(json);
    }

    public void createSkipOverride(String programId, String timestamp) throws RachioApiException {
        logger.debug("Create Smart Hose Timer skip override for program '{}' at '{}'.", programId, timestamp);
        httpPost(APIURL_CLOUD_REST_BASE + PROGRAM_CREATE_SKIP_OVERRIDES,
                buildProgramSkipOverridePayload(programId, timestamp), Priority.HIGH);
    }

    public void deleteSkipOverride(String programId, String timestamp) throws RachioApiException {
        logger.debug("Delete Smart Hose Timer skip override for program '{}' at '{}'.", programId, timestamp);
        httpPost(APIURL_CLOUD_REST_BASE + PROGRAM_DELETE_SKIP_OVERRIDES,
                buildProgramSkipOverridePayload(programId, timestamp), Priority.HIGH);
    }

    public void createPlannedRunSkipOverride(String plannedRunId, String date) throws RachioApiException {
        logger.debug("Create Smart Hose Timer planned-run skip override for plannedRun '{}' on '{}'.", plannedRunId,
                date);
        httpPost(APIURL_CLOUD_REST_BASE + PROGRAM_CREATE_PLANNED_RUN_SKIP_OVERRIDES,
                buildPlannedRunSkipOverridePayload(plannedRunId, date), Priority.HIGH);
    }

    public void deletePlannedRunSkipOverride(String plannedRunId, String date) throws RachioApiException {
        logger.debug("Delete Smart Hose Timer planned-run skip override for plannedRun '{}' on '{}'.", plannedRunId,
                date);
        httpPost(APIURL_CLOUD_REST_BASE + PROGRAM_DELETE_PLANNED_RUN_SKIP_OVERRIDES,
                buildPlannedRunSkipOverridePayload(plannedRunId, date), Priority.HIGH);
    }

    public void setZoneMoistureLevel(String zoneId, double level) throws RachioApiException {
        logger.debug("Update zone moisture level for zone '{}' to {}.", zoneId, level);
        httpPut(APIURL_BASE + APIURL_ZONE_PUT_MOISTURE_LEVEL, buildMoistureLevelPayload(zoneId, level), Priority.HIGH);
    }

    public void setZoneMoisturePercent(String zoneId, double percent) throws RachioApiException {
        if (percent < 0 || percent > 1) {
            throw new RachioApiException("Moisture percent must be between 0 and 1.");
        }
        logger.debug("Update zone moisture percent for zone '{}' to {}.", zoneId, percent);
        httpPut(APIURL_BASE + APIURL_ZONE_PUT_MOISTURE_PERCENT, buildMoisturePercentPayload(zoneId, percent),
                Priority.HIGH);
    }

    public RachioScheduleRuleResponse getScheduleRule(String scheduleRuleId) throws RachioApiException {
        return getScheduleRule(scheduleRuleId, RequestPurpose.BACKGROUND_REFRESH);
    }

    public RachioScheduleRuleResponse getScheduleRule(String scheduleRuleId, RequestPurpose requestPurpose)
            throws RachioApiException {
        logger.debug("Load schedule rule '{}'.", scheduleRuleId);
        String json = httpGet(APIURL_BASE + APIURL_GET_SCHEDULE_RULE + "/" + scheduleRuleId, null,
                readPriority(requestPurpose), requestPurpose).resultString;
        RachioScheduleRuleResponse response = new Gson().fromJson(json, RachioScheduleRuleResponse.class);
        return response != null ? response : new RachioScheduleRuleResponse();
    }

    public RachioFlexScheduleRuleResponse getFlexScheduleRule(String flexScheduleRuleId) throws RachioApiException {
        return getFlexScheduleRule(flexScheduleRuleId, RequestPurpose.BACKGROUND_REFRESH);
    }

    public RachioFlexScheduleRuleResponse getFlexScheduleRule(String flexScheduleRuleId, RequestPurpose requestPurpose)
            throws RachioApiException {
        logger.debug("Load flex schedule rule '{}'.", flexScheduleRuleId);
        logger.debug("GET {}{}/{}", APIURL_BASE, APIURL_GET_FLEX_SCHEDULE_RULE, flexScheduleRuleId);
        String json = httpGet(APIURL_BASE + APIURL_GET_FLEX_SCHEDULE_RULE + "/" + flexScheduleRuleId, null,
                readPriority(requestPurpose), requestPurpose).resultString;
        RachioFlexScheduleRuleResponse response = new Gson().fromJson(json, RachioFlexScheduleRuleResponse.class);
        return response != null ? response : new RachioFlexScheduleRuleResponse();
    }

    public void startScheduleRule(String scheduleRuleId) throws RachioApiException {
        logger.debug("Start schedule rule '{}'.", scheduleRuleId);
        httpPut(APIURL_BASE + APIURL_SCHEDULE_RULE_PUT_START, buildScheduleRuleCommandPayload(scheduleRuleId),
                Priority.HIGH);
    }

    public void skipScheduleRule(String scheduleRuleId) throws RachioApiException {
        logger.debug("Skip schedule rule '{}'.", scheduleRuleId);
        httpPut(APIURL_BASE + APIURL_SCHEDULE_RULE_PUT_SKIP, buildScheduleRuleCommandPayload(scheduleRuleId),
                Priority.HIGH);
    }

    public void setScheduleRuleSeasonalAdjustment(String scheduleRuleId, double adjustment) throws RachioApiException {
        logger.debug("Set seasonal adjustment for schedule rule '{}' to {}.", scheduleRuleId, adjustment);
        httpPut(APIURL_BASE + APIURL_SCHEDULE_RULE_PUT_SEASONAL_ADJUSTMENT,
                buildSeasonalAdjustmentPayload(scheduleRuleId, adjustment), Priority.HIGH);
    }

    public void skipForwardZoneRun(String id) throws RachioApiException {
        logger.debug("Skip forward zone run for '{}'.", id);
        httpPut(APIURL_BASE + APIURL_SCHEDULE_RULE_PUT_SKIP_FORWARD_ZONE_RUN, buildScheduleRuleCommandPayload(id),
                Priority.HIGH);
    }

    static String buildMoistureLevelPayload(String zoneId, double level) {
        return new Gson().toJson(new RachioMoistureLevelRequest(zoneId, level));
    }

    static String buildMoisturePercentPayload(String zoneId, double percent) {
        return new Gson().toJson(new RachioMoisturePercentRequest(zoneId, percent));
    }

    static String buildScheduleRuleCommandPayload(String id) {
        return new Gson().toJson(new RachioScheduleRuleCommandRequest(id));
    }

    static String buildSeasonalAdjustmentPayload(String id, double adjustment) {
        return new Gson().toJson(new RachioSeasonalAdjustmentRequest(id, adjustment));
    }

    static String buildValveDefaultRuntimePayload(String valveId, int defaultRuntimeSeconds) {
        return new Gson().toJson(new RachioValveDefaultRuntimeRequest(valveId, defaultRuntimeSeconds));
    }

    static String buildValveStartWateringPayload(String valveId, int durationSeconds) {
        return new Gson().toJson(new RachioValveStartWateringRequest(valveId, durationSeconds));
    }

    static String buildValveStopWateringPayload(String valveId) {
        return new Gson().toJson(new RachioValveStopWateringRequest(valveId));
    }

    static String buildValveDayViewsPayload(String valveId, LocalDate start, LocalDate end) {
        return new Gson().toJson(new RachioValveDayViewsRequest(start, end, valveId));
    }

    static String buildProgramSkipOverridePayload(String programId, String timestamp) {
        return new Gson().toJson(new RachioProgramSkipOverrideRequest(programId, timestamp));
    }

    static String buildPlannedRunSkipOverridePayload(String plannedRunId, String date) {
        return new Gson().toJson(new RachioPlannedRunSkipOverrideRequest(plannedRunId, date));
    }

    static String buildPropertyEntityQuery(String entityId, String entityType) throws RachioApiException {
        String queryParameter = propertyEntityQueryParameter(entityType);
        if (entityId.isBlank()) {
            throw new RachioApiException("Property entity id must not be empty.");
        }
        return queryParameter + "=" + urlEncode(entityId);
    }

    private static String propertyEntityQueryParameter(String entityType) throws RachioApiException {
        String normalizedType = entityType.trim().replaceAll("([a-z0-9])([A-Z])", "$1-$2").replace('_', '-')
                .replace('.', '-').toLowerCase(Locale.ROOT);
        switch (normalizedType) {
            case "location":
            case "location-id":
            case "resource-id-location-id":
                return PROPERTY_QUERY_LOCATION_ID;
            case "base-station":
            case "base-station-id":
            case "resource-id-base-station-id":
                return PROPERTY_QUERY_BASE_STATION_ID;
            case "lighting-area":
            case "lighting-area-id":
            case "resource-id-lighting-area-id":
                return PROPERTY_QUERY_LIGHTING_AREA_ID;
            default:
                throw new RachioApiException(
                        "Unsupported PropertyService entity type. Expected locationId, baseStationId, or lightingAreaId.");
        }
    }

    public void getDeviceInfo(String deviceId) throws RachioApiException {
        httpGet(APIURL_BASE + APIURL_GET_DEVICE + "/" + deviceId, null, Priority.MEDIUM);
    }

    public List<RachioApiLegacyWebHookEventType> listLegacyNotificationEventTypes() throws RachioApiException {
        return listLegacyNotificationEventTypes(RequestPurpose.BACKGROUND_REFRESH);
    }

    public List<RachioApiLegacyWebHookEventType> listLegacyNotificationEventTypes(RequestPurpose requestPurpose)
            throws RachioApiException {
        String json = httpGet(APIURL_BASE + APIURL_DEV_WEBHOOK_EVENT_TYPES, null, Priority.MEDIUM,
                requestPurpose).resultString;
        return parseLegacyNotificationEventTypes(json);
    }

    static List<RachioApiLegacyWebHookEventType> parseLegacyNotificationEventTypes(String json) {
        JsonElement root = JsonParser.parseString(json);
        JsonArray entries;
        if (root.isJsonArray()) {
            entries = root.getAsJsonArray();
        } else if (root.isJsonObject()) {
            JsonElement eventTypes = root.getAsJsonObject().get("eventTypes");
            if (eventTypes == null || !eventTypes.isJsonArray()) {
                return List.of();
            }
            entries = eventTypes.getAsJsonArray();
        } else {
            return List.of();
        }

        Gson gson = new Gson();
        List<RachioApiLegacyWebHookEventType> eventTypes = new ArrayList<>();
        for (JsonElement entry : entries) {
            if (entry != null && entry.isJsonObject()) {
                RachioApiLegacyWebHookEventType eventType = gson.fromJson(entry, RachioApiLegacyWebHookEventType.class);
                if (eventType != null && !eventType.id.isBlank()) {
                    eventTypes.add(eventType);
                }
            }
        }
        return eventTypes;
    }

    public List<RachioApiWebHookEntry> listLegacyNotificationWebHooks(String deviceId, RequestPurpose requestPurpose)
            throws RachioApiException {
        String json = httpGet(APIURL_BASE + APIURL_DEV_QUERY_WEBHOOK + "/" + deviceId + "/webhook", null,
                Priority.MEDIUM, requestPurpose).resultString;
        return parseWebHookList(json);
    }

    public void deleteLegacyNotificationWebHook(String hookId, RequestPurpose requestPurpose)
            throws RachioApiException {
        httpDelete(APIURL_BASE + APIURL_DEV_DELETE_WEBHOOK + "/" + hookId, null, Priority.MEDIUM, requestPurpose);
    }

    public void registerLegacyNotificationWebHook(String deviceId, String callbackUrl, String callbackUsername,
            String callbackPassword, @Nullable String externalId, Boolean clearAllCallbacks,
            RequestPurpose requestPurpose) throws RachioApiException {
        String registrationUrl = buildWebhookRegistrationUrl(callbackUrl, callbackUsername, callbackPassword);
        String expectedExternalId = externalId != null ? externalId : "";
        logger.debug(
                "Register legacy NotificationService webhook for controller '{}', callbackUrl={}, userInfoPresent={}, clearAllCallbacks={}",
                deviceId, callbackUrlLogReference(registrationUrl), webhookUrlContainsUserInfo(registrationUrl),
                clearAllCallbacks);

        boolean matchingWebhookExists = reconcileLegacyNotificationWebHooks(
                listLegacyNotificationWebHooks(deviceId, requestPurpose), registrationUrl, expectedExternalId,
                getKnownExternalIds(externalId), clearAllCallbacks, requestPurpose);
        if (matchingWebhookExists) {
            logger.debug("Retain existing matching legacy NotificationService webhook for controller '{}'", deviceId);
            return;
        }

        List<Map<String, String>> eventTypes = List
                .of(WHE_DEVICE_STATUS, WHE_RAIN_DELAY, WEATHER_INTELLIGENCE, WHE_WATER_BUDGET, WHE_SCHEDULE_STATUS,
                        WHE_ZONE_STATUS, WHE_RAIN_SENSOR_DETECTION, WHE_ZONE_DELTA, WHE_DELTA)
                .stream().map(id -> Map.of("id", id)).toList();
        Map<String, Object> createPayload = Map.of("device", Map.of("id", deviceId), "externalId", expectedExternalId,
                "url", registrationUrl, "eventTypes", eventTypes);
        try {
            httpPost(APIURL_BASE + APIURL_DEV_POST_WEBHOOK, new Gson().toJson(createPayload), Priority.HIGH,
                    requestPurpose);
        } catch (RachioApiException e) {
            throw sanitizeWebhookRegistrationException(e, registrationUrl);
        }
    }

    private boolean reconcileLegacyNotificationWebHooks(List<RachioApiWebHookEntry> webhooks, String callbackUrl,
            String expectedExternalId, Collection<String> externalIds, Boolean clearAllCallbacks,
            RequestPurpose requestPurpose) {
        boolean deleteAll = Boolean.TRUE.equals(clearAllCallbacks);
        boolean matchingWebhookRetained = false;
        logger.debug("Registered legacy NotificationService webhook count: {}", webhooks.size());
        for (RachioApiWebHookEntry webhook : webhooks) {
            boolean matchesExpectedWebhook = Objects.equals(webhook.url, callbackUrl)
                    && Objects.equals(webhook.externalId, expectedExternalId);
            boolean ownedByBinding = Objects.equals(webhook.url, callbackUrl)
                    || externalIds.stream().anyMatch(id -> Objects.equals(webhook.externalId, id));
            if (!deleteAll && matchesExpectedWebhook && !matchingWebhookRetained) {
                matchingWebhookRetained = true;
                logger.debug("Retain existing matching legacy NotificationService webhook '{}'", webhook.id);
            } else if (deleteAll || ownedByBinding) {
                try {
                    logger.debug("Delete legacy NotificationService webhook '{}' ({})", webhook.id,
                            deleteAll ? "clearAllCallbacks=true" : "stale or duplicate binding callback");
                    deleteLegacyNotificationWebHook(webhook.id, requestPurpose);
                } catch (RachioApiException e) {
                    logger.debug("Deleting legacy NotificationService webhook '{}' failed: {}", webhook.id,
                            webhookRegistrationExceptionDiagnostic(e, webhook.url));
                }
            } else {
                logger.debug("Retain unrelated legacy NotificationService webhook '{}'", webhook.id);
            }
        }
        return matchingWebhookRetained;
    }

    public void registerWebHook(String deviceId, String callbackUrl, String callbackUsername, String callbackPassword,
            @Nullable String externalId, Boolean clearAllCallbacks) throws RachioApiException {
        registerWebHook(deviceId, callbackUrl, callbackUsername, callbackPassword, externalId, clearAllCallbacks,
                RequestPurpose.BACKGROUND_REFRESH);
    }

    public void registerWebHook(String deviceId, String callbackUrl, String callbackUsername, String callbackPassword,
            @Nullable String externalId, Boolean clearAllCallbacks, RequestPurpose requestPurpose)
            throws RachioApiException {
        logger.debug("Register webhook for device '{}', externalIdPresent={}, clearAllCallbacks={}", deviceId,
                externalId != null && !externalId.isBlank(), clearAllCallbacks);
        Map<RachioWebhookResourceType, Set<String>> supportedEventTypes = getSupportedWebhookEventTypeMap(
                requestPurpose);
        RachioWebhookTarget target = RachioWebhookTarget.irrigationController(deviceId,
                getIrrigationControllerEventTypes(supportedEventTypes));
        registerWebHookTarget(target, callbackUrl, callbackUsername, callbackPassword, externalId, clearAllCallbacks,
                supportedEventTypes, requestPurpose);
    }

    public void registerWebHookTarget(RachioWebhookTarget target, String callbackUrl, String callbackUsername,
            String callbackPassword, @Nullable String externalId, Boolean clearAllCallbacks) throws RachioApiException {
        registerWebHookTarget(target, callbackUrl, callbackUsername, callbackPassword, externalId, clearAllCallbacks,
                RequestPurpose.BACKGROUND_REFRESH);
    }

    public void registerWebHookTarget(RachioWebhookTarget target, String callbackUrl, String callbackUsername,
            String callbackPassword, @Nullable String externalId, Boolean clearAllCallbacks,
            RequestPurpose requestPurpose) throws RachioApiException {
        registerWebHookTarget(target, callbackUrl, callbackUsername, callbackPassword, externalId, clearAllCallbacks,
                getSupportedWebhookEventTypeMap(requestPurpose), requestPurpose);
    }

    private void registerWebHookTarget(RachioWebhookTarget target, String callbackUrl, String callbackUsername,
            String callbackPassword, @Nullable String externalId, Boolean clearAllCallbacks,
            Map<RachioWebhookResourceType, Set<String>> supportedEventTypes, RequestPurpose requestPurpose)
            throws RachioApiException {
        if (!target.getResourceType().isKnown() || target.getResourceId().isBlank()) {
            throw new RachioApiException("Webhook target must have a known resource type and non-empty resource ID.");
        }
        target = validateWebhookTargetEventTypes(target, supportedEventTypes);
        String registrationUrl;
        try {
            registrationUrl = buildWebhookRegistrationUrl(callbackUrl, callbackUsername, callbackPassword);
        } catch (RachioApiException e) {
            logger.warn("Failed to build callback URL for webhook target '{}': {}", target.describe(),
                    webhookRegistrationExceptionDiagnostic(e, callbackUrl));
            throw e;
        }
        logWebhookRegistrationUrlDiagnostic(target, callbackUrl, callbackUsername, callbackPassword, registrationUrl);

        String expectedExternalId = externalId != null ? externalId : "";
        logger.debug("Register WebHook for target '{}'", target.describe());
        try {
            String json = httpGet(APIURL_CLOUD_REST_BASE + WEBHOOK_LIST, target.buildListQuery(), Priority.MEDIUM,
                    requestPurpose).resultString;
            boolean matchingWebhookExists = reconcileExistingWebHooks(json, target, registrationUrl, expectedExternalId,
                    getKnownExternalIds(externalId), clearAllCallbacks, requestPurpose);
            if (matchingWebhookExists) {
                logger.debug("Retain existing matching webhook for target '{}'; createWebhook is not needed",
                        target.describe());
                return;
            }
        } catch (RuntimeException e) {
            logger.debug("Unable to verify existing webhooks for target '{}'; createWebhook skipped: {}",
                    target.describe(), webhookRegistrationExceptionDiagnostic(e, registrationUrl));
            throw new RachioApiException("Unable to verify existing webhook registrations for target '"
                    + target.describe() + "'; createWebhook skipped to avoid duplicate registration.");
        }

        try {
            Map<String, Object> createPayload = target.buildCreatePayload(registrationUrl, expectedExternalId);
            Object payloadUrlValue = createPayload.get("url");
            String payloadUrl = payloadUrlValue instanceof String url ? url : "";
            logger.debug(
                    "Creating webhook target={} eventTypes={} resourceId={} externalIdPresent={} payloadUrlUserInfo={} callbackUrl={}",
                    target.getResourceType().getApiValue(), target.getEventTypes(), target.getResourceId(),
                    !expectedExternalId.isBlank(), webhookUrlContainsUserInfo(payloadUrl),
                    callbackUrlLogReference(payloadUrl));
            httpPost(APIURL_CLOUD_REST_BASE + WEBHOOK_CREATE, new Gson().toJson(createPayload), Priority.HIGH,
                    requestPurpose);
        } catch (RachioApiException e) {
            throw sanitizeWebhookRegistrationException(e, registrationUrl);
        }
    }

    /**
     * Build the URL sent to Rachio when creating a webhook. If legacy callback credentials are configured, encode them
     * as URL userinfo while keeping diagnostics redacted.
     */
    private String buildWebhookRegistrationUrl(String callbackUrl, String callbackUsername, String callbackPassword)
            throws RachioApiException {
        if (callbackUrl.isBlank()) {
            throw new RachioApiException("Webhook callback URL is not configured.");
        }

        boolean usernameConfigured = !callbackUsername.isEmpty();
        boolean passwordConfigured = !callbackPassword.isEmpty();
        if (usernameConfigured != passwordConfigured) {
            throw new RachioApiException(
                    "Webhook callback credential configuration is incomplete: both callbackUsername and callbackPassword must be provided together.");
        }

        String trimmedCallbackUrl = callbackUrl.trim();
        if (!usernameConfigured) {
            URI callbackUri = parseWebhookCallbackUri(trimmedCallbackUrl, true);
            return callbackUri.toASCIIString();
        }

        URI callbackUri;
        try {
            callbackUri = parseWebhookCallbackUri(trimmedCallbackUrl, true);
        } catch (RachioApiException e) {
            String callbackUrlWithoutUserInfo = stripPotentialEmbeddedUserInfo(trimmedCallbackUrl);
            if (callbackUrlWithoutUserInfo.equals(trimmedCallbackUrl)) {
                throw e;
            }
            logger.debug(
                    "Callback URL contains embedded credentials, but explicit callbackUsername/callbackPassword fields are configured; using the explicit fields.");
            callbackUri = parseWebhookCallbackUri(callbackUrlWithoutUserInfo, true);
        }

        String rawAuthority = callbackUri.getRawAuthority();
        if (rawAuthority == null) {
            throw new RachioApiException("Invalid callback URL format: missing URL authority.");
        }

        String authorityWithoutUserInfo = rawAuthority;
        int userInfoSeparator = rawAuthority.lastIndexOf('@');
        if (userInfoSeparator >= 0) {
            logger.debug(
                    "Callback URL contains embedded credentials, but explicit callbackUsername/callbackPassword fields are configured; using the explicit fields.");
            authorityWithoutUserInfo = rawAuthority.substring(userInfoSeparator + 1);
        }

        if (authorityWithoutUserInfo.isEmpty() || authorityWithoutUserInfo.contains("@")) {
            throw new RachioApiException("Invalid callback URL format: invalid URL authority.");
        }

        String encodedUserInfo = encodeURIComponent(callbackUsername) + ":" + encodeURIComponent(callbackPassword);
        String registrationUrl = buildUriString(callbackUri, encodedUserInfo + "@" + authorityWithoutUserInfo);
        return parseWebhookCallbackUri(registrationUrl, true).toASCIIString();
    }

    private void logWebhookRegistrationUrlDiagnostic(RachioWebhookTarget target, String callbackUrl,
            String callbackUsername, String callbackPassword, String registrationUrl) {
        logger.debug(
                "Webhook registration URL prepared for target={} resourceId={} explicitUser={} explicitPassword={} originalUserInfo={} finalUserInfo={} callbackUrl={}",
                target.getResourceType().getApiValue(), target.getResourceId(), !callbackUsername.isEmpty(),
                !callbackPassword.isEmpty(), webhookUrlContainsUserInfo(callbackUrl),
                webhookUrlContainsUserInfo(registrationUrl), callbackUrlLogReference(registrationUrl));
    }

    static boolean webhookUrlContainsUserInfo(@Nullable String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            URI uri = new URI(url.trim());
            String rawAuthority = uri.getRawAuthority();
            return uri.getRawUserInfo() != null || (rawAuthority != null && rawAuthority.contains("@"));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    static String sanitizeWebhookUrlForDiagnostic(@Nullable String url) {
        return callbackUrlLogReference(url);
    }

    private URI parseWebhookCallbackUri(String callbackUrl, boolean requireValidHost) throws RachioApiException {
        try {
            URI uri = new URI(callbackUrl);
            if (!uri.isAbsolute() || uri.getRawAuthority() == null) {
                throw new RachioApiException("Invalid callback URL format: expected an absolute URL with a host.");
            }
            if (requireValidHost && uri.getHost() == null) {
                if (uri.getRawAuthority().contains("@")) {
                    throw new RachioApiException("Invalid callback URL format: malformed embedded credentials.");
                }
                throw new RachioApiException("Invalid callback URL format: expected a valid URL host.");
            }
            return uri;
        } catch (URISyntaxException e) {
            throw new RachioApiException("Invalid callback URL format: " + e.getReason());
        }
    }

    private String stripPotentialEmbeddedUserInfo(String callbackUrl) {
        int authorityStart = callbackUrl.indexOf("://");
        if (authorityStart < 0) {
            return callbackUrl;
        }

        authorityStart += 3;
        int userInfoSeparator = callbackUrl.lastIndexOf('@');
        if (userInfoSeparator < authorityStart) {
            return callbackUrl;
        }
        return callbackUrl.substring(0, authorityStart) + callbackUrl.substring(userInfoSeparator + 1);
    }

    private String buildUriString(URI uri, String authority) {
        StringBuilder url = new StringBuilder();
        url.append(uri.getScheme()).append("://").append(authority);

        String path = uri.getRawPath();
        if (path != null) {
            url.append(path);
        }
        String query = uri.getRawQuery();
        if (query != null) {
            url.append("?").append(query);
        }
        String fragment = uri.getRawFragment();
        if (fragment != null) {
            url.append("#").append(fragment);
        }
        return url.toString();
    }

    static String callbackUrlLogReference(@Nullable String url) {
        if (url == null || url.isBlank()) {
            return "callbackUrlHash=none";
        }
        String hash = getMD5Hash(url);
        return "callbackUrlHash=" + (hash.isBlank() ? "unavailable" : hash.substring(0, Math.min(12, hash.length())));
    }

    static String webhookRegistrationExceptionDiagnostic(Throwable e, @Nullable String registrationUrl) {
        return "cause=" + e.getClass().getSimpleName() + ", " + callbackUrlLogReference(registrationUrl);
    }

    private RachioApiException sanitizeWebhookRegistrationException(RachioApiException e, String registrationUrl) {
        if (e instanceof RachioApiThrottledException) {
            return e;
        }
        String diagnostic = webhookRegistrationExceptionDiagnostic(e, registrationUrl);
        RachioApiResult result = e.getApiResult();
        result.resultString = "Rachio webhook registration failed; " + diagnostic;
        return new RachioApiException("Rachio webhook registration failed; " + diagnostic, result);
    }

    /**
     * Encodes a URI component according to RFC 3986.
     * Unreserved characters (A-Z, a-z, 0-9, -, ., _, ~) are not encoded.
     * All other characters are percent-encoded.
     *
     * @param component the component to encode
     * @return the encoded component
     */
    private String encodeURIComponent(String component) {
        StringBuilder result = new StringBuilder();
        for (byte b : component.getBytes(StandardCharsets.UTF_8)) {
            int value = b & 0xFF;
            if ((value >= 'A' && value <= 'Z') || (value >= 'a' && value <= 'z') || (value >= '0' && value <= '9')
                    || value == '-' || value == '.' || value == '_' || value == '~') {
                result.append((char) value);
            } else {
                result.append('%').append(HEX_DIGITS[value >> 4]).append(HEX_DIGITS[value & 0x0F]);
            }
        }
        return result.toString();
    }

    private Collection<String> getKnownExternalIds(@Nullable String externalId) {
        if (externalId == null) {
            externalId = "";
        }
        List<String> knownExternalIds = new ArrayList<>(getLegacyExternalIds());
        if (!externalId.isBlank() && !knownExternalIds.contains(externalId)) {
            knownExternalIds.add(externalId);
        }
        return knownExternalIds;
    }

    private List<String> getIrrigationControllerEventTypes(
            Map<RachioWebhookResourceType, Set<String>> supportedEventTypesByResourceType) {
        List<String> eventTypes = new ArrayList<>(List.of(EVENT_DEVICE_ZONE_RUN_STARTED, EVENT_DEVICE_ZONE_RUN_STOPPED,
                EVENT_DEVICE_ZONE_RUN_COMPLETED, EVENT_DEVICE_ZONE_RUN_PAUSED, EVENT_SCHEDULE_STARTED,
                EVENT_SCHEDULE_STOPPED, EVENT_SCHEDULE_COMPLETED, EVENT_RAIN_SKIP, EVENT_CLIMATE_SKIP,
                EVENT_FREEZE_SKIP, EVENT_WIND_SKIP, EVENT_NO_SKIP));

        Set<String> supportedEventTypes = supportedEventTypesByResourceType
                .getOrDefault(RachioWebhookResourceType.IRRIGATION_CONTROLLER, Set.of());
        if (supportedEventTypes.contains(EVENT_RAIN_SENSOR_DETECTION_ON)) {
            eventTypes.add(EVENT_RAIN_SENSOR_DETECTION_ON);
        }
        if (supportedEventTypes.contains(EVENT_RAIN_SENSOR_DETECTION_OFF)) {
            eventTypes.add(EVENT_RAIN_SENSOR_DETECTION_OFF);
        }
        if (supportedEventTypes.contains(EVENT_RAIN_DELAY_ON)) {
            eventTypes.add(EVENT_RAIN_DELAY_ON);
        }
        if (supportedEventTypes.contains(EVENT_RAIN_DELAY_OFF)) {
            eventTypes.add(EVENT_RAIN_DELAY_OFF);
        }

        return eventTypes;
    }

    private Map<RachioWebhookResourceType, Set<String>> getSupportedWebhookEventTypeMap(RequestPurpose requestPurpose)
            throws RachioApiException {
        try {
            return listWebhookEventTypeMap(requestPurpose);
        } catch (RachioApiThrottledException e) {
            logger.debug(
                    "Unable to query supported webhook event types because the local Rachio API budget guard is active: {}",
                    e.getMessage());
            return Map.of();
        } catch (RachioApiException e) {
            logger.debug("Unable to query supported webhook event types: {}", e.getMessage());
            throw e;
        }
    }

    public List<String> listWebhookEventTypes() throws RachioApiException {
        return listWebhookEventTypes(RequestPurpose.BACKGROUND_REFRESH);
    }

    public List<String> listWebhookEventTypes(RequestPurpose requestPurpose) throws RachioApiException {
        Map<RachioWebhookResourceType, Set<String>> eventTypesByResourceType = listWebhookEventTypeMap(requestPurpose);
        LinkedHashSet<String> eventTypes = new LinkedHashSet<>();
        for (Set<String> resourceEventTypes : eventTypesByResourceType.values()) {
            eventTypes.addAll(resourceEventTypes);
        }
        return new ArrayList<>(eventTypes);
    }

    public Map<RachioWebhookResourceType, Set<String>> listWebhookEventTypeMap() throws RachioApiException {
        return listWebhookEventTypeMap(RequestPurpose.BACKGROUND_REFRESH);
    }

    public Map<RachioWebhookResourceType, Set<String>> listWebhookEventTypeMap(RequestPurpose requestPurpose)
            throws RachioApiException {
        String json = httpGet(APIURL_CLOUD_REST_BASE + WEBHOOK_LIST_EVENT_TYPES, null, Priority.MEDIUM,
                requestPurpose).resultString;
        Map<RachioWebhookResourceType, Set<String>> eventTypesByResourceType = parseWebhookEventTypeMap(json);
        logger.debug("Loaded Rachio webhook event types: {}", formatWebhookEventTypeCounts(eventTypesByResourceType));
        return eventTypesByResourceType;
    }

    static List<String> parseWebhookEventTypeList(String json) {
        LinkedHashSet<String> eventTypes = new LinkedHashSet<>();
        for (Set<String> resourceEventTypes : parseWebhookEventTypeMap(json).values()) {
            eventTypes.addAll(resourceEventTypes);
        }
        return new ArrayList<>(eventTypes);
    }

    static Map<RachioWebhookResourceType, Set<String>> parseWebhookEventTypeMap(String json) {
        return RachioApiWebhookEventTypesResponse.fromJson(json).toResourceEventTypeMap();
    }

    private String formatWebhookEventTypeCounts(Map<RachioWebhookResourceType, Set<String>> eventTypesByResourceType) {
        if (eventTypesByResourceType.isEmpty()) {
            return "none";
        }
        List<String> counts = new ArrayList<>();
        for (Map.Entry<RachioWebhookResourceType, Set<String>> entry : eventTypesByResourceType.entrySet()) {
            String resourceType = entry.getKey() == RachioWebhookResourceType.UNKNOWN ? "UNKNOWN"
                    : entry.getKey().getApiValue();
            counts.add(resourceType + "=" + entry.getValue().size());
        }
        return String.join(", ", counts);
    }

    private RachioWebhookTarget validateWebhookTargetEventTypes(RachioWebhookTarget target,
            Map<RachioWebhookResourceType, Set<String>> supportedEventTypesByResourceType) throws RachioApiException {
        if (supportedEventTypesByResourceType.isEmpty()) {
            logger.debug("Webhook event type catalog is unavailable; using configured event types for target '{}'",
                    target.describe());
            return target;
        }

        Set<String> supportedEventTypes = supportedEventTypesByResourceType.get(target.getResourceType());
        if (supportedEventTypes == null || supportedEventTypes.isEmpty()) {
            supportedEventTypes = supportedEventTypesByResourceType.get(RachioWebhookResourceType.UNKNOWN);
        }
        if (supportedEventTypes == null || supportedEventTypes.isEmpty()) {
            throw new RachioApiException("Webhook event type catalog has no entries for resource type '"
                    + target.getResourceType().getApiValue() + "'.");
        }

        Set<String> unsupportedEventTypes = target.getUnsupportedEventTypes(supportedEventTypes);
        if (unsupportedEventTypes.isEmpty()) {
            return target;
        }

        RachioWebhookTarget filteredTarget = target.filterEventTypes(supportedEventTypes);
        if (filteredTarget.getEventTypes().isEmpty()) {
            throw new RachioApiException("Webhook target '" + target.describe()
                    + "' has no event types supported for resource type '" + target.getResourceType().getApiValue()
                    + "'. Unsupported event types: " + unsupportedEventTypes);
        }

        logger.warn("Ignoring unsupported webhook event types for target '{}': {}", target.describe(),
                unsupportedEventTypes);
        return filteredTarget;
    }

    private boolean reconcileExistingWebHooks(String json, RachioWebhookTarget target, String callbackUrl,
            String expectedExternalId, Collection<String> externalIds, Boolean clearAllCallbacks,
            RequestPurpose requestPurpose) {
        boolean deleteAll = Boolean.TRUE.equals(clearAllCallbacks);
        boolean matchingWebhookRetained = false;
        List<RachioApiWebHookEntry> webhooks = parseWebHookList(json);
        logger.debug("Registered webhook count for target '{}': {}", target.describe(), webhooks.size());
        for (RachioApiWebHookEntry whe : webhooks) {
            logger.debug("WebHook: id='{}', callbackUrl={}, externalIdPresent={}, resourceId='{}'", whe.id,
                    callbackUrlLogReference(whe.url), whe.externalId != null && !whe.externalId.isBlank(),
                    whe.resourceId == null ? null : whe.resourceId.getResourceId(target.getResourceType()));
            boolean matchesExternalId = externalIds.stream().anyMatch(id -> Objects.equals(whe.externalId, id));
            boolean matchesExpectedWebhook = target.matches(whe, callbackUrl, expectedExternalId);
            if (deleteAll) {
                try {
                    logger.debug("Delete existing webhook '{}' for target '{}' because clearAllCallbacks=true", whe.id,
                            target.describe());
                    httpDelete(APIURL_CLOUD_REST_BASE + WEBHOOK_DELETE + whe.id, null, Priority.MEDIUM, requestPurpose);
                } catch (RachioApiException e) {
                    logger.debug("Deleting WebHook '{}' failed: {}", whe.id,
                            webhookRegistrationExceptionDiagnostic(e, whe.url));
                }
            } else if (matchesExpectedWebhook && !matchingWebhookRetained) {
                matchingWebhookRetained = true;
                logger.debug("Retain existing matching webhook '{}' for target '{}'", whe.id, target.describe());
            } else if (Objects.equals(whe.url, callbackUrl) || matchesExternalId) {
                try {
                    logger.debug(
                            "Delete stale or duplicate webhook '{}' for target '{}' because it matches this binding instance",
                            whe.id, target.describe());
                    httpDelete(APIURL_CLOUD_REST_BASE + WEBHOOK_DELETE + whe.id, null, Priority.MEDIUM, requestPurpose);
                } catch (RachioApiException e) {
                    logger.debug("Deleting WebHook '{}' failed: {}", whe.id,
                            webhookRegistrationExceptionDiagnostic(e, whe.url));
                }
            } else {
                logger.debug("Retain existing webhook '{}' for target '{}'; not owned by this binding instance", whe.id,
                        target.describe());
            }
        }
        return matchingWebhookRetained;
    }

    static List<RachioApiWebHookEntry> parseWebHookList(String json) {
        JsonElement root = JsonParser.parseString(json);
        JsonArray entries;
        if (root.isJsonArray()) {
            entries = root.getAsJsonArray();
        } else if (root.isJsonObject()) {
            JsonObject rootObject = root.getAsJsonObject();
            JsonElement webhooks = rootObject.get("webhooks");
            if (webhooks == null || !webhooks.isJsonArray()) {
                webhooks = rootObject.get("data");
            }
            if (webhooks != null && webhooks.isJsonArray()) {
                entries = webhooks.getAsJsonArray();
            } else if (rootObject.has("id") || rootObject.has("url")) {
                entries = new JsonArray();
                entries.add(rootObject);
            } else {
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }

        List<RachioApiWebHookEntry> webhooks = new ArrayList<>();
        for (JsonElement entry : entries) {
            if (entry == null || !entry.isJsonObject()) {
                continue;
            }
            webhooks.add(parseWebHookEntry(entry.getAsJsonObject()));
        }
        return webhooks;
    }

    private static RachioApiWebHookEntry parseWebHookEntry(JsonObject entry) {
        RachioApiWebHookEntry webhook = new RachioApiWebHookEntry();
        webhook.createDate = readJsonLong(entry, "createDate");
        webhook.lastUpdateDate = readJsonLong(entry, "lastUpdateDate");
        webhook.id = readJsonString(entry, "id");
        webhook.url = readJsonString(entry, "url");
        webhook.externalId = readJsonString(entry, "externalId");
        webhook.resourceId = parseWebHookResourceId(entry);

        JsonElement eventTypes = entry.get("eventTypes");
        if (eventTypes == null) {
            eventTypes = entry.get("event_types");
        }
        if (eventTypes != null && eventTypes.isJsonArray()) {
            for (JsonElement eventType : eventTypes.getAsJsonArray()) {
                String normalizedEventType = parseWebHookEventType(eventType);
                if (!normalizedEventType.isBlank()) {
                    webhook.eventTypes.add(normalizedEventType);
                }
            }
        }
        return webhook;
    }

    private static RachioApiWebHookResourceId parseWebHookResourceId(JsonObject entry) {
        RachioApiWebHookResourceId resourceId = new RachioApiWebHookResourceId();
        JsonElement resourceIdElement = entry.get("resourceId");
        if (resourceIdElement == null) {
            resourceIdElement = entry.get("resource_id");
        }
        if (resourceIdElement == null || !resourceIdElement.isJsonObject()) {
            return resourceId;
        }

        JsonObject resourceIdObject = resourceIdElement.getAsJsonObject();
        resourceId.irrigationControllerId = readJsonString(resourceIdObject, "irrigationControllerId",
                "irrigation_controller_id");
        resourceId.valveId = readJsonString(resourceIdObject, "valveId", "valve_id");
        resourceId.programId = readJsonString(resourceIdObject, "programId", "program_id");
        resourceId.lightingControllerId = readJsonString(resourceIdObject, "lightingControllerId",
                "lighting_controller_id");
        resourceId.lightingZoneId = readJsonString(resourceIdObject, "lightingZoneId", "lighting_zone_id");
        resourceId.lightingSceneId = readJsonString(resourceIdObject, "lightingSceneId", "lighting_scene_id");
        resourceId.lightingProgramId = readJsonString(resourceIdObject, "lightingProgramId", "lighting_program_id");
        return resourceId;
    }

    private static String parseWebHookEventType(@Nullable JsonElement eventType) {
        if (eventType == null) {
            return "";
        }
        if (eventType.isJsonPrimitive()) {
            return eventType.getAsJsonPrimitive().getAsString();
        }
        if (eventType.isJsonObject()) {
            return readJsonString(eventType.getAsJsonObject(), "id", "type", "name", "eventType");
        }
        return "";
    }

    private static String readJsonString(JsonObject object, String... memberNames) {
        for (String memberName : memberNames) {
            JsonElement value = object.get(memberName);
            if (value != null && value.isJsonPrimitive()) {
                return value.getAsJsonPrimitive().getAsString();
            }
        }
        return "";
    }

    private static long readJsonLong(JsonObject object, String memberName) {
        JsonElement value = object.get(memberName);
        if (value == null || !value.isJsonPrimitive()) {
            return -1;
        }
        try {
            return value.getAsLong();
        } catch (RuntimeException e) {
            return -1;
        }
    }

    private Boolean initializeDevices(ThingUID BridgeUID, Priority priority, RequestPurpose requestPurpose)
            throws RachioApiException {
        String json = httpGet(APIURL_BASE + APIURL_GET_PERSONID + "/" + personId, null, priority,
                requestPurpose).resultString;

        Gson gson = new Gson();
        RachioCloudStatus cloudStatus = Objects.requireNonNull(gson.fromJson(json, RachioCloudStatus.class));
        userName = cloudStatus.username;
        fullName = cloudStatus.fullName;
        email = cloudStatus.email;

        deviceList = new HashMap<String, RachioDevice>(); // discard current list
        for (int i = 0; i < cloudStatus.devices.size(); i++) {
            RachioCloudDevice device = cloudStatus.devices.get(i);
            if (!device.deleted) {
                deviceList.put(device.id, new RachioDevice(device));
                logger.trace("Device '{}' initialized, {} zones.", device.name, device.zones.size());
            }
        }
        return true;
    }

    public Boolean initializeZones() {
        return true;
    }

    public Map<String, String> fillProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_VENDOR, RachioBindingConstants.BINDING_VENDOR);
        properties.put(RachioBindingConstants.PROPERTY_PERSON_ID, personId);
        properties.put(RachioBindingConstants.PROPERTY_PERSON_USER, userName);
        properties.put(RachioBindingConstants.PROPERTY_PERSON_NAME, fullName);
        properties.put(RachioBindingConstants.PROPERTY_PERSON_EMAIL, email);
        return properties;
    }

    /**
     * Given a string, return the MD5 hash of the String.
     *
     * @param unhashed The string contents to be hashed.
     * @return MD5 Hashed value of the String. Null if there is a problem hashing the String.
     */
    protected static String getMD5Hash(String unhashed) {
        try {
            byte[] bytesOfMessage = unhashed.getBytes(StandardCharsets.UTF_8);

            MessageDigest md5 = MessageDigest.getInstance(MD5_HASH_ALGORITHM);

            byte[] hash = md5.digest(bytesOfMessage);

            StringBuilder sb = new StringBuilder(2 * hash.length);

            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }

            String digest = sb.toString();

            return digest;
        } catch (RuntimeException | NoSuchAlgorithmException e) {
            // logger.warn("Unexpected exception while generating MD5: {} ({})", e.getMessage(), e.getClass());
            return "";
        }
    }

    public static boolean isValidWebHookSignature(@Nullable String signature, byte[] requestBody, String apikey) {
        if (signature == null || apikey.isEmpty()) {
            return false;
        }

        // Rachio's published examples validate against the exact minified, sorted JSON request bytes. The servlet
        // therefore verifies the received raw request body instead of rewriting JSON before HMAC validation.
        byte[] signatureBytes = decodeWebHookSignature(signature);
        if (signatureBytes.length == 0) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(new SecretKeySpec(apikey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM));
            return MessageDigest.isEqual(mac.doFinal(requestBody), signatureBytes);
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    private static byte[] decodeWebHookSignature(String signature) {
        String hexSignature = signature.trim();
        if (hexSignature.regionMatches(true, 0, "sha256=", 0, 7)) {
            hexSignature = hexSignature.substring(7);
        }
        if (hexSignature.length() != WEBHOOK_SIGNATURE_LENGTH_BYTES * 2) {
            return new byte[0];
        }

        byte[] bytes = new byte[WEBHOOK_SIGNATURE_LENGTH_BYTES];
        for (int i = 0; i < hexSignature.length(); i += 2) {
            int high = Character.digit(hexSignature.charAt(i), 16);
            int low = Character.digit(hexSignature.charAt(i + 1), 16);
            if (high < 0 || low < 0) {
                return new byte[0];
            }
            bytes[i / 2] = (byte) ((high << 4) + low);
        }
        return bytes;
    }

    @SuppressWarnings("rawtypes")
    public static void copyMatchingFields(Object fromObj, Object toObj) {
        Class fromClass = fromObj.getClass();
        Class toClass = toObj.getClass();
        Class superclass = Objects.requireNonNull(toClass.getSuperclass());

        Field[] fields = fromClass.getFields(); // .getDeclaredFields();
        for (Field f : fields) {
            try {
                String fname = f.getName();
                Field t = superclass.getDeclaredField(fname);

                if (t.getType() == f.getType()) {
                    // extend this if to copy more immutable types if interested
                    if (t.getType() == String.class || t.getType() == int.class || t.getType() == long.class
                            || t.getType() == double.class || t.getType() == char.class || t.getType() == boolean.class
                            || t.getType() == Double.class || t.getType() == Integer.class || t.getType() == Long.class
                            || t.getType() == Character.class || t.getType() == Boolean.class) {
                        f.setAccessible(true);
                        t.setAccessible(true);
                        t.set(toObj, f.get(fromObj));
                    } else if (t.getType() == Date.class) {
                        // dates are not immutable, so clone non-null dates into the destination object
                        Date d = (Date) f.get(fromObj);
                        f.setAccessible(true);
                        t.setAccessible(true);
                        t.set(toObj, d != null ? d.clone() : null);
                    } else if (t.getType() == java.util.ArrayList.class) {
                        // dates are not immutable, so clone non-null dates into the destination object
                        ArrayList a = (ArrayList) f.get(fromObj);
                        f.setAccessible(true);
                        t.setAccessible(true);
                        t.set(toObj, a != null ? a.clone() : null);
                    }
                }
            } catch (NoSuchFieldException ex) {
                // skip it
            } catch (IllegalAccessException ex) {
                // Unable to copy field
            }
        }
    }
}
