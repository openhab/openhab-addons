/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.openhab.binding.bmwconnecteddrive.internal.utils.HTTPConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.utils.BimmerConstants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.HTTPConstants;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AuthTest} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class AuthTest {
    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);

    @Test
    public void testAuthServerMap() {
        Map<String, String> authServers = BimmerConstants.AUTH_SERVER_MAP;
        assertEquals(3, authServers.size(), "Number of Servers");
        Map<String, String> api = BimmerConstants.SERVER_MAP;
        assertEquals(3, api.size(), "Number of Servers");
    }

    @Test
    public void testTokenDecoding() {
        String headerValue = "https://www.bmw-connecteddrive.com/app/static/external-dispatch.html#access_token=SfXKgkEXeeFJkVqdD4XMmfUU224MRuyh&token_type=Bearer&expires_in=7199";
        HttpClientFactory hcf = mock(HttpClientFactory.class);
        when(hcf.getCommonHttpClient()).thenReturn(mock(HttpClient.class));
        when(hcf.createHttpClient(HTTPConstants.AUTH_HTTP_CLIENT_NAME)).thenReturn(mock(HttpClient.class));
        ConnectedDriveConfiguration config = new ConnectedDriveConfiguration();
        config.region = BimmerConstants.REGION_ROW;
        ConnectedDriveProxy dcp = new ConnectedDriveProxy(hcf, config);
        dcp.tokenFromUrl(headerValue);
        Token t = dcp.getToken();
        assertEquals("Bearer SfXKgkEXeeFJkVqdD4XMmfUU224MRuyh", t.getBearerToken(), "Token");
    }

    public void testRealTokenUpdate() {
        ConnectedDriveConfiguration config = new ConnectedDriveConfiguration();
        config.region = BimmerConstants.REGION_ROW;
        config.userName = "bla";
        config.password = "blub";
        HttpClientFactory hcf = mock(HttpClientFactory.class);
        when(hcf.getCommonHttpClient()).thenReturn(mock(HttpClient.class));
        when(hcf.createHttpClient(HTTPConstants.AUTH_HTTP_CLIENT_NAME)).thenReturn(mock(HttpClient.class));
        ConnectedDriveProxy dcp = new ConnectedDriveProxy(hcf, config);
        Token t = dcp.getToken();
        logger.info("Token {}", t.getBearerToken());
        logger.info("Expires {}", t.isExpired());
    }

    public void testJavaHttpAuth() {
        ConnectedDriveConfiguration config = new ConnectedDriveConfiguration();
        config.region = BimmerConstants.REGION_ROW;
        config.userName = "bla";
        config.password = "bla";

        final StringBuilder legacyAuth = new StringBuilder();
        legacyAuth.append("https://");
        legacyAuth.append(BimmerConstants.AUTH_SERVER_MAP.get(config.region));
        legacyAuth.append(BimmerConstants.OAUTH_ENDPOINT);
        URL url;
        try {

            final MultiMap<String> dataMap = new MultiMap<String>();
            dataMap.add("grant_type", "password");
            dataMap.add(SCOPE, BimmerConstants.SCOPE_VALUES);
            dataMap.add(USERNAME, config.userName);
            dataMap.add(PASSWORD, config.password);

            String urlContent = UrlEncoded.encode(dataMap, StandardCharsets.UTF_8, false);
            System.out.println(urlContent);
            url = new URL(legacyAuth.toString() + "?" + urlContent);
            System.out.println(url.toString());
            System.out.println(Integer.toString(urlContent.length()));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty(HttpHeader.CONTENT_LENGTH.asString(), Integer.toString(124));
            con.setRequestProperty(HttpHeader.CONTENT_TYPE.asString(), "application/x-www-form-urlencoded");
            // System.out.println(con.getHeaderField(HttpHeader.CONTENT_LENGTH.asString()));
            // con.setRequestProperty(HttpHeader.CONNECTION.asString(), KEEP_ALIVE);
            con.setRequestProperty(HttpHeader.HOST.asString(), BimmerConstants.SERVER_MAP.get(config.region));
            con.setRequestProperty(HttpHeader.AUTHORIZATION.asString(),
                    BimmerConstants.AUTHORIZATION_VALUE_MAP.get(config.region));
            con.setRequestProperty(CREDENTIALS, BimmerConstants.CREDENTIAL_VALUES);
            con.setRequestProperty(HttpHeader.REFERER.asString(), BimmerConstants.REFERER_URL);
            System.out.println(con.getHeaderFields());
            int status = con.getResponseCode();
            System.out.println("Status: " + status);
            if (status < 400) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                System.out.println("Content: " + content.toString());
                in.close();
            }
            System.out.println(con.getContentLength());
            con.disconnect();
        } catch (MalformedURLException e) {
        } catch (ProtocolException e) {
        } catch (IOException e) {
        }
    }

    public void testCumstomerBMWAuthenticate() {
        ConnectedDriveConfiguration c = new ConnectedDriveConfiguration();
        c.region = "ROW";
        c.userName = "bla";
        c.password = "bla";

        HttpClient hc = mock(HttpClient.class);
        HttpClientFactory hct = mock(HttpClientFactory.class);
        when(hct.getCommonHttpClient()).thenReturn(hc);
        when(hct.createHttpClient(AUTH_HTTP_CLIENT_NAME)).thenReturn(hc);
        AuthProbes auth = new AuthProbes(hct, c);
        auth.updateToken();
    }
}
