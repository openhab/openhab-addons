/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.draytonwiser.internal.discovery;

import static org.openhab.binding.draytonwiser.DraytonWiserBindingConstants.BINDING_ID;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.draytonwiser.handler.HeatHubHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link DraytonWiserDiscoveryService} is used to discover devices that are connected to a Heat Hub.
 *
 * @author Andrew Schofield - Initial contribution
 */
public class DraytonWiserDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(DraytonWiserDiscoveryService.class);

    private HeatHubHandler bridgeHandler;

    public DraytonWiserDiscoveryService(HeatHubHandler bridgeHandler) {
        super(ImmutableSet.of(new ThingTypeUID(BINDING_ID, "-")), 30, false);
        this.bridgeHandler = bridgeHandler;
    }

    /**
     * Called on component activation.
     */
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
    }

    @Override
    public synchronized void stopScan() {
        super.stopScan();
    }
}
