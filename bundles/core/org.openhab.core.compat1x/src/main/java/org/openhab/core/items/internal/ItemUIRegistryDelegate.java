/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.items.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.openhab.core.compat1x.internal.ItemMapper;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemNotUniqueException;
import org.openhab.core.items.ItemRegistryChangeListener;
import org.openhab.core.types.State;
import org.openhab.model.sitemap.LinkableWidget;
import org.openhab.model.sitemap.Sitemap;
import org.openhab.model.sitemap.Widget;
import org.openhab.ui.items.ItemUIRegistry;

public class ItemUIRegistryDelegate implements ItemUIRegistry, RegistryChangeListener<org.eclipse.smarthome.core.items.Item> {

    private org.eclipse.smarthome.ui.items.ItemUIRegistry itemUIRegistry;
    private Set<ItemRegistryChangeListener> listeners = new HashSet<>();

    protected void setItemUIRegistry(org.eclipse.smarthome.ui.items.ItemUIRegistry itemUIRegistry) {
        this.itemUIRegistry = itemUIRegistry;
        itemUIRegistry.addRegistryChangeListener(this);
    }

    protected void unsetItemUIRegistry(org.eclipse.smarthome.core.items.ItemRegistry itemUIRegistry) {
        this.itemUIRegistry = null;
    }

    @Override
    public Item getItem(String name) throws ItemNotFoundException {
        org.eclipse.smarthome.core.items.Item eshItem = itemUIRegistry.get(name);
        return ItemMapper.mapToOpenHABItem(eshItem);
    }

    @Override
    public Item getItemByPattern(String name) throws ItemNotFoundException, ItemNotUniqueException {
        org.eclipse.smarthome.core.items.Item eshItem;
        try {
            eshItem = itemUIRegistry.getItemByPattern(name);
        } catch (org.eclipse.smarthome.core.items.ItemNotFoundException e) {
            throw new ItemNotFoundException(name);
        } catch (org.eclipse.smarthome.core.items.ItemNotUniqueException e) {
            throw new ItemNotUniqueException(name, null);
        }
        return ItemMapper.mapToOpenHABItem(eshItem);
    }

    @Override
    public Collection<Item> getItems() {
        Collection<org.eclipse.smarthome.core.items.Item> eshItems = itemUIRegistry.getItems();
        Collection<Item> ohItems = new HashSet<Item>(eshItems.size());

        for(org.eclipse.smarthome.core.items.Item eshItem : eshItems) {
            ohItems.add(ItemMapper.mapToOpenHABItem(eshItem));
        }
        return ohItems;
    }

    @Override
    public Collection<Item> getItems(String pattern) {
        Collection<org.eclipse.smarthome.core.items.Item> eshItems = itemUIRegistry.getItems(pattern);
        Collection<Item> ohItems = new HashSet<Item>(eshItems.size());

        for(org.eclipse.smarthome.core.items.Item eshItem : eshItems) {
            ohItems.add(ItemMapper.mapToOpenHABItem(eshItem));
        }
        return ohItems;
    }

    @Override
    public boolean isValidItemName(String itemName) {
        return itemUIRegistry.isValidItemName(itemName);
    }

    @Override
    public void addItemRegistryChangeListener(ItemRegistryChangeListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeItemRegistryChangeListener(ItemRegistryChangeListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void added(org.eclipse.smarthome.core.items.Item element) {
        Item ohItem = ItemMapper.mapToOpenHABItem(element);
        for(ItemRegistryChangeListener listener : listeners) {
            listener.itemAdded(ohItem);
        }
    }

    @Override
    public void removed(org.eclipse.smarthome.core.items.Item element) {
        Item ohItem = ItemMapper.mapToOpenHABItem(element);
        for(ItemRegistryChangeListener listener : listeners) {
            listener.itemRemoved(ohItem);
        }
    }

    @Override
    public void updated(org.eclipse.smarthome.core.items.Item oldElement, org.eclipse.smarthome.core.items.Item element) {
        Item ohItem = ItemMapper.mapToOpenHABItem(element);
        for(ItemRegistryChangeListener listener : listeners) {
            listener.itemRemoved(ohItem);
            listener.itemAdded(ohItem);
        }
    }

    @Override
    public String getIcon(String itemName) {
        return itemUIRegistry.getIcon(itemName);
    }

    @Override
    public String getLabel(String itemName) {
        return itemUIRegistry.getLabel(itemName);
    }

    @Override
    public Widget getDefaultWidget(Class<? extends Item> itemType, String itemName) {
        return null;
    }

    @Override
    public Widget getWidget(String itemName) {
        return null;
    }

    @Override
    public String getLabel(Widget w) {
        return itemUIRegistry.getLabel(w.getItem());
    }

    @Override
    public String getIcon(Widget w) {
        return itemUIRegistry.getIcon(w.getItem());
    }

    @Override
    public State getState(Widget w) {
        return null;
    }

    @Override
    public Widget getWidget(Sitemap sitemap, String id) {
        return null;
    }

    @Override
    public String getWidgetId(Widget w) {
        return null;
    }

    @Override
    public EList<Widget> getChildren(LinkableWidget w) {
        return null;
    }

    @Override
    public boolean iconExists(String icon) {
        return itemUIRegistry.iconExists(icon);
    }

    @Override
    public String getLabelColor(Widget w) {
        return null;
    }

    @Override
    public String getValueColor(Widget w) {
        return null;
    }

    @Override
    public boolean getVisiblity(Widget w) {
        return true;
    }

    @Override
    public State getItemState(String itemName) {
        try {
            return getItem(itemName).getState();
        } catch (ItemNotFoundException e) {
            return null;
        }
    }

}
