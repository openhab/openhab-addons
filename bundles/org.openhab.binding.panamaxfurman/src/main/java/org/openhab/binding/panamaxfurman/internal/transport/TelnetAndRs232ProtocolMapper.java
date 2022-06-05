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

package org.openhab.binding.panamaxfurman.internal.transport;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.panamaxfurman.internal.protocol.PowerConditionerChannel;
import org.openhab.binding.panamaxfurman.internal.protocol.ProtocolMapper;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanInformationReceivedEvent;
import org.openhab.binding.panamaxfurman.internal.util.EvictingQueue;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception thrown when an unsupported command type is sent to a channel.
 *
 * @author Dave Badia - Initial contribution
 *
 */
@NonNullByDefault
public class TelnetAndRs232ProtocolMapper implements ProtocolMapper {
    private static final Logger logger = LoggerFactory.getLogger(TelnetAndRs232ProtocolMapper.class);

    private static final String RECEIVE_BRAND_PANAMAX = "$PANAMAX";
    private static final String RECEIVE_BRAND_FURMAN = "$FURMAN";

    private static final String RECEIVE_INVALID_PARAMETER = "$INVALID_PARAMETER";
    private static final String RECEIVE_FIRMWARE = "$FIRMWARE:";
    private static final String RECEIVE_POWER = "$OUTLET";

    private EvictingQueue<PowerConditionerChannel, String> recentRequestCommandList = new EvictingQueue<>(5);
    private volatile long lastIdRequestSentAt = 0;
    private boolean wasLastBrand = false;

    @Override
    public @Nullable String buildQueryString(PowerConditionerChannel channel, @Nullable Integer outletNumber) {
        String commandToReturn;
        switch (channel) {
            case BRAND:
            case FIRMWARE:
            case MODEL:
                lastIdRequestSentAt = System.currentTimeMillis();
                commandToReturn = "?ID";
                break;
            case POWER:
                commandToReturn = "?OUTLETSTAT";
                break;
            default:
                commandToReturn = null;
        }
        if (commandToReturn == null) {
            logger.error("Don't know how to translate PowerConditionerChannel {} into a command query string", channel);
        } else {
            recentRequestCommandList.put(channel, commandToReturn);
        }
        return commandToReturn;
    }

    @Override
    public @Nullable String buildUpdateString(PowerConditionerChannel channel, @Nullable Integer outletNumber,
            State stateToSet) {
        String commandToReturn;
        switch (channel) {
            case POWER:
                Objects.nonNull(outletNumber);
                commandToReturn = "!SWITCH " + outletNumber + " " + stateToSet.toString();
                break;
            default:
                commandToReturn = null;
        }
        if (commandToReturn == null) {
            logger.error("Don't know how to translate PowerConditionerChannel {} into a command update string",
                    channel);
        }
        return commandToReturn;
    }

    @Override
    public @Nullable PanamaxFurmanInformationReceivedEvent parseUpdateIfSupported(String data) {
        if (RECEIVE_INVALID_PARAMETER.equals(data)) {
            logger.error("Received INVALID_PARAMETER from device, recent commands sent are: {}",
                    recentRequestCommandList);
            return null;
        } else if (data.equals(RECEIVE_BRAND_PANAMAX) || data.equals(RECEIVE_BRAND_FURMAN)) {
            long elapsed = System.currentTimeMillis() - lastIdRequestSentAt;
            if (System.currentTimeMillis() - lastIdRequestSentAt > 500) {
                logger.warn("Received spontaneous brand message, strange.  Assuming firmware is next. elapsed={}",
                        elapsed);
            }
            wasLastBrand = true;
            return handleBrand(data);
        } else if (wasLastBrand) {
            wasLastBrand = false;
            // assume this is the model
            if (data.trim().split(" ").length > 1) {
                logger.warn("wasLastBrand was true but model had multiple parts: {}", data);
            }
            return new PanamaxFurmanInformationReceivedEvent(PowerConditionerChannel.MODEL, null, stripDollar(data));
        } else if (data.startsWith(RECEIVE_FIRMWARE)) {
            return new PanamaxFurmanInformationReceivedEvent(PowerConditionerChannel.FIRMWARE, null,
                    chopHeader(data, RECEIVE_FIRMWARE));
        } else if (data.startsWith(RECEIVE_POWER)) {
            PowerConditionerChannel channel = PowerConditionerChannel.POWER;
            String[] parts = data.split(" = ");
            State newState = channel.buildState(parts[1]);
            return new PanamaxFurmanInformationReceivedEvent(channel, parseOutletAfter(parts[0], RECEIVE_POWER),
                    newState);
        }

        logger.debug("Received data from power conditioner but it is not supported for update: '{}'", data);
        return null;
    }

    private PanamaxFurmanInformationReceivedEvent handleBrand(String data) {
        // Set to mixed case
        String brandToSet = "Panamax";
        if (data.equals(RECEIVE_BRAND_FURMAN)) {
            brandToSet = "Furman";
        }
        return new PanamaxFurmanInformationReceivedEvent(PowerConditionerChannel.BRAND, null,
                new StringType(brandToSet));
    }

    private StringType stripDollar(String data) {
        if (!data.startsWith("$")) {
            logger.error("stripDollar called with '{}' but first char isn't $ - returning unmodified", data);
            return new StringType(data);
        }
        return new StringType(data.substring(1));
    }

    private State chopHeader(String data, String header) {
        if (!data.startsWith(header)) {
            logger.error("chopHeader called with '{}' but data doesn't start with that: {} - returning unmodified",
                    header, data);
            return new StringType(data);
        }
        return new StringType(data.substring(header.length()).trim());
    }

    public static @Nullable Integer parseOutletAfter(String data, String header) {
        if (data.startsWith(header)) {
            try {
                return Integer.parseInt(data.substring(header.length()));
            } catch (NumberFormatException e) {
                logger.warn(
                        "Caught NumberFormatException trying to parse outlet # from {} with header {} - returning null for outlet #",
                        data, header);
            }
        } else {
            logger.error(
                    "parseOutletAfter called with '{}' but data doesn't start with that: {} - returning null for outlet #",
                    header, data);
        }
        return null;
    }
}
