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
package org.openhab.binding.eltako.internal.handler;

import static org.openhab.binding.eltako.internal.misc.EltakoBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.eltako.internal.misc.CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Direktes Fahrkommando mit Angabe der Laufzeit in Sek. FUNC = 3F, Typ = 7F (universal). Für jeden Kanal separat.
 *
 * ORG        = 0x07
 * Data_byte3 = Laufzeit in 100ms MSB
 * Data_byte2 = Laufzeit in 100ms LSB, oder Laufzeit in Sekunden 1-255 dez., die Laufzeiteinstellung am Gerät wird ignoriert.
 * Data_byte1 = Kommando:
 *              0x00 = Stopp
 *              0x01 = Auf
 *              0x02 = Ab
 * Data_byte0 = DB0_Bit3 = LRN Button
 *              (0 = Lerntelegramm, 1 = Datentelegramm)
 *              DB0_Bit2 = Aktor für Taster blockieren/freigeben
 *              (0 = freigeben, 1 = blockieren)
 *              DB0_Bit1 = Umschaltung Laufzeit in Sekunden oder in 100ms.
 *              (0 = Laufzeit nur in DB2 in Sekunden)
 *              (1 = Laufzeit in DB3(MSB)+DB2(LSB) in 100ms.)
 *
 * Lerntelegramm DB3..DB0 muss so aussehen: 0xFF, 0xF8, 0x0D, 0x80
 *
 * Mit eingelernten Tastern kann jederzeit unterbrochen werden!
*/
/**
 * The {@link EltakoFsb14Handler} is responsible for processing device specific commands.
 *
 * @author Martin Wenske - Initial contribution
 */
public class EltakoFsb14Handler extends EltakoGenericHandler {

    /*
     * Logger instance to create log entries
     */
    private final Logger logger = LoggerFactory.getLogger(EltakoGenericHandler.class);

    private DecimalType time = DecimalType.ZERO;

    public EltakoFsb14Handler(Thing thing) {
        super(thing);
    }

    /**
     * Event handler is called in case a channel has received a command
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Log event to console
        logger.trace("Channel {} received command {} with class {}", channelUID, command, command.getClass());

        // Get bridge instance
        Thing bridge = getBridge();
        if (bridge == null) {
            return;
        }
        // Get bridge handler instance
        EltakoGenericBridgeHandler bridgehandler = (EltakoGenericBridgeHandler) bridge.getHandler();
        if (bridgehandler == null) {
            return;
        }

        // Handle received command
        switch (channelUID.getId()) {
            case CHANNEL_RUNTIME:
                if (command instanceof DecimalType) {
                    time = (DecimalType) command;
                    updateState(CHANNEL_RUNTIME, time);
                }
                if (command instanceof RefreshType) {
                    /*
                     * Since there is no way to request device state
                     * we have to rely on persistence service to restore
                     * the item state
                     * => Do nothing
                     * updateState(CHANNEL_TIME, time);
                     */
                }
                break;
            case CHANNEL_CONTROL:
                if (command instanceof UpDownType) {
                    if (command.equals(UpDownType.UP)) {
                        updateState(CHANNEL_CONTROL, PercentType.ZERO);
                        sendTelegram(bridgehandler, CommandType.UP);
                    }
                    if (command.equals(UpDownType.DOWN)) {
                        updateState(CHANNEL_CONTROL, PercentType.HUNDRED);
                        sendTelegram(bridgehandler, CommandType.DOWN);
                    }
                }
                if (command instanceof StopMoveType) {
                    if (command.equals(StopMoveType.STOP)) {
                        updateState(CHANNEL_CONTROL, PercentType.valueOf("50"));
                        sendTelegram(bridgehandler, CommandType.STOP);
                    }
                }
                if (command instanceof RefreshType) {
                    /*
                     * Since there is no way to request device state
                     * we have to rely on persistence service to restore
                     * the item state
                     * => Do nothing
                     * updateState(CHANNEL_CONTROL, CommandType.STOP);
                     */
                }
                break;
            default:
                // Log event to console
                logger.warn("Command {} is not supported by thing", command);
                break;
        }
    }

    /**
     * Prepares the data used for the telegram and sends it out
     */
    protected void sendTelegram(EltakoGenericBridgeHandler bridgehandler, CommandType state) {
        // Prepare channel values
        int value_time = time.intValue();
        int value_command;

        // Convert Device ID from int into 4 bytes
        int deviceId = Integer.parseInt(getThing().getConfiguration().get(GENERIC_DEVICE_ID).toString());
        int[] ID = new int[4];
        ID[0] = deviceId & 0xFF;
        ID[1] = (deviceId >> 8) & 0xFF;
        ID[2] = (deviceId >> 16) & 0xFF;
        ID[3] = 0x03;

        if (state == CommandType.UP) {
            value_command = 0x01;
        } else if (state == CommandType.DOWN) {
            value_command = 0x02;
        } else {
            value_command = 0x00;
        }

        // Calculate CRC value
        int crc = (0x0B + 0x07 + 0x00 + value_time + value_command + 0x08 + ID[3] + ID[2] + ID[1] + ID[0]) % 256;

        // Prepare telegram
        int[] data = new int[] { 0xA5, 0x5A, 0x0B, 0x07, 0x00, value_time, value_command, 0x08, ID[3], ID[2], ID[1],
                ID[0], 0x00, crc };

        // Get own state
        if (this.getThing().getStatus() == ThingStatus.ONLINE) {

            // ####################################################
            // Prepare data to be written to log
            StringBuffer strbuf = new StringBuffer();
            // Create string out of byte data
            for (int i = 0; i < 14; i++) {
                strbuf.append(String.format("%02X ", data[i]));
            }
            // Log event to console
            logger.trace("FSB14: Telegram Send: {}", strbuf);
            // ####################################################

            // Write data by calling bridge handler method
            bridgehandler.serialWrite(data, 14);
        }
    }

    /**
     * Called by Bridge when a new telegram has been received
     */
    @Override
    public void telegramReceived(int[] packet) {
        // Convert Device ID from int into 4 bytes
        int deviceId = Integer.parseInt(getThing().getConfiguration().get(GENERIC_DEVICE_ID).toString());

        // Check for HEADER == 0x04 and ORG == 0x07
        if ((packet[2] >> 5 == 0x04) && (packet[3] == 0x07)) {
            // Check for ID (ignore 4th byte)
            if ((packet[11] | (packet[10] << 8) | (packet[9] << 16)) == (deviceId)) {

                // ####################################################
                // Prepare data to be written to log
                StringBuffer strbuf = new StringBuffer();
                // Create string out of byte data
                for (int i = 0; i < 14; i++) {
                    strbuf.append(String.format("%02X ", packet[i]));
                }
                // Log event to console
                logger.trace("FSB14: Telegram Received: {}", strbuf);
                // ####################################################

                // Update channel state based on received data
                // updateState(CHANNEL_BRIGHTNESS, PercentType.valueOf(String.valueOf(packet[5])));
                // updateState(CHANNEL_POWER, OnOffType.valueOf(String.valueOf(packet[7] & 0x09)));
                // TODO: Calculate position based on runtime of rollershutter motor
            }
        }
    }
}