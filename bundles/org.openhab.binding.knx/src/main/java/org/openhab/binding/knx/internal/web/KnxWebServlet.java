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

import java.io.IOException;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.web.KnxWebFiles.Entry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves the add-on's page at {@code /knx/}: the files of {@link KnxWebFiles} and nothing else, without login. The
 * page holds no data; every data call it makes goes to the add-on's REST resource with an admin token.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
@Component(service = KnxWebServlet.class, immediate = true)
public class KnxWebServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    static final String ALIAS = "/knx";

    static final Map<String, String> FIXED_HEADERS = Map.of( //
            "Content-Security-Policy",
            "default-src 'none'; script-src 'self'; style-src 'self'; img-src 'self'; connect-src 'self'; "
                    + "base-uri 'none'; form-action 'none'; frame-ancestors 'self'",
            "X-Content-Type-Options", "nosniff", //
            "Referrer-Policy", "no-referrer", //
            "Cache-Control", "no-cache");

    private static final String NOT_FOUND_TYPE = "text/plain; charset=utf-8";
    private static final byte[] NOT_FOUND = "Not found".getBytes(StandardCharsets.UTF_8);

    private final Logger logger = LoggerFactory.getLogger(KnxWebServlet.class);
    private final HttpService httpService;
    private final KnxWebFiles files;

    @Activate
    public KnxWebServlet(final @Reference HttpService httpService) {
        this(httpService, new KnxWebFiles(KnxWebFiles.ENTRIES));
    }

    KnxWebServlet(HttpService httpService, KnxWebFiles files) {
        this.httpService = httpService;
        this.files = files;
        for (Entry entry : files.missing()) {
            logger.error("KNX page: the file {} is missing from the add-on; it answers 404", entry.path());
        }
        try {
            httpService.registerServlet(ALIAS, this, null, httpService.createDefaultHttpContext());
        } catch (ServletException | NamespaceException | RuntimeException e) {
            logger.warn("KNX page: it cannot be registered at {} ({}); the add-on works without it", ALIAS,
                    e.getClass().getName());
        }
    }

    @Deactivate
    void deactivate() {
        try {
            httpService.unregister(ALIAS);
        } catch (IllegalArgumentException e) {
            // the registration had failed
        }
    }

    /**
     * Answers {@code GET} and {@code HEAD} from the table and every other method with 405 itself, so that none of
     * {@link HttpServlet}'s own answers (to {@code OPTIONS}, {@code TRACE}, an unknown method) is given. Every answer
     * carries the fixed headers and is made with {@code setStatus}: an error page of the container could hold the
     * request's address.
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        FIXED_HEADERS.forEach(response::setHeader);
        String method = request.getMethod();
        boolean head = "HEAD".equals(method);
        if (!head && !"GET".equals(method)) {
            response.setHeader("Allow", "GET, HEAD");
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            // relative, so that it also holds behind a proxy that serves openHAB under a path of its own
            response.setHeader("Location", "knx/");
            response.setStatus(HttpServletResponse.SC_FOUND);
            return;
        }
        Entry entry = files.find(pathInfo);
        if (entry == null) {
            answer(response, HttpServletResponse.SC_NOT_FOUND, NOT_FOUND_TYPE, NOT_FOUND, head);
            return;
        }
        String etag = files.etag(entry);
        response.setHeader("ETag", etag);
        if (matches(request.getHeader("If-None-Match"), etag)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        answer(response, HttpServletResponse.SC_OK, entry.contentType(), files.load(entry), head);
    }

    private static void answer(HttpServletResponse response, int status, String contentType, byte[] body, boolean head)
            throws IOException {
        response.setStatus(status);
        response.setContentType(contentType);
        response.setContentLength(body.length);
        if (!head) {
            response.getOutputStream().write(body);
        }
    }

    private static boolean matches(@Nullable String ifNoneMatch, String etag) {
        if (ifNoneMatch == null) {
            return false;
        }
        for (String tag : ifNoneMatch.split(",")) {
            String trimmed = tag.trim();
            if (trimmed.equals(etag) || "*".equals(trimmed)) {
                return true;
            }
        }
        return false;
    }
}
