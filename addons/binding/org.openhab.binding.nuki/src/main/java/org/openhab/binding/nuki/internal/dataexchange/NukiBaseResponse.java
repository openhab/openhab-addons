/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nuki.internal.dataexchange;

import java.time.Instant;

/**
 * The {@link NukiBaseResponse} class is the base class for API Responses.
 *
 * @author Markus Katter - Initial contribution
 */
public class NukiBaseResponse {

    private int status;
    private String message;
    private boolean success;
    private Instant created;

    public NukiBaseResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.created = Instant.now();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Instant getCreated() {
        return this.created;
    }
}
