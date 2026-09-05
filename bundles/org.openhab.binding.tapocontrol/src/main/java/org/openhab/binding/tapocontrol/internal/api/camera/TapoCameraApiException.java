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
package org.openhab.binding.tapocontrol.internal.api.camera;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Signals an error reported by or during communication with the camera API.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class TapoCameraApiException extends Exception {
    private static final long serialVersionUID = 1L;
    private final int errorCode;

    public TapoCameraApiException(String message, int errorCode) {
        super(message + " (error_code " + errorCode + ")");
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
