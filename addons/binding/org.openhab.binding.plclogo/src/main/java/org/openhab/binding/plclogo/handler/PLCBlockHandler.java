/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo.handler;

import static org.openhab.binding.plclogo.PLCLogoBindingConstants.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.plclogo.PLCLogoBindingConstants;
import org.openhab.binding.plclogo.internal.PLCLogoClient;
import org.openhab.binding.plclogo.internal.PLCLogoDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Moka7.S7Client;

/**
 * The {@link PLCBlockHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
public abstract class PLCBlockHandler extends BaseThingHandler {

    public static final int INVALID = Integer.MAX_VALUE;

    private final Logger logger = LoggerFactory.getLogger(PLCBlockHandler.class);

    private PLCLogoClient client;
    private String family;

    private int address = INVALID;
    private int bit = INVALID;

    /**
     * Constructor.
     */
    public PLCBlockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final Bridge bridge = getBridge();
        Objects.requireNonNull(bridge, "PLCBlockHandler: Bridge may not be null.");

        final Thing thing = getThing();
        Objects.requireNonNull(thing, "PLCBlockHandler: Thing may not be null.");
        if ((ThingStatus.ONLINE != thing.getStatus()) || (ThingStatus.ONLINE != bridge.getStatus())) {
            return;
        }

        final String channelId = channelUID.getId();
        if (!ANALOG_CHANNEL_ID.equals(channelId) || !DIGITAL_CHANNEL_ID.equals(channelId) || (client == null)) {
            logger.warn("Can not update channel {}: {}.", channelUID, client);
            return;
        }

        if (command instanceof RefreshType) {
            final String name = getBlockName();
            final int offset = getBlockDataType().getByteCount();
            if ((offset > 0) && (name != null)) {
                final byte[] buffer = new byte[offset];
                int result = client.readDBArea(1, getAddress(), buffer.length, S7Client.S7WLByte, buffer);
                if (result == 0) {
                    setData(Arrays.copyOfRange(buffer, 0, offset));
                } else {
                    logger.warn("Can not read data from LOGO!: {}.", S7Client.ErrorText(result));
                }
            } else {
                logger.warn("Invalid block {} found.", name);
            }
        } else {
            logger.debug("Not supported command {} received.", command);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Dispose LOGO! common block handler.");
        super.dispose();

        client = null;
        family = null;
        address = INVALID;
        bit = INVALID;
    }

    /**
     * Calculate memory address for configured block.
     *
     * @return Calculated address
     */
    public int getAddress() {
        if (address == INVALID) {
            address = getAddress(getBlockName());
        }
        return address;
    }

    /**
     * Calculate bit within memory address for configured block.
     *
     * @return Calculated bit
     */
    public int getBit() {
        if (bit == INVALID) {
            bit = getBit(getBlockName());
        }
        return bit;
    }

    /**
     * Returns configured block name.
     *
     * @return Name of configured LOGO! block
     */
    public abstract String getBlockName();

    /**
     * Update value channel of current thing with new data.
     *
     * @param data Data value to update with
     */
    public abstract void setData(final byte[] data);

    /**
     * Returns data type accepted by LOGO! block.
     * Can be BIT for digital blocks and WORD/DWORD for analog
     *
     * @return Data type accepted by configured block
     */
    public abstract PLCLogoDataType getBlockDataType();

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        address = INVALID;
        bit = INVALID;
    }

    /**
     * Returns configured LOGO! communication client.
     *
     * @return Configured LOGO! client
     */
    protected PLCLogoClient getClient() {
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
        return family;
    }

    /**
     * Perform thing initialization.
     *
     */
    protected void doInitialization() {
        logger.debug("Initialize LOGO! common block handler.");

        final Bridge bridge = getBridge();
        Objects.requireNonNull(bridge, "PLCBlockHandler: Bridge may not be null.");

        final PLCBridgeHandler handler = (PLCBridgeHandler) bridge.getHandler();
        Objects.requireNonNull(handler, "PLCBlockHandler: Bridge handler may not be null.");

        String message = "";
        boolean success = false;
        if (getBlockName() != null) {
            family = handler.getLogoFamily();
            final Map<?, Integer> block = LOGO_MEMORY_BLOCK.get(family);
            if ((0 <= getAddress()) && (getAddress() <= block.get("SIZE"))) {
                success = true;
                client = handler.getLogoClient();
                updateStatus(ThingStatus.ONLINE);
            } else {
                message = "Can not initialize LOGO! block " + getBlockName() + ".";
            }
        } else {
            message = "Can not initialize LOGO! block. Please check block name.";
        }

        if (!success) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
        }
    }

    /**
     * Calculate address for the block with given name.
     *
     * @param name Name of the LOGO! block
     * @return Calculated address offset
     */
    protected abstract int getAddress(final String name);

    /**
     * Calculate bit within address for block with given name.
     *
     * @param name Name of the LOGO! block
     * @return Calculated bit
     */
    protected abstract int getBit(final String name);

}
