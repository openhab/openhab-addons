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
package org.openhab.binding.boschindego.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link IndegoTimeoutException} is thrown on gateway timeout, which
 * means that Bosch services cannot connect to the device.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class IndegoTimeoutException extends IndegoException {

    private static final long serialVersionUID = -7952585411438042139L;

    public IndegoTimeoutException(String message) {
        super(message);
    }

    public IndegoTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
