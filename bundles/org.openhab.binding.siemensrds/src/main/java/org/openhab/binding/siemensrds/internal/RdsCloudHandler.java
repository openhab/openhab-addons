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
package org.openhab.binding.siemensrds.internal;

import static org.openhab.binding.siemensrds.internal.RdsBindingConstants.*;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RdsCloudHandler} is the handler for Siemens RDS cloud account (
 * also known as the Climatix IC server account )
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
public class RdsCloudHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(RdsCloudHandler.class);

    private RdsCloudConfiguration config;
    private RdsAccessToken accessToken;

    public RdsCloudHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // there is nothing to do
    }

    @Override
    public void initialize() {
        config = getConfigAs(RdsCloudConfiguration.class);

        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "missing configuration, status => offline!");
            return;
        }

        if (config.userEmail.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "missing email address, status => offline!");
            return;
        }

        if (config.userPassword.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "missing password, status => offline!");
            return;
        }

        if (logger.isDebugEnabled())
            logger.debug("polling interval={}", config.pollingInterval);

        if (config.pollingInterval < FAST_POLL_INTERVAL || config.pollingInterval > LAZY_POLL_INTERVAL) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    String.format("polling interval out of range [%d..%d], status => offline!", FAST_POLL_INTERVAL,
                            LAZY_POLL_INTERVAL));
            return;
        }

        refreshToken();
    }

    @Override
    public void dispose() {
        // there is nothing to do
    }

    /*
     * public method: used by RDS smart thermostat handlers return the polling
     * interval (seconds)
     */
    public int getPollInterval() {
        return (config != null ? config.pollingInterval : -1);
    }

    /*
     * private method: check if the current token is valid, and renew it if
     * necessary
     */
    private synchronized void refreshToken() {
        if (accessToken == null || accessToken.isExpired()) {
            accessToken = RdsAccessToken.create(config.apiKey, config.userEmail, config.userPassword);
        }

        if (accessToken != null) {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "server responded, status => online..");
            }
        } else {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "server authentication error, status => offline!");
            }
        }
    }

    /*
     * public method: used by RDS smart thermostat handlers to fetch the current
     * token
     */
    public synchronized String getToken() {
        refreshToken();
        return (accessToken != null ? accessToken.getToken() : "");
    }

    public String getApiKey() {
        return (config != null ? config.apiKey : "");
    }
}
