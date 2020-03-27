/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openhab.binding.verisure.internal.model.VerisureAlarms;
import org.openhab.binding.verisure.internal.model.VerisureBroadbandConnections;
import org.openhab.binding.verisure.internal.model.VerisureClimates;
import org.openhab.binding.verisure.internal.model.VerisureDoorWindows;
import org.openhab.binding.verisure.internal.model.VerisureInstallations;
import org.openhab.binding.verisure.internal.model.VerisureInstallations.Owainstallation;
import org.openhab.binding.verisure.internal.model.VerisureMiceDetection;
import org.openhab.binding.verisure.internal.model.VerisureSmartLock;
import org.openhab.binding.verisure.internal.model.VerisureSmartLocks;
import org.openhab.binding.verisure.internal.model.VerisureSmartPlugs;
import org.openhab.binding.verisure.internal.model.VerisureThing;
import org.openhab.binding.verisure.internal.model.VerisureUserPresences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This class performs the communication with Verisure My Pages.
 *
 * @author Jarle Hjortland - Initial contribution
 * @author Jan Gustafsson - Re-design and support for several sites and update to new Verisure API
 * @param <T>
 *
 */
@NonNullByDefault
public class VerisureSession {

    private final HashMap<String, VerisureThing> verisureThings = new HashMap<String, VerisureThing>();
    private final Logger logger = LoggerFactory.getLogger(VerisureSession.class);
    private final Gson gson = new Gson();
    private final List<DeviceStatusListener<VerisureThing>> deviceStatusListeners = new CopyOnWriteArrayList<>();
    private final HashMap<BigDecimal, VerisureInstallation> verisureInstallations = new HashMap<>();
    private static final List<String> APISERVERLIST = Arrays.asList("https://m-api01.verisure.com",
            "https://m-api02.verisure.com");
    private int apiServerInUseIndex = 0;
    private String apiServerInUse = APISERVERLIST.get(apiServerInUseIndex);
    private String authstring = "";
    private @Nullable String csrf;
    private @Nullable String pinCode;
    private HttpClient httpClient;
    private @Nullable String userName = "";
    private String passwordName = "vid";
    private @Nullable String password = "";

    public VerisureSession(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public boolean initialize(@Nullable String authstring, @Nullable String pinCode, @Nullable String userName) {
        logger.debug("VerisureSession:initialize");
        if (authstring != null) {
            this.authstring = authstring.substring(0);
            this.pinCode = pinCode;
            this.userName = userName;
            // Try to login to Verisure
            if (logIn()) {
                return getInstallations();
            } else {
                logger.warn("Failed to login to Verisure!");
                return false;
            }
        }
        return false;
    }

    public boolean refresh() {
        logger.debug("VerisureSession:refresh");
        try {
            if (logIn()) {
                updateStatus();
                return true;
            } else {
                return false;
            }
        } catch (HttpResponseException e) {
            logger.warn("Caught an HttpResponseException {}", e.getMessage(), e);
            return false;
        }
    }

    public int sendCommand(String url, String data, BigDecimal installationId) {
        logger.debug("Sending command with URL {} and data {}", url, data);
        configureInstallationInstance(installationId);
        int httpResultCode = setSessionCookieAuthLogin();
        if (httpResultCode == HttpStatus.OK_200) {
            return postVerisureAPI(url, data);
        } else {
            return httpResultCode;
        }
    }

    public boolean unregisterDeviceStatusListener(DeviceStatusListener<? extends VerisureThing> deviceStatusListener) {
        logger.debug("unregisterDeviceStatusListener for listener {}", deviceStatusListener);
        return deviceStatusListeners.remove(deviceStatusListener);
    }

    @SuppressWarnings("unchecked")
    public boolean registerDeviceStatusListener(DeviceStatusListener<? extends VerisureThing> deviceStatusListener) {
        logger.debug("registerDeviceStatusListener for listener {}", deviceStatusListener);
        return deviceStatusListeners.add((DeviceStatusListener<VerisureThing>) deviceStatusListener);
    }

    @SuppressWarnings({ "unchecked", "null" })
    public <T extends VerisureThing> @Nullable T getVerisureThing(String deviceId, Class<T> thingType) {
        VerisureThing thing = verisureThings.get(deviceId);
        if (thing != null && thingType.isInstance(thing)) {
            return (T) thing;
        }
        return null;
    }

    public HashMap<String, VerisureThing> getVerisureThings() {
        return verisureThings;
    }

    public @Nullable String getCsrf() {
        return csrf;
    }

    public @Nullable String getPinCode() {
        return pinCode;
    }

    public String getApiServerInUse() {
        return apiServerInUse;
    }

    public void setApiServerInUse(String apiServerInUse) {
        this.apiServerInUse = apiServerInUse;
    }

    public String getNextApiServer() {
        apiServerInUseIndex++;
        if (apiServerInUseIndex > (APISERVERLIST.size() - 1)) {
            apiServerInUseIndex = 0;
        }
        return APISERVERLIST.get(apiServerInUseIndex);
    }

    public void configureInstallationInstance(BigDecimal installationId) {
        logger.debug("Attempting to fetch CSRF and configure installation instance");
        try {
            csrf = getCsrfToken(installationId);
            logger.debug("Got CSRF: {}", csrf);
            // Set installation
            String url = SET_INSTALLATION + installationId.toString();
            logger.debug("Set installation URL: {}", url);
            httpClient.GET(url);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.warn("Caught an Exception {}", e.getMessage(), e);
        }
    }

    public @Nullable String getCsrfToken(BigDecimal installationId) {
        String html = null;
        String url = SETTINGS + installationId.toString();
        logger.debug("Settings URL: {}", url);

        try {
            ContentResponse resp = httpClient.GET(url);
            html = resp.getContentAsString();
            logger.trace("{} html: {}", url, html);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.warn("Caught an Exception {}", e.getMessage(), e);
            return null;
        }

        Document htmlDocument = Jsoup.parse(html);
        Element nameInput = htmlDocument.select("input[name=_csrf]").first();
        return nameInput.attr("value");
    }

    public @Nullable String getPinCode(BigDecimal installationId) {
        return verisureInstallations.get(installationId).getPinCode();
    }

    private void setPasswordFromCookie() {
        CookieStore c = httpClient.getCookieStore();
        List<HttpCookie> cookies = c.get(URI.create("http://verisure.com"));
        cookies.forEach(cookie -> {
            logger.debug("Response Cookie: {}", cookie);
            if (cookie.getName().equals(passwordName)) {
                password = cookie.getValue();
                logger.debug("Fetching vid {} from cookie", password);
            }
        });
    }

    private void logTraceWithPattern(int responseStatus, String content) {
        if (logger.isTraceEnabled()) {
            String pattern = "(?m)^\\s*\\r?\\n|\\r?\\n\\s*(?!.*\\r?\\n)";
            String replacement = "";
            logger.trace("HTTP Response ({}) Body:{}", responseStatus, content.replaceAll(pattern, replacement));
        }
    }

    private boolean areWeLoggedIn() {
        logger.debug("areWeLoggedIn() - Checking if we are logged in");
        String url = STATUS;
        try {
            logger.debug("Check for login status, url: {}", url);
            ContentResponse response = httpClient.newRequest(url).method(HttpMethod.GET).send();
            String content = response.getContentAsString();
            String error = "";
            logTraceWithPattern(response.getStatus(), content);

            switch (response.getStatus()) {
                case HttpStatus.OK_200:
                    if (content.contains("<title>MyPages</title>")) {
                        logger.debug("Status code 200 and on MyPages!");
                        setPasswordFromCookie();
                        return true;
                    } else {
                        logger.debug("Not on MyPages, we need to login again!");
                        return false;
                    }
                case HttpStatus.MOVED_TEMPORARILY_302:
                    // Redirection
                    logger.debug("Status code 302. Redirected. Probably not logged in");
                    return false;
                case HttpStatus.INTERNAL_SERVER_ERROR_500:
                    // Verisure service temporarily down
                    error = "Status code 500. Verisure service temporarily down";
                    logger.debug(error);
                    throw new HttpResponseException(error, response);
                case HttpStatus.SERVICE_UNAVAILABLE_503:
                    // Verisure service temporarily down
                    error = "Status code 503. Verisure service temporarily down";
                    logger.debug(error);
                    throw new HttpResponseException(error, response);
                default:
                    logger.info("Status code {} body {}", response.getStatus(), content);
                    break;
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.warn("Caught an Exception {}", e.getMessage(), e);
        }
        return false;
    }

    private @Nullable <T> T getJSONVerisureAPI(String url, Class<T> jsonClass) {
        logger.debug("HTTP GET: {}", BASEURL + url);
        try {
            ContentResponse response = httpClient.GET(BASEURL + url + "?_=" + System.currentTimeMillis());
            String content = response.getContentAsString();
            logTraceWithPattern(response.getStatus(), content);

            if (response.getStatus() == HttpStatus.OK_200) {
                return gson.fromJson(content, jsonClass);
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.warn("Caught an Exception {}", e.getMessage(), e);
        }
        return null;
    }

    private @Nullable ContentResponse postVerisureAPI(String url, String data, Boolean isJSON) {
        try {
            logger.debug("postVerisureAPI URL: {} Data:{}", url, data);
            Request request = httpClient.newRequest(url).method(HttpMethod.POST);
            if (isJSON) {
                request.header("content-type", "application/json");
            } else {
                if (csrf != null) {
                    request.header("X-CSRF-TOKEN", csrf);
                }
            }
            request.header("Accept", "application/json");
            if (!data.equals("empty")) {
                request.content(new BytesContentProvider(data.getBytes(StandardCharsets.UTF_8)),
                        "application/x-www-form-urlencoded; charset=UTF-8");
            } else {
                logger.debug("Setting cookie with username {} and vid {}", userName, password);
                request.cookie(new HttpCookie("username", userName));
                request.cookie(new HttpCookie("vid", password));
            }
            logger.debug("HTTP POST Request {}.", request.toString());
            return request.send();
        } catch (ExecutionException | InterruptedException | TimeoutException | RuntimeException e) {
            logger.warn("Caught an Exception {}", e.getMessage(), e);
        }
        return null;
    }

    private @Nullable <T> T postJSONVerisureAPI(String url, String data, Class<T> jsonClass) {
        for (int cnt = 0; cnt < APISERVERLIST.size(); cnt++) {
            ContentResponse response = postVerisureAPI(apiServerInUse + url, data, Boolean.TRUE);
            if (response != null) {
                logger.debug("HTTP Response ({})", response.getStatus());
                if (response.getStatus() == HttpStatus.OK_200) {
                    String content = response.getContentAsString();
                    if (content.contains("\"message\":\"Request Failed") && content.contains("503")) {
                        // Maybe Verisure has switched API server in use
                        logger.debug("Changed API server! Response: {}", content);
                        setApiServerInUse(getNextApiServer());
                    } else {
                        String contentChomped = StringUtils.chomp(content);
                        logger.trace("Response body: {}", content);
                        return gson.fromJson(contentChomped, jsonClass);
                    }
                } else {
                    logger.debug("Failed to send POST, Http status code: {}", response.getStatus());
                }
            }
        }
        return null;
    }

    private int postVerisureAPI(String urlString, String data) {
        String url;
        if (urlString.contains("https://mypages")) {
            url = urlString;
        } else {
            url = apiServerInUse + urlString;
        }

        for (int cnt = 0; cnt < APISERVERLIST.size(); cnt++) {
            ContentResponse response = postVerisureAPI(url, data, Boolean.FALSE);
            if (response != null) {
                logger.debug("HTTP Response ({})", response.getStatus());
                int httpStatus = response.getStatus();
                if (httpStatus == HttpStatus.OK_200) {
                    String content = response.getContentAsString();
                    if (content.contains("\"message\":\"Request Failed. Code 503 from")) {
                        if (url.contains("https://mypages")) {
                            // Not an API URL
                            return HttpStatus.SERVICE_UNAVAILABLE_503;
                        } else {
                            // Maybe Verisure has switched API server in use
                            setApiServerInUse(getNextApiServer());
                            url = apiServerInUse + urlString;
                        }
                    } else {
                        logTraceWithPattern(httpStatus, content);
                        return httpStatus;
                    }
                } else {
                    logger.debug("Failed to send POST, Http status code: {}", response.getStatus());
                }
            }
        }
        return HttpStatus.SERVICE_UNAVAILABLE_503;
    }

    private int setSessionCookieAuthLogin() {
        // URL to set status which will give us 2 cookies with username and password used for the session
        String url = STATUS;

        try {
            ContentResponse response = httpClient.GET(url);
            if (logger.isTraceEnabled()) {
                String pattern = "(?m)^\\s*\\r?\\n|\\r?\\n\\s*(?!.*\\r?\\n)";
                String replacement = "";
                logger.trace("HTTP Response ({}) Body:{}", response.getStatus(),
                        response.getContentAsString().replaceAll(pattern, replacement));
            }
            setPasswordFromCookie();
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.warn("Caught an Exception {}", e.getMessage(), e);
        }

        url = AUTH_LOGIN;
        return postVerisureAPI(url, "empty");
    }

    private boolean getInstallations() {
        logger.debug("Attempting to get all installations");

        int httpResultCode = setSessionCookieAuthLogin();
        if (httpResultCode == HttpStatus.OK_200) {
            String url = START_GRAPHQL;

            String queryQLAccountInstallations = "[{\"operationName\":\"AccountInstallations\",\"variables\":{\"email\":\""
                    + userName
                    + "\"},\"query\":\"query AccountInstallations($email: String!) {\\n  account(email: $email) {\\n    owainstallations {\\n      giid\\n      alias\\n      type\\n      subsidiary\\n      dealerId\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}]";
            Class<VerisureInstallations> jsonClass = VerisureInstallations.class;
            VerisureInstallations installations = postJSONVerisureAPI(url, queryQLAccountInstallations, jsonClass);

            if (installations == null) {
                logger.debug("Failed to get installations");
            } else {
                logger.debug("Installation: {}", installations.toString());
                List<Owainstallation> owaInstList = installations.getData().getAccount().getOwainstallations();
                Boolean pinCodesMatchInstallations = true;
                List<String> pinCodes = new ArrayList<>();
                String pinCode = this.pinCode;
                if (pinCode != null) {
                    pinCodes = Arrays.asList(pinCode.split(","));
                    if (owaInstList.size() != pinCodes.size()) {
                        logger.debug("Number of installations {} does not match number of pin codes configured {}",
                                owaInstList.size(), pinCodes.size());
                        pinCodesMatchInstallations = false;
                    }
                } else {
                    logger.debug("No pin-code defined for user {}", userName);
                }

                for (int i = 0; i < owaInstList.size(); i++) {
                    VerisureInstallation vInst = new VerisureInstallation();
                    Owainstallation owaInstallation = owaInstList.get(i);
                    if (owaInstallation.getAlias() != null && owaInstallation.getGiid() != null) {
                        vInst.setInstallationId(new BigDecimal(owaInstallation.getGiid()));
                        vInst.setInstallationName(owaInstallation.getAlias());
                        if (pinCode != null) {
                            if (pinCodesMatchInstallations) {
                                vInst.setPinCode(pinCodes.get(i));
                                logger.debug("Setting pincode {} to installation ID {}", pinCodes.get(i),
                                        owaInstallation.getGiid());
                            } else {
                                vInst.setPinCode(pinCodes.get(0));
                                logger.debug("Setting pincode {} to installation ID {}", pinCodes.get(0),
                                        owaInstallation.getGiid());
                            }
                        }
                        verisureInstallations.put(new BigDecimal(owaInstallation.getGiid()), vInst);
                    } else {
                        logger.warn("Failed to get alias and/or giid");
                        return false;
                    }
                }
            }
        } else {
            logger.warn("Failed to set session cookie and auth login, HTTP result code: {}", httpResultCode);
            return false;
        }
        return true;
    }

    private synchronized boolean logIn() {
        if (!areWeLoggedIn()) {
            logger.debug("Attempting to log in to mypages.verisure.com");
            String url = LOGON_SUF;
            logger.debug("Login URL: {}", url);
            int httpStatusCode = postVerisureAPI(url, authstring);
            if (httpStatusCode != HttpStatus.OK_200) {
                logger.debug("Failed to login, HTTP status code: {}", httpStatusCode);
                return false;
            }
            return true;
        } else {
            return true;
        }
    }

    private <T extends VerisureThing> void notifyListeners(T thing) {
        deviceStatusListeners.stream().forEach(listener -> {
            if (listener.getVerisureThingClass().equals(thing.getClass())) {
                listener.onDeviceStateChanged(thing);
            }
        });
    }

    private void notifyListenersIfChanged(VerisureThing thing, VerisureInstallation installation, String deviceId) {
        String normalizedDeviceId = deviceId.replaceAll("[^a-zA-Z0-9]+", "");
        thing.setDeviceId(normalizedDeviceId);
        VerisureThing oldObj = verisureThings.get(normalizedDeviceId);
        logger.trace("Old thing: {}", oldObj);
        logger.trace("Updated thing: {}", thing);
        if (oldObj == null || !oldObj.equals(thing)) {
            thing.setSiteId(installation.getInstallationId());
            thing.setSiteName(installation.getInstallationName());
            verisureThings.put(thing.getDeviceId(), thing);
            logger.trace("Notify listener of thing {}", thing);
            notifyListeners(thing);
        }
    }

    private void updateStatus() {
        logger.debug("VerisureSession:updateStatus");
        verisureInstallations.forEach((installationId, installation) -> {
            configureInstallationInstance(installation.getInstallationId());
            int httpResultCode = setSessionCookieAuthLogin();
            if (httpResultCode == HttpStatus.OK_200) {
                updateAlarmStatus(VerisureAlarms.class, installation);
                updateSmartLockStatus(VerisureSmartLocks.class, installation);
                updateMiceDetectionStatus(VerisureMiceDetection.class, installation);
                updateClimateStatus(VerisureClimates.class, installation);
                updateDoorWindowStatus(VerisureDoorWindows.class, installation);
                updateUserPresenceStatus(VerisureUserPresences.class, installation);
                updateSmartPlugStatus(VerisureSmartPlugs.class, installation);
                updateBroadbandConnectionStatus(VerisureBroadbandConnections.class, installation);
            } else {
                logger.warn("Failed to set session cookie and auth login, HTTP result code: {}", httpResultCode);
            }
        });
    }

    private String createOperationJSON(String operation, BigDecimal installationId, String query) {
        ArrayList<Operation> list = new ArrayList<>();
        Operation operationJSON = new Operation();
        Variables variables = new Variables();

        variables.setGiid(installationId.toString());
        operationJSON.setOperationName(operation);
        operationJSON.setVariables(variables);
        operationJSON.setQuery(query);
        list.add(operationJSON);

        return gson.toJson(list);
    }

    private synchronized void updateAlarmStatus(Class<? extends VerisureThing> jsonClass,
            VerisureInstallation installation) {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "ArmState";
        String query = "query " + operation
                + "($giid: String!) {\n  installation(giid: $giid) {\n armState {\n type\n statusType\n date\n name\n changedVia\n allowedForFirstLine\n allowed\n errorCodes {\n value\n message\n __typename\n}\n __typename\n}\n __typename\n}\n}\n";

        String queryQLAlarmStatus = createOperationJSON(operation, installationId, query);
        logger.debug("Quering API for alarm status!");

        VerisureThing thing = postJSONVerisureAPI(url, queryQLAlarmStatus, jsonClass);
        logger.debug("REST Response ({})", thing);

        if (thing != null) {
            // Set unique deviceID
            String deviceId = "alarm" + installationId.toString();
            thing.setDeviceId(deviceId);
            notifyListenersIfChanged(thing, installation, deviceId);
        } else {
            logger.debug("Failed to update alarm status, thing is null!");
        }
    }

    private synchronized void updateSmartLockStatus(Class<? extends VerisureThing> jsonClass,
            VerisureInstallation installation) {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "DoorLock";
        String query = "query " + operation
                + "($giid: String!) {\n  installation(giid: $giid) {\n doorlocks {\n device {\n deviceLabel\n area\n __typename\n}\n currentLockState\n eventTime\n secureModeActive\n motorJam\n userString\n method\n __typename\n}\n __typename\n}\n}\n";

        String queryQLSmartLock = createOperationJSON(operation, installationId, query);
        logger.debug("Quering API for smart lock status");

        VerisureSmartLocks thing = (VerisureSmartLocks) postJSONVerisureAPI(url, queryQLSmartLock, jsonClass);
        logger.debug("REST Response ({})", thing);

        if (thing != null) {
            List<VerisureSmartLocks.Doorlock> doorLockList = thing.getData().getInstallation().getDoorlocks();
            doorLockList.forEach(doorLock -> {
                VerisureSmartLocks slThing = new VerisureSmartLocks();
                VerisureSmartLocks.Installation inst = new VerisureSmartLocks.Installation();
                List<VerisureSmartLocks.Doorlock> list = new ArrayList<VerisureSmartLocks.Doorlock>();
                list.add(doorLock);
                inst.setDoorlocks(list);
                VerisureSmartLocks.Data data = new VerisureSmartLocks.Data();
                data.setInstallation(inst);
                slThing.setData(data);
                // Set unique deviceID
                String deviceId = doorLock.getDevice().getDeviceLabel();
                if (deviceId != null) {
                    // Set location
                    slThing.setLocation(doorLock.getDevice().getArea());
                    slThing.setDeviceId(deviceId);
                    // Fetch more info from old endpoint
                    VerisureSmartLock smartLockThing = getJSONVerisureAPI(SMARTLOCK_PATH + slThing.getDeviceId(),
                            VerisureSmartLock.class);
                    logger.debug("REST Response ({})", smartLockThing);
                    slThing.setSmartLockJSON(smartLockThing);
                    notifyListenersIfChanged(slThing, installation, deviceId);
                }
            });
        } else {
            logger.debug("Failed to update SmartLockStatus, thing is null!");
        }
    }

    private synchronized void updateSmartPlugStatus(Class<? extends VerisureThing> jsonClass,
            VerisureInstallation installation) {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "SmartPlug";
        String query = "query " + operation
                + "($giid: String!) {\n  installation(giid: $giid) {\n smartplugs {\n device {\n deviceLabel\n area\n gui {\n support\n label\n __typename\n}\n __typename\n}\n currentState\n icon\n isHazardous\n __typename\n}\n __typename\n}\n}\n";
        String queryQLSmartPlug = createOperationJSON(operation, installationId, query);
        logger.debug("Quering API for smart plug status");

        VerisureSmartPlugs thing = (VerisureSmartPlugs) postJSONVerisureAPI(url, queryQLSmartPlug, jsonClass);
        logger.debug("REST Response ({})", thing);

        if (thing != null) {
            List<VerisureSmartPlugs.Smartplug> smartPlugList = thing.getData().getInstallation().getSmartplugs();
            smartPlugList.forEach(smartPlug -> {
                VerisureSmartPlugs spThing = new VerisureSmartPlugs();
                VerisureSmartPlugs.Installation inst = new VerisureSmartPlugs.Installation();
                List<VerisureSmartPlugs.Smartplug> list = new ArrayList<VerisureSmartPlugs.Smartplug>();
                list.add(smartPlug);
                inst.setSmartplugs(list);
                VerisureSmartPlugs.Data data = new VerisureSmartPlugs.Data();
                data.setInstallation(inst);
                spThing.setData(data);
                // Set unique deviceID
                String deviceId = smartPlug.getDevice().getDeviceLabel();
                if (deviceId != null) {
                    // Set location
                    spThing.setLocation(smartPlug.getDevice().getArea());
                    notifyListenersIfChanged(spThing, installation, deviceId);
                }
            });
        } else {
            logger.debug("Failed to update SmartPlug, thing is null");
        }
    }

    private synchronized void updateClimateStatus(Class<? extends VerisureThing> jsonClass,
            VerisureInstallation installation) {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "Climate";
        String query = "query " + operation
                + "($giid: String!) {\n installation(giid: $giid) {\n climates {\n device {\n deviceLabel\n area\n gui {\n label\n __typename\n }\n __typename\n }\n humidityEnabled\n humidityTimestamp\n humidityValue\n temperatureTimestamp\n temperatureValue\n __typename\n }\n __typename\n}\n}\n";

        String queryQLClimates = createOperationJSON(operation, installationId, query);
        logger.debug("Quering API for climate status");

        VerisureClimates thing = (VerisureClimates) postJSONVerisureAPI(url, queryQLClimates, jsonClass);
        logger.debug("REST Response ({})", thing);

        if (thing != null) {
            List<VerisureClimates.Climate> climateList = thing.getData().getInstallation().getClimates();
            climateList.forEach(climate -> {
                // If thing is Mouse detection device, then skip it, but fetch temperature from it
                String type = climate.getDevice().getGui().getLabel();
                if ("MOUSE".equals(type)) {
                    logger.debug("Mouse detection device!");
                    String deviceId = climate.getDevice().getDeviceLabel();
                    if (deviceId != null) {
                        deviceId = deviceId.replaceAll("[^a-zA-Z0-9]+", "");
                        VerisureThing mouseThing = verisureThings.get(deviceId);
                        if (mouseThing != null && mouseThing instanceof VerisureMiceDetection) {
                            VerisureMiceDetection miceDetectorThing = (VerisureMiceDetection) mouseThing;
                            miceDetectorThing.setTemperatureValue(climate.getTemperatureValue());
                            miceDetectorThing.setTemperatureTime(climate.getTemperatureTimestamp());
                            notifyListeners(miceDetectorThing);
                            logger.debug("Found climate thing for a Verisure Mouse Detector");
                        }
                    }
                    return;
                }
                VerisureClimates cThing = new VerisureClimates();
                VerisureClimates.Installation inst = new VerisureClimates.Installation();
                List<VerisureClimates.Climate> list = new ArrayList<VerisureClimates.Climate>();
                list.add(climate);
                inst.setClimates(list);
                VerisureClimates.Data data = new VerisureClimates.Data();
                data.setInstallation(inst);
                cThing.setData(data);
                // Set unique deviceID
                String deviceId = climate.getDevice().getDeviceLabel();
                if (deviceId != null) {
                    // Set location
                    cThing.setLocation(climate.getDevice().getArea());
                    notifyListenersIfChanged(cThing, installation, deviceId);
                }
            });
        } else {
            logger.debug("Failed to update ClimateStatus, thing is null");
        }
    }

    private synchronized void updateDoorWindowStatus(Class<? extends VerisureThing> jsonClass,
            VerisureInstallation installation) {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "DoorWindow";
        String query = "query " + operation
                + "($giid: String!) {\n installation(giid: $giid) {\n doorWindows {\n device {\n deviceLabel\n area\n __typename\n }\n type\n state\n wired\n reportTime\n __typename\n }\n __typename\n}\n}\n";

        String queryQLDoorWindow = createOperationJSON(operation, installationId, query);
        logger.debug("Quering API for door&window status");

        VerisureDoorWindows thing = (VerisureDoorWindows) postJSONVerisureAPI(url, queryQLDoorWindow, jsonClass);
        logger.debug("REST Response ({})", thing);

        if (thing != null) {
            List<VerisureDoorWindows.DoorWindow> doorWindowList = thing.getData().getInstallation().getDoorWindows();
            doorWindowList.forEach(doorWindow -> {
                VerisureDoorWindows dThing = new VerisureDoorWindows();
                VerisureDoorWindows.Installation inst = new VerisureDoorWindows.Installation();
                List<VerisureDoorWindows.DoorWindow> list = new ArrayList<VerisureDoorWindows.DoorWindow>();
                list.add(doorWindow);
                inst.setDoorWindows(list);
                VerisureDoorWindows.Data data = new VerisureDoorWindows.Data();
                data.setInstallation(inst);
                dThing.setData(data);
                // Set unique deviceID
                String deviceId = doorWindow.getDevice().getDeviceLabel();
                if (deviceId != null) {
                    // Set location
                    dThing.setLocation(doorWindow.getDevice().getArea());
                    notifyListenersIfChanged(dThing, installation, deviceId);
                }
            });
        } else {
            logger.debug("Failed to update DoorWindowStatus thing, is null!");
        }
    }

    private synchronized void updateBroadbandConnectionStatus(Class<? extends VerisureThing> jsonClass,
            VerisureInstallation inst) {
        BigDecimal installationId = inst.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "Broadband";
        String query = "query " + operation
                + "($giid: String!) {\n installation(giid: $giid) {\n broadband {\n testDate\n isBroadbandConnected\n __typename\n }\n __typename\n}\n}\n";

        String queryQLBroadbandConnection = createOperationJSON(operation, installationId, query);
        logger.debug("Quering API for broadband connection status");

        VerisureThing thing = postJSONVerisureAPI(url, queryQLBroadbandConnection, jsonClass);
        logger.debug("REST Response ({})", thing);

        if (thing != null) {
            // Set unique deviceID
            String deviceId = "bc" + installationId.toString();
            notifyListenersIfChanged(thing, inst, deviceId);
        } else {
            logger.debug("Failed to update BroadbandConnection, thing i null!");
        }
    }

    private synchronized void updateUserPresenceStatus(Class<? extends VerisureThing> jsonClass,
            VerisureInstallation installation) {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "userTrackings";
        String query = "query " + operation
                + "($giid: String!) {\ninstallation(giid: $giid) {\n userTrackings {\n isCallingUser\n webAccount\n status\n xbnContactId\n currentLocationName\n deviceId\n name\n currentLocationTimestamp\n deviceName\n currentLocationId\n __typename\n}\n __typename\n}\n}\n";

        String queryQLUserPresence = createOperationJSON(operation, installationId, query);
        logger.debug("Quering API for user presence status");

        VerisureUserPresences thing = (VerisureUserPresences) postJSONVerisureAPI(url, queryQLUserPresence, jsonClass);
        logger.debug("REST Response ({})", thing);

        if (thing != null) {
            List<VerisureUserPresences.UserTracking> userTrackingList = thing.getData().getInstallation()
                    .getUserTrackings();
            userTrackingList.forEach(userTracking -> {
                String localUserTrackingStatus = userTracking.getStatus();
                if (localUserTrackingStatus != null && localUserTrackingStatus.equals("ACTIVE")) {
                    VerisureUserPresences upThing = new VerisureUserPresences();
                    VerisureUserPresences.Installation inst = new VerisureUserPresences.Installation();
                    List<VerisureUserPresences.UserTracking> list = new ArrayList<VerisureUserPresences.UserTracking>();
                    list.add(userTracking);
                    inst.setUserTrackings(list);
                    VerisureUserPresences.Data data = new VerisureUserPresences.Data();
                    data.setInstallation(inst);
                    upThing.setData(data);
                    // Set unique deviceID
                    String deviceId = "up" + userTracking.getWebAccount() + installationId.toString();
                    notifyListenersIfChanged(upThing, installation, deviceId);
                }
            });
        } else {
            logger.debug("Failed to update UserPresenceStatus, thing is null!");
        }
    }

    private synchronized void updateMiceDetectionStatus(Class<? extends VerisureThing> jsonClass,
            VerisureInstallation installation) {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "Mouse";
        String query = "query " + operation
                + "($giid: String!) {\n installation(giid: $giid) {\n mice {\n device {\n deviceLabel\n area\n gui {\n support\n __typename\n}\n __typename\n}\n type\n detections {\n count\n gatewayTime\n nodeTime\n duration\n __typename\n}\n __typename\n}\n __typename\n}\n}\n";

        String queryQLMiceDetection = createOperationJSON(operation, installationId, query);
        logger.debug("Quering API for mice detection status");

        VerisureMiceDetection thing = (VerisureMiceDetection) postJSONVerisureAPI(url, queryQLMiceDetection, jsonClass);
        logger.debug("REST Response ({})", thing);

        if (thing != null) {
            List<VerisureMiceDetection.Mouse> miceList = thing.getData().getInstallation().getMice();
            miceList.forEach(mouse -> {
                VerisureMiceDetection miceThing = new VerisureMiceDetection();
                VerisureMiceDetection.Installation inst = new VerisureMiceDetection.Installation();
                List<VerisureMiceDetection.Mouse> list = new ArrayList<VerisureMiceDetection.Mouse>();
                list.add(mouse);
                inst.setMice(list);
                VerisureMiceDetection.Data data = new VerisureMiceDetection.Data();
                data.setInstallation(inst);
                miceThing.setData(data);
                // Set unique deviceID
                String deviceId = mouse.getDevice().getDeviceLabel();
                logger.debug("Mouse id: {} for thing: {}", deviceId, mouse);
                if (deviceId != null) {
                    // Set location
                    miceThing.setLocation(mouse.getDevice().getArea());
                    notifyListenersIfChanged(miceThing, installation, deviceId);
                }
            });
        } else {
            logger.debug("Failed to update Mice Detection Status, thing is null!");
        }
    }

    @NonNullByDefault
    private final class VerisureInstallation {
        private @Nullable String installationName;
        private BigDecimal installationId = new BigDecimal(0);
        private @Nullable String pinCode;

        public @Nullable String getPinCode() {
            return pinCode;
        }

        public void setPinCode(@Nullable String pinCode) {
            this.pinCode = pinCode;
        }

        public VerisureInstallation() {
        }

        public BigDecimal getInstallationId() {
            return installationId;
        }

        public @Nullable String getInstallationName() {
            return installationName;
        }

        public void setInstallationId(BigDecimal installationId) {
            this.installationId = installationId;
        }

        public void setInstallationName(@Nullable String installationName) {
            this.installationName = installationName;
        }
    }

    @NonNullByDefault
    private static class Operation {

        @SuppressWarnings("unused")
        private @Nullable String operationName;
        @SuppressWarnings("unused")
        private Variables variables = new Variables();
        @SuppressWarnings("unused")
        private @Nullable String query;

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        public void setVariables(Variables variables) {
            this.variables = variables;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }

    @NonNullByDefault
    private static class Variables {

        @SuppressWarnings("unused")
        private @Nullable String giid;

        public void setGiid(String giid) {
            this.giid = giid;
        }
    }
}
