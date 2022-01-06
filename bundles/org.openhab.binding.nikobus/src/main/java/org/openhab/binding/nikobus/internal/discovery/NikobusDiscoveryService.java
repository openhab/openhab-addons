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
package org.openhab.binding.nikobus.internal.discovery;

import static org.openhab.binding.nikobus.internal.NikobusBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikobus.internal.handler.NikobusPcLinkHandler;
import org.openhab.binding.nikobus.internal.utils.Utils;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikobusDiscoveryService} discovers push button things for Nikobus switches.
 * Buttons are not discovered via scan but only when physical button is pressed and a new
 * nikobus push button bus address is detected.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class NikobusDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(NikobusDiscoveryService.class);
    private @Nullable NikobusPcLinkHandler bridgeHandler;

    public NikobusDiscoveryService() throws IllegalArgumentException {
        super(Collections.singleton(THING_TYPE_PUSH_BUTTON), 0);
    }

    @Override
    protected void startScan() {
    }

    @Override
    protected void stopBackgroundDiscovery() {
        NikobusPcLinkHandler handler = bridgeHandler;
        if (handler != null) {
            handler.resetUnhandledCommandProcessor();
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        NikobusPcLinkHandler handler = bridgeHandler;
        if (handler != null) {
            handler.setUnhandledCommandProcessor(this::process);
        }
    }

    private void process(String command) {
        if (command.length() <= 2 || !command.startsWith("#N")) {
            logger.debug("Ignoring command() '{}'", command);
        }

        String address = command.substring(2);
        logger.debug("Received address = '{}'", address);

        NikobusPcLinkHandler handler = bridgeHandler;
        if (handler != null) {
            ThingUID thingUID = new ThingUID(THING_TYPE_PUSH_BUTTON, handler.getThing().getUID(), address);

            Map<String, Object> properties = new HashMap<>();
            properties.put(CONFIG_ADDRESS, address);

            String humanReadableNikobusAddress = Utils.convertToHumanReadableNikobusAddress(address).toUpperCase();
            logger.debug("Detected Nikobus Push Button: '{}'", humanReadableNikobusAddress);
            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_PUSH_BUTTON)
                    .withLabel("Nikobus Push Button " + humanReadableNikobusAddress).withProperties(properties)
                    .withRepresentationProperty(CONFIG_ADDRESS).withBridge(handler.getThing().getUID()).build());
        }
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof NikobusPcLinkHandler) {
            bridgeHandler = (NikobusPcLinkHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }
}
