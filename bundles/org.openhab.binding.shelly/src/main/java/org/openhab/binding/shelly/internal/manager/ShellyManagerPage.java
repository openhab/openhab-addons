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
package org.openhab.binding.shelly.internal.manager;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.discovery.ShellyThingCreator.*;
import static org.openhab.binding.shelly.internal.manager.ShellyManagerConstants.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;
import static org.openhab.core.thing.Thing.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.shelly.internal.ShellyHandlerFactory;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiResult;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api.ShellyHttpClient;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyDeviceStats;
import org.openhab.binding.shelly.internal.handler.ShellyManagerInterface;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * {@link ShellyManagerOtaPage} implements the Shelly Manager's page template
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyManagerPage {
    private final Logger logger = LoggerFactory.getLogger(ShellyManagerPage.class);
    protected final ShellyTranslationProvider resources;

    private final ShellyHandlerFactory handlerFactory;
    protected final HttpClient httpClient;
    protected final ConfigurationAdmin configurationAdmin;
    protected final ShellyBindingConfiguration bindingConfig = new ShellyBindingConfiguration();
    protected final String localIp;
    protected final int localPort;

    protected final Map<String, String> htmlTemplates = new HashMap<>();
    protected final Gson gson = new Gson();

    protected final ShellyManagerCache<String, FwRepoEntry> firmwareRepo = new ShellyManagerCache<>(15 * 60 * 1000);
    protected final ShellyManagerCache<String, FwArchList> firmwareArch = new ShellyManagerCache<>(15 * 60 * 1000);

    public static class ShellyMgrResponse {
        public @Nullable Object data = "";
        public String mimeType = "";
        public String redirectUrl = "";
        public int code;
        public Map<String, String> headers = new HashMap<>();

        public ShellyMgrResponse() {
            init("", HttpStatus.OK_200, "text/html", null);
        }

        public ShellyMgrResponse(Object data, int code) {
            init(data, code, "text/html", null);
        }

        public ShellyMgrResponse(Object data, int code, String mimeType) {
            init(data, code, mimeType, null);
        }

        public ShellyMgrResponse(Object data, int code, String mimeType, Map<String, String> headers) {
            init(data, code, mimeType, headers);
        }

        private void init(Object message, int code, String mimeType, @Nullable Map<String, String> headers) {
            this.data = message;
            this.code = code;
            this.mimeType = mimeType;
            this.headers = headers != null ? headers : new TreeMap<>();
        }

        public void setRedirect(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }
    }

    public static class FwArchEntry {
        // {"version":"v1.5.10","file":"SHSW-1.zip"}
        public @Nullable String version;
        public @Nullable String file;
    }

    public static class FwArchList {
        public @Nullable ArrayList<FwArchEntry> versions;
    }

    public static class FwRepoEntry {
        public @Nullable String url; // prod
        public @Nullable String version;

        @SerializedName("beta_url")
        public @Nullable String betaUrl; // beta version if avilable
        @SerializedName("beta_ver")
        public @Nullable String betaVer;
    }

    public ShellyManagerPage(ConfigurationAdmin configurationAdmin, ShellyTranslationProvider translationProvider,
            HttpClient httpClient, String localIp, int localPort, ShellyHandlerFactory handlerFactory) {
        this.configurationAdmin = configurationAdmin;
        this.resources = translationProvider;
        this.handlerFactory = handlerFactory;
        this.httpClient = httpClient;
        this.localIp = localIp;
        this.localPort = localPort;
    }

    public ShellyMgrResponse generateContent(String path, Map<String, String[]> parameters) throws ShellyApiException {
        return new ShellyMgrResponse("Invalid Request", HttpStatus.BAD_REQUEST_400);
    }

    protected String loadHTML(String template) throws ShellyApiException {
        if (htmlTemplates.containsKey(template)) {
            return getString(htmlTemplates.get(template));
        }

        String html = "";
        String file = TEMPLATE_PATH + template;
        logger.debug("Read HTML from {}", file);
        ClassLoader cl = ShellyManagerInterface.class.getClassLoader();
        if (cl != null) {
            try (InputStream inputStream = cl.getResourceAsStream(file)) {
                if (inputStream != null) {
                    html = new BufferedReader(new InputStreamReader(inputStream)).lines()
                            .collect(Collectors.joining("\n"));
                    htmlTemplates.put(template, html);
                }
            } catch (IOException e) {
                throw new ShellyApiException("Unable to read " + file + " from bundle resources!", e);
            }
        }
        return html;
    }

    protected String loadHTML(String template, Map<String, String> properties) throws ShellyApiException {
        properties.put(ATTRIBUTE_URI, SHELLY_MANAGER_URI);
        String html = loadHTML(template);
        return fillAttributes(html, properties);
    }

    protected Map<String, String> fillProperties(Map<String, String> properties, String uid,
            ShellyManagerInterface th) {
        try {
            Configuration serviceConfig = configurationAdmin.getConfiguration("binding." + BINDING_ID);
            bindingConfig.updateFromProperties(serviceConfig.getProperties());
        } catch (IOException e) {
            logger.debug("ShellyManager: Unable to get bindingConfig");
        }

        properties.putAll(th.getThing().getProperties());

        Thing thing = th.getThing();
        ThingStatus status = thing.getStatus();
        properties.put("thingName", getString(thing.getLabel()));
        properties.put("thingStatus", status.toString());
        ThingStatusDetail detail = thing.getStatusInfo().getStatusDetail();
        properties.put("thingStatusDetail", detail.equals(ThingStatusDetail.NONE) ? "" : getString(detail.toString()));
        properties.put("thingStatusDescr", getString(thing.getStatusInfo().getDescription()));
        properties.put(ATTRIBUTE_UID, uid);

        ShellyDeviceProfile profile = th.getProfile();
        ShellyThingConfiguration config = thing.getConfiguration().as(ShellyThingConfiguration.class);
        ShellyDeviceStats stats = th.getStats();
        properties.putAll(stats.asProperties());

        for (Map.Entry<String, @Nullable Object> p : thing.getConfiguration().getProperties().entrySet()) {
            String key = p.getKey();
            Object o = p.getValue();
            if (o != null) {
                properties.put(key, o.toString());
            }
        }

        State state = th.getChannelValue(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_NAME);
        if (state != UnDefType.NULL) {
            addAttribute(properties, th, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_NAME);
        } else {
            // If the Shelly doesn't provide a device name (not configured) we use the service name
            String deviceName = getDeviceName(properties);
            properties.put(PROPERTY_DEV_NAME,
                    !deviceName.isEmpty() ? deviceName : getString(properties.get(PROPERTY_SERVICE_NAME)));
        }

        if (config.userId.isEmpty()) {
            // Get defauls from Binding Config
            properties.put("userId", bindingConfig.defaultUserId);
            properties.put("password", bindingConfig.defaultPassword);
        }

        addAttribute(properties, th, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_RSSI);
        addAttribute(properties, th, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_UPTIME);
        addAttribute(properties, th, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_HEARTBEAT);
        addAttribute(properties, th, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ITEMP);
        addAttribute(properties, th, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_WAKEUP);
        addAttribute(properties, th, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_CHARGER);
        addAttribute(properties, th, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_UPDATE);
        addAttribute(properties, th, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ALARM);
        addAttribute(properties, th, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_CHARGER);

        properties.put(ATTRIBUTE_DEBUG_MODE, getOption(profile.settings.debugEnable));
        properties.put(ATTRIBUTE_DISCOVERABLE, String.valueOf(getBool(profile.settings.discoverable)));
        properties.put(ATTRIBUTE_WIFI_RECOVERY, String.valueOf(getBool(profile.settings.wifiRecoveryReboot)));
        properties.put(ATTRIBUTE_APR_MODE,
                profile.settings.apRoaming != null ? getOption(profile.settings.apRoaming.enabled) : "n/a");
        properties.put(ATTRIBUTE_APR_TRESHOLD,
                profile.settings.apRoaming != null ? getOption(profile.settings.apRoaming.threshold) : "n/a");
        properties.put(ATTRIBUTE_PWD_PROTECT,
                profile.auth ? "enabled, user=" + getString(profile.settings.login.username) : "disabled");
        String tz = getString(profile.settings.timezone);
        properties.put(ATTRIBUTE_TIMEZONE,
                (tz.isEmpty() ? "n/a" : tz) + ", auto-detect: " + getBool(profile.settings.tzautodetect));
        properties.put(ATTRIBUTE_ACTIONS_SKIPPED,
                profile.status.astats != null ? String.valueOf(profile.status.astats.skipped) : "n/a");
        properties.put(ATTRIBUTE_MAX_ITEMP, stats.maxInternalTemp > 0 ? stats.maxInternalTemp + " °C" : "n/a");

        // Shelly H&T: When external power is connected the battery level is not valid
        if (!profile.isHT || (getInteger(profile.settings.externalPower) == 0)) {
            addAttribute(properties, th, CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_LEVEL);
        } else {
            properties.put(CHANNEL_SENSOR_BAT_LEVEL, "USB");
        }

        String wiFiSignal = getString(properties.get(CHANNEL_DEVST_RSSI));
        if (!wiFiSignal.isEmpty()) {
            properties.put("wifiSignalRssi", wiFiSignal + " / " + stats.wifiRssi + " dBm");
            properties.put("imgWiFi", "imgWiFi" + wiFiSignal);
        }

        if (profile.settings.sntp != null) {
            properties.put(ATTRIBUTE_SNTP_SERVER,
                    getString(profile.settings.sntp.server) + ", enabled: " + getBool((profile.settings.sntp.enabled)));
        }

        boolean coiotEnabled = true;
        if ((profile.settings.coiot != null) && (profile.settings.coiot.enabled != null)) {
            coiotEnabled = profile.settings.coiot.enabled;
        }
        properties.put(ATTRIBUTE_COIOT_STATUS,
                !coiotEnabled ? "Disbaled in settings" : "Events are " + (config.eventsCoIoT ? "enabled" : "disabled"));
        properties.put(ATTRIBUTE_COIOT_PEER,
                (profile.settings.coiot != null) && !getString(profile.settings.coiot.peer).isEmpty()
                        ? profile.settings.coiot.peer
                        : "Multicast");
        if (profile.status.cloud != null) {
            properties.put(ATTRIBUTE_CLOUD_STATUS,
                    getBool(profile.settings.cloud.enabled)
                            ? getBool(profile.status.cloud.connected) ? "connected" : "enabled"
                            : "disabled");
        } else {
            properties.put(ATTRIBUTE_CLOUD_STATUS, "unknown");
        }
        if (profile.status.mqtt != null) {
            properties.put(ATTRIBUTE_MQTT_STATUS,
                    getBool(profile.settings.mqtt.enable)
                            ? getBool(profile.status.mqtt.connected) ? "connected" : "enabled"
                            : "disabled");
        } else {
            properties.put(ATTRIBUTE_MQTT_STATUS, "unknown");
        }

        String statusIcon = "";
        ThingStatus ts = th.getThing().getStatus();
        switch (ts) {
            case UNINITIALIZED:
            case REMOVED:
            case REMOVING:
                statusIcon = ICON_UNINITIALIZED;
                break;
            case OFFLINE:
                ThingStatusDetail sd = th.getThing().getStatusInfo().getStatusDetail();
                if (uid.contains(THING_TYPE_SHELLYUNKNOWN_STR) || (sd == ThingStatusDetail.CONFIGURATION_ERROR)
                        || (sd == ThingStatusDetail.HANDLER_CONFIGURATION_PENDING)) {
                    statusIcon = ICON_CONFIG;
                    break;
                }
            default:
                statusIcon = ts.toString();
        }
        properties.put(ATTRIBUTE_STATUS_ICON, statusIcon.toLowerCase());

        return properties;
    }

    private void addAttribute(Map<String, String> properties, ShellyManagerInterface thingHandler, String group,
            String attribute) {
        State state = thingHandler.getChannelValue(group, attribute);
        String value = "";
        if (state != UnDefType.NULL) {
            if (state instanceof DateTimeType) {
                DateTimeType dt = (DateTimeType) state;
                switch (attribute) {
                    case ATTRIBUTE_LAST_ALARM:
                        value = dt.format(null).replace('T', ' ').replace('-', '/');
                        break;
                    default:
                        value = getTimestamp(dt);
                        value = dt.format(null).replace('T', ' ').replace('-', '/');
                }
            } else {
                value = state.toString();
            }
        }
        properties.put(attribute, value);
    }

    protected String fillAttributes(String template, Map<String, String> properties) {
        if (!template.contains("${")) {
            // no replacement necessary
            return template;
        }

        String result = template;
        for (Map.Entry<String, String> var : properties.entrySet()) {
            result = result.replaceAll(java.util.regex.Pattern.quote("${" + var.getKey() + "}"),
                    getValue(properties, var.getKey()));
        }

        if (result.contains("${")) {
            return result.replaceAll("\\Q${\\E.*}", "");
        } else {
            return result;
        }
    }

    protected String getValue(Map<String, String> properties, String attribute) {
        String value = getString(properties.get(attribute));
        if (!value.isEmpty()) {
            switch (attribute) {
                case PROPERTY_FIRMWARE_VERSION:
                    value = substringBeforeLast(value, "-");
                    break;
                case PROPERTY_UPDATE_AVAILABLE:
                    value = value.replace(OnOffType.ON.toString(), "yes");
                    value = value.replace(OnOffType.OFF.toString(), "no");
                    break;
                case CHANNEL_DEVST_HEARTBEAT:
                    break;
            }
        }
        return value;
    }

    protected FwRepoEntry getFirmwareRepoEntry(String deviceType, String mode) throws ShellyApiException {
        logger.debug("ShellyManager: Load firmware list from {}", FWREPO_PROD_URL);
        FwRepoEntry fw = null;
        if (firmwareRepo.containsKey(deviceType)) {
            fw = firmwareRepo.get(deviceType);
        }
        String json = httpGet(FWREPO_PROD_URL); // returns a strange JSON format so we are parsing this manually
        String entry = substringBetween(json, "\"" + deviceType + "\":{", "}");
        if (!entry.isEmpty()) {
            entry = "{" + entry + "}";
            /*
             * Example:
             * "SHPLG-1":{
             * "url":"http:\/\/repo.shelly.cloud\/firmware\/SHPLG-1.zip",
             * "version":"20201228-092318\/v1.9.3@ad2bb4e3",
             * "beta_url":"http:\/\/repo.shelly.cloud\/firmware\/rc\/SHPLG-1.zip",
             * "beta_ver":"20201223-093703\/v1.9.3-rc5@3f583801"
             * },
             */
            fw = fromJson(gson, entry, FwRepoEntry.class);

            // Special case: RGW2 has a split firmware - xxx-white.zip vs. xxx-color.zip
            if (!mode.isEmpty() && deviceType.equalsIgnoreCase(SHELLYDT_RGBW2)) {
                // check for spilt firmware
                String url = substringBefore(fw.url, ".zip") + "-" + mode + ".zip";
                if (testUrl(url)) {
                    fw.url = url;
                    logger.debug("ShellyManager: Release Split-URL for device type {} is {}", deviceType, url);
                }
                url = substringBefore(fw.betaUrl, ".zip") + "-" + mode + ".zip";
                if (testUrl(url)) {
                    fw.betaUrl = url;
                    logger.debug("ShellyManager: Beta Split-URL for device type {} is {}", deviceType, url);
                }
            }

            firmwareRepo.put(deviceType, fw);
        }

        return fw != null ? fw : new FwRepoEntry();
    }

    protected FwArchList getFirmwareArchiveList(String deviceType) throws ShellyApiException {
        FwArchList list;
        String json = "";

        if (firmwareArch.contains(deviceType)) {
            list = firmwareArch.get(deviceType); // return from cache
            if (list != null) {
                return list;
            }
        }

        try {
            if (!deviceType.isEmpty()) {
                json = httpGet(FWREPO_ARCH_URL + "?type=" + deviceType);
            }
        } catch (ShellyApiException e) {
            logger.debug("{}: Unable to get firmware list for device type {}: {}", LOG_PREFIX, deviceType,
                    e.toString());
        }
        if (json.isEmpty() || json.startsWith("[]")) {
            // no files available for this device type
            logger.info("{}: No firmware files found for device type {}", LOG_PREFIX, deviceType);
            list = new FwArchList();
            list.versions = new ArrayList<FwArchEntry>();
        } else {
            // Create selection list
            json = "{" + json.replace("[{", "\"versions\":[{") + "}"; // make it an named array
            list = fromJson(gson, json, FwArchList.class);
        }

        // save list to cache
        firmwareArch.put(deviceType, list);
        return list;
    }

    protected boolean testUrl(String url) {
        try {
            if (url.isEmpty()) {
                return false;
            }
            httpHeadl(url); // causes exception on 404
            return true;
        } catch (ShellyApiException e) {
        }
        return false;
    }

    protected String httpGet(String url) throws ShellyApiException {
        return httpRequest(HttpMethod.GET, url);
    }

    protected String httpHeadl(String url) throws ShellyApiException {
        return httpRequest(HttpMethod.HEAD, url);
    }

    protected String httpRequest(HttpMethod method, String url) throws ShellyApiException {
        ShellyApiResult apiResult = new ShellyApiResult();

        try {
            Request request = httpClient.newRequest(url).method(method).timeout(SHELLY_API_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);
            request.header(HttpHeader.ACCEPT, ShellyHttpClient.CONTENT_TYPE_JSON);
            logger.trace("{}: HTTP {} {}", LOG_PREFIX, method, url);
            ContentResponse contentResponse = request.send();
            apiResult = new ShellyApiResult(contentResponse);
            String response = contentResponse.getContentAsString().replace("\t", "").replace("\r\n", "").trim();
            logger.trace("{}: HTTP Response {}: {}", LOG_PREFIX, contentResponse.getStatus(), response);

            // validate response, API errors are reported as Json
            if (contentResponse.getStatus() != HttpStatus.OK_200) {
                throw new ShellyApiException(apiResult);
            }
            return response;
        } catch (ExecutionException | TimeoutException | InterruptedException | IllegalArgumentException e) {
            throw new ShellyApiException("HTTP GET failed", e);
        }
    }

    protected String getUrlParm(Map<String, String[]> parameters, String param) {
        String[] p = parameters.get(param);
        String value = "";
        if (p != null) {
            value = getString(p[0]);
        }
        return value;
    }

    protected String getMessage(String key, Object... arguments) {
        return resources.get("manager." + key, arguments);
    }

    protected String getMessageP(String key, String msgClass, Object... arguments) {
        return "<p class=\"" + msgClass + "\">" + getMessage(key, arguments) + "</p>\n";
    }

    protected String getMessageS(String key, String msgClass, Object... arguments) {
        return "<span class=\"" + msgClass + "\">" + getMessage(key, arguments) + "</span>\n";
    }

    protected static String getDeviceType(Map<String, String> properties) {
        return getString(properties.get(PROPERTY_MODEL_ID));
    }

    protected static String getDeviceIp(Map<String, String> properties) {
        return getString(properties.get("deviceIp"));
    }

    protected static String getDeviceName(Map<String, String> properties) {
        return getString(properties.get(PROPERTY_DEV_NAME));
    }

    protected static String getOption(@Nullable Boolean option) {
        if (option == null) {
            return "n/a";
        }
        return option ? "enabled" : "disabled";
    }

    protected static String getOption(@Nullable Integer option) {
        if (option == null) {
            return "n/a";
        }
        return option.toString();
    }

    protected static String getDisplayName(Map<String, String> properties) {
        String name = getString(properties.get(PROPERTY_DEV_NAME));
        if (name.isEmpty()) {
            name = getString(properties.get(PROPERTY_SERVICE_NAME));
        }
        return name;
    }

    protected ShellyThingConfiguration getThingConfig(ShellyManagerInterface th, Map<String, String> properties) {
        Thing thing = th.getThing();
        ShellyThingConfiguration config = thing.getConfiguration().as(ShellyThingConfiguration.class);
        if (config.userId.isEmpty()) {
            config.userId = getString(properties.get("userId"));
            config.password = getString(properties.get("password"));
        }
        return config;
    }

    protected void scheduleUpdate(ShellyManagerInterface th, String name, int delay) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                th.requestUpdates(1, true);
            }
        };
        Timer timer = new Timer(name);
        timer.schedule(task, delay * 1000);
    }

    protected Map<String, ShellyManagerInterface> getThingHandlers() {
        return handlerFactory.getThingHandlers();
    }

    protected @Nullable ShellyManagerInterface getThingHandler(String uid) {
        return getThingHandlers().get(uid);
    }
}
