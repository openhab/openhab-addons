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
package org.openhab.binding.foobot.internal.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.foobot.internal.FoobotApiException;
import org.openhab.binding.foobot.internal.FoobotBindingConstants;
import org.openhab.binding.foobot.internal.FoobotHandlerFactory;
import org.openhab.binding.foobot.internal.handler.FoobotAccountHandler;
import org.openhab.binding.foobot.internal.handler.FoobotDeviceHandler;
import org.openhab.binding.foobot.internal.json.FoobotDevice;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FoobotAccountDiscoveryService} is responsible for starting the discovery procedure
 * that retrieves Foobot account and imports all registered Foobot devices.
 *
 * @author George Katsis - Initial contribution
 * @author Hilbrand Bouwkamp - Completed implementation
 */
@NonNullByDefault
public class FoobotAccountDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private static final int TIMEOUT_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(FoobotAccountDiscoveryService.class);

    private @Nullable FoobotAccountHandler handler;
    private @NonNullByDefault({}) ThingUID bridgeUID;

    public FoobotAccountDiscoveryService() {
        super(FoobotHandlerFactory.DISCOVERABLE_THING_TYPE_UIDS, TIMEOUT_SECONDS, false);
    }

    @Override
    protected void startScan() {
        scheduler.execute(this::retrieveFoobots);
    }

    private void retrieveFoobots() {
        if (handler == null) {
            return;
        }
        try {
            final List<FoobotDeviceHandler> footbotHandlers = handler.getFootbotHandlers();

            handler.getDeviceList().stream()
                    .filter(d -> !footbotHandlers.stream().anyMatch(h -> h.getUuid().equals(d.getUuid())))
                    .forEach(this::addThing);
        } catch (final FoobotApiException e) {
            logger.debug("Footbot Api connection failed: {}", e.getMessage(), e);
            logger.warn("Discovering new footbot devices failed: {}", e.getMessage());
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    private void addThing(final FoobotDevice foobot) {
        logger.debug("Adding new Foobot '{}' with uuid: {}", foobot.getName(), foobot.getUuid());

        final ThingUID thingUID = new ThingUID(FoobotBindingConstants.THING_TYPE_FOOBOT, bridgeUID, foobot.getUuid());
        final Map<String, Object> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, foobot.getUuid());
        properties.put(FoobotBindingConstants.CONFIG_UUID, foobot.getUuid());
        properties.put(Thing.PROPERTY_MAC_ADDRESS, foobot.getMac());
        properties.put(FoobotBindingConstants.PROPERTY_NAME, foobot.getName());

        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withProperties(properties)
                .withLabel(foobot.getName()).withRepresentationProperty(foobot.getUuid()).build());
    }

    @Override
    public void setThingHandler(@Nullable final ThingHandler handler) {
        if (handler instanceof FoobotAccountHandler accountHandler) {
            this.handler = accountHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
