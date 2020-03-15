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
import org.openhab.binding.vallox.internal.se.ValloxSEConstants;
import org.openhab.binding.vallox.internal.se.telegram.Telegram.TelegramState;

/**
 * The {@link TelegramFactory} creates telegrams to send.
 *
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public class TelegramFactory {

    /**
     * Create poll telegram
     *
     * @param panelNumber the panel number
     * @param variable the channel to poll
     * @return the created telegram
     */
    public static Telegram createPoll(byte panelNumber, byte variable) {
        byte[] telegram = new byte[ValloxSEConstants.TELEGRAM_LENGTH];

        telegram[0] = ValloxSEConstants.DOMAIN;
        telegram[1] = panelNumber;
        telegram[2] = ValloxSEConstants.ADDRESS_MASTER;
        telegram[3] = ValloxSEConstants.POLL_BYTE;
        telegram[4] = variable;
        telegram[5] = calculateChecksum(telegram);

        return new Telegram(TelegramState.POLL, telegram);
    }

    /**
     * Create command telegram
     *
     * @param panelNumber the panel number
     * @param variable the channel to send command to
     * @param value the value to send to channel
     * @return the created telegram
     */
    public static Telegram createCommand(byte panelNumber, byte variable, byte value) {
        byte[] telegram = new byte[ValloxSEConstants.TELEGRAM_LENGTH];

        telegram[0] = ValloxSEConstants.DOMAIN;
        telegram[1] = panelNumber;
        telegram[2] = ValloxSEConstants.ADDRESS_MASTER;
        telegram[3] = variable;
        telegram[4] = value;
        telegram[5] = calculateChecksum(telegram);

        return new Telegram(TelegramState.COMMAND, telegram);
    }

    /**
     * Calculate checksum for telegram
     *
     * @param telegram the telegram to calculate checksum from
     * @return checksum the calculated checksum
     */
    static byte calculateChecksum(byte[] telegram) {
        int checksum = 0;
        for (byte i = 0; i < telegram.length - 1; i++) {
            checksum += telegram[i];
        }
        return (byte) (checksum % 256);
    }

    /**
     * Validate telegrams checksum
     *
     * @param telegram the telegram to validate
     * @param checksum the checksum to compare to
     * @return true if checksums match
     */
    public static boolean isChecksumValid(byte[] telegram, byte checksum) {
        return calculateChecksum(telegram) == checksum;
    }
}
