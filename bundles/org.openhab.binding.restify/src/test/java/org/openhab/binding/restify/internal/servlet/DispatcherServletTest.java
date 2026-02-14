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
package org.openhab.binding.restify.internal.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class DispatcherServletTest {
    @Mock
    private TranslationProvider i18nProvider;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private Engine engine;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    void doGetEscapesTranslatedUserMessageInJsonErrorBody() throws Exception {
        // Given
        var writer = new StringWriter();
        var sut = new DispatcherServlet(new JsonEncoder(), i18nProvider, authorizationService, engine);
        when(request.getRequestURI()).thenReturn("/restify/missing");
        when(request.getLocale()).thenReturn(Locale.US);
        when(response.getWriter()).thenReturn(new PrintWriter(writer));
        when(i18nProvider.getText(nullable(Bundle.class), eq("servlet.error.not-found"), eq("servlet.error.not-found"),
                eq(Locale.US), any(), any())).thenReturn("Line \"1\"\nLine 2");

        // When
        sut.doGet(request, response);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        assertThat(writer.toString()).isEqualTo("{\"code\":404,\"error\":\"Line \\\"1\\\"\\nLine 2\"}");
    }

    @Test
    void doGetUsesLocalizedExceptionMessage() throws Exception {
        // Given
        var writer = new StringWriter();
        var sut = new DispatcherServlet(new JsonEncoder(), i18nProvider, authorizationService, engine);
        when(request.getRequestURI()).thenReturn("/invalid");
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        // When
        sut.doGet(request, response);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertThat(writer.toString()).isEqualTo("{\"code\":400,\"error\":\"Request URI must start with /restify\"}");
    }
}
