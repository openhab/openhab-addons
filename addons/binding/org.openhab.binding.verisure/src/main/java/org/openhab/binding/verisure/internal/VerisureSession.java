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
package org.openhab.binding.verisure.internal;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openhab.binding.verisure.internal.model.VerisureAlarmJSON;
import org.openhab.binding.verisure.internal.model.VerisureBroadbandConnectionJSON;
import org.openhab.binding.verisure.internal.model.VerisureClimateBaseJSON;
import org.openhab.binding.verisure.internal.model.VerisureDoorWindowJSON;
import org.openhab.binding.verisure.internal.model.VerisureSmartLockJSON;
import org.openhab.binding.verisure.internal.model.VerisureSmartPlugJSON;
import org.openhab.binding.verisure.internal.model.VerisureThingJSON;
import org.openhab.binding.verisure.internal.model.VerisureUserPresenceJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class performs the communication with Verisure My Pages.
 *
 * @author Jarle Hjortland - Initial contribution
 * @author Jan Gustafsson - Re-design and support for several sites
 *
 */
@NonNullByDefault
public class VerisureSession {
    private final class VerisureInstallation {
        private @Nullable String installationName;
        private int installationInstance;
        private @Nullable BigDecimal installationId;

        public VerisureInstallation(@Nullable String installationName) {
            this.installationName = installationName;
        }

        public @Nullable BigDecimal getInstallationId() {
            return installationId;
        }

        public int getInstallationInstance() {
            return installationInstance;
        }

        public @Nullable String getInstallationName() {
            return installationName;
        }

        public void setInstallationId(BigDecimal installationId) {
            this.installationId = installationId;
        }

        public void setInstallationInstance(int installationInstance) {
            this.installationInstance = installationInstance;
        }
    }

    private final HashMap<String, org.openhab.binding.verisure.internal.model.VerisureThingJSON> verisureThings = new HashMap<String, org.openhab.binding.verisure.internal.model.VerisureThingJSON>();
    private final Logger logger = LoggerFactory.getLogger(VerisureSession.class);
    private final Gson gson = new GsonBuilder().create();
    private final List<DeviceStatusListener> deviceStatusListeners = new CopyOnWriteArrayList<>();
    private final Hashtable<String, @Nullable VerisureInstallation> verisureInstallations = new Hashtable<String, @Nullable VerisureInstallation>();

    private boolean areWeLoggedOut = true;
    private @Nullable String authstring;
    private @Nullable String csrf;
    private @Nullable BigDecimal pinCode;
    private @Nullable BigDecimal numberOfInstallations;
    private HttpClient httpClient;

    public VerisureSession(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void initialize(@Nullable String authstring, @Nullable BigDecimal pinCode,
            @Nullable BigDecimal numberOfInstallations) {
        logger.debug("VerisureSession:initialize");
        if (authstring != null) {
            this.authstring = authstring.substring(0);
            this.pinCode = pinCode;
            this.numberOfInstallations = numberOfInstallations;
            // Try to login to Verisure
            if (logIn()) {
                getInstallations();
            } else {
                logger.warn("Failed to login to Verisure!");
            }
        }
    }

    public boolean refresh() {
        if (!areWeLoggedOut && areWeLoggedIn()) {
            updateStatus();
            return true;
        } else {
            if (logIn()) {
                updateStatus();
                return true;
            } else {
                areWeLoggedOut = true;
                return false;
            }
        }
    }

    public boolean sendCommand(String installationName, String url, String data) {
        logger.debug("Sending command with URL {} and data {} for installation {}", url, data, installationName);
        VerisureInstallation verisureInstallation = verisureInstallations.get(installationName);
        if (verisureInstallation != null) {
            int instInst = verisureInstallation.getInstallationInstance();
            configureInstallationInstance(instInst);
            sendHTTPpost(url, data);
            return true;
        }
        return false;
    }

    public boolean unregisterDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        logger.debug("unregisterDeviceStatusListener for listener {}", deviceStatusListener);
        return deviceStatusListeners.remove(deviceStatusListener);
    }

    public boolean registerDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        logger.debug("registerDeviceStatusListener for listener {}", deviceStatusListener);
        return deviceStatusListeners.add(deviceStatusListener);
    }

    public void dispose() {
    }

    public @Nullable VerisureThingJSON getVerisureThing(String key) {
        return verisureThings.get(key);
    }

    public HashMap<String, org.openhab.binding.verisure.internal.model.VerisureThingJSON> getVerisureThings() {
        return verisureThings;
    }

    public @Nullable String getCsrf() {
        return csrf;
    }

    public @Nullable BigDecimal getPinCode() {
        return pinCode;
    }

    private boolean areWeLoggedIn() {
        logger.debug("areWeLoggedIn() - Checking if we are logged in");
        String url = START_SUF;
        try {
            ContentResponse response = httpClient.newRequest(url).method(HttpMethod.HEAD).send();
            logger.debug("HTTP HEAD response: " + response.getContentAsString());
            switch (response.getStatus()) {
                case 200:
                    // Redirection
                    logger.debug("Status code 200. Probably logged in");
                    return getHtmlPageType().contains("start-page");
                case 302:
                    // Redirection
                    logger.debug("Status code 302. Redirected. Probably not logged in");
                    return false;
                case 404:
                    // not found
                    logger.debug("Status code 404. Probably logged on too");
                    return getHtmlPageType().contains("start-page");
                default:
                    logger.info("Status code {} body {}", response.getStatus(), response.getContentAsString());
                    break;
            }
        } catch (ExecutionException e) {
            logger.warn("ExecutionException: {}", e);
        } catch (InterruptedException e) {
            logger.warn("InterruptedException: {}", e);
        } catch (TimeoutException e) {
            logger.warn("TimeoutException: {}", e);
        }
        return false;
    }

    private @Nullable <T> T callJSONRest(String url, Class<T> jsonClass) {
        T result = null;
        logger.debug("HTTP GET: " + BASEURL + url);
        try {
            ContentResponse httpResult = httpClient.GET(BASEURL + url + "?_=" + System.currentTimeMillis());
            logger.debug("HTTP Response ({}) Body:{}", httpResult.getStatus(),
                    httpResult.getContentAsString().replaceAll("\n+", "\n"));
            if (httpResult.getStatus() == HttpStatus.OK_200) {
                result = gson.fromJson(httpResult.getContentAsString(), jsonClass);
            }
            return result;
        } catch (ExecutionException e) {
            logger.warn("Caught ExecutionException {} for URL string {}", e, url);
        } catch (InterruptedException e) {
            logger.warn("Caught InterruptedException {} for URL string {}", e, url);
        } catch (TimeoutException e) {
            logger.warn("Caught TimeoutException {} for URL string {}", e, url);
        }
        return null;
    }

    private @Nullable String configureInstallationInstance(int installationInstance) {
        logger.debug("Attempting to configure installation instance");
        try {
            String url = START_SUF + "?inst=" + installationInstance;
            logger.debug("Start URL: " + url);
            ContentResponse resp = httpClient.GET(url);
            String source = resp.getContentAsString();
            csrf = getCsrfToken(source);
            logger.trace(source);
            logger.debug("Got CSRF: {}", csrf);
            return source;
        } catch (ExecutionException e) {
            logger.warn("Caught ExecutionException {}", e);
        } catch (InterruptedException e) {
            logger.warn("Caught InterruptedException {}", e);
        } catch (TimeoutException e) {
            logger.warn("Caught TimeoutException {}", e);
        }
        return null;
    }

    private @Nullable String getCsrfToken(String htmlText) {
        Document htmlDocument = Jsoup.parse(htmlText);
        Element nameInput = htmlDocument.select("input[name=_csrf]").first();
        return nameInput.attr("value");
    }

    private String getHtmlPageType() {
        String url = START_SUF;
        try {
            ContentResponse response = httpClient.GET(url + "?_=" + System.currentTimeMillis());
            logger.trace("HTTP Response ({}) Body:{}", response.getStatus(),
                    response.getContentAsString().replaceAll("\n+", "\n"));
            String htmlText = response.getContentAsString();
            Document htmlDocument = Jsoup.parse(htmlText);
            Element htmlType = htmlDocument.select("html").first();
            String pageType = htmlType.attr("class");
            logger.debug("Page type: {}", pageType);
            return pageType;
        } catch (ExecutionException e) {
            logger.warn("ExecutionException: {}", e);
        } catch (InterruptedException e) {
            logger.warn("InterruptedException: {}", e);
        } catch (TimeoutException e) {
            logger.warn("TimeoutException: {}", e);
        }
        return "";
    }

    private void getInstallations() {
        logger.debug("VerisureSession:handleInstallations");
        if (numberOfInstallations != null) {
            int ni = numberOfInstallations.intValue();
            for (int i = 1; i < ni + 1; i++) {
                String html = configureInstallationInstance(i);
                if (html != null) {
                    handleInstallation(html, i);
                }
            }
        }
    }

    private void handleInstallation(String htmlText, int installationInstance) {
        // Use Jsoup to parse installation names from html
        Document htmlDocument = Jsoup.parse(htmlText);
        Element div = htmlDocument.select("span.global-navigation-item-no-shrink--text").first();
        String alarmInstallationName = div.text();
        VerisureInstallation verisureInstallation = verisureInstallations.get(alarmInstallationName);
        if (verisureInstallation == null) {
            verisureInstallation = new VerisureInstallation(alarmInstallationName);
            verisureInstallation.setInstallationInstance(installationInstance);
            verisureInstallation.setInstallationId(new BigDecimal(installationInstance));
            verisureInstallations.put(alarmInstallationName, verisureInstallation);
        } else {
            verisureInstallation.setInstallationInstance(installationInstance);
        }
        // Handle second i nstallation if it exists
        Element inst = htmlDocument.select("a.installation-select-link").first();
        if (inst != null) {
            String secondAlarmInstallationName = inst.text();
            Element instId = htmlDocument.select("input.giid").first();
            String installationId = instId.attr("value");
            verisureInstallation = verisureInstallations.get(secondAlarmInstallationName);
            if (verisureInstallation != null) {
                if (verisureInstallations.get(secondAlarmInstallationName) == null) {
                    verisureInstallation = new VerisureInstallation(secondAlarmInstallationName);
                    verisureInstallations.put(secondAlarmInstallationName, verisureInstallation);
                }
                try {
                    verisureInstallation.setInstallationId(new BigDecimal(Integer.parseInt(installationId)));
                } catch (NumberFormatException e) {
                    logger.warn("Erroneous installation id {}.", installationId);
                }
            }
        }
    }

    private synchronized boolean logIn() {
        logger.debug("Attempting to log in to mypages.verisure.com");
        String url = LOGON_SUF;
        logger.debug("Login URL: {}", url);
        String source = sendHTTPpost(url, authstring);
        if (source == null) {
            logger.debug("Failed to login");
            return false;
        } else {
            logger.debug("Login result: {}" + source);
            return true;
        }
    }

    private void notifyListeners(VerisureThingJSON thing) {
        for (DeviceStatusListener listener : deviceStatusListeners) {
            listener.onDeviceStateChanged(thing);
        }
    }

    @Nullable
    private String sendHTTPpost(String urlString, @Nullable String data) {
        if (data != null) {
            try {
                logger.debug("sendHTTPpost URL: {} Data:{}", urlString, data);
                org.eclipse.jetty.client.api.Request request = httpClient.newRequest(urlString).method(HttpMethod.POST);
                request.header("x-csrf-token", csrf).header("Accept", "application/json");
                request.content(new BytesContentProvider(data.getBytes("UTF-8")),
                        "application/x-www-form-urlencoded; charset=UTF-8");
                logger.debug("HTTP POST Request {}.", request.toString());
                ContentResponse response = request.send();
                String content = response.getContentAsString();
                String contentUTF8 = new String(content.getBytes("UTF-8"), "ISO-8859-1");
                logger.debug("HTTP Response ({}) Body:{}", response.getStatus(), contentUTF8);
                return contentUTF8;
            } catch (ExecutionException e) {
                logger.warn("Caught ExecutionException {}", e);
            } catch (UnsupportedEncodingException e) {
                logger.warn("Caught UnsupportedEncodingException {}", e);
            } catch (InterruptedException e) {
                logger.warn("Caught InterruptedException {}", e);
            } catch (TimeoutException e) {
                logger.warn("Caught TimeoutException {}", e);
            }
        }
        return null;
    }

    private void updateStatus() {
        logger.debug("VerisureSession:updateStatus");
        if (numberOfInstallations != null) {
            int ni = numberOfInstallations.intValue();
            for (int i = 1; i < ni + 1; i++) {
                configureInstallationInstance(i);
                VerisureInstallation vInst = null;
                for (Enumeration<@Nullable VerisureInstallation> num = verisureInstallations.elements(); num
                        .hasMoreElements();) {
                    vInst = num.nextElement();
                    if (vInst != null) {
                        if (vInst.getInstallationInstance() == i) {
                            break;
                        }
                    }
                }
                if (vInst != null) {
                    updateVerisureThings(ALARMSTATUS_PATH, VerisureAlarmJSON[].class, vInst);
                    updateVerisureThings(CLIMATEDEVICE_PATH, VerisureClimateBaseJSON[].class, vInst);
                    updateVerisureThings(DOORWINDOW_PATH, VerisureDoorWindowJSON[].class, vInst);
                    updateVerisureThings(USERTRACKING_PATH, VerisureUserPresenceJSON[].class, vInst);
                    updateVerisureThings(SMARTPLUG_PATH, VerisureSmartPlugJSON[].class, vInst);
                    updateVerisureBroadbandStatus(ETHERNETSTATUS_PATH, VerisureBroadbandConnectionJSON.class, vInst);
                }
            }
        }
    }

    private synchronized void updateVerisureBroadbandStatus(String urlString,
            Class<? extends VerisureThingJSON> jsonClass, VerisureInstallation verisureInstallation) {
        VerisureThingJSON thing = callJSONRest(urlString, jsonClass);
        logger.debug("REST Response ({})", thing);
        if (thing != null) {
            int instInst = verisureInstallation.getInstallationInstance();
            thing.setId(Integer.toString(instInst));
            VerisureThingJSON oldObj = verisureThings.get(thing.getId());
            if (oldObj == null || !oldObj.equals(thing)) {
                thing.setSiteId(verisureInstallation.getInstallationId());
                thing.setSiteName(verisureInstallation.getInstallationName());
                String id = thing.getId();
                if (id != null) {
                    verisureThings.put(id, thing);
                    notifyListeners(thing);
                }
            }
        }
    }

    private synchronized void updateVerisureThings(String urlString, Class<? extends VerisureThingJSON[]> jsonClass,
            @Nullable VerisureInstallation inst) {
        if (inst != null) {
            VerisureThingJSON[] things = callJSONRest(urlString, jsonClass);
            logger.debug("REST Response ({})", (Object[]) things);
            if (things != null) {
                for (VerisureThingJSON thing : things) {
                    int instInst = inst.getInstallationInstance();
                    if (thing instanceof VerisureUserPresenceJSON) {
                        thing.setId(Integer.toString(instInst));
                    } else if (thing instanceof VerisureAlarmJSON) {
                        String type = ((VerisureAlarmJSON) thing).getType();
                        if ("ARM_STATE".equals(type)) {
                            thing.setId(Integer.toString(instInst));
                        } else if ("DOOR_LOCK".equals(type)) {
                            // Then we know it is a SmartLock, lets get some more info on SmartLock Status
                            thing = updateSmartLockThing((VerisureAlarmJSON) thing, type);
                        } else {
                            logger.warn("Unknown alarm/lock type {}.", type);
                        }
                    } else {
                        String id = thing.getId();
                        if (id != null) {
                            thing.setId(id.replaceAll("[^a-zA-Z0-9_]", "_"));
                        }
                    }
                    VerisureThingJSON oldObj = verisureThings.get(thing.getId());
                    if (oldObj == null || !oldObj.equals(thing)) {
                        thing.setSiteId(inst.getInstallationId());
                        thing.setSiteName(inst.getInstallationName());
                        String id = thing.getId();
                        if (id != null) {
                            verisureThings.put(id, thing);
                            notifyListeners(thing);
                        }
                    }
                }
            }
        }
    }

    private VerisureSmartLockJSON updateSmartLockThing(VerisureAlarmJSON thing, @Nullable String type) {
        VerisureSmartLockJSON smartLockThing = callJSONRest(SMARTLOCK_PATH + thing.getId(),
                VerisureSmartLockJSON.class);
        logger.debug("REST Response ({})", smartLockThing);
        if (smartLockThing == null) {
            // Fix if doorlock query gives empty JSON
            smartLockThing = new VerisureSmartLockJSON();
        }
        String date = thing.getDate();
        if (date != null) {
            smartLockThing.setDate(date);
        }
        String notAllowedReason = thing.getNotAllowedReason();
        if (notAllowedReason != null) {
            smartLockThing.setNotAllowedReason(notAllowedReason);
        }
        Boolean changeAllowed = thing.getChangeAllowed();
        if (changeAllowed != null) {
            smartLockThing.setChangeAllowed(changeAllowed);
        }
        String label = thing.getLabel();
        if (label != null) {
            smartLockThing.setLabel(label);
        }
        if (type != null) {
            smartLockThing.setType(type);
        }
        String name = thing.getName();
        if (name != null) {
            smartLockThing.setName(name);
        }
        String location = thing.getLocation();
        if (location != null) {
            smartLockThing.setLocation(location);
        }
        String status = thing.getStatus();
        if (status != null) {
            smartLockThing.setStatus(status);
        }
        String id = smartLockThing.getId();
        if (id != null) {
            smartLockThing.setId(id.replaceAll("[^a-zA-Z0-9_]", "_"));
        }
        return smartLockThing;
    }
}
