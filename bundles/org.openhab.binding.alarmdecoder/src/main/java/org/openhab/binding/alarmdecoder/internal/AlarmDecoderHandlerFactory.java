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
package org.openhab.binding.alarmdecoder.internal;

import static org.openhab.binding.alarmdecoder.internal.AlarmDecoderBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.alarmdecoder.internal.handler.ADBridgeHandler;
import org.openhab.binding.alarmdecoder.internal.handler.IPBridgeHandler;
import org.openhab.binding.alarmdecoder.internal.handler.KeypadHandler;
import org.openhab.binding.alarmdecoder.internal.handler.LRRHandler;
import org.openhab.binding.alarmdecoder.internal.handler.RFZoneHandler;
import org.openhab.binding.alarmdecoder.internal.handler.SerialBridgeHandler;
import org.openhab.binding.alarmdecoder.internal.handler.VZoneHandler;
import org.openhab.binding.alarmdecoder.internal.handler.ZoneHandler;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AlarmDecoderHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.alarmdecoder", service = ThingHandlerFactory.class)
public class AlarmDecoderHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_IPBRIDGE, THING_TYPE_SERIALBRIDGE, THING_TYPE_ZONE, THING_TYPE_RFZONE,
                    THING_TYPE_VZONE, THING_TYPE_KEYPAD, THING_TYPE_LRR).collect(Collectors.toSet()));

    private final Logger logger = LoggerFactory.getLogger(AlarmDecoderHandlerFactory.class);

    private final SerialPortManager serialPortManager;

    @Activate
    public AlarmDecoderHandlerFactory(final @Reference SerialPortManager serialPortManager) {
        // Obtain the serial port manager service using an OSGi reference
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegMap = new HashMap<>();
    // Marked as Nullable only to fix incorrect redundant null check complaints from null annotations

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_IPBRIDGE.equals(thingTypeUID)) {
            IPBridgeHandler bridgeHandler = new IPBridgeHandler((Bridge) thing);
            registerDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (THING_TYPE_SERIALBRIDGE.equals(thingTypeUID)) {
            SerialBridgeHandler bridgeHandler = new SerialBridgeHandler((Bridge) thing, serialPortManager);
            registerDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (THING_TYPE_ZONE.equals(thingTypeUID)) {
            return new ZoneHandler(thing);
        } else if (THING_TYPE_RFZONE.equals(thingTypeUID)) {
            return new RFZoneHandler(thing);
        } else if (THING_TYPE_VZONE.equals(thingTypeUID)) {
            return new VZoneHandler(thing);
        } else if (THING_TYPE_KEYPAD.equals(thingTypeUID)) {
            return new KeypadHandler(thing);
        } else if (THING_TYPE_LRR.equals(thingTypeUID)) {
            return new LRRHandler(thing);
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof ADBridgeHandler) {
            ServiceRegistration<?> serviceReg = discoveryServiceRegMap.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                logger.debug("Unregistering discovery service.");
                serviceReg.unregister();
            }
        }
    }

    /**
     * Register a discovery service for a bridge handler.
     *
     * @param bridgeHandler bridge handler for which to register the discovery service
     */
    private synchronized void registerDiscoveryService(ADBridgeHandler bridgeHandler) {
        logger.debug("Registering discovery service.");
        AlarmDecoderDiscoveryService discoveryService = new AlarmDecoderDiscoveryService(bridgeHandler);
        bridgeHandler.setDiscoveryService(discoveryService);
        discoveryServiceRegMap.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, null));
    }
}
