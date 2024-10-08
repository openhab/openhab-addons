/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dirigera;

import static org.openhab.binding.dirigera.internal.Constants.HOME_URL;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

/**
 * {@link TestGateway} some basic tests
 *
 * @author Bernd Weymann - Initial Contribution
 */
class TestGateway {

    @Test
    void testHomeDump() {
        String homeDumpString = FileReader.readFileInString("src/test/resources/test.json");
        // System.out.println(homeDumpString);
        JSONObject homeObject = new JSONObject(homeDumpString);
        // System.out.println(homeObject);
        JSONArray devices = homeObject.getJSONArray("devices");
        Iterator<Object> entries = devices.iterator();
        while (entries.hasNext()) {
            JSONObject entry = (JSONObject) entries.next();
            System.out.println(entry.get("type") + " : " + entry.get("id"));
        }
    }

    @Test
    void testHome() {
        HttpClient insecureClient = new HttpClient(new SslContextFactory.Client(true));
        insecureClient.setUserAgentField(null);
        // from https://github.com/jetty-project/jetty-reactive-httpclient/issues/33#issuecomment-777771465
        insecureClient.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
        try {
            insecureClient.start();
            ContentResponse response = insecureClient.newRequest(String.format(HOME_URL, "192.168.1.26"))
                    .header(HttpHeader.AUTHORIZATION, "Bearer abc").timeout(5, TimeUnit.SECONDS).send();
        } catch (Exception e) {
            // catching exception is necessary due to the signature of HttpClient.start()
            // logger.warn("Failed to start http client: {}", e.getMessage());
            // throw new IllegalStateException("Could not create HttpClient", e);
        }
    }
}
