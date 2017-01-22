package org.openhab.binding.dsmr.device.p1telegram;

import java.util.List;

import org.openhab.binding.dsmr.device.cosem.CosemObject;

/**
 * Interface for receiving CosemObjects that come from a P1 Telegram
 *
 * @author M. Volaart
 * @since 2.0.0
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
     * @since 2.0.0
     */
    public enum TelegramState {
        OK,
        CRC_ERROR,
        DATA_CORRUPTION
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
