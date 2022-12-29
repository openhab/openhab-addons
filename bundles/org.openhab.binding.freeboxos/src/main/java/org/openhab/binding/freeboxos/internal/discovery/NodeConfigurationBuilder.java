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
package org.openhab.binding.freeboxos.internal.discovery;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.home.HomeNode;
import org.openhab.binding.freeboxos.internal.config.BasicShutterConfiguration;
import org.openhab.binding.freeboxos.internal.config.ClientConfiguration;
import org.openhab.binding.freeboxos.internal.config.ShutterConfiguration;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NodeConfigurationBuilder} is responsible for holding configuration informations associated to a Freebox
 * Home
 * thing type
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

    public @Nullable DiscoveryResultBuilder configure(ThingUID bridgeUID, HomeNode node) {
        DiscoveryResultBuilder discoveryResultBuilder = null;
        try {
            switch (node.getCategory()) {
                case "basic_shutter":
                    ThingUID basicShutterUID = new ThingUID(THING_TYPE_HOME_BASIC_SHUTTER, bridgeUID,
                            Integer.toString(node.getId()));
                    discoveryResultBuilder = DiscoveryResultBuilder.create(basicShutterUID);
                    BasicShutterConfiguration.configure(discoveryResultBuilder, node);
                    break;
                case "shutter":
                    ThingUID shutterUID = new ThingUID(THING_TYPE_HOME_SHUTTER, bridgeUID,
                            Integer.toString(node.getId()));
                    discoveryResultBuilder = DiscoveryResultBuilder.create(shutterUID);
                    ShutterConfiguration.configure(discoveryResultBuilder, node);
                    break;
                default:
                    break;
            }
        } catch (FreeboxException e) {
            logger.warn("Error while requesting data for home things discovery : {}", e.getMessage());
            discoveryResultBuilder = null;
        }
        if (discoveryResultBuilder != null) {
            discoveryResultBuilder.withProperty(ClientConfiguration.ID, node.getId());
        }
        return discoveryResultBuilder;
    }
}
