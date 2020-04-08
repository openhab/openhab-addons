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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.vallox.internal.se.ValloxSEConstants;
import org.openhab.binding.vallox.internal.se.cache.ValloxExpiringCacheMap;
import org.openhab.binding.vallox.internal.se.mapper.ChannelDescriptor;
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
    private final static Map<String, State> channelsToUpdate = new HashMap<>();
    public final TelegramState state;
    public final byte[] bytes;

    /**
     * Create new Telegram
     */
    public Telegram(TelegramState state) {
        this(state, new byte[6]);
    }

    /**
     * Create new Telegram with single byte. For Acknowledged telegrams.
     *
     * @param state the internal state of the telegram
     * @param singleByte the single byte
     */
    public Telegram(TelegramState state, byte singleByte) {
        this(state, new byte[] { singleByte, 0x00, 0x00, 0x00, 0x00, 0x00 });
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
    public byte getSender() {
        return bytes[1];
    }

    /**
     * Get receiver of this telegram
     *
     * @return receiver
     */
    public byte getReceiver() {
        return bytes[2];
    }

    /**
     * Get variable of this telegram
     *
     * @return variable
     */
    public byte getVariable() {
        return bytes[3];
    }

    /**
     * Get value of this telegram
     *
     * @return value
     */
    public byte getValue() {
        return bytes[4];
    }

    /**
     * Get checksum of this telegram
     *
     * @return checksum
     */
    public byte getCheksum() {
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
    public Map<String, State> parse(ChannelDescriptor descriptor, ValloxExpiringCacheMap cache) {
        channelsToUpdate.clear();
        ValloxChannel valloxChannel = descriptor.getValloxChannel();
        if (valloxChannel instanceof MultipleValueChannel) {
            Collection<String> subchannels = valloxChannel.getSubChannels();
            for (String channel : subchannels) {
                ChannelDescriptor subChannelDescriptor = ChannelDescriptor.get(channel);
                State state = subChannelDescriptor.convertToState(getValue());
                channelsToUpdate.put(channel, state);
            }
        } else if (valloxChannel instanceof TemperatureChannel) {
            State state = descriptor.convertToState(getValue());
            channelsToUpdate.put(descriptor.channelID(), state);
            calculateEfficiencies(cache);
        } else if (descriptor.equals(ChannelDescriptor.CO2)) {
            calculateCO2(cache);
        } else if (descriptor.equals(ChannelDescriptor.CO2_SETPOINT)) {
            calculateCO2SetPoint(cache);
        } else {
            State state = descriptor.convertToState(getValue());
            channelsToUpdate.put(descriptor.channelID(), state);
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
        if (!cache.containsTemperatures()) {
            return;
        }
        int tempInside = ValloxSEConstants.TEMPERATURE_MAPPING[Byte
                .toUnsignedInt(cache.getValue(ChannelDescriptor.TEMPERATURE_INSIDE))];
        int tempOutside = ValloxSEConstants.TEMPERATURE_MAPPING[Byte
                .toUnsignedInt(cache.getValue(ChannelDescriptor.TEMPERATURE_OUTSIDE))];
        int tempExhaust = ValloxSEConstants.TEMPERATURE_MAPPING[Byte
                .toUnsignedInt(cache.getValue(ChannelDescriptor.TEMPERATURE_EXHAUST))];
        int tempIncoming = ValloxSEConstants.TEMPERATURE_MAPPING[Byte
                .toUnsignedInt(cache.getValue(ChannelDescriptor.TEMPERATURE_INCOMING))];
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
    }

    /**
     * Calculate measure CO2 value from 2 bytes. Both needs to be in cache.
     *
     * @param cache the cache where values are fetched.
     */
    private void calculateCO2(ValloxExpiringCacheMap cache) {
        if (!cache.containsCO2()) {
            logger.debug("Skipping CO2 calculation. Not enough values in cache.");
            channelsToUpdate.put("status#co2", UnDefType.UNDEF);
            return;
        }
        byte co2HighByte = cache.getValue(ChannelDescriptor.CO2_HIGH);
        byte co2LowByte = cache.getValue(ChannelDescriptor.CO2_LOW);
        channelsToUpdate.put(ChannelDescriptor.CO2.channelID(),
                new DecimalType((short) ((co2HighByte << 8) | (co2LowByte & 0xFF))));
    }

    /**
     * Calculate CO2 set point value from 2 bytes. Both needs to be in cache.
     *
     * @param cache the cache where values are fetched.
     */
    private void calculateCO2SetPoint(ValloxExpiringCacheMap cache) {
        if (!cache.containsCO2SetPoint()) {
            logger.debug("Skipping CO2 set point calculation. Not enough values in cache.");
            channelsToUpdate.put("status#co2SetPoint", UnDefType.UNDEF);
            return;
        }
        byte co2SetPointHighByte = cache.getValue(ChannelDescriptor.CO2_SETPOINT_HIGH);
        byte co2SetPointLowByte = cache.getValue(ChannelDescriptor.CO2_SETPOINT_LOW);
        channelsToUpdate.put(ChannelDescriptor.CO2_SETPOINT.channelID(),
                new DecimalType((short) ((co2SetPointHighByte << 8) | (co2SetPointLowByte & 0xFF))));

    }
}
