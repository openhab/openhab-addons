/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.model.group;

import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.ItemConfigChangeHandler;
import org.openhab.binding.domintell.internal.protocol.StateChangeListener;
import org.openhab.binding.domintell.internal.protocol.message.ActionMessageBuilder;
import org.openhab.binding.domintell.internal.protocol.model.Discoverable;
import org.openhab.binding.domintell.internal.protocol.model.Item;
import org.openhab.binding.domintell.internal.protocol.model.ItemKey;
import org.openhab.binding.domintell.internal.protocol.model.type.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The {@link ItemGroup} class is container for all non-module Domintell items
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class ItemGroup extends Discoverable {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(ItemGroup.class);

    /**
     * Domintell connection
     */
    private DomintellConnection connection;

    /**
     * Group type
     */
    private ItemGroupType type;

    /**
     * Set of items
     */
    private Set<Item> items = new HashSet<>();

    /**
     * State change listener implementation
     */
    private StateChangeListener stateChangeListener;

    /**
     * Setup change listener implementation
     */
    private ItemConfigChangeHandler itemChangeListener;

    /**
     * Constructor
     *
     * @param connection Domintell connection
     * @param type Group type
     */
    public ItemGroup(DomintellConnection connection, ItemGroupType type) {
        this.type = type;
        this.connection = connection;
    }

    /**
     * Add new item
     *
     * @param item Item to add
     */
    public void addItem(Item item) {
        items.add(item);
        item.setStateChangeListener(stateChangeListener);
    }

    /**
     * Getter
     *
     * @return Group type
     */
    public ItemGroupType getType() {
        return type;
    }

    /**
     * Getter
     *
     * @return Item set
     */
    public Set<Item> getItems() {
        return Collections.unmodifiableSet(items);
    }

    /**
     * Setter for change listener. It sets the same listener for items.
     *
     * @param stateChangeListener Listener
     */
    public void setStateChangeListener(StateChangeListener stateChangeListener) {
        this.stateChangeListener = stateChangeListener;
        items.forEach(i -> i.setStateChangeListener(stateChangeListener));
    }

    /**
     * Setter
     *
     * @param itemChangeListener Item change listener
     */
    public void setItemChangeListener(ItemConfigChangeHandler itemChangeListener) {
        this.itemChangeListener = itemChangeListener;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    public void notifyItemsChanged(Item item) {
        if (itemChangeListener != null) {
            itemChangeListener.groupItemsChanged(item);
        }
    }

    /**
     * Notify the listeners about translation changes
     */
    public void notifyItemsTranslated() {
        if (itemChangeListener != null) {
            itemChangeListener.groupItemsTranslated();
        }
    }

    /**
     * Query state of the item's parent module
     *
     * @param itemKey Item key
     */
    public void queryState(ItemKey itemKey) {
        logger.debug("Updating state of item's parent module: {}", itemKey.getModuleKey());
        connection.sendCommand(ActionMessageBuilder.create()
                .withModuleKey(itemKey.getModuleKey())
                .withAction(ActionType.STATUS).build());
    }

    /**
     * Set boolean value
     *
     * @param itemKey Item key
     */
    public void setOutput(ItemKey itemKey) {
        logger.debug("Setting output: {}", itemKey);
        connection.sendCommand(ActionMessageBuilder.create()
                .withItemKey(itemKey)
                .withAction(ActionType.SET_OUTPUT)
                .build());
    }

    /**
     * Reset boolean output
     *
     * @param itemKey Item key
     */
    public void resetOutput(ItemKey itemKey) {
        logger.debug("Resetting output: {}", itemKey);
        connection.sendCommand(ActionMessageBuilder.create()
                .withItemKey(itemKey)
                .withAction(ActionType.RESET_OUTPUT)
                .build());
    }

    public void updateState() {
        items.forEach(i->connection.sendCommand(ActionMessageBuilder.create().withModuleKey(i.getItemKey().getModuleKey()).withAction(ActionType.STATUS).build()));
    }
}
