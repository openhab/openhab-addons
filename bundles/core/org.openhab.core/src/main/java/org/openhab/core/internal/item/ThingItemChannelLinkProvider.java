/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.internal.item;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkProvider;

/**
 * This class provides item channel links for all items that were created through
 * the {@link ThingItemUIProvider}.
 * 
 * @author Kai Kreuzer
 *
 */
public class ThingItemChannelLinkProvider implements ItemChannelLinkProvider, RegistryChangeListener<Thing> {

	private Set<ProviderChangeListener<ItemChannelLink>> listeners = new HashSet<>();
	private ThingRegistry thingRegistry;

	@Override
	public void addProviderChangeListener(
			ProviderChangeListener<ItemChannelLink> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeProviderChangeListener(
			ProviderChangeListener<ItemChannelLink> listener) {
		listeners.remove(listener);
	}

	@Override
	public Set<ItemChannelLink> getAll() {
		Set<ItemChannelLink> links = new HashSet<>();
		for(Thing thing : thingRegistry.getAll()) {
			links.addAll(getLinks(thing));
		}
		return links;
	}

	private Set<ItemChannelLink> getLinks(Thing element) {
		Set<ItemChannelLink> links = new HashSet<>();
		for(Channel channel : element.getChannels()) {
			links.add(new ItemChannelLink(channel.getUID().toString().replace(":", "_"), channel.getUID()));
		}
		return links;
	}

	protected void setThingItemUIProvider(ThingItemUIProvider thingItemUIProvider) {
		// we actually only need this dependency to make sure that this service is active
		// as we only want to provide links for items of that service
	}

	protected void unsetThingItemUIProvider(ThingItemUIProvider thingItemUIProvider) {
	}

	protected void setThingRegistry(ThingRegistry thingRegistry) {
		this.thingRegistry = thingRegistry;
		this.thingRegistry.addRegistryChangeListener(this);
	}

	protected void unsetThingRegistry(ThingRegistry thingRegistry) {
		this.thingRegistry.addRegistryChangeListener(this);
		this.thingRegistry = null;
	}

	public void updated(Provider<Thing> provider, Thing oldelement,
			Thing element) {
		for(ProviderChangeListener<ItemChannelLink> listener : listeners) {
			for(ItemChannelLink link : getLinks(oldelement)) {
				listener.removed(this, link);
			}
			for(ItemChannelLink link : getLinks(element)) {
				listener.added(this, link);
			}
		}
	}

	@Override
	public void added(Thing element) {
		for(ProviderChangeListener<ItemChannelLink> listener : listeners) {
			for(ItemChannelLink link : getLinks(element)) {
				listener.added(this, link);
			}
		}
	}

	@Override
	public void removed(Thing element) {
		for(ProviderChangeListener<ItemChannelLink> listener : listeners) {
			for(ItemChannelLink link : getLinks(element)) {
				listener.removed(this, link);
			}
		}
	}

	@Override
	public void updated(Thing oldElement, Thing element) {
		for(ProviderChangeListener<ItemChannelLink> listener : listeners) {
			for(ItemChannelLink link : getLinks(oldElement)) {
				listener.removed(this, link);
			}
			for(ItemChannelLink link : getLinks(element)) {
				listener.added(this, link);
			}
		}
	}

}
