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
 * The {@link PlugwiseHAException} represents a binding specific {@link Exception}.
 *
 * @author Bas van Wetten - Initial contribution
 * @author Leo Siepel - finish initial contribution
 * 
 */
@NonNullByDefault
public class PlugwiseHAException extends Exception {

    private static final long serialVersionUID = 1L;

    public PlugwiseHAException(String message) {
        super(message);
    }

    public PlugwiseHAException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlugwiseHAException(Throwable cause) {
        super(cause);
    }
}
