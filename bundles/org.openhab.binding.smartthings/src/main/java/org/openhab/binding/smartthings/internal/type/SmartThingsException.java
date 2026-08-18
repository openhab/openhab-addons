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
package org.openhab.binding.smartthings.internal.type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.dto.ErrorObject;

/**
 *
 * @author Laurent Arnal - Initial contribution
 *
 *         An exception that occurred while operating the binding
 *
 */
@NonNullByDefault
public class SmartThingsException extends Exception {
    private static final long serialVersionUID = -3398100220952729816L;
    @Nullable
    private ErrorObject err;
    private final boolean communicationError;

    public SmartThingsException(String message, Exception e) {
        super(message, e);
        communicationError = e instanceof SmartThingsException smartThingsException
                && smartThingsException.isCommunicationError();
    }

    public SmartThingsException(String message, Exception e, boolean communicationError) {
        super(message, e);
        this.communicationError = communicationError || e instanceof SmartThingsException smartThingsException
                && smartThingsException.isCommunicationError();
    }

    public SmartThingsException(String message) {
        super(message);
        communicationError = false;
    }

    public SmartThingsException(String message, ErrorObject err) {
        super(message);
        this.err = err;
        communicationError = false;
    }

    @Override
    public @Nullable String getMessage() {
        String msg = super.getMessage();
        StringBuilder message = new StringBuilder(msg == null ? "" : msg);
        ErrorObject errL = err;
        if (errL != null) {
            message.append("\r\n");
            message.append("requestId : ").append(errL.requestId).append("\r\n");

            if (errL.error != null) {
                message.append("code      : ").append(errL.error.code).append("\r\n");
                message.append("message   : ").append(errL.error.message).append("\r\n");

                if (errL.error.details != null) {
                    for (ErrorObject.Error.Detail detail : errL.error.details) {
                        message.append("code      : ").append(detail.code()).append("\r\n");
                        message.append("target    : ").append(detail.target()).append("\r\n");
                        message.append("message   : ").append(detail.message()).append("\r\n");
                    }
                }
            }
        }

        Throwable cause = getCause();
        if (cause != null) {
            String causeMessage = cause.getMessage();
            if (causeMessage != null && !causeMessage.isBlank() && !causeMessage.equals(msg)) {
                message.append("\r\ncause     : ").append(causeMessage);
            }
        }

        return message.toString();
    }

    public boolean isCommunicationError() {
        return communicationError;
    }

    public static String getRootCauseMessage(Throwable t) {
        Throwable root = t;
        while (root != null && root.getCause() != null) {
            root = root.getCause();
        }
        return root.getClass().getSimpleName() + (root.getMessage() != null ? ": " + root.getMessage() : "");
    }
}
