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
package org.openhab.binding.gardena.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception if the AccountHandler is not available.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class AccountHandlerNotAvailableException extends Exception {

    private static final long serialVersionUID = -1895774551653276530L;

    public AccountHandlerNotAvailableException(String message) {
        super(message);
    }
}
