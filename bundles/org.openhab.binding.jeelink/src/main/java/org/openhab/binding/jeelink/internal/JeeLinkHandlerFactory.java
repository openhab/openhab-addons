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
package org.openhab.binding.jeelink.internal;

import static org.openhab.binding.jeelink.internal.JeeLinkBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.jeelink.internal.discovery.SensorDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JeeLinkHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Volker Bier - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.jeelink")
public class JeeLinkHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(JeeLinkHandlerFactory.class);

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final SerialPortManager serialPortManager;

    @Activate
    public JeeLinkHandlerFactory(final @Reference SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUid) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUid);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUid = thing.getThingTypeUID();
        ThingHandler handler = null;

        if (thingTypeUid.equals(JEELINK_USB_STICK_THING_TYPE) || thingTypeUid.equals(JEELINK_TCP_STICK_THING_TYPE)
                || thingTypeUid.equals(LGW_TCP_STICK_THING_TYPE) || thingTypeUid.equals(LGW_USB_STICK_THING_TYPE)) {
            logger.debug("creating JeeLinkHandler for thing {}...", thing.getUID().getId());

            handler = new JeeLinkHandler((Bridge) thing, serialPortManager);
            registerSensorDiscoveryService((JeeLinkHandler) handler);
        } else {
            handler = SensorDefinition.createHandler(thingTypeUid, thing);

            if (handler == null) {
                logger.debug("skipping creation of unknown handler for thing {} with type {}...",
                        thing.getUID().getId(), thing.getThingTypeUID().getId());
            }
        }

        return handler;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof JeeLinkHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
            }
        }
    }

    private synchronized void registerSensorDiscoveryService(JeeLinkHandler bridgeHandler) {
        logger.debug("registering sensor discovery service...");
        SensorDiscoveryService discoveryService = new SensorDiscoveryService(bridgeHandler);
        discoveryServiceRegs.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }
}
