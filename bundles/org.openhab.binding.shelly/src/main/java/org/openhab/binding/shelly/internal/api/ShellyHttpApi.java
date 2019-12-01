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
package org.openhab.binding.shelly.internal.api;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyUtils.*;
import static org.openhab.binding.shelly.internal.api.ShellyApiJson.*;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.HttpMethod;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyControlRoller;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySendKeyList;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySenseKeyCode;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsDevice;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsLight;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyShortLightStatus;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyStatusLight;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyStatusRelay;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link ShellyHttpApi} wraps the Shelly REST API and provides various low level function to access the device api (not
 * cloud api).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyHttpApi {
    private final Logger                   logger    = LoggerFactory.getLogger(ShellyHttpApi.class);
    private final ShellyThingConfiguration config;
    private final String                   thingName = "";
    private Gson                           gson      = new Gson();

    private @Nullable ShellyDeviceProfile  profile;

    public ShellyHttpApi(ShellyThingConfiguration config) {
        Validate.notNull(config, "Shelly Http Api: Config must not be null!");
        this.config = config;
    }

    @Nullable
    public ShellySettingsDevice getDevInfo() throws IOException {
        String json = request(SHELLY_URL_DEVINFO);
        logger.debug("Shelly device info : {}", json);
        return gson.fromJson(json, ShellySettingsDevice.class);
    }

    /**
     * Initialize the device profile
     *
     * @param thingType Type of DEVICE as returned from the thing properties (based on discovery)
     * @return Initialized ShellyDeviceProfile
     * @throws IOException
     */
    @SuppressWarnings("null")
    @Nullable
    public ShellyDeviceProfile getDeviceProfile(String thingType) throws IOException {
        String json = request(SHELLY_URL_SETTINGS);
        // Shelly Dimmer returns light[]. However, the structure doesn't match the lights[] of a Bulb/RGBW2
        if (json.contains("\"type\":\"SHDM-1\"") && json.contains("\"lights\":[")) {
            logger.debug("Detected a Shelly Dimmer: replace lights[] tag with dimmers[]");
            json = json.replaceFirst(java.util.regex.Pattern.quote("\"lights\":["), "\"dimmers\":[");
        }

        // Map settings to device profile for Light and Sense
        profile = ShellyDeviceProfile.initialize(thingType, json);
        Validate.notNull(profile);

        // 2nd level initialization
        profile.thingName = profile.hostname;
        if (profile.isLight && (profile.numMeters == 0)) {
            logger.debug("Get number of meters from light status");
            ShellyStatusLight status = getLightStatus();
            profile.numMeters = status.meters != null ? status.meters.size() : 0;
        }
        if (profile.isSense) {
            profile.irCodes = getIRCodeList();
        }

        return profile;
    }

    /**
     * Get generic device settings/status. Json returned from API will be mapped to a Gson object
     *
     * @return Device settings/status as ShellySettingsStatus object
     * @throws IOException
     */
    public ShellySettingsStatus getStatus() throws IOException {
        String json = request(SHELLY_URL_STATUS);

        ShellySettingsStatus status = gson.fromJson(json, ShellySettingsStatus.class);
        Validate.notNull(status);
        status.json = json;
        return status;
    }

    @Nullable
    public ShellyStatusRelay getRelayStatus(Integer relayIndex) throws IOException {
        String result = request(SHELLY_URL_STATUS_RELEAY + "/" + relayIndex.toString());
        return gson.fromJson(result, ShellyStatusRelay.class);
    }

    @SuppressWarnings("null")
    public void setRelayTurn(Integer relayIndex, String turnMode) throws IOException {
        Validate.notNull(profile);
        request((!profile.isDimmer ? SHELLY_URL_CONTROL_RELEAY : SHELLY_URL_CONTROL_LIGHT) + "/" + relayIndex.toString()
                + "?" + SHELLY_LIGHT_TURN + "=" + turnMode.toLowerCase());
    }

    public void setDimmerBrightness(Integer relayIndex, Integer brightness) throws IOException {
        request(SHELLY_URL_CONTROL_LIGHT + "/" + relayIndex.toString() + "?" + SHELLY_LIGHT_TURN + "=" + SHELLY_API_ON
                + "&brightness=" + brightness.toString());
    }

    @Nullable
    public ShellyControlRoller getRollerStatus(Integer rollerIndex) throws IOException {
        String result = request(SHELLY_URL_CONTROL_ROLLER + "/" + rollerIndex.toString() + "/pos");
        return gson.fromJson(result, ShellyControlRoller.class);
    }

    public void setRollerTurn(Integer relayIndex, String turnMode) throws IOException {
        request(SHELLY_URL_CONTROL_ROLLER + "/" + relayIndex.toString() + "?go=" + turnMode);
    }

    public void setRollerPos(Integer relayIndex, Integer position) throws IOException {
        request(SHELLY_URL_CONTROL_ROLLER + "/" + relayIndex.toString() + "?go=to_pos&roller_pos="
                + position.toString());
    }

    public void setRollerTimer(Integer relayIndex, Integer timer) throws IOException {
        request(SHELLY_URL_CONTROL_ROLLER + "/" + relayIndex.toString() + "?timer=" + timer.toString());
    }

    @Nullable
    public ShellyShortLightStatus getLightStatus(Integer index) throws IOException {
        String result = request(SHELLY_URL_STATUS_LIGHT + "/" + index.toString());
        return gson.fromJson(result, ShellyShortLightStatus.class);
    }

    @SuppressWarnings("null")
    public ShellyStatusSensor getSensorStatus() throws IOException {
        Validate.notNull(profile);
        ShellyStatusSensor status = gson.fromJson(request(SHELLY_URL_STATUS), ShellyStatusSensor.class);
        if (profile.isSense) {
            // complete reported data
            status.tmp.tC = status.tmp.units.equals(SHELLY_TEMP_CELSIUS) ? status.tmp.value : 0;
            status.tmp.tF = status.tmp.units.equals(SHELLY_TEMP_FAHRENHEIT) ? status.tmp.value : 0;
        }
        return status;
    }

    @SuppressWarnings("null")
    public void setTimer(Integer index, String timerName, Double value) throws IOException {
        Validate.notNull(profile);
        String type = SHELLY_CLASS_RELAY;
        if (profile.isRoller) {
            type = SHELLY_CLASS_ROLLER;
        } else if (profile.isLight) {
            type = SHELLY_CLASS_LIGHT;
        }
        String uri = SHELLY_URL_SETTINGS + "/" + type + "/" + index + "?" + timerName + "="
                + ((Integer) value.intValue()).toString();
        request(uri);
    }

    public void setLedStatus(String ledName, Boolean value) throws IOException {
        request(SHELLY_URL_SETTINGS + "?" + ledName + "=" + (value ? SHELLY_API_TRUE : SHELLY_API_FALSE));
    }

    @Nullable
    public ShellySettingsLight getLightSettings() throws IOException {
        String result = request(SHELLY_URL_SETTINGS_LIGHT);
        return gson.fromJson(result, ShellySettingsLight.class);
    }

    @Nullable
    public ShellyStatusLight getLightStatus() throws IOException {
        String result = request(SHELLY_URL_STATUS);
        return gson.fromJson(result, ShellyStatusLight.class);
    }

    public void setLightSetting(String parm, String value) throws IOException {
        request(SHELLY_URL_SETTINGS + "?" + parm + "=" + value);
    }

    /**
     * Change between White and Color Mode
     *
     * @param mode
     * @throws IOException
     */
    @SuppressWarnings("null")
    public void setLightMode(String mode) throws IOException {
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
     * @throws IOException
     */
    @SuppressWarnings("null")
    public void setLightParm(Integer lightIndex, String parm, String value) throws IOException {
        // Bulb, RGW2: /<color mode>/<light id>?parm?value
        // Dimmer: /light/<light id>?parm=value
        Validate.notNull(profile);
        request((!profile.isDimmer ? "/" + profile.mode : SHELLY_URL_CONTROL_LIGHT) + "/" + lightIndex.toString() + "?"
                + parm + "=" + value);
    }

    public void setLightParms(Integer lightIndex, Map<String, String> parameters) throws IOException {
        Validate.notNull(profile);
        @SuppressWarnings("null")
        String url = (!profile.isDimmer ? "/" + profile.mode : SHELLY_URL_CONTROL_LIGHT) + "/" + lightIndex.toString()
                + "?";
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
     * @throws IOException
     */
    public Map<String, String> getIRCodeList() throws IOException {
        String result = request(SHELLY_URL_LIST_IR);

        /*
         * A string like this is returned with all key codes defined in the device (customizable with the App):
         * String result =
         * "[[\"1_231_pwr\",\"tv(231) - Power\"],[\"1_231_chdwn\",\"tv(231) - Channel Down\"],[\"1_231_chup\",
         * \"tv(231) - Channel Up\"], [\"1_231_voldwn\",\"tv(231) - Volume Down\"],[\"1_231_volup\",\"tv(231) - Volume Up\"],[\"1_231_mute\"
         * ,
         * \"tv(231) - Mute\"],[\"1_231_menu\",\"tv(231) - Menu\"],[\"1_231_inp\",\"tv(231) - Input\"],[\"1_231_info\",\"tv(231) -  Info\"
         * ],
         * [\"1_231_left\",\"tv(231) - Left\"],[\"1_231_up\",\"tv(231) - Up\"],[\"1_231_right\",\"tv(231) - Right\"],[\"1_231_ok\"
         * ,\
         * "tv(231) - OK\"],[\"1_231_down\",\"tv(231) - Down\"],[\"1_231_back\",\"tv(231) - Back\"],[\"6_546_pwr\",\"receiver(546) - Power\"
         * ],
         * [\"6_546_voldwn\",\"receiver(546) - Volume Down\"],[\"6_546_volup\",\"receiver(546) - Volume Up\"],[\"6_546_mute\"
         * ,
         * \"receiver(546) - Mute\"],[\"6_546_menu\",\"receiver(546) - Menu\"],[\"6_546_info\",\"receiver(546) - Info\"],[\"6_546_left\"
         * ,
         * \"receiver(546) - Left\"],[\"6_546_up\",\"receiver(546) - Up\"],[\"6_546_right\",\"receiver(546) - Right\"],[\"6_546_ok\"
         * ,
         * \"receiver(546) - OK\"],[\"6_546_down\",\"receiver(546) - Down\"],[\"6_546_back\",\"receiver(546) - Back\"]]"
         * ;
         */

        // take pragmatic approach to make the returned JSon into named arrays, otherwise we need to implement a
        // dedicated GSonParser
        String keyList = StringUtils.substringAfter(result, "[");
        keyList = StringUtils.substringBeforeLast(keyList, "]");
        keyList = keyList.replaceAll(java.util.regex.Pattern.quote("\",\""), "\", \"name\": \"");
        keyList = keyList.replaceAll(java.util.regex.Pattern.quote("["), "{ \"id\":");
        keyList = keyList.replaceAll(java.util.regex.Pattern.quote("]"), "} ");
        String json = "{\"key_codes\" : [" + keyList + "] }";

        ShellySendKeyList codes = gson.fromJson(json, ShellySendKeyList.class);
        Validate.notNull(codes);
        Map<String, String> list = new HashMap<String, String>();
        for (ShellySenseKeyCode key : codes.keyCodes) {
            list.put(key.id, key.name);
        }
        return list;
    }

    /**
     * Sends a IR key code to the Shelly Sense.
     *
     * @param keyCode A keyCoud could be a symbolic name (as defined in the key map on the device) or a PRONTO Code in
     *            plain or hex64 format
     *
     * @throws IOException
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("null")
    public void sendIRKey(String keyCode) throws IOException, IllegalArgumentException {
        Validate.notNull(profile);
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
            String code = Base64.getEncoder().encodeToString(keyCode.getBytes());
            Validate.notNull(code, "Unable to BASE64 encode the pronto code: " + keyCode);
            url = url + "&" + SHELLY_IR_CODET_PRONTO + "=" + code;
        } else if (type.equals(SHELLY_IR_CODET_PRONTO_HEX)) {
            url = url + "&" + SHELLY_IR_CODET_PRONTO_HEX + "=" + keyCode;
        }
        request(url);
    }

    public void setSenseSetting(String setting, String value) throws IOException {
        request(SHELLY_URL_SETTINGS + "?" + setting + "=" + value);
    }

    /**
     * Set event callback URLs. Depending on the device different event types are supported. In fact all of them will be
     * redirected to the binding's
     * servlet and act as a trigger to schedule a status update
     *
     * @param deviceName
     * @throws IOException
     */
    public void setEventURLs() throws IOException {
        setRelayEvents();
        setDimmerEvents();
        setSensorEventUrls();
    }

    @SuppressWarnings("null")
    private void setRelayEvents() throws IOException {
        Validate.notNull(profile);
        if (profile.settings.relays != null) {
            int num = profile.isRoller ? profile.numRollers : profile.numRelays;
            for (int i = 0; i < num; i++) {
                setEventUrls(i);
            }
        }
    }

    @SuppressWarnings("null")
    private void setDimmerEvents() throws IOException {
        Validate.notNull(profile);
        if (profile.settings.dimmers != null) {
            for (int i = 0; i < profile.settings.dimmers.size(); i++) {
                setEventUrls(i);
            }
        }
    }

    /**
     * Set event URL for HT (report_url)
     *
     * @param deviceName
     * @throws IOException
     */
    @SuppressWarnings("null")
    private void setSensorEventUrls() throws IOException {
        Validate.notNull(profile);
        if (profile.supportsSensorUrls && config.eventsSensorReport) {
            logger.debug("Check/set Sensor Reporting URL");
            String eventUrl = "http://" + config.localIp + ":" + config.httpPort.toString() + SHELLY_CALLBACK_URI + "/"
                    + profile.thingName + "/" + EVENT_TYPE_SENSORDATA;
            request(SHELLY_URL_SETTINGS + "?" + SHELLY_API_EVENTURL_REPORT + "=" + urlEncode(eventUrl));
        }
    }

    @SuppressWarnings("null")
    private void setEventUrls(Integer index) throws IOException {
        Validate.notNull(profile);
        String lip = config.localIp;
        String localPort = config.httpPort.toString();
        String deviceName = profile.thingName;
        if (profile.isRoller) {
            if (profile.supportsRollerUrls) {
                logger.debug("Set Roller event urls");
                request(buildSetEventUrl(lip, localPort, deviceName, index, EVENT_TYPE_ROLLER,
                        SHELLY_API_EVENTURL_ROLLER_OPEN));
                request(buildSetEventUrl(lip, localPort, deviceName, index, EVENT_TYPE_ROLLER,
                        SHELLY_API_EVENTURL_ROLLER_CLOSE));
                request(buildSetEventUrl(lip, localPort, deviceName, index, EVENT_TYPE_ROLLER,
                        SHELLY_API_EVENTURL_ROLLER_STOP));
            }
        } else {
            if (profile.supportsButtonUrls && config.eventsButton) {
                if (profile.settingsJson.contains(SHELLY_API_EVENTURL_BTN1_ON)) {
                    // 2 set of URLs, e.g. Dimmer
                    logger.debug("Set Dimmer event urls");

                    request(buildSetEventUrl(lip, localPort, deviceName, index, EVENT_TYPE_LIGHT,
                            SHELLY_API_EVENTURL_BTN1_ON));
                    request(buildSetEventUrl(lip, localPort, deviceName, index, EVENT_TYPE_LIGHT,
                            SHELLY_API_EVENTURL_BTN1_OFF));
                    request(buildSetEventUrl(lip, localPort, deviceName, index, EVENT_TYPE_LIGHT,
                            SHELLY_API_EVENTURL_BTN2_ON));
                    request(buildSetEventUrl(lip, localPort, deviceName, index, EVENT_TYPE_LIGHT,
                            SHELLY_API_EVENTURL_BTN2_OFF));
                } else {
                    // Standard relays: btn_xxx URLs
                    logger.debug("Set Relay event urls");
                    request(buildSetEventUrl(lip, localPort, deviceName, index, EVENT_TYPE_RELAY,
                            SHELLY_API_EVENTURL_BTN_ON));
                    request(buildSetEventUrl(lip, localPort, deviceName, index, EVENT_TYPE_RELAY,
                            SHELLY_API_EVENTURL_BTN_OFF));
                }
            }
            if (profile.supportsOutUrls && config.eventsSwitch) {
                request(buildSetEventUrl(lip, localPort, deviceName, index,
                        profile.isDimmer ? EVENT_TYPE_LIGHT : EVENT_TYPE_RELAY, SHELLY_API_EVENTURL_OUT_ON));
                request(buildSetEventUrl(lip, localPort, deviceName, index,
                        profile.isDimmer ? EVENT_TYPE_LIGHT : EVENT_TYPE_RELAY, SHELLY_API_EVENTURL_OUT_OFF));
            }
            if (profile.supportsPushUrls && config.eventsSwitch) {
                request(buildSetEventUrl(lip, localPort, deviceName, index,
                        profile.isDimmer ? EVENT_TYPE_LIGHT : EVENT_TYPE_RELAY, SHELLY_API_EVENTURL_SHORT_PUSH));
                request(buildSetEventUrl(lip, localPort, deviceName, index,
                        profile.isDimmer ? EVENT_TYPE_LIGHT : EVENT_TYPE_RELAY, SHELLY_API_EVENTURL_LONG_PUSH));
            }
        }
    }

    /**
     * Submit GET request and return response, check for invalid responses
     *
     * @param uri: URI (e.g. "/settings")
     */
    private String request(String uri) throws IOException {
        String httpResponse = "ERROR";
        String url = "http://" + config.deviceIp + uri;
        // boolean acquired = false;
        try {
            logger.trace("HTTP GET for {}: {}", thingName, url);

            Properties headers = new Properties();
            if (!config.userId.isEmpty()) {
                String value = config.userId + ":" + config.password;
                headers.put(HTTP_HEADER_AUTH,
                        HTTP_AUTH_TYPE_BASIC + " " + Base64.getEncoder().encodeToString(value.getBytes()));
            }

            httpResponse = HttpUtil.executeUrl(HttpMethod.GET, url, headers, null, "", SHELLY_API_TIMEOUT_MS);
            Validate.notNull(httpResponse, "httpResponse must not be null");
            // all api responses are returning the result in Json format. If we are getting
            // something else it must
            // be an error message, e.g. http result code
            if (httpResponse.contains(APIERR_HTTP_401_UNAUTHORIZED)) {
                throw new IOException(
                        APIERR_HTTP_401_UNAUTHORIZED + ", set/correct userid and password in the thing/binding config");
            }
            if (!httpResponse.startsWith("{") && !httpResponse.startsWith("[")) {
                throw new IOException("Unexpected http response: " + httpResponse);
            }

            logger.trace("HTTP response from {}: {}", thingName, httpResponse);
            return httpResponse;
        } catch (IOException e) {
            if (e.getMessage().contains("Timeout")) {
                throw new IOException("Shelly API call failed: Timeout (" + SHELLY_API_TIMEOUT_MS + " ms)");

            } else {
                throw new IOException("Shelly API call failed: " + e.getMessage() + ", url=" + url);
            }
        }
    }

}
