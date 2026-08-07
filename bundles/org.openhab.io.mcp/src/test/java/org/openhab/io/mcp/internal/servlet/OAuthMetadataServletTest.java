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
package org.openhab.io.mcp.internal.servlet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.io.mcp.internal.McpCloudWebhookService;

/**
 * Tests that {@link OAuthMetadataServlet} advertises URLs matching the route a request
 * arrived on.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class OAuthMetadataServletTest {

    private static final String HOOK_URL = "https://myopenhab.org/api/hooks/abc-123";

    private static HttpServletRequest request(String host, @Nullable String openhabSource) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn(host);
        lenient().when(request.getServerPort()).thenReturn(443);
        lenient().when(request.getScheme()).thenReturn("https");
        lenient().when(request.getHeader("Host")).thenReturn(host);
        lenient().when(request.getHeader("X-Forwarded-Proto")).thenReturn("https");
        lenient().when(request.getHeader("x-openhab-source")).thenReturn(openhabSource);
        return request;
    }

    private static McpCloudWebhookService hook() {
        McpCloudWebhookService hook = mock(McpCloudWebhookService.class);
        when(hook.getPublicUrl()).thenReturn(HOOK_URL);
        lenient().when(hook.deriveBrowserBaseUrl()).thenReturn("https://connect.myopenhab.org");
        return hook;
    }

    @Test
    void webhookRequestGetsHookUrls() {
        var ctx = OAuthMetadataServlet.resolveUrlContext(request("myopenhab.org", "org.openhab.io.openhabcloud"),
                hook());

        assertEquals(HOOK_URL, ctx.resource());
        assertEquals(HOOK_URL, ctx.issuer());
        assertEquals("https://connect.myopenhab.org/auth/authorize", ctx.authorizationEndpoint());
        assertEquals(HOOK_URL + "/oauth/token", ctx.tokenEndpoint());
        assertEquals(HOOK_URL + "/oauth/register", ctx.registrationEndpoint());
    }

    @Test
    void webhookRequestIsDetectedByHostAlone() {
        var ctx = OAuthMetadataServlet.resolveUrlContext(request("myopenhab.org", null), hook());

        assertEquals(HOOK_URL, ctx.resource());
    }

    /** Guards the regression: direct requests used to be handed the webhook URL. */
    @Test
    void directRequestGetsRequestDerivedUrlsEvenWhenHookRegistered() {
        var ctx = OAuthMetadataServlet.resolveUrlContext(request("oh.example.com", null), hook());

        assertEquals("https://oh.example.com/mcp", ctx.resource());
        assertEquals("https://oh.example.com/mcp", ctx.issuer());
        assertEquals("https://oh.example.com/auth/authorize", ctx.authorizationEndpoint());
        assertEquals("https://oh.example.com/mcp/oauth/token", ctx.tokenEndpoint());
        assertEquals("https://oh.example.com/mcp/oauth/register", ctx.registrationEndpoint());
    }

    @Test
    void directRequestWithNoHookGetsRequestDerivedUrls() {
        var ctx = OAuthMetadataServlet.resolveUrlContext(request("oh.example.com", null), null);

        assertEquals("https://oh.example.com/mcp", ctx.resource());
        assertEquals("https://oh.example.com/mcp/oauth/register", ctx.registrationEndpoint());
    }

    @Test
    void protectedResourceMetadataUrlIsAbsoluteForDirectRequests() {
        assertEquals("https://oh.example.com/mcp/.well-known/oauth-protected-resource",
                OAuthMetadataServlet.protectedResourceMetadataUrl(request("oh.example.com", null), null));
    }

    @Test
    void protectedResourceMetadataUrlUsesHookForCloudRequests() {
        assertEquals(HOOK_URL + "/.well-known/oauth-protected-resource", OAuthMetadataServlet
                .protectedResourceMetadataUrl(request("myopenhab.org", "org.openhab.io.openhabcloud"), hook()));
    }

    @Test
    void unregisteredHookFallsBackToRequestUrls() {
        McpCloudWebhookService hook = mock(McpCloudWebhookService.class);
        when(hook.getPublicUrl()).thenReturn(null);

        var ctx = OAuthMetadataServlet.resolveUrlContext(request("oh.example.com", null), hook);

        assertEquals("https://oh.example.com/mcp", ctx.resource());
    }

    @Test
    void forwardedHostNotMatchingServerNameIsIgnored() {
        HttpServletRequest request = request("oh.example.com", null);
        when(request.getHeader("X-Forwarded-Host")).thenReturn("attacker.example.net");

        var ctx = OAuthMetadataServlet.resolveUrlContext(request, null);

        assertEquals("https://oh.example.com/mcp", ctx.resource());
    }
}
