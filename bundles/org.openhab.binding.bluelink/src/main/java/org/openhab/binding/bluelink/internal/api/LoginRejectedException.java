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
package org.openhab.binding.bluelink.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Login refused by the Bluelink identity provider for a reason the user has to act on.
 *
 * @author Carlo Dischler - Initial contribution
 */
@NonNullByDefault
public class LoginRejectedException extends BluelinkApiException {

    private static final long serialVersionUID = 1L;

    public enum Reason {
        BLOCKED("blocked"),
        CONSENT_REQUIRED("consent-required"),
        INVALID_CREDENTIALS("invalid-credentials"),
        REJECTED("rejected");

        private final String key;

        Reason(final String key) {
            this.key = key;
        }
    }

    private final Reason reason;
    private final String detail;

    public LoginRejectedException(final Reason reason, final String message) {
        this(reason, message, "");
    }

    public LoginRejectedException(final Reason reason, final String message, final String detail) {
        super(message);
        this.reason = reason;
        this.detail = detail;
    }

    public Reason getReason() {
        return reason;
    }

    @Override
    public @Nullable String getStatusDescription() {
        final String key = "@text/account-handler.login." + reason.key;
        // the framework strips quotes and brackets from arguments and splits them at commas
        final String argument = detail.replaceAll("[\"\\[\\],\\s]+", " ").trim();
        return argument.isEmpty() ? key : key + " [\"" + argument + "\"]";
    }
}
