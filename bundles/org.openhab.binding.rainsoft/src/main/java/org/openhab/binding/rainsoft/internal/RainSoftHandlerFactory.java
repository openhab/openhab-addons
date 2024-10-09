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
package org.openhab.binding.rainsoft.internal;

import static org.openhab.binding.rainsoft.RainSoftBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rainsoft.handler.AccountHandler;
import org.openhab.binding.rainsoft.handler.WCSHandler;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RainSoftHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Ben Rosenblum - Initial contribution
 */

@Component(service = { ThingHandlerFactory.class,
        RainSoftHandlerFactory.class }, immediate = true, configurationPid = "binding.rainsoft")
public class RainSoftHandlerFactory extends BaseThingHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(RainSoftHandlerFactory.class);
    private final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS;

    private NetworkAddressService networkAddressService;

    @Nullable
    private HttpService httpService;
    private int httpPort;
    private ComponentContext componentContext;

    @Activate
    public RainSoftHandlerFactory(@Reference NetworkAddressService networkAddressService,
            @Reference HttpService httpService, ComponentContext componentContext) {
        super.activate(componentContext);
        httpPort = HttpServiceUtil.getHttpServicePort(componentContext.getBundleContext());
        if (httpPort == -1) {
            httpPort = 8080;
        }
        this.httpService = httpService;
        this.networkAddressService = networkAddressService;

        logger.debug("Using OH HTTP port {}", httpPort);

        SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_WCS);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.info("createHandler thingType: {}", thingTypeUID);
        if (thingTypeUID.equals(THING_TYPE_ACCOUNT)) {
            return new AccountHandler(thing, networkAddressService, httpService, httpPort);
        } else if (thingTypeUID.equals(THING_TYPE_WCS)) {
            return new WCSHandler(thing);
        }
        return null;
    }
    /*
     * @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
     * protected void setHttpService(HttpService httpService) {
     * this.httpService = httpService;
     * }
     *
     * protected void unsetHttpService(HttpService httpService) {
     * this.httpService = null;
     * }
     *
     * @Reference
     * protected void setComponentContext(ComponentContext componentContext) {
     * this.componentContext = componentContext;
     * }
     *
     * protected void unsetComponentContext(ComponentContext componentContext) {
     * this.componentContext = null;
     * }
     *
     * @Reference
     * protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
     * this.networkAddressService = networkAddressService;
     * }
     *
     * protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
     * this.networkAddressService = null;
     * }
     */
}
