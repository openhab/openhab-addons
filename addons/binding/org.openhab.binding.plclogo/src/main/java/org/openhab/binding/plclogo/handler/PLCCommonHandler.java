/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo.handler;

import static org.openhab.binding.plclogo.PLCLogoBindingConstants.LOGO_MEMORY_BLOCK;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.plclogo.PLCLogoBindingConstants;
import org.openhab.binding.plclogo.PLCLogoBindingConstants.Layout;
import org.openhab.binding.plclogo.internal.PLCLogoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PLCCommonHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
public abstract class PLCCommonHandler extends BaseThingHandler {

    public static final int INVALID = Integer.MAX_VALUE;

    private final Logger logger = LoggerFactory.getLogger(PLCCommonHandler.class);

    private Map<@NonNull String, State> oldValues = new HashMap<>();
    private PLCLogoClient client;
    private String family;

    /**
     * Constructor.
     */
    public PLCCommonHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        scheduler.execute(new Runnable() {
            @Override
            public void run() {
                doInitialization();
            }
        });
    }

    @Override
    public void dispose() {
        logger.debug("Dispose LOGO! common block handler.");
        super.dispose();

        final ThingBuilder tBuilder = editThing();
        for (Channel channel : getThing().getChannels()) {
            tBuilder.withoutChannel(channel.getUID());
        }
        updateThing(tBuilder.build());

        synchronized (oldValues) {
            oldValues.clear();
        }
        client = null;
        family = null;
    }

    @Override
    protected @NonNull Bridge getBridge() {
        final Bridge bridge = super.getBridge();
        Objects.requireNonNull(bridge, "PLCCommonHandler: Bridge may not be null.");

        return bridge;
    }

    /**
     * Return data buffer start address to read/write dependent on configured Logo! family.
     *
     * @return Start address of data buffer
     */
    public int getStartAddress() {
        final String kind = getBlockKind();
        final String family = getLogoFamily();
        logger.debug("Get start address of {} LOGO! for {} blocks.", family, kind);

        return LOGO_MEMORY_BLOCK.get(family).get(kind).address;
    }

    /**
     * Return data buffer length to read/write dependent on configured Logo! family.
     *
     * @return Length of data buffer in bytes
     */
    public int getBufferLength() {
        final String kind = getBlockKind();
        final String family = getLogoFamily();
        logger.debug("Get data buffer length of {} LOGO! for {} blocks.", family, kind);

        return LOGO_MEMORY_BLOCK.get(family).get(kind).length;
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
    protected abstract boolean isValid(final @NonNull String name);

    /**
     * Returns configured block kind.
     *
     * @return Configured block kind
     */
    protected abstract @NonNull String getBlockKind();

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
    protected int getAddress(final @NonNull String name) {
        int address = INVALID;

        logger.debug("Get address of {} LOGO! for block {} .", getLogoFamily(), name);

        final int base = getBase(name);
        if (isValid(name) && (base != INVALID)) {
            final String block = name.split("\\.")[0];
            if (Character.isDigit(block.charAt(1))) {
                address = Integer.parseInt(block.substring(1));
            } else if (Character.isDigit(block.charAt(2))) {
                address = Integer.parseInt(block.substring(2));
            } else if (Character.isDigit(block.charAt(3))) {
                address = Integer.parseInt(block.substring(3));
            }
        } else {
            logger.warn("Wrong configurated LOGO! block {} found.", name);
        }
        return address;
    }

    /**
     * Calculate address offset for given block name.
     *
     * @param name Name of the data block
     * @return Calculated address offset
     */
    protected int getBase(final @NonNull String name) {
        int base = INVALID;

        final String family = getLogoFamily();
        logger.debug("Get base address of {} LOGO! for block {} .", family, name);

        final String block = name.split("\\.")[0];
        if (isValid(name) && !block.isEmpty()) {
            final Map<?, Layout> memory = LOGO_MEMORY_BLOCK.get(family);
            if (Character.isDigit(block.charAt(1))) {
                base = memory.get(block.substring(0, 1)).address;
            } else if (Character.isDigit(block.charAt(2))) {
                base = memory.get(block.substring(0, 2)).address;
            } else if (Character.isDigit(block.charAt(3))) {
                base = memory.get(block.substring(0, 3)).address;
            }
        }

        return base;
    }

    /**
     * Checks if thing handler is valid and online.
     *
     * @return True, if handler is valid and false otherwise
     */
    protected boolean isThingOnline() {
        final Bridge bridge = getBridge();
        Objects.requireNonNull(bridge, "PLCCommonHandler: Bridge may not be null.");

        final Thing thing = getThing();
        Objects.requireNonNull(thing, "PLCCommonHandler: Thing may not be null.");
        return ((ThingStatus.ONLINE == thing.getStatus()) && (ThingStatus.ONLINE == bridge.getStatus()));
    }

    protected @Nullable State getOldValue(final @NonNull String name) {
        if (isValid(name)) {
            synchronized (oldValues) {
                return oldValues.get(name);
            }
        }
        return null;
    }

    protected void setOldValue(final @NonNull String name, final @Nullable State value) {
        if (isValid(name)) {
            synchronized (oldValues) {
                oldValues.put(name, value);
            }
        }
    }

    /**
     * Returns configured LOGO! communication client.
     *
     * @return Configured LOGO! client
     */
    protected @NonNull PLCLogoClient getLogoClient() {
        return client;
    }

    /**
     * Returns configured LOGO! family.
     *
     * @see PLCLogoBindingConstants#LOGO_0BA7
     * @see PLCLogoBindingConstants#LOGO_0BA8
     * @return Configured LOGO! family
     */
    protected @NonNull String getLogoFamily() {
        return family;
    }

    /**
     * Perform thing initialization.
     */
    protected void doInitialization() {
        final Bridge bridge = getBridge();
        Objects.requireNonNull(bridge, "PLCCommonHandler: Bridge may not be null.");

        final PLCBridgeHandler handler = (PLCBridgeHandler) bridge.getHandler();
        Objects.requireNonNull(handler, "PLCCommonHandler: Bridge handler may not be null.");

        synchronized (oldValues) {
            oldValues.clear();
        }

        family = handler.getLogoFamily();
        client = handler.getLogoClient();
        if ((client == null) || (family == null)) {
            final String message = "Can not initialize LOGO! block handler.";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
            logger.error("Can not initialize thing {} for LOGO! {}.", thing.getUID(), bridge.getUID());
        }
    }

}
