/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mcd.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * @author Simon Dengler - Initial contribution
 */
public class McdBridgeHandlerTest {
    SslContextFactory.Client ssl = new SslContextFactory.Client();
    final HttpClient httpClient = new HttpClient(ssl);
    String output = "";
    final Gson gson = new Gson();
    int responseCode = -13;
    boolean testDone = false;
    private final Logger logger = LoggerFactory.getLogger(McdBridgeHandler.class);

    @Test
    public synchronized void testHttpClient() {
        try {
            httpClient.start();
            Request request = httpClient.newRequest("https://cunds-syncapi.azurewebsites.net/api/Account")
                    .method(HttpMethod.POST).header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .header(HttpHeader.HOST, "cunds-syncapi.azurewebsites.net")
                    .header(HttpHeader.ACCEPT, "application/json");
            request.send(new BufferingResponseListener() {
                @NonNullByDefault({})
                @Override
                public void onComplete(Result result) {
                    if (result.getResponse().getStatus() == 200) {
                        output = "Status 200, set online";
                    } else {
                        output = "Status != 200, set offline";
                    }
                    testDone = true;
                    myNotify();
                }
            });
            /*
             * JsonObject a = null;
             * a.toString();
             */
        } catch (Exception e) {
            output = "error";
            logger.error("Error: {}", e.toString());
            testDone = true;
            myNotify();
        }
        while (!testDone) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.error("{}", e);
            }
        }
        assertEquals("Status != 200, set offline", output);
        testDone = false;
    }

    public synchronized void myNotify() {
        notifyAll();
    }

    @Test
    public void testLogMeIn() {
        try {
            httpClient.start();
            Request request = httpClient.newRequest("https://cunds-syncapi.azurewebsites.net/token")
                    .method(HttpMethod.POST).header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .header(HttpHeader.HOST, "cunds-syncapi.azurewebsites.net")
                    .header(HttpHeader.ACCEPT, "application/json");
            String content = "grant_type=password&username=" + "username" + "&password=" + "password";
            request.content(new StringContentProvider(content), "application/x-www-form-urlencoded");
            request.send(new BufferingResponseListener() {
                @NonNullByDefault({})
                @Override
                public void onComplete(Result result) {
                    try {
                        responseCode = result.getResponse().getStatus();
                        String contentString = getContentAsString();
                        JsonObject content = gson.fromJson(contentString, JsonObject.class);
                        if (content != null && content.has("access_token")) {
                            output = "ONLINE";
                            System.out.println(output);
                            String accessToken = content.get("access_token").getAsString();
                            int expiresIn = content.get("expires_in").getAsInt();
                            System.out.println("Access Token: " + accessToken);
                            System.out.println("Expires in: " + expiresIn);
                        } else {
                            output = "OFFLINE";
                            System.out.println(output);
                        }
                        assertEquals("OFFLINE", output);
                    } catch (Exception e) {
                        output = "ERROR";
                        System.out.println(output);
                        System.out.println(e.getMessage());
                        assertEquals(1, 2);
                    }
                }
            });
            // l1 = System.currentTimeMillis();
            Thread.sleep(1000L);
            System.out.println(responseCode);
            /*
             * l2 = System.currentTimeMillis();
             * System.out.println((long) l2-l1);
             */
        } catch (Exception e) {
            output = "ERROR";
            System.out.println(output);
            System.out.println(e.getMessage());
            assertEquals(1, 2);
        }
    }
}
