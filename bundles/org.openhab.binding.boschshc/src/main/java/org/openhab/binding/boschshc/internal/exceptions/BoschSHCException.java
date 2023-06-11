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
package org.openhab.binding.boschshc.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception class for Bosch Smart Home controller errors.
 *
 * @author Gerd Zanker - Initial contribution
 */
@SuppressWarnings("serial")
@NonNullByDefault
public class BoschSHCException extends Exception {
    public BoschSHCException() {
    }

    public BoschSHCException(String message) {
        super(message);
    }

    public BoschSHCException(String message, Throwable e) {
        super(message, e);
    }
}
