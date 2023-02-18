/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.THING_TYPE_BRIDGE;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.touchwand.internal.config.TouchwandBridgeConfiguration;
import org.openhab.binding.touchwand.internal.discovery.TouchWandUnitDiscoveryService;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitData;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TouchWandBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels TouchWand Wanderfull™ Hub channels .
 *
 * @author Roie Geron - Initial contribution
 */
@NonNullByDefault
public class TouchWandBridgeHandler extends BaseBridgeHandler implements TouchWandUnitStatusUpdateListener {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    private final Logger logger = LoggerFactory.getLogger(TouchWandBridgeHandler.class);
    private boolean addSecondaryUnits;
    private @Nullable TouchWandWebSockets touchWandWebSockets;
    private Map<String, TouchWandUnitUpdateListener> unitUpdateListeners = new ConcurrentHashMap<>();
    private volatile boolean isRunning = false;

    public TouchWandRestClient touchWandClient;

    public TouchWandBridgeHandler(Bridge bridge, HttpClient httpClient, BundleContext bundleContext) {
        super(bridge);
        touchWandClient = new TouchWandRestClient(httpClient);
        touchWandWebSockets = null;
    }

    @Override
    public synchronized void initialize() {
        String host;
        Integer port;
        TouchwandBridgeConfiguration config;

        updateStatus(ThingStatus.UNKNOWN);

        config = getConfigAs(TouchwandBridgeConfiguration.class);

        host = config.ipAddress;
        port = config.port;
        addSecondaryUnits = config.addSecondaryUnits;

        isRunning = true;

        scheduler.execute(() -> {
            boolean thingReachable = false;
            String password = config.password;
            String username = config.username;
            thingReachable = touchWandClient.connect(username, password, host, port.toString());
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
                synchronized (this) {
                    if (isRunning) {
                        TouchWandWebSockets localSockets = touchWandWebSockets = new TouchWandWebSockets(host, port,
                                scheduler);
                        localSockets.registerListener(this);
                        localSockets.connect();
                    }
                }

            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public boolean isAddSecondaryControllerUnits() {
        return addSecondaryUnits;
    }

    @Override
    public synchronized void dispose() {
        isRunning = false;
        TouchWandWebSockets myTouchWandWebSockets = touchWandWebSockets;
        if (myTouchWandWebSockets != null) {
            myTouchWandWebSockets.unregisterListener(this);
            myTouchWandWebSockets.dispose();
        }
    }

    public synchronized boolean registerUpdateListener(TouchWandUnitUpdateListener listener) {
        logger.debug("Adding Status update listener for device {}", listener.getId());
        unitUpdateListeners.put(listener.getId(), listener);
        return true;
    }

    public synchronized boolean unregisterUpdateListener(TouchWandUnitUpdateListener listener) {
        logger.debug("Remove Status update listener for device {}", listener.getId());
        unitUpdateListeners.remove(listener.getId());
        return true;
    }

    @Override
    public void onDataReceived(TouchWandUnitData unitData) {
        if (unitUpdateListeners.containsKey(unitData.getId().toString())) {
            TouchWandUnitUpdateListener updateListener = unitUpdateListeners.get(unitData.getId().toString());
            updateListener.onItemStatusUpdate(unitData);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(TouchWandUnitDiscoveryService.class);
    }
}
