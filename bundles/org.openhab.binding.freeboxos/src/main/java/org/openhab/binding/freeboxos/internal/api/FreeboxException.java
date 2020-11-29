/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception for errors when using the Freebox API
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class FreeboxException extends Exception {
    private static final long serialVersionUID = 9197365222439228186L;

    private @Nullable BaseResponse response;

    public FreeboxException(String msg) {
        this(msg, null, null);
    }

    public FreeboxException(@Nullable String msg, Throwable cause) {
        this(msg, cause, null);
    }

    FreeboxException(@Nullable String msg, BaseResponse response) {
        this(msg, null, response);
    }

    public FreeboxException(@Nullable String msg, @Nullable Throwable cause, @Nullable BaseResponse response) {
        super(msg, cause);
        this.response = response;
    }

    public @Nullable BaseResponse getResponse() {
        return response;
    }
}
