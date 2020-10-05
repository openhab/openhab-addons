/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.wled.internal;

import static org.openhab.binding.wled.internal.WLedBindingConstants.*;

import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WLedDiscoveryService} Discovers and adds any Wled devices found.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class, immediate = true)
public class WLedDiscoveryService implements MDNSDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(WLedDiscoveryService.class);

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        String name = service.getName().toLowerCase();
        if (!name.equals("wled")) {
            return null;
        }
        String address[] = service.getURLs();
        if ((address == null) || address.length < 1) {
            logger.debug("WLED discovered with empty IP address-{}", service);
            return null;
        }
        logger.info("WLED discovered at {}", address[0]);
        ThingTypeUID thingtypeuid = new ThingTypeUID("wled", "wled");
        ThingUID thingUID = new ThingUID(thingtypeuid,
                address[0].substring(7, address[0].length() - 3).replace(".", "-"));
        return DiscoveryResultBuilder.create(thingUID).withProperty(CONFIG_ADDRESS, address[0])
                .withLabel("WLED @" + address[0]).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        return null;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public String getServiceType() {
        return "_http._tcp.local.";
    }
}
