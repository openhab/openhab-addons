/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.ui.cometvisu.internal.listeners;

import java.util.Collection;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ItemRegistryChangeListener;
import org.openhab.ui.cometvisu.internal.backend.EventBroadcaster;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Listener responsible for notifying the CometVisu backend about changes
 * in the ItemRegistry
 *
 * @author Tobias Bräutigam - Initial Contribution and API
 */
@Component(immediate = true)
public class ItemRegistryEventListener implements ItemRegistryChangeListener {
    private ItemRegistry itemRegistry;

    private EventBroadcaster eventBroadcaster;

    @Reference
    protected void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        this.eventBroadcaster = eventBroadcaster;
    }

    protected void unsetEventBroadcaster(EventBroadcaster eventBroadcaster) {
        this.eventBroadcaster = null;
    }

    @Reference
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
