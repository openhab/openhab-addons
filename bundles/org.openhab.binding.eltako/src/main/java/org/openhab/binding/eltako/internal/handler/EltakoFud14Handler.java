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
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Direkte Übergabe des Dimmwertes von 0-100%, FUNC=38, Command 2 (ähnlich EEP A5-38-08)
 *
 * ORG =        0x07
 * Data_byte3 = 0x02
 * Data_byte2 = Dimmwert in % von 0-100 dez.
 * Data_byte1 = Dimmgeschwindigkeit
 *              0x00 = die am Dimmer eingestellte Dimmgeschwindigkeit wird verwendet.
 *              0x01 = sehr schnelle Dimmspeed …. Bis …
 *              0xFF = sehr langsame Dimmspeed
 * Data_byte0 = DB0_Bit3 = LRN Button
 *              (0 = Lerntelegramm, 1 = Datentelegramm)
 *              DB0_Bit0 =  1: Dimmer an, 0: Dimmer aus.
 *              DB0_Bit2 =  1: Dimmwert blockieren
 *                          0: Dimmwert nicht blockiert
 *
 * Lerntelegramm DB3..DB0 muss so aussehen: 0xE0, 0x40, 0x0D, 0x80
 *                           nur FSUD-230V: 0x02, 0x00, 0x00, 0x00
 *
 * Datentelegramme DB3..DB0 müssen z.B. so aussehen:
 * 0x02, 0x32, 0x00, 0x09 (Dimmer an mit 50% und interner Dimmspeed)
 * 0x02, 0x64, 0x01, 0x09 (Dimmer an mit 100% und schnellster Dimmspeed)
 * 0x02, 0x14, 0xFF, 0x09 (Dimmer an mit 20% und langsamster Dimmspeed)
 * 0x02, 0x.., 0x.., 0x08 (Dimmer aus)
 */

/**
 * The {@link EltakoFud14Handler} is responsible for processing FUD14 commands.
 *
 * @author Martin Wenske - Initial contribution
 */
public class EltakoFud14Handler extends EltakoGenericHandler {

    /*
     * Logger instance to create log entries
     */
    private final Logger logger = LoggerFactory.getLogger(EltakoGenericHandler.class);

    /**
     * Default internal values for additional telegram options
     */
    private DecimalType speed = DecimalType.ZERO;
    private OnOffType blocking = OnOffType.OFF;

    public EltakoFud14Handler(Thing thing) {
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
            case CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType) {
                    PercentType brightness = (PercentType) command;
                    updateState(CHANNEL_BRIGHTNESS, brightness);
                    sendTelegram(bridgehandler, brightness);
                }
                if (command instanceof RefreshType) {
                    /*
                     * Since there is no way to request device state
                     * we have to rely on persistence service to restore
                     * the item state
                     * => Do nothing
                     * updateState(CHANNEL_BRIGHTNESS, brightness);
                     */
                }
                break;
            case CHANNEL_SPEED:
                if (command instanceof DecimalType) {
                    speed = (DecimalType) command;
                    updateState(CHANNEL_SPEED, speed);
                }
                if (command instanceof RefreshType) {
                    /*
                     * Since there is no way to request device state
                     * we have to rely on persistence service to restore
                     * the item state
                     * => Do nothing
                     * updateState(CHANNEL_SPEED, speed);
                     */
                }
                break;
            case CHANNEL_BLOCKING:
                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.OFF)) {
                        blocking = OnOffType.OFF;
                        updateState(CHANNEL_BLOCKING, blocking);
                    }
                }
                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.ON)) {
                        blocking = OnOffType.ON;
                        updateState(CHANNEL_BLOCKING, blocking);
                    }
                }
                if (command instanceof RefreshType) {
                    /*
                     * Since there is no way to request device state
                     * we have to rely on persistence service to restore
                     * the item state
                     * => Do nothing
                     * updateState(CHANNEL_BLOCKING, blocking);
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
    protected void sendTelegram(EltakoGenericBridgeHandler bridgehandler, PercentType brightness) {
        // Prepare channel values
        int value_brightness = brightness.intValue();
        int value_speed = speed.intValue();
        int value_power = 9;

        if (blocking.equals(OnOffType.ON)) {
            value_power += 4;
        }

        // Convert Device ID from int into 4 bytes
        int deviceId = Integer.parseInt(getThing().getConfiguration().get(GENERIC_DEVICE_ID).toString());
        int[] ID = new int[4];
        ID[0] = deviceId & 0xFF;
        ID[1] = (deviceId >> 8) & 0xFF;
        ID[2] = (deviceId >> 16) & 0xFF;
        ID[3] = 0x03;

        // Calculate CRC value
        int crc = (0x0B + 0x07 + 0x02 + value_brightness + value_speed + value_power + ID[3] + ID[2] + ID[1] + ID[0])
                % 256;

        // Prepare telegram
        int[] data = new int[] { 0xA5, 0x5A, 0x0B, 0x07, 0x02, value_brightness, value_speed, value_power, ID[3], ID[2],
                ID[1], ID[0], 0x00, crc };

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
            logger.trace("FUD14: Telegram Send: {}", strbuf);
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

        // Check for HEADER == 0x04 and ORG == 0x07 and COMMAND == 0x02
        if ((packet[2] >> 5 == 0x04) && (packet[3] == 0x07) && (packet[4] == 0x02)) {
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
                logger.trace("FUD14: Telegram Received: {}", strbuf);
                // ####################################################

                // Update channel state based on received data
                updateState(CHANNEL_BRIGHTNESS, PercentType.valueOf(String.valueOf(packet[5])));
            }
        }
    }
}
