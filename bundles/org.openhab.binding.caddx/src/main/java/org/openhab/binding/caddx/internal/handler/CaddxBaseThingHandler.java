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
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
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
    private CaddxThingType caddxThingType;// = null;

    /** Caddx Properties. */
    private boolean thingHandlerInitialized = false;

    /** Partition Number. */
    private int partitionNumber;

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
        logger.trace("initialize(): {}; Thing ID: {}.", caddxThingType, this.getThing().getUID());

        getConfiguration(caddxThingType);

        // set the Thing offline for now
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void dispose() {
        logger.trace("Thing {} disposed.", getThing().getUID());

        this.setThingHandlerInitialized(false);

        super.dispose();
    }

    /**
     * Method to Initialize Thing Handler.
     */
    public void initializeThingHandler() {
        logger.trace("initializeThingHandler()");

        if (getCaddxBridgeHandler() != null) {
            this.setThingHandlerInitialized(true);
        }
    }

    /**
     * Get the Bridge Handler for the Caddx system.
     *
     * @return CaddxBridgeHandler
     */
    public @Nullable synchronized CaddxBridgeHandler getCaddxBridgeHandler() {
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

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        ThingStatus bridgeStatus = bridgeStatusInfo.getStatus();

        switch (bridgeStatus) {
            case ONLINE:
                updateStatus(bridgeStatus);
                this.initializeThingHandler();
                break;
            case OFFLINE:
                updateStatus(bridgeStatus, ThingStatusDetail.BRIDGE_OFFLINE);
                this.setThingHandlerInitialized(false);
                break;
            default:
                updateStatus(bridgeStatus);
                this.setThingHandlerInitialized(false);
                break;
        }

        logger.trace("bridgeStatusChanged(): Bridge Status: '{}' - Thing '{}' Status: '{}'!", bridgeStatusInfo,
                getThing().getUID(), getThing().getStatus());
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
                setPartitionNumber(partitionConfiguration.getPartitionNumber().intValue());
                break;
            case ZONE:
                CaddxZoneConfiguration zoneConfiguration = getConfigAs(CaddxZoneConfiguration.class);
                setZoneNumber(zoneConfiguration.getZoneNumber().intValue());
                break;
            case KEYPAD:
                CaddxKeypadConfiguration keypadConfiguration = getConfigAs(CaddxKeypadConfiguration.class);
                setKeypadAddress(keypadConfiguration.getKeypadAddress().intValue());
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

    /**
     * Get Thing Handler refresh status.
     *
     * @return thingRefresh
     */
    public boolean isThingHandlerInitialized() {
        return thingHandlerInitialized;
    }

    /**
     * Set Thing Handler status.
     *
     * @param deviceInitialized
     */
    public void setThingHandlerInitialized(boolean deviceInitialized) {
        this.thingHandlerInitialized = deviceInitialized;
    }
}
