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
package org.openhab.binding.fmiweather.internal.client.exception;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Specialized Exception class for ExceptionReport responses from the FMI API
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class FMIExceptionReportException extends FMIResponseException {

    private static final long serialVersionUID = -6402617339310828118L;

    private FMIExceptionReportException(String message) {
        super(message);
    }

    public FMIExceptionReportException(String exceptionCode, String[] messages) {
        this(new StringBuilder("Exception report (").append(exceptionCode).append("): ")
                .append(Arrays.deepToString(messages)).toString());
    }
}
