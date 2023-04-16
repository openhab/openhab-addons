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

import static org.openhab.binding.dmx.internal.DmxBindingConstants.THING_TYPE_SACN_BRIDGE;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dmx.internal.config.SacnBridgeHandlerConfiguration;
import org.openhab.binding.dmx.internal.dmxoverethernet.DmxOverEthernetHandler;
import org.openhab.binding.dmx.internal.dmxoverethernet.DmxOverEthernetPacket;
import org.openhab.binding.dmx.internal.dmxoverethernet.IpNode;
import org.openhab.binding.dmx.internal.dmxoverethernet.SacnNode;
import org.openhab.binding.dmx.internal.dmxoverethernet.SacnPacket;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SacnBridgeHandler} is responsible for handling the communication
 * with sACN/E1.31 devices
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SacnBridgeHandler extends DmxOverEthernetHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_SACN_BRIDGE);
    public static final int MIN_UNIVERSE_ID = 1;
    public static final int MAX_UNIVERSE_ID = 63999;

    private final Logger logger = LoggerFactory.getLogger(SacnBridgeHandler.class);
    private final UUID senderUUID;

    public SacnBridgeHandler(Bridge sacnBridge) {
        super(sacnBridge);
        senderUUID = UUID.randomUUID();
    }

    @Override
    protected void updateConfiguration() {
        SacnBridgeHandlerConfiguration configuration = getConfig().as(SacnBridgeHandlerConfiguration.class);

        setUniverse(configuration.universe, MIN_UNIVERSE_ID, MAX_UNIVERSE_ID);
        DmxOverEthernetPacket packetTemplate = this.packetTemplate;
        if (packetTemplate == null) {
            packetTemplate = new SacnPacket(senderUUID);
            this.packetTemplate = packetTemplate;
        }
        packetTemplate.setUniverse(universe.getUniverseId());

        receiverNodes.clear();
        if ((configuration.mode.equals("unicast"))) {
            if (configuration.address.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Could not initialize unicast sender (address not set)");
                return;
            } else {
                try {
                    receiverNodes = IpNode.fromString(configuration.address, SacnNode.DEFAULT_PORT);
                    logger.debug("using unicast mode to {} for {}", receiverNodes.toString(), this.thing.getUID());
                } catch (IllegalArgumentException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                    return;
                }
            }
        } else {
            receiverNodes = new ArrayList<>();
            receiverNodes.add(SacnNode.getBroadcastNode(universe.getUniverseId()));
            logger.debug("using multicast mode to {} for {}", receiverNodes, this.thing.getUID());
        }

        if (!configuration.localaddress.isEmpty()) {
            senderNode = new IpNode(configuration.localaddress);
        }
        logger.debug("originating address is {} for {}", senderNode, this.thing.getUID());

        refreshAlways = configuration.refreshmode.equals("always");
        logger.debug("refresh mode set to always: {}", refreshAlways);

        updateStatus(ThingStatus.UNKNOWN);
        super.updateConfiguration();

        logger.debug("updated configuration for sACN/E1.31 bridge {}", this.thing.getUID());
    }

    @Override
    public void initialize() {
        logger.debug("initializing sACN/E1.31 bridge {}", this.thing.getUID());

        packetTemplate = new SacnPacket(senderUUID);
        updateConfiguration();
    }
}
