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
package org.openhab.binding.knx.internal.project;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown when an ETS project file cannot be read. The message names the place in the file, never a value
 * from it.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class KnxProjectException extends Exception {

    private static final long serialVersionUID = 1L;

    /** Why the file could not be read. */
    public enum Reason {
        /** Not a zip archive, or a zip archive without a project part. */
        NOT_A_PROJECT,
        /** The project part is protected and no password was given. */
        PASSWORD_REQUIRED,
        /** The project part is protected and the password does not open it. */
        WRONG_PASSWORD,
        /** The project part uses a protection this reader cannot open. */
        UNSUPPORTED_PROTECTION,
        /** The file exceeds one of the reader's size limits. */
        LIMIT_EXCEEDED,
        /** The file is damaged or its content does not have the expected form. */
        MALFORMED
    }

    private final Reason reason;

    public KnxProjectException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
