/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import static org.openhab.binding.shelly.internal.api.ShellyDeviceProfile.extractFwVersion;
import static org.openhab.binding.shelly.internal.manager.ShellyManagerConstants.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.shelly.internal.ShellyHandlerFactory;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyDeviceStats;
import org.openhab.binding.shelly.internal.handler.ShellyManagerInterface;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.binding.shelly.internal.util.ShellyVersionDTO;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ShellyManagerOtaPage} implements the Shelly Manager's device overview page
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyManagerOverviewPage extends ShellyManagerPage {
    private final Logger logger = LoggerFactory.getLogger(ShellyManagerOverviewPage.class);

    public ShellyManagerOverviewPage(ConfigurationAdmin configurationAdmin,
            ShellyTranslationProvider translationProvider, HttpClient httpClient, String localIp, int localPort,
            ShellyHandlerFactory handlerFactory) {
        super(configurationAdmin, translationProvider, httpClient, localIp, localPort, handlerFactory);
    }

    @Override
    public ShellyMgrResponse generateContent(String path, Map<String, String[]> parameters) throws ShellyApiException {
        String filter = getUrlParm(parameters, URLPARM_FILTER).toLowerCase();
        String action = getUrlParm(parameters, URLPARM_ACTION).toLowerCase();
        String uidParm = getUrlParm(parameters, URLPARM_UID).toLowerCase();

        logger.debug("Generating overview for {} devices", getThingHandlers().size());

        String html = "";
        Map<String, String> properties = new HashMap<>();
        properties.put(ATTRIBUTE_METATAG, "<meta http-equiv=\"refresh\" content=\"60\" />");
        properties.put(ATTRIBUTE_CSS_HEADER, loadHTML(OVERVIEW_HEADER, properties));

        String deviceHtml = "";
        TreeMap<String, ShellyManagerInterface> sortedMap = new TreeMap<>();
        for (Map.Entry<String, ShellyManagerInterface> th : getThingHandlers().entrySet()) { // sort by Device Name
            ShellyManagerInterface handler = th.getValue();
            sortedMap.put(getDisplayName(handler.getThing().getProperties()), handler);
        }

        html = loadHTML(HEADER_HTML, properties);
        html += loadHTML(OVERVIEW_HTML, properties);

        int filteredDevices = 0;
        for (Map.Entry<String, ShellyManagerInterface> handler : sortedMap.entrySet()) {
            try {
                ShellyManagerInterface th = handler.getValue();
                ThingStatus status = th.getThing().getStatus();
                ShellyDeviceProfile profile = th.getProfile();
                String uid = getString(th.getThing().getUID().getAsString()); // handler.getKey();

                if (action.equals(ACTION_REFRESH) && (uidParm.isEmpty() || uidParm.equals(uid))) {
                    // Refresh thing status, this is asynchronosly and takes 0-3sec
                    th.requestUpdates(1, true);
                } else if (action.equals(ACTION_RES_STATS) && (uidParm.isEmpty() || uidParm.equals(uid))) {
                    th.resetStats();
                } else if (action.equals(ACTION_OTACHECK) && (uidParm.isEmpty() || uidParm.equals(uid))) {
                    th.resetStats();
                }

                Map<String, String> warnings = getStatusWarnings(th);
                if (applyFilter(th, filter) || (filter.equals(FILTER_ATTENTION) && !warnings.isEmpty())) {
                    filteredDevices++;
                    properties.clear();
                    fillProperties(properties, uid, handler.getValue());
                    String deviceType = getDeviceType(properties);

                    properties.put(ATTRIBUTE_DISPLAY_NAME, getDisplayName(properties));
                    properties.put(ATTRIBUTE_DEV_STATUS, fillDeviceStatus(warnings));
                    if (!warnings.isEmpty() && (status != ThingStatus.UNKNOWN)) {
                        properties.put(ATTRIBUTE_STATUS_ICON, ICON_ATTENTION);
                    }
                    if (!"unknown".equalsIgnoreCase(deviceType) && (status == ThingStatus.ONLINE)) {
                        properties.put(ATTRIBUTE_FIRMWARE_SEL, fillFirmwareHtml(profile, uid, deviceType));
                        properties.put(ATTRIBUTE_ACTION_LIST, fillActionHtml(th, uid));
                    } else {
                        properties.put(ATTRIBUTE_FIRMWARE_SEL, "");
                        properties.put(ATTRIBUTE_ACTION_LIST, "");
                    }
                    html += loadHTML(OVERVIEW_DEVICE, properties);
                }
            } catch (ShellyApiException e) {
                logger.debug("{}: Exception", LOG_PREFIX, e);
            }
        }

        properties.clear();
        properties.put("numberDevices", "<span class=\"footerDevices\">" + "Number of devices: " + filteredDevices
                + " of " + getThingHandlers().size() + "&nbsp;</span>");
        properties.put(ATTRIBUTE_CSS_FOOTER, loadHTML(OVERVIEW_FOOTER, properties));
        html += deviceHtml + loadHTML(FOOTER_HTML, properties);
        return new ShellyMgrResponse(fillAttributes(html, properties), HttpStatus.OK_200);
    }

    private String fillFirmwareHtml(ShellyDeviceProfile profile, String uid, String deviceType)
            throws ShellyApiException {
        String html = "\n\t\t\t\t<select name=\"fwList\" id=\"fwList\" onchange=\"location = this.options[this.selectedIndex].value;\">\n";
        html += "\t\t\t\t\t<option value=\"\" selected disabled hidden>update to</option>\n";

        String pVersion = "";
        String bVersion = "";
        String updateUrl = SHELLY_MGR_FWUPDATE_URI + "?" + URLPARM_UID + "=" + urlEncode(uid);
        try {
            if (!profile.isGen2) { // currently there is no public firmware repo for Gen2
                logger.debug("{}: Load firmware version list for device type {}", LOG_PREFIX, deviceType);
                FwRepoEntry fw = getFirmwareRepoEntry(deviceType, profile.mode);

                pVersion = extractFwVersion(fw.version);
                bVersion = extractFwVersion(fw.betaVer);
            } else {
                pVersion = extractFwVersion(getString(profile.status.update.newVersion));
                bVersion = extractFwVersion(getString(profile.status.update.betaVersion));
            }
            if (!pVersion.isEmpty()) {
                html += "\t\t\t\t\t<option value=\"" + updateUrl + "&" + URLPARM_VERSION + "=" + FWPROD + "\">Release "
                        + pVersion + "</option>\n";
            }
            if (!bVersion.isEmpty()) {
                html += "\t\t\t\t\t<option value=\"" + updateUrl + "&" + URLPARM_VERSION + "=" + FWBETA + "\">Beta "
                        + bVersion + "</option>\n";
            }

            if (!profile.isGen2) { // currently no online repo for Gen2
                // Add those from Shelly Firmware Archive
                String json = httpGet(FWREPO_ARCH_URL + "?" + URLPARM_TYPE + "=" + deviceType);
                if (json.startsWith("[]")) {
                    // no files available for this device type
                    logger.debug("{}: No firmware files found for device type {}", LOG_PREFIX, deviceType);
                } else {
                    // Create selection list
                    json = "{" + json.replace("[{", "\"versions\":[{") + "}"; // make it a named array
                    FwArchList list = getFirmwareArchiveList(deviceType);
                    ArrayList<FwArchEntry> versions = list.versions;
                    if (versions != null) {
                        html += "\t\t\t\t\t<option value=\"\" disabled>-- Archive:</option>\n";
                        for (int i = versions.size() - 1; i >= 0; i--) {
                            FwArchEntry e = versions.get(i);
                            String version = getString(e.version);
                            ShellyVersionDTO v = new ShellyVersionDTO();
                            if (!version.equalsIgnoreCase(pVersion) && !version.equalsIgnoreCase(bVersion)
                                    && (v.compare(version, SHELLY_API_MIN_FWCOIOT) >= 0)
                                    || version.contains("master")) {
                                html += "\t\t\t\t\t<option value=\"" + updateUrl + "&" + URLPARM_VERSION + "=" + version
                                        + "\">" + version + "</option>\n";
                            }
                        }
                    }
                }
            }
        } catch (

        ShellyApiException e) {
            logger.debug("{}: Unable to retrieve firmware list: {}", LOG_PREFIX, e.toString());
        }

        html += "\t\t\t\t\t<option class=\"select-hr\" value=\"" + SHELLY_MGR_FWUPDATE_URI + "?uid=" + uid
                + "&connection=custom\">Custom URL</option>\n";

        html += "\t\t\t\t</select>\n\t\t\t";

        return html;
    }

    private String fillActionHtml(ShellyManagerInterface handler, String uid) {
        String html = "\n\t\t\t\t<select name=\"actionList\" id=\"actionList\" onchange=\"location = '"
                + SHELLY_MGR_ACTION_URI + "?uid=" + urlEncode(uid) + "&" + URLPARM_ACTION
                + "='+this.options[this.selectedIndex].value;\">\n";
        html += "\t\t\t\t\t<option value=\"\" selected disabled>select</option>\n";

        Map<String, String> actionList = ShellyManagerActionPage.getActions(handler.getProfile());
        for (Map.Entry<String, String> a : actionList.entrySet()) {
            String value = a.getValue();
            String seperator = "";
            if (value.startsWith("-")) {
                // seperator = "class=\"select-hr\" ";
                html += "\t\t\t\t\t<option class=\"select-hr\" role=\"seperator\" disabled>&nbsp;</option>\n";
                value = substringAfterLast(value, "-");
            }
            html += "\t\t\t\t\t<option " + seperator + "value=\"" + a.getKey()
                    + (value.startsWith(ACTION_NONE) ? " disabled " : "") + "\">" + value + "</option>\n";
        }
        html += "\t\t\t\t</select>\n\t\t\t";
        return html;
    }

    private boolean applyFilter(ShellyManagerInterface handler, String filter) {
        ThingStatus status = handler.getThing().getStatus();
        ShellyDeviceProfile profile = handler.getProfile();

        switch (filter) {
            case FILTER_ONLINE:
                return status == ThingStatus.ONLINE;
            case FILTER_INACTIVE:
                return status != ThingStatus.ONLINE;
            case FILTER_ATTENTION:
                return false;
            case FILTER_UPDATE:
                // return handler.getChannelValue(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_UPDATE) == OnOffType.ON;
                return getBool(profile.status.hasUpdate);
            case FILTER_UNPROTECTED:
                return !profile.auth;
            case "*":
            default:
                return true;
        }
    }

    private Map<String, String> getStatusWarnings(ShellyManagerInterface handler) {
        Thing thing = handler.getThing();
        ThingStatus status = handler.getThing().getStatus();
        ShellyDeviceStats stats = handler.getStats();
        ShellyDeviceProfile profile = handler.getProfile();
        ShellyThingConfiguration config = thing.getConfiguration().as(ShellyThingConfiguration.class);
        TreeMap<String, String> result = new TreeMap<>();

        if ((status != ThingStatus.ONLINE) && (status != ThingStatus.UNKNOWN)) {
            result.put("Thing Status", status.toString());
        }
        State wifiSignal = handler.getChannelValue(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_RSSI);
        if ((profile.alwaysOn || (profile.hasBattery && (status == ThingStatus.ONLINE)))
                && ((wifiSignal != UnDefType.NULL) && (((DecimalType) wifiSignal).intValue() < 2))) {
            result.put("Weak WiFi Signal", wifiSignal.toString());
        }
        if (profile.hasBattery) {
            State lowBattery = handler.getChannelValue(CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_LOW);
            if ((lowBattery == OnOffType.ON)) {
                lowBattery = handler.getChannelValue(CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_LEVEL);
                result.put("Battery Low", lowBattery.toString());
            }
        }

        if (stats.lastAlarm.equalsIgnoreCase(ALARM_TYPE_RESTARTED)) {
            result.put("Device Alarm", ALARM_TYPE_RESTARTED + " (" + convertTimestamp(stats.lastAlarmTs) + ")");
        }
        if (getBool(profile.status.overtemperature)) {
            result.put("Device Alarm", ALARM_TYPE_OVERTEMP);
        }
        if (getBool(profile.status.overload)) {
            result.put("Device Alarm", ALARM_TYPE_OVERLOAD);
        }
        if (getBool(profile.status.loaderror)) {
            result.put("Device Alarm", ALARM_TYPE_LOADERR);
        }
        if (profile.isSensor) {
            State sensorError = handler.getChannelValue(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_ERROR);
            if (sensorError != UnDefType.NULL) {
                if (!sensorError.toString().isEmpty()) {
                    result.put("Device Alarm", ALARM_TYPE_SENSOR_ERROR);
                }
            }
        }
        if (profile.alwaysOn && (status == ThingStatus.ONLINE)) {
            if ((config.eventsCoIoT) && (profile.settings.coiot != null)) {
                if ((profile.settings.coiot.enabled != null) && !profile.settings.coiot.enabled) {
                    result.put("CoIoT Status", "COIOT_DISABLED");
                } else if (stats.protocolMessages == 0) {
                    result.put("CoIoT Discovery", "NO_COIOT_DISCOVERY");
                } else if (stats.protocolMessages < 2) {
                    result.put("CoIoT Multicast", "NO_COIOT_MULTICAST");
                }
            }
        }

        return result;
    }

    private String fillDeviceStatus(Map<String, String> devStatus) {
        if (devStatus.isEmpty()) {
            return "";
        }

        String result = "\t\t\t\t<tr><td colspan = \"2\">Notifications:</td></tr>";
        for (Map.Entry<String, String> ds : devStatus.entrySet()) {
            result += "\t\t\t\t<tr><td>" + ds.getKey() + "</td><td>" + ds.getValue() + "</td></tr>\n";
        }
        return result;
    }
}
