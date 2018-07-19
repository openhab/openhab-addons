/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.konnected.internal;

import static org.openhab.binding.konnected.internal.KonnectedBindingConstants.THING_TYPE_MODULE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServlet;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.konnected.internal.handler.KonnectedHandler;
import org.openhab.binding.konnected.internal.servelet.KonnectedHTTPServlet;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

/**
 * The {@link KonnectedHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@Component(service = { ThingHandlerFactory.class,
        KonnectedHandlerFactory.class }, immediate = true, configurationPid = "binding.konnected")

public class KonnectedHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_MODULE);
    private Map<ThingUID, ServiceRegistration<?>> webHookServiceRegs = new HashMap<>();
    private HttpService httpService;
    private KonnectedDynamicStateDescriptionProvider dynamicStateDescriptionProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        KonnectedHTTPServlet servlet = registerWebHookServlet(thing.getUID());
        return new KonnectedHandler(thing, servlet, dynamicStateDescriptionProvider);

    }

    private KonnectedHTTPServlet registerWebHookServlet(ThingUID thingUID) {
        KonnectedHTTPServlet servlet = null;

        servlet = new KonnectedHTTPServlet(httpService, thingUID.getId());
        if (bundleContext != null) {
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
    protected void setDynamicStateDescriptionProvider(KonnectedDynamicStateDescriptionProvider provider) {
        this.dynamicStateDescriptionProvider = provider;
    }

    protected void unsetDynamicStateDescriptionProvider(KonnectedDynamicStateDescriptionProvider provider) {
        this.dynamicStateDescriptionProvider = null;
    }
}
