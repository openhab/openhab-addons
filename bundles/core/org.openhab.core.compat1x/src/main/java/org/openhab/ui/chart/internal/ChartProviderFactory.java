/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.chart.internal;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.openhab.ui.chart.ChartProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * This class listens for services that implement the old chart provider service interface and registers
 * an according service for each under the new interface.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 */
public class ChartProviderFactory {

	private Map<String, ServiceRegistration<org.eclipse.smarthome.ui.chart.ChartProvider>> delegates = new HashMap<>();
	private BundleContext context;
	
	private Set<ChartProvider> chartProviders = new HashSet<>();
	
	public void activate(BundleContext context) {
		this.context = context;
		for(ChartProvider provider : chartProviders) {
			registerDelegateProvider(provider);
		}
	}
	
	public void deactivate() {
		for(ServiceRegistration<org.eclipse.smarthome.ui.chart.ChartProvider> serviceReg : delegates.values()) {
			serviceReg.unregister();
		}
		delegates.clear();
		this.context = null;
	}
	
	public void addChartProvider(ChartProvider provider) {
		if(context!=null) {
			registerDelegateProvider(provider);			
		} else {
			chartProviders.add(provider);
		}
	}

	public void removeChartProvider(ChartProvider provider) {
		if(context!=null) {
			unregisterDelegateProvider(provider);
		}
	}

	private void registerDelegateProvider(ChartProvider chartProvider) {
		if(!delegates.containsKey(chartProvider.getName())) {
			ChartProviderDelegate service = new ChartProviderDelegate(chartProvider);
			Dictionary<String, Object> props = new Hashtable<String, Object>();
			ServiceRegistration<org.eclipse.smarthome.ui.chart.ChartProvider> serviceReg = 
					context.registerService(org.eclipse.smarthome.ui.chart.ChartProvider.class, service, props);
			delegates.put(chartProvider.getName(), serviceReg);
		}
	}

	private void unregisterDelegateProvider(ChartProvider chartProvider) {
		if(delegates.containsKey(chartProvider.getName())) {
			ServiceRegistration<org.eclipse.smarthome.ui.chart.ChartProvider> serviceReg = 
					delegates.get(chartProvider.getName());
			delegates.remove(chartProvider.getName());
			serviceReg.unregister();
		}
	}
}
