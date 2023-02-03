/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.dmx.internal.handler;

import static org.openhab.binding.dmx.internal.DmxBindingConstants.THING_TYPE_ARTNET_BRIDGE;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dmx.internal.config.ArtnetBridgeHandlerConfiguration;
import org.openhab.binding.dmx.internal.dmxoverethernet.ArtnetNode;
import org.openhab.binding.dmx.internal.dmxoverethernet.ArtnetPacket;
import org.openhab.binding.dmx.internal.dmxoverethernet.DmxOverEthernetHandler;
import org.openhab.binding.dmx.internal.dmxoverethernet.DmxOverEthernetPacket;
import org.openhab.binding.dmx.internal.dmxoverethernet.IpNode;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ArtnetBridgeHandler} is responsible for handling the communication
 * with ArtNet devices
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class ArtnetBridgeHandler extends DmxOverEthernetHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_ARTNET_BRIDGE);
    public static final int MIN_UNIVERSE_ID = 0;
    public static final int MAX_UNIVERSE_ID = 32767;

    private final Logger logger = LoggerFactory.getLogger(ArtnetBridgeHandler.class);

    public ArtnetBridgeHandler(Bridge artnetBridge) {
        super(artnetBridge);
    }

    @Override
    protected void updateConfiguration() {
        ArtnetBridgeHandlerConfiguration configuration = getConfig().as(ArtnetBridgeHandlerConfiguration.class);

        setUniverse(configuration.universe, MIN_UNIVERSE_ID, MAX_UNIVERSE_ID);
        DmxOverEthernetPacket packetTemplate = this.packetTemplate;
        if (packetTemplate == null) {
            packetTemplate = new ArtnetPacket();
            this.packetTemplate = packetTemplate;
        }
        packetTemplate.setUniverse(universe.getUniverseId());

        receiverNodes.clear();
        if (configuration.address.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not initialize sender (address not set)");
            uninstallScheduler();
            logger.debug("remote address not set for {}", this.thing.getUID());
            return;
        } else {
            try {
                receiverNodes = IpNode.fromString(configuration.address, ArtnetNode.DEFAULT_PORT);
                logger.debug("using unicast mode to {} for {}", receiverNodes.toString(), this.thing.getUID());
            } catch (IllegalArgumentException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                return;
            }
        }

        if (!configuration.localaddress.isEmpty()) {
            senderNode = new IpNode(configuration.localaddress);
        }
        logger.debug("originating address is {} for {}", senderNode, this.thing.getUID());

        refreshAlways = configuration.refreshmode.equals("always");

        logger.debug("refresh mode set to always: {}", refreshAlways);

        updateStatus(ThingStatus.UNKNOWN);
        super.updateConfiguration();

        logger.debug("updated configuration for ArtNet bridge {}", this.thing.getUID());
    }

    @Override
    public void initialize() {
        logger.debug("initializing ArtNet bridge {}", this.thing.getUID());

        packetTemplate = new ArtnetPacket();
        updateConfiguration();
    }
}
