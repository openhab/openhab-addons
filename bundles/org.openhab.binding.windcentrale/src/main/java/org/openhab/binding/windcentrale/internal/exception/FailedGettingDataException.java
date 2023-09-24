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
package org.openhab.binding.windcentrale.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An error occurred while retrieving data from the Windcentrale API.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class FailedGettingDataException extends Exception {

    private static final long serialVersionUID = 4494062464212681327L;

    public FailedGettingDataException(String message) {
        super(message);
    }

    public FailedGettingDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedGettingDataException(Throwable cause) {
        super(cause);
    }
}
