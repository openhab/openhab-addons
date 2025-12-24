/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linktap.protocol.http;

import java.io.Serial;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link I18Exception} is a abstract class for exceptions that support
 * i18key functionality.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public abstract class I18Exception extends Exception {

    @Serial
    private static final long serialVersionUID = -7784829349743963947L;

    protected String i18Key = "";

    public I18Exception() {
    }

    public I18Exception(final String message) {
        super(message);
    }

    public I18Exception(final Throwable cause) {
        super(cause);
    }

    public I18Exception(final String message, final Throwable cause) {
        super(message, cause);
    }

    public abstract String getI18Key();

    public String getI18Key(final String defaultI18) {
        if (!i18Key.isBlank()) {
            return i18Key;
        }
        return Objects.requireNonNullElse(getMessage(), defaultI18);
    }
}
