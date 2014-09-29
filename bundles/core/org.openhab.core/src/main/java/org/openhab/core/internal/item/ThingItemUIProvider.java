package org.openhab.core.internal.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.items.ItemUIProvider;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

/**
 * This class dynamically provides items incl. labels from all things of the {@link ManagedThingProvider}.
 * All items are hierarchically sorted with a root group item called "Things".
 * 
 * @author Kai Kreuzer
 *
 */
public class ThingItemUIProvider implements ItemUIProvider, ItemProvider, ProviderChangeListener<Thing>, ManagedService {

	private Set<ProviderChangeListener<Item>> listeners = new HashSet<>();

	private ManagedThingProvider thingProvider;
	private ItemFactory itemFactory;
	private ThingTypeRegistry thingTypeRegistry;
	private GroupItem rootItem;

	private boolean enabled = false;
	
	@Override
	public String getIcon(String itemName) {
		if(!enabled) return null; 

		if("Things".equals(itemName)) {
			return "network";
		}
		
		for(Thing thing : thingProvider.getAll()) {
			if(thing.getUID().toString().replaceAll(":",  "_").equals(itemName)) {
				return "none";
			}
		}
		return null;
	}

	@Override
	public String getLabel(String itemName) {
		if(!enabled) return null;
		
 		for(Thing thing : thingProvider.getAll()) {
			if(thing.getUID().toString().replaceAll(":",  "_").equals(itemName)) {
				return (String) thing.getConfiguration().get("label");
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
		
		List<Item> items = new ArrayList<>();
		GroupItem all = getRootItem();
		for(Thing thing : thingProvider.getAll()) {
			GroupItem group = createItemsForThing(thing);
			if(!(thing instanceof Bridge) && thing.getBridgeUID()!=null) {
				group.addGroupName(thing.getBridgeUID().toString().replaceAll(":", "_"));
			} else {
				all.addMember(group);
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
			group.addMember(item);
		}
		if(thing instanceof Bridge) {
			Bridge bridge = (Bridge) thing;
			for(Thing child : bridge.getThings()) {
				group.addMember(createItemsForThing(child));
			}
		}
		return group;
	}

	@Override
	public void addProviderChangeListener(ProviderChangeListener<Item> listener) {
		listeners.add(listener);
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

	protected void setThingProvider(ManagedThingProvider thingProvider) {
		this.thingProvider = thingProvider;
		this.thingProvider.addProviderChangeListener(this);
	}

	protected void unsetThingProvider(ManagedThingProvider thingProvider) {
		this.thingProvider.removeProviderChangeListener(this);
		this.thingProvider = null;
	}

	protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
		this.thingTypeRegistry = thingTypeRegistry;
	}

	protected void unsetThingTypeRegistry(ThingTypeRegistry thingProvider) {
		this.thingTypeRegistry = null;
	}

	@Override
	public void added(Provider<Thing> provider, Thing thing) {
		for(ProviderChangeListener<Item> listener : listeners) {
			listener.removed(this, getRootItem());
			rootItem = null;
			GroupItem group = createItemsForThing(thing);
			listener.added(this, group);
			for(Item item : group.getMembers()) {
				listener.added(this, item);
			}
			listener.added(this, getRootItem());
		}
	}

	@Override
	public void removed(Provider<Thing> provider, Thing thing) {
		for(ProviderChangeListener<Item> listener : listeners) {
			listener.removed(this, getRootItem());
			rootItem = null;
			GroupItem group = createItemsForThing(thing);
			listener.removed(this, group);
			for(Item item : group.getMembers()) {
				listener.removed(this, item);
			}
			listener.added(this, getRootItem());
		}		
	}

	private synchronized GroupItem getRootItem() {
		if(rootItem==null) {
			rootItem = new GroupItem("Things");
			getAll();
		}
		return rootItem;
	}

	@Override
	public void updated(Provider<Thing> provider, Thing oldelement,
			Thing element) {
		added(provider, element);
		removed(provider, oldelement);
		added(provider, element);
	}

	@Override
	public void updated(Dictionary<String, ?> properties)
			throws ConfigurationException {
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
	
}
