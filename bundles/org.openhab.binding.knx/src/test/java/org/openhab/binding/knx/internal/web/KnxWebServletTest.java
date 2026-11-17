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
package org.openhab.binding.knx.internal.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.projectstore.LogCapture;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * Checks the page's servlet on mocked requests and responses, without a server: how it registers, and what it
 * answers.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxWebServletTest {
    private HttpService httpService = mock(HttpService.class);
    private HttpContext context = mock(HttpContext.class);

    /** What the servlet did to a mocked response. */
    static final class Answer {
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final Map<String, String> headers = new HashMap<>();
        final ByteArrayOutputStream body = new ByteArrayOutputStream();
        int status = 200;
        @Nullable
        String contentType;
        int contentLength = -1;

        Answer() throws IOException {
            doAnswer(call -> headers.put(call.getArgument(0), call.getArgument(1))).when(response)
                    .setHeader(anyString(), anyString());
            doAnswer(call -> headers.put(call.getArgument(0), call.getArgument(1))).when(response)
                    .addHeader(anyString(), anyString());
            doAnswer(call -> status = call.getArgument(0)).when(response).setStatus(anyInt());
            doAnswer(call -> contentType = call.getArgument(0)).when(response).setContentType(anyString());
            doAnswer(call -> contentLength = call.getArgument(0)).when(response).setContentLength(anyInt());
            when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
                @Override
                public void write(int b) {
                    body.write(b);
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(@Nullable WriteListener listener) {
                }
            });
        }

        String text() {
            return body.toString(StandardCharsets.UTF_8);
        }
    }

    @BeforeEach
    void setUp() {
        httpService = mock(HttpService.class);
        context = mock(HttpContext.class);
        when(httpService.createDefaultHttpContext()).thenReturn(context);
    }

    static HttpServletRequest request(String method, @Nullable String pathInfo) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(request.getRequestURI()).thenReturn("/knx" + (pathInfo == null ? "" : pathInfo));
        return request;
    }

    static Answer call(KnxWebServlet servlet, HttpServletRequest request) throws IOException, ServletException {
        Answer answer = new Answer();
        servlet.service(request, answer.response);
        return answer;
    }

    @Test
    void itRegistersItselfAtKnxWithTheDefaultContextAndUnregistersOnDeactivate() throws Exception {
        KnxWebServlet servlet = new KnxWebServlet(httpService);

        verify(httpService).registerServlet("/knx", servlet, null, context);
        servlet.deactivate();
        verify(httpService).unregister("/knx");
    }

    @Test
    void aFailedRegistrationIsOneWarningAndTheDeactivateAfterItThrowsNothing() throws Exception {
        doThrow(new NamespaceException("made-up clash")).when(httpService).registerServlet(anyString(), any(), any(),
                any());
        doThrow(new IllegalArgumentException("made-up: not registered")).when(httpService).unregister(anyString());

        try (LogCapture log = new LogCapture()) {
            KnxWebServlet servlet = new KnxWebServlet(httpService);
            servlet.deactivate();

            List<String> warnings = log.warnings();
            assertEquals(1, warnings.size(), warnings.toString());
            assertTrue(warnings.get(0).contains("/knx"), warnings.get(0));
        }
    }

    @Test
    void theStartPageIsServedAtTheSlash() throws Exception {
        KnxWebServlet servlet = new KnxWebServlet(httpService);

        Answer answer = call(servlet, request("GET", "/"));

        assertEquals(200, answer.status);
        assertEquals("text/html; charset=utf-8", answer.contentType);
        byte[] expected = Files.readAllBytes(KnxWebFilesTest.WEB.resolve("index.html"));
        assertArrayEquals(expected, answer.body.toByteArray());
        assertEquals(expected.length, answer.contentLength);
    }

    private static final Map<String, String> FIXED_HEADERS = Map.of( //
            "Content-Security-Policy",
            "default-src 'none'; script-src 'self'; style-src 'self'; img-src 'self'; connect-src 'self'; "
                    + "base-uri 'none'; form-action 'none'; frame-ancestors 'self'",
            "X-Content-Type-Options", "nosniff", //
            "Referrer-Policy", "no-referrer", //
            "Cache-Control", "no-cache");

    private static final String MARK = "zz-made-up-mark-7-7-9";

    private static void assertFixedHeaders(Answer answer) {
        FIXED_HEADERS.forEach((name, value) -> assertEquals(value, answer.headers.get(name), name));
    }

    /** Asserts that nothing of the request's path, query or headers came back, and that no error page was asked for. */
    private static void assertNothingEchoed(Answer answer) throws IOException {
        assertFalse(answer.text().contains(MARK), answer.text());
        answer.headers.forEach((name, value) -> assertFalse(value.contains(MARK), name + ": " + value));
        verify(answer.response, never()).sendError(anyInt());
        verify(answer.response, never()).sendError(anyInt(), any());
        verify(answer.response, never()).sendRedirect(any());
    }

    private static HttpServletRequest marked(String method, @Nullable String pathInfo) {
        HttpServletRequest request = request(method, pathInfo);
        when(request.getQueryString()).thenReturn("q=" + MARK);
        when(request.getHeader(anyString())).thenReturn(MARK);
        return request;
    }

    private String etag() {
        KnxWebFiles files = new KnxWebFiles(KnxWebFiles.ENTRIES);
        return files.etag(java.util.Objects.requireNonNull(files.find("/")));
    }

    @Test
    void aFoundFileHasTheFourHeadersAnEtagAndItsLength() throws Exception {
        Answer answer = call(new KnxWebServlet(httpService), request("GET", "/index.html"));

        assertEquals(200, answer.status);
        assertFixedHeaders(answer);
        assertEquals(etag(), answer.headers.get("ETag"));
        assertEquals(answer.body.size(), answer.contentLength);
    }

    @Test
    void theTileImageIsServedAsSvgToARequestWithoutAnyHeader() throws Exception {
        HttpServletRequest request = request("GET", "/img/tile.svg");

        Answer answer = call(new KnxWebServlet(httpService), request);

        assertEquals(200, answer.status);
        assertEquals("image/svg+xml", answer.contentType);
        assertFixedHeaders(answer);
        assertArrayEquals(Files.readAllBytes(KnxWebFilesTest.WEB.resolve("img/tile.svg")), answer.body.toByteArray());
        verify(request, never()).getHeader("Authorization");
    }

    @Test
    void headIsGetWithoutTheBody() throws Exception {
        Answer answer = call(new KnxWebServlet(httpService), request("HEAD", "/index.html"));

        assertEquals(200, answer.status);
        assertFixedHeaders(answer);
        assertEquals("text/html; charset=utf-8", answer.contentType);
        assertEquals(Files.size(KnxWebFilesTest.WEB.resolve("index.html")), answer.contentLength);
        assertEquals(etag(), answer.headers.get("ETag"));
        assertEquals(0, answer.body.size());
    }

    @Test
    void aMatchingIfNoneMatchGives304WithoutBody() throws Exception {
        HttpServletRequest request = request("GET", "/");
        when(request.getHeader("If-None-Match")).thenReturn(etag());

        Answer answer = call(new KnxWebServlet(httpService), request);

        assertEquals(304, answer.status);
        assertFixedHeaders(answer);
        assertEquals(etag(), answer.headers.get("ETag"));
        assertEquals(0, answer.body.size());
    }

    @Test
    void anIfNoneMatchListHoldingTheEtagGives304() throws Exception {
        HttpServletRequest request = request("GET", "/");
        when(request.getHeader("If-None-Match")).thenReturn("\"0000000000000000\", " + etag());

        assertEquals(304, call(new KnxWebServlet(httpService), request).status);
    }

    @Test
    void anIfNoneMatchThatDiffersGivesTheFile() throws Exception {
        HttpServletRequest request = request("GET", "/");
        when(request.getHeader("If-None-Match")).thenReturn("\"0000000000000000\"");

        Answer answer = call(new KnxWebServlet(httpService), request);

        assertEquals(200, answer.status);
        assertTrue(answer.body.size() > 0);
    }

    @Test
    void knxWithoutSlashRedirectsToTheRelativeFolder() throws Exception {
        Answer answer = call(new KnxWebServlet(httpService), marked("GET", null));

        assertEquals(302, answer.status);
        assertEquals("knx/", answer.headers.get("Location"));
        assertFixedHeaders(answer);
        assertNothingEchoed(answer);
        assertEquals(0, answer.body.size());
    }

    @Test
    void anUnknownPathIs404WithAFixedTextAndTheFourHeaders() throws Exception {
        Answer answer = call(new KnxWebServlet(httpService), marked("GET", "/" + MARK + ".html"));

        assertEquals(404, answer.status);
        assertFixedHeaders(answer);
        assertEquals("text/plain; charset=utf-8", answer.contentType);
        assertEquals("Not found", answer.text());
        assertEquals(9, answer.contentLength);
        assertNothingEchoed(answer);
    }

    @Test
    void aHeadOfAnUnknownPathIs404WithoutBody() throws Exception {
        Answer answer = call(new KnxWebServlet(httpService), request("HEAD", "/../index.html"));

        assertEquals(404, answer.status);
        assertFixedHeaders(answer);
        assertEquals(0, answer.body.size());
    }

    @Test
    void everyOtherMethodIs405WithAllowAndTheFourHeadersAndNothingEchoed() throws Exception {
        for (String method : List.of("POST", "PUT", "DELETE", "OPTIONS", "TRACE", "PATCH", "PROPFIND")) {
            for (String path : new String[] { "/", "/index.html", "/" + MARK }) {
                Answer answer = call(new KnxWebServlet(httpService), marked(method, path));

                assertEquals(405, answer.status, method);
                assertEquals("GET, HEAD", answer.headers.get("Allow"), method);
                assertFixedHeaders(answer);
                assertEquals(0, answer.body.size(), method);
                assertNothingEchoed(answer);
            }
        }
    }

    @Test
    void anEntryWhoseFileIsMissingIsOneErrorAtActivationAnd404WithoutALogLine() throws Exception {
        KnxWebFiles.Entry missing = new KnxWebFiles.Entry("gone.js", "web/gone.js", KnxWebFiles.JAVASCRIPT);
        KnxWebFiles files = new KnxWebFiles(
                List.of(java.util.Objects.requireNonNull(new KnxWebFiles(KnxWebFiles.ENTRIES).find("/")), missing));

        try (LogCapture log = new LogCapture()) {
            KnxWebServlet servlet = new KnxWebServlet(httpService, files);
            List<String> errors = log.errors();
            assertEquals(1, errors.size(), errors.toString());
            assertTrue(errors.get(0).contains("gone.js"), errors.get(0));
            int atActivation = log.all().size();

            assertEquals(404, call(servlet, request("GET", "/gone.js")).status);
            assertEquals(404, call(servlet, request("HEAD", "/gone.js")).status);
            assertEquals(200, call(servlet, request("GET", "/")).status);

            assertEquals(atActivation, log.all().size(), log.all().toString());
        }
    }

    @Test
    void requestsLogNothing() throws Exception {
        KnxWebServlet servlet = new KnxWebServlet(httpService);

        try (LogCapture log = new LogCapture()) {
            call(servlet, request("GET", "/"));
            call(servlet, marked("GET", "/" + MARK));
            call(servlet, marked("POST", "/"));
            call(servlet, marked("GET", null));

            assertEquals(List.of(), log.all());
        }
    }
}
