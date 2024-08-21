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
package org.openhab.binding.plugwiseha.internal.api.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PlugwiseHAInvalidHostException} signals there was a problem with the hostname of the controller.
 *
 * @author Bas van Wetten - Initial contribution
 * @author Leo Siepel - finish initial contribution
 * 
 */

@NonNullByDefault
public class PlugwiseHAInvalidHostException extends PlugwiseHAException {

    private static final long serialVersionUID = 1L;

    public PlugwiseHAInvalidHostException(String message) {
        super(message);
    }

    public PlugwiseHAInvalidHostException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlugwiseHAInvalidHostException(Throwable cause) {
        super(cause);
    }
}
