/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown to indicate that the Hive client got a "success" response from the
 * Hive API but either does not understand the response or thinks it is
 * malformed.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class HiveClientResponseException extends HiveClientException {
    private static final long serialVersionUID = 1L;

    public HiveClientResponseException() {
        super();
    }

    public HiveClientResponseException(final String message) {
        super(message);
    }

    public HiveClientResponseException(final Throwable cause) {
        super(cause);
    }

    public HiveClientResponseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
