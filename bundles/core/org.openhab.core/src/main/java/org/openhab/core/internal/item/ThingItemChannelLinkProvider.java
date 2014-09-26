package org.openhab.core.internal.item;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkProvider;

/**
 * This class provides item channel links for all items that were created through
 * the {@link ThingItemUIProvider}.
 * 
 * @author Kai Kreuzer
 *
 */
public class ThingItemChannelLinkProvider implements ItemChannelLinkProvider, ProviderChangeListener<Thing> {

	private Set<ProviderChangeListener<ItemChannelLink>> listeners = new HashSet<>();
	private ManagedThingProvider thingProvider;

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
		for(Thing thing : thingProvider.getAll()) {
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

	protected void unsetThingItemUIProvider(ThingItemUIProvider thingProvider) {
	}

	protected void setThingProvider(ManagedThingProvider thingProvider) {
		this.thingProvider = thingProvider;
		this.thingProvider.addProviderChangeListener(this);
	}

	protected void unsetThingProvider(ManagedThingProvider thingProvider) {
		this.thingProvider.removeProviderChangeListener(this);
		this.thingProvider = null;
	}

	@Override
	public void added(Provider<Thing> provider, Thing element) {
		for(ProviderChangeListener<ItemChannelLink> listener : listeners) {
			for(ItemChannelLink link : getLinks(element)) {
				listener.added(this, link);
			}
		}
	}

	@Override
	public void removed(Provider<Thing> provider, Thing element) {
		for(ProviderChangeListener<ItemChannelLink> listener : listeners) {
			for(ItemChannelLink link : getLinks(element)) {
				listener.removed(this, link);
			}
		}
	}

	@Override
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

}
