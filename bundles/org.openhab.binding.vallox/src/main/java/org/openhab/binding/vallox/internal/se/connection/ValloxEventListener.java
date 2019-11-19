/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.vallox.internal.se.connection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.vallox.internal.se.telegram.Telegram;

/**
 * Define methods to receive events from Vallox.
 *
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public interface ValloxEventListener {

    /**
     * Receive telegram from Vallox.
     *
     * @param telegram received telegram
     */
    void telegramReceived(Telegram telegram);

    /**
     * Receive error from Vallox.
     *
     * @param error the error message
     * @param exception the exception or null
     */
    void errorOccurred(String error, @Nullable Exception exception);
}
