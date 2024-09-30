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
package org.openhab.binding.linktap.protocol.servers;

import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.EMPTY_STRING;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.linktap.internal.LinkTapBindingConstants;
import org.openhab.binding.linktap.internal.TransactionProcessor;
import org.openhab.binding.linktap.internal.Utils;
import org.openhab.binding.linktap.protocol.frames.TLGatewayFrame;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BindingServlet} defines the request to enable or disable alerts from a given device.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class BindingServlet extends HttpServlet {

    public static final BindingServlet INSTANCE = new BindingServlet();
    public static final String SERVLET_URL_WITHOUT_ROOT = "linktap";
    private static final String SERVLET_URL = "/" + SERVLET_URL_WITHOUT_ROOT;
    private static final long serialVersionUID = -23L;

    private final Logger logger = LoggerFactory.getLogger(BindingServlet.class);
    private final Object registerLock = new Object();

    private volatile boolean registered;

    @Nullable
    HttpService httpService;
    private @Nullable TranslationProvider translationProvider;
    private @Nullable LocaleProvider localeProvider;
    private @Nullable Bundle bundle;

    public void setTranslationProviderInfo(TranslationProvider translationProvider, LocaleProvider localeProvider,
            Bundle bundle) {
        this.bundle = bundle;
        this.localeProvider = localeProvider;
        this.translationProvider = translationProvider;
    }

    public static final BindingServlet getInstance() {
        return INSTANCE;
    }

    public String getLocalizedText(String key, @Nullable Object @Nullable... arguments) {
        TranslationProvider translationProv = translationProvider;
        LocaleProvider localeProv = localeProvider;
        if (translationProv == null || localeProv == null) {
            return key;
        }
        String result = translationProv.getText(bundle, key, key, localeProv.getLocale(), arguments);
        return Objects.nonNull(result) ? result : key;
    }

    public void setHttpService(final HttpService httpService) {
        this.httpService = httpService;
    }

    public static String getServletAddress(final String hostname, final String localizedWarning) {
        final String httpPortStr = System.getProperty("org.osgi.service.http.port");
        final Logger logger = LoggerFactory.getLogger(BindingServlet.class);
        if (httpPortStr == null || httpPortStr.isEmpty()) {
            logger.warn("{}", localizedWarning);
            return EMPTY_STRING;
        }
        return "http://" + hostname + ":" + httpPortStr + SERVLET_URL;
    }

    public void registerServlet() {
        final HttpService srv = httpService;
        synchronized (registerLock) {
            if (!registered && srv != null) {
                try {
                    srv.registerServlet(SERVLET_URL, this, null, srv.createDefaultHttpContext());
                    registered = true;
                    logger.trace("Registered servlet " + SERVLET_URL);
                } catch (NamespaceException | ServletException e) {
                    logger.warn("{}",
                            getLocalizedText("exception.fail-servlet-registration", SERVLET_URL, Utils.getMessage(e)));
                }
            }
        }
    }

    public void unregisterServlet() {
        final HttpService srv = httpService;
        synchronized (registerLock) {
            if (registered && srv != null) {
                srv.unregister(SERVLET_URL);
                registered = false;
                logger.trace("Unregistered servlet");
            }
        }
    }

    @Override
    protected void doPost(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req == null) {
            return;
        }

        int bufferSize = 1000; // The payload string is technically limited to 768 characters - this should be enough
                               // a single read to fully fit
        char[] buffer = new char[bufferSize];
        StringBuilder out = new StringBuilder();
        try (Reader in = new InputStreamReader(req.getInputStream(), StandardCharsets.UTF_8)) {
            for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0;) {
                out.append(buffer, 0, numRead);
            }
        }

        String payload = out.toString();
        final TLGatewayFrame tlFrame = LinkTapBindingConstants.GSON.fromJson(payload, TLGatewayFrame.class);
        String result = "";
        if (tlFrame != null) {
            TransactionProcessor tp = TransactionProcessor.getInstance();
            result = tp.processGwRequest(req.getRemoteAddr(), tlFrame.command, payload);
        }
        if (resp == null) {
            return;
        }

        resp.setContentType(MediaType.APPLICATION_JSON);
        resp.setStatus(HttpStatus.OK_200);
        resp.getWriter().append(result);
        resp.getWriter().close();
    }
}
