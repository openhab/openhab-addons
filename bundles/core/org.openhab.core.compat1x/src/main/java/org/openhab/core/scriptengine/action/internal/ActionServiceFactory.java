/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.scriptengine.action.internal;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.openhab.core.scriptengine.action.ActionService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * This class listens for services that implement the old action service interface and registers
 * an according service for each under the new interface.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class ActionServiceFactory {

    private Map<String, ServiceRegistration<org.eclipse.smarthome.model.script.engine.action.ActionService>> delegates = new HashMap<>();
    private BundleContext context;

    private Set<ActionService> actionServices = new HashSet<>();

    public void activate(BundleContext context) {
        this.context = context;
        for (ActionService service : actionServices) {
            registerDelegateService(service);
        }
    }

    public void deactivate() {
        for (ServiceRegistration<org.eclipse.smarthome.model.script.engine.action.ActionService> serviceReg : delegates
                .values()) {
            serviceReg.unregister();
        }
        delegates.clear();
        this.context = null;
    }

    public void addActionService(ActionService service) {
        if (context != null) {
            registerDelegateService(service);
        } else {
            actionServices.add(service);
        }
    }

    public void removeActionService(ActionService service) {
        if (context != null) {
            unregisterDelegateService(service);
        }
    }

    private void registerDelegateService(ActionService actionService) {
        if (!delegates.containsKey(actionService.getActionClassName())) {
            ActionServiceDelegate service = new ActionServiceDelegate(actionService);
            Dictionary<String, Object> props = new Hashtable<String, Object>();
            ServiceRegistration<org.eclipse.smarthome.model.script.engine.action.ActionService> serviceReg = context
                    .registerService(org.eclipse.smarthome.model.script.engine.action.ActionService.class, service,
                            props);
            delegates.put(actionService.getActionClassName(), serviceReg);
        }
    }

    private void unregisterDelegateService(ActionService service) {
        if (delegates.containsKey(service.getActionClassName())) {
            ServiceRegistration<org.eclipse.smarthome.model.script.engine.action.ActionService> serviceReg = delegates
                    .get(service.getActionClassName());
            delegates.remove(service.getActionClassName());
            serviceReg.unregister();
        }
    }
}
