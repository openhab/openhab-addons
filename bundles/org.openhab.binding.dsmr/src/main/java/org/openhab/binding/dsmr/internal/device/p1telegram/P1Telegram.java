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

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

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
        DATA_CORRUPTION("Received P1 telegram is corrupted"),
        /**
         * P1TelegramListener. The smarty telegram was successful received but could not be decoded because of an
         * invalid
         * encryption key.
         */
        INVALID_ENCRYPTION_KEY("Failed to decrypt P1 telegram due to invalid encryption key");

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
    private final List<Entry<String, String>> unknownCosemObjects;

    public P1Telegram(List<CosemObject> cosemObjects, TelegramState telegramState) {
        this(cosemObjects, telegramState, "", Collections.emptyList());
    }

    public P1Telegram(List<CosemObject> cosemObjects, TelegramState telegramState, String rawTelegram,
            List<Entry<String, String>> unknownCosemObjects) {
        this.cosemObjects = cosemObjects;
        this.telegramState = telegramState;
        this.rawTelegram = rawTelegram;
        this.unknownCosemObjects = unknownCosemObjects;
    }

    /**
     * @return The list of CosemObjects
     */
    public List<CosemObject> getCosemObjects() {
        return cosemObjects;
    }

    /**
     * @return The raw telegram data.
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

    /**
     * @return The list of CosemObject found in the telegram but not known to the binding
     */
    public List<Entry<String, String>> getUnknownCosemObjects() {
        return unknownCosemObjects;
    }
}
