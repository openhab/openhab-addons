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
package org.openhab.binding.caddx.internal.handler;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.caddx.internal.CaddxEvent;
import org.openhab.binding.caddx.internal.config.CaddxKeypadConfiguration;
import org.openhab.binding.caddx.internal.config.CaddxPartitionConfiguration;
import org.openhab.binding.caddx.internal.config.CaddxZoneConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for a Caddx Thing Handler.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public abstract class CaddxBaseThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(CaddxBaseThingHandler.class);

    /** Bridge Handler for the Thing. */
    private @Nullable CaddxBridgeHandler caddxBridgeHandler = null;

    /** Caddx Alarm Thing type. */
    private CaddxThingType caddxThingType;

    /** Partition Number. */
    private int partitionNumber;

    /** User Number. */
    private int userNumber;

    /** Zone Number. */
    private int zoneNumber;

    /** Keypad Address. */
    private int keypadAddress;

    public CaddxBaseThingHandler(Thing thing, CaddxThingType caddxThingType) {
        super(thing);
        this.caddxThingType = caddxThingType;
    }

    @Override
    public void initialize() {
        getConfiguration(caddxThingType);

        // set the Thing offline for now
        updateStatus(ThingStatus.OFFLINE);
    }

    /**
     * Get the Bridge Handler for the Caddx system.
     *
     * @return CaddxBridgeHandler
     */
    public @Nullable CaddxBridgeHandler getCaddxBridgeHandler() {
        if (this.caddxBridgeHandler == null) {
            Bridge bridge = getBridge();

            if (bridge == null) {
                logger.debug("getCaddxBridgeHandler(): Unable to get bridge!");
                return null;
            }

            logger.trace("getCaddxBridgeHandler(): Bridge for '{}' - '{}'", getThing().getUID(), bridge.getUID());

            ThingHandler handler = bridge.getHandler();

            if (handler instanceof CaddxBridgeHandler) {
                this.caddxBridgeHandler = (CaddxBridgeHandler) handler;
            } else {
                logger.debug("getCaddxBridgeHandler(): Unable to get bridge handler!");
            }
        }

        return this.caddxBridgeHandler;
    }

    /**
     * Method to Update a Channel
     *
     * @param channel
     * @param state
     * @param description
     */
    public abstract void updateChannel(ChannelUID channel, String data);

    /**
     * Receives Events from the bridge.
     *
     * @param event.
     * @param thing
     */
    public abstract void caddxEventReceived(CaddxEvent event, Thing thing);

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Get the thing configuration.
     *
     * @param caddxThingType The Thing type
     */
    private void getConfiguration(CaddxThingType caddxThingType) {
        switch (caddxThingType) {
            case PARTITION:
                CaddxPartitionConfiguration partitionConfiguration = getConfigAs(CaddxPartitionConfiguration.class);
                setPartitionNumber(partitionConfiguration.getPartitionNumber());
                setUserNumber(partitionConfiguration.getUserNumber());
                break;
            case ZONE:
                CaddxZoneConfiguration zoneConfiguration = getConfigAs(CaddxZoneConfiguration.class);
                setZoneNumber(zoneConfiguration.getZoneNumber());
                break;
            case KEYPAD:
                CaddxKeypadConfiguration keypadConfiguration = getConfigAs(CaddxKeypadConfiguration.class);
                setKeypadAddress(keypadConfiguration.getKeypadAddress());
            default:
                break;
        }
    }

    /**
     * Get the Thing type.
     *
     * @return caddxThingType
     */
    public CaddxThingType getCaddxThingType() {
        return caddxThingType;
    }

    /**
     * Get Partition Number.
     *
     * @return partitionNumber
     */
    public int getPartitionNumber() {
        return partitionNumber;
    }

    /**
     * Set Partition Number.
     *
     * @param partitionNumber
     */
    public void setPartitionNumber(int partitionNumber) {
        this.partitionNumber = partitionNumber;
    }

    /**
     * Get User Number.
     *
     * @return userNumber
     */
    public int getUserNumber() {
        return userNumber;
    }

    /**
     * Set User Number.
     *
     * @param userNumber
     */
    public void setUserNumber(int userNumber) {
        this.userNumber = userNumber;
    }

    /**
     * Get Zone Number.
     *
     * @return zoneNumber
     */
    public int getZoneNumber() {
        return zoneNumber;
    }

    /**
     * Set Zone Number.
     *
     * @param zoneNumber
     */
    public void setZoneNumber(int zoneNumber) {
        this.zoneNumber = zoneNumber;
    }

    /**
     * Get Keypad Address.
     *
     * @return keypadAddress
     */
    public int getKeypadAddress() {
        return keypadAddress;
    }

    /**
     * Set Keypad Address.
     *
     * @param keypadAddress
     */
    public void setKeypadAddress(int keypadAddress) {
        this.keypadAddress = keypadAddress;
    }

    /**
     * Get Channel by ChannelUID.
     *
     * @param channelUID
     */
    public @Nullable Channel getChannel(ChannelUID channelUID) {
        Channel channel = null;

        List<Channel> channels = getThing().getChannels();

        for (Channel ch : channels) {
            if (channelUID == ch.getUID()) {
                channel = ch;
                break;
            }
        }

        return channel;
    }
}
