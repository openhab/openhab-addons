/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

/**
 * Interface for receiving CosemObjects that come from a P1 Telegram
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored interface to use single data object
 */
@NonNullByDefault
public interface P1TelegramListener {

    /**
     * Callback on received telegram.
     *
     * @param telegram The received telegram
     */
    public void telegramReceived(P1Telegram telegram);
}
