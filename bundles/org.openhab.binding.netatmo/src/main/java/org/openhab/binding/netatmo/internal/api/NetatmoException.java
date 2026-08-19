/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
 * @author Martin Littkovsky - Keep HTTP status and raw error code for unclassified errors
 */
@NonNullByDefault
public class NetatmoException extends IOException {
    private static final long serialVersionUID = 1513549973502021727L;
    private ServiceError statusCode = ServiceError.UNKNOWN;
    private int httpStatus = -1;
    private @Nullable String rawErrorCode;

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

    /**
     * Additionally keeps the HTTP status and raw error code, used by {@link #getMessage()} only when {@code error}
     * does not classify into a known {@link ServiceError}.
     */
    public NetatmoException(ApiError error, int httpStatus, @Nullable String rawErrorCode) {
        this(error);
        this.httpStatus = httpStatus;
        this.rawErrorCode = rawErrorCode;
    }

    public ServiceError getStatusCode() {
        return statusCode;
    }

    @Override
    public @Nullable String getMessage() {
        String message = super.getMessage();
        if (message == null) {
            return null;
        }
        if (!ServiceError.UNKNOWN.equals(statusCode)) {
            return "Rest call failed: statusCode=%s, message=%s".formatted(statusCode, message);
        }
        if (httpStatus <= 0) {
            return message;
        }
        String rawErrorCode = this.rawErrorCode;
        String suffix = "(HTTP %s%s)".formatted(Integer.toString(httpStatus),
                rawErrorCode == null ? "" : ", error code %s".formatted(rawErrorCode));
        return message.isEmpty() ? suffix : "%s %s".formatted(message, suffix);
    }
}
