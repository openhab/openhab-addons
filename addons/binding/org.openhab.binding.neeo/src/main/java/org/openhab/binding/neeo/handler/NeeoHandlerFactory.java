/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.handler;

import static org.openhab.binding.neeo.NeeoConstants.BRIDGE_TYPE_BRAIN;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.net.HttpServiceUtil;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.neeo.NeeoConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

/**
 * The {@link NeeoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tim Roberts - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true)
public class NeeoHandlerFactory extends BaseThingHandlerFactory {

    /** The {@link HttpService} used to register callbacks */
    @NonNullByDefault({})
    private HttpService httpService;

    /** The {@link NetworkAddressService} used for ip lookup */
    @NonNullByDefault({})
    private NetworkAddressService networkAddressService;

    /** The {@link DiscoveryServiceRegistry} used to initiate discovery */
    @NonNullByDefault({})
    private DiscoveryServiceRegistry discoveryServiceRegistry;

    /** The scheduler used to schedule tasks */
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(NeeoConstants.THREADPOOL_ID);

    /**
     * Sets the {@link HttpService}.
     *
     * @param httpService the non-null {@link HttpService} to use
     */
    @Reference
    protected void setHttpService(HttpService httpService) {
        Objects.requireNonNull(httpService, "httpService cannot be null");
        this.httpService = httpService;
    }

    /**
     * Unsets the {@link HttpService}
     *
     * @param httpService the {@link HttpService} (not used in this implementation)
     */
    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    /**
     * Sets the {@link NetworkAddressService}.
     *
     * @param networkAddressService the non-null {@link NetworkAddressService} to use
     */
    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        Objects.requireNonNull(networkAddressService, "networkAddressService cannot be null");
        this.networkAddressService = networkAddressService;
    }

    /**
     * Unsets the {@link HttpService}
     *
     * @param networkAddressService the {@link HttpService} (not used in this implementation)
     */
    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
    }

    /**
     * Sets the {@link NetworkAddressService}.
     *
     * @param discoveryServiceRegistry the non-null {@link NetworkAddressService} to use
     */
    @Reference
    protected void setDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        Objects.requireNonNull(discoveryServiceRegistry, "discoveryServiceRegistry cannot be null");
        this.discoveryServiceRegistry = discoveryServiceRegistry;
    }

    /**
     * Unsets the {@link HttpService}
     *
     * @param discoveryServiceRegistry the {@link HttpService} (not used in this implementation)
     */
    protected void unsetDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        Objects.requireNonNull(thingTypeUID, "thingTypeUID cannot be null");

        return NeeoConstants.BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    @Nullable
    @Override
    protected ThingHandler createHandler(Thing thing) {
        Objects.requireNonNull(thing, "thing cannot be null");
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(BRIDGE_TYPE_BRAIN)) {
            final HttpService localHttpService = httpService;
            final NetworkAddressService localNetworkAddressService = networkAddressService;

            Objects.requireNonNull(localHttpService, "HttpService cannot be null");
            Objects.requireNonNull(localNetworkAddressService, "networkAddressService cannot be null");

            final int port = HttpServiceUtil.getHttpServicePort(this.bundleContext);

            startScan(NeeoConstants.SCAN_ROOMS); // starts scan for rooms
            return new NeeoBrainHandler((Bridge) thing, port < 0 ? NeeoConstants.DEFAULT_BRAIN_HTTP_PORT : port,
                    localHttpService, localNetworkAddressService);
        } else if (thingTypeUID.getId().startsWith("room")) {
            startScan(NeeoConstants.SCAN_DEVICES); // starts scan for devices
            return new NeeoRoomHandler((Bridge) thing);
        } else if (thingTypeUID.getId().startsWith("device")) {
            return new NeeoDeviceHandler(thing);
        }

        return null;
    }

    /**
     * Helper method to start a new discovery scan for NEEO after a specified number of milliseconds (gives time for the
     * system to create the thing properly).
     *
     * @param waitMilliSeconds a >=0 milliseconds to schedule the scan
     */
    private void startScan(int waitMilliSeconds) {
        if (waitMilliSeconds < 0) {
            throw new IllegalArgumentException("waitMilliSeconds cannot be below 0: " + waitMilliSeconds);
        }
        final DiscoveryServiceRegistry localRegistry = discoveryServiceRegistry;

        if (localRegistry != null) {
            scheduler.schedule(() -> {
                localRegistry.startScan(NeeoConstants.BINDING_ID, null);
            }, waitMilliSeconds, TimeUnit.MILLISECONDS);
        }
    }
}
