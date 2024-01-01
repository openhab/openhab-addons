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
package org.openhab.binding.panamaxfurman.internal.transport;

import static org.openhab.binding.panamaxfurman.internal.PanamaxFurmanConstants.*;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.panamaxfurman.internal.protocol.PowerConditionerChannel;
import org.openhab.binding.panamaxfurman.internal.protocol.ProtocolMapper;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanInformationReceivedEvent;
import org.openhab.binding.panamaxfurman.internal.util.EvictingQueue;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps a Channel to a command that can be sent to the device
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

    private final Thing thing;

    private EvictingQueue<PowerConditionerChannel, String> recentRequestCommandList = new EvictingQueue<>(5);
    private volatile long lastIdRequestSentAt = 0;
    private boolean wasLastBrand = false;

    public TelnetAndRs232ProtocolMapper(Thing thing) {
        this.thing = thing;
    }

    @Override
    public @Nullable String buildQueryString(PowerConditionerChannel channel, @Nullable Integer outletNumber) {
        String commandToReturn;
        switch (channel) {
            case POWER:
                commandToReturn = "?OUTLETSTAT";
                break;
            case ID:
                commandToReturn = "?ID";
                lastIdRequestSentAt = System.currentTimeMillis();
                break;
            default:
                commandToReturn = null;
        }
        if (commandToReturn == null) {
            logger.warn("Don't know how to translate PowerConditionerChannel {} into a command query string", channel);
        } else {
            recentRequestCommandList.put(channel, commandToReturn);
        }
        return commandToReturn;
    }

    @Override
    public @Nullable String buildUpdateString(PowerConditionerChannel channel, @Nullable Integer outletNumber,
            State stateToSet) {
        return switch (channel) {
            case POWER -> {
                Objects.requireNonNull(outletNumber);
                yield "!SWITCH " + outletNumber + " " + stateToSet.toString();
            }
            case ID -> null;
        };
    }

    @Override
    public @Nullable PanamaxFurmanInformationReceivedEvent parseUpdateIfSupported(String data) {
        if (RECEIVE_INVALID_PARAMETER.equals(data)) {
            logger.warn("Received INVALID_PARAMETER from device, recent commands sent are: {}",
                    recentRequestCommandList);
            return null;
        } else if (data.equals(RECEIVE_BRAND_PANAMAX) || data.equals(RECEIVE_BRAND_FURMAN)) {
            long elapsed = System.currentTimeMillis() - lastIdRequestSentAt;
            if (System.currentTimeMillis() - lastIdRequestSentAt > 500) {
                logger.debug("Received spontaneous brand message, strange.  Assuming firmware is next. elapsed={}",
                        elapsed);
            }
            wasLastBrand = true;
            // Set to mixed case
            String brandToSet = "Panamax";
            if (data.equals(RECEIVE_BRAND_FURMAN)) {
                brandToSet = "Furman";
            }
            thing.setProperty(PROPERTY_BRAND_NAME, brandToSet);
            logger.debug("Set property {}={}", PROPERTY_BRAND_NAME, brandToSet);
            return null; // nothing to return for properties
        } else if (wasLastBrand) {
            wasLastBrand = false;
            // assume this is the model
            if (data.trim().split(" ").length > 1) {
                logger.warn("wasLastBrand was true but model had multiple parts: {}", data);
            }
            thing.setProperty(PROPERTY_MODEL_NAME, stripDollar(data));
            logger.debug("Set property {}={}", PROPERTY_MODEL_NAME, stripDollar(data));
            return null; // nothing to return for properties
        } else if (data.startsWith(RECEIVE_FIRMWARE)) {
            thing.setProperty(PROPERTY_FIRMWARE_VERSION, chopHeader(data, RECEIVE_FIRMWARE));
            logger.debug("Set property {}={}", PROPERTY_FIRMWARE_VERSION, chopHeader(data, RECEIVE_FIRMWARE));
            return null; // nothing to return for properties
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

    private String stripDollar(String data) {
        if (!data.startsWith("$")) {
            logger.debug("stripDollar called with '{}' but first char isn't $ - returning unmodified", data);
            return data;
        }
        return data.substring(1);
    }

    private String chopHeader(String data, String header) {
        if (!data.startsWith(header)) {
            logger.debug("chopHeader called with '{}' but data doesn't start with that: {} - returning unmodified",
                    header, data);
            return data.trim();
        }
        return data.substring(header.length()).trim();
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
            logger.warn(
                    "parseOutletAfter called with '{}' but data doesn't start with that: {} - returning null for outlet #",
                    header, data);
        }
        return null;
    }
}
