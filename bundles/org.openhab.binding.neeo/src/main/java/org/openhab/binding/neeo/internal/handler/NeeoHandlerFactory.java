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
package org.openhab.binding.neeo.internal.handler;

import java.util.Hashtable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.neeo.internal.NeeoConstants;
import org.openhab.binding.neeo.internal.discovery.NeeoDeviceDiscoveryService;
import org.openhab.binding.neeo.internal.discovery.NeeoRoomDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
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
    private final HttpService httpService;

    /** The {@link NetworkAddressService} used for ip lookup */
    private final NetworkAddressService networkAddressService;

    /** The {@link ClientBuilder} used in HttpRequest */
    private final ClientBuilder clientBuilder;

    /** The discovery services created by this class (one per room and one for each device) */
    private final ConcurrentMap<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new ConcurrentHashMap<>();

    @Activate
    public NeeoHandlerFactory(@Reference HttpService httpService,
            @Reference NetworkAddressService networkAddressService, @Reference ClientBuilder clientBuilder) {
        this.httpService = httpService;
        this.networkAddressService = networkAddressService;
        this.clientBuilder = clientBuilder;
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
            final int port = HttpServiceUtil.getHttpServicePort(this.bundleContext);
            final NeeoBrainHandler handler = new NeeoBrainHandler((Bridge) thing,
                    port < 0 ? NeeoConstants.DEFAULT_BRAIN_HTTP_PORT : port, httpService, networkAddressService,
                    clientBuilder);
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
        this.discoveryServiceRegs.put(handler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    /**
     * Helper method to register the device discovery service
     *
     * @param handler a non-null room handler
     */
    private void registerDeviceDiscoveryService(NeeoRoomHandler handler) {
        Objects.requireNonNull(handler, "handler cannot be null");
        final NeeoDeviceDiscoveryService discoveryService = new NeeoDeviceDiscoveryService(handler);
        this.discoveryServiceRegs.put(handler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        final ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
        if (serviceReg != null) {
            serviceReg.unregister();
        }
    }
}
