/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.cbus.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.openhab.binding.cbus.internal.CBusGroupConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daveoxley.cbus.Application;
import com.daveoxley.cbus.CGateException;
import com.daveoxley.cbus.Group;
import com.daveoxley.cbus.Network;

/**
 * The {@link CBusGroupHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Linton - Initial contribution
 */

@NonNullByDefault
public abstract class CBusGroupHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(CBusGroupHandler.class);
    protected @Nullable CBusNetworkHandler cBusNetworkHandler;
    protected @Nullable Group group;
    protected int applicationId = -1;
    protected int groupId = -1;

    public CBusGroupHandler(Thing thing, int applicationId) {
        super(thing);
        this.applicationId = applicationId;
    }

    @Override
    public abstract void handleCommand(ChannelUID channelUID, Command command);

    @Override
    public void initialize() {
        /*
         * Cast to Nullable in map to avoid compiler warnings
         */
        CBusGroupConfiguration configuration = getConfigAs(CBusGroupConfiguration.class);
        logger.debug("Using configuration {}", configuration);
        groupId = configuration.group;

        CBusNetworkHandler cBusNetworkHandler = getCBusNetworkHandler();
        this.cBusNetworkHandler = cBusNetworkHandler;
        if (cBusNetworkHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No CBusNetworkHandler Bridge available");
            return;
        }
        updateStatus();

        Map<String, String> updatedProperties = editProperties();
        updatedProperties.put(CBusBindingConstants.PROPERTY_APPLICATION_ID, Integer.toString(applicationId));
        updatedProperties.put(CBusBindingConstants.PROPERTY_NETWORK_ID,
                Integer.toString(cBusNetworkHandler.getNetworkId()));
        updateProperties(updatedProperties);
    }

    public void updateStatus() {
        try {
            logger.debug("updateStatus UID: {} applicaton: {} group: {}", getThing().getUID(), applicationId, groupId);
            CBusNetworkHandler networkHandler = cBusNetworkHandler;
            if (networkHandler == null || !networkHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            } else {
                Group group = this.group;
                if (group == null) {
                    group = getGroup();
                    this.group = group;
                }
                if (group == null) {
                    logger.debug("Set state to configuration error -no group");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "No Group object available");
                } else if (group.getNetwork().isOnline()) {
                    updateStatus(ThingStatus.ONLINE);

                    try {
                        Map<String, String> updatedProperties = editProperties();
                        updatedProperties.put(CBusBindingConstants.PROPERTY_GROUP_NAME, group.getName());
                        updateProperties(updatedProperties);
                    } catch (CGateException ignore) {
                        // Cant get name so properties wont be updated
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Network is not reporting online");
                }
            }
        } catch (CGateException e) {
            logger.debug("Problem checking network state for network {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    public abstract void updateGroup(int application, int group, String value);

    private @Nullable Group getGroup() {
        try {
            CBusNetworkHandler networkHandler = cBusNetworkHandler;
            if (networkHandler == null) {
                return null;
            }
            Network network = networkHandler.getNetwork();
            if (network != null) {
                Application application = network.getApplication(applicationId);
                if (application == null) {
                    logger.debug("getGroup() Cant get application for id {}", applicationId);
                    return null;
                }
                logger.debug("GetGroup for {}/id {}", applicationId, groupId);
                return application.getGroup(groupId);
            }
        } catch (CGateException e) {
            logger.debug("GetGroup for id {}/{} failed {}", applicationId, groupId, e.getMessage());
        }
        return null;
    }

    private @Nullable CBusNetworkHandler getCBusNetworkHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Required bridge not defined for device .");
            return null;
        }
        ThingHandler handler = bridge.getHandler();
        if (handler instanceof CBusNetworkHandler) {
            return (CBusNetworkHandler) handler;
        }
        logger.debug("No available bridge handler found for bridge: {}", bridge.getUID());
        return null;
    }
}
