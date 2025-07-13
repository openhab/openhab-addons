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
package org.openhab.binding.huesync.internal.exceptions;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * @author Patrik Gfeller - Initial contribution
 * @author Patrik Gfeller - Issue #18376, Fix/improve log message and exception handling
 */
@NonNullByDefault
public class HueSyncConnectionException extends HueSyncException {
    private static final long serialVersionUID = 0L;
    private @Nullable Exception innerException = null;

    public HueSyncConnectionException(String message, Exception exception) {
        super(message);
        this.innerException = exception;
    }

    public HueSyncConnectionException(String message) {
        super(message);
    }

    public @Nullable Exception getInnerException() {
        return this.innerException;
    }

    @Override
    public @Nullable String getLocalizedMessage() {
        var innerMessage = Optional.ofNullable(this.innerException.getLocalizedMessage());
        var message = super.getLocalizedMessage();

        if (innerMessage.isPresent()) {
            message = message + " (" + innerMessage.get() + ")";
        }

        return message;
    }
}
