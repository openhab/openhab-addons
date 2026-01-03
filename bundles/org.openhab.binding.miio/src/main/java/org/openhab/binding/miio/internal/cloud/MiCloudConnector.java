/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.miio.internal.MiIoCryptoException;
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

    protected static final int REQUEST_TIMEOUT_SECONDS = 10;
    protected static final String AGENT_ID = (new Random().ints(65, 70).limit(13)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString());
    protected static final String USERAGENT = "Android-9.1.1-1.0.0-ONEPLUS A3010-136-" + AGENT_ID
            + " APP/com.xiaomi.mihome APPV/10.5.201";
    protected static Locale locale = Locale.getDefault();
    protected static final TimeZone TZ = TimeZone.getDefault();
    protected static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("OOOO");
    protected static final Gson GSON = new GsonBuilder().serializeNulls().create();
    protected List<CloudLogonListener> listeners = new CopyOnWriteArrayList<>();

    protected String username;
    protected String password;
    protected String userId = "";
    protected String serviceToken = "";
    protected String ssecurity = "";
    protected final String clientId;

    protected int loginFailedCounter = 0;
    protected HttpClient httpClient;
    private String captchaResponse = "";
    protected String sign = "";
    protected CloudLoginState loginState = CloudLoginState.INTIATING;

    public enum CloudLoginState {
        INTIATING,
        ACCESS_DENIED,
        AWAITING_2FA,
        AWAITING_CAPTCHA,
        AWAITING_QRLOGIN,
        CAPTCHA_FAILED,
        ONLINE,
    }

    public enum CloudLoginMode {
        QRCODE,
        PASSWORD,
        TOKEN
    }

    // {"code":66108,"desc":"发送的参数信息不合法"} wrong parameters
    // code":10012,"action":"","title":"","tips":"Onwettig verzoek","desc":"非法请求"}
    private final Logger logger = LoggerFactory.getLogger(MiCloudConnector.class);

    static public String generateClientId() {
        return (new Random().ints(97, 122 + 1).limit(6)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString());
    }

    public MiCloudConnector(@Nullable String username, @Nullable String password, HttpClient httpClient,
            @Nullable String clientId, @Nullable String userId, @Nullable String serviceToken,
            @Nullable String ssecurity) throws MiCloudException {
        this.username = username != null ? username : "";
        this.password = password != null ? password : "";
        this.httpClient = httpClient;
        this.userId = userId != null ? userId : "";
        this.serviceToken = serviceToken != null ? serviceToken : "";
        this.ssecurity = ssecurity != null ? ssecurity : "";
        this.clientId = clientId != null ? clientId : generateClientId();
        this.httpClient.setFollowRedirects(true);
        if (!checkCredentials()) {
            throw new MiCloudException("username or password can't be empty");
        }
    }

    public MiCloudConnector(String username, String password, HttpClient httpClient) throws MiCloudException {
        this.username = username;
        this.password = password;
        this.httpClient = httpClient;
        this.httpClient.setFollowRedirects(true);

        if (!checkCredentials()) {
            throw new MiCloudException("username or password can't be empty");
        }
        clientId = generateClientId();
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

    protected boolean checkCredentials() {
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

    public Optional<String> getMapUrl(String vacuumMap, String country) throws MiCloudException {
        String url = getApiUrl(country) + "/home/getmapfileurl";
        Map<String, String> map = new HashMap<>();
        map.put("data", "{\"obj_name\":\"" + vacuumMap + "\"}");
        try {
            String mapResponse = request(url, map);
            logger.trace("Response: {}", mapResponse);
            JsonElement response = JsonParser.parseString(mapResponse);
            if (response.isJsonObject()) {
                logger.debug("Received  JSON message {}", response);
                if (response.getAsJsonObject().has("result")
                        && response.getAsJsonObject().get("result").isJsonObject()) {
                    JsonObject jo = response.getAsJsonObject().get("result").getAsJsonObject();
                    if (jo.has("url")) {
                        return Optional.of(jo.get("url").getAsString());
                    } else {
                        throw new MiCloudException("Could not get url from response");
                    }
                } else {
                    throw new MiCloudException("Could not get result from response");
                }
            } else {
                throw new MiCloudException("Received message is invalid JSON");
            }
        } catch (JsonParseException | ClassCastException | IllegalStateException e) {
            logger.debug("Error parsing map URL response: {}", e.getMessage());
            throw new MiCloudException("Received message could not be parsed", e);
        }
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
        List<CloudDeviceDTO> devicesList = new ArrayList<>();
        try {
            final String response = getDeviceString(country);
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
        } catch (MiCloudException e) {
            logger.info("{}", e.getMessage());
            loginFailedCounter++;
        } catch (JsonSyntaxException | IllegalStateException | ClassCastException e) {
            loginFailedCounter++;
            logger.info("Error while parsing devices: {}", e.getMessage());
        }
        return devicesList;
    }

    public String getDeviceString(String country) throws MiCloudException {
        try {
            String resp = request("/home/device_list_page", country,
                    "{\"getVirtualModel\":true,\"getHuamiDevices\":1}");
            logger.trace("Get devices response: {}", resp);
            if (resp.length() > 2) {
                CloudUtil.saveDeviceInfoFile(resp, country, logger);
                return resp;
            }
        } catch (MiCloudException e) {
            logger.info("{}", e.getMessage());
            loginFailedCounter++;
        }
        throw new MiCloudException("Empty device list response");
    }

    public String request(String urlPart, String country, String params) throws MiCloudException {
        Map<String, String> map = new HashMap<>();
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

        // request.cookie(new HttpCookie("cUserId", "9ujlMfMzZ-HvcQbOdtiqwdFeqss"));

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
        dumpCookies(url, false);

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
            throw new MiCloudException("Error while executing request: " + e.getMessage(), e);
        } catch (InterruptedException | TimeoutException | ExecutionException | IOException e) {
            logger.debug("Error while executing request to {} :{}", url, e.getMessage());
            loginFailedCounter++;
            throw new MiCloudException("Error while executing request: " + e.getMessage(), e);
        } catch (MiIoCryptoException e) {
            logger.debug("Error while decrypting response of request to {} :{}", url, e.getMessage(), e);
            loginFailedCounter++;
            throw new MiCloudException("Error decrypting response: " + e.getMessage(), e);
        }
    }

    private void addCookie(CookieStore cookieStore, String name, String value, String domain) {
        HttpCookie cookie = new HttpCookie(name, value);
        cookie.setDomain("." + domain);
        cookie.setPath("/");
        cookieStore.add(URI.create("https://" + domain), cookie);
    }

    protected byte[] fetchImageBytes(String url, int timeoutSeconds) throws MiCloudException {
        try {
            Request request = httpClient.newRequest(url).agent(USERAGENT).method("GET").timeout(timeoutSeconds,
                    TimeUnit.SECONDS);
            final ContentResponse response = request.send();
            if (response.getStatus() >= HttpStatus.BAD_REQUEST_400) {
                throw new MiCloudException("Failed to fetch image from " + url + " status=" + response.getStatus());
            }
            return response.getContent();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new MiCloudException("Error fetching image from " + url + ": " + e.getMessage(), e);
        }
    }

    protected Path saveBytesToTempFile(byte[] content, String prefix, String suffix) throws MiCloudException {
        try {
            Path tmp = Files.createTempFile(prefix, suffix == null ? ".img" : suffix);
            Files.write(tmp, content, StandardOpenOption.WRITE);
            return tmp;
        } catch (IOException e) {
            throw new MiCloudException("Error writing temporary image file: " + e.getMessage(), e);
        }
    }

    protected byte[] fetchAndInformImage(String url, int timeoutSeconds, String tempPrefix) throws MiCloudException {
        byte[] content = fetchImageBytes(url, timeoutSeconds);
        try {
            Path path = saveBytesToTempFile(content, tempPrefix == null ? "miio-img-" : tempPrefix, ".jpg");
            logger.info("Saved image to {} -> {} bytes", path.toAbsolutePath(), content.length);
        } catch (MiCloudException e) {
            logger.debug("Could not save image to temp file: {}", e.getMessage());
        }
        informImageListeners(content);
        return content;
    }

    public synchronized boolean login() {
        logger.info(" client Id={}", clientId);

        return login("");
    }

    public synchronized boolean login(String captchaResponse) {
        if (!checkCredentials()) {
            return false;
        }
        if (!userId.isEmpty() && !serviceToken.isEmpty()) {
            return true;
        }
        logger.debug("Xiaomi cloud login with userid {}", username);
        /*
         * try {
         * // if (loginRequest(captchaResponse)) {
         * // loginFailedCounter = 0;
         * // } else {
         * // loginFailedCounter++;
         * logger.debug("Xiaomi cloud login attempt {}", loginFailedCounter);
         * }
         * }catch(
         *
         * MiCloudException e)
         * {
         * logger.info("Error logging on to Xiaomi cloud ({}): {}", loginFailedCounter, e.getMessage());
         * loginFailedCounter++;
         * serviceToken = "";
         * loginFailedCounterCheck();
         * return false;
         * }return true;
         */
        return false;
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

    protected void updateLoginState(CloudLoginState state) {
        loginState = state;
        for (CloudLogonListener listener : listeners) {
            logger.debug("inform listener {}, state {}", listener, state);
            try {
                listener.onStatusUpdated(state, state.toString());
            } catch (Exception e) {
                logger.debug("Could not inform listener {}: {}: ", listener, e.getMessage(), e);
            }
        }
    };

    protected ContentResponse debugRequest(Request request)
            throws InterruptedException, TimeoutException, ExecutionException {
        ContentResponse response;
        logger.debug("Xiaomi cloud request  URL= {} {} {}", request.getMethod(), request.getHost(), request.getPath());
        logger.debug("Xiaomi cloud request content req= {}",
                request.getContent() == null ? "" : request.getContent().toString());
        logger.debug("Xiaomi cloud request headers= {}", request.getHeaders().toString());
        logger.debug("Xiaomi cloud request param= {}", request.getParams());
        logger.debug("Xiaomi cloud request cookie= {}", request.getCookies().toString());
        response = request.send();
        logger.debug("Xiaomi cloud response status = {}", response.getStatus());
        logger.debug("Xiaomi cloud response response = {}", response);
        logger.debug("Xiaomi cloud response content = {}", response.toString());
        logger.debug("Xiaomi cloud response header = {}", response.getHeaders().toString());
        logger.debug("Xiaomi cloud response content = {}", response.getContentAsString());
        // logger.debug("Xiaomi cloud response cookie= {}", response.s.getCookies().toString());
        dumpCookies(request.getHost() + request.getPath(), false);
        return response;
    }

    protected void informImageListeners(byte[] image) {
        for (CloudLogonListener listener : listeners) {
            logger.debug("Inform listener {}, with image", listener);
            try {
                listener.onCaptcha(image);
            } catch (Exception e) {
                logger.debug("Could not inform listener {}: {}: ", listener, e.getMessage(), e);
            }
        }
    }

    protected ContentResponse loginStep3(String location)
            throws MalformedURLException, InterruptedException, TimeoutException, ExecutionException {
        final ContentResponse responseStep3;
        Request request;
        logger.trace("Xiaomi Login step 3 @ {}", (URI.create(location).toURL()).getHost());
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
            updateLoginState(CloudLoginState.ONLINE);
        }
        return responseStep3;
    }

    protected void dumpCookies(String url, boolean delete) {
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

    protected String extractServiceToken(URI uri) {
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getServiceToken() {
        return serviceToken;
    }

    public void setServiceToken(String serviceToken) {
        this.serviceToken = serviceToken;
    }

    public String getSsecurity() {
        return ssecurity;
    }

    public void setSsecurity(String ssecurity) {
        this.ssecurity = ssecurity;
    }

    protected List<CloudLogonListener> getListeners() {
        return listeners;
    }

    /**
     * Registers a {@link CloudLogonListener} to be called back, when data is received.
     * If no {@link MessageSenderThread} exists, when the method is called, it is being set up.
     *
     * @param listener {@link CloudLogonListener} to be called back
     */
    public synchronized void registerListener(CloudLogonListener listener) {
        if (!getListeners().contains(listener)) {
            logger.debug("Adding cloud listener {}", listener);
            getListeners().add(listener);
        }
    }

    /**
     * Unregisters a {@link CloudLogonListener}. If there are no listeners left,
     * the {@link MessageSenderThread} is being closed.
     *
     * @param listener {@link CloudLogonListener} to be unregistered
     */
    public synchronized void unregisterListener(CloudLogonListener listener) {
        getListeners().remove(listener);
    }
}
