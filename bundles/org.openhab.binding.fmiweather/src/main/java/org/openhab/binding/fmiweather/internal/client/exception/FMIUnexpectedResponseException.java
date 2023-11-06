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
package org.openhab.binding.fmiweather.internal.client.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Specialized Exception class for unexpected responses from the FMI API, such as invalid XML or format.
 *
 * Different from FMIExceptionReportException which is reserved for explicit error responses from server.
 *
 * @see FMIExceptionReportException
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class FMIUnexpectedResponseException extends FMIResponseException {

    private static final long serialVersionUID = 5068780757336770041L;

    public FMIUnexpectedResponseException(String message) {
        super(message);
    }

    public FMIUnexpectedResponseException(Exception cause) {
        super(cause);
    }
}
