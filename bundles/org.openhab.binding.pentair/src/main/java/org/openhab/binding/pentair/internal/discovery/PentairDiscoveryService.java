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
package org.openhab.binding.pentair.internal.discovery;

import static org.openhab.binding.pentair.internal.PentairBindingConstants.*;

import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pentair.internal.handler.PentairBaseBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairDiscoveryService} handles discovery of devices as they are identified by the bridge handler.
 * Requests from the framework to startScan() are ignored, since no active scanning is possible.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class PentairDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES = Set.of(CONTROLLER_THING_TYPE,
            INTELLIFLO_THING_TYPE, INTELLICHLOR_THING_TYPE, INTELLICHEM_THING_TYPE);

    private final Logger logger = LoggerFactory.getLogger(PentairDiscoveryService.class);
    private @Nullable PentairBaseBridgeHandler bridgeHandler;

    public PentairDiscoveryService() throws IllegalArgumentException {
        super(DISCOVERABLE_THING_TYPES, 0, false);
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        // Ignore start scan requests
    }

    public void notifyDiscoveredThing(ThingTypeUID thingTypeUID, int id, String label) {
        PentairBaseBridgeHandler bridgeHandler = Objects.requireNonNull(this.bridgeHandler,
                "Discovery with null bridge handler.");
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, label);

        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperty(PARAMETER_ID, id).withRepresentationProperty(PARAMETER_ID).withLabel(label).build();
        thingDiscovered(result);
        logger.debug("Discovered Thing {}", thingUID);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof PentairBaseBridgeHandler baseBridgeHandler) {
            this.bridgeHandler = baseBridgeHandler;
            baseBridgeHandler.setDiscoveryService(this);
        } else {
            this.bridgeHandler = null;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.bridgeHandler;
    }
}
