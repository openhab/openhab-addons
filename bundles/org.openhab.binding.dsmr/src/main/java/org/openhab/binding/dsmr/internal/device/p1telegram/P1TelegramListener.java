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
package org.openhab.binding.dsmr.internal.device.p1telegram;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dsmr.internal.device.connector.DSMRErrorStatus;

/**
 * Interface for receiving CosemObjects that come from a P1 Telegram
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored interface to use single data object
 */
@NonNullByDefault
public interface P1TelegramListener {

    /**
     * Called when reading the telegram failed. Passes the failed state and optional an additional error message.
     *
     * @param errorStatus error state
     * @param message optional additional message
     */
    void onError(DSMRErrorStatus errorStatus, String message);

    /**
     * Callback on received telegram.
     *
     * @param telegram The received telegram
     */
    void telegramReceived(P1Telegram telegram);
}
