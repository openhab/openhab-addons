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

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.PROPERTY_SERVICE_NAME;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.SHELLY_COIOT_MCAST;
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
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyOtaCheckResult;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsLogin;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapJSonDTO;
import org.openhab.binding.shelly.internal.api1.Shelly1HttpApi;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyManagerInterface;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
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

    public ShellyManagerActionPage(ConfigurationAdmin configurationAdmin, ShellyTranslationProvider translationProvider,
            HttpClient httpClient, String localIp, int localPort, ShellyHandlerFactory handlerFactory) {
        super(configurationAdmin, translationProvider, httpClient, localIp, localPort, handlerFactory);
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

            Map<String, String> actions = getActions(th.getProfile());
            String actionUrl = SHELLY_MGR_OVERVIEW_URI;
            String actionButtonLabel = "OK"; // Default
            String serviceName = getValue(properties, PROPERTY_SERVICE_NAME);
            String message = "";

            ShellyThingConfiguration config = getThingConfig(th, properties);
            ShellyDeviceProfile profile = th.getProfile();
            ShellyApiInterface api = th.getApi();
            new Shelly1HttpApi(uid, config, httpClient);

            int refreshTimer = 0;
            switch (action) {
                case ACTION_RES_STATS:
                    th.resetStats();
                    message = getMessageP("action.resstats.confirm", MCINFO);
                    refreshTimer = 3;
                    break;
                case ACTION_RESTART:
                    if ("yes".equalsIgnoreCase(update)) {
                        message = getMessageP("action.restart.info", MCINFO);
                        actionButtonLabel = "Ok";
                        new Thread(() -> { // schedule asynchronous reboot
                            try {
                                api.deviceReboot();
                            } catch (ShellyApiException e) {
                                // maybe the device restarts before returning the http response
                            }
                            setRestarted(th, uid); // refresh after reboot
                        }).start();
                        refreshTimer = profile.isMotion ? 60 : 30;
                    } else {
                        message = getMessageS("action.restart.confirm", MCINFO);
                        actionUrl = buildActionUrl(uid, action);
                    }
                    break;
                case ACTION_PROTECT:
                    // Get device settings
                    if (config.userId.isEmpty() || config.password.isEmpty()) {
                        message = getMessageP("action.protect.id-missing", MCWARNING);
                        break;
                    }

                    if (!"yes".equalsIgnoreCase(update)) {
                        ShellySettingsLogin status = api.getLoginSettings();
                        message = getMessage("action.protect.status", getBool(status.enabled) ? "enabled" : "disabled",
                                status.username)
                                + getMessageP("action.protect.new", MCINFO, config.userId, config.password);
                        actionUrl = buildActionUrl(uid, action);
                    } else {
                        api.setLoginCredentials(config.userId, config.password);
                        message = getMessageP("action.protect.confirm", MCINFO, config.userId, config.password);
                        refreshTimer = 3;
                    }
                    break;
                case ACTION_SETCOIOT_MCAST:
                case ACTION_SETCOIOT_PEER:
                    if ((profile.settings.coiot == null) || (profile.settings.coiot.peer == null)) {
                        // feature not available
                        message = getMessage("coiot.mode-not-suppored", MCWARNING, action);
                        break;
                    }

                    String peer = getString(profile.settings.coiot.peer);
                    boolean mcast = peer.isEmpty() || SHELLY_COIOT_MCAST.equalsIgnoreCase(peer);
                    String newPeer = mcast ? localIp + ":" + Shelly1CoapJSonDTO.COIOT_PORT : SHELLY_COIOT_MCAST;
                    String displayPeer = mcast ? newPeer : "Multicast";

                    if (profile.isMotion && action.equalsIgnoreCase(ACTION_SETCOIOT_MCAST)) {
                        // feature not available
                        message = getMessageP("coiot.multicast-not-supported", "warning", displayPeer);
                        break;
                    }

                    if (!"yes".equalsIgnoreCase(update)) {
                        message = getMessageP("coiot.current-peer", MCMESSAGE, mcast ? "Multicast" : peer)
                                + getMessageP("coiot.new-peer", MCINFO, displayPeer)
                                + getMessageP(mcast ? "coiot.mode-peer" : "coiot.mode-mcast", MCMESSAGE);
                        actionUrl = buildActionUrl(uid, action);
                    } else {
                        new Thread(() -> { // schedule asynchronous reboot
                            try {
                                api.setCoIoTPeer(newPeer);
                                api.deviceReboot();
                            } catch (ShellyApiException e) {
                                // maybe the device restarts before returning the http response
                            }
                            setRestarted(th, uid); // refresh after reboot
                        }).start();

                        // The device needs a restart after changing the peer mode
                        message = getMessageP("action.restart.info", MCINFO);
                        refreshTimer = 30;
                    }
                    break;
                case ACTION_ENCLOUD:
                case ACTION_DISCLOUD:
                    boolean enabled = action.equals(ACTION_ENCLOUD);
                    api.setCloud(enabled);
                    message = getMessageP("action.setcloud.config", MCINFO, enabled ? "enabled" : "disabled");
                    refreshTimer = 20;
                    break;
                case ACTION_RESET:
                    if (!"yes".equalsIgnoreCase(update)) {
                        message = getMessageP("action.reset.warning", MCWARNING, serviceName);
                        actionUrl = buildActionUrl(uid, action);
                    } else {
                        new Thread(() -> { // schedule asynchronous reboot
                            try {
                                api.factoryReset();
                                setRestarted(th, uid);
                            } catch (ShellyApiException e) {
                                // maybe the device restarts before returning the http response
                            }
                        }).start();
                        message = getMessageP("action.reset.confirm", MCINFO, serviceName);
                        refreshTimer = 5;
                    }
                    break;
                case ACTION_OTACHECK:
                    try {
                        ShellyOtaCheckResult result = api.checkForUpdate();
                        message = getMessage("action.checkupd." + result.status);
                    } catch (ShellyApiException e) {
                        // maybe the device restarts before returning the http response
                        message = getMessageP("action.checkupd.failed", e.toString());
                    }
                    refreshTimer = 3;
                    break;
                case ACTION_ENDEBUG:
                case ACTION_DISDEBUG:
                    boolean enable = ACTION_ENDEBUG.equalsIgnoreCase(action);
                    if (!"yes".equalsIgnoreCase(update)) {
                        message = getMessage(enable ? "action.debug-enable" : "action.debug-disable");
                        actionUrl = buildActionUrl(uid, action);
                    } else {
                        new Thread(() -> { // schedule asynchronous reboot
                            try {
                                api.setDebug(enable);
                            } catch (ShellyApiException e) {
                                // maybe the device restarts before returning the http response
                            }
                        }).start();

                        message = getMessage("action.debug-confirm", enable ? "enabled" : "disabled");
                        refreshTimer = 3;
                    }
                    break;
                case ACTION_RESSTA:
                    if (!"yes".equalsIgnoreCase(update)) {
                        message = getMessage("action.resetsta-info");
                        actionUrl = buildActionUrl(uid, action);
                    } else {
                        try {
                            api.resetStaCache();
                            message = getMessage("action.resetsta-confirm");
                        } catch (ShellyApiException e) {
                            message = getMessageP("action.resetsta-failed", e.toString());
                        }
                        refreshTimer = 10;
                    }
                    break;
                case ACTION_ENWIFIREC:
                case ACTION_DISWIFIREC:
                    enable = ACTION_ENWIFIREC.equalsIgnoreCase(action);
                    if (!"yes".equalsIgnoreCase(update)) {
                        message = getMessage(enable ? "action.setwifirec-enable" : "action.setwifirec-disable");
                        actionUrl = buildActionUrl(uid, action);
                    } else {
                        try {
                            api.setWiFiRecovery(enable);
                            message = getMessage("action.setwifirec-confirm", enable ? "enabled" : "disabled");
                        } catch (ShellyApiException e) {
                            message = getMessage("action.setwifirec-failed", e.toString());
                        }
                        refreshTimer = 3;
                    }
                    break;

                case ACTION_ENAPROAMING:
                case ACTION_DISAPROAMING:
                    enable = ACTION_ENAPROAMING.equalsIgnoreCase(action);
                    if (!"yes".equalsIgnoreCase(update)) {
                        message = getMessage(enable ? "action.aproaming-enable" : "action.aproaming-disable");
                        actionUrl = buildActionUrl(uid, action);
                    } else {
                        try {
                            api.setApRoaming(enable);
                            message = getMessage("action.aproaming-confirm", enable ? "enabled" : "disabled");
                        } catch (ShellyApiException e) {
                            message = getMessage("action.aproaming-failed", e.toString());
                        }
                        refreshTimer = 3;
                    }
                    break;

                case ACTION_GETDEB:
                case ACTION_GETDEB1:
                    try {
                        message = api.getDebugLog(ACTION_GETDEB.equalsIgnoreCase(action) ? "log" : "log1");
                        message = message.replaceAll("[\r]", "").replaceAll("[\r\n]", "<br>");
                    } catch (ShellyApiException e) {
                        message = getMessage("action.getdebug-failed", e.toString());
                    }
                    break;
                case ACTION_NONE:
                    break;
                default:
                    logger.warn("{}: {}", LOG_PREFIX, getMessage("action.unknown", action));
            }

            properties.put(ATTRIBUTE_ACTION, getString(actions.get(action))); // get description for command
            properties.put(ATTRIBUTE_ACTION_BUTTON, actionButtonLabel);
            properties.put(ATTRIBUTE_ACTION_URL, actionUrl);
            message = fillAttributes(message, properties);
            properties.put(ATTRIBUTE_MESSAGE, message);
            properties.put(ATTRIBUTE_REFRESH, String.valueOf(refreshTimer));
            html += loadHTML(ACTION_HTML, properties);

            th.requestUpdates(1, refreshTimer > 0); // trigger background update
        }

        properties.clear();
        html += loadHTML(FOOTER_HTML, properties);
        return new ShellyMgrResponse(html, HttpStatus.OK_200);
    }

    public static Map<String, String> getActions(ShellyDeviceProfile profile) {
        Map<String, String> list = new LinkedHashMap<>();
        list.put(ACTION_RES_STATS, "Reset Statistics");
        list.put(ACTION_RESTART, "Reboot Device");
        list.put(ACTION_PROTECT, "Protect Device");

        if ((profile.settings.coiot != null) && (profile.settings.coiot.peer != null) && !profile.isMotion) {
            boolean mcast = profile.settings.coiot.peer.isEmpty()
                    || SHELLY_COIOT_MCAST.equalsIgnoreCase(profile.settings.coiot.peer);
            list.put(mcast ? ACTION_SETCOIOT_PEER : ACTION_SETCOIOT_MCAST,
                    mcast ? "Set CoIoT Peer Mode" : "Set CoIoT Multicast Mode");
        }
        if (profile.isSensor && !profile.isMotion && (profile.settings.wifiSta != null)
                && profile.settings.wifiSta.enabled) {
            // FW 1.10+: Reset STA list, force WiFi rescan and connect to stringest AP
            list.put(ACTION_RESSTA, "Reconnect WiFi");
        }
        if (profile.settings.apRoaming != null) {
            list.put(!profile.settings.apRoaming.enabled ? ACTION_ENAPROAMING : ACTION_DISAPROAMING,
                    !profile.settings.apRoaming.enabled ? "Enable WiFi Roaming" : "Disable WiFi Roaming");
        }
        if (profile.settings.wifiRecoveryReboot != null) {
            list.put(!profile.settings.wifiRecoveryReboot ? ACTION_ENWIFIREC : ACTION_DISWIFIREC,
                    !profile.settings.wifiRecoveryReboot ? "Enable WiFi Recovery" : "Disable WiFi Recovery");
        }

        boolean set = profile.settings.cloud != null && profile.settings.cloud.enabled;
        list.put(set ? ACTION_DISCLOUD : ACTION_ENCLOUD, set ? "Disable Cloud" : "Enable Cloud");

        list.put(ACTION_RESET, "-Factory Reset");
        if (profile.extFeatures) {
            list.put(ACTION_OTACHECK, "Check for Update");
            boolean debug_enable = getBool(profile.settings.debugEnable);
            list.put(!debug_enable ? ACTION_ENDEBUG : ACTION_DISDEBUG,
                    !debug_enable ? "Enable Debug" : "Disable Debug");
            if (debug_enable) {
                list.put(ACTION_GETDEB, "Get Debug log");
                list.put(ACTION_GETDEB1, "Get Debug log1");
            }
        }

        return list;
    }

    private String buildActionUrl(String uid, String action) {
        return SHELLY_MGR_ACTION_URI + "?" + URLPARM_ACTION + "=" + action + "&" + URLPARM_UID + "=" + urlEncode(uid)
                + "&" + URLPARM_UPDATE + "=yes";
    }

    private void setRestarted(ShellyManagerInterface th, String uid) {
        th.setThingOffline(ThingStatusDetail.GONE, "offline.status-error-restarted");
        scheduleUpdate(th, uid + "_upgrade", 25); // wait 25s before refresh
    }
}
