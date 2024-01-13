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
package org.openhab.binding.netatmo.internal.api;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.ServiceError;

/**
 * An exception that occurred while communicating with Netatmo server or related processes.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NetatmoException extends IOException {
    private static final long serialVersionUID = 1513549973502021727L;
    private ServiceError statusCode = ServiceError.UNKNOWN;

    public NetatmoException(String format, Object... args) {
        super(format.formatted(args));
    }

    public NetatmoException(Exception e, String format, Object... args) {
        super(format.formatted(args), e);
    }

    public NetatmoException(String message) {
        super(message);
    }

    public NetatmoException(ApiError error) {
        super(error.getMessage());
        this.statusCode = error.getCode();
    }

    public ServiceError getStatusCode() {
        return statusCode;
    }

    @Override
    public @Nullable String getMessage() {
        String message = super.getMessage();
        return message == null ? null
                : ServiceError.UNKNOWN.equals(statusCode) ? message
                        : "Rest call failed: statusCode=%s, message=%s".formatted(statusCode, message);
    }
}
