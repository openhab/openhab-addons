/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tellstick.internal.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tellstick.internal.TellstickBindingConstants;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tellstick.device.TellstickController;

/**
 * The {@link TellstickBridgeDiscovery} is responsible for discovering new Telldus gateway devices on the network
 *
 * @author Jarle Hjortland - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.tellstick")
public class TellstickBridgeDiscovery extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(TellstickBridgeDiscovery.class);

    static boolean discoveryRunning = false;
    static boolean initilized = false;

    public TellstickBridgeDiscovery() {
        super(TellstickBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS, 15);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return TellstickBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS;
    }

    @Override
    public void startScan() {
        discoverController();
    }

    @Override
    protected void startBackgroundDiscovery() {
        discoverController();
    }

    @Override
    public boolean isBackgroundDiscoveryEnabled() {
        return true;
    }

    private synchronized void discoverController() {
        if (!discoveryRunning) {
            discoveryRunning = true;
            listBridge();
        }
    }

    private void listBridge() {
        try {
            List<TellstickController> cntrls = TellstickController.getControllers();
            for (TellstickController contrl : cntrls) {
                discoveryResultSubmission(contrl);
            }
        } catch (UnsatisfiedLinkError e) {
            logger.error(
                    "Could not load telldus core, please make sure Telldus is installed and correct 32/64 bit java.",
                    e);
        } catch (NoClassDefFoundError e) {
            logger.error(
                    "Could not load telldus core, please make sure Telldus is installed and correct 32/64 bit java.",
                    e);
        } finally {
            // Close the port!
            discoveryRunning = false;
        }
    }

    private void discoveryResultSubmission(TellstickController controller) {
        if (controller != null && controller.isOnline()) {
            logger.trace("Adding new Telldus Controller  {}", controller);
            Map<String, Object> properties = new HashMap<>(2);
            ThingUID uid = new ThingUID(TellstickBindingConstants.TELLDUSCOREBRIDGE_THING_TYPE,
                    Integer.toString(controller.getId()));
            thingDiscovered(DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(controller.getType().name() + ": " + controller.getName()).build());
        }
    }
}
