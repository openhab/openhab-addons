/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.api;

import org.openhab.binding.freebox.internal.api.model.FreeboxResponse;

/**
 * Exception for errors when using the Freebox API
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxException extends Exception {

    private static final long serialVersionUID = 1L;

    protected FreeboxResponse<?> response;

    public FreeboxException(String msg) {
        this(msg, null, null);
    }

    public FreeboxException(String msg, Throwable cause) {
        this(msg, cause, null);
    }

    public FreeboxException(String msg, FreeboxResponse<?> response) {
        this(msg, null, response);
    }

    public FreeboxException(FreeboxResponse<?> response) {
        this(response.getMsg(), null, response);
    }

    public FreeboxException(String msg, Throwable cause, FreeboxResponse<?> response) {
        super(msg, cause);
        this.response = response;
    }

    public FreeboxResponse<?> getResponse() {
        return response;
    }

    public boolean isAuthRequired() {
        return getResponse() != null && getResponse().isAuthRequired();
    }

    public boolean isMissingRights() {
        return getResponse() != null && getResponse().isMissingRights();
    }
}
