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
package org.openhab.binding.jellyfin.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.events.ErrorEvent;
import org.openhab.binding.jellyfin.internal.events.ErrorEventBus;

/**
 * Contextual exception handler that categorizes exceptions and publishes events
 * 
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class ContextualExceptionHandler implements org.openhab.binding.jellyfin.internal.types.ExceptionHandlerType {

    private final ErrorEventBus eventBus;
    private final String context;

    public ContextualExceptionHandler(ErrorEventBus eventBus, String context) {
        this.eventBus = eventBus;
        this.context = context;
    }

    @Override
    public void handle(Exception exception) {
        // Still do the basic exception handling
        exception.printStackTrace();

        // Determine error type and severity based on exception type
        ErrorEvent.ErrorType errorType = determineErrorType(exception);
        ErrorEvent.ErrorSeverity severity = determineErrorSeverity(exception);

        // Create and publish error event
        ErrorEvent event = new ErrorEvent(exception, errorType, severity, context);
        eventBus.publishEvent(event);
    }

    private ErrorEvent.ErrorType determineErrorType(Exception exception) {
        // Strategy pattern for determining error type
        String exceptionName = exception.getClass().getSimpleName();

        if (exceptionName.contains("Connection") || exceptionName.contains("Network")) {
            return ErrorEvent.ErrorType.CONNECTION_ERROR;
        }
        if (exceptionName.contains("Auth") || exceptionName.contains("Unauthorized")) {
            return ErrorEvent.ErrorType.AUTHENTICATION_ERROR;
        }
        if (exceptionName.contains("URI") || exceptionName.contains("Config")) {
            return ErrorEvent.ErrorType.CONFIGURATION_ERROR;
        }
        if (exceptionName.contains("Api") || exceptionName.contains("Http")) {
            return ErrorEvent.ErrorType.API_ERROR;
        }

        return ErrorEvent.ErrorType.UNKNOWN_ERROR;
    }

    private ErrorEvent.ErrorSeverity determineErrorSeverity(Exception exception) {
        // Strategy pattern for determining severity
        String exceptionName = exception.getClass().getSimpleName();

        if (exceptionName.contains("Fatal") || exceptionName.contains("OutOfMemory")) {
            return ErrorEvent.ErrorSeverity.FATAL;
        }
        if (exceptionName.contains("Timeout") || exceptionName.contains("Connection")) {
            return ErrorEvent.ErrorSeverity.RECOVERABLE;
        }

        return ErrorEvent.ErrorSeverity.WARNING;
    }
}
