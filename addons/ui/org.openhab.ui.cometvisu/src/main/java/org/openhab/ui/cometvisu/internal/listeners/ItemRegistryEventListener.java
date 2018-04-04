/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.internal.listeners;

import java.util.Collection;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ItemRegistryChangeListener;
import org.openhab.ui.cometvisu.internal.backend.EventBroadcaster;

/**
 * Listener responsible for notifying the CometVisu backend about changes
 * in the ItemRegistry
 * 
 * @author Tobias Bräutigam - Initial Contribution and API
 * @since 2.0.0
 */
public class ItemRegistryEventListener implements ItemRegistryChangeListener {
    private ItemRegistry itemRegistry;

    private EventBroadcaster eventBroadcaster;

    protected void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        this.eventBroadcaster = eventBroadcaster;
    }

    protected void unsetEventBroadcaster(EventBroadcaster eventBroadcaster) {
        this.eventBroadcaster = null;
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
        this.itemRegistry.addRegistryChangeListener(this);
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry.removeRegistryChangeListener(this);
        this.itemRegistry = null;
    }

    @Override
    public void added(Item element) {
        eventBroadcaster.registerItem(element);
    }

    @Override
    public void removed(Item element) {
        eventBroadcaster.unregisterItem(element);
    }

    @Override
    public void updated(Item oldElement, Item element) {
        eventBroadcaster.unregisterItem(oldElement);
        eventBroadcaster.registerItem(element);
    }

    @Override
    public void allItemsChanged(Collection<String> oldItemNames) {
        // All items have changed, StateListener needs to be registered to the new Items
        eventBroadcaster.registerItems();
    }
}
