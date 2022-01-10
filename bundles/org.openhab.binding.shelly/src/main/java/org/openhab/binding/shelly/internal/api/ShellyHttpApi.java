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
package org.openhab.binding.shelly.internal.api;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyControlRoller;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyOtaCheckResult;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySendKeyList;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySenseKeyCode;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsDevice;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsLight;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsLogin;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsUpdate;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyShortLightStatus;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyStatusLight;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyStatusRelay;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * {@link ShellyHttpApi} wraps the Shelly REST API and provides various low level function to access the device api (not
 * cloud api).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyHttpApi {
    public static final String HTTP_HEADER_AUTH = "Authorization";
    public static final String HTTP_AUTH_TYPE_BASIC = "Basic";
    public static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

    private final Logger logger = LoggerFactory.getLogger(ShellyHttpApi.class);
    private final HttpClient httpClient;
    private ShellyThingConfiguration config = new ShellyThingConfiguration();
    private String thingName;
    private final Gson gson = new Gson();
    private int timeoutErrors = 0;
    private int timeoutsRecovered = 0;

    private ShellyDeviceProfile profile = new ShellyDeviceProfile();

    public ShellyHttpApi(String thingName, ShellyThingConfiguration config, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.thingName = thingName;
        setConfig(thingName, config);
        profile.initFromThingType(thingName);
    }

    public void setConfig(String thingName, ShellyThingConfiguration config) {
        this.thingName = thingName;
        this.config = config;
    }

    public ShellySettingsDevice getDevInfo() throws ShellyApiException {
        return callApi(SHELLY_URL_DEVINFO, ShellySettingsDevice.class);
    }

    public String setDebug(boolean enabled) throws ShellyApiException {
        return callApi(SHELLY_URL_SETTINGS + "?debug_enable=" + Boolean.valueOf(enabled), String.class);
    }

    public String getDebugLog(String id) throws ShellyApiException {
        return callApi("/debug/" + id, String.class);
    }

    /**
     * Initialize the device profile
     *
     * @param thingType Type of DEVICE as returned from the thing properties (based on discovery)
     * @return Initialized ShellyDeviceProfile
     * @throws ShellyApiException
     */
    public ShellyDeviceProfile getDeviceProfile(String thingType) throws ShellyApiException {
        String json = request(SHELLY_URL_SETTINGS);
        if (json.contains("\"type\":\"SHDM-")) {
            logger.trace("{}: Detected a Shelly Dimmer: fix Json (replace lights[] tag with dimmers[]", thingName);
            json = fixDimmerJson(json);
        }

        // Map settings to device profile for Light and Sense
        profile.initialize(thingType, json);

        // 2nd level initialization
        profile.thingName = profile.hostname;
        if (profile.isLight && (profile.numMeters == 0)) {
            logger.debug("{}: Get number of meters from light status", thingName);
            ShellyStatusLight status = getLightStatus();
            profile.numMeters = status.meters != null ? status.meters.size() : 0;
        }
        if (profile.isSense) {
            profile.irCodes = getIRCodeList();
            logger.debug("{}: Sense stored key list loaded, {} entries.", thingName, profile.irCodes.size());
        }

        return profile;
    }

    public boolean isInitialized() {
        return profile.initialized;
    }

    /**
     * Get generic device settings/status. Json returned from API will be mapped to a Gson object
     *
     * @return Device settings/status as ShellySettingsStatus object
     * @throws ShellyApiException
     */
    public ShellySettingsStatus getStatus() throws ShellyApiException {
        String json = "";
        try {
            json = request(SHELLY_URL_STATUS);
            // Dimmer2 returns invalid json type for loaderror :-(
            json = getString(json.replace("\"loaderror\":0,", "\"loaderror\":false,"));
            json = getString(json.replace("\"loaderror\":1,", "\"loaderror\":true,"));
            ShellySettingsStatus status = fromJson(gson, json, ShellySettingsStatus.class);
            status.json = json;
            return status;
        } catch (JsonSyntaxException e) {
            throw new ShellyApiException("Unable to parse JSON: " + json, e);
        }
    }

    public ShellyStatusRelay getRelayStatus(Integer relayIndex) throws ShellyApiException {
        return callApi(SHELLY_URL_STATUS_RELEAY + "/" + relayIndex.toString(), ShellyStatusRelay.class);
    }

    public ShellyShortLightStatus setRelayTurn(Integer id, String turnMode) throws ShellyApiException {
        return callApi(getControlUriPrefix(id) + "?" + SHELLY_LIGHT_TURN + "=" + turnMode.toLowerCase(),
                ShellyShortLightStatus.class);
    }

    public void setBrightness(Integer id, Integer brightness, boolean autoOn) throws ShellyApiException {
        String turn = autoOn ? SHELLY_LIGHT_TURN + "=" + SHELLY_API_ON + "&" : "";
        request(getControlUriPrefix(id) + "?" + turn + "brightness=" + brightness.toString());
    }

    public ShellyControlRoller getRollerStatus(Integer rollerIndex) throws ShellyApiException {
        String uri = SHELLY_URL_CONTROL_ROLLER + "/" + rollerIndex.toString() + "/pos";
        return callApi(uri, ShellyControlRoller.class);
    }

    public void setRollerTurn(Integer relayIndex, String turnMode) throws ShellyApiException {
        request(SHELLY_URL_CONTROL_ROLLER + "/" + relayIndex.toString() + "?go=" + turnMode);
    }

    public void setRollerPos(Integer relayIndex, Integer position) throws ShellyApiException {
        request(SHELLY_URL_CONTROL_ROLLER + "/" + relayIndex.toString() + "?go=to_pos&roller_pos="
                + position.toString());
    }

    public void setRollerTimer(Integer relayIndex, Integer timer) throws ShellyApiException {
        request(SHELLY_URL_CONTROL_ROLLER + "/" + relayIndex.toString() + "?timer=" + timer.toString());
    }

    public ShellyShortLightStatus getLightStatus(Integer index) throws ShellyApiException {
        return callApi(getControlUriPrefix(index), ShellyShortLightStatus.class);
    }

    public ShellyStatusSensor getSensorStatus() throws ShellyApiException {
        ShellyStatusSensor status = callApi(SHELLY_URL_STATUS, ShellyStatusSensor.class);
        if (profile.isSense) {
            // complete reported data, map C to F or vice versa: C=(F - 32) * 0.5556;
            status.tmp.tC = status.tmp.units.equals(SHELLY_TEMP_CELSIUS) ? status.tmp.value
                    : ImperialUnits.FAHRENHEIT.getConverterTo(SIUnits.CELSIUS).convert(getDouble(status.tmp.value))
                            .doubleValue();
            double f = SIUnits.CELSIUS.getConverterTo(ImperialUnits.FAHRENHEIT).convert(getDouble(status.tmp.value))
                    .doubleValue();
            status.tmp.tF = status.tmp.units.equals(SHELLY_TEMP_FAHRENHEIT) ? status.tmp.value : f;
        }
        if ((status.charger == null) && (profile.settings.externalPower != null)) {
            // SHelly H&T uses external_power, Sense uses charger
            status.charger = profile.settings.externalPower != 0;
        }
        return status;
    }

    public void setTimer(int index, String timerName, int value) throws ShellyApiException {
        String type = SHELLY_CLASS_RELAY;
        if (profile.isRoller) {
            type = SHELLY_CLASS_ROLLER;
        } else if (profile.isLight) {
            type = SHELLY_CLASS_LIGHT;
        }
        String uri = SHELLY_URL_SETTINGS + "/" + type + "/" + index + "?" + timerName + "=" + value;
        request(uri);
    }

    public void setSleepTime(int value) throws ShellyApiException {
        request(SHELLY_URL_SETTINGS + "?sleep_time=" + value);
    }

    public void setLedStatus(String ledName, Boolean value) throws ShellyApiException {
        request(SHELLY_URL_SETTINGS + "?" + ledName + "=" + (value ? SHELLY_API_TRUE : SHELLY_API_FALSE));
    }

    public ShellySettingsLight getLightSettings() throws ShellyApiException {
        return callApi(SHELLY_URL_SETTINGS_LIGHT, ShellySettingsLight.class);
    }

    public ShellyStatusLight getLightStatus() throws ShellyApiException {
        return callApi(SHELLY_URL_STATUS, ShellyStatusLight.class);
    }

    public void setLightSetting(String parm, String value) throws ShellyApiException {
        request(SHELLY_URL_SETTINGS + "?" + parm + "=" + value);
    }

    public ShellySettingsLogin getLoginSettings() throws ShellyApiException {
        return callApi(SHELLY_URL_SETTINGS + "/login", ShellySettingsLogin.class);
    }

    public ShellySettingsLogin setLoginCredentials(String user, String password) throws ShellyApiException {
        return callApi(SHELLY_URL_SETTINGS + "/login?enabled=yes&username=" + urlEncode(user) + "&password="
                + urlEncode(password), ShellySettingsLogin.class);
    }

    public String getCoIoTDescription() throws ShellyApiException {
        try {
            return callApi("/cit/d", String.class);
        } catch (ShellyApiException e) {
            if (e.getApiResult().isNotFound()) {
                return ""; // only supported by FW 1.10+
            }
            throw e;
        }
    }

    public ShellySettingsLogin setCoIoTPeer(String peer) throws ShellyApiException {
        return callApi(SHELLY_URL_SETTINGS + "?coiot_enable=true&coiot_peer=" + peer, ShellySettingsLogin.class);
    }

    public String deviceReboot() throws ShellyApiException {
        return callApi(SHELLY_URL_RESTART, String.class);
    }

    public String factoryReset() throws ShellyApiException {
        return callApi(SHELLY_URL_SETTINGS + "?reset=true", String.class);
    }

    public ShellyOtaCheckResult checkForUpdate() throws ShellyApiException {
        return callApi("/ota/check", ShellyOtaCheckResult.class); // nw FW 1.10+: trigger update check
    }

    public String setWiFiRecovery(boolean enable) throws ShellyApiException {
        return callApi(SHELLY_URL_SETTINGS + "?wifirecovery_reboot_enabled=" + (enable ? "true" : "false"),
                String.class); // FW 1.10+: Enable auto-restart on WiFi problems
    }

    public String setApRoaming(boolean enable) throws ShellyApiException { // FW 1.10+: Enable AP Roadming
        return callApi(SHELLY_URL_SETTINGS + "?ap_roaming_enabled=" + (enable ? "true" : "false"), String.class);
    }

    public String resetStaCache() throws ShellyApiException { // FW 1.10+: Reset cached STA/AP list and to a rescan
        return callApi("/sta_cache_reset", String.class);
    }

    public ShellySettingsUpdate firmwareUpdate(String uri) throws ShellyApiException {
        return callApi("/ota?" + uri, ShellySettingsUpdate.class);
    }

    public String setCloud(boolean enabled) throws ShellyApiException {
        return callApi("/settings/cloud/?enabled=" + (enabled ? "1" : "0"), String.class);
    }

    /**
     * Change between White and Color Mode
     *
     * @param mode
     * @throws ShellyApiException
     */
    public void setLightMode(String mode) throws ShellyApiException {
        if (!mode.isEmpty() && !profile.mode.equals(mode)) {
            setLightSetting(SHELLY_API_MODE, mode);
            profile.mode = mode;
            profile.inColor = profile.isLight && profile.mode.equalsIgnoreCase(SHELLY_MODE_COLOR);
        }
    }

    /**
     * Set a single light parameter
     *
     * @param lightIndex Index of the light, usually 0 for Bulb and 0..3 for RGBW2.
     * @param parm Name of the parameter (see API spec)
     * @param value The value
     * @throws ShellyApiException
     */
    public void setLightParm(Integer lightIndex, String parm, String value) throws ShellyApiException {
        // Bulb, RGW2: /<color mode>/<light id>?parm?value
        // Dimmer: /light/<light id>?parm=value
        request(getControlUriPrefix(lightIndex) + "?" + parm + "=" + value);
    }

    public void setLightParms(Integer lightIndex, Map<String, String> parameters) throws ShellyApiException {
        String url = getControlUriPrefix(lightIndex) + "?";
        int i = 0;
        for (String key : parameters.keySet()) {
            if (i > 0) {
                url = url + "&";
            }
            url = url + key + "=" + parameters.get(key);
            i++;
        }
        request(url);
    }

    /**
     * Retrieve the IR Code list from the Shelly Sense device. The list could be customized by the user. It defines the
     * symbolic key code, which gets
     * map into a PRONTO code
     *
     * @return Map of key codes
     * @throws ShellyApiException
     */
    public Map<String, String> getIRCodeList() throws ShellyApiException {
        String result = request(SHELLY_URL_LIST_IR);
        // take pragmatic approach to make the returned JSon into named arrays for Gson parsing
        String keyList = substringAfter(result, "[");
        keyList = substringBeforeLast(keyList, "]");
        keyList = keyList.replaceAll(java.util.regex.Pattern.quote("\",\""), "\", \"name\": \"");
        keyList = keyList.replaceAll(java.util.regex.Pattern.quote("["), "{ \"id\":");
        keyList = keyList.replaceAll(java.util.regex.Pattern.quote("]"), "} ");
        String json = "{\"key_codes\" : [" + keyList + "] }";
        ShellySendKeyList codes = fromJson(gson, json, ShellySendKeyList.class);
        Map<String, String> list = new HashMap<>();
        for (ShellySenseKeyCode key : codes.keyCodes) {
            if (key != null) {
                list.put(key.id, key.name);
            }
        }
        return list;
    }

    /**
     * Sends a IR key code to the Shelly Sense.
     *
     * @param keyCode A keyCoud could be a symbolic name (as defined in the key map on the device) or a PRONTO Code in
     *            plain or hex64 format
     *
     * @throws ShellyApiException
     * @throws IllegalArgumentException
     */
    public void sendIRKey(String keyCode) throws ShellyApiException, IllegalArgumentException {
        String type = "";
        if (profile.irCodes.containsKey(keyCode)) {
            type = SHELLY_IR_CODET_STORED;
        } else if ((keyCode.length() > 4) && keyCode.contains(" ")) {
            type = SHELLY_IR_CODET_PRONTO;
        } else {
            type = SHELLY_IR_CODET_PRONTO_HEX;
        }
        String url = SHELLY_URL_SEND_IR + "?type=" + type;
        if (type.equals(SHELLY_IR_CODET_STORED)) {
            url = url + "&" + "id=" + keyCode;
        } else if (type.equals(SHELLY_IR_CODET_PRONTO)) {
            String code = Base64.getEncoder().encodeToString(keyCode.getBytes(StandardCharsets.UTF_8));
            url = url + "&" + SHELLY_IR_CODET_PRONTO + "=" + code;
        } else if (type.equals(SHELLY_IR_CODET_PRONTO_HEX)) {
            url = url + "&" + SHELLY_IR_CODET_PRONTO_HEX + "=" + keyCode;
        }
        request(url);
    }

    public void setSenseSetting(String setting, String value) throws ShellyApiException {
        request(SHELLY_URL_SETTINGS + "?" + setting + "=" + value);
    }

    /**
     * Set event callback URLs. Depending on the device different event types are supported. In fact all of them will be
     * redirected to the binding's servlet and act as a trigger to schedule a status update
     *
     * @param ShellyApiException
     * @throws ShellyApiException
     */
    public void setActionURLs() throws ShellyApiException {
        setRelayEvents();
        setDimmerEvents();
        setSensorEventUrls();
    }

    private void setRelayEvents() throws ShellyApiException {
        if (profile.settings.relays != null) {
            int num = profile.isRoller ? profile.numRollers : profile.numRelays;
            for (int i = 0; i < num; i++) {
                setEventUrls(i);
            }
        }
    }

    private void setDimmerEvents() throws ShellyApiException {
        if (profile.settings.dimmers != null) {
            for (int i = 0; i < profile.settings.dimmers.size(); i++) {
                setEventUrls(i);
            }
        } else if (profile.isLight) {
            setEventUrls(0);
        }
    }

    /**
     * Set sensor Action URLs
     *
     * @throws ShellyApiException
     */
    private void setSensorEventUrls() throws ShellyApiException, ShellyApiException {
        if (profile.isSensor) {
            logger.debug("{}: Set Sensor Reporting URL", thingName);
            setEventUrl(config.eventsSensorReport, SHELLY_EVENT_SENSORREPORT, SHELLY_EVENT_DARK, SHELLY_EVENT_TWILIGHT,
                    SHELLY_EVENT_FLOOD_DETECTED, SHELLY_EVENT_FLOOD_GONE, SHELLY_EVENT_OPEN, SHELLY_EVENT_CLOSE,
                    SHELLY_EVENT_VIBRATION, SHELLY_EVENT_ALARM_MILD, SHELLY_EVENT_ALARM_HEAVY, SHELLY_EVENT_ALARM_OFF,
                    SHELLY_EVENT_TEMP_OVER, SHELLY_EVENT_TEMP_UNDER);
        }
    }

    /**
     * Set/delete Relay/Roller/Dimmer Action URLs
     *
     * @param index Device Index (0-based)
     * @throws ShellyApiException
     */
    private void setEventUrls(Integer index) throws ShellyApiException {
        if (profile.isRoller) {
            setEventUrl(EVENT_TYPE_ROLLER, 0, config.eventsRoller, SHELLY_EVENT_ROLLER_OPEN, SHELLY_EVENT_ROLLER_CLOSE,
                    SHELLY_EVENT_ROLLER_STOP);
        } else if (profile.isDimmer) {
            // 2 set of URLs
            setEventUrl(EVENT_TYPE_LIGHT, index, config.eventsButton, SHELLY_EVENT_BTN1_ON, SHELLY_EVENT_BTN1_OFF,
                    SHELLY_EVENT_BTN2_ON, SHELLY_EVENT_BTN2_OFF);
            setEventUrl(EVENT_TYPE_LIGHT, index, config.eventsPush, SHELLY_EVENT_SHORTPUSH1, SHELLY_EVENT_LONGPUSH1,
                    SHELLY_EVENT_SHORTPUSH2, SHELLY_EVENT_LONGPUSH2);

            // Relay output
            setEventUrl(EVENT_TYPE_LIGHT, index, config.eventsSwitch, SHELLY_EVENT_OUT_ON, SHELLY_EVENT_OUT_OFF);
        } else if (profile.hasRelays) {
            // Standard relays: btn_xxx, out_xxx, short/longpush URLs
            setEventUrl(EVENT_TYPE_RELAY, index, config.eventsButton, SHELLY_EVENT_BTN_ON, SHELLY_EVENT_BTN_OFF);
            setEventUrl(EVENT_TYPE_RELAY, index, config.eventsPush, SHELLY_EVENT_SHORTPUSH, SHELLY_EVENT_LONGPUSH);
            setEventUrl(EVENT_TYPE_RELAY, index, config.eventsSwitch, SHELLY_EVENT_OUT_ON, SHELLY_EVENT_OUT_OFF);
        } else if (profile.isLight) {
            // Duo, Bulb
            setEventUrl(EVENT_TYPE_LIGHT, index, config.eventsSwitch, SHELLY_EVENT_OUT_ON, SHELLY_EVENT_OUT_OFF);
        }
    }

    private void setEventUrl(boolean enabled, String... eventTypes) throws ShellyApiException {
        if (config.localIp.isEmpty()) {
            throw new ShellyApiException(thingName + ": Local IP address was not detected, can't build Callback URL");
        }
        for (String eventType : eventTypes) {
            if (profile.containsEventUrl(eventType)) {
                // H&T adds the type=xx to report_url itself, so we need to ommit here
                String eclass = profile.isSensor ? EVENT_TYPE_SENSORDATA : eventType;
                String urlParm = eventType.contains("temp") || profile.isHT ? "" : "?type=" + eventType;
                String callBackUrl = "http://" + config.localIp + ":" + config.localPort + SHELLY_CALLBACK_URI + "/"
                        + profile.thingName + "/" + eclass + urlParm;
                String newUrl = enabled ? callBackUrl : SHELLY_NULL_URL;
                String testUrl = "\"" + mkEventUrl(eventType) + "\":\"" + newUrl + "\"";
                if (!enabled && !profile.settingsJson.contains(testUrl)) {
                    // Don't set URL to null when the current one doesn't point to this OH
                    // Don't interfere with a 3rd party App
                    continue;
                }
                if (!profile.settingsJson.contains(testUrl)) {
                    // Current Action URL is != new URL
                    logger.debug("{}: Set new url for event type {}: {}", thingName, eventType, newUrl);
                    request(SHELLY_URL_SETTINGS + "?" + mkEventUrl(eventType) + "=" + urlEncode(newUrl));
                }
            }
        }
    }

    private void setEventUrl(String deviceClass, Integer index, boolean enabled, String... eventTypes)
            throws ShellyApiException {
        for (String eventType : eventTypes) {
            if (profile.containsEventUrl(eventType)) {
                String callBackUrl = "http://" + config.localIp + ":" + config.localPort + SHELLY_CALLBACK_URI + "/"
                        + profile.thingName + "/" + deviceClass + "/" + index + "?type=" + eventType;
                String newUrl = enabled ? callBackUrl : SHELLY_NULL_URL;
                String test = "\"" + mkEventUrl(eventType) + "\":\"" + callBackUrl + "\"";
                if (!enabled && !profile.settingsJson.contains(test)) {
                    // Don't set URL to null when the current one doesn't point to this OH
                    // Don't interfere with a 3rd party App
                    continue;
                }
                test = "\"" + mkEventUrl(eventType) + "\":\"" + newUrl + "\"";
                if (!profile.settingsJson.contains(test)) {
                    // Current Action URL is != new URL
                    logger.debug("{}: Set URL for type {} to {}", thingName, eventType, newUrl);
                    request(SHELLY_URL_SETTINGS + "/" + deviceClass + "/" + index + "?" + mkEventUrl(eventType) + "="
                            + urlEncode(newUrl));
                }
            }
        }
    }

    private static String mkEventUrl(String eventType) {
        return eventType + SHELLY_EVENTURL_SUFFIX;
    }

    /**
     * Submit GET request and return response, check for invalid responses
     *
     * @param uri: URI (e.g. "/settings")
     */
    public <T> T callApi(String uri, Class<T> classOfT) throws ShellyApiException {
        String json = request(uri);
        return fromJson(gson, json, classOfT);
    }

    private String request(String uri) throws ShellyApiException {
        ShellyApiResult apiResult = new ShellyApiResult();
        int retries = 3;
        boolean timeout = false;
        while (retries > 0) {
            try {
                apiResult = innerRequest(HttpMethod.GET, uri);
                if (timeout) {
                    logger.debug("{}: API timeout #{}/{} recovered ({})", thingName, timeoutErrors, timeoutsRecovered,
                            apiResult.getUrl());
                    timeoutsRecovered++;
                }
                return apiResult.response; // successful
            } catch (ShellyApiException e) {
                if ((!e.isTimeout() && !apiResult.isHttpServerError()) || profile.hasBattery || (retries == 0)) {
                    // Sensor in sleep mode or API exception for non-battery device or retry counter expired
                    throw e; // non-timeout exception
                }

                timeout = true;
                retries--;
                timeoutErrors++; // count the retries
                logger.debug("{}: API Timeout, retry #{} ({})", thingName, timeoutErrors, e.toString());
            }
        }
        throw new ShellyApiException("API Timeout or inconsistent result"); // successful
    }

    private ShellyApiResult innerRequest(HttpMethod method, String uri) throws ShellyApiException {
        Request request = null;
        String url = "http://" + config.deviceIp + uri;
        ShellyApiResult apiResult = new ShellyApiResult(method.toString(), url);

        try {
            request = httpClient.newRequest(url).method(method.toString()).timeout(SHELLY_API_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);

            if (!config.userId.isEmpty()) {
                String value = config.userId + ":" + config.password;
                request.header(HTTP_HEADER_AUTH,
                        HTTP_AUTH_TYPE_BASIC + " " + Base64.getEncoder().encodeToString(value.getBytes()));
            }
            request.header(HttpHeader.ACCEPT, CONTENT_TYPE_JSON);
            logger.trace("{}: HTTP {} for {}", thingName, method, url);

            // Do request and get response
            ContentResponse contentResponse = request.send();
            apiResult = new ShellyApiResult(contentResponse);
            String response = contentResponse.getContentAsString().replace("\t", "").replace("\r\n", "").trim();
            logger.trace("{}: HTTP Response {}: {}", thingName, contentResponse.getStatus(), response);

            // validate response, API errors are reported as Json
            if (contentResponse.getStatus() != HttpStatus.OK_200) {
                throw new ShellyApiException(apiResult);
            }
            if (response.isEmpty() || !response.startsWith("{") && !response.startsWith("[") && !url.contains("/debug/")
                    && !url.contains("/sta_cache_reset")) {
                throw new ShellyApiException("Unexpected response: " + response);
            }
        } catch (ExecutionException | InterruptedException | TimeoutException | IllegalArgumentException e) {
            ShellyApiException ex = new ShellyApiException(apiResult, e);
            if (!ex.isTimeout()) { // will be handled by the caller
                logger.trace("{}: API call returned exception", thingName, ex);
            }
            throw ex;
        }
        return apiResult;
    }

    public String getControlUriPrefix(Integer id) {
        String uri = "";
        if (profile.isLight || profile.isDimmer) {
            if (profile.isDuo || profile.isDimmer) {
                // Duo + Dimmer
                uri = SHELLY_URL_CONTROL_LIGHT;
            } else {
                // Bulb + RGBW2
                uri = "/" + (profile.inColor ? SHELLY_MODE_COLOR : SHELLY_MODE_WHITE);
            }
        } else {
            // Roller, Relay
            uri = SHELLY_URL_CONTROL_RELEAY;
        }
        uri = uri + "/" + id;
        return uri;
    }

    public int getTimeoutErrors() {
        return timeoutErrors;
    }

    public int getTimeoutsRecovered() {
        return timeoutsRecovered;
    }
}
