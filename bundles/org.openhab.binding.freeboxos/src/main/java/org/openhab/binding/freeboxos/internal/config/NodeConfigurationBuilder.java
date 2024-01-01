/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.Category;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.HomeNode;
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
    }
}
