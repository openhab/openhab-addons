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
package org.openhab.binding.nest.internal.sdm.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An error occurred while sending data to the SDM REST API.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class FailedSendingSDMDataException extends Exception {

    private static final long serialVersionUID = 5377279669017810297L;

    public FailedSendingSDMDataException(String message) {
        super(message);
    }

    public FailedSendingSDMDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedSendingSDMDataException(Throwable cause) {
        super(cause);
    }
}
