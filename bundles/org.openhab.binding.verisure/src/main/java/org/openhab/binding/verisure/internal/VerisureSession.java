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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import org.openhab.binding.verisure.internal.dto.VerisureAlarmsDTO;
import org.openhab.binding.verisure.internal.dto.VerisureBroadbandConnectionsDTO;
import org.openhab.binding.verisure.internal.dto.VerisureClimatesDTO;
import org.openhab.binding.verisure.internal.dto.VerisureDoorWindowsDTO;
import org.openhab.binding.verisure.internal.dto.VerisureEventLogDTO;
import org.openhab.binding.verisure.internal.dto.VerisureGatewayDTO;
import org.openhab.binding.verisure.internal.dto.VerisureGatewayDTO.CommunicationState;
import org.openhab.binding.verisure.internal.dto.VerisureInstallationsDTO;
import org.openhab.binding.verisure.internal.dto.VerisureInstallationsDTO.Owainstallation;
import org.openhab.binding.verisure.internal.dto.VerisureMiceDetectionDTO;
import org.openhab.binding.verisure.internal.dto.VerisureSmartLockDTO;
import org.openhab.binding.verisure.internal.dto.VerisureSmartLocksDTO;
import org.openhab.binding.verisure.internal.dto.VerisureSmartPlugsDTO;
import org.openhab.binding.verisure.internal.dto.VerisureThingDTO;
import org.openhab.binding.verisure.internal.dto.VerisureUserPresencesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This class performs the communication with Verisure My Pages.
 *
 * @author Jarle Hjortland - Initial contribution
 * @author Jan Gustafsson - Re-design and support for several sites and update to new Verisure API
 *
 */
@NonNullByDefault
public class VerisureSession {

    @NonNullByDefault({})
    private final Map<String, VerisureThingDTO> verisureThings = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(VerisureSession.class);
    private final Gson gson = new Gson();
    private final List<DeviceStatusListener<VerisureThingDTO>> deviceStatusListeners = new CopyOnWriteArrayList<>();
    private final Map<BigDecimal, VerisureInstallation> verisureInstallations = new ConcurrentHashMap<>();
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

    public boolean unregisterDeviceStatusListener(
            DeviceStatusListener<? extends VerisureThingDTO> deviceStatusListener) {
        logger.debug("unregisterDeviceStatusListener for listener {}", deviceStatusListener);
        return deviceStatusListeners.remove(deviceStatusListener);
    }

    @SuppressWarnings("unchecked")
    public boolean registerDeviceStatusListener(DeviceStatusListener<? extends VerisureThingDTO> deviceStatusListener) {
        logger.debug("registerDeviceStatusListener for listener {}", deviceStatusListener);
        return deviceStatusListeners.add((DeviceStatusListener<VerisureThingDTO>) deviceStatusListener);
    }

    @SuppressWarnings({ "unchecked", "null" })
    public <T extends VerisureThingDTO> @Nullable T getVerisureThing(String deviceId, Class<T> thingType) {
        VerisureThingDTO thing = verisureThings.get(deviceId);
        if (thing != null && thingType.isInstance(thing)) {
            return (T) thing;
        }
        return null;
    }

    public Collection<VerisureThingDTO> getVerisureThings() {
        return verisureThings.values();
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

    private <T> @Nullable T getJSONVerisureAPI(String url, Class<T> jsonClass) {
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
            Class<VerisureInstallationsDTO> jsonClass = VerisureInstallationsDTO.class;
            VerisureInstallationsDTO installations = postJSONVerisureAPI(url, queryQLAccountInstallations, jsonClass);

            if (installations == null) {
                logger.debug("Failed to get installations");
            } else {
                logger.debug("Installation: {}", installations.toString());
                List<Owainstallation> owaInstList = installations.getData().getAccount().getOwainstallations();
                Boolean pinCodesMatchInstallations = true;
                List<String> pinCodes = null;
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
                    String installationId = owaInstallation.getGiid();
                    if (owaInstallation.getAlias() != null && installationId != null) {
                        vInst.setInstallationId(new BigDecimal(installationId));
                        vInst.setInstallationName(owaInstallation.getAlias());
                        if (pinCode != null && pinCodes != null) {
                            int pinCodeIndex = pinCodesMatchInstallations ? i : 0;
                            vInst.setPinCode(pinCodes.get(pinCodeIndex));
                            logger.debug("Setting pincode {} to installation ID {}", pinCodeIndex, installationId);
                        }
                        verisureInstallations.put(new BigDecimal(installationId), vInst);
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

    private <T extends VerisureThingDTO> void notifyListeners(T thing) {
        deviceStatusListeners.stream().forEach(listener -> {
            if (listener.getVerisureThingClass().equals(thing.getClass())) {
                listener.onDeviceStateChanged(thing);
            }
        });
    }

    private void notifyListenersIfChanged(VerisureThingDTO thing, VerisureInstallation installation, String deviceId) {
        String normalizedDeviceId = deviceId.replaceAll("[^a-zA-Z0-9]+", "");
        thing.setDeviceId(normalizedDeviceId);
        VerisureThingDTO oldObj = verisureThings.get(normalizedDeviceId);
        logger.trace("Old thing: {}", oldObj);
        logger.trace("Updated thing: {}", thing);
        if (!thing.equals(oldObj)) {
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
                updateAlarmStatus(installation);
                updateSmartLockStatus(installation);
                updateMiceDetectionStatus(installation);
                updateClimateStatus(installation);
                updateDoorWindowStatus(installation);
                updateUserPresenceStatus(installation);
                updateSmartPlugStatus(installation);
                updateBroadbandConnectionStatus(installation);
                updateEventLogStatus(installation);
                updateGatewayStatus(installation);
            } else {
                logger.warn("Failed to set session cookie and auth login, HTTP result code: {}", httpResultCode);
            }
        });
    }

    private String createOperationJSON(String operation, VariablesDTO variables, String query) {
        OperationDTO operationJSON = new OperationDTO();
        operationJSON.setOperationName(operation);
        operationJSON.setVariables(variables);
        operationJSON.setQuery(query);
        List<OperationDTO> list = Collections.singletonList(operationJSON);

        return gson.toJson(list);
    }

    private synchronized void updateAlarmStatus(VerisureInstallation installation) {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "ArmState";
        VariablesDTO variables = new VariablesDTO();
        variables.setGiid(installationId.toString());
        String query = "query " + operation
                + "($giid: String!) {\n  installation(giid: $giid) {\n armState {\n type\n statusType\n date\n name\n changedVia\n allowedForFirstLine\n allowed\n errorCodes {\n value\n message\n __typename\n}\n __typename\n}\n __typename\n}\n}\n";

        String queryQLAlarmStatus = createOperationJSON(operation, variables, query);
        logger.debug("Quering API for alarm status!");

        VerisureThingDTO thing = postJSONVerisureAPI(url, queryQLAlarmStatus, VerisureAlarmsDTO.class);
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

    private synchronized void updateSmartLockStatus(VerisureInstallation installation) {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "DoorLock";
        String query = "query " + operation
                + "($giid: String!) {\n  installation(giid: $giid) {\n doorlocks {\n device {\n deviceLabel\n area\n __typename\n}\n currentLockState\n eventTime\n secureModeActive\n motorJam\n userString\n method\n __typename\n}\n __typename\n}\n}\n";
        VariablesDTO variables = new VariablesDTO();
        variables.setGiid(installationId.toString());
        String queryQLSmartLock = createOperationJSON(operation, variables, query);
        logger.debug("Quering API for smart lock status");

        VerisureSmartLocksDTO thing = postJSONVerisureAPI(url, queryQLSmartLock, VerisureSmartLocksDTO.class);
        logger.debug("REST Response ({})", thing);

        if (thing != null) {
            List<VerisureSmartLocksDTO.Doorlock> doorLockList = thing.getData().getInstallation().getDoorlocks();
            doorLockList.forEach(doorLock -> {
                VerisureSmartLocksDTO slThing = new VerisureSmartLocksDTO();
                VerisureSmartLocksDTO.Installation inst = new VerisureSmartLocksDTO.Installation();
                List<VerisureSmartLocksDTO.Doorlock> list = Collections.singletonList(doorLock);
                inst.setDoorlocks(list);
                VerisureSmartLocksDTO.Data data = new VerisureSmartLocksDTO.Data();
                data.setInstallation(inst);
                slThing.setData(data);
                // Set unique deviceID
                String deviceId = doorLock.getDevice().getDeviceLabel();
                if (deviceId != null) {
                    // Set location
                    slThing.setLocation(doorLock.getDevice().getArea());
                    slThing.setDeviceId(deviceId);
                    // Fetch more info from old endpoint
                    VerisureSmartLockDTO smartLockThing = getJSONVerisureAPI(SMARTLOCK_PATH + slThing.getDeviceId(),
                            VerisureSmartLockDTO.class);
                    logger.debug("REST Response ({})", smartLockThing);
                    slThing.setSmartLockJSON(smartLockThing);
                    notifyListenersIfChanged(slThing, installation, deviceId);
                }
            });
        } else {
            logger.debug("Failed to update SmartLockStatus, thing is null!");
        }
    }

    private synchronized void updateSmartPlugStatus(VerisureInstallation installation) {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "SmartPlug";
        VariablesDTO variables = new VariablesDTO();
        variables.setGiid(installationId.toString());
        String query = "query " + operation
                + "($giid: String!) {\n  installation(giid: $giid) {\n smartplugs {\n device {\n deviceLabel\n area\n gui {\n support\n label\n __typename\n}\n __typename\n}\n currentState\n icon\n isHazardous\n __typename\n}\n __typename\n}\n}\n";
        String queryQLSmartPlug = createOperationJSON(operation, variables, query);
        logger.debug("Quering API for smart plug status");

        VerisureSmartPlugsDTO thing = postJSONVerisureAPI(url, queryQLSmartPlug, VerisureSmartPlugsDTO.class);
        logger.debug("REST Response ({})", thing);

        if (thing != null) {
            List<VerisureSmartPlugsDTO.Smartplug> smartPlugList = thing.getData().getInstallation().getSmartplugs();
            smartPlugList.forEach(smartPlug -> {
                VerisureSmartPlugsDTO spThing = new VerisureSmartPlugsDTO();
                VerisureSmartPlugsDTO.Installation inst = new VerisureSmartPlugsDTO.Installation();
                List<VerisureSmartPlugsDTO.Smartplug> list = Collections.singletonList(smartPlug);
                inst.setSmartplugs(list);
                VerisureSmartPlugsDTO.Data data = new VerisureSmartPlugsDTO.Data();
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

    private synchronized void updateClimateStatus(VerisureInstallation installation) {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        VariablesDTO variables = new VariablesDTO();
        variables.setGiid(installationId.toString());
        String operation = "Climate";
        String query = "query " + operation
                + "($giid: String!) {\n installation(giid: $giid) {\n climates {\n device {\n deviceLabel\n area\n gui {\n label\n __typename\n }\n __typename\n }\n humidityEnabled\n humidityTimestamp\n humidityValue\n temperatureTimestamp\n temperatureValue\n __typename\n }\n __typename\n}\n}\n";

        String queryQLClimates = createOperationJSON(operation, variables, query);
        logger.debug("Quering API for climate status");

        VerisureClimatesDTO thing = postJSONVerisureAPI(url, queryQLClimates, VerisureClimatesDTO.class);
        logger.debug("REST Response ({})", thing);

        if (thing != null) {
            List<VerisureClimatesDTO.Climate> climateList = thing.getData().getInstallation().getClimates();
            climateList.forEach(climate -> {
                // If thing is Mouse detection device, then skip it, but fetch temperature from it
                String type = climate.getDevice().getGui().getLabel();
                if ("MOUSE".equals(type)) {
                    logger.debug("Mouse detection device!");
                    String deviceId = climate.getDevice().getDeviceLabel();
                    if (deviceId != null) {
                        deviceId = deviceId.replaceAll("[^a-zA-Z0-9]+", "");
                        VerisureThingDTO mouseThing = verisureThings.get(deviceId);
                        if (mouseThing != null && mouseThing instanceof VerisureMiceDetectionDTO) {
                            VerisureMiceDetectionDTO miceDetectorThing = (VerisureMiceDetectionDTO) mouseThing;
                            miceDetectorThing.setTemperatureValue(climate.getTemperatureValue());
                            miceDetectorThing.setTemperatureTime(climate.getTemperatureTimestamp());
                            notifyListeners(miceDetectorThing);
                            logger.debug("Found climate thing for a Verisure Mouse Detector");
                        }
                    }
                    return;
                }
                VerisureClimatesDTO cThing = new VerisureClimatesDTO();
                VerisureClimatesDTO.Installation inst = new VerisureClimatesDTO.Installation();
                List<VerisureClimatesDTO.Climate> list = Collections.singletonList(climate);
                inst.setClimates(list);
                VerisureClimatesDTO.Data data = new VerisureClimatesDTO.Data();
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

    private synchronized void updateDoorWindowStatus(VerisureInstallation installation) {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "DoorWindow";
        VariablesDTO variables = new VariablesDTO();
        variables.setGiid(installationId.toString());
        String query = "query " + operation
                + "($giid: String!) {\n installation(giid: $giid) {\n doorWindows {\n device {\n deviceLabel\n area\n __typename\n }\n type\n state\n wired\n reportTime\n __typename\n }\n __typename\n}\n}\n";

        String queryQLDoorWindow = createOperationJSON(operation, variables, query);
        logger.debug("Quering API for door&window status");

        VerisureDoorWindowsDTO thing = postJSONVerisureAPI(url, queryQLDoorWindow, VerisureDoorWindowsDTO.class);
        logger.debug("REST Response ({})", thing);

        if (thing != null) {
            List<VerisureDoorWindowsDTO.DoorWindow> doorWindowList = thing.getData().getInstallation().getDoorWindows();
            doorWindowList.forEach(doorWindow -> {
                VerisureDoorWindowsDTO dThing = new VerisureDoorWindowsDTO();
                VerisureDoorWindowsDTO.Installation inst = new VerisureDoorWindowsDTO.Installation();
                List<VerisureDoorWindowsDTO.DoorWindow> list = Collections.singletonList(doorWindow);
                inst.setDoorWindows(list);
                VerisureDoorWindowsDTO.Data data = new VerisureDoorWindowsDTO.Data();
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

    private synchronized void updateBroadbandConnectionStatus(VerisureInstallation inst) {
        BigDecimal installationId = inst.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "Broadband";
        VariablesDTO variables = new VariablesDTO();
        variables.setGiid(installationId.toString());
        String query = "query " + operation
                + "($giid: String!) {\n installation(giid: $giid) {\n broadband {\n testDate\n isBroadbandConnected\n __typename\n }\n __typename\n}\n}\n";

        String queryQLBroadbandConnection = createOperationJSON(operation, variables, query);
        logger.debug("Quering API for broadband connection status");

        VerisureThingDTO thing = postJSONVerisureAPI(url, queryQLBroadbandConnection,
                VerisureBroadbandConnectionsDTO.class);
        logger.debug("REST Response ({})", thing);

        if (thing != null) {
            // Set unique deviceID
            String deviceId = "bc" + installationId.toString();
            notifyListenersIfChanged(thing, inst, deviceId);
        } else {
            logger.debug("Failed to update BroadbandConnection, thing is null!");
        }
    }

    private synchronized void updateUserPresenceStatus(VerisureInstallation installation) {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "userTrackings";
        VariablesDTO variables = new VariablesDTO();
        variables.setGiid(installationId.toString());
        String query = "query " + operation
                + "($giid: String!) {\ninstallation(giid: $giid) {\n userTrackings {\n isCallingUser\n webAccount\n status\n xbnContactId\n currentLocationName\n deviceId\n name\n currentLocationTimestamp\n deviceName\n currentLocationId\n __typename\n}\n __typename\n}\n}\n";

        String queryQLUserPresence = createOperationJSON(operation, variables, query);
        logger.debug("Quering API for user presence status");

        VerisureUserPresencesDTO thing = postJSONVerisureAPI(url, queryQLUserPresence, VerisureUserPresencesDTO.class);
        logger.debug("REST Response ({})", thing);

        if (thing != null) {
            List<VerisureUserPresencesDTO.UserTracking> userTrackingList = thing.getData().getInstallation()
                    .getUserTrackings();
            userTrackingList.forEach(userTracking -> {
                String localUserTrackingStatus = userTracking.getStatus();
                if (localUserTrackingStatus != null && localUserTrackingStatus.equals("ACTIVE")) {
                    VerisureUserPresencesDTO upThing = new VerisureUserPresencesDTO();
                    VerisureUserPresencesDTO.Installation inst = new VerisureUserPresencesDTO.Installation();
                    List<VerisureUserPresencesDTO.UserTracking> list = Collections.singletonList(userTracking);
                    inst.setUserTrackings(list);
                    VerisureUserPresencesDTO.Data data = new VerisureUserPresencesDTO.Data();
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

    private synchronized void updateMiceDetectionStatus(VerisureInstallation installation) {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "Mouse";
        VariablesDTO variables = new VariablesDTO();
        variables.setGiid(installationId.toString());
        String query = "query " + operation
                + "($giid: String!) {\n installation(giid: $giid) {\n mice {\n device {\n deviceLabel\n area\n gui {\n support\n __typename\n}\n __typename\n}\n type\n detections {\n count\n gatewayTime\n nodeTime\n duration\n __typename\n}\n __typename\n}\n __typename\n}\n}\n";

        String queryQLMiceDetection = createOperationJSON(operation, variables, query);
        logger.debug("Quering API for mice detection status");

        VerisureMiceDetectionDTO thing = postJSONVerisureAPI(url, queryQLMiceDetection, VerisureMiceDetectionDTO.class);
        logger.debug("REST Response ({})", thing);

        if (thing != null) {
            List<VerisureMiceDetectionDTO.Mouse> miceList = thing.getData().getInstallation().getMice();
            miceList.forEach(mouse -> {
                VerisureMiceDetectionDTO miceThing = new VerisureMiceDetectionDTO();
                VerisureMiceDetectionDTO.Installation inst = new VerisureMiceDetectionDTO.Installation();
                List<VerisureMiceDetectionDTO.Mouse> list = Collections.singletonList(mouse);
                inst.setMice(list);
                VerisureMiceDetectionDTO.Data data = new VerisureMiceDetectionDTO.Data();
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

    private synchronized void updateEventLogStatus(VerisureInstallation installation) {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "EventLog";
        int offset = 0;
        int numberOfEvents = 15;
        List<String> eventCategories = new ArrayList<>(Arrays.asList("INTRUSION", "FIRE", "SOS", "WATER", "ANIMAL",
                "TECHNICAL", "WARNING", "ARM", "DISARM", "LOCK", "UNLOCK", "PICTURE", "CLIMATE", "CAMERA_SETTINGS"));
        VariablesDTO variables = new VariablesDTO();
        variables.setGiid(installationId.toString());
        variables.setHideNotifications(true);
        variables.setOffset(offset);
        variables.setPagesize(numberOfEvents);
        variables.setEventCategories(eventCategories);
        String query = "query " + operation
                + "($giid: String!, $offset: Int!, $pagesize: Int!, $eventCategories: [String], $fromDate: String, $toDate: String, $eventContactIds: [String]) {\n installation(giid: $giid) {\n eventLog(offset: $offset, pagesize: $pagesize, eventCategories: $eventCategories, eventContactIds: $eventContactIds, fromDate: $fromDate, toDate: $toDate) {\n moreDataAvailable\n pagedList {\n device {\n deviceLabel\n area\n gui {\n label\n __typename\n }\n __typename\n }\n gatewayArea\n eventType\n eventCategory\n eventSource\n eventId\n eventTime\n userName\n armState\n userType\n climateValue\n sensorType\n eventCount\n  __typename\n }\n __typename\n }\n __typename\n }\n}\n";

        String queryQLEventLog = createOperationJSON(operation, variables, query);
        logger.debug("Quering API for event log status");

        VerisureEventLogDTO thing = postJSONVerisureAPI(url, queryQLEventLog, VerisureEventLogDTO.class);
        logger.debug("REST Response ({})", thing);

        if (thing != null) {
            // Set unique deviceID
            String deviceId = "el" + installationId.toString();
            notifyListenersIfChanged(thing, installation, deviceId);
        } else {
            logger.debug("Failed to update EventLogStatus, thing is null!");
        }
    }

    private synchronized void updateGatewayStatus(VerisureInstallation installation) {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "communicationState";
        VariablesDTO variables = new VariablesDTO();
        variables.setGiid(installationId.toString());

        String query = "query " + operation
                + "($giid: String!) {\n installation(giid: $giid) {\n communicationState {\n hardwareCarrierType\n result\n mediaType\n device {\n deviceLabel\n area\n gui {\n label\n __typename\n }\n __typename\n }\n testDate\n __typename\n }\n __typename\n }\n}";

        String queryQLEventLog = createOperationJSON(operation, variables, query);
        logger.debug("Quering API for gateway status");

        VerisureGatewayDTO thing = postJSONVerisureAPI(url, queryQLEventLog, VerisureGatewayDTO.class);
        logger.debug("REST Response ({})", thing);

        if (thing != null) {
            // Set unique deviceID
            List<CommunicationState> communicationStateList = thing.getData().getInstallation().getCommunicationState();
            if (!communicationStateList.isEmpty()) {
                String deviceId = communicationStateList.get(0).getDevice().getDeviceLabel();
                if (deviceId != null) {
                    notifyListenersIfChanged(thing, installation, deviceId);
                }
            }
        } else {
            logger.debug("Failed to update EventLogStatus, thing is null!");
        }
    }

    private final class VerisureInstallation {
        private @Nullable String installationName;
        private BigDecimal installationId = BigDecimal.ZERO;
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

    private static class OperationDTO {

        @SuppressWarnings("unused")
        private @Nullable String operationName;
        @SuppressWarnings("unused")
        private VariablesDTO variables = new VariablesDTO();
        @SuppressWarnings("unused")
        private @Nullable String query;

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        public void setVariables(VariablesDTO variables) {
            this.variables = variables;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }

    public static class VariablesDTO {

        @SuppressWarnings("unused")
        private @Nullable Boolean hideNotifications = null;
        @SuppressWarnings("unused")
        private @Nullable Integer offset = null;
        @SuppressWarnings("unused")
        private @Nullable Integer pagesize = null;
        @SuppressWarnings("unused")
        private @Nullable List<String> eventCategories = null;
        @SuppressWarnings("unused")
        private @Nullable String giid;

        public void setHideNotifications(boolean hideNotifications) {
            this.hideNotifications = hideNotifications;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public void setPagesize(Integer pagesize) {
            this.pagesize = pagesize;
        }

        public void setEventCategories(List<String> eventCategories) {
            this.eventCategories = eventCategories;
        }

        public void setGiid(String giid) {
            this.giid = giid;
        }
    }
}
