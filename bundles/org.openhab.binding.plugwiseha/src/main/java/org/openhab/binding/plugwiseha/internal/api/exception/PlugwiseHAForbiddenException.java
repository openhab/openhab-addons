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
package org.openhab.binding.plugwiseha.internal.api.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PlugwiseHAForbiddenException} signals the controller denied a request due to invalid credentials.
 *
 * @author Bas van Wetten - Initial contribution
 * @author Leo Siepel - finish initial contribution
 * 
 */

@NonNullByDefault
public class PlugwiseHAForbiddenException extends PlugwiseHAException {

    private static final long serialVersionUID = 1L;

    public PlugwiseHAForbiddenException(String message) {
        super(message);
    }

    public PlugwiseHAForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlugwiseHAForbiddenException(Throwable cause) {
        super(cause);
    }
}
