/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device.p1telegram;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;

/**
 * Data class containing a Telegram with CosemObjects and TelegramState and if in lenient mode also the raw telegram
 * data.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class P1Telegram {

    /**
     * The TelegramState described the meta data of the P1Telegram
     */
    public enum TelegramState {
    /**
     * OK. Telegram was successful received and CRC16 checksum is verified (CRC16 only for DSMR V4 and up)
     */
    OK("P1 telegram received OK"),
    /**
     * CRC_ERROR. CRC16 checksum failed (only DSMR V4 and up)
     */
    CRC_ERROR("CRC checksum failed for received P1 telegram"),
    /**
     * DATA_CORRUPTION. The P1 telegram has syntax errors.
     */
    DATA_CORRUPTION("Received P1 telegram is corrupted");

        /**
         * public accessible state details
         */
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

    private final List<CosemObject> cosemObjects;
    private final TelegramState telegramState;
    private final String rawTelegram;

    public P1Telegram(List<CosemObject> cosemObjects, TelegramState telegramState, String rawTelegram) {
        this.cosemObjects = cosemObjects;
        this.telegramState = telegramState;
        this.rawTelegram = rawTelegram;
    }

    /**
     * @return The list of CosemObjects
     */
    public List<CosemObject> getCosemObjects() {
        return cosemObjects;
    }

    /**
     * @return The raw telegram data
     */
    public String getRawTelegram() {
        return rawTelegram;
    }

    /**
     * @return The state of the telegram
     */
    public TelegramState getTelegramState() {
        return telegramState;
    }
}
