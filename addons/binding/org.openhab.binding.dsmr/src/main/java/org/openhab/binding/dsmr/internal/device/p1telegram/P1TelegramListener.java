/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device.p1telegram;

import java.util.List;

import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;

/**
 * Interface for receiving CosemObjects that come from a P1 Telegram
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public interface P1TelegramListener {
    /**
     * The TelegramState described the meta data of the P1Telegram
     *
     * The following levels are supported:
     * - OK. Telegram was successful received and CRC16 checksum is verified (CRC16 only for DSMR V4 and up)
     * - CRC_ERROR. CRC16 checksum failed (only DSMR V4 and up)
     * - DATA_CORRUPTION. The P1 telegram has syntax errors.
     *
     * @author M. Volaart
     * @since 2.1.0
     */
    public enum TelegramState {
        OK("P1 telegram received OK"),
        CRC_ERROR("CRC checksum failed for received P1 telegram"),
        DATA_CORRUPTION("Received P1 telegram is corrupted");

        // public accessible state details
        public final String stateDetails;

        /**
         * Constructs a new TelegramState enum
         *
         * @param stateDetails String containing the details of this TelegramState
         */
        private TelegramState(String stateDetails) {
            this.stateDetails = stateDetails;
        }
    }

    /**
     * Event listener when a telegram is received.
     *
     * This method received the available Cosem objects. The listener is also notified about the quality of the
     * received telegram.
     *
     * It is up to the listener how to handle the TelegramState
     *
     * The caller of this method should be aware that implementations can be time consuming and should
     * consider to call this method asynchronous.
     *
     * @param cosemObjects List of received CosemObjects within the P1 telegram
     * @param telegramState {@link TelegramState} containing meta data about the received telegram
     */
    public void telegramReceived(List<CosemObject> cosemObjects, TelegramState telegramState);
}
