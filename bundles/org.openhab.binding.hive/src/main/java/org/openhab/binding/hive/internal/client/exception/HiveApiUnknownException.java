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
 * Thrown to indicate something of an unknown nature has gone wrong while
 * making a call to the Hive API (and it is the server's fault).
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class HiveApiUnknownException extends HiveApiException {
    private static final long serialVersionUID = 1L;

    public HiveApiUnknownException() {
        super();
    }

    public HiveApiUnknownException(final String message) {
        super(message);
    }

    public HiveApiUnknownException(final Throwable cause) {
        super(cause);
    }

    public HiveApiUnknownException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
