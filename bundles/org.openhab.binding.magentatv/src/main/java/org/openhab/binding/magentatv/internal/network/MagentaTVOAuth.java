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
package org.openhab.binding.magentatv.internal.network;

import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.*;
import static org.openhab.binding.magentatv.internal.MagentaTVUtil.*;

import java.net.HttpCookie;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.HttpMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.magentatv.internal.MagentaTVException;
import org.openhab.binding.magentatv.internal.MagentaTVGsonDTO.OAuthAuthenticateResponse;
import org.openhab.binding.magentatv.internal.MagentaTVGsonDTO.OAuthAuthenticateResponseInstanceCreator;
import org.openhab.binding.magentatv.internal.MagentaTVGsonDTO.OAuthTokenResponse;
import org.openhab.binding.magentatv.internal.MagentaTVGsonDTO.OAuthTokenResponseInstanceCreator;
import org.openhab.binding.magentatv.internal.MagentaTVGsonDTO.OauthCredentials;
import org.openhab.binding.magentatv.internal.MagentaTVGsonDTO.OauthCredentialsInstanceCreator;
import org.openhab.binding.magentatv.internal.MagentaTVGsonDTO.OauthKeyValue;
import org.openhab.binding.magentatv.internal.handler.MagentaTVControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link MagentaTVOAuth} class implements the OAuth authentication, which
 * is used to query the userID from the Telekom platform.
 *
 * @author Markus Michels - Initial contribution
 *
 *         Deutsche Telekom uses an OAuth-based authentication to access the EPG portal. The
 *         communication between the MR and the remote app requires a pairing before the receiver could be
 *         controlled by sending keys etc. The so called userID is not directly derived from any local parameters
 *         (like terminalID as a has from the mac address), but will be returned as a result from the OAuth
 *         authentication. This will be performed in 3 steps
 *         1. Get OAuth credentials -> Service URL, Scope, Secret, Client ID
 *         2. Get OAth Token -> authentication token for step 3
 *         3. Authenticate, which then provides the userID (beside other parameters)
 *
 */
@NonNullByDefault
public class MagentaTVOAuth {
    private final Logger logger = LoggerFactory.getLogger(MagentaTVOAuth.class);
    private HttpClient httpClient;
    private final Gson gson;
    private List<HttpCookie> cookies = new ArrayList<>();

    public MagentaTVOAuth(HttpClient httpClient) {
        this.httpClient = httpClient;
        gson = new GsonBuilder().registerTypeAdapter(OauthCredentials.class, new OauthCredentialsInstanceCreator())
                .registerTypeAdapter(OAuthTokenResponse.class, new OAuthTokenResponseInstanceCreator())
                .registerTypeAdapter(OAuthAuthenticateResponse.class, new OAuthAuthenticateResponseInstanceCreator())
                .create();
    }

    public String getUserId(String accountName, String accountPassword) throws MagentaTVException {
        logger.debug("Authenticate with account {}", accountName);
        if (accountName.isEmpty() || accountPassword.isEmpty()) {
            throw new MagentaTVException("Credentials for OAuth missing, check thing config!");
        }

        String url = "";
        Properties httpHeader = initHttpHeader();
        String postData = "";
        String httpResponse = "";

        // OAuth autentication results
        String oAuthScope = "";
        String oAuthService = "";
        String epghttpsurl = "";

        // Get credentials
        url = OAUTH_GET_CRED_URL + ":" + OAUTH_GET_CRED_PORT + OAUTH_GET_CRED_URI;
        httpHeader.setProperty(HttpHeader.HOST.toString(), substringAfterLast(OAUTH_GET_CRED_URL, "/"));
        httpResponse = httpRequest(HttpMethod.GET, url, httpHeader, "");
        OauthCredentials cred = gson.fromJson(httpResponse, OauthCredentials.class);
        epghttpsurl = getString(cred.epghttpsurl);
        if (epghttpsurl.isEmpty()) {
            throw new MagentaTVException("Unable to determine EPG url");
        }
        if (!epghttpsurl.contains("/EPG")) {
            epghttpsurl = epghttpsurl + "/EPG";
        }
        logger.debug("OAuth: epghttpsurl = {}", epghttpsurl);

        // get OAuth data from response
        if (cred.sam3Para != null) {
            for (OauthKeyValue si : cred.sam3Para) {
                logger.trace("sam3Para.{} = {}", si.key, si.value);
                if (si.key.equalsIgnoreCase("oAuthScope")) {
                    oAuthScope = si.value;
                } else if (si.key.equalsIgnoreCase("SAM3ServiceURL")) {
                    oAuthService = si.value;
                }
            }
        }
        if (oAuthScope.isEmpty() || oAuthService.isEmpty()) {
            throw new MagentaTVException("OAuth failed: Can't get Scope and Service: " + httpResponse);
        }

        // Get OAuth token (New flow based on WebTV)
        String userId = "";
        String terminalId = UUID.randomUUID().toString();
        String cnonce = MagentaTVControl.computeMD5(terminalId);

        url = oAuthService + "/oauth2/tokens";
        postData = MessageFormat.format(
                "password={0}&scope={1}+offline_access&grant_type=password&username={2}&x_telekom.access_token.format=CompactToken&x_telekom.access_token.encoding=text%2Fbase64&client_id=10LIVESAM30000004901NGTVWEB0000000000000",
                urlEncode(accountPassword), oAuthScope, urlEncode(accountName));
        url = oAuthService + "/oauth2/tokens";
        httpResponse = httpRequest(HttpMethod.POST, url, httpHeader, postData);
        OAuthTokenResponse resp = gson.fromJson(httpResponse, OAuthTokenResponse.class);
        if (resp.accessToken.isEmpty()) {
            String errorMessage = MessageFormat.format("Unable to authenticate: accountName={0}, rc={1} ({2})",
                    accountName, getString(resp.errorDescription), getString(resp.error));
            logger.warn("{}", errorMessage);
            throw new MagentaTVException(errorMessage);
        }
        logger.debug("OAuth: Access Token retrieved");

        // General authentication
        logger.debug("OAuth: Generating CSRF token");
        url = "https://api.prod.sngtv.magentatv.de/EPG/JSON/Authenticate";
        httpHeader = initHttpHeader();
        httpHeader.setProperty(HttpHeader.HOST.toString(), "api.prod.sngtv.magentatv.de");
        httpHeader.setProperty("Origin", "https://web.magentatv.de");
        httpHeader.setProperty(HttpHeader.REFERER.toString(), "https://web.magentatv.de/");
        postData = "{\"areaid\":\"1\",\"cnonce\":\"" + cnonce + "\",\"mac\":\"" + terminalId
                + "\",\"preSharedKeyID\":\"NGTV000001\",\"subnetId\":\"4901\",\"templatename\":\"NGTV\",\"terminalid\":\""
                + terminalId
                + "\",\"terminaltype\":\"WEB-MTV\",\"terminalvendor\":\"WebTV\",\"timezone\":\"Europe/Berlin\",\"usergroup\":\"-1\",\"userType\":3,\"utcEnable\":1}";
        httpResponse = httpRequest(HttpMethod.POST, url, httpHeader, postData);
        String csrf = "";
        for (HttpCookie c : cookies) { // get CRSF Token
            String value = c.getValue();
            if (value.contains("CSRFSESSION")) {
                csrf = substringBetween(value, "CSRFSESSION" + "=", ";");
            }
        }
        if (csrf.isEmpty()) {
            throw new MagentaTVException("OAuth: Unable to get CSRF token!");
        }

        // Final step: Retrieve userId
        url = "https://api.prod.sngtv.magentatv.de/EPG/JSON/DTAuthenticate";
        httpHeader = initHttpHeader();
        httpHeader.setProperty(HttpHeader.HOST.toString(), "api.prod.sngtv.magentatv.de");
        httpHeader.setProperty("Origin", "https://web.magentatv.de");
        httpHeader.setProperty(HttpHeader.REFERER.toString(), "https://web.magentatv.de/");
        httpHeader.setProperty("X_CSRFToken", csrf);
        postData = "{\"areaid\":\"1\",\"cnonce\":\"" + cnonce + "\",\"mac\":\"" + terminalId + "\","
                + "\"preSharedKeyID\":\"NGTV000001\",\"subnetId\":\"4901\",\"templatename\":\"NGTV\","
                + "\"terminalid\":\"" + terminalId + "\",\"terminaltype\":\"WEB-MTV\",\"terminalvendor\":\"WebTV\","
                + "\"timezone\":\"Europe/Berlin\",\"usergroup\":\"\",\"userType\":\"1\",\"utcEnable\":1,"
                + "\"accessToken\":\"" + resp.accessToken
                + "\",\"caDeviceInfo\":[{\"caDeviceId\":\"4ef4d933-9a43-41d3-9e3a-84979f22c9eb\","
                + "\"caDeviceType\":8}],\"connectType\":1,\"osversion\":\"Mac OS 10.15.7\",\"softwareVersion\":\"1.33.4.3\","
                + "\"terminalDetail\":[{\"key\":\"GUID\",\"value\":\"" + terminalId + "\"},"
                + "{\"key\":\"HardwareSupplier\",\"value\":\"WEB-MTV\"},{\"key\":\"DeviceClass\",\"value\":\"TV\"},"
                + "{\"key\":\"DeviceStorage\",\"value\":0},{\"key\":\"DeviceStorageSize\",\"value\":0}]}";
        httpResponse = httpRequest(HttpMethod.POST, url, httpHeader, postData);
        OAuthAuthenticateResponse authResp = gson.fromJson(httpResponse, OAuthAuthenticateResponse.class);
        if (authResp.userID.isEmpty()) {
            String errorMessage = MessageFormat.format("Unable to authenticate: accountName={0}, rc={1} {2}",
                    accountName, getString(authResp.retcode), getString(authResp.desc));
            logger.warn("{}", errorMessage);
            throw new MagentaTVException(errorMessage);
        }
        userId = getString(authResp.userID);
        if (userId.isEmpty()) {
            throw new MagentaTVException("No userID received!");
        }
        String hashedUserID = MagentaTVControl.computeMD5(userId).toUpperCase();
        logger.trace("done, userID = {}", hashedUserID);
        return hashedUserID;
    }

    private String httpRequest(String method, String url, Properties headers, String data) throws MagentaTVException {
        String result = "";
        try {
            Request request = httpClient.newRequest(url).method(method).timeout(NETWORK_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);
            for (Enumeration<?> e = headers.keys(); e.hasMoreElements();) {
                String key = (String) e.nextElement();
                String val = (String) headers.get(key);
                request.header(key, val);
            }
            if (method.equals(HttpMethod.POST)) {
                fillPostData(request, data);
            }
            if (!cookies.isEmpty()) {
                // Add cookies
                String cookieValue = "";
                for (HttpCookie c : cookies) {
                    cookieValue = cookieValue + substringBefore(c.getValue(), ";") + "; ";
                }
                request.header("Cookie", substringBeforeLast(cookieValue, ";"));
            }
            logger.debug("OAuth: HTTP Request\n\tHTTP {} {}\n\tData={}", method, url, data.isEmpty() ? "<none>" : data);
            logger.trace("\n\tHeaders={}\tCookies={}", request.getHeaders(), request.getCookies());

            ContentResponse contentResponse = request.send();
            result = contentResponse.getContentAsString().replace("\t", "").replace("\r\n", "").trim();
            int status = contentResponse.getStatus();
            logger.debug("OAuth: HTTP Response\n\tStatus={} {}\n\tData={}", status, contentResponse.getReason(),
                    result.isEmpty() ? "<none>" : result);
            logger.trace("\n\tHeaders={}", contentResponse.getHeaders());

            // validate response, API errors are reported as Json
            HttpFields responseHeaders = contentResponse.getHeaders();
            for (HttpField f : responseHeaders) {
                if (f.getName().equals("Set-Cookie")) {
                    HttpCookie c = new HttpCookie(f.getName(), f.getValue());
                    cookies.add(c);
                }
            }

            if (status != HttpStatus.OK_200) {
                String error = "HTTP reqaest failed for URL " + url + ", Code=" + contentResponse.getReason() + "("
                        + status + ")";
                throw new MagentaTVException(error);
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            String error = "HTTP reqaest failed for URL " + url;
            logger.info("{}", error, e);
            throw new MagentaTVException(e, error);
        }
        return result;
    }

    private Properties initHttpHeader() {
        Properties httpHeader = new Properties();
        httpHeader.setProperty(HttpHeader.ACCEPT.toString(), "*/*");
        httpHeader.setProperty(HttpHeader.ACCEPT_LANGUAGE.toString(), "en-US,en;q=0.9,de;q=0.8");
        httpHeader.setProperty(HttpHeader.CACHE_CONTROL.toString(), "no-cache");
        return httpHeader;
    }

    private void fillPostData(Request request, String data) {
        if (!data.isEmpty()) {
            StringContentProvider postData;
            if (request.getHeaders().contains(HttpHeader.CONTENT_TYPE)) {
                String contentType = request.getHeaders().get(HttpHeader.CONTENT_TYPE);
                postData = new StringContentProvider(contentType, data, StandardCharsets.UTF_8);
            } else {
                boolean json = data.startsWith("{");
                postData = new StringContentProvider(json ? "application/json" : "application/x-www-form-urlencoded",
                        data, StandardCharsets.UTF_8);
            }
            request.content(postData);
            request.header(HttpHeader.CONTENT_LENGTH, Long.toString(postData.getLength()));
        }
    }

    private String getString(@Nullable String value) {
        return value != null ? value : "";
    }

    private String urlEncode(String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }
}
