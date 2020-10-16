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
package org.openhab.io.transport.modbus;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Encapsulates result of modbus read operations
 *
 * @author Nagy Attila Gabor - Initial contribution
 */
@NonNullByDefault
public class AsyncModbusFailure<R> {
    private final R request;

    private final Exception cause;

    public AsyncModbusFailure(R request, Exception cause) {
        Objects.requireNonNull(request, "Request must not be null!");
        Objects.requireNonNull(cause, "Cause must not be null!");
        this.request = request;
        this.cause = cause;
    }

    /**
     * Get request matching this response
     *
     * @return request object
     */
    public R getRequest() {
        return request;
    }

    /**
     * Get cause of error
     *
     * @return exception representing error
     */
    public Exception getCause() {
        return cause;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("AsyncModbusReadResult(");
        builder.append("request = ");
        builder.append(request);
        builder.append(", error = ");
        builder.append(cause);
        builder.append(")");
        return builder.toString();
    }
}
