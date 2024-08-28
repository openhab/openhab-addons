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
package org.openhab.binding.hydrawise.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown for connection issues to the Hydrawise controller
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class HydrawiseConnectionException extends Exception {
    private static final long serialVersionUID = 1L;

    private int code = 0;
    private String response = "";

    public HydrawiseConnectionException(Exception e) {
        super(e);
    }

    public HydrawiseConnectionException(String message) {
        super(message);
    }

    public HydrawiseConnectionException(String message, int code, String response) {
        super(message);
        this.code = code;
        this.response = response;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public int getCode() {
        return code;
    }

    public String getResponse() {
        return response;
    }
}
