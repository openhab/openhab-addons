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
package org.openhab.binding.amazonechocontrol.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Test;

/**
 * The {@link AmazonEchoControlServletTest} contains tests for the login dialog handling of the
 * {@link AmazonEchoControlServlet}
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class AmazonEchoControlServletTest {
    private static final ServletUri ACCOUNT = new ServletUri("account1", "");
    private static final String RETAIL_URL = "https://www.amazon.de";
    private static final String SIGN_IN_LINK_BASE = "/amazonechocontrol/account1/FORWARD/www.amazon.com/";

    @Test
    public void testTheDialogHostsAreTheSignInHostAndTheRetailHost() {
        assertThat(AmazonEchoControlServlet.dialogHosts(RETAIL_URL), contains("www.amazon.com", "www.amazon.de"));
        assertThat(AmazonEchoControlServlet.dialogHosts("https://WWW.Amazon.DE"),
                contains("www.amazon.com", "www.amazon.de"));
        assertThat(AmazonEchoControlServlet.dialogHosts("https://www.amazon.com"), contains("www.amazon.com"));
        assertThat(AmazonEchoControlServlet.dialogHosts("amazon.de"), contains("www.amazon.com"));
        assertThat(AmazonEchoControlServlet.dialogHosts("http://exa mple"), contains("www.amazon.com"));
    }

    @Test
    public void testAProxyRedirectOnTheRetailHostStaysOnTheProxyWhileLoggedIn() {
        String target = AmazonEchoControlServlet.proxyRedirectTarget(RETAIL_URL + "/spa/index.html?x=1", RETAIL_URL,
                true, ACCOUNT);
        assertThat(target, is("/amazonechocontrol/account1/PROXY/spa/index.html?x=1"));
    }

    @Test
    public void testOtherProxyRedirectsAreRefused() {
        assertThat(AmazonEchoControlServlet.proxyRedirectTarget(RETAIL_URL + "/spa/index.html", RETAIL_URL, false,
                ACCOUNT), is(nullValue()));
        assertThat(AmazonEchoControlServlet.proxyRedirectTarget("https://www.amazon.com/ap/signin", RETAIL_URL, true,
                ACCOUNT), is(nullValue()));
        assertThat(AmazonEchoControlServlet.proxyRedirectTarget("/api/devices-v2/device", RETAIL_URL, true, ACCOUNT),
                is(nullValue()));
    }

    @Test
    public void testTheBrowsersIdentityIsPassedOnAndThePageOriginIsTheReferer() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Windows NT 10.0) Chrome/140.0");
        when(req.getHeader("Accept-Language")).thenReturn("de-DE,de;q=0.9");
        LoginDialogPage page = new LoginDialogPage("www.amazon.com", "/ap/signin");

        Map<String, String> get = AmazonEchoControlServlet.browserHeaders(req, page, HttpMethod.GET);
        assertThat(get, is(Map.of("User-Agent", "Mozilla/5.0 (Windows NT 10.0) Chrome/140.0", "Accept-Language",
                "de-DE,de;q=0.9", "Referer", "https://www.amazon.com")));

        Map<String, String> post = AmazonEchoControlServlet.browserHeaders(req, page, HttpMethod.POST);
        assertThat(post.get("Origin"), is("https://www.amazon.com"));
        assertThat(post.get("Referer"), is("https://www.amazon.com"));

        assertThat(AmazonEchoControlServlet.browserHeaders(req).keySet(), contains("User-Agent", "Accept-Language"));
    }

    @Test
    public void testMissingBrowserHeadersAreLeftToTheDefaults() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("Accept-Language")).thenReturn(" ");

        assertThat(AmazonEchoControlServlet.browserHeaders(req), is(Map.of()));
    }

    @Test
    public void testDataAttributesAreNotRewrittenAndFormactionIs() {
        String html = "<a data-href=\"/x\" href=\"/y\">1</a><button formaction=\"/z\">2</button>";
        assertThat(AmazonEchoControlServlet.rewriteLinks(html, "www.amazon.com", SIGN_IN_LINK_BASE),
                is("<a data-href=\"/x\" href=\"" + SIGN_IN_LINK_BASE + "y\">1</a><button formaction=\""
                        + SIGN_IN_LINK_BASE + "z\">2</button>"));
    }

    @Test
    public void testOnlyLinkAndFormTargetsOfThePageHostAreRewritten() {
        String html = "<form action=\"/ap/signin\"><a href=\"https://www.amazon.com/ap/forgotpassword\">x</a>"
                + "<a HREF=\"https:&#x2F;&#x2F;www.amazon.com&#x2F;gp&#x2F;help\">y</a>"
                + "<a href=\"https://www.amazon.com:443/gp/css\">z</a>" + "<a href=\"/gp/help\">root relative</a>"
                + "<a href=\"//evil.example/x\">protocol relative</a>"
                + "<a href=\"https://www.amazon.de/ap/signin\">other host</a>"
                + "<input type=\"hidden\" name=\"openid.return_to\" value=\"https://www.amazon.com/ap/maplanding\"/>"
                + "<script>var u = \"https://www.amazon.com/ap/x\";</script>";
        String rewritten = AmazonEchoControlServlet.rewriteLinks(html, "www.amazon.com", SIGN_IN_LINK_BASE);
        assertThat(rewritten, is("<form action=\"" + SIGN_IN_LINK_BASE + "ap/signin\"><a href=\"" + SIGN_IN_LINK_BASE
                + "ap/forgotpassword\">x</a><a HREF=\"" + SIGN_IN_LINK_BASE + "gp&#x2F;help\">y</a>" + "<a href=\""
                + SIGN_IN_LINK_BASE + "gp/css\">z</a>" + "<a href=\"" + SIGN_IN_LINK_BASE
                + "gp/help\">root relative</a>" + "<a href=\"//evil.example/x\">protocol relative</a>"
                + "<a href=\"https://www.amazon.de/ap/signin\">other host</a>"
                + "<input type=\"hidden\" name=\"openid.return_to\" value=\"https://www.amazon.com/ap/maplanding\"/>"
                + "<script>var u = \"https://www.amazon.com/ap/x\";</script>"));
    }
}
