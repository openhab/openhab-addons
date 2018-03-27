/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitAccessory;

/**
 * Abstract class for HomekitAccessory implementations, this provides the
 * accessory metadata using information from the underlying Item.
 *
 * @author Andy Lintner
 */
abstract class AbstractHomekitAccessoryImpl<T extends GenericItem> implements HomekitAccessory {

    private final int accessoryId;
    private final String itemName;
    private final String itemLabel;
    private final ItemRegistry itemRegistry;
    private final HomekitAccessoryUpdater updater;

    private Logger logger = LoggerFactory.getLogger(AbstractHomekitAccessoryImpl.class);

    public AbstractHomekitAccessoryImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, Class<T> expectedItemClass) {
        this.accessoryId = taggedItem.getId();
        this.itemName = taggedItem.getItem().getName();
        this.itemLabel = taggedItem.getItem().getLabel();
        this.itemRegistry = itemRegistry;
        this.updater = updater;
        Item baseItem = taggedItem.getItem();
        if (baseItem instanceof GroupItem && ((GroupItem) baseItem).getBaseItem() != null) {
            baseItem = ((GroupItem) baseItem).getBaseItem();
        }
        if (expectedItemClass != taggedItem.getItem().getClass()
                && !expectedItemClass.isAssignableFrom(baseItem.getClass())) {
            logger.error("Type {} is a {} instead of the expected {}", taggedItem.getItem().getName(),
                    baseItem.getClass().getName(), expectedItemClass.getName());
        }
    }

    @Override
    public int getId() {
        return accessoryId;
    }

    @Override
    public String getLabel() {
        return itemLabel;
    }

    @Override
    public String getManufacturer() {
        return "none";
    }

    @Override
    public String getModel() {
        return "none";
    }

    @Override
    public String getSerialNumber() {
        return "none";
    }

    @Override
    public void identify() {
        // We're not going to support this for now
    }

    protected ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    protected String getItemName() {
        return itemName;
    }

    protected HomekitAccessoryUpdater getUpdater() {
        return updater;
    }

    protected GenericItem getItem() {
        return (GenericItem) getItemRegistry().get(getItemName());
    }
}
