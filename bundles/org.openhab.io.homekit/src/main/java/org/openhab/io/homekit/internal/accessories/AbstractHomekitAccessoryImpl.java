/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.io.homekit.internal.accessories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.accessories.HomekitAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.services.Service;

/**
 * Abstract class for Homekit Accessory implementations, this provides the
 * accessory metadata using information from the underlying Item.
 *
 * @author Andy Lintner - Initial contribution
 */
abstract class AbstractHomekitAccessoryImpl<T extends GenericItem> implements HomekitAccessory {
    private Logger LOGGER = LoggerFactory.getLogger(AbstractHomekitAccessoryImpl.class);

    protected final List<HomekitTaggedItem> mandatoryCharacteristics;
    private final int accessoryId;
    private final String itemName;
    private final String itemLabel;
    private final ItemRegistry itemRegistry;
    private final HomekitAccessoryUpdater updater;
    private final HomekitSettings settings;
    private final List<Service> services;

    public AbstractHomekitAccessoryImpl(HomekitTaggedItem accessory, List<HomekitTaggedItem> mandatoryCharacteristics, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        this.mandatoryCharacteristics = mandatoryCharacteristics;
        this.accessoryId = accessory.getId();
        this.itemName = accessory.getItem().getName();
        this.itemLabel = accessory.getItem().getLabel();
        this.itemRegistry = itemRegistry;
        this.updater = updater;
        this.services = new ArrayList<>();
        this.settings = settings;
    }

    protected Optional<HomekitTaggedItem> getMandatoryCharacteristic(HomekitCharacteristicType type) {
        return  mandatoryCharacteristics.stream().filter(c -> c.getCharacteristicType().equals(type)).findAny();
    }
    @Override
    public int getId() {
        return accessoryId;
    }

    @Override
    public CompletableFuture<String> getName() {
        return CompletableFuture.completedFuture(itemLabel);
    }

    @Override
    public CompletableFuture<String> getManufacturer() {
        return CompletableFuture.completedFuture("none");
    }

    @Override
    public CompletableFuture<String> getModel() {
        return CompletableFuture.completedFuture("none");
    }

    @Override
    public CompletableFuture<String> getSerialNumber() {
        return CompletableFuture.completedFuture("none");
    }

    @Override
    public CompletableFuture<String> getFirmwareRevision() {
        return CompletableFuture.completedFuture("none");
    }

    @Override
    public void identify() {
        // We're not going to support this for now
    }

    public Collection<Service> getServices() {
        return this.services;
    };

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

    protected HomekitSettings getSettings() {
        return settings;
    }

    protected void subscribeToCharacteristic(HomekitCharacteristicType characteristicType, HomekitCharacteristicChangeCallback callback) {
        final Optional<HomekitTaggedItem> characteristic = getMandatoryCharacteristic(characteristicType);
        if (characteristic.isPresent()) {
            getUpdater().subscribe((GenericItem ) characteristic.get().getItem(), callback);
        }  else {
            LOGGER.error("Missing mandatory characteristic {}", characteristicType);
        }
    }

    protected void unsubscribeFromCharacteristic(HomekitCharacteristicType characteristicType) {
        final Optional<HomekitTaggedItem> characteristic = getMandatoryCharacteristic(characteristicType);
        if (characteristic.isPresent()) {
            getUpdater().unsubscribe((GenericItem ) characteristic.get().getItem());
        }  else {
            LOGGER.error("Missing mandatory characteristic {}", characteristicType);
        }
    }
}
