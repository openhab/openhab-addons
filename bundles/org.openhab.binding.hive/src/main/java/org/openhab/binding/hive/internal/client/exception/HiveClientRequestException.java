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
 * Thrown to indicate a HTTP request to the Hive API has failed before it
 * could be completed.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class HiveClientRequestException extends HiveClientException {
    private static final long serialVersionUID = 1L;

    public HiveClientRequestException() {
        super();
    }

    public HiveClientRequestException(final String message) {
        super(message);
    }

    public HiveClientRequestException(final Throwable cause) {
        super(cause);
    }

    public HiveClientRequestException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
