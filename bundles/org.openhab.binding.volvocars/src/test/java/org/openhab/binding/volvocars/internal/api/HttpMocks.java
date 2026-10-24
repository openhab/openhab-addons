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
package org.openhab.binding.volvocars.internal.api;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;

/**
 * Stubs the Jetty {@link HttpClient} with canned responses per URL.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class HttpMocks {
    private record Response(int status, String content) {
    }

    final HttpClient httpClient = mock(HttpClient.class);
    final List<String> requestedUrls = new ArrayList<>();
    private final Map<String, Response> responses = new HashMap<>();

    HttpMocks() {
        when(httpClient.newRequest(anyString())).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            requestedUrls.add(url);
            Request request = mock(Request.class, RETURNS_SELF);
            Response response = responses.getOrDefault(url, new Response(404, "{}"));
            ContentResponse contentResponse = mock(ContentResponse.class);
            when(contentResponse.getStatus()).thenReturn(response.status());
            when(contentResponse.getContentAsString()).thenReturn(response.content());
            when(request.send()).thenReturn(contentResponse);
            return request;
        });
    }

    void respond(String url, int status, String content) {
        responses.put(url, new Response(status, content));
    }
}
