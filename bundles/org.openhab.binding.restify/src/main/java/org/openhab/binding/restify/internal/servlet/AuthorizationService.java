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
package org.openhab.binding.restify.internal.servlet;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.restify.internal.RestifyBinding;
import org.openhab.binding.restify.internal.RestifyBindingConfig;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(service = AuthorizationService.class)
public class AuthorizationService {
    private final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);
    private final RestifyBinding restifyBinding;

    @Activate
    public AuthorizationService(@Reference RestifyBinding restifyBinding) {
        this.restifyBinding = restifyBinding;
    }

    public void authorize(@Nullable Authorization required, @Nullable String provided) throws AuthorizationException {
        var config = restifyBinding.getConfig();
        var effectiveRequired = required;
        if (effectiveRequired == null) {
            effectiveRequired = resolveDefaultAuthorization(config, provided);
            if (effectiveRequired != null) {
                logger.debug("No endpoint authorization configured, using {} from binding defaults",
                        effectiveRequired.getClass().getSimpleName());
            } else {
                logger.debug("No endpoint authorization configured and no valid default authorization provided");
            }
        }
        if (effectiveRequired == null) {
            if (config.enforceAuthentication()) {
                throw new AuthorizationException("servlet.error.authorization.missing-config-or-disable-enforce");
            }
            return; // no authorization required
        }
        if (provided == null) {
            throw new AuthorizationException("servlet.error.authorization.required");
        }
        switch (effectiveRequired) {
            case Authorization.Basic basic -> authorizeBasic(basic, provided);
            case Authorization.Bearer bearer -> authorizeBearer(bearer, provided);
        }
    }

    private @Nullable Authorization resolveDefaultAuthorization(RestifyBindingConfig config,
            @Nullable String provided) {
        if (provided == null) {
            return null;
        }
        if (provided.startsWith(Authorization.BASIC_PREFIX)) {
            return parseDefaultBasic(config.defaultBasic());
        }
        if (provided.startsWith(Authorization.BEARER_PREFIX)) {
            return parseDefaultBearer(config.defaultBearer());
        }
        return null;
    }

    private @Nullable Authorization parseDefaultBasic(@Nullable String defaultBasic) {
        if (defaultBasic == null) {
            return null;
        }
        var separatorIndex = defaultBasic.indexOf(':');
        if (separatorIndex <= 0 || separatorIndex >= defaultBasic.length() - 1) {
            logger.warn("Ignoring invalid restify defaultBasic value, expected username:password format");
            return null;
        }
        var username = defaultBasic.substring(0, separatorIndex);
        var password = defaultBasic.substring(separatorIndex + 1);
        return new Authorization.Basic(username, password);
    }

    private @Nullable Authorization parseDefaultBearer(@Nullable String defaultBearer) {
        if (defaultBearer == null) {
            return null;
        }
        return new Authorization.Bearer(defaultBearer);
    }

    private void authorizeBasic(Authorization.Basic basic, String provided) throws AuthorizationException {
        if (!provided.startsWith(basic.prefix())) {
            throw new AuthorizationException("servlet.error.authorization.invalid-username-or-password");
        }
        var encodedCredentials = provided.substring(basic.prefix().length());
        final String credentials;
        try {
            credentials = new String(java.util.Base64.getDecoder().decode(encodedCredentials), UTF_8);
        } catch (IllegalArgumentException e) {
            throw new AuthorizationException("servlet.error.authorization.invalid-username-or-password");
        }
        var separatorIndex = credentials.indexOf(':');
        if (separatorIndex <= 0) {
            throw new AuthorizationException("servlet.error.authorization.invalid-username-or-password");
        }
        var providedUsername = credentials.substring(0, separatorIndex);
        var providedPassword = credentials.substring(separatorIndex + 1);
        if (timingSafeNotEquals(providedUsername, basic.username())
                || timingSafeNotEquals(providedPassword, basic.password())) {
            throw new AuthorizationException("servlet.error.authorization.invalid-username-or-password");
        }
    }

    private void authorizeBearer(Authorization.Bearer bearer, String provided) throws AuthorizationException {
        if (!provided.startsWith(bearer.prefix())) {
            throw new AuthorizationException("servlet.error.authorization.invalid-token");
        }
        var providedToken = provided.substring(bearer.prefix().length());
        if (timingSafeNotEquals(providedToken, bearer.token())) {
            throw new AuthorizationException("servlet.error.authorization.invalid-token");
        }
    }

    private static boolean timingSafeNotEquals(String left, String right) {
        return !MessageDigest.isEqual(left.getBytes(UTF_8), right.getBytes(UTF_8));
    }
}
