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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mielecloud.internal.webservice.ConnectionError;

/**
 * {@link RuntimeException} thrown if the Miele service is not available or unable to handle requests.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class MieleWebserviceException extends RuntimeException {

    private static final long serialVersionUID = 6268725866086530042L;

    private final ConnectionError connectionError;

    public MieleWebserviceException(final String message, final ConnectionError connectionError) {
        super(message);
        this.connectionError = connectionError;
    }

    public MieleWebserviceException(final String message, @Nullable final Throwable cause,
            final ConnectionError connectionError) {
        super(message, cause);
        this.connectionError = connectionError;
    }

    public ConnectionError getConnectionError() {
        return connectionError;
    }
}
