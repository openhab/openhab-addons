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
package org.openhab.binding.nest.internal.sdm.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An error occurred while sending data to the Pub/Sub REST API.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class FailedSendingPubSubDataException extends Exception {

    private static final long serialVersionUID = 8615651337708366903L;

    public FailedSendingPubSubDataException(String message) {
        super(message);
    }

    public FailedSendingPubSubDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedSendingPubSubDataException(Throwable cause) {
        super(cause);
    }
}
