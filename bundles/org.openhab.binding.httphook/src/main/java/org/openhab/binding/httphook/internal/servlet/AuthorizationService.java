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
package org.openhab.binding.httphook.internal.servlet;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.Serial;
import java.io.Serializable;
import java.security.MessageDigest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.httphook.internal.HttpHookBinding;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Martin Grzeslowski - Initial contribution
 * @author Martin Grześlowski - Authentication policy for binding defaults
 */
@NonNullByDefault
@Component(service = AuthorizationService.class)
public class AuthorizationService implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final HttpHookBinding httpHookBinding;

    @Activate
    public AuthorizationService(@Reference HttpHookBinding httpHookBinding) {
        this.httpHookBinding = httpHookBinding;
    }

    public void authorize(@Nullable Authorization required, @Nullable String provided) throws AuthorizationException {
        if (required != null) {
            authorizeRequired(required, provided);
            return;
        }
        var config = httpHookBinding.getConfig();
        if (config == null) {
            throw new AuthorizationException("servlet.error.authorization.not-initialized");
        }
        var defaultBasic = config.defaultBasic();
        var defaultBearer = config.defaultBearer();
        if (defaultBasic == null && defaultBearer == null) {
            if (config.enforceAuthentication()) {
                throw new AuthorizationException("servlet.error.authorization.missing-config-or-disable-enforce");
            }
            return;
        }
        if (provided != null && defaultBasic != null && matchesScheme(provided, Authorization.BASIC_PREFIX)) {
            authorizeBasic(parseDefaultBasic(defaultBasic), provided);
        } else if (provided != null && defaultBearer != null && matchesScheme(provided, Authorization.BEARER_PREFIX)) {
            authorizeBearer(new Authorization.Bearer(defaultBearer), provided);
        } else {
            throw new AuthorizationException("servlet.error.authorization.required");
        }
    }

    private void authorizeRequired(Authorization required, @Nullable String provided) throws AuthorizationException {
        if (provided == null) {
            throw new AuthorizationException("servlet.error.authorization.required");
        }
        switch (required) {
            case Authorization.Basic basic -> authorizeBasic(basic, provided);
            case Authorization.Bearer bearer -> authorizeBearer(bearer, provided);
        }
    }

    private static Authorization.Basic parseDefaultBasic(String defaultBasic) throws AuthorizationException {
        var separatorIndex = defaultBasic.indexOf(':');
        if (separatorIndex <= 0 || separatorIndex >= defaultBasic.length() - 1) {
            throw new AuthorizationException("servlet.error.authorization.invalid-username-or-password");
        }
        var username = defaultBasic.substring(0, separatorIndex);
        var password = defaultBasic.substring(separatorIndex + 1);
        return new Authorization.Basic(username, password);
    }

    private void authorizeBasic(Authorization.Basic basic, String provided) throws AuthorizationException {
        if (!matchesScheme(provided, basic.prefix())) {
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
        if (!matchesScheme(provided, bearer.prefix())) {
            throw new AuthorizationException("servlet.error.authorization.invalid-token");
        }
        var providedToken = provided.substring(bearer.prefix().length());
        if (timingSafeNotEquals(providedToken, bearer.token())) {
            throw new AuthorizationException("servlet.error.authorization.invalid-token");
        }
    }

    private static boolean matchesScheme(String provided, String prefix) {
        return provided.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private static boolean timingSafeNotEquals(String left, String right) {
        return !MessageDigest.isEqual(left.getBytes(UTF_8), right.getBytes(UTF_8));
    }
}
