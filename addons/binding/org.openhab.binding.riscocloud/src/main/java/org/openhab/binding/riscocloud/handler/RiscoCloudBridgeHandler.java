/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.riscocloud.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RiscoCloudBridgeHandler} is the handler for RiscoCloud API and connects it
 * to the webservice.
 *
 * @author Sebastien Cantineau - Initial contribution
 *
 */
@NonNullByDefault
public class RiscoCloudBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(RiscoCloudBridgeHandler.class);

    private Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private @Nullable LoginResult loginResult;

    public RiscoCloudBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {

        logger.debug("Initializing RiscoCloud main bridge handler.");
        Configuration config = getThing().getConfiguration();

        loginResult = WebSiteInterface.webSiteLogin(config);

        // Updates the thing status accordingly
        if (loginResult.error == null) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            loginResult.error = loginResult.error.trim();
            logger.debug("Disabling thing '{}': Error '{}': {}", getThing().getUID(), loginResult.error,
                    loginResult.errorDetail);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, loginResult.statusDescr);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // not needed
    }

    @Override
    public void updateStatus(ThingStatus newStatus) {
        super.updateStatus(newStatus);
    }

    public Map<ThingUID, @Nullable ServiceRegistration<?>> getDiscoveryServiceRegs() {
        return discoveryServiceRegs;
    }

    public void setDiscoveryServiceRegs(Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs) {
        this.discoveryServiceRegs = discoveryServiceRegs;
    }

    @Override
    public void handleRemoval() {
        // removes the old registration service associated to the bridge, if existing
        ServiceRegistration<?> dis = this.getDiscoveryServiceRegs().get(this.getThing().getUID());
        logger.debug("handleRemoval() '{}': ServiceRegistration {}", getThing().getUID(), dis);
        if (null != dis) {
            dis.unregister();
        }
        super.handleRemoval();
    }

    public Map<Integer, String> getSiteList() {
        return loginResult == null ? new HashMap<Integer, String>() : loginResult.siteList;
    }

    public ThingUID getID() {
        return getThing().getUID();
    }

    public boolean isValidConfig() {
        return loginResult == null ? false : loginResult.error == null;
    }

}
