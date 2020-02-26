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
 * Thrown to indicate that the Hive Client got a non-success response from the
 * Hive API but does not understand why (and it is the client's fault).
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class HiveClientUnknownException extends HiveClientException {
    private static final long serialVersionUID = 1L;

    public HiveClientUnknownException() {
        super();
    }

    public HiveClientUnknownException(final String message) {
        super(message);
    }

    public HiveClientUnknownException(final Throwable cause) {
        super(cause);
    }

    public HiveClientUnknownException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
