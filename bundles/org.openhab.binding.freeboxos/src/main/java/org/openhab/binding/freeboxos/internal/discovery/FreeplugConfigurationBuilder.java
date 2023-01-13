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
package org.openhab.binding.freeboxos.internal.discovery;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.THING_TYPE_FREEPLUG;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.freeplug.Freeplug;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        String mac = plug.getId();
        ThingUID thingUID = new ThingUID(THING_TYPE_FREEPLUG, bridgeUID, macToUid(mac));
        logger.debug("Adding new Freeplug {} to inbox", thingUID);
        return DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withLabel("Freeplug " + macToUid(mac))
                .withProperty(Thing.PROPERTY_MAC_ADDRESS, mac).withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS);
    }

    private String macToUid(String mac) {
        return mac.replaceAll("[^A-Za-z0-9_]", "");
    }
}
