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
package org.openhab.binding.homeconnectdirect.internal.servlet.routing;

import static org.mockito.Mockito.*;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homeconnectdirect.internal.common.utils.ConfigurationUtils;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.service.cm.ConfigurationAdmin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Tests for {@link RequestHandlerContext}.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
class RequestHandlerContextTest {

    @Test
    void testSendHtmlSetsSecurityHeaders() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class);
        ThingRegistry thingRegistry = mock(ThingRegistry.class);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        RequestHandlerContext context = new RequestHandlerContext(request, response, Map.of(), configAdmin,
                ConfigurationUtils.createGson(), thingRegistry);

        context.sendHtml("<html><body>Test</body></html>");

        verify(response).setContentType(CONTENT_TYPE_HTML_UTF8);
        verify(response).setHeader(HEADER_X_FRAME_OPTIONS, SAME_ORIGIN);
        verify(response).setHeader(HEADER_CONTENT_SECURITY_POLICY, CONTENT_SECURITY_POLICY);
    }
}
