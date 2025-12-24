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
package org.openhab.binding.miio.internal.miot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Will be thrown for cloud errors
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiotParseException extends Exception {
    /**
     * required variable to avoid IncorrectMultilineIndexException warning
     */
    private static final long serialVersionUID = -1280858607995252322L;

    public MiotParseException() {
    }

    public MiotParseException(@Nullable String message) {
        super(message);
    }

    public MiotParseException(@Nullable String message, @Nullable Exception e) {
        super(message, e);
    }
}
