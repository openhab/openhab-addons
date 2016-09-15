/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.edimax.discovery;

import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.edimax.EdimaxBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdimaxDiscoveryParticipant extends AbstractDiscoveryService implements EdimaxDiscoveryListener {

    private static final int TIMEOUT = 15;
    private EdimaxDiscoverer disco = null;

    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public EdimaxDiscoveryParticipant() {
        super(EdimaxBindingConstants.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, true);
        disco = new EdimaxDiscoverer(TIMEOUT);
        disco.addListener(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return EdimaxBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startScan() {
        logger.debug("StartScan called");
        disco.startDiscovery();
    }

    @Override
    public void smartplugDiscovered(EdimaxDiscoveryResult result) {
        // TODO Auto-generated method stub

    }

    @Override
    public void discoveryFinished() {
        // TODO Auto-generated method stub

    }
}
