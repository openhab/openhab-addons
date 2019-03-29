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
package org.openhab.binding.netatmo.internal;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.openhab.binding.netatmo.internal.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.internal.handler.NetatmoHomeHandler;
import org.openhab.binding.netatmo.internal.handler.NetatmoRoomHandler;
import org.openhab.binding.netatmo.internal.discovery.NetatmoModuleDiscoveryService;
import org.openhab.binding.netatmo.internal.energy.RelayHandler;
import org.openhab.binding.netatmo.internal.energy.ThermosthatHandler;
import org.openhab.binding.netatmo.internal.energy.ValveHandler;
import org.openhab.binding.netatmo.internal.homecoach.NAHealthyHomeCoachHandler;
import org.openhab.binding.netatmo.internal.station.*;
import org.openhab.binding.netatmo.internal.webhook.WelcomeWebHookServlet;
import org.openhab.binding.netatmo.internal.welcome.NAWelcomeCameraHandler;
import org.openhab.binding.netatmo.internal.welcome.NAWelcomeHomeHandler;
import org.openhab.binding.netatmo.internal.welcome.NAWelcomePersonHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

/**
 * The {@link NetatmoHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */

@Component(service = { ThingHandlerFactory.class,
        NetatmoHandlerFactory.class }, immediate = true, configurationPid = "binding.netatmo")
public class NetatmoHandlerFactory extends BaseThingHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(NetatmoHandlerFactory.class);
    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private Map<ThingUID, ServiceRegistration<?>> webHookServiceRegs = new HashMap<>();
    private HttpService httpService;
    private ThermosthatStateDescriptionProvider stateDescriptionProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        ThingType type = getThingTypeByUID(thingTypeUID);
        logger.error("Supports {} {}", thingTypeUID, type);
        return (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID));
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.error("Insatnciate {} {}", thingTypeUID, ROOM);
        if (thingTypeUID.equals(APIBRIDGE_THING_TYPE)) {
            WelcomeWebHookServlet servlet = registerWebHookServlet(thing.getUID());
            NetatmoBridgeHandler bridgeHandler = new NetatmoBridgeHandler((Bridge) thing, servlet);
            registerDeviceDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (thingTypeUID.equals(OUTDOORMODULE)) {
            return new NAModule1Handler(thing);
        } else if (thingTypeUID.equals(WINDMODULE)) {
            return new NAModule2Handler(thing);
        } else if (thingTypeUID.equals(RAINGAUGEMODULE)) {
            return new NAModule3Handler(thing);
        } else if (thingTypeUID.equals(INDOORMODULE)) {
            return new NAModule4Handler(thing);
        } else if (thingTypeUID.equals(BASESTATION)) {
            return new NAMainHandler(thing);
        } else if (thingTypeUID.equals(HOMECOACH)) {
            return new NAHealthyHomeCoachHandler(thing);
        } else if (thingTypeUID.equals(VALVE)) {
            return new ValveHandler(thing);
        } else if (thingTypeUID.equals(RELAY)) {
            return new RelayHandler(thing);
        } else if (thingTypeUID.equals(THERMOSTAT)) {
            return new ThermosthatHandler(thing, stateDescriptionProvider);
        } else if (thingTypeUID.equals(WELCOME_HOME_THING_TYPE)) {
            return new NAWelcomeHomeHandler(thing);
        } else if (thingTypeUID.equals(WELCOME_CAMERA) || thingTypeUID.equals(PRESENCE_CAMERA)) {
            return new NAWelcomeCameraHandler(thing);
        } else if (thingTypeUID.equals(WELCOME_PERSON_THING_TYPE)) {
            return new NAWelcomePersonHandler(thing);
        } else if (thingTypeUID.equals(HOME)) {
            return new NetatmoHomeHandler(thing);
        } else if (thingTypeUID.equals(ROOM)) {
            return new NetatmoRoomHandler(thing);
        } else {
            logger.warn("ThingHandler not found for {}", thing.getThingTypeUID());
            return null;
        }
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof NetatmoBridgeHandler) {
            ThingUID thingUID = thingHandler.getThing().getUID();
            unregisterDeviceDiscoveryService(thingUID);
            unregisterWebHookServlet(thingUID);
        }
        super.removeHandler(thingHandler);
    }

    private void registerDeviceDiscoveryService(@NonNull NetatmoBridgeHandler netatmoBridgeHandler) {
        if (bundleContext != null) {
            NetatmoModuleDiscoveryService discoveryService = new NetatmoModuleDiscoveryService(netatmoBridgeHandler);
            discoveryService.activate(null);
            discoveryServiceRegs.put(netatmoBridgeHandler.getThing().getUID(), bundleContext.registerService(
                    DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
        }
    }

    private void unregisterDeviceDiscoveryService(ThingUID thingUID) {
        ServiceRegistration<?> serviceReg = discoveryServiceRegs.get(thingUID);
        if (serviceReg != null) {
            NetatmoModuleDiscoveryService service = (NetatmoModuleDiscoveryService) bundleContext
                    .getService(serviceReg.getReference());
            service.deactivate();
            serviceReg.unregister();
            discoveryServiceRegs.remove(thingUID);
        }
    }

    private WelcomeWebHookServlet registerWebHookServlet(ThingUID thingUID) {
        WelcomeWebHookServlet servlet = null;
        if (bundleContext != null) {
            servlet = new WelcomeWebHookServlet(httpService, thingUID.getId());
            webHookServiceRegs.put(thingUID, bundleContext.registerService(HttpServlet.class.getName(), servlet,
                    new Hashtable<String, Object>()));
        }
        return servlet;
    }

    private void unregisterWebHookServlet(ThingUID thingUID) {
        ServiceRegistration<?> serviceReg = webHookServiceRegs.get(thingUID);
        if (serviceReg != null) {
            serviceReg.unregister();
            webHookServiceRegs.remove(thingUID);
        }
    }

    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    @Reference
    protected void setDynamicStateDescriptionProvider(ThermosthatStateDescriptionProvider provider) {
        this.stateDescriptionProvider = provider;
    }

    protected void unsetDynamicStateDescriptionProvider(ThermosthatStateDescriptionProvider provider) {
        this.stateDescriptionProvider = null;
    }

}
