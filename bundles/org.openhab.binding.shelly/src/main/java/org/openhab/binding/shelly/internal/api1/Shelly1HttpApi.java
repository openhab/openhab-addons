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
package org.openhab.binding.shelly.internal.api1;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api.ShellyHttpClient;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyOtaCheckResult;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyRollerStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySendKeyList;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySenseKeyCode;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDevice;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsLogin;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsUpdate;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyShortLightStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyThermnostat;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * {@link Shelly1HttpApi} wraps the Shelly REST API and provides various low level function to access the device api
 * (not
 * cloud api).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly1HttpApi extends ShellyHttpClient implements ShellyApiInterface {
    private final Logger logger = LoggerFactory.getLogger(Shelly1HttpApi.class);
    private final ShellyDeviceProfile profile;

    public Shelly1HttpApi(String thingName, ShellyThingInterface thing) {
        super(thingName, thing);
        profile = thing.getProfile();
    }

    /**
     * Simple initialization - called by discovery handler
     *
     * @param thingName Symbolic thing name
     * @param config Thing Configuration
     * @param httpClient HTTP Client to be passed to ShellyHttpClient
     */
    public Shelly1HttpApi(String thingName, ShellyThingConfiguration config, HttpClient httpClient) {
        super(thingName, config, httpClient);
        this.profile = new ShellyDeviceProfile();
    }

    @Override
    public void initialize() throws ShellyApiException {
        profile.device = getDeviceInfo();
    }

    @Override
    public ShellySettingsDevice getDeviceInfo() throws ShellyApiException {
        ShellySettingsDevice info = callApi(SHELLY_URL_DEVINFO, ShellySettingsDevice.class);
        info.gen = 1;
        basicAuth = getBool(info.auth);

        if (getString(info.mode).isEmpty()) { // older Gen1 Firmware
            if (getInteger(info.numRollers) > 0) {
                info.mode = SHELLY_CLASS_ROLLER;
            } else if (getInteger(info.numOutputs) > 0) {
                info.mode = SHELLY_CLASS_RELAY;
            } else {
                info.mode = "";
            }
        }
        return info;
    }

    @Override
    public String setDebug(boolean enabled) throws ShellyApiException {
        return callApi(SHELLY_URL_SETTINGS + "?debug_enable=" + Boolean.valueOf(enabled), String.class);
    }

    @Override
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
    @Override
    public ShellyDeviceProfile getDeviceProfile(String thingType, @Nullable ShellySettingsDevice device)
            throws ShellyApiException {
        if (device != null) {
            profile.device = device;
        }
        if (profile.device.type == null) {
            profile.device = getDeviceInfo();
        }
        String json = httpRequest(SHELLY_URL_SETTINGS);
        if (json.contains("\"type\":\"SHDM-")) {
            logger.trace("{}: Detected a Shelly Dimmer: fix Json (replace lights[] tag with dimmers[]", thingName);
            json = fixDimmerJson(json);
        }

        // Map settings to device profile for Light and Sense
        profile.initialize(thingType, json, profile.device);

        // 2nd level initialization
        profile.thingName = profile.device.hostname;
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

    @Override
    public boolean isInitialized() {
        return profile.initialized;
    }

    /**
     * Get generic device settings/status. Json returned from API will be mapped to a Gson object
     *
     * @return Device settings/status as ShellySettingsStatus object
     * @throws ShellyApiException
     */
    @Override
    public ShellySettingsStatus getStatus() throws ShellyApiException {
        String json = "";
        try {
            json = httpRequest(SHELLY_URL_STATUS);

            // Dimmer2 returns invalid json type for loaderror :-(
            json = json.replace("\"loaderror\":0,", "\"loaderror\":false,")
                    .replace("\"loaderror\":1,", "\"loaderror\":true,")
                    .replace("\"tmp\":{\"value\": \"null\",", "\"tmp\":{\"value\": null,");
            ShellySettingsStatus status = fromJson(gson, json, ShellySettingsStatus.class);
            status.json = json;
            return status;
        } catch (JsonSyntaxException e) {
            throw new ShellyApiException("Unable to parse JSON: " + json, e);
        }
    }

    @Override
    public ShellyStatusRelay getRelayStatus(int relayIndex) throws ShellyApiException {
        return callApi(SHELLY_URL_STATUS_RELEAY + "/" + relayIndex, ShellyStatusRelay.class);
    }

    @Override
    public void setRelayTurn(int id, String turnMode) throws ShellyApiException {
        callApi(getControlUriPrefix(id) + "?" + SHELLY_LIGHT_TURN + "=" + turnMode.toLowerCase(), String.class);
    }

    @Override
    public void resetMeterTotal(int id) throws ShellyApiException {
        callApi(SHELLY_URL_STATUS_EMETER + "/" + id + "?reset_totals=true", ShellyStatusRelay.class);
    }

    @Override
    public ShellyShortLightStatus setLightTurn(int id, String turnMode) throws ShellyApiException {
        return callApi(getControlUriPrefix(id) + "?" + SHELLY_LIGHT_TURN + "=" + turnMode.toLowerCase(),
                ShellyShortLightStatus.class);
    }

    @Override
    public void setBrightness(int id, int brightness, boolean autoOn) throws ShellyApiException {
        String turn = autoOn ? SHELLY_LIGHT_TURN + "=" + SHELLY_API_ON + "&" : "";
        httpRequest(getControlUriPrefix(id) + "?" + turn + "brightness=" + brightness);
    }

    @Override
    public ShellyRollerStatus getRollerStatus(int rollerIndex) throws ShellyApiException {
        String uri = SHELLY_URL_CONTROL_ROLLER + "/" + rollerIndex + "/pos";
        return callApi(uri, ShellyRollerStatus.class);
    }

    @Override
    public void setRollerTurn(int relayIndex, String turnMode) throws ShellyApiException {
        httpRequest(SHELLY_URL_CONTROL_ROLLER + "/" + relayIndex + "?go=" + turnMode);
    }

    @Override
    public void setRollerPos(int relayIndex, int position) throws ShellyApiException {
        httpRequest(SHELLY_URL_CONTROL_ROLLER + "/" + relayIndex + "?go=to_pos&roller_pos=" + position);
    }

    @Override
    public ShellyShortLightStatus getLightStatus(int index) throws ShellyApiException {
        return callApi(getControlUriPrefix(index), ShellyShortLightStatus.class);
    }

    @Override
    public ShellyStatusSensor getSensorStatus() throws ShellyApiException {
        ShellyStatusSensor status = callApi(SHELLY_URL_STATUS, ShellyStatusSensor.class);
        if (profile.isSense) {
            // complete reported data, map C to F or vice versa: C=(F - 32) * 0.5556;
            status.tmp.tC = status.tmp.units.equals(SHELLY_TEMP_CELSIUS) ? status.tmp.value
                    : ImperialUnits.FAHRENHEIT.getConverterTo(SIUnits.CELSIUS).convert(getDouble(status.tmp.value))
                            .doubleValue();
            double f = (double) SIUnits.CELSIUS.getConverterTo(ImperialUnits.FAHRENHEIT)
                    .convert(getDouble(status.tmp.value));
            status.tmp.tF = status.tmp.units.equals(SHELLY_TEMP_FAHRENHEIT) ? status.tmp.value : f;
        }
        if ((status.charger == null) && (profile.settings.externalPower != null)) {
            // SHelly H&T uses external_power, Sense uses charger
            status.charger = profile.settings.externalPower != 0;
        }
        if (status.tmp != null && status.tmp.tC == null && status.tmp.value != null) { // Motion is is missing tC and tF
            status.tmp.tC = getString(status.tmp.units).toUpperCase().equals(SHELLY_TEMP_FAHRENHEIT)
                    ? ImperialUnits.FAHRENHEIT.getConverterTo(SIUnits.CELSIUS).convert(status.tmp.value).doubleValue()
                    : status.tmp.value;
        }
        return status;
    }

    @Override
    public void setAutoTimer(int index, String timerName, double value) throws ShellyApiException {
        String type = SHELLY_CLASS_RELAY;
        if (profile.isRoller) {
            type = SHELLY_CLASS_ROLLER;
        } else if (profile.isLight) {
            type = SHELLY_CLASS_LIGHT;
        }
        String uri = SHELLY_URL_SETTINGS + "/" + type + "/" + index + "?" + timerName + "=" + value;
        httpRequest(uri);
    }

    @Override
    public void setSleepTime(int value) throws ShellyApiException {
        httpRequest(SHELLY_URL_SETTINGS + "?sleep_time=" + value);
    }

    @Override
    public void setValveTemperature(int valveId, double value) throws ShellyApiException {
        httpRequest("/thermostat/" + valveId + "?target_t_enabled=1&target_t=" + value);
    }

    @Override
    public void setValveMode(int valveId, boolean auto) throws ShellyApiException {
        String uri = "/settings/thermostat/" + valveId + "?target_t_enabled=" + (auto ? "1" : "0");
        if (auto && profile.settings.thermostats != null) {
            uri = uri + "&target_t=" + getDouble(profile.settings.thermostats.get(0).targetTemp.value);
        }
        httpRequest(uri); // percentage to open the valve
    }

    @Override
    public void setValveProfile(int valveId, int value) throws ShellyApiException {
        String uri = "/settings/thermostat/" + valveId + "?";
        httpRequest(uri + (value == 0 ? "schedule=0" : "schedule=1&schedule_profile=" + value));
    }

    @Override
    public void setValvePosition(int valveId, double value) throws ShellyApiException {
        httpRequest("/thermostat/" + valveId + "?pos=" + value); // percentage to open the valve
    }

    @Override
    public void setValveBoostTime(int valveId, int value) throws ShellyApiException {
        httpRequest("/settings/thermostat/" + valveId + "?boost_minutes=" + value);
    }

    @Override
    public void startValveBoost(int valveId, int value) throws ShellyApiException {
        if (profile.settings.thermostats != null) {
            ShellyThermnostat t = profile.settings.thermostats.get(0);
            int minutes = value != -1 ? value : getInteger(t.boostMinutes);
            httpRequest("/thermostat/0?boost_minutes=" + minutes);
        }
    }

    @Override
    public void setLedStatus(String ledName, boolean value) throws ShellyApiException {
        httpRequest(SHELLY_URL_SETTINGS + "?" + ledName + "=" + (value ? SHELLY_API_TRUE : SHELLY_API_FALSE));
    }

    public ShellySettingsLight getLightSettings() throws ShellyApiException {
        return callApi(SHELLY_URL_SETTINGS_LIGHT, ShellySettingsLight.class);
    }

    @Override
    public ShellyStatusLight getLightStatus() throws ShellyApiException {
        return callApi(SHELLY_URL_STATUS, ShellyStatusLight.class);
    }

    public void setLightSetting(String parm, String value) throws ShellyApiException {
        httpRequest(SHELLY_URL_SETTINGS + "?" + parm + "=" + value);
    }

    @Override
    public ShellySettingsLogin getLoginSettings() throws ShellyApiException {
        return callApi(SHELLY_URL_SETTINGS + "/login", ShellySettingsLogin.class);
    }

    @Override
    public ShellySettingsLogin setLoginCredentials(String user, String password) throws ShellyApiException {
        return callApi(SHELLY_URL_SETTINGS + "/login?enabled=yes&username=" + urlEncode(user) + "&password="
                + urlEncode(password), ShellySettingsLogin.class);
    }

    @Override
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

    @Override
    public ShellySettingsLogin setCoIoTPeer(String peer) throws ShellyApiException {
        return callApi(SHELLY_URL_SETTINGS + "?coiot_enable=true&coiot_peer=" + peer, ShellySettingsLogin.class);
    }

    @Override
    public String deviceReboot() throws ShellyApiException {
        return callApi(SHELLY_URL_RESTART, String.class);
    }

    @Override
    public String factoryReset() throws ShellyApiException {
        return callApi(SHELLY_URL_SETTINGS + "?reset=true", String.class);
    }

    @Override
    public ShellyOtaCheckResult checkForUpdate() throws ShellyApiException {
        return callApi("/ota/check", ShellyOtaCheckResult.class); // nw FW 1.10+: trigger update check
    }

    @Override
    public String setWiFiRecovery(boolean enable) throws ShellyApiException {
        return callApi(SHELLY_URL_SETTINGS + "?wifirecovery_reboot_enabled=" + (enable ? "true" : "false"),
                String.class); // FW 1.10+: Enable auto-restart on WiFi problems
    }

    @Override
    public String setApRoaming(boolean enable) throws ShellyApiException { // FW 1.10+: Enable AP Roadming
        return callApi(SHELLY_URL_SETTINGS + "?ap_roaming_enabled=" + (enable ? "true" : "false"), String.class);
    }

    @Override
    public boolean setWiFiRangeExtender(boolean enable) throws ShellyApiException {
        return false;
    }

    @Override
    public boolean setEthernet(boolean enable) throws ShellyApiException {
        return false;
    }

    @Override
    public boolean setBluetooth(boolean enable) throws ShellyApiException {
        return false;
    }

    @Override
    public String resetStaCache() throws ShellyApiException { // FW 1.10+: Reset cached STA/AP list and to a rescan
        return callApi("/sta_cache_reset", String.class);
    }

    @Override
    public ShellySettingsUpdate firmwareUpdate(String uri) throws ShellyApiException {
        return callApi("/ota?" + uri, ShellySettingsUpdate.class);
    }

    @Override
    public String setCloud(boolean enabled) throws ShellyApiException {
        return callApi("/settings/cloud/?enabled=" + (enabled ? "1" : "0"), String.class);
    }

    /**
     * Change between White and Color Mode
     *
     * @param mode
     * @throws ShellyApiException
     */
    @Override
    public void setLightMode(String mode) throws ShellyApiException {
        if (!mode.isEmpty() && !profile.device.mode.equals(mode)) {
            setLightSetting(SHELLY_API_MODE, mode);
            profile.device.mode = mode;
            profile.inColor = profile.isLight && mode.equalsIgnoreCase(SHELLY_MODE_COLOR);
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
    @Override
    public void setLightParm(int lightIndex, String parm, String value) throws ShellyApiException {
        // Bulb, RGW2: /<color mode>/<light id>?parm?value
        // Dimmer: /light/<light id>?parm=value
        httpRequest(getControlUriPrefix(lightIndex) + "?" + parm + "=" + value);
    }

    @Override
    public void setLightParms(int lightIndex, Map<String, String> parameters) throws ShellyApiException {
        String url = getControlUriPrefix(lightIndex) + "?";
        int i = 0;
        for (String key : parameters.keySet()) {
            if (i > 0) {
                url = url + "&";
            }
            url = url + key + "=" + parameters.get(key);
            i++;
        }
        httpRequest(url);
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
        String result = httpRequest(SHELLY_URL_LIST_IR);
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
     * Sends an IR key code to the Shelly Sense.
     *
     * @param keyCode A keyCoud could be a symbolic name (as defined in the key map on the device) or a PRONTO Code in
     *            plain or hex64 format
     *
     * @throws ShellyApiException
     * @throws IllegalArgumentException
     */
    @Override
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
        httpRequest(url);
    }

    public void setSenseSetting(String setting, String value) throws ShellyApiException {
        httpRequest(SHELLY_URL_SETTINGS + "?" + setting + "=" + value);
    }

    /**
     * Set event callback URLs. Depending on the device different event types are supported. In fact all of them will be
     * redirected to the binding's servlet and act as a trigger to schedule a status update
     *
     * @throws ShellyApiException
     */
    @Override
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
            int sz = profile.settings.dimmers.size();
            for (int i = 0; i < sz; i++) {
                setEventUrls(i);
            }
        } else if (profile.isLight) {
            setEventUrls(0);
        }
    }

    @Override
    public void muteSmokeAlarm(int id) throws ShellyApiException {
        throw new ShellyApiException("Request not supported");
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
                String callBackUrl = "http://" + config.localIp + ":" + config.localPort + SHELLY1_CALLBACK_URI + "/"
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
                    httpRequest(SHELLY_URL_SETTINGS + "?" + mkEventUrl(eventType) + "=" + urlEncode(newUrl));
                }
            }
        }
    }

    private void setEventUrl(String deviceClass, Integer index, boolean enabled, String... eventTypes)
            throws ShellyApiException {
        for (String eventType : eventTypes) {
            if (profile.containsEventUrl(eventType)) {
                String callBackUrl = "http://" + config.localIp + ":" + config.localPort + SHELLY1_CALLBACK_URI + "/"
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
                    httpRequest(SHELLY_URL_SETTINGS + "/" + deviceClass + "/" + index + "?" + mkEventUrl(eventType)
                            + "=" + urlEncode(newUrl));
                }
            }
        }
    }

    private static String mkEventUrl(String eventType) {
        return eventType + SHELLY_EVENTURL_SUFFIX;
    }

    @Override
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

    @Override
    public int getTimeoutErrors() {
        return timeoutErrors;
    }

    @Override
    public int getTimeoutsRecovered() {
        return timeoutsRecovered;
    }

    @Override
    public void close() {
    }

    @Override
    public void startScan() {
    }
}
