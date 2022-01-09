/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal.automation;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.handler.ActionHandler;
import org.openhab.core.automation.handler.BaseModuleHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The action module type handled by this class allows to execute a http request (GET, POST, PUT, etc)
 * on a given address. Relative addresses are supported.
 * <p>
 * The optional mimetype and body configuration parameters allow to send arbitrary data with a request.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HttpActionHandler extends BaseModuleHandler<Action> implements ActionHandler {
    private final Logger logger = LoggerFactory.getLogger(HttpActionHandler.class);

    public static final String MODULE_TYPE_ID = "rules.HttpAction";
    public static final String CALLBACK_CONTEXT_NAME = "CALLBACK";
    public static final String MODULE_CONTEXT_NAME = "MODULE";

    public static final String CFG_METHOD = "method";
    public static final String CFG_URL = "url";
    public static final String CFG_BODY = "body";
    public static final String CFG_MIMETYPE = "mimetype";
    public static final String CFG_TIMEOUT = "timeout";

    private static class Config {
        HttpMethod method = HttpMethod.GET;
        String url = "";
        String body = "";
        String mimetype = "application/json";
        int timeout = 5;
    }

    private Config config = new Config();
    private HttpClient httpClient;

    public HttpActionHandler(final Action module, HttpClientFactory httpFactory) {
        super(module);

        this.config = module.getConfiguration().as(Config.class);
        if (config.url.isEmpty()) {
            throw new IllegalArgumentException("URL not set!");
        }
        // convert relative path to absolute one
        String url = config.url;
        if (url.startsWith("/")) {
            config.url = "http://localhost:" + Integer.getInteger("org.osgi.service.http.port", 8080).toString() + url;
        }

        httpClient = httpFactory.createHttpClient("HttpActionHandler_" + module.getId());
    }

    @Override
    public @Nullable Map<String, Object> execute(Map<String, Object> context) {
        try {
            Request request = httpClient.newRequest(URI.create(config.url)).method(config.method)
                    .timeout(config.timeout, TimeUnit.SECONDS);
            if (config.method == HttpMethod.POST || config.method == HttpMethod.PUT) {
                request.content(new StringContentProvider(config.body), config.mimetype);
            }
            request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Failed to send http request", e);
        }
        return null;
    }
}
