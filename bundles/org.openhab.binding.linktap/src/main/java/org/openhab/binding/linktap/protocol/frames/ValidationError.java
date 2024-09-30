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
package org.openhab.binding.linktap.protocol.frames;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ValidationError} represents a data payload validation error.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class ValidationError {
    String variable = "";
    String error = "";
    Cause cause = Cause.BUG;

    public ValidationError(String var, String err) {
        this.variable = var;
        this.error = err;
    }

    public ValidationError(String var, String err, Cause cause) {
        this.variable = var;
        this.error = err;
        this.cause = cause;
    }

    public static enum Cause {
        BUG,
        USER
    }

    public Cause getCause() {
        return this.cause;
    }

    public String toString() {
        return this.variable + " " + this.cause;
    }
}
