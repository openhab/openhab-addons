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
package org.openhab.binding.freeboxos.internal.config;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.THING_TYPE_HOME_BASIC_SHUTTER;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.home.HomeNode;
import org.openhab.binding.freeboxos.internal.api.home.HomeNodeEndpoint;
import org.openhab.binding.freeboxos.internal.api.home.HomeNodeEndpoint.EpType;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NodeConfiguration} is responsible for holding configuration informations associated to a Freebox Home
 * thing type
 *
 * @author ben12 - Initial contribution
 */
@NonNullByDefault
public class NodeConfiguration {
    public static final String NODE_ID = "nodeId";
    private static final NodeConfigurationBuilder BUILDER_INSTANCE = new NodeConfigurationBuilder();
    private static final Logger logger = LoggerFactory.getLogger(NodeConfiguration.class);

    public int nodeId = 1;

    public static NodeConfigurationBuilder builder() {
        return BUILDER_INSTANCE;
    }

    public static class NodeConfigurationBuilder {
        private final Logger logger = LoggerFactory.getLogger(NodeConfigurationBuilder.class);

        private NodeConfigurationBuilder() {
        }

        public @Nullable DiscoveryResultBuilder configure(ThingUID bridgeUID, HomeNode node) {
            DiscoveryResultBuilder discoveryResultBuilder = null;
            try {
                switch (node.getCategory()) {
                    case "basic_shutter":
                        ThingUID thingUID = new ThingUID(THING_TYPE_HOME_BASIC_SHUTTER, bridgeUID,
                                Integer.toString(node.getId()));
                        discoveryResultBuilder = DiscoveryResultBuilder.create(thingUID);
                        BasicShutter.configure(discoveryResultBuilder, node);
                        break;
                    default:
                        break;
                }
            } catch (FreeboxException e) {
                logger.warn("Error while requesting data for home things discovery : {}", e.getMessage());
                discoveryResultBuilder = null;
            }
            if (discoveryResultBuilder != null) {
                discoveryResultBuilder.withProperty(NODE_ID, node.getId());
            }
            return discoveryResultBuilder;
        }
    }

    public static class BasicShutter extends NodeConfiguration {
        public static final String UP_SLOT_ID = "upSlotId";
        public static final String STOP_SLOT_ID = "stopSlotId";
        public static final String DOWN_SLOT_ID = "downSlotId";
        public static final String STATE_SIGNAL_ID = "stateSignalId";

        public int upSlotId = 0;
        public int stopSlotId = 1;
        public int downSlotId = 2;
        public int stateSignalId = 3;

        public static void configure(DiscoveryResultBuilder discoveryResultBuilder, HomeNode homeNode)
                throws FreeboxException {
            List<HomeNodeEndpoint> showEndpoints = homeNode.getShowEndpoints();
            for (HomeNodeEndpoint endpoint : showEndpoints) {
                String name = endpoint.getName();
                if (EpType.SLOT.equals(endpoint.getEpType()) && name != null) {
                    switch (name) {
                        case "up":
                            discoveryResultBuilder.withProperty(UP_SLOT_ID, endpoint.getId());
                            break;
                        case "stop":
                            discoveryResultBuilder.withProperty(STOP_SLOT_ID, endpoint.getId());
                            break;
                        case "down":
                            discoveryResultBuilder.withProperty(DOWN_SLOT_ID, endpoint.getId());
                            break;
                        default:
                            logger.info("Unknown endpoint name :" + name);
                    }
                } else if (EpType.SIGNAL.equals(endpoint.getEpType()) && "state".equals(name)) {
                    discoveryResultBuilder.withProperty(STATE_SIGNAL_ID, endpoint.getId());
                }
            }
        }
    }
}
