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
package org.openhab.binding.sunsa.internal;

import static java.util.Objects.requireNonNull;
import static org.openhab.binding.sunsa.internal.SunsaBindingConstants.THING_TYPE_BRIDGE;
import static org.openhab.binding.sunsa.internal.SunsaBindingConstants.THING_TYPE_DEVICE;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sunsa.internal.bridge.SunsaCloudBridgeHandler;
import org.openhab.binding.sunsa.internal.device.SunsaDeviceHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * The {@link SunsaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author jirom - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.sunsa", service = ThingHandlerFactory.class)
public class SunsaHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE, THING_TYPE_DEVICE);

    private final List<ServiceRegistration<?>> serviceRegistrations;
    private final ClientBuilder clientBuilder;
    private final ObjectMapper objectMapper;

    @Activate
    public SunsaHandlerFactory(@Reference ClientBuilder clientBuilder) {
        this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(Include.NON_EMPTY);
        this.clientBuilder = requireNonNull(clientBuilder).register(new JacksonJsonProvider(objectMapper));
        this.serviceRegistrations = new ArrayList<>();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_DEVICE.equals(thingTypeUID)) {
            return new SunsaDeviceHandler(thing);
        } else if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            final SunsaCloudBridgeHandler bridgeHandler = new SunsaCloudBridgeHandler((Bridge) thing, clientBuilder);
            final DiscoveryService discoveryService = new SunsaDiscoveryService(bridgeHandler.getSunsaService(),
                    thing.getUID());
            registerService(discoveryService, DiscoveryService.class);
            return bridgeHandler;
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        super.removeHandler(thingHandler);

        if (thingHandler instanceof SunsaCloudBridgeHandler) {
            unregisterServices();
        }
    }

    private synchronized <T> void registerService(final T service, final Class<T> clazz) {
        serviceRegistrations.add(bundleContext.registerService(clazz.getName(), service, new Hashtable<>()));
    }

    private synchronized void unregisterServices() {
        serviceRegistrations.stream().map(ServiceRegistration::getReference).forEach(bundleContext::ungetService);
    }
}
