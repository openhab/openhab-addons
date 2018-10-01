/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.model.module;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.ItemConfigChangeHandler;
import org.openhab.binding.domintell.internal.protocol.StateChangeListener;
import org.openhab.binding.domintell.internal.protocol.message.ActionMessageBuilder;
import org.openhab.binding.domintell.internal.protocol.message.StatusMessage;
import org.openhab.binding.domintell.internal.protocol.model.*;
import org.openhab.binding.domintell.internal.protocol.model.type.ActionType;
import org.openhab.binding.domintell.internal.protocol.model.type.ItemType;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link Module} class is a base class for all supported Domintell modules
 *
 * @author Gabor Bicskei - Initial contribution
 */
public abstract class Module extends Discoverable {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(Module.class);

    /**
     * Connection
     */
    private DomintellConnection connection;

    /**
     * Module key
     */
    private ModuleKey moduleKey;

    /**
     * Module description received from APPINFO
     */
    private Description description;

    /**
     * Module items
     */
    private Map<ItemKey, Item> items = new HashMap<>();

    /**
     * Configuration change listener
     */
    private ItemConfigChangeHandler configChangeListener;

    /**
     * Constructor
     *
     * @param connection Connection
     * @param moduleType Module type
     * @param serialNumber Module serial number
     */
    protected Module(DomintellConnection connection, ModuleType moduleType, SerialNumber serialNumber) {
        this.connection = connection;
        this.moduleKey = new ModuleKey(moduleType, serialNumber);
        this.description = new Description(new StringBuilder().append(moduleType).append(" ").append(serialNumber.getAddressInt()).append("/").append(serialNumber.getAddressHex()).toString(), null, null);
    }

    /**
     * Getter
     *
     * @return Domintell connection
     */
    DomintellConnection getConnection() {
        return connection;
    }

    public ModuleKey getModuleKey() {
        return moduleKey;
    }

    public Description getDescription() {
        return description;
    }

    public Map<ItemKey, Item> getItems() {
        return items;
    }

    public void setConfigChangeListener(ItemConfigChangeHandler configChangeListener) {
        this.configChangeListener = configChangeListener;
    }

    public ItemConfigChangeHandler getConfigChangeListener() {
        return configChangeListener;
    }

    /**
     * Process module state update message
     *
     * @param message Received message
     */
    public void processStateUpdate(StatusMessage message) {
        String data = message.getData();
        if (data != null) {
            if (data.contains("[")) {
                logger.debug("Updating module description: {}->{}", getModuleKey(), data);
                if (message.getIoNumber() == null) {
                    description = Description.parseInfo(data);
                } else {
                    ItemKey key = new ItemKey(getModuleKey(), message.getIoNumber());
                    Item item = items.get(key);
                    if (item != null) {
                        item.setDescription(Description.parseInfo(message.getData()));
                    }
                }
            } else {
                logger.debug("Update module state: {}->{}", getModuleKey(), data);
                updateItems(message);
                for (Item item : items.values()) {
                    item.notifyStateUpdate();
                }
            }
        }
    }

    /**
     * Updates state of module items
     *
     * @param message Message to process
     */
    protected abstract void updateItems(@NonNull StatusMessage message);

    /**
     * Common helper for updating boolean type (input and output) items
     *
     * @param message Message to process
     */
    void updateBooleanItems(StatusMessage message) {
        String data = message.getData();
        Integer state = Integer.parseInt(data, 16);
        for (int i = 0; i < items.size(); i++) {
            int mask = 1 << i;
            Item item = items.get(new ItemKey(getModuleKey(), i + 1));
            item.setValue((state & mask) == mask);
        }
    }

    /**
     * Add new item to the module
     *
     * @param id Item id
     * @param itemType Item type
     * @param clazz Type parameter
     * @param <T> Item value type
     * @return Newly added module
     */
    <T> Item<T> addItem(Integer id, ItemType itemType, Class<T> clazz) {
        ItemKey key = new ItemKey(moduleKey, id);
        Item<T> item = new Item<>(key, this, itemType);
        items.put(key, item);
        return item;
    }

    /**
     * Add new item to the module
     *
     * @param name Item name
     * @param itemType Item type
     * @param clazz Type parameter
     * @param <T> Item value type
     * @return Newly added module
     */
    <T> Item<T> addItem(String name, ItemType itemType, Class<T> clazz) {
        ItemKey key = new ItemKey(moduleKey, name);
        Item<T> item = new Item<>(key, this, itemType);
        items.put(key, item);
        return item;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    /**
     * Notifies all event listeners about item level translation changes
     */
    public void notifyItemsTranslated() {
        if (configChangeListener != null) {
            configChangeListener.groupItemsTranslated();
        }
    }

    /**
     * Request status update for the parent Domintell module module
     */
    public void queryState() {
        connection.sendCommand(ActionMessageBuilder.create()
                .withModuleKey(moduleKey)
                .withAction(ActionType.STATUS).build());
    }

    public void setStateChangeListener(StateChangeListener listener) {
        getItems().values().forEach(i -> i.setStateChangeListener(listener));
    }

    public void updateState() {
        connection.sendCommand(ActionMessageBuilder.create().withModuleKey(moduleKey).withAction(ActionType.STATUS).build());
    }
}
