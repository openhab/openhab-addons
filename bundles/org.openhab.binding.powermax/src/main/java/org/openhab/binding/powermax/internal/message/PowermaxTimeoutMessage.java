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
package org.openhab.binding.powermax.internal.message;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.powermax.internal.state.PowermaxState;

/**
 * A class for TIMEOUT message handling
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PowermaxTimeoutMessage extends PowermaxBaseMessage {

    /**
     * Constructor
     *
     * @param message
     *            the received message as a buffer of bytes
     */
    public PowermaxTimeoutMessage(byte[] message) {
        super(message);
    }

    @Override
    protected @Nullable PowermaxState handleMessageInternal(@Nullable PowermaxCommManager commManager) {
        if (commManager != null) {
            commManager.sendMessage(PowermaxSendType.EXIT);
        }

        return null;
    }
}
