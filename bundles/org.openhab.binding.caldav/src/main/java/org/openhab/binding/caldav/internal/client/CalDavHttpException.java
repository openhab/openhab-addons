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
package org.openhab.binding.caldav.internal.client;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;

/**
 * HTTP failure without sensitive response bodies or URLs.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Structured synchronization failures
 * @author Andreas Vilippus - Safe operation diagnostics and transport causes
 */
@NonNullByDefault
public class CalDavHttpException extends IOException {
    private static final long serialVersionUID = 1L;
    private final int statusCode;
    private final String operation;
    private final boolean invalidSyncToken;
    private final boolean unsupportedReport;

    public CalDavHttpException(String operation, int statusCode) {
        this(operation, statusCode, false);
    }

    public CalDavHttpException(String operation, int statusCode, boolean invalidSyncToken) {
        this(operation, statusCode, invalidSyncToken, false);
    }

    public CalDavHttpException(String operation, int statusCode, boolean invalidSyncToken, boolean unsupportedReport) {
        this(operation, statusCode, invalidSyncToken, unsupportedReport, null);
    }

    CalDavHttpException(String operation, int statusCode, boolean invalidSyncToken, boolean unsupportedReport,
            @Nullable Throwable cause) {
        super("CalDAV " + operation + " failed with HTTP status " + statusCode, cause);
        this.statusCode = statusCode;
        this.operation = operation;
        this.invalidSyncToken = invalidSyncToken;
        this.unsupportedReport = unsupportedReport;
    }

    public int statusCode() {
        return statusCode;
    }

    /**
     * Returns the HTTP method used for the failed CalDAV request.
     */
    public String operation() {
        return operation;
    }

    public boolean invalidSyncToken() {
        return invalidSyncToken;
    }

    public boolean unsupportedReport() {
        return unsupportedReport || statusCode == HttpStatus.METHOD_NOT_ALLOWED_405
                || statusCode == HttpStatus.NOT_IMPLEMENTED_501;
    }
}
