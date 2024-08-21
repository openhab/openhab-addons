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
package org.openhab.binding.nikobus.internal.discovery;

import static org.openhab.binding.nikobus.internal.NikobusBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nikobus.internal.handler.NikobusPcLinkHandler;
import org.openhab.binding.nikobus.internal.utils.Utils;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikobusDiscoveryService} discovers push button things for Nikobus switches.
 * Buttons are not discovered via scan but only when physical button is pressed and a new
 * nikobus push button bus address is detected.
 *
 * @author Boris Krivonog - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = NikobusDiscoveryService.class)
@NonNullByDefault
public class NikobusDiscoveryService extends AbstractThingHandlerDiscoveryService<NikobusPcLinkHandler> {
    private final Logger logger = LoggerFactory.getLogger(NikobusDiscoveryService.class);

    public NikobusDiscoveryService() throws IllegalArgumentException {
        super(NikobusPcLinkHandler.class, Set.of(THING_TYPE_PUSH_BUTTON), 0);
    }

    @Override
    protected void startScan() {
    }

    @Override
    protected void stopBackgroundDiscovery() {
        thingHandler.resetUnhandledCommandProcessor();
    }

    @Override
    protected void startBackgroundDiscovery() {
        thingHandler.setUnhandledCommandProcessor(this::process);
    }

    private void process(String command) {
        if (command.length() <= 2 || !command.startsWith("#N")) {
            logger.debug("Ignoring command() '{}'", command);
        }

        String address = command.substring(2);
        logger.debug("Received address = '{}'", address);

        ThingUID thingUID = new ThingUID(THING_TYPE_PUSH_BUTTON, thingHandler.getThing().getUID(), address);

        Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIG_ADDRESS, address);

        String humanReadableNikobusAddress = Utils.convertToHumanReadableNikobusAddress(address).toUpperCase();
        logger.debug("Detected Nikobus Push Button: '{}'", humanReadableNikobusAddress);
        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_PUSH_BUTTON)
                .withLabel("Nikobus Push Button " + humanReadableNikobusAddress).withProperties(properties)
                .withRepresentationProperty(CONFIG_ADDRESS).withBridge(thingHandler.getThing().getUID()).build());
    }
}
