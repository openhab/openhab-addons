/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.persistence.internal;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.openhab.core.compat1x.internal.ItemMapper;


/**
 * This class serves as a mapping from the "old" org.openhab namespace to the new org.eclipse.smarthome
 * namespace for the persistence service. It wraps an instance with the old interface
 * into a class with the new interface. 
 * 
 * @author Kai Kreuzer - Initial contribution and API
 */
public class PersistenceServiceDelegate implements PersistenceService {

	protected org.openhab.core.persistence.PersistenceService service;

	public PersistenceServiceDelegate(org.openhab.core.persistence.PersistenceService service) {
		this.service = service;
	}

	@Override
	public String getName() {
		return service.getName();
	}

	@Override
	public void store(Item item) {
		org.openhab.core.items.Item ohItem = ItemMapper.mapToOpenHABItem(item);
		if(ohItem!=null) {
			service.store(ohItem);
		}
	}

	@Override
	public void store(Item item, String alias) {
		org.openhab.core.items.Item ohItem = ItemMapper.mapToOpenHABItem(item);
		if(ohItem!=null) {
			service.store(ohItem, alias);
		}
	}

}
