/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.discovery;

import java.util.Collections;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Craig Hamilton
 *
 */
public class OmnilinkBridgeDiscoveryService extends AbstractDiscoveryService {
    private static final Logger logger = LoggerFactory.getLogger(OmnilinkBridgeDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;

    public OmnilinkBridgeDiscoveryService() {
        super(Collections.singleton(new ThingTypeUID(OmnilinkBindingConstants.BINDING_ID, "-")),
                DISCOVER_TIMEOUT_SECONDS, false);
    }

    @Override
    protected void startScan() {
        logger.debug("start scan called for omnilink bridge");

    }
}