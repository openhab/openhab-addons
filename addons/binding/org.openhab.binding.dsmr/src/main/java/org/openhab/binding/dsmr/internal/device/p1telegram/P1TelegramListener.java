/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
