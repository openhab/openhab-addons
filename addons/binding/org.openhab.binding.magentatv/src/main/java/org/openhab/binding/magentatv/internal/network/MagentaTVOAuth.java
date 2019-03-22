/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.magentatv.internal.MagentaTVControl;
import org.openhab.binding.magentatv.internal.MagentaTVException;
import org.openhab.binding.magentatv.internal.MagentaTVLogger;

/**
 * The {@link MagentaTVOAuth} class implements the OAuth authentication, which
 * is used to query the userID from the Telekom platform.
 *
 * @author Mathias Gisch - Initial contribution - bash script
 * @author Markus Michels - Initial contribution - adapted to Java
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
public class MagentaTVOAuth {
    private final MagentaTVLogger logger = new MagentaTVLogger(MagentaTVOAuth.class, "OAuth");

    private final String OAUTH_GET_CRED_URL = "https://slbedmfk11100.prod.sngtv.t-online.de";
    private final String OAUTH_GET_CRED_PORT = "33428";
    private final String OAUTH_GET_CRED_URI = "/EDS/JSON/Login?UserID=Guest";
    private final String OAUTH_USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_3 like Mac OS X) AppleWebKit/603.3.8 (KHTML, like Gecko) Mobile/14G60 (400962928)";

    // OAuth autentication results
    private String oAuthScope = "";
    private String oAuthService = "";
    private String oAuthClientSecret = "";
    private String oAuthClientId = "";
    private String accessToken = "";
    private String epghttpsurl = "";

    public String getOAuthCredentials(String accountName, String accountPassword) throws Exception {
        logger.info("Authenticate with account '{}'", accountName);
        Properties httpHeader = initHttpHeader();
        String url = "";
        String httpResponse = "";
        String postData = "";
        InputStream dataStream = null;
        JsonReader reader;
        JsonObject json;

        if (accountName.isEmpty() || accountPassword.isEmpty()) {
            throw new MagentaTVException("Credentials for OAuth missing, check thing config!");
        }

        try {
            httpHeader = initHttpHeader();
            url = OAUTH_GET_CRED_URL + ":" + OAUTH_GET_CRED_PORT + OAUTH_GET_CRED_URI;
            httpHeader.setProperty(HEADER_HOST, StringUtils.substringAfterLast(OAUTH_GET_CRED_URL, "/"));
            logger.trace("Get OAuth credentials from '{}'", url);
            httpResponse = HttpUtil.executeUrl(HTTP_GET, url, httpHeader, null, null, NETWORK_TIMEOUT);

            // Sample response:
            /*
             * { "enctytoken":"7FA9A6C05EDD873799392BBDDC5B7F34","encryptiontype":"0002",
             * "platformcode":"0200",
             * "epgurl":"http://appepmfk20005.prod.sngtv.t-online.de:33200",
             * "version":"MEM V200R008C15B070",
             * "epghttpsurl":"https://appepmfk20005.prod.sngtv.t-online.de:33207",
             * "rootCerAddr":
             * "http://appepmfk20005.prod.sngtv.t-online.de:33200/EPG/CA/iptv_ca.der",
             * "upgAddr4IPTV":
             * "https://slbedifk11100.prod.sngtv.t-online.de:33428/EDS/jsp/upgrade.jsp",
             * "upgAddr4OTT":
             * "https://slbedmfk11100.prod.sngtv.t-online.de:33428/EDS/jsp/upgrade.jsp,https://slbedmfk11100.prod.sngtv.t-online.de:33428/EDS/jsp/upgrade.jsp",
             * "sam3Para": [
             * {"key":"SAM3ServiceURL","value":"https://accounts.login.idm.telekom.com"},
             * {"key":"OAuthClientSecret","value":"21EAB062-C4EE-489C-BC80-6A65397F3F96"},
             * {"key":"OAuthScope","value":"ngtvepg"},
             * {"key":"OAuthClientId","value":"10LIVESAM30000004901NGTV0000000000000000"} ]
             * }
             */
            logger.trace("Response = '{}'", httpResponse);
            reader = Json.createReader(new StringReader(httpResponse));
            logger.trace("read JSon", httpResponse);
            json = reader.readObject();
            logger.trace("get epghttpsurl", httpResponse);
            epghttpsurl = getJString(json, "epghttpsurl", "");
            if (epghttpsurl.isEmpty()) {
                throw new MagentaTVException("OAuth failed: Unable to determine EPG url");
            }
            if (!epghttpsurl.contains("/EPG")) {
                epghttpsurl = epghttpsurl + "/EPG";
            }
            logger.trace("epghttpsurl = '{}'", epghttpsurl);
            JsonArray sam3Para = json.getJsonArray("sam3Para");
            if (sam3Para != null) {
                for (JsonValue si : sam3Para) {
                    JsonObject sevent = si.asJsonObject();
                    String key = sevent.getString("key");
                    String value = sevent.getString("value");
                    logger.trace("sam3Para.{} = '{}'", key, value);
                    if (key.equalsIgnoreCase("oAuthScope")) {
                        oAuthScope = value;
                    } else if (key.equalsIgnoreCase("SAM3ServiceURL")) {
                        oAuthService = value;
                    } else if (key.equalsIgnoreCase("oAuthClientSecret")) {
                        oAuthClientSecret = value;
                    } else if (key.equalsIgnoreCase("oAuthClientId")) {
                        oAuthClientId = value;
                    }
                }
            }

            if (oAuthScope.isEmpty() || oAuthService.isEmpty() || oAuthClientSecret.isEmpty()
                    || oAuthClientId.isEmpty()) {
                String errorMessage = MessageFormat.format("Invalid getOAuthCredentials result: {}", httpResponse);
                throw new MagentaTVException(errorMessage);
            }
        } catch (Exception e) {
            String errorMessage = MessageFormat.format(
                    "Authentication failed (Get OAuth credentials; Service URL={0}, accountName={1} - {2} ({3})",
                    oAuthService, accountName, e.getMessage(), e.getClass());
            logger.fatal(errorMessage);
            logger.trace("response='{}'", httpResponse);
            throw new MagentaTVException(errorMessage, e);
        }

        try {
            // getOAuthToken
            httpHeader.setProperty(HEADER_HOST, StringUtils.substringAfterLast(oAuthService, "/"));
            url = oAuthService + "/oauth2/tokens";
            logger.trace("Get OAuth Token from '{}'", url);
            // build url parameters:
            // {0} = user id
            // {1} = password
            // {2} = oAuthScope
            // {3} = oAuthClientId
            // {4} = oAuthClientSecret
            // Data: = ;
            postData = MessageFormat.format(
                    "grant_type=password&username={0}&password={1}&scope={2}%20offline_access&client_id={3}&client_secret={4}&x_telekom.access_token.format=CompactToken&x_telekom.access_token.encoding=text%2Fbase64",
                    URLEncoder.encode(accountName, "UTF-8"), URLEncoder.encode(accountPassword, "UTF-8"), oAuthScope,
                    oAuthClientId, oAuthClientSecret);
            dataStream = new ByteArrayInputStream(postData.getBytes(Charset.forName("UTF-8")));
            httpResponse = HttpUtil.executeUrl(HTTP_POST, url, httpHeader, dataStream, null, NETWORK_TIMEOUT);
            reader = Json.createReader(new StringReader(httpResponse));
            json = reader.readObject();
            String errorDesc = getJString(json, "error_description", "");
            if (!errorDesc.isEmpty()) {
                String error = getJString(json, "error", "");
                String errorMessage = MessageFormat.format("Authentication for account {0} failed: {1} - {2}",
                        accountName, errorDesc, error);
                logger.fatal(errorMessage);
                throw new MagentaTVException(errorMessage);
            }

            accessToken = getJString(json, "access_token", "");
            if (accessToken.isEmpty()) {
                throw new MagentaTVException("OAuth: Can't create access token");
            }
        } catch (Exception e) {
            String errorMessage = MessageFormat.format(
                    "Authentication failed (Get OAuth token; Service URL={0}, accountName={1} - {2} ({3})",
                    oAuthService, accountName, e.getMessage(), e.getClass());
            logger.fatal(errorMessage);
            logger.trace("response='{}'", httpResponse);
            throw new MagentaTVException(errorMessage, e);
        }

        String retcode = "";
        String retmsg = "";
        try {
            // authenticateDevice
            logger.trace("Authenticate with token");
            String uuid = UUID.randomUUID().toString();
            String cnonce = MagentaTVControl.computeMD5(uuid);

            // build url string
            // {0} = uuid
            // {1} = uuid
            // {2} = uuid
            // {3} = accessToken
            // {4} = cnonce
            // {5} = uuid
            postData = MessageFormat.format(
                    "'{'\"userType\": 1,\"terminalid\": \"{0}\",\"mac\":\"{1}\",\"terminaltype\":\"Iphone\",\"utcEnable\":1,\"timezone\":\"Europe/Berlin\",\"terminalvendor\":\"iPhone5\",\"osversion\":\"iOS10.3.3\",\"softwareVersion\":\"2.3.10.26\",\"terminalDetail\":['{'\"key\":\"HardwareSupplier\",\"value\":\"MyPhone\"'}','{'\"key\":\"DeviceClass\",\"value\":\"IPhone\"'}','{'\"key\":\"DeviceStorage\",\"value\":\"1\"'}','{'\"key\":\"DeviceStorageSize\",\"value\":12475'}','{'\"key\":\"GUID\",\"value\":\"{2}\"'}'],\"connectType\":1,\"reconnect\":true,\"accessToken\":\"{3}\",\"cnonce\":\"{4}\",\"caDeviceInfo\":['{'\"caDeviceType\":6,\"caDeviceId\":\"{5}\"'}'],\"preSharedKeyID\":\"NGTV000001\"'}'",
                    // uuid, uuid, cnonce, uuid);
                    "", "", "", accessToken, cnonce, "");
            httpHeader.setProperty(HEADER_HOST, StringUtils.substringAfterLast(epghttpsurl, "/"));
            httpHeader.setProperty(HEADER_CONTENT_TYPE, "text/plain;charset=UTF-8");
            url = epghttpsurl + "/JSON/DTAuthenticate?SID=user&T=Iphone";
            dataStream = new ByteArrayInputStream(postData.getBytes(Charset.forName("UTF-8")));
            httpResponse = HttpUtil.executeUrl(HTTP_POST, url, httpHeader, dataStream, null, NETWORK_TIMEOUT);
            reader = Json.createReader(new StringReader(httpResponse));
            json = reader.readObject();
            retcode = getJString(json, "retcode", "");
            if (retcode.isEmpty() || !retcode.equals("0")) {
                retmsg = getJString(json, "desc", "");
                String errorMessage = MessageFormat.format("Unable to authenticate: accountName={}, rc={} - {}",
                        accountName, retcode, retmsg);
                logger.fatal(errorMessage);
                throw new MagentaTVException(errorMessage);
            }

            String epgurl = getJString(json, "epgurl", "");
            String userID = getJString(json, "userID", "");
            if ((userID == null) || userID.isEmpty()) {
                throw new MagentaTVException("OAuth failed, no userID received!");
            }
            String hashedUserID = MagentaTVControl.computeMD5(userID).toUpperCase();
            logger.trace("Done, userID = '{}'", hashedUserID);
            return hashedUserID;
        } catch (Exception e) {
            String errorMessage = MessageFormat.format(
                    "Authentication failed (Authenticate; Service URL={0}, accountName={1} - rc={2} - {3} ({4} - {5})",
                    oAuthService, accountName, retcode, retmsg, e.getMessage(), e.getClass());
            logger.fatal(errorMessage);
            logger.trace("response='{}'", httpResponse);
            throw new MagentaTVException(errorMessage, e);
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

    private String getJString(JsonObject json, String key, String defaultValue) {
        if (json != null) {
            return json.containsKey(key) ? json.getString(key) : defaultValue;
        }
        return defaultValue;
    }
}
