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
package org.openhab.binding.gardena.internal.exception;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception if something happens in the communication to Gardena smart system.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class GardenaException extends IOException {

    private static final long serialVersionUID = 8568935118878542270L;
    private int status; // http status

    public GardenaException(String message) {
        super(message);
        this.status = -1;
    }

    public GardenaException(Throwable ex) {
        super(ex);
        this.status = -1;
    }

    public GardenaException(@Nullable String message, Throwable cause) {
        super(message, cause);
        this.status = -1;
    }

    public GardenaException(String message, int status) {
        super(message);
        this.status = status;
    }

    public GardenaException(Throwable ex, int status) {
        super(ex);
        this.status = status;
    }

    public GardenaException(@Nullable String message, Throwable cause, int status) {
        super(message, cause);
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }
}
