/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.internal.handler;

import org.openhab.binding.cul.internal.CULManager;
import org.openhab.binding.cul.internal.CULMode;
import org.openhab.binding.cul.internal.config.MaxCUNBridgeConfiguration;
import org.openhab.binding.cul.internal.network.CULNetworkConfig;
import org.openhab.binding.cul.internal.network.CULNetworkConfigFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Johannes Goehr (johgoe) - Initial contribution
 */
public class CunMaxBridgeHandler extends CulCunBaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(CunMaxBridgeHandler.class);

    private MaxCUNBridgeConfiguration config;

    public CunMaxBridgeHandler(Bridge thing, CULManager manager) {
        super(thing, manager);
    }

    @Override
    public void initialize() {
        config = getConfigAs(MaxCUNBridgeConfiguration.class);
        if (config.networkPath.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Network Path is not configured.");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        culConfig = new CULNetworkConfigFactory().create("network", config.networkPath, CULMode.MAX);
        culConfig = new CULNetworkConfig("network", config.networkPath, CULMode.MAX);
        logger.debug("Schedule connect to cun");
        scheduler.submit(this::connect);
    }
}
