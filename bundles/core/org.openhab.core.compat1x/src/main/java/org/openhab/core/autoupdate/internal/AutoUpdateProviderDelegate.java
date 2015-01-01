/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.autoupdate.internal;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.core.autoupdate.AutoUpdateBindingConfigProvider;

/**
 * This class serves as a mapping from the "old" org.openhab namespace to the new org.eclipse.smarthome
 * namespace for the auto update provider. It gathers all services that implement the old interface
 * and makes them available as single provider of the new interface. 
 * 
 * @author Kai Kreuzer - Initial contribution and API
 */
public class AutoUpdateProviderDelegate implements AutoUpdateBindingConfigProvider {

	private Set<org.openhab.core.autoupdate.AutoUpdateBindingProvider> providers = new CopyOnWriteArraySet<>();

	public void addAutoUpdateBindingProvider(org.openhab.core.autoupdate.AutoUpdateBindingProvider provider) {
		providers.add(provider);
	}

	public void removeAutoUpdateBindingProvider(org.openhab.core.autoupdate.AutoUpdateBindingProvider provider) {
		providers.remove(provider);
	}

	@Override
	public Boolean autoUpdate(String itemName) {
		for(org.openhab.core.autoupdate.AutoUpdateBindingProvider provider : providers) {
			Boolean autoUpdate = provider.autoUpdate(itemName);
			if(autoUpdate!=null) {
				return autoUpdate;
			}
		}
		return null;
	}
}
