package org.openhab.binding.freeboxos.internal.config;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.THING_TYPE_HOME_BASIC_SHUTTER;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.home.HomeNode;
import org.openhab.binding.freeboxos.internal.api.home.HomeNodeEndpoint;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeConfiguration.class);

    public static final String NODE_ID = "nodeId";

    public int nodeId = 1;

    public static @Nullable DiscoveryResultBuilder configure(ThingUID bridgeUID, HomeNode node) {
        DiscoveryResultBuilder discoveryResultBuilder = null;
        String category = node.getCategory();
        if (category != null) {
            try {
                switch (category) {
                    case "basic_shutter":
                        ThingUID thingUID = new ThingUID(THING_TYPE_HOME_BASIC_SHUTTER, bridgeUID,
                                Long.toString(node.getId()));
                        discoveryResultBuilder = DiscoveryResultBuilder.create(thingUID);
                        BasicShutter.configure(discoveryResultBuilder, node);

                    default:
                        break;
                }
            } catch (FreeboxException e) {
                LOGGER.warn("Error while requesting data for home things discovery : {}", e.getMessage());
                discoveryResultBuilder = null;
            }
        }
        if (discoveryResultBuilder != null) {
            discoveryResultBuilder.withProperty(NODE_ID, node.getId());
        }
        return discoveryResultBuilder;
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
            if (showEndpoints != null) {
                for (HomeNodeEndpoint endpoint : showEndpoints) {
                    if ("slot".equals(endpoint.getEpType())) {
                        switch (endpoint.getName()) {
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
                                throw new FreeboxException("Unknown endpoint name {}", endpoint.getName());
                        }
                    } else if ("signal".equals(endpoint.getEpType()) && "state".equals(endpoint.getName())) {
                        discoveryResultBuilder.withProperty(STATE_SIGNAL_ID, endpoint.getId());
                    }
                }
            } else {
                throw new FreeboxException("Missing slot");
            }
        }
    }

}
