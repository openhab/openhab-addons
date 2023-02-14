/**
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
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
package org.openhab.binding.freeboxos.internal.discovery;

<<<<<<< Upstream, based on origin/main
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.Category;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.HomeNode;
import org.openhab.binding.freeboxos.internal.config.ClientConfiguration;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link NodeConfigurationBuilder} is responsible for holding configuration informations associated to a Freebox
 * Home thing type
 *
 * @author ben12 - Initial contribution
 */
@NonNullByDefault
public class NodeConfigurationBuilder {
    private static final NodeConfigurationBuilder BUILDER_INSTANCE = new NodeConfigurationBuilder();

    private NodeConfigurationBuilder() {
    }

    public static NodeConfigurationBuilder getInstance() {
        return BUILDER_INSTANCE;
    }

    public Optional<DiscoveryResultBuilder> configure(ThingUID bridgeUID, HomeNode node) {
        if (node.category() == Category.UNKNOWN) {
            return Optional.empty();
        }
        ThingUID thingUID = new ThingUID(node.category().getThingTypeUID(), bridgeUID, Integer.toString(node.id()));
        DiscoveryResultBuilder discoveryResultBuilder = DiscoveryResultBuilder.create(thingUID);
        discoveryResultBuilder.withProperty(ClientConfiguration.ID, node.id()).withLabel(node.label())
                .withRepresentationProperty(ClientConfiguration.ID).withBridge(bridgeUID);
        return Optional.of(discoveryResultBuilder);
=======
 * Copyright (c) 2010-2022 Contributors to the openHAB project
=======
 * Copyright (c) 2010-2023 Contributors to the openHAB project
>>>>>>> 006a813 Saving work before instroduction of ArrayListDeserializer
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
package org.openhab.binding.freeboxos.internal.discovery;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

=======
>>>>>>> 6340384 Commiting work
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.Category;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.HomeNode;
import org.openhab.binding.freeboxos.internal.config.BasicShutterConfiguration;
import org.openhab.binding.freeboxos.internal.config.ClientConfiguration;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NodeConfigurationBuilder} is responsible for holding configuration informations associated to a Freebox
 * Home thing type
 *
 * @author ben12 - Initial contribution
 */
@NonNullByDefault
public class NodeConfigurationBuilder {
    private static final NodeConfigurationBuilder BUILDER_INSTANCE = new NodeConfigurationBuilder();

    private final Logger logger = LoggerFactory.getLogger(NodeConfigurationBuilder.class);

    private NodeConfigurationBuilder() {
    }

    public static NodeConfigurationBuilder getInstance() {
        return BUILDER_INSTANCE;
    }

    public Optional<DiscoveryResultBuilder> configure(ThingUID bridgeUID, HomeNode node) {
        DiscoveryResultBuilder discoveryResultBuilder = null;
        try {
            if (node.category() != Category.UNKNOWN) {
                String id = Integer.toString(node.id());
                ThingUID thingUID = new ThingUID(node.category().getThingTypeUID(), bridgeUID, id);
                discoveryResultBuilder = DiscoveryResultBuilder.create(thingUID);
                switch (node.category()) {
                    case BASIC_SHUTTER:
                        BasicShutterConfiguration.configure(discoveryResultBuilder, node);
                        break;
                    // case SHUTTER:
                    // ShutterConfiguration.configure(discoveryResultBuilder, node);
                    // break;
                    default:
                        break;
                }
            }
        } catch (FreeboxException e) {
            logger.warn("Error while requesting data for home things discovery : {}", e.getMessage());
            discoveryResultBuilder = null;
        }
        if (discoveryResultBuilder != null) {
            discoveryResultBuilder.withProperty(ClientConfiguration.ID, node.id()).withLabel(node.label())
                    .withRepresentationProperty(ClientConfiguration.ID).withBridge(bridgeUID);
        }
<<<<<<< Upstream, based on origin/main
        return discoveryResultBuilder;
>>>>>>> e4c7780 Implementing SHUTTER Home Node
=======
        return Optional.ofNullable(discoveryResultBuilder);
>>>>>>> 006a813 Saving work before instroduction of ArrayListDeserializer
    }
}
