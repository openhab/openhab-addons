/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.plclogo.internal.handler;

import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.BLOCK_PROPERTY;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.LOGO_MEMORY_BLOCK;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.NOT_SUPPORTED;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plclogo.internal.PLCLogoBindingConstants;
import org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.Layout;
import org.openhab.binding.plclogo.internal.PLCLogoClient;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PLCCommonHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
public abstract class PLCCommonHandler extends BaseThingHandler {

    public static final int INVALID = Integer.MAX_VALUE;

    private final Logger logger = LoggerFactory.getLogger(PLCCommonHandler.class);

    private String family = NOT_SUPPORTED;
    private final Map<String, @Nullable State> oldValues = new HashMap<>();

    /**
     * Constructor.
     */
    public PLCCommonHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        synchronized (oldValues) {
            oldValues.clear();
        }
        scheduler.execute(this::doInitialization);
    }

    @Override
    public void dispose() {
        logger.debug("Dispose LOGO! common block handler.");
        super.dispose();

        final var tBuilder = editThing();
        for (final var channel : getThing().getChannels()) {
            tBuilder.withoutChannel(channel.getUID());
        }
        updateThing(tBuilder.build());

        synchronized (oldValues) {
            oldValues.clear();
        }

        family = NOT_SUPPORTED;
    }

    /**
     * Return data buffer start address to read/write dependent on configured Logo! family.
     *
     * @return Start address of data buffer
     */
    public int getStartAddress() {
        final var kind = getBlockKind();
        final var family = getLogoFamily();
        logger.debug("Get start address of {} LOGO! for {} blocks.", family, kind);

        final var memory = LOGO_MEMORY_BLOCK.get(family);
        final var layout = (memory != null) ? memory.get(kind) : null;
        return (layout != null ? layout.address() : INVALID);
    }

    /**
     * Return data buffer length to read/write dependent on configured Logo! family.
     *
     * @return Length of data buffer in bytes
     */
    public int getBufferLength() {
        final var kind = getBlockKind();
        final var family = getLogoFamily();
        logger.debug("Get data buffer length of {} LOGO! for {} blocks.", family, kind);

        final var memory = LOGO_MEMORY_BLOCK.get(family);
        final var layout = (memory != null) ? memory.get(kind) : null;
        return (layout != null ? layout.length() : 0);
    }

    /**
     * Update value channel of current thing with new data.
     *
     * @param data Data value to update with
     */
    public abstract void setData(final byte[] data);

    /**
     * Checks if block name is valid.
     *
     * @param name Name of the LOGO! block to check
     * @return True, if the name is valid and false otherwise
     */
    protected abstract boolean isValid(final String name);

    /**
     * Returns configured block kind.
     *
     * @return Configured block kind
     */
    protected abstract String getBlockKind();

    /**
     * Return number of channels dependent on configured Logo! family.
     *
     * @return Number of channels
     */
    protected abstract int getNumberOfChannels();

    /**
     * Calculate address for the block with given name.
     *
     * @param name Name of the LOGO! block
     * @return Calculated address
     */
    protected int getAddress(final String name) {
        var address = INVALID;

        logger.debug("Get address of {} LOGO! for block {} .", getLogoFamily(), name);

        final var base = getBase(name);
        if (isValid(name) && (base != INVALID)) {
            String block = name.split("\\.")[0];
            if (Character.isDigit(block.charAt(1))) {
                address = Integer.parseInt(block.substring(1));
            } else if (Character.isDigit(block.charAt(2))) {
                address = Integer.parseInt(block.substring(2));
            } else if (Character.isDigit(block.charAt(3))) {
                address = Integer.parseInt(block.substring(3));
            }
        } else {
            logger.info("Wrong configurated LOGO! block {} found.", name);
        }

        return address;
    }

    /**
     * Calculate address offset for given block name.
     *
     * @param name Name of the data block
     * @return Calculated address offset
     */
    protected int getBase(final String name) {
        Layout layout = null;
        final var family = getLogoFamily();

        logger.debug("Get base address of {} LOGO! for block {} .", family, name);

        final var block = name.split("\\.")[0];
        final var memory = LOGO_MEMORY_BLOCK.get(family);
        if (isValid(name) && !block.isEmpty() && (memory != null)) {
            if (Character.isDigit(block.charAt(1))) {
                layout = memory.get(block.substring(0, 1));
            } else if (Character.isDigit(block.charAt(2))) {
                layout = memory.get(block.substring(0, 2));
            } else if (Character.isDigit(block.charAt(3))) {
                layout = memory.get(block.substring(0, 3));
            }
        }

        return layout != null ? layout.address() : INVALID;
    }

    /**
     * Checks if thing handler is valid and online.
     *
     * @return True, if handler is valid and false otherwise
     */
    protected boolean isThingOnline() {
        final var bridge = getBridge();
        if (bridge != null) {
            final var thing = getThing();
            return ((ThingStatus.ONLINE == bridge.getStatus()) && (ThingStatus.ONLINE == thing.getStatus()));
        }
        return false;
    }

    protected @Nullable State getOldValue(final String name) {
        synchronized (oldValues) {
            return oldValues.get(name);
        }
    }

    protected void setOldValue(final String name, final @Nullable State value) {
        synchronized (oldValues) {
            if (!NOT_SUPPORTED.equalsIgnoreCase(name)) {
                oldValues.put(name, value);
            } else {
                logger.info("Wrong configurated LOGO! block {} found.", name);
            }
        }
    }

    /**
     * Returns configured LOGO! communication client.
     *
     * @return Configured LOGO! client
     */
    protected @Nullable PLCLogoClient getLogoClient() {
        final var handler = getBridgeHandler();
        return (handler != null) ? handler.getLogoClient() : null;
    }

    protected @Nullable PLCBridgeHandler getBridgeHandler() {
        final var bridge = getBridge();
        if (bridge != null) {
            final var handler = bridge.getHandler();
            if (handler instanceof PLCBridgeHandler bridgeHandler) {
                return bridgeHandler;
            }
        }
        return null;
    }

    /**
     * Returns configured LOGO! family.
     *
     * @see PLCLogoBindingConstants#LOGO_0BA7
     * @see PLCLogoBindingConstants#LOGO_0BA8
     * @return Configured LOGO! family
     */
    protected String getLogoFamily() {
        return family;
    }

    /**
     * Perform thing initialization.
     */
    protected void doInitialization() {
        final var handler = getBridgeHandler();
        if (handler != null) {
            family = handler.getLogoFamily();
            final var client = handler.getLogoClient();
            if ((client == null) || NOT_SUPPORTED.equalsIgnoreCase(family)) {
                final var message = "Can not initialize LOGO! block handler.";
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);

                final var thing = getThing();
                logger.warn("Can not initialize thing {} for LOGO! {}.", thing.getUID(), thing.getBridgeUID());
            }
        }
    }

    protected static String getBlockFromChannel(final @Nullable Channel channel) {
        if (channel == null) {
            return NOT_SUPPORTED;
        }
        String block = channel.getProperties().get(BLOCK_PROPERTY);
        return block == null ? NOT_SUPPORTED : block;
    }
}
