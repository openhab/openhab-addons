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
package org.openhab.binding.rachio.internal.api;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.SERVLET_IMAGE_PATH;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.SERVLET_IMAGE_URL_BASE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.SERVLET_WEBHOOK_PATH;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.RachioHandlerFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

/**
 * Tests idempotent manual servlet registration.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class RachioServletLifecycleTest {
    @Test
    void imageServletUsesCommonHttpClient() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        when(httpClientFactory.getCommonHttpClient()).thenReturn(httpClient);

        RachioImageServlet servlet = new RachioImageServlet(httpClientFactory);

        verify(httpClientFactory).getCommonHttpClient();
        Field field = RachioImageServlet.class.getDeclaredField("httpClient");
        field.setAccessible(true);
        assertSame(httpClient, field.get(servlet));
    }

    @Test
    void imageServletStreamsSuccessfulUpstreamResponse() throws Exception {
        byte[] image = "image-data".getBytes(StandardCharsets.UTF_8);
        HttpClient httpClient = mock(HttpClient.class);
        Request upstreamRequest = upstreamRequest(httpClient, "front-yard.png");
        Response upstreamResponse = mock(Response.class);
        when(upstreamResponse.getStatus()).thenReturn(HttpStatus.OK_200);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(servletOutputStream(output));

        try (MockedConstruction<InputStreamResponseListener> listenerConstruction = Mockito
                .mockConstruction(InputStreamResponseListener.class, (listener, context) -> {
                    when(listener.get(Mockito.anyLong(), Mockito.eq(TimeUnit.MILLISECONDS)))
                            .thenReturn(upstreamResponse);
                    when(listener.getInputStream()).thenReturn(new ByteArrayInputStream(image));
                })) {
            RachioImageServlet servlet = new RachioImageServlet(httpClient);

            servlet.service(imageRequest(SERVLET_IMAGE_PATH + "/front-yard.png", "GET"), response);

            InputStreamResponseListener listener = listenerConstruction.constructed().getFirst();
            verify(upstreamRequest).send(listener);
        }
        assertArrayEquals(image, output.toByteArray());
        verify(response, never()).sendError(HttpServletResponse.SC_BAD_GATEWAY);
    }

    @Test
    void imageServletMapsUpstreamErrorToBadGateway() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        Request upstreamRequest = upstreamRequest(httpClient, "front-yard.png");
        Response upstreamResponse = mock(Response.class);
        when(upstreamResponse.getStatus()).thenReturn(HttpStatus.NOT_FOUND_404);
        HttpServletResponse response = mock(HttpServletResponse.class);

        try (MockedConstruction<InputStreamResponseListener> ignored = Mockito.mockConstruction(
                InputStreamResponseListener.class,
                (listener, context) -> when(listener.get(Mockito.anyLong(), Mockito.eq(TimeUnit.MILLISECONDS)))
                        .thenReturn(upstreamResponse))) {
            RachioImageServlet servlet = new RachioImageServlet(httpClient);

            servlet.service(imageRequest(SERVLET_IMAGE_PATH + "/front-yard.png", "GET"), response);
        }

        verify(upstreamRequest).abort(any(IOException.class));
        verify(response).sendError(HttpServletResponse.SC_BAD_GATEWAY);
        verify(response, never()).getOutputStream();
    }

    @Test
    void imageServletRejectsTooShortPathBeforeSubstring() throws Exception {
        RachioImageServlet servlet = imageServlet();
        HttpServletResponse response = mock(HttpServletResponse.class);

        servlet.service(imageRequest("/rachio", "GET"), response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
        verify(response, never()).getOutputStream();
    }

    @Test
    void imageServletRejectsEmptyImagePath() throws Exception {
        RachioImageServlet servlet = imageServlet();
        HttpServletResponse response = mock(HttpServletResponse.class);

        servlet.service(imageRequest(SERVLET_IMAGE_PATH + "/", "GET"), response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST);
        verify(response, never()).getOutputStream();
    }

    @Test
    void imageServletRejectsUnsupportedMethodWithAllowHeader() throws Exception {
        RachioImageServlet servlet = imageServlet();
        HttpServletResponse response = mock(HttpServletResponse.class);

        servlet.service(imageRequest(SERVLET_IMAGE_PATH + "/front-yard.png", "POST"), response);

        verify(response).setHeader("Allow", "GET");
        verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verify(response, never()).getOutputStream();
    }

    @Test
    void imageServletDuplicateBindRegistersOnce() throws Exception {
        HttpService httpService = httpService();
        RachioImageServlet servlet = imageServlet();

        servlet.bindHttpService(httpService);
        servlet.bindHttpService(httpService);

        verify(httpService, times(1)).registerServlet(eq(SERVLET_IMAGE_PATH), same(servlet), isNull(),
                any(HttpContext.class));
    }

    @Test
    void imageServletDuplicateUnbindIsSafe() throws Exception {
        HttpService httpService = httpService();
        RachioImageServlet servlet = imageServlet();

        servlet.bindHttpService(httpService);
        servlet.unbindHttpService(httpService);
        servlet.unbindHttpService(httpService);

        verify(httpService, times(1)).unregister(SERVLET_IMAGE_PATH);
    }

    @Test
    void imageServletAlreadyRemovedAliasDuringUnbindIsSafe() throws Exception {
        HttpService httpService = httpService();
        RachioImageServlet servlet = imageServlet();
        doThrow(new IllegalArgumentException("already gone")).when(httpService).unregister(SERVLET_IMAGE_PATH);

        servlet.bindHttpService(httpService);
        servlet.unbindHttpService(httpService);
        servlet.bindHttpService(httpService);

        verify(httpService, times(2)).registerServlet(eq(SERVLET_IMAGE_PATH), same(servlet), isNull(),
                any(HttpContext.class));
        verify(httpService, times(1)).unregister(SERVLET_IMAGE_PATH);
    }

    @Test
    void imageServletRebindAfterUnbindRegistersAgain() throws Exception {
        HttpService httpService = httpService();
        RachioImageServlet servlet = imageServlet();

        servlet.bindHttpService(httpService);
        servlet.unbindHttpService(httpService);
        servlet.bindHttpService(httpService);

        verify(httpService, times(2)).registerServlet(eq(SERVLET_IMAGE_PATH), same(servlet), isNull(),
                any(HttpContext.class));
        verify(httpService, times(1)).unregister(SERVLET_IMAGE_PATH);
    }

    @Test
    void imageServletBindToNewServiceUnregistersPreviousService() throws Exception {
        HttpService firstHttpService = httpService();
        HttpService secondHttpService = httpService();
        RachioImageServlet servlet = imageServlet();

        servlet.bindHttpService(firstHttpService);
        servlet.bindHttpService(secondHttpService);

        verify(firstHttpService, times(1)).unregister(SERVLET_IMAGE_PATH);
        verify(secondHttpService, times(1)).registerServlet(eq(SERVLET_IMAGE_PATH), same(servlet), isNull(),
                any(HttpContext.class));
    }

    @Test
    void webhookServletDuplicateBindRegistersOnce() throws Exception {
        HttpService httpService = httpService();
        RachioWebhookServlet servlet = webhookServlet();

        servlet.bindHttpService(httpService);
        servlet.bindHttpService(httpService);

        verify(httpService, times(1)).registerServlet(eq(SERVLET_WEBHOOK_PATH), same(servlet), isNull(),
                any(HttpContext.class));
    }

    @Test
    void webhookServletDuplicateUnbindIsSafe() throws Exception {
        HttpService httpService = httpService();
        RachioWebhookServlet servlet = webhookServlet();

        servlet.bindHttpService(httpService);
        servlet.unbindHttpService(httpService);
        servlet.unbindHttpService(httpService);

        verify(httpService, times(1)).unregister(SERVLET_WEBHOOK_PATH);
    }

    @Test
    void webhookServletAlreadyRemovedAliasDuringUnbindIsSafe() throws Exception {
        HttpService httpService = httpService();
        RachioWebhookServlet servlet = webhookServlet();
        doThrow(new IllegalArgumentException("already gone")).when(httpService).unregister(SERVLET_WEBHOOK_PATH);

        servlet.bindHttpService(httpService);
        servlet.unbindHttpService(httpService);
        servlet.bindHttpService(httpService);

        verify(httpService, times(2)).registerServlet(eq(SERVLET_WEBHOOK_PATH), same(servlet), isNull(),
                any(HttpContext.class));
        verify(httpService, times(1)).unregister(SERVLET_WEBHOOK_PATH);
    }

    @Test
    void webhookServletRebindAfterUnbindRegistersAgain() throws Exception {
        HttpService httpService = httpService();
        RachioWebhookServlet servlet = webhookServlet();

        servlet.bindHttpService(httpService);
        servlet.unbindHttpService(httpService);
        servlet.bindHttpService(httpService);

        verify(httpService, times(2)).registerServlet(eq(SERVLET_WEBHOOK_PATH), same(servlet), isNull(),
                any(HttpContext.class));
        verify(httpService, times(1)).unregister(SERVLET_WEBHOOK_PATH);
    }

    @Test
    void webhookServletBindToNewServiceUnregistersPreviousService() throws Exception {
        HttpService firstHttpService = httpService();
        HttpService secondHttpService = httpService();
        RachioWebhookServlet servlet = webhookServlet();

        servlet.bindHttpService(firstHttpService);
        servlet.bindHttpService(secondHttpService);

        verify(firstHttpService, times(1)).unregister(SERVLET_WEBHOOK_PATH);
        verify(secondHttpService, times(1)).registerServlet(eq(SERVLET_WEBHOOK_PATH), same(servlet), isNull(),
                any(HttpContext.class));
    }

    private HttpService httpService() {
        HttpService httpService = mock(HttpService.class);
        when(httpService.createDefaultHttpContext()).thenReturn(mock(HttpContext.class));
        return httpService;
    }

    private Request upstreamRequest(HttpClient httpClient, String imageId) {
        Request request = mock(Request.class);
        when(httpClient.newRequest(SERVLET_IMAGE_URL_BASE + imageId)).thenReturn(request);
        when(request.method("GET")).thenReturn(request);
        when(request.timeout(Mockito.anyLong(), Mockito.eq(TimeUnit.MILLISECONDS))).thenReturn(request);
        when(request.header(Mockito.any(HttpHeader.class), Mockito.anyString())).thenReturn(request);
        return request;
    }

    private ServletOutputStream servletOutputStream(ByteArrayOutputStream output) {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(@Nullable WriteListener writeListener) {
            }

            @Override
            public void write(int value) {
                output.write(value);
            }
        };
    }

    private RachioImageServlet imageServlet() {
        return new RachioImageServlet(mock(HttpClient.class));
    }

    private HttpServletRequest imageRequest(String requestUri, String method) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(requestUri);
        when(request.getMethod()).thenReturn(method);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRemotePort()).thenReturn(443);
        when(request.getRemoteHost()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8443);
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        return request;
    }

    private RachioWebhookServlet webhookServlet() {
        return new RachioWebhookServlet(mock(RachioHandlerFactory.class));
    }
}
