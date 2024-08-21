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

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.rest.FreeplugManager.Freeplug;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import inet.ipaddr.mac.MACAddress;

/**
 * The {@link FreeplugConfigurationBuilder} is responsible for holding configuration informations associated to a
 * Freeplug
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FreeplugConfigurationBuilder {
    private static final FreeplugConfigurationBuilder BUILDER_INSTANCE = new FreeplugConfigurationBuilder();

    private final Logger logger = LoggerFactory.getLogger(FreeplugConfigurationBuilder.class);

    private FreeplugConfigurationBuilder() {
    }

    public static FreeplugConfigurationBuilder getInstance() {
        return BUILDER_INSTANCE;
    }

    public DiscoveryResultBuilder configure(ThingUID bridgeUID, Freeplug plug) {
        MACAddress mac = plug.id();
        String uid = mac.toHexString(false);
        ThingUID thingUID = new ThingUID(THING_TYPE_FREEPLUG, bridgeUID, uid);

        logger.debug("Adding new {} {} to inbox", THING_FREEPLUG, thingUID);

        return DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS)
                .withLabel("%s (%s) %s".formatted(THING_FREEPLUG, plug.netRole().name(), uid))
                .withProperty(Thing.PROPERTY_MAC_ADDRESS, mac.toColonDelimitedString());
    }
}
