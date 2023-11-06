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
package org.openhab.binding.deutschebahn.internal.filter;

import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception for errors within the filter scanner.
 * 
 * @author Sönke Küper - initial contribution
 */
@NonNullByDefault
public final class FilterScannerException extends Exception {

    private static final long serialVersionUID = -7319023069454747511L;

    /**
     * Creates an exception with given position and message.
     */
    FilterScannerException(int position, String message) {
        super("Scanner failed at positon: " + position + ": " + message);
    }

    /**
     * Creates an exception with given position, message and cause.
     */
    FilterScannerException(int position, String message, PatternSyntaxException e) {
        super("Scanner failed at positon: " + position + ": " + message, e);
    }
}
