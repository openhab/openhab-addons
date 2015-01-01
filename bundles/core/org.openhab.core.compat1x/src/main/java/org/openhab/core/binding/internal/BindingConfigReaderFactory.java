/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.binding.internal;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.openhab.model.item.binding.BindingConfigReader;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * This class listens for services that implement the old binding config reader interface and registers
 * an according service for each under the new interface.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 */
public class BindingConfigReaderFactory {

	private Map<String, ServiceRegistration<org.eclipse.smarthome.model.item.BindingConfigReader>> delegates = new HashMap<>();
	private BundleContext context;
	
	private Set<BindingConfigReader> readers = new HashSet<>();
	
	public void activate(BundleContext context) {
		this.context = context;
		for(BindingConfigReader reader : readers) {
			registerDelegateService(reader);
		}
	}
	
	public void deactivate() {
		for(ServiceRegistration<org.eclipse.smarthome.model.item.BindingConfigReader> serviceReg : delegates.values()) {
			serviceReg.unregister();
		}
		delegates.clear();
		this.context = null;
	}
	
	public void addBindingConfigReader(BindingConfigReader reader) {
		if(context!=null) {
			registerDelegateService(reader);			
		} else {
			readers.add(reader);
		}
	}

	public void removeBindingConfigReader(BindingConfigReader reader) {
		if(context!=null) {
			unregisterDelegateService(reader);
		}
	}

	private void registerDelegateService(BindingConfigReader reader) {
		if(!delegates.containsKey(reader.getBindingType())) {
			BindingConfigReaderDelegate service = new BindingConfigReaderDelegate(reader);
			Dictionary<String, Object> props = new Hashtable<String, Object>();
			ServiceRegistration<org.eclipse.smarthome.model.item.BindingConfigReader> serviceReg = 
					context.registerService(org.eclipse.smarthome.model.item.BindingConfigReader.class, service, props);
			delegates.put(reader.getBindingType(), serviceReg);
		}
	}

	private void unregisterDelegateService(BindingConfigReader reader) {
		if(delegates.containsKey(reader.getBindingType())) {
			ServiceRegistration<org.eclipse.smarthome.model.item.BindingConfigReader> serviceReg = 
					delegates.get(reader.getBindingType());
			delegates.remove(reader.getBindingType());
			serviceReg.unregister();
		}
	}
}
