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
import org.openhab.binding.freeboxos.internal.api.rest.PhoneManager.Status;
import org.openhab.binding.freeboxos.internal.api.rest.PhoneManager.Type;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PhoneConfigurationBuilder} is responsible for holding configuration informations associated the phone
 * lines (DECT and FXS / landline)
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PhoneConfigurationBuilder {
    private static final PhoneConfigurationBuilder BUILDER_INSTANCE = new PhoneConfigurationBuilder();

    private final Logger logger = LoggerFactory.getLogger(PhoneConfigurationBuilder.class);

    private PhoneConfigurationBuilder() {
    }

    public static PhoneConfigurationBuilder getInstance() {
        return BUILDER_INSTANCE;
    }

    public DiscoveryResultBuilder configure(ThingUID bridgeUID, Status config) {
        ThingUID thingUID = new ThingUID(Type.DECT.equals(config.type()) ? THING_TYPE_DECT : THING_TYPE_FXS, bridgeUID,
                Integer.toString(config.id()));

        logger.debug("Adding new Freebox Phone {} to inbox", thingUID);

        return DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperty(ClientConfiguration.ID, config.id()).withLabel(config.type().name())
                .withRepresentationProperty(ClientConfiguration.ID);
    }
}
