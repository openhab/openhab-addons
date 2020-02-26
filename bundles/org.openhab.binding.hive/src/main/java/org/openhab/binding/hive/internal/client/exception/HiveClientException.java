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
 * Thrown to indicate that something has gone wrong with the Hive Client
 * library.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public abstract class HiveClientException extends HiveException {
    private static final long serialVersionUID = 1L;

    public HiveClientException() {
        super();
    }

    public HiveClientException(final String message) {
        super(message);
    }

    public HiveClientException(final Throwable cause) {
        super(cause);
    }

    public HiveClientException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
