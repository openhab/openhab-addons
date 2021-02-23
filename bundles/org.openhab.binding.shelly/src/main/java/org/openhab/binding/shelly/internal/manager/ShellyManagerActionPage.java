/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.PROPERTY_SERVICE_NAME;
import static org.openhab.binding.shelly.internal.manager.ShellyManagerConstants.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.shelly.internal.ShellyHandlerFactory;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsLogin;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api.ShellyHttpApi;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyManagerInterface;
import org.openhab.core.thing.ThingStatusDetail;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ShellyManagerActionPage} implements the Shelly Manager's action page
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyManagerActionPage extends ShellyManagerPage {
    private final Logger logger = LoggerFactory.getLogger(ShellyManagerActionPage.class);

    public ShellyManagerActionPage(ConfigurationAdmin configurationAdmin, HttpClient httpClient, String localIp,
            int localPort, ShellyHandlerFactory handlerFactory) {
        super(configurationAdmin, httpClient, localIp, localPort, handlerFactory);
    }

    @Override
    public ShellyMgrResponse generateContent(String path, Map<String, String[]> parameters) throws ShellyApiException {
        String action = getUrlParm(parameters, URLPARM_ACTION);
        String uid = getUrlParm(parameters, URLPARM_UID);
        String update = getUrlParm(parameters, URLPARM_UPDATE);
        if (uid.isEmpty() || action.isEmpty()) {
            return new ShellyMgrResponse("Invalid URL parameters: " + parameters.toString(),
                    HttpStatus.BAD_REQUEST_400);
        }

        Map<String, String> properties = new HashMap<>();
        properties.put(ATTRIBUTE_METATAG, "");
        properties.put(ATTRIBUTE_CSS_HEADER, "");
        properties.put(ATTRIBUTE_CSS_FOOTER, "");
        String html = loadHTML(HEADER_HTML, properties);

        ShellyManagerInterface th = getThingHandler(uid);
        if (th != null) {
            fillProperties(properties, uid, th);

            Map<String, String> actions = getActions();
            String actionUrl = SHELLY_MGR_OVERVIEW_URI;
            String actionButtonLabel = "Perform Action"; // Default
            String serviceName = getValue(properties, PROPERTY_SERVICE_NAME);
            String message = "";

            ShellyThingConfiguration config = getThingConfig(th, properties);
            ShellyDeviceProfile profile = th.getProfile();
            ShellyHttpApi api = th.getApi();
            new ShellyHttpApi(uid, config, httpClient);

            int refreshTimer = 5; // standard
            switch (action) {
                case ACTION_RESTART:
                    if (update.equalsIgnoreCase("yes")) {
                        message = "The device is restarting and reconnects to WiFi. It will take a moment until device status is refreshed in openHAB.";
                        actionButtonLabel = "Ok";
                        new Thread(() -> { // schedule asynchronous reboot
                            try {
                                api.deviceReboot();
                            } catch (ShellyApiException e) {
                                // maybe the device restarts before returning the http response
                            }
                            setRestarted(th, uid); // refresh 20s after reboot
                        }).start();
                        refreshTimer = profile.isMotion ? 30 : 15;
                    } else {
                        message = "<span class\"info\">The device will restart and reconnects to WiFi.</span>";
                        actionUrl = buildActionUrl(uid, action);
                    }
                    break;
                case ACTION_RESET:
                    if (!update.equalsIgnoreCase("yes")) {
                        message = "<p class=\"warning\">Attention: Performing this action will reset the device to factory defaults.<br/>"
                                + "All configuration data incl. WiFi settings get lost and device will return to Access Point mode (WiFi "
                                + serviceName + ").</p>";
                        actionUrl = buildActionUrl(uid, action);
                    } else {
                        message = "<p class=\"info\">Factorry reset was performed. Connect to WiFi network "
                                + serviceName + " and open http://192.168.33.1 to restart with device setup.</p>";
                        actionButtonLabel = "Ok";
                        new Thread(() -> { // schedule asynchronous reboot
                            try {
                                api.factoryReset();
                            } catch (ShellyApiException e) {
                                // maybe the device restarts before returning the http response
                            }
                            setRestarted(th, uid);
                        }).start();
                    }
                    break;
                case ACTION_PROTECT:
                    // Get device settings
                    if (config.userId.isEmpty() || config.password.isEmpty()) {
                        message = "<p style=\"color:red;\">To use this feature you need to set default credentials in the Shelly Binding settings.</p>";
                        break;
                    }

                    if (!update.equalsIgnoreCase("yes")) {
                        ShellySettingsLogin status = api.getLoginSettings();
                        message = "Device protection is currently " + (status.enabled ? "enabled" : "disabled<br/>");
                        message += "<p class=\"info\">Device login will be set to user ${userId} with password ${password}.</p>";
                        actionUrl = buildActionUrl(uid, action);
                    } else {
                        api.setLoginCredentials(config.userId, config.password);
                        message = "<p class=\"info\">Device login was updated to user ${userId} with password ${password}.</p>";
                        actionButtonLabel = "Ok";
                    }
                    break;
                case ACTION_RES_STATS:
                    th.resetStats();
                    message = "<p class=\"info\">Device statistics and alarm has been reset.</p>";
                    actionButtonLabel = "Ok";
                    break;
                case ACTION_ENCLOUD:
                case ACTION_DISCLOUD:
                    boolean enabled = action.equals(ACTION_ENCLOUD);
                    api.setCloud(enabled);
                    message = "<p class=\"info\">Cloud function is now " + (enabled ? "enabled" : "disabled") + ".</p>";
                    actionButtonLabel = "Ok";
                    refreshTimer = 15;
                    break;
                case ACTION_NONE:
                    break;
                default:
                    logger.warn("{}: Unknown action {} requested", LOG_PREFIX, action);
            }

            properties.put(ATTRIBUTE_ACTION, getString(actions.get(action))); // get description for command
            properties.put(ATTRIBUTE_ACTION_BUTTON, actionButtonLabel);
            properties.put(ATTRIBUTE_ACTION_URL, actionUrl);
            message = fillAttributes(message, properties);
            properties.put(ATTRIBUTE_MESSAGE, message);
            properties.put(ATTRIBUTE_REFRESH, String.valueOf(refreshTimer));
            html += loadHTML(ACTION_HTML, properties);
            th.requestUpdates(1, false); // trigger background update
        }

        properties.clear();
        html += loadHTML(FOOTER_HTML, properties);
        return new ShellyMgrResponse(html, HttpStatus.OK_200);
    }

    public static Map<String, String> getActions() {
        Map<String, String> list = new LinkedHashMap<>();
        list.put(ACTION_RES_STATS, "Reset Statistics");
        list.put(ACTION_RESTART, "Reboot Device");
        // list.put(ACTION_NONE, "----------------");
        list.put(ACTION_PROTECT, "Protect Device");
        list.put(ACTION_ENCLOUD, "Enable Cloud");
        list.put(ACTION_DISCLOUD, "Disable Cloud");
        list.put(ACTION_RESET, "-Factory Reset");
        return list;
    }

    private String buildActionUrl(String uid, String action) {
        return SHELLY_MGR_ACTION_URI + "?" + URLPARM_ACTION + "=" + action + "&" + URLPARM_UID + "=" + urlEncode(uid)
                + "&" + URLPARM_UPDATE + "=yes";
    }

    private void setRestarted(ShellyManagerInterface th, String uid) {
        th.setThingOffline(ThingStatusDetail.GONE, "offline.status-error-restarted");
        scheduleUpdate(th, uid + "_upgrade", 20); // wait 20s before refresh
    }
}
