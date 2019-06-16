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
 * The {@link RdsCloudHandler} is the OpenHab Handler for Siemens RDS cloud 
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * 
*/
public class RdsCloudHandler extends BaseBridgeHandler {

    private static final Logger LOGGER = 
            LoggerFactory.getLogger(RdsCloudHandler.class);

    private RdsConfiguration config;
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
        String msg;

        config = getConfigAs(RdsConfiguration.class);

        if (config == null) {
            msg = "missing configuration, status => offline!";
            LOGGER.error(msg);
            updateStatus(ThingStatus.OFFLINE, 
                ThingStatusDetail.CONFIGURATION_PENDING, msg);
            return;
        }

        if (config.userEmail.isEmpty()) {
            msg = "missing email address, status => offline!";
            LOGGER.error(msg);
            updateStatus(ThingStatus.OFFLINE, 
                ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        }

        if (config.userPassword.isEmpty()) {
            msg = "missing password, status => offline!";
            LOGGER.error(msg);
            updateStatus(ThingStatus.OFFLINE, 
                ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        }

        if (LOGGER.isDebugEnabled()) 
            LOGGER.debug("polling interval={}", config.pollInterval);

        if (config.pollInterval < FAST_POLL_INTERVAL || 
            config.pollInterval > LAZY_POLL_INTERVAL) {
            msg = String.format(
                    "polling interval out of range [%d..%d], status => offline!",
                    FAST_POLL_INTERVAL, LAZY_POLL_INTERVAL);
            LOGGER.error(msg);
            updateStatus(ThingStatus.OFFLINE, 
                ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        }
        
        refreshToken();
    }
    
    
    @Override
    public void dispose() {
        // there is nothing to do 
    }

    
    /*
     * public method:
     * used by RDS smart thermostat handlers
     * return the polling interval (seconds)
     */
    public int getPollInterval() {
        return (config != null ? config.pollInterval : -1);
    }

    
    /*
     * private method:
     * check if the current token is valid, and renew it if necessary
     */
    private synchronized void refreshToken() {
        String msg;
        
        if (accessToken == null || accessToken.isExpired()) {
            accessToken = 
                RdsAccessToken.create(config.userEmail, config.userPassword);
        }

        if (accessToken != null ) { 
            if (getThing().getStatus() != ThingStatus.ONLINE) {   
                msg = "server responded, status => online.."; 
                LOGGER.info(msg);
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, msg);
            }
        } else { 
            if (getThing().getStatus() == ThingStatus.ONLINE) {   
                msg = "server authentication error, status => offline!"; 
                LOGGER.error(msg);
                updateStatus(ThingStatus.OFFLINE, 
                    ThingStatusDetail.CONFIGURATION_ERROR, msg);
            }
        }
    }


    /*
     * public method:
     * used by RDS smart thermostat handlers to fetch the current token
     */
    public synchronized String getToken() {
        refreshToken();
        return (accessToken != null ? accessToken.getToken() : "");
    }
}
