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
package org.openhab.binding.twilio.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown by {@link TwilioApiClient} when an API call fails.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class TwilioApiException extends Exception {

    private static final long serialVersionUID = 1L;

    private final boolean configurationError;

    public TwilioApiException(String message) {
        super(message);
        this.configurationError = false;
    }

    public TwilioApiException(String message, boolean configurationError) {
        super(message);
        this.configurationError = configurationError;
    }

    public TwilioApiException(String message, Throwable cause) {
        super(message, cause);
        this.configurationError = false;
    }

    /**
     * @return true if this error indicates a configuration problem (e.g. bad credentials)
     */
    public boolean isConfigurationError() {
        return configurationError;
    }
}
