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
package org.openhab.binding.miio.internal.cloud;

import java.io.IOException;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.miio.internal.MiIoCrypto;
import org.openhab.binding.miio.internal.MiIoCryptoException;
import org.openhab.binding.miio.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link MiCloudConnector} class is used for connecting to the Xiaomi cloud access
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiCloudConnector {

    private static final int REQUEST_TIMEOUT_SECONDS = 10;
    private static final String UNEXPECTED = "Unexpected :";
    private static final String AGENT_ID = (new Random().ints(65, 70).limit(13)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString());
    private static final String USERAGENT = "Android-7.1.1-1.0.0-ONEPLUS A3010-136-" + AGENT_ID
            + " APP/xiaomi.smarthome APPV/62830";
    private static Locale locale = Locale.getDefault();
    private static final TimeZone TZ = TimeZone.getDefault();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("OOOO");
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final String clientId;

    private String username;
    private String password;
    private String userId = "";
    private String serviceToken = "";
    private String ssecurity = "";
    private int loginFailedCounter = 0;
    private HttpClient httpClient;

    private final Logger logger = LoggerFactory.getLogger(MiCloudConnector.class);

    public MiCloudConnector(String username, String password, HttpClient httpClient) throws MiCloudException {
        this.username = username;
        this.password = password;
        this.httpClient = httpClient;
        if (!checkCredentials()) {
            throw new MiCloudException("username or password can't be empty");
        }
        clientId = (new Random().ints(97, 122 + 1).limit(6)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString());
    }

    void startClient() throws MiCloudException {
        if (!httpClient.isStarted()) {
            try {
                httpClient.start();
                CookieStore cookieStore = httpClient.getCookieStore();
                // set default cookies
                addCookie(cookieStore, "sdkVersion", "accountsdk-18.8.15", "mi.com");
                addCookie(cookieStore, "sdkVersion", "accountsdk-18.8.15", "xiaomi.com");
                addCookie(cookieStore, "deviceId", this.clientId, "mi.com");
                addCookie(cookieStore, "deviceId", this.clientId, "xiaomi.com");
            } catch (Exception e) {
                throw new MiCloudException("No http client cannot be started: " + e.getMessage(), e);
            }
        }
    }

    public void stopClient() {
        try {
            this.httpClient.stop();
        } catch (Exception e) {
            logger.debug("Error stopping httpclient :{}", e.getMessage(), e);
        }
    }

    private boolean checkCredentials() {
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            logger.info("Xiaomi Cloud: username or password missing.");
            return false;
        }
        return true;
    }

    private String getApiUrl(String country) {
        return "https://" + ("cn".equalsIgnoreCase(country.trim()) ? "" : country.trim().toLowerCase() + ".")
                + "api.io.mi.com/app";
    }

    public String getClientId() {
        return clientId;
    }

    String parseJson(String data) {
        if (data.contains("&&&START&&&")) {
            return data.replace("&&&START&&&", "");
        } else {
            return UNEXPECTED.concat(data);
        }
    }

    public String getMapUrl(String vacuumMap, String country) throws MiCloudException {
        String url = getApiUrl(country) + "/home/getmapfileurl";
        Map<String, String> map = new HashMap<String, String>();
        map.put("data", "{\"obj_name\":\"" + vacuumMap + "\"}");
        String mapResponse = request(url, map);
        logger.trace("Response: {}", mapResponse);
        String errorMsg = "";
        try {
            JsonElement response = JsonParser.parseString(mapResponse);
            if (response.isJsonObject()) {
                logger.debug("Received  JSON message {}", response);
                if (response.getAsJsonObject().has("result")
                        && response.getAsJsonObject().get("result").isJsonObject()) {
                    JsonObject jo = response.getAsJsonObject().get("result").getAsJsonObject();
                    if (jo.has("url")) {
                        return jo.get("url").getAsString();
                    } else {
                        errorMsg = "Could not get url";
                    }
                } else {
                    errorMsg = "Could not get result";
                }
            } else {
                errorMsg = "Received message is invalid JSON";
            }
        } catch (ClassCastException | IllegalStateException e) {
            errorMsg = "Received message could not be parsed";
        }
        logger.debug("{}: {}", errorMsg, mapResponse);
        return "";
    }

    public String getDeviceStatus(String device, String country) throws MiCloudException {
        return request("/home/device_list", country, "{\"dids\":[\"" + device + "\"]}");
    }

    public String sendRPCCommand(String device, String country, String command) throws MiCloudException {
        if (device.length() != 8) {
            logger.debug("Device ID ('{}') incorrect or missing. Command not send: {}", device, command);
        }
        if (country.length() > 3 || country.length() < 2) {
            logger.debug("Country ('{}') incorrect or missing. Command not send: {}", device, command);
        }
        String id = "";
        try {
            id = String.valueOf(Long.parseUnsignedLong(device, 16));
        } catch (NumberFormatException e) {
            String err = "Could not parse device ID ('" + device.toString() + "')";
            logger.debug("{}", err);
            throw new MiCloudException(err, e);
        }
        return request("/home/rpc/" + id, country, command);
    }

    public JsonObject getHomeList(String country) {
        String response = "";
        try {
            response = request("/homeroom/gethome", country,
                    "{\"fg\":false,\"fetch_share\":true,\"fetch_share_dev\":true,\"limit\":300,\"app_ver\":7,\"fetch_cariot\":true}");
            logger.trace("gethome response: {}", response);
            final JsonElement resp = JsonParser.parseString(response);
            if (resp.isJsonObject() && resp.getAsJsonObject().has("result")) {
                return resp.getAsJsonObject().get("result").getAsJsonObject();
            }
        } catch (JsonParseException e) {
            logger.info("{} error while parsing rooms: '{}'", e.getMessage(), response);
        } catch (MiCloudException e) {
            logger.info("{}", e.getMessage());
            loginFailedCounter++;
        }
        return new JsonObject();
    }

    public List<CloudDeviceDTO> getDevices(String country) {
        final String response = getDeviceString(country);
        List<CloudDeviceDTO> devicesList = new ArrayList<>();
        try {
            final JsonElement resp = JsonParser.parseString(response);
            if (resp.isJsonObject()) {
                final JsonObject jor = resp.getAsJsonObject();
                if (jor.has("result")) {
                    CloudDeviceListDTO cdl = GSON.fromJson(jor.get("result"), CloudDeviceListDTO.class);
                    if (cdl != null) {
                        devicesList.addAll(cdl.getCloudDevices());
                        for (CloudDeviceDTO device : devicesList) {
                            device.setServer(country);
                            logger.debug("Xiaomi cloud info: {}", device);
                        }
                    }
                } else {
                    logger.debug("Response missing result: '{}'", response);
                }
            } else {
                logger.debug("Response is not a json object: '{}'", response);
            }
        } catch (JsonSyntaxException | IllegalStateException | ClassCastException e) {
            loginFailedCounter++;
            logger.info("Error while parsing devices: {}", e.getMessage());
        }
        return devicesList;
    }

    public String getDeviceString(String country) {
        String resp;
        try {
            resp = request("/home/device_list_page", country, "{\"getVirtualModel\":true,\"getHuamiDevices\":1}");
            logger.trace("Get devices response: {}", resp);
            if (resp.length() > 2) {
                CloudUtil.saveDeviceInfoFile(resp, country, logger);
                return resp;
            }
        } catch (MiCloudException e) {
            logger.info("{}", e.getMessage());
            loginFailedCounter++;
        }
        return "";
    }

    public String request(String urlPart, String country, String params) throws MiCloudException {
        Map<String, String> map = new HashMap<String, String>();
        map.put("data", params);
        return request(urlPart, country, map);
    }

    public String request(String urlPart, String country, Map<String, String> params) throws MiCloudException {
        String url = urlPart.trim();
        url = getApiUrl(country) + (url.startsWith("/app") ? url.substring(4) : url);
        String response = request(url, params);
        logger.debug("Request to '{}' server '{}'. Response: '{}'", country, urlPart, response);
        return response;
    }

    public String request(String url, Map<String, String> params) throws MiCloudException {
        if (this.serviceToken.isEmpty() || this.userId.isEmpty()) {
            throw new MiCloudException("Cannot execute request. service token or userId missing");
        }
        loginFailedCounterCheck();
        startClient();
        logger.debug("Send request to {} with data '{}'", url, params.get("data"));
        Request request = httpClient.newRequest(url).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        request.agent(USERAGENT);
        request.header("x-xiaomi-protocal-flag-cli", "PROTOCAL-HTTP2");
        request.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
        request.cookie(new HttpCookie("userId", this.userId));
        request.cookie(new HttpCookie("yetAnotherServiceToken", this.serviceToken));
        request.cookie(new HttpCookie("serviceToken", this.serviceToken));
        request.cookie(new HttpCookie("locale", locale.toString()));
        request.cookie(new HttpCookie("timezone", ZonedDateTime.now().format(FORMATTER)));
        request.cookie(new HttpCookie("is_daylight", TZ.inDaylightTime(new Date()) ? "1" : "0"));
        request.cookie(new HttpCookie("dst_offset", Integer.toString(TZ.getDSTSavings())));
        request.cookie(new HttpCookie("channel", "MI_APP_STORE"));

        if (logger.isTraceEnabled()) {
            for (HttpCookie cookie : request.getCookies()) {
                logger.trace("Cookie set for request ({}) : {} --> {}     (path: {})", cookie.getDomain(),
                        cookie.getName(), cookie.getValue(), cookie.getPath());
            }
        }
        String method = "POST";
        request.method(method);

        try {
            String nonce = CloudUtil.generateNonce(System.currentTimeMillis());
            String signedNonce = CloudUtil.signedNonce(ssecurity, nonce);
            String signature = CloudUtil.generateSignature(url.replace("/app", ""), signedNonce, nonce, params);

            Fields fields = new Fields();
            fields.put("signature", signature);
            fields.put("_nonce", nonce);
            fields.put("data", params.get("data"));
            request.content(new FormContentProvider(fields));

            logger.trace("fieldcontent: {}", fields.toString());
            final ContentResponse response = request.send();
            if (response.getStatus() >= HttpStatus.BAD_REQUEST_400
                    && response.getStatus() < HttpStatus.INTERNAL_SERVER_ERROR_500) {
                this.serviceToken = "";
            }
            return response.getContentAsString();
        } catch (HttpResponseException e) {
            serviceToken = "";
            logger.debug("Error while executing request to {} :{}", url, e.getMessage());
            loginFailedCounter++;
        } catch (InterruptedException | TimeoutException | ExecutionException | IOException e) {
            logger.debug("Error while executing request to {} :{}", url, e.getMessage());
            loginFailedCounter++;
        } catch (MiIoCryptoException e) {
            logger.debug("Error while decrypting response of request to {} :{}", url, e.getMessage(), e);
            loginFailedCounter++;
        }
        return "";
    }

    private void addCookie(CookieStore cookieStore, String name, String value, String domain) {
        HttpCookie cookie = new HttpCookie(name, value);
        cookie.setDomain("." + domain);
        cookie.setPath("/");
        cookieStore.add(URI.create("https://" + domain), cookie);
    }

    public synchronized boolean login() {
        if (!checkCredentials()) {
            return false;
        }
        if (!userId.isEmpty() && !serviceToken.isEmpty()) {
            return true;
        }
        logger.debug("Xiaomi cloud login with userid {}", username);
        try {
            if (loginRequest()) {
                loginFailedCounter = 0;
            } else {
                loginFailedCounter++;
                logger.debug("Xiaomi cloud login attempt {}", loginFailedCounter);
            }
        } catch (MiCloudException e) {
            logger.info("Error logging on to Xiaomi cloud ({}): {}", loginFailedCounter, e.getMessage());
            loginFailedCounter++;
            serviceToken = "";
            loginFailedCounterCheck();
            return false;
        }
        return true;
    }

    void loginFailedCounterCheck() {
        if (loginFailedCounter > 10) {
            logger.info("Repeated errors logging on to Xiaomi cloud. Cleaning stored cookies");
            dumpCookies(".xiaomi.com", true);
            dumpCookies(".mi.com", true);
            serviceToken = "";
            loginFailedCounter = 0;
        }
    }

    protected boolean loginRequest() throws MiCloudException {
        try {
            startClient();
            String sign = loginStep1();
            String location;
            if (!sign.startsWith("http")) {
                location = loginStep2(sign);
            } else {
                location = sign; // seems we already have login location
            }
            final ContentResponse responseStep3 = loginStep3(location);
            switch (responseStep3.getStatus()) {
                case HttpStatus.FORBIDDEN_403:
                    throw new MiCloudException("Access denied. Did you set the correct api-key and/or username?");
                case HttpStatus.OK_200:
                    return true;
                default:
                    logger.trace("request returned status '{}', reason: {}, content = {}", responseStep3.getStatus(),
                            responseStep3.getReason(), responseStep3.getContentAsString());
                    throw new MiCloudException(responseStep3.getStatus() + responseStep3.getReason());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new MiCloudException("Cannot logon to Xiaomi cloud: " + e.getMessage(), e);
        } catch (MiIoCryptoException e) {
            throw new MiCloudException("Error decrypting. Cannot logon to Xiaomi cloud: " + e.getMessage(), e);
        } catch (MalformedURLException | JsonParseException e) {
            throw new MiCloudException("Error getting logon URL. Cannot logon to Xiaomi cloud: " + e.getMessage(), e);
        }
    }

    private String loginStep1() throws InterruptedException, TimeoutException, ExecutionException, MiCloudException {
        final ContentResponse responseStep1;

        logger.trace("Xiaomi Login step 1");
        String url = "https://account.xiaomi.com/pass/serviceLogin?sid=xiaomiio&_json=true";
        Request request = httpClient.newRequest(url).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        request.agent(USERAGENT);
        request.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
        request.cookie(new HttpCookie("userId", this.userId.length() > 0 ? this.userId : this.username));

        responseStep1 = request.send();
        final String content = responseStep1.getContentAsString();
        logger.trace("Xiaomi Login step 1 content response= {}", content);
        logger.trace("Xiaomi Login step 1 response = {}", responseStep1);
        try {
            JsonElement resp = JsonParser.parseString(parseJson(content));
            CloudLogin1DTO jsonResp = GSON.fromJson(resp, CloudLogin1DTO.class);
            final String sign = jsonResp != null ? jsonResp.getSign() : null;
            if (sign != null && !sign.isBlank()) {
                logger.trace("Xiaomi Login step 1 sign = {}", sign);
                return sign;
            } else {
                logger.debug("Xiaomi Login _sign missing. Maybe still has login cookie.");
                return "";
            }
        } catch (JsonParseException | IllegalStateException | ClassCastException e) {
            throw new MiCloudException("Error getting logon sign. Cannot parse response: " + e.getMessage(), e);
        }
    }

    private String loginStep2(String sign) throws MiIoCryptoException, InterruptedException, TimeoutException,
            ExecutionException, MiCloudException, JsonSyntaxException, JsonParseException {
        String passToken;
        String cUserId;

        logger.trace("Xiaomi Login step 2");
        String url = "https://account.xiaomi.com/pass/serviceLoginAuth2";
        Request request = httpClient.newRequest(url).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        request.agent(USERAGENT);
        request.method(HttpMethod.POST);
        final ContentResponse responseStep2;

        Fields fields = new Fields();
        fields.put("sid", "xiaomiio");
        fields.put("hash", Utils.getHex(MiIoCrypto.md5(password.getBytes())));
        fields.put("callback", "https://sts.api.io.mi.com/sts");
        fields.put("qs", "%3Fsid%3Dxiaomiio%26_json%3Dtrue");
        fields.put("user", username);
        if (!sign.isEmpty()) {
            fields.put("_sign", sign);
        }
        fields.put("_json", "true");

        request.content(new FormContentProvider(fields));
        responseStep2 = request.send();

        final String content2 = responseStep2.getContentAsString();
        logger.trace("Xiaomi login step 2 response = {}", responseStep2);
        logger.trace("Xiaomi login step 2 content = {}", content2);

        JsonElement resp2 = JsonParser.parseString(parseJson(content2));
        CloudLoginDTO jsonResp = GSON.fromJson(resp2, CloudLoginDTO.class);
        if (jsonResp == null) {
            throw new MiCloudException("Error getting logon details from step 2: " + content2);
        }
        ssecurity = jsonResp.getSsecurity();
        userId = jsonResp.getUserId();
        cUserId = jsonResp.getcUserId();
        passToken = jsonResp.getPassToken();
        String location = jsonResp.getLocation();
        String code = jsonResp.getCode();

        logger.trace("Xiaomi login ssecurity = {}", ssecurity);
        logger.trace("Xiaomi login userId = {}", userId);
        logger.trace("Xiaomi login cUserId = {}", cUserId);
        logger.trace("Xiaomi login passToken = {}", passToken);
        logger.trace("Xiaomi login location = {}", location);
        logger.trace("Xiaomi login code = {}", code);
        if (0 != jsonResp.getSecurityStatus()) {
            logger.debug("Xiaomi Cloud Step2 response: {}", parseJson(content2));
            logger.debug(
                    """
                            Xiaomi Login code: {}
                            SecurityStatus: {}
                            Pwd code: {}
                            Location logon URL: {}
                            In case of login issues check userId/password details are correct.
                            If login details are correct, try to logon using browser from the openHAB ip using the browser. Alternatively try to complete logon with above URL.\
                            """,
                    jsonResp.getCode(), jsonResp.getSecurityStatus(), jsonResp.getPwd(), jsonResp.getLocation());
        }
        if (logger.isTraceEnabled()) {
            dumpCookies(url, false);
        }
        if (!location.isEmpty()) {
            return location;
        } else {
            throw new MiCloudException("Error getting logon location URL. Return code: " + code);
        }
    }

    private ContentResponse loginStep3(String location)
            throws MalformedURLException, InterruptedException, TimeoutException, ExecutionException {
        final ContentResponse responseStep3;
        Request request;
        logger.trace("Xiaomi Login step 3 @ {}", (new URL(location)).getHost());
        request = httpClient.newRequest(location).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        request.agent(USERAGENT);
        request.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
        responseStep3 = request.send();
        logger.trace("Xiaomi login step 3 content = {}", responseStep3.getContentAsString());
        logger.trace("Xiaomi login step 3 response = {}", responseStep3);
        if (logger.isTraceEnabled()) {
            dumpCookies(location, false);
        }
        URI uri = URI.create("http://sts.api.io.mi.com");
        String serviceToken = extractServiceToken(uri);
        if (!serviceToken.isEmpty()) {
            this.serviceToken = serviceToken;
        }
        return responseStep3;
    }

    private void dumpCookies(String url, boolean delete) {
        if (logger.isTraceEnabled()) {
            try {
                URI uri = URI.create(url);
                logger.trace("Cookie dump for {}", uri);
                CookieStore cs = httpClient.getCookieStore();
                if (cs != null) {
                    List<HttpCookie> cookies = cs.get(uri);
                    for (HttpCookie cookie : cookies) {
                        logger.trace("Cookie ({}) : {} --> {}     (path: {}. Removed: {})", cookie.getDomain(),
                                cookie.getName(), cookie.getValue(), cookie.getPath(), delete);
                        if (delete) {
                            cs.remove(uri, cookie);
                        }
                    }
                } else {
                    logger.trace("Could not create cookiestore from {}", url);
                }
            } catch (IllegalArgumentException e) {
                logger.trace("Error dumping cookies from {}: {}", url, e.getMessage(), e);
            }
        }
    }

    private String extractServiceToken(URI uri) {
        String serviceToken = "";
        List<HttpCookie> cookies = httpClient.getCookieStore().get(uri);
        for (HttpCookie cookie : cookies) {
            logger.trace("Cookie :{} --> {}", cookie.getName(), cookie.getValue());
            if (cookie.getName().contentEquals("serviceToken")) {
                serviceToken = cookie.getValue();
                logger.debug("Xiaomi cloud logon successful.");
                logger.trace("Xiaomi cloud servicetoken: {}", serviceToken);
            }
        }
        return serviceToken;
    }

    public boolean hasLoginToken() {
        return !serviceToken.isEmpty();
    }
}
