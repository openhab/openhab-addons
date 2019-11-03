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
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.magentatv.internal.handler.MagentaTVControl;
import org.openhab.binding.magentatv.internal.handler.MagentaTVGson.MROAuthGson.OAuthAutenhicateResponse;
import org.openhab.binding.magentatv.internal.handler.MagentaTVGson.MROAuthGson.OAuthTokenResponse;
import org.openhab.binding.magentatv.internal.handler.MagentaTVGson.MROAuthGson.OauthCredentials;
import org.openhab.binding.magentatv.internal.handler.MagentaTVGson.MROAuthGson.OauthKeyValue;
import org.openhab.binding.magentatv.internal.utils.MagentaTVException;
import org.openhab.binding.magentatv.internal.utils.MagentaTVLogger;

import com.google.gson.Gson;

/**
 * The {@link MagentaTVOAuth} class implements the OAuth authentication, which
 * is used to query the userID from the Telekom platform.
 *
 * @author Markus Michels - Initial contribution
 * @author Mathias Gisch - initial bash script
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
    private final MagentaTVLogger logger = new MagentaTVLogger(MagentaTVOAuth.class, "OAuth");

    @SuppressWarnings("null")
    public String getOAuthCredentials(String accountName, String accountPassword) throws MagentaTVException {
        logger.info("Authenticate with account {0}", accountName);
        if (accountName.isEmpty() || accountPassword.isEmpty()) {
            throw new MagentaTVException("Credentials for OAuth missing, check thing config!");
        }

        String step = "initialize";
        String url = "";
        Properties httpHeader;
        String postData = "";
        String httpResponse = "";
        InputStream dataStream = null;
        Gson gson = new Gson();

        // OAuth autentication results
        String oAuthScope = "";
        String oAuthService = "";
        String oAuthClientSecret = "";
        String oAuthClientId = "";
        String accessToken = "";
        String epghttpsurl = "";
        String retcode = "";
        String retmsg = "";

        try {
            step = "get credentials";
            httpHeader = initHttpHeader();
            url = OAUTH_GET_CRED_URL + ":" + OAUTH_GET_CRED_PORT + OAUTH_GET_CRED_URI;
            httpHeader.setProperty(HEADER_HOST, StringUtils.substringAfterLast(OAUTH_GET_CRED_URL, "/"));
            logger.trace("{0} from {1}", step, url);

            httpResponse = HttpUtil.executeUrl(HTTP_GET, url, httpHeader, null, null, NETWORK_TIMEOUT);
            logger.trace("http response = {0}", httpResponse);
            OauthCredentials cred = gson.fromJson(httpResponse, OauthCredentials.class);
            epghttpsurl = cred.epghttpsurl;
            if (epghttpsurl.isEmpty()) {
                throw new MagentaTVException("Unable to determine EPG url");
            }
            if (!epghttpsurl.contains("/EPG")) {
                epghttpsurl = epghttpsurl + "/EPG";
            }
            logger.trace("epghttpsurl = {0}", epghttpsurl);

            // get OAuth data from response

            if (cred.sam3Para != null) {
                for (OauthKeyValue si : cred.sam3Para) {
                    logger.trace("sam3Para.{0} = {1}", si.key, si.value);
                    if (si.key.equalsIgnoreCase("oAuthScope")) {
                        oAuthScope = si.value;
                    } else if (si.key.equalsIgnoreCase("SAM3ServiceURL")) {
                        oAuthService = si.value;
                    } else if (si.key.equalsIgnoreCase("oAuthClientSecret")) {
                        oAuthClientSecret = si.value;
                    } else if (si.key.equalsIgnoreCase("oAuthClientId")) {
                        oAuthClientId = si.value;
                    }
                }
            }

            if (oAuthScope.isEmpty() || oAuthService.isEmpty() || oAuthClientSecret.isEmpty()
                    || oAuthClientId.isEmpty()) {
                throw new MagentaTVException("OAuth failed: Can't get credentials: " + httpResponse);
            }

            // Get OAuth token
            step = "get token";
            url = oAuthService + "/oauth2/tokens";
            logger.debug("{0} from {1}", step, url);

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
            httpHeader.setProperty(HEADER_HOST, StringUtils.substringAfterLast(oAuthService, "/"));
            httpResponse = HttpUtil.executeUrl(HTTP_POST, url, httpHeader, dataStream, null, NETWORK_TIMEOUT);
            logger.trace("http response={0}", httpResponse);

            OAuthTokenResponse token = gson.fromJson(httpResponse, OAuthTokenResponse.class);
            if ((token.access_token == null) || token.access_token.isEmpty()) {
                String errorMessage = MessageFormat.format("Authentication for account {0} failed: {1} (rc={2})",
                        accountName, token.error_description, token.error);
                throw new MagentaTVException(errorMessage);
            }
            accessToken = token.access_token;

            // authenticateDevice
            step = "authenticate with token";
            logger.trace(step);
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
            logger.trace("http response={0}", httpResponse);
            OAuthAutenhicateResponse resp = gson.fromJson(httpResponse, OAuthAutenhicateResponse.class);
            if (resp.retcode.isEmpty() || !resp.retcode.equals("0")) {
                retmsg = resp.desc;
                String errorMessage = MessageFormat.format("Unable to authenticate: accountName={0}, rc={1} - {2}",
                        accountName, retcode, retmsg);
                logger.fatal(errorMessage);
                throw new MagentaTVException(errorMessage);
            }

            if ((resp.userID == null) || resp.userID.isEmpty()) {
                throw new MagentaTVException("No userID received!");
            }
            String hashedUserID = MagentaTVControl.computeMD5(resp.userID).toUpperCase();
            logger.trace("done, userID = {0}", hashedUserID);
            return hashedUserID;
        } catch (RuntimeException | IOException e) {
            throw new MagentaTVException(e,
                    "Unable to authenticate ({0} failed; serviceURL={1}, accountName={1} - rc={3}({4}", step,
                    oAuthService, accountName, retcode, retmsg);
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
}
