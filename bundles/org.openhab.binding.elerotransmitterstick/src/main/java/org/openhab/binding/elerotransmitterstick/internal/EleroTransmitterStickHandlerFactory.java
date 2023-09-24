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
package org.openhab.binding.elerotransmitterstick.internal;

import static org.openhab.binding.elerotransmitterstick.internal.EleroTransmitterStickBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.elerotransmitterstick.internal.discovery.EleroChannelDiscoveryService;
import org.openhab.binding.elerotransmitterstick.internal.handler.EleroChannelHandler;
import org.openhab.binding.elerotransmitterstick.internal.handler.EleroTransmitterStickHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.serial.SerialPortManager;
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

/**
 * The {@link EleroTransmitterStickHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Volker Bier - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.elerotransmitterstick")
public class EleroTransmitterStickHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_STICK, THING_TYPE_ELERO_CHANNEL).collect(Collectors.toSet()));

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private final SerialPortManager serialPortManager;

    @Activate
    public EleroTransmitterStickHandlerFactory(final @Reference SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_STICK)) {
            EleroTransmitterStickHandler bridgeHandler = new EleroTransmitterStickHandler((Bridge) thing,
                    serialPortManager);

            EleroChannelDiscoveryService discoveryService = new EleroChannelDiscoveryService(bridgeHandler);
            discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                    .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));

            return bridgeHandler;
        } else if (thingTypeUID.equals(THING_TYPE_ELERO_CHANNEL)) {
            return new EleroChannelHandler(thing);
        }

        return null;
    }
}
