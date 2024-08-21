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
package org.openhab.binding.neato.internal.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.neato.internal.NeatoBindingConstants;
import org.openhab.binding.neato.internal.NeatoHandlerFactory;
import org.openhab.binding.neato.internal.classes.Robot;
import org.openhab.binding.neato.internal.handler.NeatoAccountHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NeatoAccountDiscoveryService} is responsible for starting the discovery procedure
 * that connects to Neato Web and imports all registered vacuum cleaners.
 *
 * @author Patrik Wimnell - Initial contribution
 * @author Jeff Lauterbach - Start discovery service from bridge
 */
public class NeatoAccountDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(NeatoAccountDiscoveryService.class);

    private static final int TIMEOUT = 15;

    private final NeatoAccountHandler handler;
    private final ThingUID bridgeUID;

    private ScheduledFuture<?> scanTask;

    public NeatoAccountDiscoveryService(NeatoAccountHandler handler) {
        super(NeatoHandlerFactory.DISCOVERABLE_THING_TYPE_UIDS, TIMEOUT);
        this.handler = handler;
        this.bridgeUID = handler.getThing().getUID();
    }

    private void findRobots() {
        List<Robot> robots = handler.getRobotsFromNeato();

        for (Robot robot : robots) {
            addThing(robot);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        findRobots();
    }

    @Override
    protected void startScan() {
        if (this.scanTask != null) {
            scanTask.cancel(true);
        }
        this.scanTask = scheduler.schedule(this::findRobots, 0, TimeUnit.SECONDS);
    }

    @Override
    protected void stopScan() {
        super.stopScan();

        if (this.scanTask != null) {
            this.scanTask.cancel(true);
            this.scanTask = null;
        }
    }

    private void addThing(Robot robot) {
        if (robot == null || !robot.discoveryInformationPresent()) {
            return;
        }

        logger.debug("addThing(): Adding new Neato unit ({}) to the inbox", robot.getName());

        Map<String, Object> properties = new HashMap<>();
        ThingUID thingUID = new ThingUID(NeatoBindingConstants.THING_TYPE_VACUUMCLEANER, bridgeUID, robot.getSerial());
        properties.put(NeatoBindingConstants.CONFIG_SECRET, robot.getSecretKey());
        properties.put(NeatoBindingConstants.CONFIG_SERIAL, robot.getSerial());
        properties.put(Thing.PROPERTY_MODEL_ID, robot.getModel());
        properties.put(NeatoBindingConstants.PROPERTY_NAME, robot.getName());

        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withLabel(robot.getName()).withBridge(bridgeUID)
                .withProperties(properties).build());
    }
}
