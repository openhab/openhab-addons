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
package org.openhab.binding.opensprinkler.internal.api.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MismatchApiException} exception is thrown when result from the OpenSprinkler
 * API is "result" : 3.
 *
 * @author Chris Graham - Initial contribution
 */
@NonNullByDefault
public class MismatchApiException extends GeneralApiException {
    /**
     * Serial ID of this error class.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Basic constructor allowing the storing of a single message.
     *
     * @param message Descriptive message about the error.
     */
    public MismatchApiException(String message) {
        super(message);
    }
}
