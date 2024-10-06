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
package org.openhab.binding.http.internal.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Jetty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HttpThingConfig} class contains fields mapping thing configuration parameters.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class HttpThingConfig {
    private final Logger logger = LoggerFactory.getLogger(HttpThingConfig.class);

    public String baseURL = "";
    public int refresh = 30;
    public int timeout = 3000;
    public int delay = 0;

    public String username = "";
    public String password = "";

    public HttpAuthMode authMode = HttpAuthMode.BASIC;
    public HttpMethod stateMethod = HttpMethod.GET;

    public HttpMethod commandMethod = HttpMethod.GET;
    public int bufferSize = 2048;

    public @Nullable String encoding = null;
    public @Nullable String contentType = null;

    public boolean ignoreSSLErrors = false;
    public boolean strictErrorHandling = false;

    // ArrayList is required as implementation because list may be modified later
    public ArrayList<String> headers = new ArrayList<>();
    public String userAgent = "";

    public Map<String, String> getHeaders() {
        Map<String, String> headersMap = new HashMap<>();
        // add user agent first, in case it is also defined in the headers, it'll be overwritten
        headersMap.put(HttpHeader.USER_AGENT.asString(),
                userAgent.isBlank() ? "Jetty/" + Jetty.VERSION : userAgent.trim());
        headers.forEach(header -> {
            String[] keyValuePair = header.split("=", 2);
            if (keyValuePair.length == 2) {
                headersMap.put(keyValuePair[0].trim(), keyValuePair[1].trim());
            } else {
                logger.warn("Splitting header '{}' failed. No '=' was found. Ignoring", header);
            }
        });

        return headersMap;
    }
}
