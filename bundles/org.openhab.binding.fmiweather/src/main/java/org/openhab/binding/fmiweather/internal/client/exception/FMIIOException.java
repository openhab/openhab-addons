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
package org.openhab.binding.fmiweather.internal.client.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Specialized Exception class for I/O errors related to the FMI API, such as invalid HTTP timeout
 *
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class FMIIOException extends FMIResponseException {
    private static final long serialVersionUID = 4835819504565701063L;

    public FMIIOException(String message) {
        super(message);
    }

    public FMIIOException(Exception cause) {
        super(cause);
    }
}
