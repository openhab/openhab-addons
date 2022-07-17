/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.argoclima.internal.exception;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The class {@code ArgoApiCommunicationException} is thrown in case of any issues with communication with the Argo HVAC
 * device (incl. indirect communication, via sniffing)
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class ArgoApiCommunicationException extends Exception {

    private static final long serialVersionUID = -6618438267962155601L;

    public ArgoApiCommunicationException(@Nullable String message) {
        super(message);
    }

    public ArgoApiCommunicationException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    @Override
    public @Nullable String getMessage() {
        var msg = super.getMessage();
        if (msg != null && this.getCause() != null) {
            var causeMessage = Objects.requireNonNull(this.getCause()).getMessage();
            if (causeMessage != null && !(msg.endsWith(causeMessage))) {
                // Sometimes the cause is already embedded in the message at throw site. If it isn't though... let's add
                // it
                msg += ". Caused by: " + causeMessage;
            }
        }
        return msg;
    }
}
