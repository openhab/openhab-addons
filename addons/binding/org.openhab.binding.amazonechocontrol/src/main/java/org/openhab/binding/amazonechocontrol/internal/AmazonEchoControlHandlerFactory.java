/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal;

import static org.openhab.binding.amazonechocontrol.AmazonEchoControlBindingConstants.*;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.amazonechocontrol.handler.AccountHandler;
import org.openhab.binding.amazonechocontrol.handler.EchoHandler;
import org.openhab.binding.amazonechocontrol.handler.FlashBriefingProfileHandler;
import org.openhab.binding.amazonechocontrol.internal.discovery.AmazonEchoDiscovery;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;

/**
 * The {@link AmazonEchoControlHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Geramb - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.amazonechocontrol")
@NonNullByDefault
public class AmazonEchoControlHandlerFactory extends BaseThingHandlerFactory {

    @Nullable
    HttpService httpService;
    @Nullable
    AmazonEchoDiscovery amazonEchoDiscovery;

    boolean showIdsInGUI;
    @Nullable
    ServiceRegistration<?> discoverServiceRegistration;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Object configShowIdsInGui = componentContext.getProperties().get("showIdsInGUI");
        showIdsInGUI = (configShowIdsInGui instanceof Boolean) ? (Boolean) configShowIdsInGui : false;

    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
        AmazonEchoDiscovery amazonEchoDiscovery = this.amazonEchoDiscovery;
        if (amazonEchoDiscovery != null) {
            amazonEchoDiscovery.deactivate();
        }
        this.amazonEchoDiscovery = null;
        @Nullable
        ServiceRegistration<?> discoverServiceRegistration = this.discoverServiceRegistration;
        if (discoverServiceRegistration != null) {
            discoverServiceRegistration.unregister();
            this.discoverServiceRegistration = null;
        }
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        HttpService httpService = this.httpService;
        if (httpService == null) {
            return null;
        }
        AmazonEchoDiscovery amazonEchoDiscovery = this.amazonEchoDiscovery;
        if (amazonEchoDiscovery == null) {
            amazonEchoDiscovery = new AmazonEchoDiscovery();
            discoverServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                    amazonEchoDiscovery, new Hashtable<String, Object>());
            amazonEchoDiscovery.activate();
            this.amazonEchoDiscovery = amazonEchoDiscovery;

        }

        if (thingTypeUID.equals(THING_TYPE_ACCOUNT)) {
            AccountHandler bridgeHandler = new AccountHandler((Bridge) thing, httpService, amazonEchoDiscovery);
            return bridgeHandler;
        }
        if (thingTypeUID.equals(THING_TYPE_FLASH_BRIEFING_PROFILE)) {
            return new FlashBriefingProfileHandler(thing, amazonEchoDiscovery);
        }
        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new EchoHandler(thing, showIdsInGUI);
        }
        return null;
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }
}
