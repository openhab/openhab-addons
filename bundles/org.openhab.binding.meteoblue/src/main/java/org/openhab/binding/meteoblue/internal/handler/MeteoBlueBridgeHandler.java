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
package org.openhab.binding.meteoblue.internal.handler;

import static org.openhab.binding.meteoblue.internal.MeteoBlueBindingConstants.THING_TYPE_BRIDGE;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.meteoblue.internal.MeteoBlueBridgeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MeteoBlueBridgeHandler} is responsible for handling the
 * bridge things created to use the meteoblue weather service.
 *
 * @author Chris Carman - Initial contribution
 */
public class MeteoBlueBridgeHandler extends BaseBridgeHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    private final Logger logger = LoggerFactory.getLogger(MeteoBlueBridgeHandler.class);

    private String apiKey;
    private boolean isValidConfig;

    public MeteoBlueBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    /**
     * Initialize the bridge.
     */
    @Override
    public void initialize() {
        logger.debug("Initializing meteoblue bridge");

        MeteoBlueBridgeConfig config = getConfigAs(MeteoBlueBridgeConfig.class);
        String apiKeyTemp = config.getApiKey();
        if (StringUtils.isBlank(apiKeyTemp)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Cannot initialize meteoblue bridge. No apiKey provided.");
            return;
        }

        apiKey = apiKeyTemp;

        healthCheck();
    }

    /**
     * No commands are supported here.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public String getApiKey() {
        return new String(apiKey);
    }

    private void healthCheck() {
        String url = "http://my.meteoblue.com/packages/";
        try {
            String httpResponse = HttpUtil.executeUrl("GET", url, 30 * 1000);
            logger.trace("HealthCheck succeeded.");
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            logger.trace("HealthCheck failed", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "HealthCheck failed");
        }
    }
}
