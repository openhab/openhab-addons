/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.danfossairunit.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An exception representing an unexpected value received from the air unit.
 *
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
public class UnexpectedResponseValueException extends Exception {

    private static final long serialVersionUID = -5727747058755880978L;

    public UnexpectedResponseValueException(String message) {
        super(message);
    }

    public UnexpectedResponseValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
