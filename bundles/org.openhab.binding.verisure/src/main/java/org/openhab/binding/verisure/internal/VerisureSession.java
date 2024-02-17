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
package org.openhab.binding.verisure.internal;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.math.BigDecimal;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import org.openhab.binding.verisure.internal.dto.VerisureBatteryStatusDTO;
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
import org.openhab.binding.verisure.internal.handler.VerisureThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
    private final Map<String, VerisureThingHandler<?>> verisureHandlers = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(VerisureSession.class);
    private final Gson gson = new Gson();
    private final Set<DeviceStatusListener<VerisureThingDTO>> deviceStatusListeners = ConcurrentHashMap.newKeySet();
    private final Map<BigDecimal, VerisureInstallation> verisureInstallations = new ConcurrentHashMap<>();
    private static final List<String> APISERVERLIST = Arrays.asList("https://m-api01.verisure.com",
            "https://m-api02.verisure.com");
    private int apiServerInUseIndex = 0;
    private int numberOfEvents = 15;
    private static final int REQUEST_TIMEOUT_MS = 10_000;
    private static final String USER_NAME = "username";
    private static final String VID = "vid";
    private static final String VS_STEPUP = "vs-stepup";
    private static final String VS_ACCESS = "vs-access";
    private String apiServerInUse = APISERVERLIST.get(apiServerInUseIndex);
    private String authstring = "";
    private @Nullable String csrf;
    private @Nullable String pinCode;
    private HttpClient httpClient;
    private String userName = "";
    private String password = "";
    private String vid = "";
    private String vsAccess = "";
    private String vsStepup = "";

    public VerisureSession(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public boolean initialize(@Nullable String authstring, @Nullable String pinCode, String userName, String password) {
        if (authstring != null) {
            this.authstring = authstring;
            this.pinCode = pinCode;
            this.userName = userName;
            this.password = password;
            // Try to login to Verisure
            if (logIn()) {
                return getInstallations();
            } else {
                return false;
            }
        }
        return false;
    }

    public boolean refresh() {
        if (logIn()) {
            if (updateStatus()) {
                return true;
            }
        }
        return false;
    }

    public int sendCommand(String url, String data, BigDecimal installationId) {
        logger.debug("Sending command with URL {} and data {}", url, data);
        try {
            configureInstallationInstance(installationId);
            int httpResultCode = setSessionCookieAuthLogin();
            if (httpResultCode == HttpStatus.OK_200) {
                return postVerisureAPI(url, data);
            } else {
                return httpResultCode;
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.debug("Failed to send command {}", e.getMessage());
        }
        return HttpStatus.BAD_REQUEST_400;
    }

    public boolean unregisterDeviceStatusListener(
            DeviceStatusListener<? extends VerisureThingDTO> deviceStatusListener) {
        return deviceStatusListeners.remove(deviceStatusListener);
    }

    @SuppressWarnings("unchecked")
    public boolean registerDeviceStatusListener(DeviceStatusListener<? extends VerisureThingDTO> deviceStatusListener) {
        return deviceStatusListeners.add((DeviceStatusListener<VerisureThingDTO>) deviceStatusListener);
    }

    @SuppressWarnings({ "unchecked" })
    public <T extends VerisureThingDTO> @Nullable T getVerisureThing(String deviceId, Class<T> thingType) {
        VerisureThingDTO thing = verisureThings.get(deviceId);
        if (thingType.isInstance(thing)) {
            return (T) thing;
        }
        return null;
    }

    public <T extends VerisureThingDTO> @Nullable T getVerisureThing(String deviceId) {
        VerisureThingDTO thing = verisureThings.get(deviceId);
        if (thing != null) {
            @SuppressWarnings("unchecked")
            T thing2 = (T) thing;
            return thing2;
        }
        return null;
    }

    public @Nullable VerisureThingHandler<?> getVerisureThinghandler(String deviceId) {
        VerisureThingHandler<?> thingHandler = verisureHandlers.get(deviceId);
        return thingHandler;
    }

    public void setVerisureThingHandler(VerisureThingHandler<?> vth, String deviceId) {
        verisureHandlers.put(deviceId, vth);
    }

    public void removeVerisureThingHandler(String deviceId) {
        verisureHandlers.remove(deviceId);
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

    public void setNumberOfEvents(int numberOfEvents) {
        this.numberOfEvents = numberOfEvents;
    }

    public void configureInstallationInstance(BigDecimal installationId)
            throws ExecutionException, InterruptedException, TimeoutException {
        csrf = getCsrfToken(installationId);
        logger.debug("Got CSRF: {}", csrf);
        // Set installation
        String url = SET_INSTALLATION + installationId;
        httpClient.GET(url);
    }

    public @Nullable String getCsrfToken(BigDecimal installationId)
            throws ExecutionException, InterruptedException, TimeoutException {
        String html = null;
        String url = SETTINGS + installationId;

        ContentResponse resp = httpClient.GET(url);
        html = resp.getContentAsString();
        logger.trace("url: {} html: {}", url, html);

        Document htmlDocument = Jsoup.parse(html);
        Element nameInput = htmlDocument.select("input[name=_csrf]").first();
        if (nameInput != null) {
            return nameInput.attr("value");
        } else {
            return null;
        }
    }

    public @Nullable String getPinCode(BigDecimal installationId) {
        VerisureInstallation inst = verisureInstallations.get(installationId);
        if (inst != null) {
            return inst.getPinCode();
        } else {
            logger.debug("Installation is null!");
            return null;
        }
    }

    private void analyzeCookies() {
        CookieStore c = httpClient.getCookieStore();
        List<HttpCookie> cookies = c.getCookies();
        final List<HttpCookie> unmodifiableList = List.of(cookies.toArray(new HttpCookie[] {}));
        unmodifiableList.forEach(cookie -> {
            logger.trace("Response Cookie: {}", cookie);
            if (VID.equals(cookie.getName())) {
                vid = cookie.getValue();
                logger.debug("Fetching vid {} from cookie", vid);
            } else if (VS_ACCESS.equals(cookie.getName())) {
                vsAccess = cookie.getValue();
                logger.debug("Fetching vs-access {} from cookie", vsAccess);
            } else if (VS_STEPUP.equals(cookie.getName())) {
                vsStepup = cookie.getValue();
                logger.debug("Fetching vs-stepup {} from cookie", vsStepup);
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

    private boolean areWeLoggedIn() throws ExecutionException, InterruptedException, TimeoutException {
        logger.debug("Checking if we are logged in");
        String url = STATUS;

        ContentResponse response = httpClient.newRequest(url).method(HttpMethod.GET).send();
        String content = response.getContentAsString();
        logTraceWithPattern(response.getStatus(), content);

        switch (response.getStatus()) {
            case HttpStatus.OK_200:
                if (content.contains("<link href=\"/newapp")) {
                    return true;
                } else {
                    logger.debug("We need to login again!");
                    return false;
                }
            case HttpStatus.MOVED_TEMPORARILY_302:
                // Redirection
                logger.debug("Status code 302. Redirected. Probably not logged in");
                return false;
            case HttpStatus.INTERNAL_SERVER_ERROR_500:
            case HttpStatus.SERVICE_UNAVAILABLE_503:
                throw new HttpResponseException(
                        "Status code " + response.getStatus() + ". Verisure service temporarily down", response);
            default:
                logger.debug("Status code {} body {}", response.getStatus(), content);
                break;
        }
        return false;
    }

    private <T> @Nullable T getJSONVerisureAPI(String url, Class<T> jsonClass)
            throws ExecutionException, InterruptedException, TimeoutException, JsonSyntaxException {
        logger.debug("HTTP GET: {}", BASE_URL + url);

        ContentResponse response = httpClient.GET(BASE_URL + url + "?_=" + System.currentTimeMillis());
        String content = response.getContentAsString();
        logTraceWithPattern(response.getStatus(), content);

        return gson.fromJson(content, jsonClass);
    }

    private ContentResponse postVerisureAPI(String url, String data, boolean isJSON)
            throws ExecutionException, InterruptedException, TimeoutException {
        logger.debug("postVerisureAPI URL: {} Data:{}", url, data);

        Request request = httpClient.newRequest(url).method(HttpMethod.POST);
        request.timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (isJSON) {
            request.header("content-type", "application/json");
        } else {
            if (csrf != null) {
                request.header("X-CSRF-TOKEN", csrf);
            }
        }
        request.header("Accept", "application/json");

        if (url.contains(AUTH_LOGIN)) {
            request.header("APPLICATION_ID", "OpenHAB Verisure");
            String basicAuhentication = Base64.getEncoder().encodeToString((userName + ":" + password).getBytes());
            request.header("authorization", "Basic " + basicAuhentication);
        } else {
            if (!vid.isEmpty()) {
                request.cookie(new HttpCookie(VID, vid));
                logger.debug("Setting cookie with vid {}", vid);
            }
            if (!vsAccess.isEmpty()) {
                request.cookie(new HttpCookie(VS_ACCESS, vsAccess));
                logger.debug("Setting cookie with vs-access {}", vsAccess);
            }
            logger.debug("Setting cookie with username {}", userName);
            request.cookie(new HttpCookie(USER_NAME, userName));
        }

        if (!"empty".equals(data)) {
            request.content(new BytesContentProvider(data.getBytes(StandardCharsets.UTF_8)),
                    "application/x-www-form-urlencoded; charset=UTF-8");
        }

        logger.debug("HTTP POST Request {}.", request.toString());
        return request.send();
    }

    private <T> T postJSONVerisureAPI(String url, String data, Class<T> jsonClass)
            throws ExecutionException, InterruptedException, TimeoutException, JsonSyntaxException, PostToAPIException {
        for (int cnt = 0; cnt < APISERVERLIST.size(); cnt++) {
            ContentResponse response = postVerisureAPI(apiServerInUse + url, data, Boolean.TRUE);
            logger.debug("HTTP Response ({})", response.getStatus());
            if (response.getStatus() == HttpStatus.OK_200) {
                String content = response.getContentAsString();
                if (content.contains("\"message\":\"Request Failed") && content.contains("503")) {
                    // Maybe Verisure has switched API server in use?
                    logger.debug("Changed API server! Response: {}", content);
                    setApiServerInUse(getNextApiServer());
                } else if (content.contains("\"message\":\"Request Failed")
                        && content.contains("Invalid session cookie")) {
                    throw new PostToAPIException("Invalid session cookie");
                } else {
                    String contentChomped = content.trim();
                    logger.trace("Response body: {}", content);
                    return gson.fromJson(contentChomped, jsonClass);
                }
            } else {
                logger.debug("Failed to send POST, Http status code: {}", response.getStatus());
            }
        }
        throw new PostToAPIException("Failed to POST to API");
    }

    private int postVerisureAPI(String urlString, String data) {
        String url;
        if (urlString.contains("https://mypages")) {
            url = urlString;
        } else {
            url = apiServerInUse + urlString;
        }

        for (int cnt = 0; cnt < APISERVERLIST.size(); cnt++) {
            try {
                ContentResponse response = postVerisureAPI(url, data, Boolean.FALSE);
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
                } else if (httpStatus == HttpStatus.BAD_REQUEST_400) {
                    setApiServerInUse(getNextApiServer());
                    url = apiServerInUse + urlString;
                } else {
                    logger.debug("Failed to send POST, Http status code: {}", response.getStatus());
                }
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                logger.warn("Failed to send a POST to the API {}", e.getMessage());
            }
        }
        return HttpStatus.SERVICE_UNAVAILABLE_503;
    }

    private int setSessionCookieAuthLogin() throws ExecutionException, InterruptedException, TimeoutException {
        // URL to set status which will give us 2 cookies with username and password used for the session
        String url = STATUS;
        ContentResponse response = httpClient.GET(url);
        logTraceWithPattern(response.getStatus(), response.getContentAsString());

        url = AUTH_LOGIN;
        int httpStatusCode = postVerisureAPI(url, "empty");
        analyzeCookies();

        // return response.getStatus();
        return httpStatusCode;
    }

    private boolean getInstallations() {
        int httpResultCode = 0;

        try {
            httpResultCode = setSessionCookieAuthLogin();
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.warn("Failed to set session cookie {}", e.getMessage());
            return false;
        }

        if (httpResultCode == HttpStatus.OK_200) {
            String url = START_GRAPHQL;

            String queryQLAccountInstallations = "[{\"operationName\":\"AccountInstallations\",\"variables\":{\"email\":\""
                    + userName
                    + "\"},\"query\":\"query AccountInstallations($email: String!) {\\n  account(email: $email) {\\n    owainstallations {\\n      giid\\n      alias\\n      type\\n      subsidiary\\n      dealerId\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}]";
            try {
                VerisureInstallationsDTO installations = postJSONVerisureAPI(url, queryQLAccountInstallations,
                        VerisureInstallationsDTO.class);
                logger.debug("Installation: {}", installations.toString());
                List<Owainstallation> owaInstList = installations.getData().getAccount().getOwainstallations();
                boolean pinCodesMatchInstallations = true;
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
                            logger.debug("Setting configured pincode index[{}] to installation ID {}", pinCodeIndex,
                                    installationId);
                        }
                        verisureInstallations.put(new BigDecimal(installationId), vInst);
                    } else {
                        logger.warn("Failed to get alias and/or giid");
                        return false;
                    }
                }
            } catch (ExecutionException | InterruptedException | TimeoutException | JsonSyntaxException
                    | PostToAPIException e) {
                logger.warn("Failed to send a POST to the API {}", e.getMessage());
            }
        } else {
            logger.warn("Failed to set session cookie and auth login, HTTP result code: {}", httpResultCode);
            return false;
        }
        return true;
    }

    private synchronized boolean logIn() {
        try {
            if (!areWeLoggedIn()) {
                vid = "";
                vsAccess = "";
                logger.debug("Attempting to log in to {}, remove all cookies to ensure a fresh session", BASE_URL);
                URI authUri = new URI(BASE_URL);
                CookieStore store = httpClient.getCookieStore();
                store.get(authUri).forEach(cookie -> {
                    store.remove(authUri, cookie);
                });

                String url = AUTH_LOGIN;
                int httpStatusCode = postVerisureAPI(url, "empty");
                analyzeCookies();
                if (!vsStepup.isEmpty()) {
                    logger.warn("MFA is activated on this user! Not supported by binding!");
                    return false;
                }

                url = LOGON_SUF;
                logger.debug("Login URL: {}", url);
                httpStatusCode = postVerisureAPI(url, authstring);
                if (httpStatusCode != HttpStatus.OK_200) {
                    logger.debug("Failed to login, HTTP status code: {}", httpStatusCode);
                    return false;
                }
                return true;
            } else {
                return true;
            }
        } catch (ExecutionException | InterruptedException | TimeoutException | URISyntaxException
                | HttpResponseException e) {
            logger.warn("Failed to login {}", e.getMessage());
        }
        return false;
    }

    private <T extends VerisureThingDTO> void notifyListeners(T thing) {
        deviceStatusListeners.forEach(listener -> {
            if (listener.getVerisureThingClass().equals(thing.getClass())) {
                listener.onDeviceStateChanged(thing);
            }
        });
    }

    private void notifyListenersIfChanged(VerisureThingDTO thing, VerisureInstallation installation, String deviceId) {
        String normalizedDeviceId = VerisureThingConfiguration.normalizeDeviceId(deviceId);
        thing.setDeviceId(normalizedDeviceId);
        thing.setSiteId(installation.getInstallationId());
        thing.setSiteName(installation.getInstallationName());
        VerisureThingDTO oldObj = verisureThings.get(normalizedDeviceId);
        if (!thing.equals(oldObj)) {
            verisureThings.put(thing.getDeviceId(), thing);
            notifyListeners(thing);
        } else {
            logger.trace("No need to notify listeners for thing: {}", thing);
        }
    }

    private boolean updateStatus() {
        logger.debug("Update status");
        for (Map.Entry<BigDecimal, VerisureInstallation> verisureInstallations : verisureInstallations.entrySet()) {
            VerisureInstallation installation = verisureInstallations.getValue();
            try {
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
                    logger.debug("Failed to set session cookie and auth login, HTTP result code: {}", httpResultCode);
                    return false;
                }
            } catch (ExecutionException | InterruptedException | TimeoutException | PostToAPIException e) {
                logger.debug("Failed to update status {}", e.getMessage());
                return false;
            }
        }
        return true;
    }

    private String createOperationJSON(String operation, VariablesDTO variables, String query) {
        OperationDTO operationJSON = new OperationDTO();
        operationJSON.setOperationName(operation);
        operationJSON.setVariables(variables);
        operationJSON.setQuery(query);
        return gson.toJson(Collections.singletonList(operationJSON));
    }

    private synchronized void updateAlarmStatus(VerisureInstallation installation) throws PostToAPIException {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "ArmState";
        VariablesDTO variables = new VariablesDTO();
        variables.setGiid(installationId.toString());
        String query = "query " + operation
                + "($giid: String!) {\n  installation(giid: $giid) {\n armState {\n type\n statusType\n date\n name\n changedVia\n allowedForFirstLine\n allowed\n errorCodes {\n value\n message\n __typename\n}\n __typename\n}\n __typename\n}\n}\n";

        String queryQLAlarmStatus = createOperationJSON(operation, variables, query);
        logger.debug("Quering API for alarm status!");
        try {
            VerisureThingDTO thing = postJSONVerisureAPI(url, queryQLAlarmStatus, VerisureAlarmsDTO.class);
            logger.debug("REST Response ({})", thing);
            // Set unique deviceID
            String deviceId = "alarm" + installationId;
            thing.setDeviceId(deviceId);
            notifyListenersIfChanged(thing, installation, deviceId);
        } catch (ExecutionException | InterruptedException | TimeoutException | JsonSyntaxException e) {
            logger.warn("Failed to send a POST to the API {}", e.getMessage());
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

        try {
            VerisureSmartLocksDTO thing = postJSONVerisureAPI(url, queryQLSmartLock, VerisureSmartLocksDTO.class);
            logger.debug("REST Response ({})", thing);
            List<VerisureSmartLocksDTO.Doorlock> doorLockList = thing.getData().getInstallation().getDoorlocks();
            if (doorLockList != null) {
                doorLockList.forEach(doorLock -> {
                    VerisureSmartLocksDTO slThing = new VerisureSmartLocksDTO();
                    VerisureSmartLocksDTO.Installation inst = new VerisureSmartLocksDTO.Installation();
                    inst.setDoorlocks(Collections.singletonList(doorLock));
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
                        try {
                            VerisureSmartLockDTO smartLockThing = getJSONVerisureAPI(
                                    SMARTLOCK_PATH + slThing.getDeviceId(), VerisureSmartLockDTO.class);
                            logger.debug("REST Response ({})", smartLockThing);
                            slThing.setSmartLockJSON(smartLockThing);
                        } catch (ExecutionException | InterruptedException | TimeoutException | JsonSyntaxException e) {
                            logger.warn("Failed to query for smartlock status: {}", e.getMessage());
                        }
                        notifyListenersIfChanged(slThing, installation, deviceId);
                    }
                });
            }
        } catch (ExecutionException | InterruptedException | TimeoutException | JsonSyntaxException
                | PostToAPIException e) {
            logger.warn("Failed to send a POST to the API {}", e.getMessage());
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

        try {
            VerisureSmartPlugsDTO thing = postJSONVerisureAPI(url, queryQLSmartPlug, VerisureSmartPlugsDTO.class);
            logger.debug("REST Response ({})", thing);
            List<VerisureSmartPlugsDTO.Smartplug> smartPlugList = thing.getData().getInstallation().getSmartplugs();
            if (smartPlugList != null) {
                smartPlugList.forEach(smartPlug -> {
                    VerisureSmartPlugsDTO spThing = new VerisureSmartPlugsDTO();
                    VerisureSmartPlugsDTO.Installation inst = new VerisureSmartPlugsDTO.Installation();
                    inst.setSmartplugs(Collections.singletonList(smartPlug));
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
            }
        } catch (ExecutionException | InterruptedException | TimeoutException | JsonSyntaxException
                | PostToAPIException e) {
            logger.warn("Failed to send a POST to the API {}", e.getMessage());
        }
    }

    private @Nullable VerisureBatteryStatusDTO getBatteryStatus(String deviceId,
            VerisureBatteryStatusDTO @Nullable [] batteryStatus) {
        if (batteryStatus != null) {
            for (VerisureBatteryStatusDTO verisureBatteryStatusDTO : batteryStatus) {
                String id = verisureBatteryStatusDTO.getId();
                if (id != null && id.equals(deviceId)) {
                    return verisureBatteryStatusDTO;
                }
            }
        }
        return null;
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

        try {
            VerisureClimatesDTO thing = postJSONVerisureAPI(url, queryQLClimates, VerisureClimatesDTO.class);
            logger.debug("REST Response ({})", thing);
            List<VerisureClimatesDTO.Climate> climateList = thing.getData().getInstallation().getClimates();
            if (climateList != null) {
                climateList.forEach(climate -> {
                    // If thing is Mouse detection device, then skip it, but fetch temperature from it
                    String type = climate.getDevice().getGui().getLabel();
                    if ("MOUSE".equals(type)) {
                        logger.debug("Mouse detection device!");
                        String deviceId = climate.getDevice().getDeviceLabel();
                        if (deviceId != null) {
                            deviceId = VerisureThingConfiguration.normalizeDeviceId(deviceId);
                            VerisureThingDTO mouseThing = verisureThings.get(deviceId);
                            if (mouseThing instanceof VerisureMiceDetectionDTO) {
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
                    inst.setClimates(Collections.singletonList(climate));
                    VerisureClimatesDTO.Data data = new VerisureClimatesDTO.Data();
                    data.setInstallation(inst);
                    cThing.setData(data);
                    // Set unique deviceID
                    String deviceId = climate.getDevice().getDeviceLabel();
                    if (deviceId != null) {
                        try {
                            VerisureBatteryStatusDTO[] batteryStatusThingArray = getJSONVerisureAPI(BATTERY_STATUS,
                                    VerisureBatteryStatusDTO[].class);
                            VerisureBatteryStatusDTO batteryStatus = getBatteryStatus(deviceId,
                                    batteryStatusThingArray);
                            if (batteryStatus != null) {
                                logger.debug("REST Response ({})", batteryStatus);
                                cThing.setBatteryStatus(batteryStatus);
                            }
                        } catch (ExecutionException | InterruptedException | TimeoutException | JsonSyntaxException e) {
                            logger.debug("Failed to query for battery status: {}", e.getMessage());
                        }
                        // Set location
                        cThing.setLocation(climate.getDevice().getArea());
                        notifyListenersIfChanged(cThing, installation, deviceId);
                    }
                });
            }
        } catch (ExecutionException | InterruptedException | TimeoutException | JsonSyntaxException
                | PostToAPIException e) {
            logger.warn("Failed to send a POST to the API {}", e.getMessage());
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

        try {
            VerisureDoorWindowsDTO thing = postJSONVerisureAPI(url, queryQLDoorWindow, VerisureDoorWindowsDTO.class);
            logger.debug("REST Response ({})", thing);
            List<VerisureDoorWindowsDTO.DoorWindow> doorWindowList = thing.getData().getInstallation().getDoorWindows();
            if (doorWindowList != null) {
                doorWindowList.forEach(doorWindow -> {
                    VerisureDoorWindowsDTO dThing = new VerisureDoorWindowsDTO();
                    VerisureDoorWindowsDTO.Installation inst = new VerisureDoorWindowsDTO.Installation();
                    inst.setDoorWindows(Collections.singletonList(doorWindow));
                    VerisureDoorWindowsDTO.Data data = new VerisureDoorWindowsDTO.Data();
                    data.setInstallation(inst);
                    dThing.setData(data);
                    // Set unique deviceID
                    String deviceId = doorWindow.getDevice().getDeviceLabel();
                    if (deviceId != null) {
                        try {
                            VerisureBatteryStatusDTO[] batteryStatusThingArray = getJSONVerisureAPI(BATTERY_STATUS,
                                    VerisureBatteryStatusDTO[].class);
                            VerisureBatteryStatusDTO batteryStatus = getBatteryStatus(deviceId,
                                    batteryStatusThingArray);
                            if (batteryStatus != null) {
                                logger.debug("REST Response ({})", batteryStatus);
                                dThing.setBatteryStatus(batteryStatus);
                            }
                        } catch (ExecutionException | InterruptedException | TimeoutException | JsonSyntaxException e) {
                            logger.warn("Failed to query for door&window status: {}", e.getMessage());
                        }
                        // Set location
                        dThing.setLocation(doorWindow.getDevice().getArea());
                        notifyListenersIfChanged(dThing, installation, deviceId);
                    }
                });
            }
        } catch (ExecutionException | InterruptedException | TimeoutException | JsonSyntaxException
                | PostToAPIException e) {
            logger.warn("Failed to send a POST to the API {}", e.getMessage());
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

        try {
            VerisureThingDTO thing = postJSONVerisureAPI(url, queryQLBroadbandConnection,
                    VerisureBroadbandConnectionsDTO.class);
            logger.debug("REST Response ({})", thing);
            // Set unique deviceID
            String deviceId = "bc" + installationId;
            notifyListenersIfChanged(thing, inst, deviceId);
        } catch (ExecutionException | InterruptedException | TimeoutException | JsonSyntaxException
                | PostToAPIException e) {
            logger.warn("Failed to send a POST to the API {}", e.getMessage());
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

        try {
            VerisureUserPresencesDTO thing = postJSONVerisureAPI(url, queryQLUserPresence,
                    VerisureUserPresencesDTO.class);
            logger.debug("REST Response ({})", thing);
            List<VerisureUserPresencesDTO.UserTracking> userTrackingList = thing.getData().getInstallation()
                    .getUserTrackings();
            if (userTrackingList != null) {
                userTrackingList.forEach(userTracking -> {
                    String localUserTrackingStatus = userTracking.getStatus();
                    if ("ACTIVE".equals(localUserTrackingStatus)) {
                        VerisureUserPresencesDTO upThing = new VerisureUserPresencesDTO();
                        VerisureUserPresencesDTO.Installation inst = new VerisureUserPresencesDTO.Installation();
                        inst.setUserTrackings(Collections.singletonList(userTracking));
                        VerisureUserPresencesDTO.Data data = new VerisureUserPresencesDTO.Data();
                        data.setInstallation(inst);
                        upThing.setData(data);
                        // Set unique deviceID
                        String deviceId = "up" + userTracking.getWebAccount() + installationId;
                        notifyListenersIfChanged(upThing, installation, deviceId);
                    }
                });
            }
        } catch (ExecutionException | InterruptedException | TimeoutException | JsonSyntaxException
                | PostToAPIException e) {
            logger.warn("Failed to send a POST to the API {}", e.getMessage());
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

        try {
            VerisureMiceDetectionDTO thing = postJSONVerisureAPI(url, queryQLMiceDetection,
                    VerisureMiceDetectionDTO.class);
            logger.debug("REST Response ({})", thing);
            List<VerisureMiceDetectionDTO.Mouse> miceList = thing.getData().getInstallation().getMice();
            if (miceList != null) {
                miceList.forEach(mouse -> {
                    VerisureMiceDetectionDTO miceThing = new VerisureMiceDetectionDTO();
                    VerisureMiceDetectionDTO.Installation inst = new VerisureMiceDetectionDTO.Installation();
                    inst.setMice(Collections.singletonList(mouse));
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
            }
        } catch (ExecutionException | InterruptedException | TimeoutException | JsonSyntaxException
                | PostToAPIException e) {
            logger.warn("Failed to send a POST to the API {}", e.getMessage());
        }
    }

    private synchronized void updateEventLogStatus(VerisureInstallation installation) {
        BigDecimal installationId = installation.getInstallationId();
        String url = START_GRAPHQL;
        String operation = "EventLog";
        int offset = 0;
        int numberOfEvents = this.numberOfEvents;
        List<String> eventCategories = new ArrayList<>(Arrays.asList("INTRUSION", "FIRE", "SOS", "WATER", "ANIMAL",
                "TECHNICAL", "WARNING", "ARM", "DISARM", "LOCK", "UNLOCK", "PICTURE", "CLIMATE", "CAMERA_SETTINGS",
                "DOORWINDOW_STATE_OPENED", "DOORWINDOW_STATE_CLOSED", "USERTRACKING"));
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

        try {
            VerisureEventLogDTO thing = postJSONVerisureAPI(url, queryQLEventLog, VerisureEventLogDTO.class);
            logger.debug("REST Response ({})", thing);
            // Set unique deviceID
            String deviceId = "el" + installationId;
            notifyListenersIfChanged(thing, installation, deviceId);
        } catch (ExecutionException | InterruptedException | TimeoutException | JsonSyntaxException
                | PostToAPIException e) {
            logger.warn("Failed to send a POST to the API {}", e.getMessage());
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

        try {
            VerisureGatewayDTO thing = postJSONVerisureAPI(url, queryQLEventLog, VerisureGatewayDTO.class);
            logger.debug("REST Response ({})", thing);
            // Set unique deviceID
            List<CommunicationState> communicationStateList = thing.getData().getInstallation().getCommunicationState();
            if (communicationStateList != null) {
                if (!communicationStateList.isEmpty()) {
                    String deviceId = communicationStateList.get(0).getDevice().getDeviceLabel();
                    if (deviceId != null) {
                        notifyListenersIfChanged(thing, installation, deviceId);
                    }
                }
            }
        } catch (ExecutionException | InterruptedException | TimeoutException | JsonSyntaxException
                | PostToAPIException e) {
            logger.warn("Failed to send a POST to the API {}", e.getMessage());
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
        private boolean hideNotifications;
        @SuppressWarnings("unused")
        private int offset;
        @SuppressWarnings("unused")
        private int pagesize;
        @SuppressWarnings("unused")
        private @Nullable List<String> eventCategories = null;
        @SuppressWarnings("unused")
        private @Nullable String giid;

        public void setHideNotifications(boolean hideNotifications) {
            this.hideNotifications = hideNotifications;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public void setPagesize(int pagesize) {
            this.pagesize = pagesize;
        }

        public void setEventCategories(List<String> eventCategories) {
            this.eventCategories = eventCategories;
        }

        public void setGiid(String giid) {
            this.giid = giid;
        }
    }

    private class PostToAPIException extends Exception {

        private static final long serialVersionUID = 1L;

        public PostToAPIException(String message) {
            super(message);
        }
    }
}
