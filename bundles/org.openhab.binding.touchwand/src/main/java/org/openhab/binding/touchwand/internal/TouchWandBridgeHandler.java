/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.touchwand.internal.discovery.TouchWandUnitDiscoveryService;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitData;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
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
    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = Collections
            .synchronizedMap(new HashMap<>());

    private int statusRefreshRateSec;
    private boolean addSecondaryUnits;
    private BundleContext bundleContext;
    private @Nullable TouchWandWebSockets touchWandWebSockets;
    private Map<String, TouchWandUnitUpdateListener> unitUpdateListeners = new ConcurrentHashMap<>();

    public TouchWandRestClient touchWandClient;

    public TouchWandBridgeHandler(Bridge bridge, HttpClient httpClient, BundleContext bundleContext) {
        super(bridge);
        touchWandClient = new TouchWandRestClient(httpClient);
        this.bundleContext = bundleContext;
        touchWandWebSockets = null;
    }

    @Override
    public void initialize() {
        String host;
        String port;
        Configuration config;

        updateStatus(ThingStatus.UNKNOWN);
        config = getThing().getConfiguration();

        host = config.get(HOST).toString();
        port = config.get(PORT).toString();
        statusRefreshRateSec = Integer.parseInt((config.get(STATUS_REFRESH_TIME).toString()));
        addSecondaryUnits = Boolean.valueOf(config.get(ADD_SECONDARY_UNITS).toString());

        scheduler.execute(() -> {
            boolean thingReachable = false;
            String password = config.get(PASS).toString();
            String username = config.get(USER).toString();
            thingReachable = touchWandClient.connect(username, password, host, port);
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
                registerItemDiscoveryService(this);
                TouchWandWebSockets localSockets = touchWandWebSockets = new TouchWandWebSockets(host, scheduler);
                localSockets.registerListener(this);
                localSockets.connect();
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

    public int getStatusRefreshTime() {
        return statusRefreshRateSec;
    }

    private synchronized void registerItemDiscoveryService(TouchWandBridgeHandler bridgeHandler) {
        TouchWandUnitDiscoveryService discoveryService = new TouchWandUnitDiscoveryService(bridgeHandler);
        discoveryService.registerListener(this); // Register for Unit Status updates as well
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    public void dispose() {
        ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(this.getThing().getUID());
        if (serviceReg != null) {
            // remove discovery service
            TouchWandUnitDiscoveryService service = (TouchWandUnitDiscoveryService) bundleContext
                    .getService(serviceReg.getReference());
            service.unregisterListener(this); // Unregister Unit status polling
            serviceReg.unregister();
            service.deactivate();
        }

        if (touchWandWebSockets != null) {
            touchWandWebSockets.unregisterListener(this);
            touchWandWebSockets.dispose();
        }
    }

    public synchronized boolean registerUpdateListener(TouchWandUnitUpdateListener listener) {
        logger.debug("Adding Status update listener for device {}", listener.getId());

        return unitUpdateListeners.put(listener.getId(), listener) == null;
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
}
