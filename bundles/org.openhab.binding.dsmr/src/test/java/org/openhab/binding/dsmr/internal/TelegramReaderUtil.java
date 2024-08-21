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
package org.openhab.binding.dsmr.internal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dsmr.internal.device.connector.DSMRErrorStatus;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramListener;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramParser;

/**
 * Util class to read test input telegrams.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public final class TelegramReaderUtil {
    private static final String TELEGRAM_EXT = ".telegram";

    private TelegramReaderUtil() {
        // Util class
    }

    /**
     * Reads the raw bytes of the telegram given the file relative to this package and returns the objects.
     *
     * @param telegramName name of the telegram file to read
     * @return The raw bytes of a telegram
     */
    public static byte[] readRawTelegram(final String telegramName) {
        try (InputStream is = TelegramReaderUtil.class.getResourceAsStream(telegramName + TELEGRAM_EXT)) {
            if (is == null) {
                fail("Could not find telegram file with name:" + telegramName + TELEGRAM_EXT);
            }
            return is.readAllBytes();
        } catch (final IOException e) {
            throw new AssertionError("IOException reading telegram data: ", e);
        }
    }

    /**
     * Reads a telegram given the file relative to this package and returns the objects.
     *
     * @param telegramName name of the telegram file to read
     * @param expectedTelegramState expected state of the telegram read
     * @return a P1Telegram object
     */
    public static P1Telegram readTelegram(final String telegramName) {
        final byte[] telegram = readRawTelegram(telegramName);
        final P1TelegramListenerImpl listener = new P1TelegramListenerImpl();
        final P1TelegramParser parser = new P1TelegramParser(listener, true);

        parser.setLenientMode(true);
        parser.parse(telegram, telegram.length);
        final P1Telegram p1Telegram = listener.telegram;

        assertNotNull(p1Telegram, "Telegram state should have been set. (Missing newline at end of message?)");
        assertNull(listener.state, "Expected TelegramState should not be set");
        return p1Telegram;
    }

    public static class P1TelegramListenerImpl implements P1TelegramListener {
        public @Nullable P1Telegram telegram;
        public @Nullable DSMRErrorStatus state;

        @Override
        public void telegramReceived(final P1Telegram telegram) {
            this.telegram = telegram;
        }

        @Override
        public void onError(final DSMRErrorStatus state, final String error) {
            this.state = state;
        }
    }
}
