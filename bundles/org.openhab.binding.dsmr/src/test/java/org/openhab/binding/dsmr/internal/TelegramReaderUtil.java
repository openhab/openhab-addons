/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram.TelegramState;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramParser;

/**
 * Util class to read test input telegrams.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
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
    public static byte[] readRawTelegram(String telegramName) {
        try (InputStream is = TelegramReaderUtil.class.getResourceAsStream(telegramName + TELEGRAM_EXT)) {
            return IOUtils.toByteArray(is);
        } catch (IOException e) {
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
    public static P1Telegram readTelegram(String telegramName, TelegramState expectedTelegramState) {
        AtomicReference<P1Telegram> p1Telegram = new AtomicReference<>(null);
        byte[] telegram = readRawTelegram(telegramName);
        P1TelegramParser parser = new P1TelegramParser(p1Telegram::set);

        parser.setLenientMode(true);
        parser.parse(telegram, telegram.length);
        assertNotNull("Telegram state should have been set. (Missing newline at end of message?)", p1Telegram.get());
        assertEquals("Expected TelegramState should be as expected", expectedTelegramState,
                p1Telegram.get().getTelegramState());
        return p1Telegram.get();
    }
}
