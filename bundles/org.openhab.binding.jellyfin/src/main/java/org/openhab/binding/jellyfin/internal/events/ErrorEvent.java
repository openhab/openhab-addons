/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event representing an error that occurred in the system
 * 
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class ErrorEvent {

    public enum ErrorType {
        CONNECTION_ERROR,
        AUTHENTICATION_ERROR,
        API_ERROR,
        CONFIGURATION_ERROR,
        UNKNOWN_ERROR
    }

    public enum ErrorSeverity {
        WARNING, // Log but don't change state
        RECOVERABLE, // Set to error state but allow recovery
        FATAL // Set to error state and require restart
    }

    private final Exception exception;
    private final ErrorType type;
    private final ErrorSeverity severity;
    private final String context;

    public ErrorEvent(Exception exception, ErrorType type, ErrorSeverity severity, String context) {
        this.exception = exception;
        this.type = type;
        this.severity = severity;
        this.context = context;
    }

    public Exception getException() {
        return exception;
    }

    public ErrorType getType() {
        return type;
    }

    public ErrorSeverity getSeverity() {
        return severity;
    }

    public String getContext() {
        return context;
    }
}
