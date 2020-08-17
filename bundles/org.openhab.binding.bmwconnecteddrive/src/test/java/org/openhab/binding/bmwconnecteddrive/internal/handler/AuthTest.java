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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.junit.Assert.assertEquals;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.junit.Test;
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
    private final Logger logger = LoggerFactory.getLogger(ConnectedCarHandler.class);

    @Test
    public void test() {
        HttpClient httpClient = new HttpClient();
        try {
            httpClient.start();
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            // e1.printStackTrace();
        }
        String uri = "https://customer.bmwgroup.com/gcdm/oauth/authenticate";
        Request req = httpClient.POST(uri);
        AuthenticationStore authStore = httpClient.getAuthenticationStore();
        // Authentication a = new Authentication();

        // "Content-Type": "application/x-www-form-urlencoded",
        // "Content-Length": "124",
        // "Connection": "Keep-Alive",
        // "Host": self.serverUrl,
        // "Accept-Encoding": "gzip",
        // "Authorization": "Basic blF2NkNxdHhKdVhXUDc0eGYzQ0p3VUVQOjF6REh4NnVuNGNEanli"
        // "TEVOTjNreWZ1bVgya0VZaWdXUGNRcGR2RFJwSUJrN3JPSg==",
        // "Credentials": "nQv6CqtxJuXWP74xf3CJwUEP:1zDHx6un4cDjybLENN3kyfumX2kEYigWPcQpdvDRpIBk7rOJ",
        // "User-Agent": "okhttp/2.60",
        req.header("Content-Type", "application/x-www-form-urlencoded");
        req.header("Connection", "Keep-Alive");
        req.header("Host", "b2vapi.bmwgroup.com");
        req.header("Authorization",
                "Basic blF2NkNxdHhKdVhXUDc0eGYzQ0p3VUVQOjF6REh4NnVuNGNEanliTEVOTjNreWZ1bVgya0VZaWdXUGNRcGR2RFJwSUJrN3JPSg==");
        req.header("Credentials", "nQv6CqtxJuXWP74xf3CJwUEP:1zDHx6un4cDjybLENN3kyfumX2kEYigWPcQpdvDRpIBk7rOJ");

        // 'client_id': 'dbf0a542-ebd1-4ff0-a9a7-55172fbfce35',
        // 'response_type': 'token',
        // 'redirect_uri': 'https://www.bmw-connecteddrive.com/app/static/external-dispatch.html',
        // 'scope': 'authenticate_user vehicle_data remote_services',
        // 'username': self.bmwUsername,
        // 'password': self.bmwPassword

        String data = "  {'client_id': 'dbf0a542-ebd1-4ff0-a9a7-55172fbfce35'," + "        'response_type': 'token',"
                + "        'redirect_uri': 'https://www.bmw-connecteddrive.com/app/static/external-dispatch.html',"
                + "        'scope': 'authenticate_user vehicle_data remote_services',"
                + "        'username': 'marika.weymann@gmail.com'," + "        'password': 'P4nd4b3r'}";
        MultiMap<String> map = new MultiMap<String>();
        map.add("client_id", "dbf0a542-ebd1-4ff0-a9a7-55172fbfce35");
        map.add("response_type", "token");
        map.add("redirect_uri", "https://www.bmw-connecteddrive.com/app/static/external-dispatch.html");
        map.add("scope", "authenticate_user vehicle_data remote_services");
        map.add("username", "marika.weymann@gmail.com");
        map.add("password", "P4nd4b3r");
        String urlEncodedData = UrlEncoded.encode(map, Charset.defaultCharset(), false);
        logger.info("URL encoded data {}", urlEncodedData);
        logger.info("Data size {} ", urlEncodedData.length());
        req.header("Content-Length", urlEncodedData.length() + "");
        req.content(new StringContentProvider(urlEncodedData));
        try {
            ContentResponse contentResponse = req.timeout(30, TimeUnit.SECONDS).send();
            logger.info("Status {} ", contentResponse.getStatus());
            logger.info("Reason {} ", contentResponse.getReason());
            logger.info("Encoding {} ", contentResponse.getEncoding());
            logger.info("Content length {} ", contentResponse.getContent().length);
            logger.info("Media Type {} ", contentResponse.getMediaType());
            HttpFields fields = contentResponse.getHeaders();
            for (int i = 0; i < fields.size(); i++) {
                HttpField field = fields.getField(i);
                logger.info("Field {}, Name {}, Value {}", i, field.getName(), field.getValue());
            }
            String content = contentResponse.getContentAsString();
            logger.info("Auth response: {}", content);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.info("Auth Exception: {}", e.getMessage());
            // e.printStackTrace();
        }
        try {
            httpClient.stop();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
        httpClient.destroy();
    }

    @Test
    public void testTokenDecoding() {
        Token t = new Token();
        ConnectedDrivePortalHandler cdpHandler = new ConnectedDrivePortalHandler(new HttpClient());
        String headerValue = "https://www.bmw-connecteddrive.com/app/static/external-dispatch.html#access_token=SfXKgkEXeeFJkVqdD4XMmfUU224MRuyh&token_type=Bearer&expires_in=7199";
        MultiMap<String> map = new MultiMap<String>();
        cdpHandler.storeToken(headerValue, t);
        assertEquals("Token", "Bearer SfXKgkEXeeFJkVqdD4XMmfUU224MRuyh", t.getToken());
    }
}
