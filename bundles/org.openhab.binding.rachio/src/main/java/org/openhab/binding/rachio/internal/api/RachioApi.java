/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.rachio.internal.RachioBindingConstants;
import org.openhab.binding.rachio.internal.api.json.RachioCloudDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link RachioApi} implements the interface to the Rachio cloud service (using http).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class RachioApi {
    private final Logger        logger             = LoggerFactory.getLogger(RachioApi.class);
    private static final String MD5_HASH_ALGORITHM = "MD5";
    private static final String UTF8_CHAR_SET      = "UTF-8";

    public static class RachioApiResult {
        private final Logger logger        = LoggerFactory.getLogger(RachioApiResult.class);

        public String        requestMethod = "";
        public String        url           = "";
        @Nullable
        public String        apikey        = "";
        public Integer       responseCode  = 0;
        public String        resultString  = "";

        public Integer       apiCalls      = 0;
        public Integer       rateLimit     = 0;
        public Integer       rateRemaining = 0;
        public String        rateReset     = "";

        public void setRateLimit(int rateLimit, int rateRemaining, String rateReset) {
            this.rateLimit = rateLimit;
            this.rateRemaining = rateRemaining;
            this.rateReset = rateReset;
        }

        public void setRateLimit(@Nullable String rateLimit, @Nullable String rateRemaining,
                @Nullable String rateReset) {
            if (rateLimit != null) {
                this.rateLimit = Integer.parseInt(rateLimit);
            }
            if (rateRemaining != null) {
                this.rateRemaining = Integer.parseInt(rateRemaining);
            }
            if (rateReset != null) {
                this.rateReset = rateReset;
            }

            if ((this.rateLimit == 0) || (this.rateRemaining == 0)) {
                return;
            }

            if (isRateLimitCritical()) {
                logger.error(
                        "RachioApi: Remaing number of API calls is getting critical: limit={}, remaining={}, reset at {}",
                        rateLimit, rateRemaining, rateReset);
                return;
            }
            if (isRateLimitWarning()) {
                logger.info(
                        "RachioApi: Remaing number of remaining API calls is low: limit={}, remaining={}, reset at {}",
                        rateLimit, rateRemaining, rateReset);
                return;
            }

            logger.trace("RachioApi: Remaing number of API: limit={}, remaining={}, reset at {}", rateLimit,
                    this.rateRemaining, this.rateReset);
        }

        public boolean isRateLimitWarning() {
            return (rateRemaining > 0) && (rateRemaining < RACHIO_RATE_LIMIT_WARNING);
        }

        public boolean isRateLimitCritical() {
            return (rateRemaining > 0) && (rateRemaining <= RACHIO_RATE_LIMIT_CRITICAL);
        }

        public boolean isRateLimitBlocked() {
            return (rateRemaining > 0) && (rateRemaining <= RACHIO_RATE_LIMIT_BLOCK);
        }
    }

    protected String                      apikey         = "";
    protected String                      personId       = "";
    protected String                      userName       = "";
    protected String                      fullName       = "";
    protected String                      email          = "";

    protected RachioApiResult             lastApiResult  = new RachioApiResult();
    protected static final Integer        externalIdSalt = (int) (Math.random() * 50 + 1);

    private HashMap<String, RachioDevice> deviceList     = new HashMap<String, RachioDevice>();
    @Nullable
    private RachioHttp                    httpApi        = null;

    class RachioCloudPersonId {
        String id = ""; // "id":"xxxxxxx-xxxx-xxxx-xxxx-xxxxxxxx"
    }

    class RachioCloudStatus {
        long                                createDate = -1; // "createDate":1494626927000,
        String                              id         = ""; // "id":"xxxxxxx-xxxx-xxxx-xxxx-xxxxxxxx",
        String                              username   = ""; // "username":"markus7017",
        String                              fullName   = ""; // "fullName":"Markus Michels",
        String                              email      = ""; // "email":"markus.michels@me.com",
        public ArrayList<RachioCloudDevice> devices    = new ArrayList<>(); // "devices":[]
        boolean                             deleted    = false; // "deleted":false
    }

    public class RachioApiWebHookEntry {
        public long   createDate     = -1;
        public long   lastUpdateDate = -1;
        public String id             = "";
        public String url            = "";
        public String externalId     = "";
    }

    public class RachioApiWebHookList {
        public ArrayList<RachioApiWebHookEntry> webhooks = new ArrayList<>();

    }

    public class RachioCloudDelta {
        // V3: ZONE_DELTA / SCHEDULE_DELTA
        String routingId  = ""; // "routingId" : "d3beb3ab-b85a-49fe-a45d-37c4d95ea9a8",
        String icon       = ""; // "icon" : "NO_ICON",
        String action     = ""; // "action" : "UPDATED",
        String zoneId     = ""; // "zoneId" : "e49c8b55-a553-4733-b1cf-0e402b97db49",
        String externalId = ""; // "externalId" : "cc765dfb-d095-4ceb-8062-b9d88dcce911",
        String subType    = ""; // "subType" : "ZONE_DELTA",
        String id         = ""; // "id" : "e9d4fa9f-1619-37c4-b457-3845620643d2",
        String type       = ""; // "type" : "DELTA",
        String category   = "";  // "category" : "DEVICE",
        String deviceId   = ""; // "deviceId" : "d3beb3ab-b85a-49fe-a45d-37c4d95ea9a8",
        String timestamp  = ""; // "timestamp" : "2018-04-09T23:17:14.365Z"
    }

    @SuppressWarnings("null")
    public RachioApi(String personId) {
        if (personId != null) {
            this.personId = personId;
        }
    }

    public RachioApiResult getLastApiResult() {
        return lastApiResult;
    }

    protected void setApiResult(RachioApiResult result) {
        lastApiResult = result;
    }

    public String getPersonId() {
        return personId;
    }

    @Nullable
    public String getExternalId() {
        // return a salted ash of the apikey
        String hash = "OH_" + getMD5Hash(apikey) + "_" + externalIdSalt.toString();
        return getMD5Hash(hash);
    }

    public boolean initialize(String apikey, ThingUID bridgeUID) throws RachioApiException {
        this.apikey = apikey;
        httpApi = new RachioHttp(this.apikey);
        if (initializePersonId() && initializeDevices(bridgeUID) && initializeZones()) {
            logger.trace("Rachio API initialized");
            return true;
        }

        httpApi = null;
        logger.error("RachioApi.initialize(): API initialization failed!");
        return false;
    }

    public HashMap<String, RachioDevice> getDevices() {
        return deviceList;
    }

    @SuppressWarnings({ "null" })
    @Nullable
    public RachioDevice getDevByUID(@Nullable ThingUID bridgeUID, @Nullable ThingUID thingUID) {
        for (HashMap.Entry<String, RachioDevice> entry : deviceList.entrySet()) {
            RachioDevice dev = entry.getValue();
            logger.trace("RachioDev.getDevByUID: bridge {} / {}, device {} / {}", bridgeUID, dev.bridge_uid, thingUID,
                    dev.dev_uid);
            Validate.notNull(dev);
            Validate.notNull(dev.bridge_uid);
            if (dev.bridge_uid.equals(bridgeUID) && dev.getUID().equals(thingUID)) {
                logger.trace("RachioApi: Device '{}' found.", dev.name);
                return dev;
            }
        }
        logger.debug("RachioApi.getDevByUID: Unable map UID to device");
        return null;
    }

    @SuppressWarnings({ "null", "unused" })
    @Nullable
    public RachioZone getZoneByUID(@Nullable ThingUID bridgeUID, @Nullable ThingUID zoneUID) {
        Validate.notNull(bridgeUID);
        Validate.notNull(zoneUID);
        HashMap<String, RachioDevice> deviceList = getDevices();
        if (deviceList == null) {
            return null;
        }
        for (HashMap.Entry<String, RachioDevice> de : deviceList.entrySet()) {
            RachioDevice dev = de.getValue();

            HashMap<String, RachioZone> zoneList = dev.getZones();
            for (HashMap.Entry<String, RachioZone> ze : zoneList.entrySet()) {
                RachioZone zone = ze.getValue();
                if (zone.getUID().equals(zoneUID)) {
                    return zone;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("null")
    private Boolean initializePersonId() throws RachioApiException, RachioApiException {
        if (!personId.isEmpty()) {
            logger.trace("RachioApi: Using cached personId ('{}').", personId);
            return true;
        }

        Validate.notNull(httpApi);
        lastApiResult = httpApi.httpGet(APIURL_BASE + APIURL_GET_PERSON, null);
        @Nullable
        Gson gson = new Gson();
        Validate.notNull(gson);
        @Nullable
        RachioCloudPersonId pid = gson.fromJson(lastApiResult.resultString, RachioCloudPersonId.class);
        Validate.notNull(pid);
        personId = pid.id;
        logger.debug("Using personId '{}'", personId);
        if (lastApiResult.isRateLimitCritical()) {
            String errorMessage = MessageFormat.format(
                    "Rachio Cloud API Rate Limit is critical ({0} of {1}), reset at {2}", lastApiResult.rateRemaining,
                    lastApiResult.rateLimit, lastApiResult.rateReset);
            throw new RachioApiException(errorMessage, lastApiResult);
        }
        return true;
    }

    @SuppressWarnings("null")
    public String getUserInfo() {
        return userName != null ? fullName + "(" + userName + ", " + email + ")" : "";
    }

    @SuppressWarnings("null")
    public void stopWatering(String deviceId) throws RachioApiException {
        logger.debug("RachioApi. Stop watering for device '{}'", deviceId);
        Validate.notNull(httpApi);
        httpApi.httpPut(APIURL_BASE + APIURL_DEV_PUT_STOP, "{ \"id\" : \"" + deviceId + "\" }");
    }

    @SuppressWarnings("null")
    public void enableDevice(String deviceId) throws RachioApiException {
        logger.debug("RachioApi: Enable device '{}'.", deviceId);
        Validate.notNull(httpApi);
        httpApi.httpPut(APIURL_BASE + APIURL_DEV_PUT_ON, "{ \"id\" : \"" + deviceId + "\" }");
    }

    @SuppressWarnings("null")
    public void disableDevice(String deviceId) throws RachioApiException {
        logger.debug("RachioApi: Disable device '{}'.", deviceId);
        Validate.notNull(httpApi);
        httpApi.httpPut(APIURL_BASE + APIURL_DEV_PUT_OFF, "{ \"id\" : \"" + deviceId + "\" }");
    }

    @SuppressWarnings("null")
    public void rainDelay(String deviceId, Integer delay) throws RachioApiException {
        logger.debug("RachioApi: Start dain relay for device '{}'.", deviceId);
        Validate.notNull(httpApi);
        httpApi.httpPut(APIURL_BASE + APIURL_DEV_PUT_RAIN_DELAY,
                "{ \"id\" : \"" + deviceId + "\", \"durartion\" : " + delay + " }");
    }

    @SuppressWarnings("null")
    public void runMultilpeZones(String zoneListJson) throws RachioApiException {
        logger.debug("RachioApi: Start multiple zones '{}'.", zoneListJson);
        Validate.notNull(httpApi);
        httpApi.httpPut(APIURL_BASE + APIURL_ZONE_PUT_MULTIPLE_START, zoneListJson);
    }

    @SuppressWarnings("null")
    public void runZone(String zoneId, int duration) throws RachioApiException {
        logger.debug("RachioApi: Start zone '{}' for {} sec.", zoneId, duration);
        Validate.notNull(httpApi);
        httpApi.httpPut(APIURL_BASE + APIURL_ZONE_PUT_START,
                "{ \"id\" : \"" + zoneId + "\", \"duration\" : " + duration + " }");
    }

    @SuppressWarnings("null")
    public void getDeviceInfo(String deviceId) throws RachioApiException {
        Validate.notNull(httpApi);
        httpApi.httpGet(APIURL_BASE + APIURL_GET_DEVICE + "/" + deviceId, null);
    }

    @SuppressWarnings("null")
    public void registerWebHook(String deviceId, String callbackUrl, @Nullable String externalId,
            Boolean clearAllCallbacks) throws RachioApiException {
        // first check/delete existing webhooks
        logger.debug("RachioApi: Register webhook, url={}, externalId={}, clearAllCallbacks={}", callbackUrl,
                externalId, clearAllCallbacks.toString());

        String json = "";
        Validate.notNull(httpApi);
        try {
            Validate.notNull(httpApi);
            json = httpApi.httpGet(APIURL_BASE + APIURL_DEV_QUERY_WEBHOOK + "/" + deviceId + "/webhook",
                    null).resultString; // throws
            logger.debug("RachioApi: Registered webhooks for device '{}': {}", deviceId, json);
            logger.trace("RachioWebHook: Registered WebHooks - JSON='{}'", json);
            json = "{\"webhooks\":" + json + "}";
            Gson gson = new Gson();
            RachioApiWebHookList list = gson.fromJson(json, RachioApiWebHookList.class);
            for (int i = 0; i < list.webhooks.size(); i++) {
                RachioApiWebHookEntry whe = list.webhooks.get(i);
                logger.debug("RachioApi: WebHook #{}: id='{}', url='{}', externalId='{}'", i, whe.id, whe.url,
                        whe.externalId);
                if (clearAllCallbacks || whe.url.equals(callbackUrl)) {
                    logger.debug("RachioApi: The callback url '{}' is already registered -> delete", callbackUrl);
                    httpApi.httpDelete(APIURL_BASE + APIURL_DEV_DELETE_WEBHOOK + "/" + whe.id, null);
                }
            }
        } catch (Exception e) {
            logger.debug("RachioApi: Deleting WebHook(s); failed: {}, JSON='{}'", e.getMessage(), json);
        }

        // Build json, example:
        // {
        // "device":{"id":"2a5e7d3c-c140-4e2e-91a1-a212a518adc5"},
        // "externalId" : "external company ID",
        // "url":"https://www.mydomain.com/another_webhook",
        // "eventTypes":[{"id":"1"},{"id":"2"}]
        // }
        //
        logger.debug("RachioApi: Register WebHook, callback url = '{}'", callbackUrl);
        String jsonData = "{ " + "\"device\":{\"id\":\"" + deviceId + "\"}, " + "\"externalId\" : \"" + externalId
                + "\", " + "\"url\" : \"" + callbackUrl + "\", " + "\"eventTypes\" : [" + "{\"id\" : \""
                + WHE_DEVICE_STATUS + "\"}, " + "{\"id\" : \"" + WHE_RAIN_DELAY + "\"}, " + "{\"id\" : \""
                + WEATHER_INTELLIGENCE + "\"}, " + "{\"id\" : \"" + WHE_WATER_BUDGET + "\"}, " + "{\"id\" : \""
                + WHE_ZONE_DELTA + "\"}, " + "{\"id\" : \"" + WHE_SCHEDULE_STATUS + "\"}, " + "{\"id\" : \""
                + WHE_ZONE_STATUS + "\"}, " + "{\"id\" : \"" + WHE_RAIN_SENSOR_DETECTION + "\"}, " + "{\"id\" : \""
                + WHE_DELTA + "\"} " + "]" + "}";
        httpApi.httpPost(APIURL_BASE + APIURL_DEV_POST_WEBHOOK, jsonData);
    }

    @SuppressWarnings("null")
    private Boolean initializeDevices(ThingUID BridgeUID) throws RachioApiException {
        String json = "";
        if (httpApi == null) {
            logger.debug("RachioApi.initializeDevices: httpAPI not initialized");
            return false;
        }
        Validate.notNull(httpApi);
        json = httpApi.httpGet(APIURL_BASE + APIURL_GET_PERSONID + "/" + personId, null).resultString;
        logger.trace("RachioApi: Initialize from JSON='{}'", json);

        @Nullable
        Gson gson = new Gson();
        Validate.notNull(gson);
        @Nullable
        RachioCloudStatus cloudStatus = gson.fromJson(json, RachioCloudStatus.class);
        Validate.notNull(cloudStatus);
        userName = cloudStatus.username;
        fullName = cloudStatus.fullName;
        email = cloudStatus.email;

        deviceList = new HashMap<String, RachioDevice>(); // discard current list
        Validate.notNull(deviceList);
        for (int i = 0; i < cloudStatus.devices.size(); i++) {
            @Nullable
            RachioCloudDevice device = cloudStatus.devices.get(i);
            Validate.notNull(device);
            if (!device.deleted) {
                deviceList.put(device.id, new RachioDevice(device));
                logger.trace("RachioApi: Device '{}' initialized, {} zones.", device.name, device.zones.size());
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
        properties.put(RachioBindingConstants.PROPERTY_APIKEY, apikey);
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
    @Nullable
    protected static String getMD5Hash(String unhashed) {
        try {
            byte[] bytesOfMessage = unhashed.getBytes(UTF8_CHAR_SET);

            MessageDigest md5 = MessageDigest.getInstance(MD5_HASH_ALGORITHM);

            byte[] hash = md5.digest(bytesOfMessage);

            StringBuilder sb = new StringBuilder(2 * hash.length);

            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }

            String digest = sb.toString();

            return digest;
        } catch (RuntimeException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
            // logger.warn("Unexpected exception while generating MD5: {} ({})", e.getMessage(), e.getClass());
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    public static void copyMatchingFields(Object fromObj, Object toObj) {
        Validate.notNull(fromObj, "Source and destination objects must not be null");
        Validate.notNull(toObj, "Source and destination objects must not be null");

        Class fromClass = fromObj.getClass();
        Class toClass = toObj.getClass();

        Field[] fields = fromClass.getFields(); // .getDeclaredFields();
        for (Field f : fields) {
            try {
                String fname = f.getName();
                Field t = toClass.getSuperclass().getDeclaredField(fname);

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
                    } else {
                        // logger.debug("RachioApiInternal: Unable to update field '{}', '{}'", t.getName(), t.getType());
                    }
                }
            } catch (NoSuchFieldException ex) {
                // skip it
            } catch (IllegalAccessException ex) {
                // logger.error("Unable to copy field: {}", f.getName());
            }
        }
    }

}
