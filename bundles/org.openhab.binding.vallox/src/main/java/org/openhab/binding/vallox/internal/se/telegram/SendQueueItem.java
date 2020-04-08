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
package org.openhab.binding.vallox.internal.se.telegram;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vallox.internal.se.telegram.Telegram.TelegramState;

/**
 * Key value pair implementation for send queue.
 * Set retry count depending on telegrams state.
 *
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public class SendQueueItem {

    private final Telegram telegram;
    private int retryCount;

    /**
     * Create new instance.
     *
     * @param telegram the telegram
     */
    public SendQueueItem(Telegram telegram) {
        this.telegram = telegram;
        if (telegram.state == TelegramState.COMMAND) {
            this.retryCount = 5;
        } else {
            this.retryCount = 1;
        }
    }

    /**
     * Get telegram of this item
     *
     * @return telegram the telegram
     */
    public Telegram getTelegram() {
        return telegram;
    }

    /**
     * Get retry count of this item
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Check if should retry sending
     *
     * @return true if retry counter hasn't reached zero
     */
    public boolean retry() {
        if (retryCount < 1) {
            return false;
        }
        retryCount--;
        return true;
    }

    @Override
    public String toString() {
        return "Telegram = " + telegram.toString() + " Retry count = " + retryCount;
    }
}
