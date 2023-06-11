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
package org.openhab.binding.millheat.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.millheat.internal.dto.AbstractRequest;
import org.openhab.binding.millheat.internal.dto.AbstractResponse;

/**
 * The {@link MillheatCommunicationException} class wraps exceptions raised when communicating with the API
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class MillheatCommunicationException extends Exception {
    private static final long serialVersionUID = 1L;
    private int errorCode = 0;

    public MillheatCommunicationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MillheatCommunicationException(final String message) {
        super(message);
    }

    public MillheatCommunicationException(final AbstractRequest request, final AbstractResponse response) {
        super("Server responded with error to request " + request.getClass().getSimpleName() + "/"
                + request.getRequestUrl() + ": " + response.errorCode + "/" + response.errorName + "/"
                + response.errorDescription);
        this.errorCode = response.errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
