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
package org.openhab.binding.magentatv.internal.network;

import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.*;
import static org.openhab.binding.magentatv.internal.MagentaTVUtil.substringAfterLast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.UUID;

import javax.ws.rs.HttpMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.net.http.HttpUtil;
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
 *         Deutsche Telekom uses a OAuth-based authentication to access the EPG portal. The
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
    final Gson gson;

    public MagentaTVOAuth() {
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

        String step = "initialize";
        String url = "";
        Properties httpHeader;
        String postData = "";
        String httpResponse = "";
        InputStream dataStream = null;

        // OAuth autentication results
        String oAuthScope = "";
        String oAuthService = "";
        String epghttpsurl = "";
        String retcode = "";
        String retmsg = "";

        try {
            step = "get credentials";
            httpHeader = initHttpHeader();
            url = OAUTH_GET_CRED_URL + ":" + OAUTH_GET_CRED_PORT + OAUTH_GET_CRED_URI;
            httpHeader.setProperty(HEADER_HOST, substringAfterLast(OAUTH_GET_CRED_URL, "/"));
            logger.trace("{} from {}", step, url);
            httpResponse = HttpUtil.executeUrl(HttpMethod.GET, url, httpHeader, null, null, NETWORK_TIMEOUT_MS);
            logger.trace("http response = {}", httpResponse);
            OauthCredentials cred = gson.fromJson(httpResponse, OauthCredentials.class);
            epghttpsurl = getString(cred.epghttpsurl);
            if (epghttpsurl.isEmpty()) {
                throw new MagentaTVException("Unable to determine EPG url");
            }
            if (!epghttpsurl.contains("/EPG")) {
                epghttpsurl = epghttpsurl + "/EPG";
            }
            logger.debug("epghttpsurl = {}", epghttpsurl);

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

            // Get OAuth token
            step = "get token";
            url = oAuthService + "/oauth2/tokens";
            logger.debug("{} from {}", step, url);

            String userId = "";
            String uuid = UUID.randomUUID().toString();
            String cnonce = MagentaTVControl.computeMD5(uuid);
            // New flow based on WebTV
            postData = MessageFormat.format(
                    "password={0}&scope={1}+offline_access&grant_type=password&username={2}&x_telekom.access_token.format=CompactToken&x_telekom.access_token.encoding=text%2Fbase64&client_id=10LIVESAM30000004901NGTVWEB0000000000000",
                    URLEncoder.encode(accountPassword, UTF_8), oAuthScope, URLEncoder.encode(accountName, UTF_8));
            url = oAuthService + "/oauth2/tokens";
            dataStream = new ByteArrayInputStream(postData.getBytes(Charset.forName("UTF-8")));
            httpResponse = HttpUtil.executeUrl(HttpMethod.POST, url, httpHeader, dataStream, null, NETWORK_TIMEOUT_MS);
            logger.trace("http response={}", httpResponse);
            OAuthTokenResponse resp = gson.fromJson(httpResponse, OAuthTokenResponse.class);
            if (resp.accessToken.isEmpty()) {
                String errorMessage = MessageFormat.format("Unable to authenticate: accountName={0}, rc={1} ({2})",
                        accountName, getString(resp.errorDescription), getString(resp.error));
                logger.warn("{}", errorMessage);
                throw new MagentaTVException(errorMessage);
            }

            uuid = "t_" + MagentaTVControl.computeMD5(accountName);
            url = "https://web.magentatv.de/EPG/JSON/DTAuthenticate?SID=user&T=Mac_chrome_81";
            postData = "{\"userType\":1,\"terminalid\":\"" + uuid + "\",\"mac\":\"" + uuid + "\""
                    + ",\"terminaltype\":\"MACWEBTV\",\"utcEnable\":1,\"timezone\":\"Europe/Berlin\","
                    + "\"terminalDetail\":[{\"key\":\"GUID\",\"value\":\"" + uuid + "\"},"
                    + "{\"key\":\"HardwareSupplier\",\"value\":\"\"},{\"key\":\"DeviceClass\",\"value\":\"PC\"},"
                    + "{\"key\":\"DeviceStorage\",\"value\":\"1\"},{\"key\":\"DeviceStorageSize\",\"value\":\"\"}],"
                    + "\"softwareVersion\":\"\",\"osversion\":\"\",\"terminalvendor\":\"Unknown\","
                    + "\"caDeviceInfo\":[{\"caDeviceType\":6,\"caDeviceId\":\"" + uuid + "\"}]," + "\"accessToken\":\""
                    + resp.accessToken + "\",\"preSharedKeyID\":\"PC01P00002\",\"cnonce\":\"" + cnonce + "\"}";
            dataStream = new ByteArrayInputStream(postData.getBytes(Charset.forName("UTF-8")));
            logger.debug("HTTP POST {}, postData={}", url, postData);
            httpResponse = HttpUtil.executeUrl(HttpMethod.POST, url, httpHeader, dataStream, null, NETWORK_TIMEOUT_MS);

            logger.trace("http response={}", httpResponse);
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
        } catch (IOException e) {
            throw new MagentaTVException(e,
                    "Unable to authenticate {0}: {1} failed; serviceURL={2}, rc={3}/{4}, response={5}", accountName,
                    step, oAuthService, retcode, retmsg, httpResponse);
        }
    }

    private Properties initHttpHeader() {
        Properties httpHeader = new Properties();
        httpHeader.setProperty(HEADER_USER_AGENT, OAUTH_USER_AGENT);
        httpHeader.setProperty(HEADER_ACCEPT, "*/*");
        httpHeader.setProperty(HEADER_LANGUAGE, "de-de");
        httpHeader.setProperty(HEADER_CACHE_CONTROL, "no-cache");
        return httpHeader;
    }

    private String getString(@Nullable String value) {
        return value != null ? value : "";
    }
}
