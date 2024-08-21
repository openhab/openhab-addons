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
package org.openhab.binding.ojelectronics.internal.common;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.LoggerFactory;

import com.github.signalr4j.client.LogLevel;
import com.github.signalr4j.client.Logger;

/**
 * Logs SignalR information
 *
 * @author Christian Kittel - Initial Contribution
 */
@NonNullByDefault
public class SignalRLogger implements Logger {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(SignalRLogger.class);

    @Override
    public void log(@Nullable String message, @Nullable LogLevel level) {
        if (message == null || level == null) {
            return;
        }
        switch (level) {
            case Critical:
                logger.warn("Critical SignalR Message: {}", message);
                break;
            case Information:
                logger.info("SignalR information message: {}", message);
                break;
            case Verbose:
            default:
                logger.trace("SignalR information message: {}", message);
                break;
        }
    }
}
