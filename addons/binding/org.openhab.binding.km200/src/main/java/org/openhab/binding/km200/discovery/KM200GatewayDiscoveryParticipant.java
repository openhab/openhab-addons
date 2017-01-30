/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.km200.discovery;

import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.km200.KM200BindingConstants;
import org.openhab.binding.km200.handler.KM200GatewayHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KM200GatewayDiscoveryParticipant} class discovers gateways and adds the results to the inbox.
 *
 * @author Markus Eckhardt
 *
 */
public class KM200GatewayDiscoveryParticipant extends AbstractDiscoveryService
        implements KM200GatewayDiscoveryListener {

    private Logger logger = LoggerFactory.getLogger(KM200GatewayDiscoveryParticipant.class);

    private static final int TIMEOUT = 300;

    private KM200GatewayDiscovery gatewayDiscovery;

    public KM200GatewayDiscoveryParticipant() {
        super(KM200GatewayHandler.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, true);
        gatewayDiscovery = new KM200GatewayDiscovery(TIMEOUT);
        gatewayDiscovery.addListener(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return KM200GatewayHandler.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public void startScan() {
        logger.debug("StartScan executed");
        gatewayDiscovery.startDiscovery();
    }

    @Override
    public void gatewayDiscoveryFinished() {
    }

    @Override
    public void gatewayDiscovered(KM200GatewayDiscoveryResult result) {
        logger.trace("Adding gateway mit IP: {}", result.getIP());

        ThingUID uid = new ThingUID(KM200BindingConstants.THING_TYPE_KMDEVICE, "kmdevice");
        if (uid != null) {
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                    .withProperty("ip4Address", result.getIP().getHostAddress())
                    .withRepresentationProperty(result.getIP().getHostAddress())
                    .withLabel("KM50/100/200 Gateway (" + result.getIP().getHostAddress() + ")").build();
            thingDiscovered(discoveryResult);
        }
    }
}
