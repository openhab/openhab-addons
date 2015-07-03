/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.persistence.internal;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.openhab.core.persistence.PersistenceService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * This class listens for services that implement the old persistence service interface and registers
 * an according service for each under the new interface.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 */
public class PersistenceServiceFactory {

	private Map<String, ServiceRegistration<org.eclipse.smarthome.core.persistence.PersistenceService>> delegates = new HashMap<>();
	private BundleContext context;
	
	private Set<PersistenceService> persistenceServices = new HashSet<>();
	
	public void activate(BundleContext context) {
		this.context = context;
		for(PersistenceService service : persistenceServices) {
			registerDelegateService(service);
		}
	}
	
	public void deactivate() {
		for(ServiceRegistration<org.eclipse.smarthome.core.persistence.PersistenceService> serviceReg : delegates.values()) {
			serviceReg.unregister();
		}
		delegates.clear();
		this.context = null;
	}

	public void addPersistenceService(PersistenceService service) {
		if(context!=null) {
			registerDelegateService(service);			
		} else {
			persistenceServices.add(service);
		}
	}

	public void removePersistenceService(PersistenceService service) {
		if(context!=null) {
			unregisterDelegateService(service);
		}
	}

	private void registerDelegateService(PersistenceService persistenceService) {
		if(!delegates.containsKey(persistenceService.getName())) {
			org.eclipse.smarthome.core.persistence.PersistenceService service = 
					(persistenceService instanceof org.openhab.core.persistence.QueryablePersistenceService) ?
					new QueryablePersistenceServiceDelegate(persistenceService) 
				:	new PersistenceServiceDelegate(persistenceService);
			Dictionary<String, Object> props = new Hashtable<String, Object>();
			ServiceRegistration<org.eclipse.smarthome.core.persistence.PersistenceService> serviceReg = 
					context.registerService(org.eclipse.smarthome.core.persistence.PersistenceService.class, service, props);
			delegates.put(persistenceService.getName(), serviceReg);
		}
	}

	private void unregisterDelegateService(PersistenceService service) {
		if(delegates.containsKey(service.getName())) {
			ServiceRegistration<org.eclipse.smarthome.core.persistence.PersistenceService> serviceReg = 
					delegates.get(service.getName());
			delegates.remove(service.getName());
			serviceReg.unregister();
		}
	}
}
