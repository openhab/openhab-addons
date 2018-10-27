/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nuki.internal;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.net.HttpServiceUtil;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.nuki.NukiBindingConstants;
import org.openhab.binding.nuki.handler.NukiBridgeHandler;
import org.openhab.binding.nuki.handler.NukiSmartLockHandler;
import org.openhab.binding.nuki.internal.dataexchange.NukiApiServlet;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NukiHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Markus Katter - Initial contribution
 */
@Component(service = { ThingHandlerFactory.class, NukiHandlerFactory.class }, configurationPid = "binding.nuki")
public class NukiHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(NukiHandlerFactory.class);

    private HttpService httpService;
    private HttpClient httpClient;
    private NetworkAddressService networkAddressService;
    private Map<ThingUID, ServiceRegistration<NukiApiServlet>> nukiApiServletRegs = new ConcurrentHashMap<>();
    private String openHabIpAndPort;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return NukiBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        logger.debug("NukiHandlerFactory:createHandler({})", thing);
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (NukiBindingConstants.THING_TYPE_BRIDGE_UIDS.contains(thingTypeUID)) {
            NukiApiServlet nukiApiServlet = new NukiApiServlet(httpService);
            @SuppressWarnings("unchecked")
            ServiceRegistration<NukiApiServlet> reg = (ServiceRegistration<NukiApiServlet>) bundleContext
                    .registerService(HttpServlet.class.getName(), nukiApiServlet, new Hashtable<String, Object>());
            nukiApiServletRegs.put(thing.getUID(), reg);
            openHabIpAndPort = getOpenHabIpAndPort();
            return new NukiBridgeHandler((Bridge) thing, httpClient, nukiApiServlet, openHabIpAndPort);
        } else if (NukiBindingConstants.THING_TYPE_SMARTLOCK_UIDS.contains(thingTypeUID)) {
            return new NukiSmartLockHandler(thing);
        }
        logger.trace("No valid Handler found for Thing[{}]!", thingTypeUID);
        return null;
    }

    @Override
    public void unregisterHandler(Thing thing) {
        super.unregisterHandler(thing);
        logger.trace("NukiHandlerFactory:unregisterHandler({})", thing);
        ServiceRegistration<NukiApiServlet> reg = nukiApiServletRegs.get(thing.getUID());
        if (reg != null) {
            logger.trace("Unregistering NukiApiServlet for Thing[{}])", thing.getUID());
            reg.unregister();
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
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }

    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
    }

    private String getOpenHabIpAndPort() {
        logger.trace("NukiHandlerFactory:getOpenHabIpAndPort()");
        if (openHabIpAndPort != null) {
            return openHabIpAndPort;
        }
        final String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
        if (ipAddress == null) {
            logger.warn("No network interface could be found.");
            return null;
        }
        // we do not use SSL as it can cause certificate validation issues.
        final int port = HttpServiceUtil.getHttpServicePort(bundleContext);
        if (port == -1) {
            logger.warn("Cannot find port of the http service.");
            return null;
        }
        openHabIpAndPort = ipAddress + ":" + port;
        logger.trace("openHabIpAndPort[{}]", openHabIpAndPort);
        return openHabIpAndPort;
    }

}
