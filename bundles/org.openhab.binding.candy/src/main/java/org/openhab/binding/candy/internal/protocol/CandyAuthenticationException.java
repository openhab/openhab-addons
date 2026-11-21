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
package org.openhab.binding.candy.internal.protocol;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Account authorization failed and requires an explicit retry or configuration change.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class CandyAuthenticationException extends CandyProtocolException {
    @Serial
    private static final long serialVersionUID = 1L;

    public CandyAuthenticationException() {
        super("@text/account-error-authentication");
    }

    public CandyAuthenticationException(String message) {
        super(message);
    }

    public CandyAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
