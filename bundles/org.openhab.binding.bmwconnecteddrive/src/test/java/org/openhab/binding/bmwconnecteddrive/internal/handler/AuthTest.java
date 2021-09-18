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
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.VehicleConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.utils.BimmerConstants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
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
    private final Logger logger = LoggerFactory.getLogger(AuthTest.class);

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

    @Test
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
            dataMap.add(SCOPE, BimmerConstants.LEGACY_SCOPE_VALUES);
            dataMap.add(USERNAME, config.userName);
            dataMap.add(PASSWORD, config.password);

            String urlContent = UrlEncoded.encode(dataMap, StandardCharsets.UTF_8, false);
            url = new URL(legacyAuth.toString() + "?" + urlContent);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty(HttpHeader.CONTENT_LENGTH.asString(), Integer.toString(124));
            con.setRequestProperty(HttpHeader.CONTENT_TYPE.asString(), "application/x-www-form-urlencoded");
            // System.out.println(con.getHeaderField(HttpHeader.CONTENT_LENGTH.asString()));
            // con.setRequestProperty(HttpHeader.CONNECTION.asString(), KEEP_ALIVE);
            con.setRequestProperty(HttpHeader.HOST.asString(), BimmerConstants.API_SERVER_MAP.get(config.region));
            con.setRequestProperty(HttpHeader.AUTHORIZATION.asString(),
                    BimmerConstants.LEGACY_AUTHORIZATION_VALUE_MAP.get(config.region));
            con.setRequestProperty(CREDENTIALS, BimmerConstants.LEGACY_CREDENTIAL_VALUES);
            con.setRequestProperty(HttpHeader.REFERER.asString(), BimmerConstants.LEGACY_REFERER_URL);
            int status = con.getResponseCode();
            if (status < 400) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
            }
            con.disconnect();
        } catch (MalformedURLException e) {
        } catch (ProtocolException e) {
        } catch (IOException e) {
        }
    }

    @Test
    public void urlencodeTest() {
        String expectedResult = "client_id=31c357a0-7a1d-4590-aa99-33b97244d048&response_type=code&redirect_uri=com.bmw.connected%3A%2F%2Foauth&state=cEG9eLAIi6Nv-aaCAniziE_B6FPoobva3qr5gukilYw&nonce=login_nonce&scope=openid+profile+email+offline_access+smacc+vehicle_data+perseus+dlm+svds+cesim+vsapi+remote_services+fupo+authenticate_user&authorization=G0s3x-LE682iWMJ3WSLm0TmB2R4.%2AAAJTSQACMDIAAlNLABw2c0llQVVyaTB5OFdpUlptQjVtWXhmaWNVTzQ9AAR0eXBlAANDVFMAAlMxAAIwMQ..%2A";

        MultiMap<String> baseValues = new MultiMap<String>();
        baseValues.add(CLIENT_ID, Constants.EMPTY + BimmerConstants.CLIENT_ID.get("ROW"));
        baseValues.add(RESPONSE_TYPE, CODE);
        baseValues.add(REDIRECT_URI, BimmerConstants.REDIRECT_URI_VALUE);
        baseValues.add("state", Constants.EMPTY + BimmerConstants.STATE.get("ROW"));
        baseValues.add("nonce", "login_nonce");
        baseValues.add(SCOPE, BimmerConstants.SCOPE_VALUES);

        MultiMap codeChallenge = new MultiMap<String>();
        codeChallenge.addAllValues(baseValues);
        // codeChallenge.put("authorization", authCode);
        logger.info("Code MultiMap {}", codeChallenge);

        // String codeEncoded = UrlEncoded.encode(codeChallenge, Charset.defaultCharset(), false);
        String enc = URLEncoder.encode(codeChallenge.toString(), Charset.defaultCharset());
        logger.info("Code Url enc  {}", enc);

        System.out.println(Charset.availableCharsets());
        String authCodeGot = "G0s3x-LE682iWMJ3WSLm0TmB2R4.*AAJTSQACMDIAAlNLABw2c0llQVVyaTB5OFdpUlptQjVtWXhmaWNVTzQ9AAR0eXBlAANDVFMAAlMxAAIwMQ..*";
        logger.info("Encoded {}", UrlEncoded.encodeString(authCodeGot));

        codeChallenge.put("authorization", authCodeGot);
        logger.info("Encoded {}", UrlEncoded.encode(codeChallenge, Charset.forName("UTF-8"), false));
        String authCodeSent = "G0s3x-LE682iWMJ3WSLm0TmB2R4.%2AAAJTSQACMDIAAlNLABw2c0llQVVyaTB5OFdpUlptQjVtWXhmaWNVTzQ9AAR0eXBlAANDVFMAAlMxAAIwMQ..%2A";
    }

    public void testLegacyJetty() {
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        HttpClient client = new HttpClient(sslContextFactory);
        client.setIdleTimeout(20000);
        try {
            client.start();
        } catch (Exception e) {
            logger.info("Client Start Exception {}", e.getMessage());
        }
        ConnectedDriveConfiguration c = new ConnectedDriveConfiguration();
        c.region = "ROW";
        c.userName = "bla";
        c.password = "bla";

        HttpClient hc = mock(HttpClient.class);
        HttpClientFactory hct = mock(HttpClientFactory.class);
        when(hct.getCommonHttpClient()).thenReturn(hc);
        when(hct.createHttpClient(AUTH_HTTP_CLIENT_NAME)).thenReturn(hc);
        ConnectedDriveProxy auth = new ConnectedDriveProxy(hct, c);
        auth.updateToken(client);
    }

    @Test
    public void testRemote() {
        // String profile =
        // "{\"weeklyPlanner\":{\"climatizationEnabled\":false,\"chargingMode\":\"IMMEDIATE_CHARGING\",\"chargingPreferences\":\"CHARGING_WINDOW\",\"timer1\":{\"departureTime\":\"16:00\",\"timerEnabled\":false,\"weekdays\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"]},\"timer2\":{\"departureTime\":\"12:02\",\"timerEnabled\":false,\"weekdays\":[\"SATURDAY\"]},\"timer3\":{\"departureTime\":\"13:03\",\"timerEnabled\":false,\"weekdays\":[\"SATURDAY\"]},\"overrideTimer\":{\"departureTime\":\"00:00\",\"timerEnabled\":false,\"weekdays\":[]},\"preferredChargingWindow\":{\"startTime\":\"10:00\",\"endTime\":\"15:00\"}}}";
        VehicleConfiguration vc = new VehicleConfiguration();
        vc.vin = "WBY1Z81040V905639";
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        String profile = "{\"weeklyPlanner\":{\"climatizationEnabled\":false,\"chargingMode\":\"IMMEDIATE_CHARGING\",\"chargingPreferences\":\"CHARGING_WINDOW\",\"timer1\":{\"departureTime\":\"16:00\",\"timerEnabled\":false,\"weekdays\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"]},\"timer2\":{\"departureTime\":\"12:02\",\"timerEnabled\":false,\"weekdays\":[\"SATURDAY\"]},\"timer3\":{\"departureTime\":\"13:03\",\"timerEnabled\":false,\"weekdays\":[\"SATURDAY\"]},\"overrideTimer\":{\"departureTime\":\"00:00\",\"timerEnabled\":false,\"weekdays\":[]},\"preferredChargingWindow\":{\"startTime\":\"10:00\",\"endTime\":\"15:00\"}}}";
        // String profile =
        // "{\"type\":\"CHARGING_PROFILE\",\"weeklyPlanner\":{\"climatizationEnabled\":false,\"chargingMode\":\"IMMEDIATE_CHARGING\",\"chargingPreferences\":\"CHARGING_WINDOW\",\"timer1\":{\"departureTime\":\"16:00\",\"timerEnabled\":false,\"weekdays\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"]},\"timer2\":{\"departureTime\":\"12:02\",\"timerEnabled\":false,\"weekdays\":[\"SATURDAY\"]},\"timer3\":{\"departureTime\":\"13:03\",\"timerEnabled\":false,\"weekdays\":[\"SATURDAY\"]},\"overrideTimer\":{\"departureTime\":\"00:00\",\"timerEnabled\":false,\"weekdays\":[]},\"preferredChargingWindow\":{\"startTime\":\"10:00\",\"endTime\":\"15:00\"}}}";
        String dataProfile = "{\"type\":\"CHARGING_PROFILE\",\"data\":" + profile + "}";
        HttpClient client = new HttpClient(sslContextFactory);
        client.setIdleTimeout(20000);
        try {
            client.start();
        } catch (Exception e) {
            logger.info("Client Start Exception {}", e.getMessage());
        }
        ConnectedDriveConfiguration c = new ConnectedDriveConfiguration();
        c.region = "ROW";
        c.userName = "bla";
        c.password = "bla";

        HttpClientFactory hct = mock(HttpClientFactory.class);
        when(hct.getCommonHttpClient()).thenReturn(client);
        when(hct.createHttpClient(AUTH_HTTP_CLIENT_NAME)).thenReturn(client);
        VehicleHandler vh = mock(VehicleHandler.class);
        when(vh.getConfiguration()).thenReturn(Optional.of(vc));
        ConnectedDriveProxy auth = new ConnectedDriveProxy(hct, c);
        RemoteServiceHandler rsh = new RemoteServiceHandler(vh, auth);
        rsh.setMyBmwApiUsage(true);
        auth.updateToken(client);
        // String url = "https://cocoapi.bmwgroup.com/eadrax-dcs/v1/send-to-car/send-to-car";
        String url = "https://cocoapi.bmwgroup.com" + ConnectedDriveProxy.REMOTE_SERVICE_EADRAX_BASE_URL + vc.vin
                + "/vehicle-finder";
        // "/"
        // + "charging-settings";
        // String url = "https://cocoapi.bmwgroup.com/eadrax-vrccs/v2/presentation/remote-history/" + vc.vin;

        String type = "{\"type\":\"LIGHTS\"}";
        // {"type":"CHARGING_PROFILE"

        auth.call(url, true, CONTENT_TYPE_JSON_ENCODED, dataProfile, rsh);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        // MultiMap<String> dataMap = new MultiMap<>();
        // dataMap.put("data", profile);
        // auth.call(url, true, CONTENT_TYPE_URL_ENCODED, UrlEncoded.encode(dataMap, StandardCharsets.UTF_8, false),
        // rsh);
        // try {
        // Thread.sleep(2000);
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
    }
}
