/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.blink.internal.servlet;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.blink.internal.BlinkTestUtil;
import org.openhab.binding.blink.internal.dto.BlinkAccount;
import org.openhab.binding.blink.internal.handler.AccountHandler;
import org.openhab.binding.blink.internal.service.AccountService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.internal.BridgeImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * Test class.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class AccountVerificationServletTest {

    @Mock
    @NonNullByDefault({})
    HttpService httpService;

    @Mock
    @NonNullByDefault({})
    BundleContext bundleContext;

    @Mock
    @NonNullByDefault({})
    Bundle bundle;

    @Mock
    @NonNullByDefault({})
    AccountHandler accountHandler;

    @Mock
    @NonNullByDefault({})
    AccountService accountService;

    @NonNullByDefault({})
    AccountVerificationServlet servlet;

    @BeforeEach
    void setup() {
        Bridge account = new BridgeImpl(new ThingTypeUID("blink", "account"), "myuid");
        doReturn(account).when(accountHandler).getThing();
        servlet = spy(new AccountVerificationServlet(httpService, bundleContext, accountHandler, accountService));
    }

    @Test
    void testServletRegisteredAtConstructionWithCorrectUrl() throws ServletException, NamespaceException {
        String url = "/blink/" + URLEncoder.encode(accountHandler.getThing().getUID().getId(), StandardCharsets.UTF_8);
        verify(httpService).registerServlet(eq(url), any(), any(), any());
    }

    @Test
    void testServletUnregisteredAtDisposal() {
        servlet.dispose();
        String url = "/blink/" + URLEncoder.encode(accountHandler.getThing().getUID().getId(), StandardCharsets.UTF_8);
        verify(httpService).unregister(url);
    }

    @Test
    void testGenerateVerificationPageThrowsIllegalArgumentExceptionIfNotExist() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        URL html = this.getClass().getResource("idonotexist.html");
        doReturn(html).when(bundle).getEntry(anyString());
        doReturn(bundle).when(bundleContext).getBundle();
        assertThrows(IllegalArgumentException.class, () -> servlet.generateVerificationPage(outputStream, true));
    }

    @Test
    void testGenerateVerificationPageThrowsNullPointerExceptionOnNullStream() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        URL html = spy(this.getClass().getResource("validation.txt"));
        doReturn(null).when(html).openStream(); // openStream never returns null
        doReturn(html).when(bundle).getEntry(anyString());
        doReturn(bundle).when(bundleContext).getBundle();
        assertThrows(NullPointerException.class, () -> servlet.generateVerificationPage(outputStream, true));
    }

    @Test
    void testGenerateVerificationPageWithoutError() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        URL html = spy(this.getClass().getResource("validation.txt"));
        doReturn(html).when(bundle).getEntry(anyString());
        doReturn(bundle).when(bundleContext).getBundle();
        servlet.generateVerificationPage(outputStream, false);
        outputStream.close();
        String expected = "<body>\n<div>\n  \n</div>\n</body>";
        MatcherAssert.assertThat(outputStream.toString(StandardCharsets.UTF_8), is(expected));
    }

    @Test
    void testGenerateVerificationPageWithError() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        URL html = spy(this.getClass().getResource("validation.txt"));
        doReturn(html).when(bundle).getEntry(anyString());
        doReturn(bundle).when(bundleContext).getBundle();
        servlet.generateVerificationPage(outputStream, true);
        outputStream.close();
        String errorMessage = "  <div class=\"error\"><b>Invalid 2 factor verification PIN code.</b><br/>\n"
                + "    The code is only valid for a 40 minute period. Please try disabling and enabling the blink Account Thing\n"
                + "    to generate a new PIN code if you think that might be the problem.</div>";
        String expected = "<body>\n<div>\n" + errorMessage + "\n</div>\n</body>";
        MatcherAssert.assertThat(outputStream.toString(StandardCharsets.UTF_8), is(expected));
    }

    @Test
    void testPostWithoutRequestSendingError() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        servlet.doPost(null, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void testPostWithoutRequestParametersNotFailing() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        BlinkAccount account = BlinkTestUtil.testBlinkAccount();
        doReturn(account).when(accountHandler).getBlinkAccount();
        doNothing().when(servlet).generateVerificationPage(any(), anyBoolean());
        doReturn(false).when(accountService).verifyPin(account, "");
        servlet.doPost(request, response);
    }
}
