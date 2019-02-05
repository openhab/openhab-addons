/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.neato.internal.NeatoBindingConstants;
import org.openhab.binding.neato.internal.NeatoHandlerFactory;
import org.openhab.binding.neato.internal.classes.Robot;
import org.openhab.binding.neato.internal.handler.NeatoAccountHandler;
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

    private NeatoAccountHandler handler;
    private ThingUID bridgeUID;

    private ScheduledFuture<?> scanTask;

    public NeatoAccountDiscoveryService(NeatoAccountHandler handler) {
        super(NeatoHandlerFactory.DISCOVERABLE_THING_TYPE_UIDS, TIMEOUT);
        this.handler = handler;
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
        this.scanTask = scheduler.schedule(() -> findRobots(), 0, TimeUnit.SECONDS);
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
        logger.debug("addThing(): Adding new Neato unit {} to the smarthome inbox", robot.getName());

        Map<String, Object> properties = new HashMap<>();
        ThingUID thingUID = new ThingUID(NeatoBindingConstants.THING_TYPE_VACUUMCLEANER, robot.getSerial());
        properties.put(NeatoBindingConstants.CONFIG_SECRET, robot.getSecretKey());
        properties.put(NeatoBindingConstants.CONFIG_SERIAL, robot.getSerial());
        properties.put(Thing.PROPERTY_MODEL_ID, robot.getModel());
        properties.put(NeatoBindingConstants.PROPERTY_NAME, robot.getName());

        thingDiscovered(
                DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withProperties(properties).build());
    }

}
