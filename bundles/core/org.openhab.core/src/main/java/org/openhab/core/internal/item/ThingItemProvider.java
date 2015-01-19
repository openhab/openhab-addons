/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.internal.item;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ActiveItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.types.UnDefType;
import org.osgi.service.cm.ConfigurationException;

/**
 * This class dynamically provides items incl. labels from all things of the {@link ManagedThingProvider}.
 * All items are hierarchically sorted with a root group item called "Things".
 * 
 * @author Kai Kreuzer
 *
 */
public class ThingItemProvider implements ItemProvider, RegistryChangeListener<Item> {

	private Set<ProviderChangeListener<Item>> listeners = new HashSet<>();

	private ItemRegistry itemRegistry;
	private EventPublisher eventPublisher;
	private GroupItem rootItem;

	private boolean enabled = false;
	
	@Override
	public Collection<Item> getAll() {
		if(!enabled) return Collections.emptySet();
		
		Set<Item> items = new HashSet<>();
		GroupItem all = getRootItem();
		for(Item item : itemRegistry.getItemsByTag("thing")) {
			ActiveItem aItem = (ActiveItem) item;
			aItem.addGroupName(all.getName());
			all.addMember(item);
		}
		items.add(all);
		return items;
	}

	@Override
	public void addProviderChangeListener(ProviderChangeListener<Item> listener) {
		listeners.add(listener);
		for(Item item : getAll()) {
			listener.added(this, item);
		}
	}

	@Override
	public void removeProviderChangeListener(
			ProviderChangeListener<Item> listener) {
		listeners.remove(listener);
		
	}
	
	protected void setItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = itemRegistry;
		itemRegistry.addRegistryChangeListener(this);
	}

	protected void unsetItemRegistry(ItemRegistry itemRegistry) {
		itemRegistry.removeRegistryChangeListener(this);
		this.itemRegistry = null;
	}

	protected void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	protected void unsetEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = null;
	}

	private synchronized GroupItem getRootItem() {
		if(rootItem==null) {
			rootItem = new GroupItem("All");
			rootItem.addTag("home-group");
			rootItem.setLabel("All");
			getAll();
		}
		return rootItem;
	}

	protected void activate(Map<String, Object> properties) throws ConfigurationException {
		if(properties!=null) {
			String enabled = (String) properties.get("enabled");
			if("true".equalsIgnoreCase(enabled)) {
				this.enabled = true;
				for(ProviderChangeListener<Item> listener : listeners) {
					for(Item item : getAll()) {
						listener.added(this, item);
					}
				}
			} else {
		    	this.enabled = false;
				for(ProviderChangeListener<Item> listener : listeners) {
					for(Item item : getAll()) {
						listener.removed(this, item);
					}
				}
			}
		}
	}

	@Override
	public void added(Item element) {
		if(!enabled) return;
		if(!element.getTags().contains("thing")) return;
		rootItem = null;
		rootItem = getRootItem();
		for(ProviderChangeListener<Item> listener : listeners) {
			listener.removed(this, getRootItem());
			listener.added(this, getRootItem());
		}
	}

	@Override
	public void removed(Item element) {
		if(!enabled) return;
		if(!element.getTags().contains("thing")) return;
		for(ProviderChangeListener<Item> listener : listeners) {
			listener.removed(this, getRootItem());
			rootItem = null;
			listener.added(this, getRootItem());
		}	
	}

	@Override
	public void updated(Item oldElement, Item element) {
		if(!enabled) return;
		removed(oldElement);
		added(element);
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
}
