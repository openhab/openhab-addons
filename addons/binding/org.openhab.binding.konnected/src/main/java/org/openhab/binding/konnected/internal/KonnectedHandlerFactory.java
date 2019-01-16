/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.konnected.internal;

import static org.openhab.binding.konnected.internal.KonnectedBindingConstants.*;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.net.HttpServiceUtil;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.konnected.internal.handler.KonnectedHandler;
import org.openhab.binding.konnected.internal.servlet.KonnectedHTTPServlet;
import org.openhab.binding.konnected.internal.servlet.KonnectedWebHookFail;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KonnectedHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@Component(configurationPid = "binding.konnected", service = ThingHandlerFactory.class)
public class KonnectedHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(KonnectedHandlerFactory.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_MODULE);
    private HttpService httpService;
    private String callbackUrl = null;
    private NetworkAddressService networkAddressService;
    private KonnectedHTTPServlet servlet;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Dictionary<String, Object> properties = componentContext.getProperties();
        callbackUrl = (String) properties.get("callbackUrl");
        KonnectedHTTPServlet servlet = registerWebHookServlet();
        this.servlet = servlet;
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
        servlet.deactivate();
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        KonnectedHandler thingHandler = new KonnectedHandler(thing, '/' + BINDING_ID, createCallbackUrl(),
                createCallbackPort());
        logger.debug("Adding thinghandler for thing {} to webhook.", thing.getUID().getId());
        try {
            servlet.add(thingHandler);
        } catch (KonnectedWebHookFail e) {
            logger.trace("there was an error adding the thing handler to the servlet: {}", e.getMessage());
        }
        return thingHandler;
    }

    /**
     * @param thingHandler thing handler to be removed
     */
    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        servlet.remove((KonnectedHandler) thingHandler);
        thingHandler.dispose();
        super.removeHandler(thingHandler);
    }

    private KonnectedHTTPServlet registerWebHookServlet() {
        KonnectedHTTPServlet servlet = null;
        String configCallBack = '/' + BINDING_ID;
        servlet = new KonnectedHTTPServlet(httpService, configCallBack);
        return servlet;
    }

    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    private String createCallbackUrl() {
        if (callbackUrl != null) {
            logger.debug("The callback ip address from the OSGI is:{}", callbackUrl);
            return callbackUrl;
        } else {
            final String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
            if (ipAddress == null) {
                logger.warn("No network interface could be found.");
                return null;
            }
            logger.debug("The callback ip address obtained from the Network Address Service was:{}", ipAddress);
            return ipAddress;
        }
    }

    private String createCallbackPort() {
        // we do not use SSL as it can cause certificate validation issues.
        final int port = HttpServiceUtil.getHttpServicePort(bundleContext);
        if (port == -1) {
            logger.warn("Cannot find port of the http service.");
            return null;
        }
        logger.debug("the port for the callback is: {}", port);
        return Integer.toString(port);
    }

    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
    }
}
