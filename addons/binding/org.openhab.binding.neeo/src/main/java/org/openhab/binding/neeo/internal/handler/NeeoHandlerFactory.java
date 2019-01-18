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
package org.openhab.binding.neeo.internal.handler;

import java.util.Hashtable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.net.HttpServiceUtil;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.neeo.internal.NeeoConstants;
import org.openhab.binding.neeo.internal.discovery.NeeoDeviceDiscoveryService;
import org.openhab.binding.neeo.internal.discovery.NeeoRoomDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

/**
 * The {@link NeeoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.neeo")
public class NeeoHandlerFactory extends BaseThingHandlerFactory {

    /** The {@link HttpService} used to register callbacks */
    @NonNullByDefault({})
    private HttpService httpService;

    /** The {@link NetworkAddressService} used for ip lookup */
    @NonNullByDefault({})
    private NetworkAddressService networkAddressService;

    /** The discovery services created by this class (one per room and one for each device) */
    private final ConcurrentMap<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new ConcurrentHashMap<>();

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
     * Unsets the {@link NetworkAddressService}
     *
     * @param networkAddressService the {@link NetworkAddressService} (not used in this implementation)
     */
    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
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

        if (thingTypeUID.equals(NeeoConstants.BRIDGE_TYPE_BRAIN)) {
            final HttpService localHttpService = httpService;
            final NetworkAddressService localNetworkAddressService = networkAddressService;

            Objects.requireNonNull(localHttpService, "HttpService cannot be null");
            Objects.requireNonNull(localNetworkAddressService, "networkAddressService cannot be null");

            final int port = HttpServiceUtil.getHttpServicePort(this.bundleContext);

            final NeeoBrainHandler handler = new NeeoBrainHandler((Bridge) thing,
                    port < 0 ? NeeoConstants.DEFAULT_BRAIN_HTTP_PORT : port, localHttpService,
                    localNetworkAddressService);
            registerRoomDiscoveryService(handler);
            return handler;
        } else if (thingTypeUID.equals(NeeoConstants.BRIDGE_TYPE_ROOM)) {
            final NeeoRoomHandler handler = new NeeoRoomHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        } else if (thingTypeUID.equals(NeeoConstants.THING_TYPE_DEVICE)) {
            return new NeeoDeviceHandler(thing);
        }

        return null;
    }

    /**
     * Helper method to register the room discovery service
     *
     * @param handler a non-null brain handler
     */
    private void registerRoomDiscoveryService(NeeoBrainHandler handler) {
        Objects.requireNonNull(handler, "handler cannot be null");
        final NeeoRoomDiscoveryService discoveryService = new NeeoRoomDiscoveryService(handler);
        this.discoveryServiceRegs.put(handler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    /**
     * Helper method to register the device discovery service
     *
     * @param handler a non-null room handler
     */
    private void registerDeviceDiscoveryService(NeeoRoomHandler handler) {
        Objects.requireNonNull(handler, "handler cannot be null");
        final NeeoDeviceDiscoveryService discoveryService = new NeeoDeviceDiscoveryService(handler);
        this.discoveryServiceRegs.put(handler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        final ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
        if (serviceReg != null) {
            serviceReg.unregister();
        }
    }
}
