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
package org.openhab.binding.mielecloud.internal.webservice.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.webservice.ConnectionError;

/**
 * {@link RuntimeException} thrown if a transient error occurred which the binding can recover from by retrying.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class MieleWebserviceTransientException extends RuntimeException {
    private static final long serialVersionUID = -1863609233382694104L;

    private final ConnectionError connectionError;

    public MieleWebserviceTransientException(final String message, final ConnectionError connectionError) {
        super(message);
        this.connectionError = connectionError;
    }

    public MieleWebserviceTransientException(final String message, final Throwable cause,
            final ConnectionError connectionError) {
        super(message, cause);
        this.connectionError = connectionError;
    }

    public ConnectionError getConnectionError() {
        return connectionError;
    }
}
