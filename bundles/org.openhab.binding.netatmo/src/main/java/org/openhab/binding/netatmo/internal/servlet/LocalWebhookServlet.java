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
package org.openhab.binding.netatmo.internal.servlet;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.WebhookApi;
import org.openhab.binding.netatmo.internal.deserialization.NADeserializer;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netatmo webhook implementation using a configured local URL.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LocalWebhookServlet extends WebhookServlet {
    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(LocalWebhookServlet.class);
    private final String urlPattern;

    public LocalWebhookServlet(ApiBridgeHandler handler, HttpService httpService, NADeserializer deserializer,
            WebhookApi api, String urlPattern) {
        super(handler, httpService, deserializer, api);
        this.urlPattern = urlPattern;
    }

    @Override
    protected void registerEndpoint() {
        try {
            URI uri = UriBuilder.fromUri(publishedPath()).build();
            registerWebhook(uri);
        } catch (UriBuilderException e) {
            logger.warn("Invalid webhook URL pattern: {}", e.getMessage());
        }
    }

    @Override
    protected String publishedPath() {
        return urlPattern.formatted(getPath());
    }
}
