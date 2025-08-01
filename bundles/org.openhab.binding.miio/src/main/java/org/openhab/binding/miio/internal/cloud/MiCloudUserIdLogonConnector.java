/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link MiCloudUserIdLogonConnector} class is used for connecting to the Xiaomi cloud access
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiCloudUserIdLogonConnector extends MiCloudConnector {
    private final Logger logger = LoggerFactory.getLogger(MiCloudUserIdLogonConnector.class);
    private @Nullable Request fa;

    public MiCloudUserIdLogonConnector(String username, String password, HttpClient httpClient)
            throws MiCloudException {
        super(username, password, httpClient);
    }

    public MiCloudUserIdLogonConnector(@Nullable String username, @Nullable String password, HttpClient httpClient,
            @Nullable String clientId, @Nullable String userId, @Nullable String serviceToken,
            @Nullable String ssecurity) throws MiCloudException {
        super(username, password, httpClient, clientId, userId, serviceToken, ssecurity);
    }

    @Override
    public synchronized boolean login() {
        logger.info(" client Id={}", clientId);

        return login("");
    }

    @Override
    public synchronized boolean login(String capchaResponse) {
        if (!checkCredentials()) {
            return false;
        }
        if (!userId.isEmpty() && !serviceToken.isEmpty()) {
            return true;
        }
        logger.debug("Xiaomi cloud login with userid {}", username);
        try {
            if (loginRequest(capchaResponse)) {
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

    protected boolean loginRequest(String capchaResponse) throws MiCloudException {
        try {
            startClient();

            String sign = this.sign.isEmpty() ? loginStep1() : this.sign;
            String location;
            if (!sign.startsWith("http")) {
                this.sign = sign;
                location = loginStep2(sign, capchaResponse);
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
            JsonElement resp = JsonParser.parseString(CloudUtil.parseJson(content));
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

    private String loginStep2(String sign, String capchaResponse) throws MiIoCryptoException, InterruptedException,
            TimeoutException, ExecutionException, MiCloudException, JsonSyntaxException, JsonParseException {
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
        if (!capchaResponse.isEmpty()) {
            fields.put("captCode", capchaResponse);
            logger.debug("Logon with capcha response {}", capchaResponse);
        } else {
            logger.debug("Logon step 2 without capcha response");
        }

        fields.put("_json", "true");

        request.content(new FormContentProvider(fields));
        responseStep2 = request.send();

        final String content2 = responseStep2.getContentAsString();
        logger.trace("Xiaomi login step 2 response = {}", responseStep2);
        logger.trace("Xiaomi login step 2 content = {}", content2);

        JsonElement resp2 = JsonParser.parseString(CloudUtil.parseJson(content2));
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
        String capchaUrl = jsonResp.getCapchaUrl();
        String callbackUrl = jsonResp.getCallback();

        logger.trace("Xiaomi login ssecurity = {}", ssecurity);
        logger.trace("Xiaomi login userId = {}", userId);
        logger.trace("Xiaomi login cUserId = {}", cUserId);
        logger.trace("Xiaomi login passToken = {}", passToken);
        logger.trace("Xiaomi login location = {}", location);
        logger.trace("Xiaomi login code = {}", code);
        logger.trace("Xiaomi login capcha URL = {}", capchaUrl);
        logger.trace("Xiaomi login callbackUrl = {}", callbackUrl);
        if ("87001".equals(code)) {
            logger.debug("Xiaomi Cloud Step2 failed capcha: {}", CloudUtil.parseJson(content2));
            updateLoginState(CloudLoginState.CAPTCHA_FAILED);
        }

        if (jsonResp.getSecurityStatus() == null || 0 != jsonResp.getSecurityStatus()) {
            logger.debug("Xiaomi Cloud Step2 response: {}", CloudUtil.parseJson(content2));
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

            if (jsonResp.getNotificationUrl() != null && !jsonResp.getNotificationUrl().isEmpty()) {
                logger.info("Click submit and get token. Then enter the token in OH:\r\n{} ",
                        jsonResp.getNotificationUrl());
                get2factory(jsonResp.getNotificationUrl());
                updateLoginState(CloudLoginState.AWAITING_2FA);
            }

        }

        if (capchaUrl != null && !capchaUrl.isEmpty()) {
            updateLoginState(CloudLoginState.AWAITING_CAPTCHA);
            downloadCaptcha(capchaUrl);
        }

        if (logger.isTraceEnabled()) {
            dumpCookies(url, false);
        }
        if (!location.isEmpty()) {
            return location;
        } else {
            if (loginState.equals(CloudLoginState.AWAITING_2FA) || loginState.equals(CloudLoginState.AWAITING_CAPTCHA)
                    || loginState.equals(CloudLoginState.CAPTCHA_FAILED)) {
                logger.debug(" retry with new capcha/2fa");
                throw new MiCloudException("Error getting logon location URL. Return code: " + code);
            } else {
                throw new MiCloudException("Error getting logon location URL. Return code: " + code);
            }
        }
    }

    private void get2factory(String url) {
        try {
            String agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0";
            CookieStore cookieStore = httpClient.getCookieStore();
            httpClient.setCookieStore(new HttpCookieStore.Empty());
            logger.debug("Trying to request code from {}", url);
            Request request = httpClient.newRequest(url).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            // request.agent("Android-9.1.1-1.0.0-ONEPLUS A3010-136-" + AGENT_ID);
            request.agent(agent);
            request.method(HttpMethod.GET);

            Fields stor = request.getParams();
            debugRequest(request);

            String newurl = url.replace("authStart", "list");

            request = httpClient.newRequest(newurl).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            request.agent(agent);
            debugRequest(request);

            request = httpClient.newRequest("https://account.xiaomi.com/identity/auth/verifyEmail?_flag=8&_json=true")
                    .timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            request.agent(agent);
            debugRequest(request);

            long ms = java.time.Instant.now().toEpochMilli();
            String mailticket = "https://account.xiaomi.com/identity/auth/sendEmailTicket?_dc=" + String.valueOf(ms);
            request = httpClient.newRequest(mailticket).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            request.agent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0");

            request.method(HttpMethod.POST);

            Fields fields = new Fields();
            fields.put("retry", "0");
            fields.put("icode", "");
            fields.put("_json", "true");
            request.content(new FormContentProvider(fields));

            debugRequest(request);

            String verifyticket = "https://account.xiaomi.com/identity/auth/verifyEmail?_dc=" + String.valueOf(ms);

            request = httpClient.newRequest(verifyticket).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            request.method(HttpMethod.POST);
            this.fa = request;
            httpClient.setCookieStore(cookieStore);

        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Error requesting 2FA code: {}", e.getMessage(), e);
        }
    }

    public void FAResponse(String faCode) {
        try {
            Fields fields = new Fields();
            fields.put("_flag", "0");
            fields.put("trust", "false");
            fields.put("_json", "true");
            fields.put("ticket", faCode.trim());
            Request fa = this.fa;
            if (fa == null) {
                logger.warn("2FA request not initialized");
                return;
            }
            fa.content(new FormContentProvider(fields));
            ContentResponse result = debugRequest(fa);
            JsonElement resultJson = JsonParser.parseString(CloudUtil.parseJson(result.getContentAsString()));
            String location = resultJson.getAsJsonObject().get("location").getAsString();

            Request request = httpClient.newRequest(location).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            request.method(HttpMethod.POST);
            ContentResponse endresult = debugRequest(request);

            HttpFields h = endresult.getHeaders();

            location = "";
            for (HttpField header : h) {
                if ("extension-pragma".equals(header.getName())) {
                    logger.trace("end ss= {}", header.getValue());
                }
                if ("location".equals(header.getName())) {
                    location = header.getValue();
                    logger.trace("end ss= {}", header.getValue());
                }
            }

            if (!location.isBlank()) {
                final ContentResponse response = loginStep3(location);
                logger.trace("Xiaomi login step 2 startus = {}", response.getStatus());
                logger.trace("Xiaomi login step 2 response = {}", response);
                logger.trace("Xiaomi login step 2 content = {}", response.toString());
                logger.trace("Xiaomi login step 2 header = {}", response.getHeaders().toString());
                logger.trace("Xiaomi login step 2 content = {}", response.getContentAsString());
            }

        } catch (InterruptedException | TimeoutException | ExecutionException | MalformedURLException e) {
            logger.warn("Error in 2FA code: {}", e.getMessage(), e);
        }

        // Request request = httpClient.newRequest(cb).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        // request.agent("Android-9.1.1-1.0.0-ONEPLUS A3010-136-" + AGENT_ID);
    }

    protected void downloadCaptcha(String capchaURL) {
        String imgUrl = capchaURL.startsWith("/") ? "https://account.xiaomi.com" + capchaURL : capchaURL;

        if (!imgUrl.isEmpty() && imgUrl.startsWith("http")) {
            logger.debug("Downloading captcha from: {}", imgUrl);

            try {

                // Request request = httpClient.newRequest(imgUrl).timeout(REQUEST_TIMEOUT_SECONDS,
                // TimeUnit.SECONDS).agent(USERAGENT);
                // request.agent(USERAGENT);
                // final ContentResponse responseStep2 = request.send();

                final ContentResponse responseStep2 = httpClient.newRequest(imgUrl)
                        .timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).agent(USERAGENT).send();
                if (responseStep2 == null) {
                    logger.warn("Error downloading capcha ");
                    return;
                }
                logger.trace("Xiaomi login step 2 response = {}", responseStep2.getContentAsString());
                // logger.trace("Xiaomi login step 2 content = {}", content2);
                final byte[] content = responseStep2.getContent();
                String fileDest = "capcha.jpg";
                try {
                    Path path = Paths.get(fileDest);
                    logger.info("Saved to {} -> {} bytes", path.getFileName().toAbsolutePath(), content.length);
                    Files.write(path, content);
                } catch (IOException e) {
                    logger.warn("Error writing {}: {}", fileDest, e.getMessage(), e);
                }
                informImageListeners(content);

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.debug("Error while executing request to {} :{}", capchaURL, e.getMessage());
            }
        } else {
            logger.debug("Captcha URL wrong: {}", capchaURL);

        }
    }
}
