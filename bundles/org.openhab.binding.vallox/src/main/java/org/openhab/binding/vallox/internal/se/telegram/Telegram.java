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

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.vallox.internal.se.cache.ValloxExpiringCacheMap;
import org.openhab.binding.vallox.internal.se.constants.ValloxSEConstants;
import org.openhab.binding.vallox.internal.se.mapper.ChannelMapper;
import org.openhab.binding.vallox.internal.se.mapper.MultipleValueChannel;
import org.openhab.binding.vallox.internal.se.mapper.TemperatureChannel;
import org.openhab.binding.vallox.internal.se.mapper.ValloxChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Telegram} class holds telegram state, data and parse methods.
 *
 * @author Hauke Fuhrmann - Initial contribution
 * @author Miika Jukka - Rewrite
 */
@NonNullByDefault
public class Telegram {

    public enum TelegramState {

        // States for received telegram
        EMPTY("Empty telegram"),
        NOT_FOR_US("Telegram not for us"),
        OK("Telegram received OK"),
        ACK("ACK byte received"),
        CRC_ERROR("CRC checksum failed"),
        CORRUPTED("Telegram is corrupted"),
        NOT_DOMAIN("First byte is not domain byte"),
        SUSPEND("Stop all traffic"),
        RESUME("Resume normal use"),

        // States for telegrams to send
        POLL("Poll"),
        COMMAND("Command");

        public final String stateDetails;

        private TelegramState(String stateDetails) {
            this.stateDetails = stateDetails;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(Telegram.class);
    private static Map<String, State> channelsToUpdate = new HashMap<>();
    public TelegramState state;
    public byte[] bytes = new byte[6];

    /**
     * Create new Telegram
     */
    public Telegram(TelegramState state) {
        this.state = state;
    }

    /**
     * Create new Telegram with single byte. For Acknowledged telegrams.
     *
     * @param state the internal state of the telegram
     * @param singleByte the single byte
     */
    public Telegram(TelegramState state, byte singleByte) {
        this.state = state;
        this.bytes[0] = singleByte;
    }

    /**
     * Create new Telegram.
     *
     * @param state the internal state of the telegram
     * @param telegram the telegram as byte array
     */
    public Telegram(TelegramState state, byte[] telegram) {
        this.state = state;
        this.bytes = telegram;
    }

    /**
     * Get sender of this telegram
     *
     * @return sender
     */
    public Byte getSender() {
        return bytes[1];
    }

    /**
     * Get receiver of this telegram
     *
     * @return receiver
     */
    public Byte getReceiver() {
        return bytes[2];
    }

    /**
     * Get variable of this telegram
     *
     * @return variable
     */
    public Byte getVariable() {
        return bytes[3];
    }

    /**
     * Get value of this telegram
     *
     * @return value
     */
    public Byte getValue() {
        return bytes[4];
    }

    /**
     * Get checksum of this telegram
     *
     * @return checksum
     */
    public Byte getCheksum() {
        return bytes[5];
    }

    /**
     * Get state details as string
     */
    public String stateDetails() {
        return state.stateDetails;
    }

    /**
     * Get telegram as string.
     */
    @Override
    public String toString() {
        return HexUtils.bytesToHex(bytes, "-");
    }

    /**
     * Process telegram and return a map of channels to update
     */
    public Map<String, State> parse(String channelID, ValloxExpiringCacheMap cache) {
        channelsToUpdate.clear();
        ValloxChannel valloxChannel = ChannelMapper.getValloxChannel(channelID);
        if (valloxChannel instanceof MultipleValueChannel) {
            Collection<String> subchannels = valloxChannel.getSubChannels();
            for (String channel : subchannels) {
                ValloxChannel vc = ChannelMapper.getValloxChannel(channel);
                State state = vc.convertToState(getValue());
                channelsToUpdate.put(channel, state);
            }
        } else if (valloxChannel instanceof TemperatureChannel) {
            State state = valloxChannel.convertToState(getValue());
            channelsToUpdate.put(channelID, state);
            calculateEfficiencies(cache);
        } else if (channelID.contains("status#co2")) {
            calculateCO2(cache);
        } else if (channelID.contains("setting#co2SetPoint")) {
            calculateCO2SetPoint(cache);
        } else {
            State state = valloxChannel.convertToState(getValue());
            channelsToUpdate.put(channelID, state);
        }
        logger.debug("Channels parsed from telegram {}", channelsToUpdate);
        return channelsToUpdate;
    }

    /**
     * Calculate efficiencies from measured temperatures. This is specified by Vallox.
     *
     * @param cache the cache where temperatures are fetched
     */
    public void calculateEfficiencies(ValloxExpiringCacheMap cache) {
        try {
            if (!cache.containsTemperatures()) {
                return;
            }
            int tempInside = ValloxSEConstants.TEMPERATURE_MAPPING[Byte.toUnsignedInt(cache.getValue((byte) 0x34))];
            int tempOutside = ValloxSEConstants.TEMPERATURE_MAPPING[Byte.toUnsignedInt(cache.getValue((byte) 0x32))];
            int tempExhaust = ValloxSEConstants.TEMPERATURE_MAPPING[Byte.toUnsignedInt(cache.getValue((byte) 0x33))];
            int tempIncoming = ValloxSEConstants.TEMPERATURE_MAPPING[Byte.toUnsignedInt(cache.getValue((byte) 0x35))];
            int maxPossible = tempInside - tempOutside;
            if (maxPossible <= 0) {
                channelsToUpdate.put("efficiency#inEfficiency", new DecimalType(100));
                channelsToUpdate.put("efficiency#outEfficiency", new DecimalType(100));
                channelsToUpdate.put("efficiency#averageEfficiency", new DecimalType(100));
            }
            if (maxPossible > 0) {
                int inEfficiency = (tempIncoming - tempOutside) * 100 / maxPossible;
                channelsToUpdate.put("efficiency#inEfficiency", new DecimalType(inEfficiency));
                int outEfficiency = (tempInside - tempExhaust) * 100 / maxPossible;
                channelsToUpdate.put("efficiency#outEfficiency", new DecimalType(outEfficiency));
                int averageEfficiency = (inEfficiency + outEfficiency) / 2;
                channelsToUpdate.put("efficiency#averageEfficiency", new DecimalType(averageEfficiency));
            }
        } catch (Exception e) {
            logger.debug("Exception caught while calculating efficiencies", e);
        }
    }

    /**
     * Calculate measure CO2 value from 2 bytes. Both needs to be in cache.
     *
     * @param cache the cache where values are fetched.
     */
    private void calculateCO2(ValloxExpiringCacheMap cache) {
        try {
            if (!cache.containsCO2()) {
                logger.debug("Skipping CO2 calculation. Not enough values in cache.");
                channelsToUpdate.put("status#co2", UnDefType.UNDEF);
                return;
            }
            byte co2High = cache.getValue((byte) 0x2B);
            byte co2Low = cache.getValue((byte) 0x2C);
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[] { co2High, co2Low });
            channelsToUpdate.put("status#co2", new DecimalType(byteBuffer.getShort()));
        } catch (Exception e) {
            logger.debug("Exception caught while calculating co2 {}", e.getMessage());
            channelsToUpdate.put("status#co2", UnDefType.UNDEF);
        }
    }

    /**
     * Calculate CO2 set point value from 2 bytes. Both needs to be in cache.
     *
     * @param cache the cache where values are fetched.
     */
    private void calculateCO2SetPoint(ValloxExpiringCacheMap cache) {
        try {
            if (!cache.containsCO2SetPoint()) {
                logger.debug("Skipping CO2 set point calculation. Not enough values in cache.");
                channelsToUpdate.put("status#co2SetPoint", UnDefType.UNDEF);
                return;
            }
            byte high = cache.getValue((byte) 0xB3);
            byte low = cache.getValue((byte) 0xB4);
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[] { high, low });
            channelsToUpdate.put("setting#co2SetPoint", new DecimalType(byteBuffer.getShort()));
        } catch (Exception e) {
            logger.debug("Exception caught while calculating co2 set point {}", e.getMessage());
            channelsToUpdate.put("status#co2SetPoint", UnDefType.UNDEF);
        }
    }
}
