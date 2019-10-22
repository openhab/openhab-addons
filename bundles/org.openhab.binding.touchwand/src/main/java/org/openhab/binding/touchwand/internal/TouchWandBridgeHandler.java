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
package org.openhab.binding.touchwand.internal;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TouchWandBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels TouchWand Wanderfull™ Hub channels .
 *
 * @author Roie Geron - Initial contribution
 */

public class TouchWandBridgeHandler extends ConfigStatusBridgeHandler {

    public TouchWandBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    private final Logger logger = LoggerFactory.getLogger(TouchWandBridgeHandler.class);

    private Configuration config;
    private String host;
    private String port; // not used at the moment
    public TouchWandRestClient touchWandClient = new TouchWandRestClient();

    @Override
    public void initialize() {

        updateStatus(ThingStatus.UNKNOWN);

        config = getThing().getConfiguration();

        try {

            InetAddress addr = InetAddress.getByName(config.get(HOST).toString());
            host = config.get(HOST).toString();
            port = config.get(PORT).toString();
        } catch (UnknownHostException e) {
            logger.warn("Bridge IP/PORT config is not set or not valid");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        scheduler.execute(() -> {
            boolean thingReachable = false;
            String password = config.get(PASS).toString();
            String username = config.get(USER).toString();
            thingReachable = touchWandClient.connect(username, password, host, port);
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        logger.debug("Finished initializing!");
    }

    @Override
    public @NonNull Collection<@NonNull ConfigStatusMessage> getConfigStatus() {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        // TODO Auto-generated method stub

    }

}
