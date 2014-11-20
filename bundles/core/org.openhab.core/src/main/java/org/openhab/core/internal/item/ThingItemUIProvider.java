/**
 * Copyright (c) 2010-2014, openHAB.org and others.
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.items.ItemUIProvider;
import org.osgi.service.cm.ConfigurationException;

/**
 * This class dynamically provides items incl. labels from all things of the {@link ManagedThingProvider}.
 * All items are hierarchically sorted with a root group item called "Things".
 * 
 * @author Kai Kreuzer
 *
 */
public class ThingItemUIProvider implements ItemUIProvider, ItemProvider, RegistryChangeListener<Thing> {

	private Set<ProviderChangeListener<Item>> listeners = new HashSet<>();

	private ThingRegistry thingRegistry;
	private ItemFactory itemFactory;
	private ThingTypeRegistry thingTypeRegistry;
	private EventPublisher eventPublisher;
	private GroupItem rootItem;

	private boolean enabled = false;

	
	@Override
	public String getIcon(String itemName) {
		if(!enabled) return null; 

		if("Things".equals(itemName)) {
			return "network";
		}
		
		for(Thing thing : thingRegistry.getAll()) {
			if(thing.getUID().toString().replaceAll(":",  "_").equals(itemName)) {
				String icon = null;
				if(thing instanceof Bridge) {
					icon = "network";
				} else {
					icon = "switch";
				}
				if(thing.getStatus().equals(ThingStatus.ONLINE)) {
					return icon + "-on";
				} else {
					return icon + "-off";
				}
			}
			for(Channel ch : thing.getChannels()) {
				if(ch.getUID().toString().replaceAll(":",  "_").equals(itemName)) {
					if(ch.getAcceptedItemType().equals("Color")) {
						return "switch";
					}
					if(ch.getAcceptedItemType().equals("Dimmer")) {
						return "switch";
					}
				}
			}
		}
		return null;
	}

	@Override
	public String getLabel(String itemName) {
		if(!enabled) return null;
		
 		for(Thing thing : thingRegistry.getAll()) {
			if(thing.getUID().toString().replaceAll(":",  "_").equals(itemName)) {
				String label = (String) thing.getConfiguration().get("label");
				if(label!=null && !label.isEmpty()) {
					return label;
				} else {
					return WordUtils.capitalize(itemName.replace("_", " "));
				}
			}
			for(Channel channel : thing.getChannels()) {
				if(channel.getUID().toString().replaceAll(":",  "_").equals(itemName)) {
					String label = (String) StringUtils.capitalize(channel.getUID().getId());
					ThingType thingType = thingTypeRegistry.getThingType(thing.getThingTypeUID());
					if(thingType!=null) {
						for(ChannelDefinition chDef : thingType.getChannelDefinitions()) {
							if (chDef.getId().equals(channel.getUID().getId())) {
								label = chDef.getType().getLabel();
							}
						}
					}
					if(channel.getAcceptedItemType().equals("String")) label += " [%s]";
					if(channel.getAcceptedItemType().equals("Number")) label += " [%.1f]";
					return label;
				}
			}
		}		
		return null;
	}

	@Override
	public Widget getDefaultWidget(Class<? extends Item> itemType,
			String itemName) {
		return null;
	}

	@Override
	public Widget getWidget(String itemName) {
		return null;
	}

	@Override
	public Collection<Item> getAll() {
		if(!enabled) return Collections.emptySet();
		
		Set<Item> items = new HashSet<>();
		GroupItem all = getRootItem();
		for(Thing thing : thingRegistry.getAll()) {
			GroupItem group = createItemsForThing(thing);
			if((thing instanceof Bridge) || thing.getBridgeUID() == null) {
				if(group!=null) {
					if(all.getMembers().contains(group)) {
						all.removeMember(group);
					}
					all.addMember(group);
				}
			}
			for(Item item : group.getAllMembers()) {
				items.add(item);
			}
			items.add(group);
		}
		items.add(all);
		return items;
	}

	/*default*/ GroupItem createItemsForThing(Thing thing) {
		GroupItem group = new GroupItem(thing.getUID().toString().replaceAll(":",  "_"));
		for(Channel channel : thing.getChannels()) {
			Item item = itemFactory.createItem(channel.getAcceptedItemType(), channel.getUID().toString().replaceAll(":",  "_"));
			if(item!=null) {
				if(group.getMembers().contains(item)) {
					group.removeMember(item);
				}
				group.addMember(item);
			}
		}
		if(thing instanceof Bridge) {
			Bridge bridge = (Bridge) thing;
			for(Thing child : bridge.getThings()) {
				group.addMember(createItemsForThing(child));
			}
		}
		if(thing.getBridgeUID()!=null) {
			group.addGroupName(thing.getBridgeUID().toString().replaceAll(":",  "_"));
		} else {
			group.addGroupName("Things");
		}
		return group;
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
	
	protected void setItemFactory(ItemFactory itemFactory) {
		this.itemFactory = itemFactory;
	}

	protected void unsetItemFactory(ItemFactory itemFactory) {
		this.itemFactory = null;
	}

	protected void setThingRegistry(ThingRegistry thingRegistry) {
		this.thingRegistry = thingRegistry;
		this.thingRegistry.addRegistryChangeListener(this);
	}

	protected void unsetThingRegistry(ThingRegistry thingRegistry) {
		this.thingRegistry.addRegistryChangeListener(this);
		this.thingRegistry = null;
	}

	protected void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	protected void unsetEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = null;
	}

	protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
		this.thingTypeRegistry = thingTypeRegistry;
	}

	protected void unsetThingTypeRegistry(ThingTypeRegistry thingProvider) {
		this.thingTypeRegistry = null;
	}

	private synchronized GroupItem getRootItem() {
		if(rootItem==null) {
			rootItem = new GroupItem("Things");
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
	public void added(Thing element) {
		for(ProviderChangeListener<Item> listener : listeners) {
			listener.removed(this, getRootItem());
		}
		rootItem = null;
		GroupItem group = createItemsForThing(element);
		rootItem = getRootItem();
		for(ProviderChangeListener<Item> listener : listeners) {
			for(Item item : group.getMembers()) {
				listener.added(this, item);
			}
			listener.added(this, group);
			listener.added(this, getRootItem());

		}
		eventPublisher.postUpdate(group.getName(), UnDefType.UNDEF);
		eventPublisher.postUpdate(rootItem.getName(), UnDefType.UNDEF);
	}

	@Override
	public void removed(Thing element) {
		for(ProviderChangeListener<Item> listener : listeners) {
			listener.removed(this, getRootItem());
			rootItem = null;
			GroupItem group = createItemsForThing(element);
			listener.removed(this, group);
			for(Item item : group.getMembers()) {
				listener.removed(this, item);
			}
			listener.added(this, getRootItem());
		}	
	}

	@Override
	public void updated(Thing oldElement, Thing element) {
		removed(oldElement);
		added(element);
	}
	
}
