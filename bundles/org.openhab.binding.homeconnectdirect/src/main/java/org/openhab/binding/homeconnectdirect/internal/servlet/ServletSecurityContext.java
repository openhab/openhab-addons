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
package org.openhab.binding.homeconnectdirect.internal.servlet;

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ZONE_ID;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.common.utils.ConfigurationUtils;
import org.openhab.core.util.StringUtils;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 *
 * Home Connect Direct servlet security context.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class ServletSecurityContext {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int TOKEN_LENGTH = 32;
    private static final int TOKEN_EXPIRATION_HOURS = 24;

    @Nullable
    private static ServletSecurityContext instance;

    private final Map<String, LocalDateTime> activeTokens = new ConcurrentHashMap<>();

    private ServletSecurityContext() {
        // Singleton class
    }

    /**
     * Get the servlet security context.
     * 
     * @return context
     */
    @NonNullByDefault({})
    public static synchronized ServletSecurityContext get() {
        if (instance == null) {
            instance = new ServletSecurityContext();
        }
        return instance;
    }

    /**
     * Validate an authorization token.
     * 
     * @param request HTTP request
     * @param configurationAdmin Configuration admin service
     * @return true if valid
     */
    public boolean isValidAuthorization(HttpServletRequest request, ConfigurationAdmin configurationAdmin) {
        var configuration = ConfigurationUtils.getConfiguration(configurationAdmin);
        if (!configuration.loginEnabled) {
            return true;
        }

        String token;
        var authHeader = request.getHeader(HEADER_AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            token = authHeader.substring(BEARER_PREFIX.length());
        } else {
            token = request.getParameter("token");
        }

        if (token != null && activeTokens.containsKey(token)) {
            LocalDateTime expiration = activeTokens.get(token);
            if (expiration != null && expiration.isAfter(LocalDateTime.now(ZONE_ID))) {
                return true;
            } else {
                activeTokens.remove(token); // Token expired
            }
        }
        return false;
    }

    /**
     * Invalidate an authorization token.
     * 
     * @param request HTTP request
     */
    public void invalidateAuthorization(HttpServletRequest request) {
        String authHeader = request.getHeader(HEADER_AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            activeTokens.remove(token);
        }
    }

    /**
     * Create and register a new authorization token.
     * 
     * @return token
     */
    public String createAndRegisterAuthorizationToken() {
        String token = StringUtils.getRandomAlphanumeric(TOKEN_LENGTH);
        activeTokens.put(token, LocalDateTime.now(ZONE_ID).plusHours(TOKEN_EXPIRATION_HOURS));
        return token;
    }

    /**
     * Clear all active tokens.
     */
    public void clearAllTokens() {
        activeTokens.clear();
    }
}
