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

import static org.openhab.binding.bmwconnecteddrive.internal.handler.HTTPConstants.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedCarConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.dto.CarData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ConnectedDrivePortalHandler} Deals with the Authorization & Communication for one specific Car and the BMW
 * Connected Drive Portal
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConnectedDrivePortalHandler {
    private final Logger logger = LoggerFactory.getLogger(ConnectedDrivePortalHandler.class);
    private final HttpClient httpClient;
    private final Token token = new Token();
    private @Nullable ConnectedCarConfiguration configuration;
    private @Nullable String vehicleAPI;

    public ConnectedDrivePortalHandler(HttpClient hc) {
        httpClient = hc;
        httpClient.setFollowRedirects(false);
    }

    public void initialize(ConnectedCarConfiguration config) {
        configuration = config;
        vehicleAPI = "https://" + BimmerConstants.SERVER_MAP.get(configuration.region)
                + "/api/vehicle/dynamic/v1/" + configuration.vin + "?offset=-60";
        logger.info("Vehicle API set to {}", vehicleAPI);

        // r = requests.get(self.vehicleApi+'/dynamic/v1/'+self.bmwVin+'?offset=-60',
        // headers=headers,allow_redirects=True)
    }

    /**
     * Authorize at BMW Connected Drive Portal and re
     *
     * @return
     */
    void auth() {
        String uri = "https://customer.bmwgroup.com/gcdm/oauth/authenticate";
        Request req = httpClient.POST(uri);

        req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED);
        req.header(HttpHeader.CONNECTION, KEEP_ALIVE);
        req.header(HttpHeader.HOST, BimmerConstants.SERVER_MAP.get(configuration.region));
        req.header(HttpHeader.AUTHORIZATION, BimmerConstants.AUTHORIZATION_VALUE);
        req.header(CREDENTIALS, BimmerConstants.CREDENTIAL_VALUES);

        MultiMap<String> map = new MultiMap();
        map.add(CLIENT_ID, BimmerConstants.CLIENT_ID_VALUE);
        map.add(RESPONSE_TYPE, TOKEN);
        map.add(REDIRECT_URI, BimmerConstants.REDIRECT_URI_VALUE);
        map.add(SCOPE, BimmerConstants.SCOPE_VALUES);
        map.add(USERNAME, configuration.userName);
        map.add(PASSWORD, configuration.password);
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
            HttpField field = fields.getField(HttpHeader.LOCATION);
            storeToken(field.getValue(), token);
            //
            // for (int i = 0; i < fields.size(); i++) {
            // HttpField field = fields.getField(i);
            // logger.info("Field {}, Name {}, Value {}", i, field.getName(), field.getValue());
            // }
            // String content = contentResponse.getContentAsString();
            // logger.info("Auth response: {}", content);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.warn("Auth Exception: {}", e.getMessage());
        }
    }

    public void storeToken(String urlEncoded, Token t) {
        MultiMap<String> map = new MultiMap<String>();
        UrlEncoded.decodeTo(urlEncoded, map, StandardCharsets.US_ASCII);
        map.forEach((key, value) -> {
            logger.info("Key {} Value {}", key, value);
            if (key.endsWith(ACCESS_TOKEN)) {
                t.setToken(value.get(0).toString());
            } else if (key.equals(EXPIRES_IN)) {
                logger.info("Expires {}", value.get(0).toString());
                t.setExpiration(Integer.parseInt(value.get(0).toString()));
            } else if (key.equals(TOKEN_TYPE)) {
                t.setType(value.get(0).toString());
            }
        });
    }

    public @Nullable CarData getData() {
        if (token.isExpired()) {
            auth();
            if (token.isExpired()) {
                return null;
            }
        }
        Request req = httpClient.newRequest(vehicleAPI);

        req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_JSON);
        req.header(HttpHeader.AUTHORIZATION, token.getToken());
        try {
            ContentResponse contentResponse = req.timeout(30, TimeUnit.SECONDS).send();
            logger.info("Status {}", contentResponse.getStatus());
            logger.info("Reason {}", contentResponse.getReason());
            logger.info("Reason {}", contentResponse.getContentAsString());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Get Data Exception {}", e.getMessage());
        }
        return null;
    }
}
