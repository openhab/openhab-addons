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
package org.openhab.binding.opensprinkler.internal.handler;

import static org.openhab.binding.opensprinkler.internal.OpenSprinklerBindingConstants.DEFAULT_REFRESH_RATE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApi;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApiFactory;
import org.openhab.binding.opensprinkler.internal.config.OpenSprinklerPiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Florian Schmidt - Refactoring
 */
@NonNullByDefault
public class OpenSprinklerPiBridgeHandler extends OpenSprinklerBaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(OpenSprinklerPiBridgeHandler.class);

    @Nullable
    private OpenSprinklerPiConfig openSprinklerConfig;
    private OpenSprinklerApiFactory apiFactory;

    public OpenSprinklerPiBridgeHandler(Bridge bridge, OpenSprinklerApiFactory apiFactory) {
        super(bridge);
        this.apiFactory = apiFactory;
    }

    @Override
    public void initialize() {
        OpenSprinklerPiConfig openSprinklerConfig = getConfig().as(OpenSprinklerPiConfig.class);
        this.openSprinklerConfig = openSprinklerConfig;

        logger.debug("Initializing OpenSprinkler with config (Refresh: {}).", openSprinklerConfig.refresh);

        OpenSprinklerApi openSprinklerDevice;
        try {
            openSprinklerDevice = apiFactory.getGpioApi(openSprinklerConfig.stations);
            this.openSprinklerDevice = openSprinklerDevice;
        } catch (Exception exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not create API connection to the OpenSprinkler device. Error received: " + exp);

            return;
        }

        logger.debug("Successfully created API connection to the OpenSprinkler device.");

        try {
            openSprinklerDevice.enterManualMode();
        } catch (Exception exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not open API connection to the OpenSprinkler device. Error received: " + exp);
        }

        if (openSprinklerDevice.isManualModeEnabled()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not initialize the connection to the OpenSprinkler.");

            return;
        }

        super.initialize();
    }

    @Override
    protected long getRefreshInterval() {
        OpenSprinklerPiConfig openSprinklerConfig = this.openSprinklerConfig;
        if (openSprinklerConfig == null) {
            return DEFAULT_REFRESH_RATE;
        }
        return openSprinklerConfig.refresh;
    }

}
