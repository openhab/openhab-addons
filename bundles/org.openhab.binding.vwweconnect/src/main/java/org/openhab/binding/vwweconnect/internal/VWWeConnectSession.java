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
package org.openhab.binding.vwweconnect.internal;

import static org.openhab.binding.vwweconnect.internal.VWWeConnectBindingConstants.*;

import java.math.BigDecimal;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openhab.binding.vwweconnect.internal.model.BaseVehicle;
import org.openhab.binding.vwweconnect.internal.model.Details;
import org.openhab.binding.vwweconnect.internal.model.EManager;
import org.openhab.binding.vwweconnect.internal.model.HeaterStatus;
import org.openhab.binding.vwweconnect.internal.model.Location;
import org.openhab.binding.vwweconnect.internal.model.Status;
import org.openhab.binding.vwweconnect.internal.model.Trips;
import org.openhab.binding.vwweconnect.internal.model.Vehicle;
import org.openhab.binding.vwweconnect.internal.model.Vehicle.CompleteVehicleJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * This class performs the communication with VW WeConnect Portal API.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VWWeConnectSession {

    private final HashMap<String, BaseVehicle> vwWeConnectThings = new HashMap<String, BaseVehicle>();
    private final Logger logger = LoggerFactory.getLogger(VWWeConnectSession.class);
    private final Gson gson = new GsonBuilder().create();
    private final List<DeviceStatusListener> deviceStatusListeners = new CopyOnWriteArrayList<>();

    private @Nullable String csrf;
    private @Nullable String xCsrfToken;
    private @Nullable String referer;
    private @Nullable String authRefUrl;
    private @Nullable String guestLanguageId;

    private @Nullable String securePIN;
    private HttpClient httpClient;
    private @Nullable String userName = "";
    private @Nullable String password = "";

    private static final int RETRIES = 3;
    private static int count = 0;

    public VWWeConnectSession(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void initialize(@Nullable String userName, @Nullable String password, @Nullable String pinCode) {
        logger.debug("VWWeConnectSession:initialize");
        this.userName = userName;
        this.password = password;
        this.securePIN = pinCode;
    }

    public boolean refresh() {
        logger.debug("VWWeConnectSession:refresh");
        if (logIn()) {
            if (updateStatus()) {
                return true;
            }
        }
        return false;
    }

    public boolean requestVehicleStatus() {
        logger.debug("VWWeConnectSession:getVehicleStatus");

        if (logIn()) {
            if (getVehicleStatus()) {
                return true;
            }
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

    public @Nullable BaseVehicle getVWWeConnectThing(String vin) {
        return vwWeConnectThings.get(vin);
    }

    public HashMap<String, BaseVehicle> getVWWeConnectThings() {
        return vwWeConnectThings;
    }

    public @Nullable String getCsrf() {
        return csrf;
    }

    public @Nullable String getPinCode() {
        return securePIN;
    }

    public @Nullable String getPinCode(@Nullable BigDecimal installationId) {
        return securePIN;
    }

    public @Nullable ContentResponse sendCommand(String url, String data) {
        logger.debug("Sending command: {}", url);
        return postJSONVWWeConnectAPI(url, data, referer, xCsrfToken);
    }

    public @Nullable ContentResponse sendCommand(String url, @Nullable Fields fields) {
        logger.debug("Sending command: {}", url);
        return postJSONVWWeConnectAPI(url, fields, referer, xCsrfToken);
    }

    public @Nullable <T> T convertFromJSON(String json, Class<T> jsonClass) {
        return gson.fromJson(json, jsonClass);
    }

    public boolean isErrorCode(@Nullable String jsonContent) {
        if (jsonContent != null && !jsonContent.equals("")) {
            String errorCode = JsonPath.read(jsonContent, "$.errorCode");
            logger.debug("ErrorCode: {}", errorCode);
            if (errorCode.equals("0")) {
                return false;
            } else {
                try {
                    if (errorCode.equals("1")) {
                        String errorType = JsonPath.read(jsonContent, "$.errorType");
                        if (errorType.equals("429")) {
                            logger.debug(
                                    "Too many requests, switch ignition on/off to be able to communicate fully to vehicle");
                        }
                    } else if (errorCode.equals("2")) {
                        String errorTextCopybookId = JsonPath.read(jsonContent, "$.errorTextCopybookId");
                        logger.debug("Error: {}", errorTextCopybookId);
                    }
                } catch (PathNotFoundException e) {
                }
            }
        }
        return true;
    }

    private boolean areWeLoggedIn() {
        logger.debug("areWeLoggedIn() - Checking if we are logged in");
        if (xCsrfToken == null) {
            return false;
        }
        String url = authRefUrl + LOGIN_CHECK;

        logger.debug("Check for login status, url: {}", url);
        Fields fields = null;
        ContentResponse httpResponse = postJSONVWWeConnectAPI(url, fields, referer, xCsrfToken);
        if (httpResponse == null) {
            logger.debug("We are not logged in, Exception caught!");
            return false;
        }
        String content = httpResponse.getContentAsString();
        switch (httpResponse.getStatus()) {
            case HttpStatus.OK_200:
                if (!isErrorCode(content)) {
                    return true;
                }
            default:
                logger.debug("Status code {} body {}", httpResponse.getStatus(), content);
                break;
        }
        return false;
    }

    private @Nullable ContentResponse getVWWeConnectAPI(String url) {
        logger.debug("getVWWeConnectAPI: {}", url);
        ContentResponse httpResponse = null;

        try {
            httpResponse = httpClient.GET(url);
            if (logger.isTraceEnabled()) {
                String pattern = "(?m)^\\s*\\r?\\n|\\r?\\n\\s*(?!.*\\r?\\n)";
                String replacement = "";
                logger.trace("HTTP Response ({}) Body:{}", httpResponse.getStatus(),
                        httpResponse.getContentAsString().replaceAll(pattern, replacement));
            }
            return httpResponse;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.warn("Caught Exception {} for URL string {}", e, url);
        }
        return httpResponse;
    }

    private @Nullable ContentResponse getVWWeConnectAPI(String url, Boolean headers) {
        try {
            logger.debug("getVWWeConnectAPI URL: {} headers: {}", url, headers);
            ContentResponse httpResponse = null;
            String correctEncodedURL = url.replace(" ", "%20");
            logger.debug("Encoded URL: {}", correctEncodedURL);

            httpClient.setFollowRedirects(false);
            Request request = httpClient.newRequest(correctEncodedURL).method(HttpMethod.GET);
            if (headers) {
                request.header("accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
                request.header("content-type", "application/x-www-form-urlencoded");
                request.header("user-agent",
                        "Mozilla/5.0 (Linux; Android 6.0.1; D5803 Build/23.5.A.1.291; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/63.0.3239.111 Mobile Safari/537.36");
            }
            logger.debug("HTTP GET Request {}.", request);
            httpResponse = request.send();
            if (logger.isTraceEnabled()) {
                String pattern = "(?m)^\\s*\\r?\\n|\\r?\\n\\s*(?!.*\\r?\\n)";
                String replacement = "";
                logger.trace("HTTP Response ({}) Body:{}", httpResponse.getStatus(),
                        httpResponse.getContentAsString().replaceAll(pattern, replacement));
            }
            return httpResponse;
        } catch (ExecutionException | InterruptedException | TimeoutException | RuntimeException e) {
            logger.warn("Caught Exception {}", e.getMessage(), e);
        }
        return null;
    }

    private @Nullable ContentResponse postVWWeConnectAPI(String url, @Nullable Fields fields, @Nullable String referer,
            String csrf) {
        try {
            logger.debug("postVWWeConnectAPI URL: {} Fields: {} Referer: {} CSRF:{}", url, fields, referer, csrf);
            ContentResponse httpResponse = null;
            Request request = httpClient.newRequest(url).method(HttpMethod.POST);

            request.header("accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
            request.header("content-type", "application/x-www-form-urlencoded");
            request.header("user-agent",
                    "Mozilla/5.0 (Linux; Android 6.0.1; D5803 Build/23.5.A.1.291; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/63.0.3239.111 Mobile Safari/537.36");
            request.header("referer", SESSION_BASE + referer);

            if (fields != null) {
                fields.forEach(f -> request.param(f.getName(), f.getValue()));
            } else {
                request.header("x-csrf-token", csrf);
            }
            logger.debug("HTTP POST Request {}.", request.toString());
            httpResponse = request.send();
            if (logger.isTraceEnabled()) {
                logger.trace("HTTP Response ({}) Body:{}", httpResponse.getStatus(),
                        httpResponse.getContentAsString().replaceAll("\n+", "\n"));
            }
            return httpResponse;
        } catch (ExecutionException | InterruptedException | TimeoutException | RuntimeException e) {
            logger.warn("Caught Exception {}", e.getMessage(), e);
        }
        return null;
    }

    private @Nullable ContentResponse postJSONVWWeConnectAPI(String url, @Nullable Fields fields,
            @Nullable String referer, @Nullable String xCsrf) {
        int count = 0;
        while (true) {
            try {
                Thread.sleep(2 * SLEEP_TIME_MILLIS);
                logger.debug("postJSONVWWeConnectAPI URL: {} Fields: {} Referer: {} XCSRF:{}", url, fields, referer,
                        xCsrf);
                ContentResponse httpResponse = null;
                Request request = httpClient.newRequest(url).method(HttpMethod.POST);

                request.header("accept", "application/json, text/plain, */*");
                request.header("content-type", "application/json;charset=UTF-8");
                request.header("user-agent",
                        "Mozilla/5.0 (Linux; Android 6.0.1; D5803 Build/23.5.A.1.291; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/63.0.3239.111 Mobile Safari/537.36");
                request.header("referer", referer);
                request.header("x-csrf-token", xCsrf);

                if (fields != null) {
                    fields.forEach(f -> request.param(f.getName(), f.getValue()));
                }

                logger.debug("HTTP POST Request {}.", request.toString());
                httpResponse = request.send();

                if (logger.isTraceEnabled()) {
                    logger.trace("HTTP Response ({}) Body:{}", httpResponse.getStatus(),
                            httpResponse.getContentAsString().replaceAll("\n+", "\n"));
                }
                return httpResponse;
            } catch (ExecutionException e) {
                if (count <= RETRIES) {
                    logger.warn("Caught 3 consecutive ExecutionExceptions {}", e.getMessage(), e);
                    break;
                } else {
                    logger.debug("Retry POST due to ExecutionException, try #: {}", count);
                    count++;
                }
                logger.warn("Caught ExecutionException {}", e.getMessage(), e);
            } catch (InterruptedException | TimeoutException | RuntimeException e) {
                logger.warn("Caught Exception {}", e.getMessage(), e);
                break;
            }
        }
        return null;
    }

    private @Nullable ContentResponse postJSONVWWeConnectAPI(String url, String data, @Nullable String referer,
            @Nullable String xCsrf) {
        try {
            logger.debug("postJSONVWWeConnectAPI URL: {} Data: {} Referer: {} XCSRF:{}", url, data, referer, xCsrf);
            ContentResponse httpResponse = null;
            Request request = httpClient.newRequest(url).method(HttpMethod.POST);

            request.header("accept", "application/json, text/plain, */*");
            request.header("content-type", "application/json;charset=UTF-8");
            request.header("user-agent",
                    "Mozilla/5.0 (Linux; Android 6.0.1; D5803 Build/23.5.A.1.291; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/63.0.3239.111 Mobile Safari/537.36");
            request.header("referer", referer);
            request.header("x-csrf-token", xCsrf);

            if (!data.equals("empty")) {
                request.content(new BytesContentProvider(data.getBytes(StandardCharsets.UTF_8)),
                        "application/x-www-form-urlencoded; charset=UTF-8");
            }
            logger.debug("HTTP POST Request {}.", request.toString());
            httpResponse = request.send();
            if (logger.isTraceEnabled()) {
                logger.trace("HTTP Response ({}) Body:{}", httpResponse.getStatus(),
                        httpResponse.getContentAsString().replaceAll("\n+", "\n"));
            }
            return httpResponse;
        } catch (ExecutionException | InterruptedException | TimeoutException | RuntimeException e) {
            logger.warn("Caught Exception {}", e.getMessage(), e);
        }
        return null;
    }

    private @Nullable <T> T postJSONVWWeConnectAPI(String url, @Nullable Fields fields, Class<T> jsonClass) {
        String content = null;
        try {
            logger.debug("postJSONVWWeConnectAPI URL: {} Fields: {}", url, fields);
            Request request = httpClient.newRequest(url).method(HttpMethod.POST);

            request.header("accept", "application/json, text/plain, */*");
            request.header("content-type", "application/json;charset=UTF-8");
            request.header("user-agent",
                    "Mozilla/5.0 (Linux; Android 6.0.1; D5803 Build/23.5.A.1.291; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/63.0.3239.111 Mobile Safari/537.36");
            request.header("referer", referer);
            request.header("x-csrf-token", xCsrfToken);

            if (fields != null) {
                fields.forEach(f -> request.param(f.getName(), f.getValue()));
            }

            logger.debug("HTTP POST Request {}.", request.toString());
            ContentResponse httpResponse = request.send();
            if (httpResponse != null) {
                content = httpResponse.getContentAsString();
                logger.trace("Http response JSON: {}", content);
                if (isErrorCode(content)) {
                    logger.warn("Error code or JSON is null or empty on POST: url {}, response {}", url, content);
                    return null;
                }
                return gson.fromJson(content, jsonClass);
            }
        } catch (ExecutionException | InterruptedException | TimeoutException | RuntimeException e) {
            logger.warn("Caught Exception {}", e.getMessage(), e);
        }
        return null;
    }

    private Fields getFields(@Nullable String email, @Nullable String password, String loginToken, String loginHmac,
            String loginCsrf) {
        Fields fields = new Fields(true);
        fields.put("email", email);
        fields.put("password", password);
        fields.put("relayState", loginToken);
        fields.put("hmac", loginHmac);
        fields.put("_csrf", loginCsrf);
        return fields;
    }

    private boolean checkHttpResponse200(@Nullable ContentResponse httpResponse) {
        if (httpResponse == null) {
            logger.debug("Failed to login, Exception caught!");
            return false;
        } else if (httpResponse.getStatus() != HttpStatus.OK_200) {
            logger.debug("Failed to login, HTTP status code: {}", httpResponse.getStatus());
            return false;
        }
        return true;
    }

    private boolean checkHttpResponse302(@Nullable ContentResponse httpResponse) {
        if (httpResponse == null) {
            logger.debug("Failed to login, Exception caught!");
            return false;
        } else if (httpResponse.getStatus() != HttpStatus.MOVED_TEMPORARILY_302) {
            logger.debug("HTTP status code: {}", httpResponse.getStatus());
            return false;
        }
        return true;
    }

    private boolean checkHttpResponse303(@Nullable ContentResponse httpResponse) {
        if (httpResponse == null) {
            logger.debug("Failed to login, Exception caught!");
            return false;
        } else if (httpResponse.getStatus() != HttpStatus.SEE_OTHER_303) {
            logger.debug("Failed to login, HTTP status code: {}", httpResponse.getStatus());
            return false;
        }
        return true;
    }

    private synchronized boolean logIn() {
        if (!areWeLoggedIn()) {
            logger.info("Attempting to log in to https://www.portal.volkswagen-we.com");
            boolean isRedirectLogin = false;
            Document htmlDocument = null;
            Element nameInput = null;
            // Request landing page and get CSRF
            String url = SESSION_BASE + REQUEST_LANDING_PAGE;
            logger.debug("Login URL: {}", url);
            ContentResponse httpResponse = getVWWeConnectAPI(url);
            if (httpResponse != null && !checkHttpResponse200(httpResponse)) {
                if (checkHttpResponse302(httpResponse)) {
                    url = httpResponse.getHeaders().get("location");
                    logger.debug("Redirection to: {}", url);
                    httpResponse = getVWWeConnectAPI(url);
                    if (httpResponse != null && !checkHttpResponse200(httpResponse)) {
                        if (checkHttpResponse302(httpResponse)) {
                            url = httpResponse.getHeaders().get("location");
                            logger.debug("Redirection to: {}", url);
                            httpResponse = getVWWeConnectAPI(url);
                            if (checkHttpResponse302(httpResponse)) {
                                logger.debug("Redirect login!");
                                isRedirectLogin = true;
                            } else if (checkHttpResponse200(httpResponse)) {
                                logger.debug("200 OK, continue to log in! URL: {}", url);
                            } else {
                                logger.debug("Login failed, HTTP response: {}", httpResponse);
                                return false;
                            }
                        } else {
                            logger.debug("Login failed, HTTP response: {}", httpResponse);
                            return false;
                        }
                    }
                }
            }

            if (httpResponse == null) {
                logger.debug("Login failed, HTTP response: {}", httpResponse);
                return false;
            } else {
                if (isRedirectLogin) {
                    url = httpResponse.getHeaders().get("Location");
                    httpResponse = getVWWeConnectAPI(url, Boolean.TRUE);
                    if (httpResponse != null && !checkHttpResponse302(httpResponse)) {
                        logger.debug("Redirect login failed! URL: {}", url);
                        return false;
                    }
                    if (httpResponse != null) {
                        url = httpResponse.getHeaders().get("Location");
                    } else {
                        logger.debug("Login failed, HTTP response: {}", httpResponse);
                        return false;
                    }
                } else {
                    // Parse csrf
                    htmlDocument = Jsoup.parse(httpResponse.getContentAsString());
                    nameInput = htmlDocument.select("meta[name=_csrf]").first();
                    if (nameInput == null) {
                        logger.debug("Login failed, nameInput is null");
                        return false;
                    }
                    String csrf = nameInput.attr("content");
                    logger.trace("Found csrf {}", csrf);

                    // Request login page and get login URL
                    url = SESSION_BASE + GET_LOGIN_URL;
                    httpResponse = postVWWeConnectAPI(url, null, "portal", csrf);
                    if (httpResponse != null && httpResponse.getStatus() == HttpStatus.OK_200) {
                        String json = httpResponse.getContentAsString();
                        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
                        url = jsonObject.get("loginURL").getAsJsonObject().get("path").getAsString();
                    } else if (httpResponse != null && httpResponse.getStatus() == HttpStatus.MOVED_TEMPORARILY_302) {
                        url = httpResponse.getHeaders().get("location");
                        logger.debug("Redirection to: {}", url);
                        httpResponse = postVWWeConnectAPI(url, null, "portal", csrf);
                        if (httpResponse != null) {
                            String json = httpResponse.getContentAsString();
                            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
                            url = jsonObject.get("loginURL").getAsJsonObject().get("path").getAsString();
                        } else {
                            logger.debug("Login failed, HTTP response: {}", httpResponse);
                            return false;
                        }
                    } else {
                        logger.debug("Failed to login, HTTP response: {}", httpResponse);
                        return false;
                    }
                }
            }

            httpResponse = getVWWeConnectAPI(url, Boolean.TRUE);
            if (httpResponse == null || !checkHttpResponse302(httpResponse)) {
                logger.debug("Redirect failed! URL: {}", url);
                return false;
            }

            url = httpResponse.getHeaders().get("Location");
            httpResponse = getVWWeConnectAPI(url, Boolean.TRUE);
            if (httpResponse == null || !checkHttpResponse200(httpResponse)) {
                logger.debug("Redirect failed! URL: {}", url);
                return false;
            }

            String previousUrl = url;

            // Get actual login page and get session id and ViewState
            htmlDocument = Jsoup.parse(httpResponse.getContentAsString());
            nameInput = htmlDocument.select("input[name=_csrf]").first();
            String loginCsrf = nameInput.attr("value");
            logger.trace("Found loginCsrf {}", loginCsrf);
            nameInput = htmlDocument.select("input[name=relayState]").first();
            String loginToken = nameInput.attr("value");
            nameInput = htmlDocument.select("input[name=hmac]").first();
            String loginHmac = nameInput.attr("value");
            Element loginForm = htmlDocument.getElementById("emailPasswordForm");
            url = AUTH_BASE + loginForm.attr("action");

            Fields fields = getFields(userName, password, loginToken, loginHmac, loginCsrf);

            httpResponse = postVWWeConnectAPI(url, fields, previousUrl, loginCsrf);
            if (httpResponse == null || !checkHttpResponse303(httpResponse)) {
                return false;
            }

            url = httpResponse.getHeaders().get("Location");
            url = AUTH_BASE + url;
            httpResponse = getVWWeConnectAPI(url, Boolean.TRUE);
            if (httpResponse == null || !checkHttpResponse200(httpResponse)) {
                return false;
            }

            previousUrl = url;
            htmlDocument = Jsoup.parse(httpResponse.getContentAsString());
            nameInput = htmlDocument.select("input[name=_csrf]").first();
            String authCsrf = nameInput.attr("value");
            logger.trace("Found authCsrf {}", loginCsrf);
            nameInput = htmlDocument.select("input[name=relayState]").first();
            String authToken = nameInput.attr("value");
            nameInput = htmlDocument.select("input[name=hmac]").first();
            String authHmac = nameInput.attr("value");
            Element authForm = htmlDocument.getElementById("credentialsForm");
            if (authForm == null) {
                logger.warn("Failed to login, check your credentials!");
                return false;
            }

            url = AUTH_BASE + authForm.attr("action");
            fields = getFields(userName, password, authToken, authHmac, authCsrf);

            httpResponse = postVWWeConnectAPI(url, fields, previousUrl, authCsrf);
            if (httpResponse == null || !checkHttpResponse302(httpResponse)) {
                return false;
            }

            url = httpResponse.getHeaders().get("Location");
            httpResponse = getVWWeConnectAPI(url, Boolean.TRUE);
            if (httpResponse == null || !checkHttpResponse302(httpResponse)) {
                return false;
            }

            url = httpResponse.getHeaders().get("Location");
            httpResponse = getVWWeConnectAPI(url, Boolean.TRUE);
            if (httpResponse == null || !checkHttpResponse302(httpResponse)) {
                return false;
            }

            url = httpResponse.getHeaders().get("Location");
            httpResponse = getVWWeConnectAPI(url, Boolean.TRUE);
            if (httpResponse == null || !checkHttpResponse302(httpResponse)) {
                return false;
            }

            url = httpResponse.getHeaders().get("Location");
            previousUrl = url;
            URI uri;
            String state = "", code = "", path = "";

            try {
                uri = new URI(url);
                String[] queryStrings = uri.getQuery().split("&");
                for (String query : queryStrings) {
                    String[] nameValuePair = query.split("=");
                    if (nameValuePair[0].equals("state")) {
                        state = nameValuePair[1];
                    } else if (nameValuePair[0].equals("code")) {
                        code = nameValuePair[1];
                    }
                }
                path = uri.getPath();
            } catch (URISyntaxException e) {
                logger.warn("Failed to login, caught URISyntaxException {}", e.getMessage(), e);
                return false;
            }

            fields = new Fields(true);
            fields.put("_33_WAR_cored5portlet_code", code);
            fields.put("_33_WAR_cored5portlet_landingPageUrl", "");

            url = SESSION_BASE + path + "?p_auth=" + state
                    + "&p_p_id=33_WAR_cored5portlet&p_p_lifecycle=1&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_count=1&_33_WAR_cored5portlet_javax.portlet.action=getLoginStatus";

            httpResponse = postVWWeConnectAPI(url, fields, previousUrl, authCsrf);
            if (httpResponse == null || !checkHttpResponse302(httpResponse)) {
                return false;
            }

            url = httpResponse.getHeaders().get("Location");
            httpResponse = getVWWeConnectAPI(url, Boolean.TRUE);
            if (httpResponse == null || !checkHttpResponse200(httpResponse)) {
                return false;
            }

            htmlDocument = Jsoup.parse(httpResponse.getContentAsString());
            nameInput = htmlDocument.select("meta[name=_csrf]").first();
            xCsrfToken = nameInput.attr("content");
            logger.trace("Found xCsrfToken {}", xCsrfToken);
            referer = url;
            authRefUrl = url + "/";

            CookieStore cookieStore = httpClient.getCookieStore();
            List<HttpCookie> cookies = cookieStore.get(URI.create(COOKIESTORE));
            cookies.forEach(cookie -> {
                logger.debug("Cookie: {}", cookie);
                if (cookie.getName().equals("GUEST_LANGUAGE_ID")) {
                    guestLanguageId = cookie.getValue();
                    logger.debug("Fetching guest language id {} from cookie", guestLanguageId);
                }
            });
            return true;
        } else {
            return true;
        }
    }

    private void notifyListeners(BaseVehicle thing) {
        for (DeviceStatusListener listener : deviceStatusListeners) {
            listener.onDeviceStateChanged(thing);
        }
    }

    private boolean updateStatus() {
        logger.debug("VWWeConnectSession:updateStatus");
        return updateVehicleStatus(Vehicle.class);
    }

    private DocumentContext getContext(String content, String path) {
        DocumentContext context = JsonPath.parse(content);
        List<Object> vehicleList = context.read(path);
        return JsonPath.parse(vehicleList);
    }

    private @Nullable DocumentContext getFullyLoadedCars() {
        Fields fields = null;
        String content = null;
        List<Object> vinList = null;
        DocumentContext context = null;

        String url = authRefUrl + GET_FULLY_LOADED_CARS;
        ContentResponse httpResponse = postJSONVWWeConnectAPI(url, fields, referer, xCsrfToken);
        if (httpResponse != null) {
            content = httpResponse.getContentAsString();
            if (isErrorCode(content)) {
                logger.warn("Failed to get fully loaded cars.");
                return null;
            } else {
                // Find all not fully loaded vehicles
                context = getContext(content, VEHICLES_NOT_FULLY_LOADED);
                vinList = context.read("$[*]['vin']");
                if (vinList.size() == 0) {
                    logger.debug("No not fully loaded vehicle(s) found on VW We Connect portal! JSON: {}", content);
                    // Are they already loaded
                    context = getContext(content, COMPLETE_VEHICLES);
                    vinList = context.read("$[*]['vin']");
                    if (vinList.size() == 0) {
                        logger.warn("No fully loaded vehicle(s) yet found on VW We Connect portal! JSON: {}", content);
                        // Sleep, w8 for fully loaded cars
                        try {
                            Thread.sleep(5 * SLEEP_TIME_MILLIS);
                        } catch (InterruptedException e) {
                            logger.warn("Exception caught: {}", e.getMessage(), e);
                        }

                        // Get fully loaded cars
                        url = authRefUrl + GET_FULLY_LOADED_CARS;
                        httpResponse = postJSONVWWeConnectAPI(url, fields, referer, xCsrfToken);
                        if (httpResponse != null) {
                            content = httpResponse.getContentAsString();
                            if (isErrorCode(content)) {
                                logger.warn("Failed to get fully loaded cars!");
                                return null;
                            }
                        } else {
                            context = getContext(content, COMPLETE_VEHICLES);
                        }
                    }
                }
            }
        }
        return context;
    }

    private synchronized boolean updateVehicleStatus(Class<? extends Vehicle> jsonClass) {
        Fields fields = null;
        String content = null;
        String url = null;

        DocumentContext context = getFullyLoadedCars();
        if (context == null) {
            logger.warn("No Fully loaded vehicle(s) found on VW We Connect portal! JSON: {}", content);
            return false;
        }

        List<Object> vinList = context.read("$[*]['vin']");
        List<Object> dashboardUrlList = context.read("$[*]['dashboardUrl']");
        if (vinList == null || vinList.size() == 0 || dashboardUrlList == null) {
            logger.warn("No Fully loaded vehicle(s) found on VW We Connect portal! JSON: {}", content);
            return false;
        }

        // Loop trough all found vehicles
        for (int i = 0; i < vinList.size(); i++) {
            String vin = (String) vinList.get(i);
            String dashboardUrl = (String) dashboardUrlList.get(i);

            // Query API for vehicle details for this VIN
            url = SESSION_BASE + dashboardUrl + VEHICLE_DETAILS + vin;
            Vehicle vehicle = postJSONVWWeConnectAPI(url, fields, Vehicle.class);
            logger.debug("API Response ({})", vehicle);

            // Query API for more specific vehicle details
            url = SESSION_BASE + dashboardUrl + VEHICLE_DETAILS_SPECIFIC;
            Details vehicleDetails = postJSONVWWeConnectAPI(url, fields, Details.class);
            logger.debug("API Response ({})", vehicleDetails);

            // Query API for trip statistics
            url = SESSION_BASE + dashboardUrl + TRIP_STATISTICS;
            Trips trips = postJSONVWWeConnectAPI(url, fields, Trips.class);
            logger.trace("API Response ({})", trips);

            // Query API for homeLocation status
            url = SESSION_BASE + dashboardUrl + VEHICLE_LOCATION;
            Location location = postJSONVWWeConnectAPI(url, fields, Location.class);
            logger.debug("API Response ({})", location);

            // Query API for vehicle status
            url = SESSION_BASE + dashboardUrl + VEHICLE_STATUS;
            Status vehicleStatus = postJSONVWWeConnectAPI(url, fields, Status.class);
            logger.debug("API Response ({})", vehicleStatus);

            EManager eManager = null;
            HeaterStatus vehicleHeaterStatus = null;

            String eManagerJSON;
            /*
             * logger.warn("Count: {}", count);
             * if (count == 0) {
             * eManagerJSON =
             * "{\"errorCode\":\"0\",\"EManager\":{\"rbc\":{\"status\":{\"batteryPercentage\":100,\"chargingState\":\"OFF\",\"chargingRemaningHour\":\"2\",\"chargingRemaningMinute\":\"28\",\"chargingReason\":\"INVALID\",\"pluginState\":\"DISCONNECTED\",\"lockState\":\"UNLOCKED\",\"extPowerSupplyState\":\"UNAVAILABLE\",\"range\":\"7\",\"electricRange\":216,\"combustionRange\":null,\"combinedRange\":216,\"rlzeUp\":false},\"settings\":{\"chargerMaxCurrent\":16,\"maxAmpere\":32,\"maxCurrentReduced\":false}},\"rpc\":{\"status\":{\"climatisationState\":\"OFF\",\"climatisationRemaningTime\":10,\"windowHeatingStateFront\":\"OFF\",\"windowHeatingStateRear\":\"OFF\",\"climatisationReason\":null,\"windowHeatingAvailable\":true},\"settings\":{\"targetTemperature\":\"26\",\"climatisationWithoutHVPower\":true,\"electric\":true},\"climaterActionState\":\"AVAILABLE\",\"auAvailable\":false},\"rdt\":{\"status\":{\"timers\":[{\"timerId\":1,\"timerProfileId\":1,\"timerStatus\":\"NOT_EXPIRED\",\"timerChargeScheduleStatus\":\"IDLE\",\"timerClimateScheduleStatus\":\"IDLE\",\"timerExpStatusTimestamp\":\"11.04.2020\",\"timerProgrammedStatus\":\"NOT_PROGRAMMED\",\"schedule\":{\"type\":2,\"start\":{\"hours\":12,\"minutes\":0},\"end\":{\"hours\":null,\"minutes\":null},\"index\":null,\"daypicker\":[\"Y\",\"Y\",\"Y\",\"Y\",\"Y\",\"Y\",\"Y\"],\"startDateActive\":\"12.04.2020\",\"endDateActive\":null},\"startDateActive\":\"12.04.2020\",\"timeRangeActive\":\"12:00\"},{\"timerId\":2,\"timerProfileId\":1,\"timerStatus\":\"NOT_EXPIRED\",\"timerChargeScheduleStatus\":\"IDLE\",\"timerClimateScheduleStatus\":\"IDLE\",\"timerExpStatusTimestamp\":\"11.04.2020\",\"timerProgrammedStatus\":\"NOT_PROGRAMMED\",\"schedule\":{\"type\":2,\"start\":{\"hours\":12,\"minutes\":0},\"end\":{\"hours\":null,\"minutes\":null},\"index\":null,\"daypicker\":[\"Y\",\"Y\",\"Y\",\"Y\",\"Y\",\"Y\",\"Y\"],\"startDateActive\":\"12.04.2020\",\"endDateActive\":null},\"startDateActive\":\"12.04.2020\",\"timeRangeActive\":\"12:00\"},{\"timerId\":3,\"timerProfileId\":1,\"timerStatus\":\"NOT_EXPIRED\",\"timerChargeScheduleStatus\":\"IDLE\",\"timerClimateScheduleStatus\":\"IDLE\",\"timerExpStatusTimestamp\":\"11.04.2020\",\"timerProgrammedStatus\":\"NOT_PROGRAMMED\",\"schedule\":{\"type\":2,\"start\":{\"hours\":12,\"minutes\":0},\"end\":{\"hours\":null,\"minutes\":null},\"index\":null,\"daypicker\":[\"Y\",\"Y\",\"Y\",\"Y\",\"Y\",\"Y\",\"Y\"],\"startDateActive\":\"12.04.2020\",\"endDateActive\":null},\"startDateActive\":\"12.04.2020\",\"timeRangeActive\":\"12:00\"}],\"profiles\":[{\"profileId\":1,\"profileName\":\"Standard\",\"timeStamp\":\"11.04.2020\",\"charging\":true,\"climatisation\":false,\"targetChargeLevel\":100,\"nightRateActive\":false,\"nightRateTimeStart\":\"23:00\",\"nightRateTimeEnd\":\"23:00\",\"chargeMaxCurrent\":16,\"heaterSource\":\"ELECTRIC\"}]},\"settings\":{\"minChargeLimit\":100,\"lowerLimitMax\":100},\"auxHeatingAllowed\":false,\"auxHeatingEnabled\":false},\"actionPending\":false,\"rdtAvailable\":true}}";
             * count++;
             * } else if (count == 1) {
             * eManagerJSON =
             * " {\"errorCode\":\"0\",\"EManager\":{\"rbc\":{\"status\":{\"batteryPercentage\":100,\"chargingState\":\"CHARGING\",\"chargingRemaningHour\":\"0\",\"chargingRemaningMinute\":\"30\",\"chargingReason\":\"IMMEDIATE\",\"pluginState\":\"CONNECTED\",\"lockState\":\"LOCKED\",\"extPowerSupplyState\":\"AVAILABLE\",\"range\":\"7\",\"electricRange\":32,\"combustionRange\":390,\"combinedRange\":424,\"rlzeUp\":false},\"settings\":{\"chargerMaxCurrent\":16,\"maxAmpere\":16,\"maxCurrentReduced\":false}},\"rpc\":{\"status\":{\"climatisationState\":\"OFF\",\"climatisationRemaningTime\":16,\"windowHeatingStateFront\":\"OFF\",\"windowHeatingStateRear\":\"OFF\",\"climatisationReason\":null,\"windowHeatingAvailable\":true},\"settings\":{\"targetTemperature\":\"21\",\"climatisationWithoutHVPower\":true,\"electric\":true},\"climaterActionState\":\"AVAILABLE\",\"auAvailable\":false},\"rdt\":{\"status\":{\"timers\":[{\"timerId\":1,\"timerProfileId\":2,\"timerStatus\":\"NOT_EXPIRED\",\"timerChargeScheduleStatus\":\"IDLE\",\"timerClimateScheduleStatus\":\"IDLE\",\"timerExpStatusTimestamp\":\"01.05.2020\",\"timerProgrammedStatus\":\"NOT_PROGRAMMED\",\"schedule\":{\"type\":2,\"start\":{\"hours\":16,\"minutes\":30},\"end\":{\"hours\":null,\"minutes\":null},\"index\":null,\"daypicker\":[\"Y\",\"Y\",\"N\",\"Y\",\"Y\",\"N\",\"N\"],\"startDateActive\":\"02.05.2020\",\"endDateActive\":null},\"startDateActive\":\"02.05.2020\",\"timeRangeActive\":\"16:30\"},{\"timerId\":2,\"timerProfileId\":1,\"timerStatus\":\"NOT_EXPIRED\",\"timerChargeScheduleStatus\":\"IDLE\",\"timerClimateScheduleStatus\":\"IDLE\",\"timerExpStatusTimestamp\":\"01.05.2020\",\"timerProgrammedStatus\":\"NOT_PROGRAMMED\",\"schedule\":{\"type\":2,\"start\":{\"hours\":7,\"minutes\":15},\"end\":{\"hours\":null,\"minutes\":null},\"index\":null,\"daypicker\":[\"Y\",\"Y\",\"N\",\"N\",\"Y\",\"N\",\"N\"],\"startDateActive\":\"02.05.2020\",\"endDateActive\":null},\"startDateActive\":\"02.05.2020\",\"timeRangeActive\":\"07:15\"},{\"timerId\":3,\"timerProfileId\":2,\"timerStatus\":\"NOT_EXPIRED\",\"timerChargeScheduleStatus\":\"IDLE\",\"timerClimateScheduleStatus\":\"IDLE\",\"timerExpStatusTimestamp\":\"01.05.2020\",\"timerProgrammedStatus\":\"NOT_PROGRAMMED\",\"schedule\":{\"type\":2,\"start\":{\"hours\":7,\"minutes\":50},\"end\":{\"hours\":null,\"minutes\":null},\"index\":null,\"daypicker\":[\"N\",\"N\",\"Y\",\"Y\",\"N\",\"N\",\"N\"],\"startDateActive\":\"02.05.2020\",\"endDateActive\":null},\"startDateActive\":\"02.05.2020\",\"timeRangeActive\":\"07:50\"}],\"profiles\":[{\"profileId\":1,\"profileName\":\"Zuhause\",\"timeStamp\":\"01.05.2020\",\"charging\":true,\"climatisation\":true,\"targetChargeLevel\":100,\"nightRateActive\":false,\"nightRateTimeStart\":\"22:00\",\"nightRateTimeEnd\":\"06:00\",\"chargeMaxCurrent\":10,\"heaterSource\":\"ELECTRIC\"},{\"profileId\":2,\"profileName\":\"Firma\",\"timeStamp\":\"01.05.2020\",\"charging\":true,\"climatisation\":true,\"targetChargeLevel\":100,\"nightRateActive\":false,\"nightRateTimeStart\":\"22:00\",\"nightRateTimeEnd\":\"06:00\",\"chargeMaxCurrent\":32,\"heaterSource\":\"ELECTRIC\"}]},\"settings\":{\"minChargeLimit\":100,\"lowerLimitMax\":100},\"auxHeatingAllowed\":false,\"auxHeatingEnabled\":false},\"actionPending\":false,\"rdtAvailable\":true}}"
             * ;
             * count++;
             * } else {
             * eManagerJSON =
             * "{\"errorCode\":\"0\",\"EManager\":{\"rbc\":{\"status\":{\"batteryPercentage\":100,\"chargingState\":\"OFF\",\"chargingRemaningHour\":\"2\",\"chargingRemaningMinute\":\"28\",\"chargingReason\":\"INVALID\",\"pluginState\":\"DISCONNECTED\",\"lockState\":\"UNLOCKED\",\"extPowerSupplyState\":\"UNAVAILABLE\",\"range\":\"7\",\"electricRange\":216,\"combustionRange\":null,\"combinedRange\":216,\"rlzeUp\":false},\"settings\":{\"chargerMaxCurrent\":16,\"maxAmpere\":32,\"maxCurrentReduced\":false}},\"rpc\":{\"status\":{\"climatisationState\":\"OFF\",\"climatisationRemaningTime\":10,\"windowHeatingStateFront\":\"OFF\",\"windowHeatingStateRear\":\"OFF\",\"climatisationReason\":null,\"windowHeatingAvailable\":true},\"settings\":{\"targetTemperature\":\"26\",\"climatisationWithoutHVPower\":true,\"electric\":true},\"climaterActionState\":\"AVAILABLE\",\"auAvailable\":false},\"rdt\":{\"status\":{\"timers\":[{\"timerId\":1,\"timerProfileId\":1,\"timerStatus\":\"NOT_EXPIRED\",\"timerChargeScheduleStatus\":\"IDLE\",\"timerClimateScheduleStatus\":\"IDLE\",\"timerExpStatusTimestamp\":\"11.04.2020\",\"timerProgrammedStatus\":\"NOT_PROGRAMMED\",\"schedule\":{\"type\":2,\"start\":{\"hours\":12,\"minutes\":0},\"end\":{\"hours\":null,\"minutes\":null},\"index\":null,\"daypicker\":[\"Y\",\"Y\",\"Y\",\"Y\",\"Y\",\"Y\",\"Y\"],\"startDateActive\":\"12.04.2020\",\"endDateActive\":null},\"startDateActive\":\"12.04.2020\",\"timeRangeActive\":\"12:00\"},{\"timerId\":2,\"timerProfileId\":1,\"timerStatus\":\"NOT_EXPIRED\",\"timerChargeScheduleStatus\":\"IDLE\",\"timerClimateScheduleStatus\":\"IDLE\",\"timerExpStatusTimestamp\":\"11.04.2020\",\"timerProgrammedStatus\":\"NOT_PROGRAMMED\",\"schedule\":{\"type\":2,\"start\":{\"hours\":12,\"minutes\":0},\"end\":{\"hours\":null,\"minutes\":null},\"index\":null,\"daypicker\":[\"Y\",\"Y\",\"Y\",\"Y\",\"Y\",\"Y\",\"Y\"],\"startDateActive\":\"12.04.2020\",\"endDateActive\":null},\"startDateActive\":\"12.04.2020\",\"timeRangeActive\":\"12:00\"},{\"timerId\":3,\"timerProfileId\":1,\"timerStatus\":\"NOT_EXPIRED\",\"timerChargeScheduleStatus\":\"IDLE\",\"timerClimateScheduleStatus\":\"IDLE\",\"timerExpStatusTimestamp\":\"11.04.2020\",\"timerProgrammedStatus\":\"NOT_PROGRAMMED\",\"schedule\":{\"type\":2,\"start\":{\"hours\":12,\"minutes\":0},\"end\":{\"hours\":null,\"minutes\":null},\"index\":null,\"daypicker\":[\"Y\",\"Y\",\"Y\",\"Y\",\"Y\",\"Y\",\"Y\"],\"startDateActive\":\"12.04.2020\",\"endDateActive\":null},\"startDateActive\":\"12.04.2020\",\"timeRangeActive\":\"12:00\"}],\"profiles\":[{\"profileId\":1,\"profileName\":\"Standard\",\"timeStamp\":\"11.04.2020\",\"charging\":true,\"climatisation\":false,\"targetChargeLevel\":100,\"nightRateActive\":false,\"nightRateTimeStart\":\"23:00\",\"nightRateTimeEnd\":\"23:00\",\"chargeMaxCurrent\":16,\"heaterSource\":\"ELECTRIC\"}]},\"settings\":{\"minChargeLimit\":100,\"lowerLimitMax\":100},\"auxHeatingAllowed\":false,\"auxHeatingEnabled\":false},\"actionPending\":false,\"rdtAvailable\":true}}";
             * count++;
             * }
             * eManager = gson.fromJson(eManagerJSON, EManager.class);
             */

            if (vehicle != null) {
                CompleteVehicleJson vehicleJSON = vehicle.getCompleteVehicleJson();
                // Query API for electric vehicle status if engine type electric/hybrid
                if (vehicleJSON.getEngineTypeElectric() || vehicleJSON.getEngineTypeHybridOCU1()
                        || vehicleJSON.getEngineTypeHybridOCU2()) {
                    url = SESSION_BASE + dashboardUrl + EMANAGER_GET_EMANAGER;
                    eManager = postJSONVWWeConnectAPI(url, fields, EManager.class);
                    logger.debug("API Response ({})", eManager);
                } else {
                    // Query API for vehicle heating status for all other vehicles types
                    url = SESSION_BASE + dashboardUrl + GET_HEATER_STATUS;
                    vehicleHeaterStatus = postJSONVWWeConnectAPI(url, fields, HeaterStatus.class);
                    logger.debug("API Response ({})", vehicleHeaterStatus);
                }
            }

            if (vehicle != null) {
                if (vehicleDetails != null) {
                    vehicle.setVehicleDetails(vehicleDetails);
                } else {
                    logger.warn("Vehicle details is null!");
                }
                if (vehicleStatus != null) {
                    vehicle.setVehicleStatus(vehicleStatus);
                } else {
                    logger.warn("Vehicle status is null!");
                }
                if (trips != null) {
                    vehicle.setTrips(trips);
                } else {
                    logger.warn("Vehicle trips is null!");
                }
                if (location != null) {
                    vehicle.setVehicleLocation(location);
                } else {
                    logger.warn("Vehicle location is null!");
                }
                if (vehicleHeaterStatus != null) {
                    vehicle.setHeaterStatus(vehicleHeaterStatus);
                } else {
                    logger.debug("No heater installed!");
                }
                if (eManager != null) {
                    vehicle.setEManager(eManager);
                } else {
                    logger.debug("No electric/hybrid vehicle");
                }

                BaseVehicle oldObj = vwWeConnectThings.get(vin);
                if (oldObj == null || !oldObj.equals(vehicle)) {
                    vwWeConnectThings.put(vin, vehicle);
                }
                notifyListeners(vehicle);
            } else {
                logger.warn("Failed to update vehicle details for VIN: {}", vin);
                return false;
            }
        }
        return true;
    }

    private synchronized boolean getVehicleStatus() {
        Fields fields = null;
        String content = null;
        String url = null;
        ContentResponse httpResponse = null;

        DocumentContext context = getFullyLoadedCars();
        if (context == null) {
            logger.warn("No Fully loaded vehicle(s) found on VW We Connect portal! JSON: {}", content);
            return false;
        }

        List<Object> vinList = context.read("$[*]['vin']");
        List<Object> dashboardUrlList = context.read("$[*]['dashboardUrl']");
        if (vinList == null || vinList.size() == 0 || dashboardUrlList == null) {
            logger.warn("No Fully loaded vehicle(s) found on VW We Connect portal! JSON: {}", content);
            return false;
        }

        // Loop trough all found vehicles
        for (int i = 0; i < vinList.size(); i++) {
            String vin = (String) vinList.get(i);
            logger.debug("Trying to fetch status for vehicle with VIN {}", vin);
            String dashboardUrl = (String) dashboardUrlList.get(i);

            // Request a Vehicle Status report to be sent from vehicle
            url = SESSION_BASE + dashboardUrl + REQUEST_VEHICLE_STATUS_REPORT;
            httpResponse = postJSONVWWeConnectAPI(url, fields, referer, xCsrfToken);
            if (httpResponse != null) {
                content = httpResponse.getContentAsString();
                if (isErrorCode(content)) {
                    logger.debug("Failed to request vehicle status report!");
                }
            } else {
                logger.warn("Request vehicle status report failed, Http response null");
            }

            url = SESSION_BASE + dashboardUrl + VEHICLE_STATUS;
            httpResponse = postJSONVWWeConnectAPI(url, fields, referer, xCsrfToken);
            if (httpResponse != null) {
                content = httpResponse.getContentAsString();
                logger.debug(content);
                Status status;
                String requestStatus = null;
                try {
                    status = gson.fromJson(content, Status.class);
                    requestStatus = status.getVehicleStatusData().getRequestStatus();
                    // Check for progress of Vehicle report
                    long start = System.currentTimeMillis();
                    while (requestStatus != null && requestStatus.equals(REQUEST_IN_PROGRESS)) {
                        try {
                            long currentTime = System.currentTimeMillis();
                            logger.debug("Time: {}", new Timestamp(currentTime));
                            long elapsedTime = currentTime - start;
                            if (elapsedTime > MAX_WAIT_MILLIS) {
                                logger.debug("Wait timeout, request status: {}",
                                        status.getVehicleStatusData().getRequestStatus());
                                break;
                            } else {
                                Thread.sleep(2 * SLEEP_TIME_MILLIS);
                                httpResponse = postJSONVWWeConnectAPI(url, fields, referer, xCsrfToken);
                                if (httpResponse != null) {
                                    content = httpResponse.getContentAsString();
                                    logger.debug("Status JSON {}, Vehicle status: {}", content, status);
                                    status = gson.fromJson(content, Status.class);
                                    requestStatus = status.getVehicleStatusData().getRequestStatus();
                                }
                            }
                        } catch (InterruptedException e) {
                            logger.warn("Exception caught: {}", e.getMessage(), e);
                        }
                    }
                    if (requestStatus != null && requestStatus.equals(REQUEST_SUCCESSFUL)) {
                        return true;
                    }
                } catch (JsonSyntaxException e) {
                    logger.warn("Exception caught {} while parsing JSON response {} for request {}", e.getMessage(),
                            content, url);
                }
                if (requestStatus != null) {
                    logger.debug("Request status: {}", requestStatus);
                }
            } else {
                logger.warn("Request status, Http response null");
            }
        }
        return false;
    }
}
