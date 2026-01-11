/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.cloud;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Test;

/**
 * The {@link CloudTest} tests the methods in the Cloud
 * class with mock responses.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class CloudTest {

    @Test
    public void testLogin() throws Exception {
        // Mock HttpClient and ContentResponse
        HttpClient mockHttpClient = mock(HttpClient.class);
        Request mockRequest = mock(Request.class);
        ContentResponse mockResponse = mock(ContentResponse.class);
        HttpFields mockHeaders = mock(HttpFields.class);

        // Define behavior of HttpFields
        when(mockHeaders.toString()).thenReturn("Mocked Headers");

        // Mock fluent methods of Request
        when(mockHttpClient.newRequest(anyString())).thenReturn(mockRequest);
        when(mockRequest.method(HttpMethod.POST)).thenReturn(mockRequest);
        when(mockRequest.timeout(anyLong(), any(TimeUnit.class))).thenReturn(mockRequest);
        when(mockRequest.content(any(StringContentProvider.class))).thenReturn(mockRequest);
        when(mockRequest.getHeaders()).thenReturn(mockHeaders); // Attach mocked headers
        when(mockRequest.send()).thenReturn(mockResponse);

        // Define behavior of ContentResponse
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.getContentAsString()).thenReturn(
                "{\"msg\": \"ok\", \"result\": {\"accessToken\": \"mock-token\", \"sessionId\": \"mock-session-id\"}, \"errorCode\": \"0\"}");

        // Create a CloudProvider instance (replace arguments with appropriate values)
        CloudProvider provider = new CloudProvider("NetHome Plus", "3742e9e5842d4ad59c2db887e12449f9", "1017",
                "https://mapp.appsmb.com", "xhdiwjnchekd4d512chdjx5d8e4c394D2D7S", "", "", "");

        // Inject the mocked HttpClient into the Cloud class
        Cloud cloud = new Cloud("email", "password", provider, mockHttpClient);

        // Set loginId using reflection so that the getLoginId() check doesn't trigger
        Field loginIdField = Cloud.class.getDeclaredField("loginId");
        loginIdField.setAccessible(true);
        loginIdField.set(cloud, "mock-loginId");

        // Execute the login method
        boolean login = cloud.login();

        // Assert the result
        assertTrue(login);

        // Verify that accessToken is returned
        Field accessTokenField = Cloud.class.getDeclaredField("accessToken");
        accessTokenField.setAccessible(true);
        assertEquals("mock-token", accessTokenField.get(cloud));

        // Verify that accessToken is returned
        Field sessionIdField = Cloud.class.getDeclaredField("sessionId");
        sessionIdField.setAccessible(true);
        assertEquals("mock-session-id", sessionIdField.get(cloud));
    }

    @Test
    public void testLoginproxy() throws Exception {
        // Mock HttpClient and ContentResponse
        HttpClient mockHttpClient = mock(HttpClient.class);
        Request mockRequest = mock(Request.class);
        ContentResponse mockResponse = mock(ContentResponse.class);
        HttpFields mockHeaders = mock(HttpFields.class);

        // Define behavior of HttpFields
        when(mockHeaders.toString()).thenReturn("Mocked Headers");

        // Mock fluent methods of Request
        when(mockHttpClient.newRequest(anyString())).thenReturn(mockRequest);
        when(mockRequest.method(HttpMethod.POST)).thenReturn(mockRequest);
        when(mockRequest.timeout(anyLong(), any(TimeUnit.class))).thenReturn(mockRequest);
        when(mockRequest.content(any(StringContentProvider.class))).thenReturn(mockRequest);
        when(mockRequest.getHeaders()).thenReturn(mockHeaders); // Attach mocked headers
        when(mockRequest.send()).thenReturn(mockResponse);

        // Define behavior of ContentResponse
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.getContentAsString()).thenReturn(
                "{\"msg\":\"ok\",\"data\":{\"mdata\":{\"accessToken\":\"mock-token\"}},\"errorCode\":\"0\"}");

        // Create a CloudProvider instance (replace arguments with appropriate values)
        CloudProvider provider = new CloudProvider("MSmartHome", "ac21b9f9cbfe4ca5a88562ef25e2b768", "1010",
                "https://mp-prod.appsmb.com/mas/v5/app/proxy?alias=", "xhdiwjnchekd4d512chdjx5d8e4c394D2D7S",
                "meicloud", "PROD_VnoClJI9aikS8dyy", "v5");

        // Inject the mocked HttpClient into the Cloud class
        Cloud cloud = new Cloud("email", "password", provider, mockHttpClient);

        // Set loginId using reflection so that the getLoginId() check doesn't trigger
        Field loginIdField = Cloud.class.getDeclaredField("loginId");
        loginIdField.setAccessible(true);
        loginIdField.set(cloud, "mock-loginId");

        // Execute the login method
        boolean login = cloud.login();

        // Assert the result
        assertTrue(login);

        // Verify that accessToken is returned
        Field accessTokenField = Cloud.class.getDeclaredField("accessToken");
        accessTokenField.setAccessible(true);
        assertEquals("mock-token", accessTokenField.get(cloud));
    }

    @Test
    public void testLoginWithSessionId() throws Exception {
        // Create a CloudProvider instance
        CloudProvider provider = new CloudProvider("NetHome Plus", "3742e9e5842d4ad59c2db887e12449f9", "1017",
                "https://mapp.appsmb.com", "xhdiwjnchekd4d512chdjx5d8e4c394D2D7S", "", "", "");

        // Create the Cloud class
        HttpClient mockHttpClient = mock(HttpClient.class);
        Cloud cloud = new Cloud("email", "password", provider, mockHttpClient);

        // Set loginId using reflection so that the getLoginId() check doesn't trigger
        Field loginIdField = Cloud.class.getDeclaredField("loginId");
        loginIdField.setAccessible(true);
        loginIdField.set(cloud, "mock-loginId");

        // Set sessionId using reflection
        Field sessionIdField = Cloud.class.getDeclaredField("sessionId");
        sessionIdField.setAccessible(true);
        sessionIdField.set(cloud, "mock-session-id"); // Set sessionId to trigger early exit

        // Execute the login method
        boolean login = cloud.login();

        // Assert the result
        assertTrue(login); // Validate early exit with sessionId
    }

    @Test
    public void testGetLoginId() throws Exception {
        // Mock HttpClient and dependent objects
        HttpClient mockHttpClient = mock(HttpClient.class);
        Request mockRequest = mock(Request.class);
        ContentResponse mockResponse = mock(ContentResponse.class);
        HttpFields mockHeaders = mock(HttpFields.class);

        // Define behavior for HttpFields
        when(mockHeaders.toString()).thenReturn("Mocked Headers");

        // Properly configure the fluent chain for Request
        when(mockHttpClient.newRequest(any(String.class))).thenReturn(mockRequest);
        when(mockRequest.method(HttpMethod.POST)).thenReturn(mockRequest);
        when(mockRequest.timeout(any(Long.class), any(TimeUnit.class))).thenReturn(mockRequest);
        when(mockRequest.content(any(StringContentProvider.class))).thenReturn(mockRequest);
        when(mockRequest.getHeaders()).thenReturn(mockHeaders);
        when(mockRequest.send()).thenReturn(mockResponse);

        // Define behavior for ContentResponse
        when(mockResponse.getStatus()).thenReturn(200);
        // Include "code": 0 to simulate a successful response
        when(mockResponse.getContentAsString())
                .thenReturn("{\"msg\": \"ok\", \"result\": {\"loginId\": \"mock-loginId\"}, \"errorCode\": \"0\"}");

        // Create a CloudProvider instance
        CloudProvider provider = new CloudProvider("NetHome Plus", "3742e9e5842d4ad59c2db887e12449f9", "1017",
                "https://mapp.appsmb.com", "xhdiwjnchekd4d512chdjx5d8e4c394D2D7S", "", "", "");

        // Inject the mocked HttpClient into the Cloud class
        Cloud cloud = new Cloud("email", "password", provider, mockHttpClient);

        // Execute the getLoginId method
        boolean getLogin = cloud.getLoginId();

        // Assert the result
        assertTrue(getLogin); // Validate that getLoginId returned true

        // Verify that loginId was set correctly
        Field loginIdField = Cloud.class.getDeclaredField("loginId");
        loginIdField.setAccessible(true);
        assertEquals("mock-loginId", loginIdField.get(cloud)); // Ensure loginId is correctly set
    }
}
