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
import static org.openhab.binding.rachio.internal.RachioBindingConstants.SERVLET_WEBHOOK_PATH;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rachio.internal.RachioHandlerFactory;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

/**
 * Tests idempotent manual servlet registration.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class RachioServletLifecycleTest {
    @Test
    void imageServletRejectsTooShortPathBeforeSubstring() throws Exception {
        RachioImageServlet servlet = new RachioImageServlet();
        HttpServletResponse response = mock(HttpServletResponse.class);

        servlet.service(imageRequest("/rachio", "GET"), response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
        verify(response, never()).getOutputStream();
    }

    @Test
    void imageServletRejectsEmptyImagePath() throws Exception {
        RachioImageServlet servlet = new RachioImageServlet();
        HttpServletResponse response = mock(HttpServletResponse.class);

        servlet.service(imageRequest(SERVLET_IMAGE_PATH + "/", "GET"), response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST);
        verify(response, never()).getOutputStream();
    }

    @Test
    void imageServletRejectsUnsupportedMethodWithAllowHeader() throws Exception {
        RachioImageServlet servlet = new RachioImageServlet();
        HttpServletResponse response = mock(HttpServletResponse.class);

        servlet.service(imageRequest(SERVLET_IMAGE_PATH + "/front-yard.png", "POST"), response);

        verify(response).setHeader("Allow", "GET");
        verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verify(response, never()).getOutputStream();
    }

    @Test
    void imageServletDuplicateBindRegistersOnce() throws Exception {
        HttpService httpService = httpService();
        RachioImageServlet servlet = new RachioImageServlet();

        servlet.bindHttpService(httpService);
        servlet.bindHttpService(httpService);

        verify(httpService, times(1)).registerServlet(eq(SERVLET_IMAGE_PATH), same(servlet), isNull(),
                any(HttpContext.class));
    }

    @Test
    void imageServletDuplicateUnbindIsSafe() throws Exception {
        HttpService httpService = httpService();
        RachioImageServlet servlet = new RachioImageServlet();

        servlet.bindHttpService(httpService);
        servlet.unbindHttpService(httpService);
        servlet.unbindHttpService(httpService);

        verify(httpService, times(1)).unregister(SERVLET_IMAGE_PATH);
    }

    @Test
    void imageServletAlreadyRemovedAliasDuringUnbindIsSafe() throws Exception {
        HttpService httpService = httpService();
        RachioImageServlet servlet = new RachioImageServlet();
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
        RachioImageServlet servlet = new RachioImageServlet();

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
        RachioImageServlet servlet = new RachioImageServlet();

        servlet.bindHttpService(firstHttpService);
        servlet.bindHttpService(secondHttpService);

        verify(firstHttpService, times(1)).unregister(SERVLET_IMAGE_PATH);
        verify(secondHttpService, times(1)).registerServlet(eq(SERVLET_IMAGE_PATH), same(servlet), isNull(),
                any(HttpContext.class));
    }

    @Test
    void webhookServletDuplicateBindRegistersOnce() throws Exception {
        HttpService httpService = httpService();
        RachioWebHookServlet servlet = webhookServlet();

        servlet.bindHttpService(httpService);
        servlet.bindHttpService(httpService);

        verify(httpService, times(1)).registerServlet(eq(SERVLET_WEBHOOK_PATH), same(servlet), isNull(),
                any(HttpContext.class));
    }

    @Test
    void webhookServletDuplicateUnbindIsSafe() throws Exception {
        HttpService httpService = httpService();
        RachioWebHookServlet servlet = webhookServlet();

        servlet.bindHttpService(httpService);
        servlet.unbindHttpService(httpService);
        servlet.unbindHttpService(httpService);

        verify(httpService, times(1)).unregister(SERVLET_WEBHOOK_PATH);
    }

    @Test
    void webhookServletAlreadyRemovedAliasDuringUnbindIsSafe() throws Exception {
        HttpService httpService = httpService();
        RachioWebHookServlet servlet = webhookServlet();
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
        RachioWebHookServlet servlet = webhookServlet();

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
        RachioWebHookServlet servlet = webhookServlet();

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

    private RachioWebHookServlet webhookServlet() {
        return new RachioWebHookServlet(mock(RachioHandlerFactory.class));
    }
}
