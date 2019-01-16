/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo.handler;

import static org.openhab.binding.plclogo.PLCLogoBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
@NonNullByDefault
public abstract class PLCCommonHandler extends BaseThingHandler {

    public static final int INVALID = Integer.MAX_VALUE;

    private final Logger logger = LoggerFactory.getLogger(PLCCommonHandler.class);

    private Map<String, @Nullable State> oldValues = new HashMap<>();

    @Nullable
    private PLCLogoClient client;

    @Nullable
    private String family;

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

        ThingBuilder tBuilder = editThing();
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

    /**
     * Return data buffer start address to read/write dependent on configured Logo! family.
     *
     * @return Start address of data buffer
     */
    public int getStartAddress() {
        String kind = getBlockKind();
        String family = getLogoFamily();
        logger.debug("Get start address of {} LOGO! for {} blocks.", family, kind);

        Layout layout = LOGO_MEMORY_BLOCK.get(family).get(kind);
        return layout != null ? layout.address : INVALID;
    }

    /**
     * Return data buffer length to read/write dependent on configured Logo! family.
     *
     * @return Length of data buffer in bytes
     */
    public int getBufferLength() {
        String kind = getBlockKind();
        String family = getLogoFamily();
        logger.debug("Get data buffer length of {} LOGO! for {} blocks.", family, kind);

        Layout layout = LOGO_MEMORY_BLOCK.get(family).get(kind);
        return layout != null ? layout.length : 0;
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
        int address = INVALID;

        logger.debug("Get address of {} LOGO! for block {} .", getLogoFamily(), name);

        int base = getBase(name);
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

        logger.debug("Get base address of {} LOGO! for block {} .", getLogoFamily(), name);

        String block = name.split("\\.")[0];
        if (isValid(name) && !block.isEmpty()) {
            Map<?, @Nullable Layout> memory = LOGO_MEMORY_BLOCK.get(family);
            if (Character.isDigit(block.charAt(1))) {
                layout = memory.get(block.substring(0, 1));
            } else if (Character.isDigit(block.charAt(2))) {
                layout = memory.get(block.substring(0, 2));
            } else if (Character.isDigit(block.charAt(3))) {
                layout = memory.get(block.substring(0, 3));
            }
        }

        return layout != null ? layout.address : INVALID;
    }

    /**
     * Checks if thing handler is valid and online.
     *
     * @return True, if handler is valid and false otherwise
     */
    protected boolean isThingOnline() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            Thing thing = getThing();
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
            oldValues.put(name, value);
        }
    }

    /**
     * Returns configured LOGO! communication client.
     *
     * @return Configured LOGO! client
     */
    protected @Nullable PLCLogoClient getLogoClient() {
        return client;
    }

    /**
     * Returns configured LOGO! family.
     *
     * @see PLCLogoBindingConstants#LOGO_0BA7
     * @see PLCLogoBindingConstants#LOGO_0BA8
     * @return Configured LOGO! family
     */
    protected String getLogoFamily() {
        return family != null ? family : "NOT SUPPORTED";
    }

    /**
     * Perform thing initialization.
     */
    protected void doInitialization() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            PLCBridgeHandler handler = (PLCBridgeHandler) bridge.getHandler();
            if (handler != null) {
                family = handler.getLogoFamily();
                client = handler.getLogoClient();
                if ((client == null) || (family == null)) {
                    String message = "Can not initialize LOGO! block handler.";
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
                    logger.warn("Can not initialize thing {} for LOGO! {}.", thing.getUID(), bridge.getUID());
                }
            }
        }
    }

    protected static String getBlockFromChannel(final Channel channel) {
        return channel.getProperties().get(BLOCK_PROPERTY);
    }

}
