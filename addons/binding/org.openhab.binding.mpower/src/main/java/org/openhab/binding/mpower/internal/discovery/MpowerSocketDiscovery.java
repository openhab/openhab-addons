/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mpower.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.mpower.MpowerBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An discovery service which allows to discover mPower sockets. Its only triggered manually once a new mPower has been
 * discovered.
 *
 * @author magcode
 *
 */
public class MpowerSocketDiscovery extends AbstractDiscoveryService {
    public MpowerSocketDiscovery(int timeout) throws IllegalArgumentException {
        super(timeout);
    }

    public MpowerSocketDiscovery() throws IllegalArgumentException {
        super(MpowerBindingConstants.SUPPORTED_SOCKET_THING_TYPES_UIDS, 60);
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void startScan() {
        logger.trace("No need to scan.");
    }

    public void onDeviceAddedInternal(ThingUID bridge, String bridgeLabel, int socketNumber) {
        String label = bridgeLabel + " Socket " + socketNumber;
        ThingUID socketUID = new ThingUID(MpowerBindingConstants.THING_TYPE_SOCKET,
                bridge.getId() + "_" + socketNumber);

        Map<String, Object> properties = new HashMap<>(2, 1);
        properties.put(MpowerBindingConstants.SOCKET_NUMBER_PROP_NAME, socketNumber);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(socketUID).withBridge(bridge).withLabel(label)
                .withProperties(properties).build();

        thingDiscovered(discoveryResult);

    }

    public void activate() {
        super.activate(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deactivate() {
        super.deactivate();
    }
}