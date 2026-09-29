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

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.HttpCookieStore;
import org.openhab.binding.miio.internal.MiIoCrypto;
import org.openhab.binding.miio.internal.MiIoCryptoException;
import org.openhab.binding.miio.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link MiCloudUserIdLoginConnector} class is used for connecting to the Xiaomi cloud access
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiCloudUserIdLoginConnector extends MiCloudConnector {
    private static final String BROWSER_USERAGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0";

    private final Logger logger = LoggerFactory.getLogger(MiCloudUserIdLoginConnector.class);
    private @Nullable Request fa;

    public MiCloudUserIdLoginConnector(String username, String password, HttpClient httpClient)
            throws MiCloudException {
        super(username, password, httpClient);
    }

    public MiCloudUserIdLoginConnector(@Nullable String username, @Nullable String password, HttpClient httpClient,
            @Nullable String clientId, @Nullable String userId, @Nullable String serviceToken,
            @Nullable String ssecurity) throws MiCloudException {
        super(username, password, httpClient, clientId, userId, serviceToken, ssecurity);
    }

    @Override
    public synchronized boolean login() {
        logger.debug("client Id={}", clientId);
        return login("");
    }

    @Override
    public synchronized boolean login(String captchaResponse) {
        if (!userId.isEmpty() && !serviceToken.isEmpty()) {
            updateLoginState(CloudLoginState.ONLINE);
            return true;
        }
        if (!checkCredentials()) {
            return false;
        }
        if (!userId.isEmpty() && !serviceToken.isEmpty()) {
            return true;
        }
        logger.debug("Xiaomi cloud login with userid {}", username);
        try {
            if (loginRequest(captchaResponse)) {
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

    protected boolean loginRequest(String captchaResponse) throws MiCloudException {
        try {
            startClient();

            String sign = this.sign.isEmpty() ? loginStep1() : this.sign;
            String location;
            if (!sign.startsWith("http")) {
                this.sign = sign;
                location = loginStep2(sign, captchaResponse);
            } else {
                location = sign; // seems we already have login location
            }
            final ContentResponse responseStep3 = loginStep3(location);
            switch (responseStep3.getStatus()) {
                case HttpStatus.FORBIDDEN_403:
                    updateLoginState(CloudLoginState.ACCESS_DENIED);
                    throw new MiCloudException("Access denied. Did you set the correct api-key and/or username?");
                case HttpStatus.OK_200:
                    return true;
                default:
                    logger.trace("request returned status '{}', reason: {}, content = {}", responseStep3.getStatus(),
                            responseStep3.getReason(), responseStep3.getContentAsString());
                    throw new MiCloudException(responseStep3.getStatus() + responseStep3.getReason());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MiCloudException("Xiaomi cloud login interrupted", e);
        } catch (TimeoutException | ExecutionException e) {
            throw new MiCloudException("Cannot login to Xiaomi cloud: " + e.getMessage(), e);
        } catch (MiIoCryptoException e) {
            throw new MiCloudException("Error decrypting. Cannot login to Xiaomi cloud: " + e.getMessage(), e);
        } catch (MalformedURLException | JsonParseException e) {
            throw new MiCloudException("Error getting login URL. Cannot login to Xiaomi cloud: " + e.getMessage(), e);
        }
    }

    private String loginStep1() throws InterruptedException, TimeoutException, ExecutionException, MiCloudException {
        logger.trace("Xiaomi Login step 1");
        String url = "https://account.xiaomi.com/pass/serviceLogin?sid=xiaomiio&_json=true";
        Request request = httpClient.newRequest(url).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        request.agent(USERAGENT);
        request.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
        request.cookie(new HttpCookie("userId", !this.userId.isEmpty() ? this.userId : this.username));

        final ContentResponse responseStep1 = request.send();
        final String content = responseStep1.getContentAsString();
        logger.trace("Xiaomi Login step 1 content response= {}", content);
        logger.trace("Xiaomi Login step 1 response = {}", responseStep1);

        try {
            JsonElement resp = JsonParser.parseString(CloudUtil.parseJson(content));
            CloudLogin1DTO jsonResp = GSON.fromJson(resp, CloudLogin1DTO.class);

            if (jsonResp == null) {
                throw new MiCloudException("Xiaomi Login step 1: Failed to parse response");
            }

            String sign = jsonResp.getSign();
            if (!sign.isEmpty()) {
                logger.trace("Xiaomi Login step 1 sign = {}", sign);
                return sign;
            } else {
                logger.debug("Xiaomi Login _sign missing. Maybe still has login cookie.");
                throw new MiCloudException("Xiaomi Login _sign missing. Maybe still has login cookie.");
            }
        } catch (JsonParseException | IllegalStateException | ClassCastException e) {
            throw new MiCloudException("Error getting login sign. Cannot parse response: " + e.getMessage(), e);
        }
    }

    private String loginStep2(String sign, String captchaResponse) throws MiIoCryptoException, InterruptedException,
            TimeoutException, ExecutionException, MiCloudException, JsonSyntaxException, JsonParseException {
        logger.trace("Xiaomi Login step 2");
        String url = "https://account.xiaomi.com/pass/serviceLoginAuth2";

        Request request = httpClient.newRequest(url).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        request.agent(USERAGENT);
        request.method(HttpMethod.POST);

        Fields fields = new Fields();
        fields.put("sid", "xiaomiio");
        fields.put("hash", Utils.getHex(MiIoCrypto.md5(password.getBytes(java.nio.charset.StandardCharsets.UTF_8))));
        fields.put("callback", "https://sts.api.io.mi.com/sts");
        fields.put("qs", "%3Fsid%3Dxiaomiio%26_json%3Dtrue");
        fields.put("user", username);
        if (!sign.isEmpty()) {
            fields.put("_sign", sign);
        }
        if (!captchaResponse.isEmpty()) {
            fields.put("captCode", captchaResponse);
            logger.debug("login with captcha response {}", Utils.obfuscateToken(captchaResponse));
        } else {
            logger.debug("login step 2 without captcha response");
        }
        fields.put("_json", "true");

        request.content(new FormContentProvider(fields));
        final ContentResponse responseStep2 = request.send();

        final String content2 = responseStep2.getContentAsString();
        logger.trace("Xiaomi login step 2 response = {}", responseStep2);
        logger.trace("Xiaomi login step 2 content = {}", content2);

        JsonElement resp2 = JsonParser.parseString(CloudUtil.parseJson(content2));
        CloudLoginDTO jsonResp = GSON.fromJson(resp2, CloudLoginDTO.class);

        if (jsonResp == null) {
            throw new MiCloudException("Error getting login details from step 2: " + content2);
        }

        ssecurity = jsonResp.getSsecurity();
        userId = jsonResp.getUserId();
        String cUserId = jsonResp.getcUserId();
        String passToken = jsonResp.getPassToken();
        String location = jsonResp.getLocation();
        String code = jsonResp.getCode();
        String captchaUrl = jsonResp.getCaptchaUrl();
        String callbackUrl = jsonResp.getCallback();
        String notificationUrl = jsonResp.getNotificationUrl();
        Integer securityStatus = jsonResp.getSecurityStatus();
        Integer pwd = jsonResp.getPwd();

        logger.trace("Xiaomi login ssecurity = {}", ssecurity);
        logger.trace("Xiaomi login userId = {}", userId);
        logger.trace("Xiaomi login cUserId = {}", cUserId);
        logger.trace("Xiaomi login passToken = {}", passToken);
        logger.trace("Xiaomi login location = {}", location);
        logger.trace("Xiaomi login code = {}", code);
        logger.trace("Xiaomi login captcha URL = {}", captchaUrl);
        logger.trace("Xiaomi login callbackUrl = {}", callbackUrl);

        if ("87001".equals(code)) {
            logger.debug("Xiaomi Cloud Step2 failed captcha: {}", CloudUtil.parseJson(content2));
            updateLoginState(CloudLoginState.CAPTCHA_FAILED);
        }

        if (securityStatus != 0) {
            logger.debug("Xiaomi Cloud Step2 response: {}", CloudUtil.parseJson(content2));
            logger.debug(
                    """
                            Xiaomi Login code: {}
                            SecurityStatus: {}
                            Pwd code: {}
                            Location login URL: {}
                            In case of login issues check userId/password details are correct.
                            If login details are correct, try to login using browser from the openHAB ip using the browser. Alternatively try to complete login with above URL.\
                            """,
                    code, securityStatus, pwd, location);

            if (!notificationUrl.isEmpty()) {
                logger.info("Click submit and get token. Then enter the token in OH:\r\n{} ", notificationUrl);
                get2factory(notificationUrl);
                updateLoginState(CloudLoginState.AWAITING_2FA);
            }
        }

        if (!captchaUrl.isEmpty()) {
            updateLoginState(CloudLoginState.AWAITING_CAPTCHA);
            downloadCaptcha(captchaUrl);
        }

        if (logger.isTraceEnabled()) {
            dumpCookies(url, false);
        }

        if (!location.isEmpty()) {
            return location;
        } else {
            if (loginState.equals(CloudLoginState.AWAITING_2FA) || loginState.equals(CloudLoginState.AWAITING_CAPTCHA)
                    || loginState.equals(CloudLoginState.CAPTCHA_FAILED)) {
                logger.debug("Retry with new captcha/2fa");
            }
            throw new MiCloudException("Error getting login location URL. Return code: " + code);
        }
    }

    private void get2factory(String url) {
        CookieStore cookieStore = httpClient.getCookieStore();
        try {
            httpClient.setCookieStore(new HttpCookieStore.Empty());
            logger.debug("Trying to request code from {}", url);

            Request request = httpClient.newRequest(url).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            request.agent(BROWSER_USERAGENT);
            request.method(HttpMethod.GET);
            debugRequest(request);

            String newurl = url.replace("authStart", "list");
            request = httpClient.newRequest(newurl).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            request.agent(BROWSER_USERAGENT);
            debugRequest(request);

            request = httpClient.newRequest("https://account.xiaomi.com/identity/auth/verifyEmail?_flag=8&_json=true")
                    .timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            request.agent(BROWSER_USERAGENT);
            debugRequest(request);

            long ms = java.time.Instant.now().toEpochMilli();
            String mailticket = "https://account.xiaomi.com/identity/auth/sendEmailTicket?_dc=" + ms;
            request = httpClient.newRequest(mailticket).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            request.agent(BROWSER_USERAGENT);
            request.method(HttpMethod.POST);

            Fields fields = new Fields();
            fields.put("retry", "0");
            fields.put("icode", "");
            fields.put("_json", "true");
            request.content(new FormContentProvider(fields));
            debugRequest(request);

            String verifyticket = "https://account.xiaomi.com/identity/auth/verifyEmail?_dc=" + ms;
            request = httpClient.newRequest(verifyticket).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            request.method(HttpMethod.POST);
            this.fa = request;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("2FA setup interrupted: {}", e.getMessage());
        } catch (TimeoutException | ExecutionException e) {
            logger.warn("Error requesting 2FA code: {}", e.getMessage(), e);
        } finally {
            httpClient.setCookieStore(cookieStore);
        }
    }

    /**
     * Processes the 2FA response code.
     *
     * @param faCode The 2FA code entered by the user
     */
    @Override
    public void faResponse(String faCode) {
        try {
            Request fa = this.fa;
            if (fa == null) {
                logger.warn("2FA request not initialized");
                return;
            }

            Fields fields = new Fields();
            fields.put("_flag", "0");
            fields.put("trust", "false");
            fields.put("_json", "true");
            fields.put("ticket", faCode.trim());
            fa.content(new FormContentProvider(fields));

            ContentResponse result = debugRequest(fa);
            String resultContent = result.getContentAsString();
            String jsonContent = CloudUtil.parseJson(resultContent);
            JsonElement resultJson = JsonParser.parseString(jsonContent);

            if (!resultJson.isJsonObject()) {
                logger.warn("2FA response is not a valid JSON object: {}", resultContent);
                return;
            }

            JsonObject jsonObject = resultJson.getAsJsonObject();
            String location = CloudUtil.getJsonString(jsonObject, "location", "");

            if (location.isEmpty()) {
                String code = CloudUtil.getJsonString(jsonObject, "code", "");
                String description = CloudUtil.getJsonString(jsonObject, "description", "");
                logger.warn("2FA failed - code: {}, description: {}", code, description);
                return;
            }

            Request request = httpClient.newRequest(location).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            request.method(HttpMethod.POST);
            ContentResponse endresult = debugRequest(request);

            HttpFields headers = endresult.getHeaders();
            location = extractLocationFromHeaders(headers);

            if (!location.isEmpty()) {
                final ContentResponse response = loginStep3(location);
                logger.trace("Xiaomi login step 3 status = {}", response.getStatus());
                logger.trace("Xiaomi login step 3 response = {}", response);
                logger.trace("Xiaomi login step 3 header = {}", response.getHeaders().toString());
                logger.trace("Xiaomi login step 3 content = {}", response.getContentAsString());
            } else {
                logger.warn("2FA completed but no redirect location found");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("2FA response interrupted: {}", e.getMessage());
        } catch (TimeoutException | ExecutionException | MalformedURLException e) {
            logger.warn("Error in 2FA code: {}", e.getMessage(), e);
        } catch (JsonParseException e) {
            logger.warn("Error parsing 2FA response: {}", e.getMessage(), e);
        }
    }

    /**
     * Extracts the location URL from HTTP response headers.
     *
     * @param headers The HTTP response headers
     * @return The location URL or empty string if not found
     */
    private String extractLocationFromHeaders(HttpFields headers) {
        for (HttpField header : headers) {
            String headerName = header.getName();
            if ("location".equalsIgnoreCase(headerName)) {
                String value = header.getValue();
                logger.trace("Found location header: {}", value);
                return value != null ? value : "";
            }
            if ("extension-pragma".equalsIgnoreCase(headerName)) {
                logger.trace("Found extension-pragma header: {}", header.getValue());
            }
        }
        return "";
    }

    /**
     * Downloads and processes a captcha image.
     *
     * @param captchaURL The URL of the captcha image
     */
    protected void downloadCaptcha(String captchaURL) {
        String imgUrl = captchaURL.startsWith("/") ? "https://account.xiaomi.com" + captchaURL : captchaURL;

        if (imgUrl.isEmpty() || !imgUrl.startsWith("http")) {
            logger.debug("Captcha URL invalid: {}", captchaURL);
            return;
        }

        logger.debug("Downloading captcha from: {}", imgUrl);
        try {
            fetchAndInformImage(imgUrl, REQUEST_TIMEOUT_SECONDS, "miio-captcha-");
        } catch (MiCloudException e) {
            logger.debug("Error while downloading captcha: {}", e.getMessage());
        }
    }
}
