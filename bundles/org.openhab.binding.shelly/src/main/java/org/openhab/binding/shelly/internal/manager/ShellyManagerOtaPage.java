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
package org.openhab.binding.shelly.internal.manager;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.SHELLY_API_TIMEOUT_MS;
import static org.openhab.binding.shelly.internal.manager.ShellyManagerConstants.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.shelly.internal.ShellyHandlerFactory;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsUpdate;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyManagerInterface;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.thing.ThingStatusDetail;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ShellyManagerOtaPage} implements the Shelly Manager's download proxy for images (load them from bundle)
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyManagerOtaPage extends ShellyManagerPage {
    protected final Logger logger = LoggerFactory.getLogger(ShellyManagerOtaPage.class);

    public ShellyManagerOtaPage(ConfigurationAdmin configurationAdmin, ShellyTranslationProvider translationProvider,
            HttpClient httpClient, String localIp, int localPort, ShellyHandlerFactory handlerFactory) {
        super(configurationAdmin, translationProvider, httpClient, localIp, localPort, handlerFactory);
    }

    @Override
    public ShellyMgrResponse generateContent(String path, Map<String, String[]> parameters) throws ShellyApiException {
        if (path.contains(SHELLY_MGR_OTA_URI)) {
            return loadFirmware(path, parameters);
        } else {
            return generatePage(path, parameters);
        }
    }

    public ShellyMgrResponse generatePage(String path, Map<String, String[]> parameters) throws ShellyApiException {
        String uid = getUrlParm(parameters, URLPARM_UID);
        String version = getUrlParm(parameters, URLPARM_VERSION);
        String update = getUrlParm(parameters, URLPARM_UPDATE);
        String connection = getUrlParm(parameters, URLPARM_CONNECTION);
        String url = getUrlParm(parameters, URLPARM_URL);
        if (uid.isEmpty() || (version.isEmpty() && connection.isEmpty()) || !getThingHandlers().containsKey(uid)) {
            return new ShellyMgrResponse("Invalid URL parameters: " + parameters, HttpStatus.BAD_REQUEST_400);
        }

        Map<String, String> properties = new HashMap<>();
        String html = loadHTML(HEADER_HTML, properties);
        ShellyManagerInterface th = getThingHandlers().get(uid);
        if (th != null) {
            properties = fillProperties(new HashMap<>(), uid, th);
            ShellyThingConfiguration config = getThingConfig(th, properties);
            ShellyDeviceProfile profile = th.getProfile();
            String deviceType = getDeviceType(properties);

            String uri = !url.isEmpty() && connection.equals(CONNECTION_TYPE_CUSTOM) ? url
                    : getFirmwareUrl(config.deviceIp, deviceType, profile.device.mode, version,
                            connection.equals(CONNECTION_TYPE_LOCAL));
            if (connection.equalsIgnoreCase(CONNECTION_TYPE_INTERNET)) {
                // If target
                // - contains "update=xx" then use -> ?update=true for release and ?beta=true for beta
                // - otherwise qualify full url with ?url=xxxx
                if (uri.contains("update=") || uri.contains("beta=")) {
                    url = uri;
                } else {
                    url = URLPARM_URL + "=" + uri;
                }
            } else if (connection.equalsIgnoreCase(CONNECTION_TYPE_LOCAL)) {
                // redirect to local server -> http://<oh-ip>:<oh-port>/shelly/manager/ota?deviceType=xxx&version=xxx
                String modeParm = !profile.device.mode.isEmpty() ? "&" + URLPARM_DEVMODE + "=" + profile.device.mode
                        : "";
                url = URLPARM_URL + "=http://" + localIp + ":" + localPort + SHELLY_MGR_OTA_URI + urlEncode(
                        "?" + URLPARM_DEVTYPE + "=" + deviceType + modeParm + "&" + URLPARM_VERSION + "=" + version);
            } else if (connection.equalsIgnoreCase(CONNECTION_TYPE_CUSTOM)) {
                // else custom -> don't modify url
                uri = url;
                url = URLPARM_URL + "=" + uri;
            }
            String updateUrl = url;

            properties.put(ATTRIBUTE_VERSION, version);
            properties.put(ATTRIBUTE_FW_URL, uri);
            properties.put(ATTRIBUTE_UPDATE_URL, "http://" + getDeviceIp(properties) + "/ota?" + updateUrl);
            properties.put(URLPARM_CONNECTION, connection);

            if ("yes".equalsIgnoreCase(update)) {
                // do the update
                th.setThingOffline(ThingStatusDetail.FIRMWARE_UPDATING, "offline.status-error-fwupgrade");
                html += loadHTML(FWUPDATE2_HTML, properties);

                new Thread(() -> { // schedule asynchronous reboot
                    try {
                        ShellyApiInterface api = th.getApi();
                        ShellySettingsUpdate result = api.firmwareUpdate(updateUrl);
                        String status = getString(result.status);
                        logger.info("{}: {}", th.getThingName(), getMessage("fwupdate.initiated", status));

                        // Shelly Motion needs almost 2min for upgrade
                        scheduleUpdate(th, uid + "_upgrade", profile.isMotion ? 110 : 30);
                    } catch (ShellyApiException e) {
                        // maybe the device restarts before returning the http response
                        logger.warn("{}: {}", th.getThingName(), getMessage("fwupdate.initiated", e.toString()));
                    }
                }).start();
            } else {
                String message = getMessageP("fwupdate.confirm", MCINFO);
                properties.put(ATTRIBUTE_MESSAGE, message);
                html += loadHTML(FWUPDATE1_HTML, properties);
            }
        }

        html += loadHTML(FOOTER_HTML, properties);
        return new ShellyMgrResponse(html, HttpStatus.OK_200);
    }

    protected ShellyMgrResponse loadFirmware(String path, Map<String, String[]> parameters) throws ShellyApiException {
        String deviceType = getUrlParm(parameters, URLPARM_DEVTYPE);
        String deviceMode = getUrlParm(parameters, URLPARM_DEVMODE);
        String version = getUrlParm(parameters, URLPARM_VERSION);
        String url = getUrlParm(parameters, URLPARM_URL);
        logger.info("ShellyManager: {}", getMessage("fwupdate.info", deviceType, version, url));

        String failure = getMessage("fwupdate.notfound", deviceType, version, url);
        try {
            if (url.isEmpty()) {
                url = getFirmwareUrl("", deviceType, deviceMode, version, true);
                if (url.isEmpty()) {
                    logger.warn("ShellyManager: {}", failure);
                    return new ShellyMgrResponse(failure, HttpStatus.BAD_REQUEST_400);
                }
            }

            logger.debug("ShellyManager: Loading firmware from {}", url);
            // BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
            // byte[] buf = new byte[in.available()];
            // in.read(buf);
            Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(SHELLY_API_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);
            ContentResponse contentResponse = request.send();
            HttpFields fields = contentResponse.getHeaders();
            Map<String, String> headers = new TreeMap<>();
            String etag = getString(fields.get("ETag"));
            String ranges = getString(fields.get("accept-ranges"));
            String modified = getString(fields.get("Last-Modified"));
            headers.put("ETag", etag);
            headers.put("accept-ranges", ranges);
            headers.put("Last-Modified", modified);
            byte[] data = contentResponse.getContent();
            logger.info("ShellyManager: {}", getMessage("fwupdate.success", data.length, etag, modified));
            return new ShellyMgrResponse(data, HttpStatus.OK_200, contentResponse.getMediaType(), headers);
        } catch (ExecutionException | TimeoutException | InterruptedException | RuntimeException e) {
            logger.info("ShellyManager: {}", failure, e);
            return new ShellyMgrResponse(failure, HttpStatus.BAD_REQUEST_400);

        }
    }

    protected String getFirmwareUrl(String deviceIp, String deviceType, String mode, String version, boolean local)
            throws ShellyApiException {
        switch (version) {
            case FWPROD:
            case FWBETA:
                boolean prod = version.equals(FWPROD);
                if (!local) {
                    // run regular device update
                    return prod ? "update=true" : "beta=true";
                } else {
                    // convert prod/beta to full url
                    FwRepoEntry fw = getFirmwareRepoEntry(deviceType, mode);
                    String url = getString(prod ? fw.url : fw.betaUrl);
                    logger.debug("ShellyManager: Map {} release to url {}, version {}", url, prod ? fw.url : fw.betaUrl,
                            prod ? fw.version : fw.betaVer);
                    return url;
                }
            default: // Update from firmware archive
                FwArchList list = getFirmwareArchiveList(deviceType);
                ArrayList<FwArchEntry> versions = list.versions;
                if (versions != null) {
                    for (FwArchEntry e : versions) {
                        String url = FWREPO_ARCFILE_URL + version + "/" + getString(e.file);
                        if (getString(e.version).equalsIgnoreCase(version)) {
                            return url;
                        }
                    }
                }
        }
        return "";
    }
}
