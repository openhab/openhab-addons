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
package org.openhab.binding.webthing.internal.client;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;

/**
 * Mock helper
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public class Mocks {

    public static Request mockRequest(@Nullable String requestContent, String responseContent) throws Exception {
        return mockRequest(requestContent, responseContent, 200, 200);
    }

    public static Request mockRequest(@Nullable String requestContent, String responseContent, int getResponse,
            int postResponse) throws Exception {
        var request = mock(Request.class);

        // GET request -> request.timeout(30, TimeUnit.SECONDS).send();
        var getRequest = mock(Request.class);
        var getContentResponse = mock(ContentResponse.class);
        when(getContentResponse.getStatus()).thenReturn(getResponse);
        when(getContentResponse.getContentAsString()).thenReturn(responseContent);
        when(getRequest.send()).thenReturn(getContentResponse);
        when(getRequest.accept("application/json")).thenReturn(getRequest);
        when(request.timeout(30, TimeUnit.SECONDS)).thenReturn(getRequest);

        // POST request -> request.method("PUT").content(new StringContentProvider(json)).timeout(30,
        // TimeUnit.SECONDS).send();
        if (requestContent != null) {
            var postRequest = mock(Request.class);
            when(postRequest.content(argThat((ContentProvider content) -> bufToString(content).equals(requestContent)),
                    eq("application/json"))).thenReturn(postRequest);
            when(postRequest.timeout(30, TimeUnit.SECONDS)).thenReturn(postRequest);

            var postContentResponse = mock(ContentResponse.class);
            when(postContentResponse.getStatus()).thenReturn(postResponse);
            when(postRequest.send()).thenReturn(postContentResponse);
            when(request.method("PUT")).thenReturn(postRequest);
        }
        return request;
    }

    private static String bufToString(Iterable<ByteBuffer> data) {
        var result = "";
        for (var byteBuffer : data) {
            result += StandardCharsets.UTF_8.decode(byteBuffer).toString();
        }
        return result;
    }
}
