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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.types.State;
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
abstract class AbstractHomekitAccessoryImpl implements HomekitAccessory {
    private final Logger logger = LoggerFactory.getLogger(AbstractHomekitAccessoryImpl.class);
    private final List<HomekitTaggedItem> characteristics;
    private final HomekitTaggedItem accessory;
    private final HomekitAccessoryUpdater updater;
    private final HomekitSettings settings;
    private final List<Service> services;

    public AbstractHomekitAccessoryImpl(HomekitTaggedItem accessory, List<HomekitTaggedItem> characteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        this.characteristics = characteristics;
        this.accessory = accessory;
        this.updater = updater;
        this.services = new ArrayList<>();
        this.settings = settings;
    }

    protected Optional<HomekitTaggedItem> getCharacteristic(HomekitCharacteristicType type) {
        return characteristics.stream().filter(c -> c.getCharacteristicType() == type).findAny();
    }

    @Override
    public int getId() {
        return accessory.getId();
    }

    @Override
    public CompletableFuture<String> getName() {
        return CompletableFuture.completedFuture(accessory.getItem().getLabel());
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

    public HomekitTaggedItem getRootAccessory() {
        return accessory;
    }

    public Collection<Service> getServices() {
        return this.services;
    }

    protected HomekitAccessoryUpdater getUpdater() {
        return updater;
    }

    protected HomekitSettings getSettings() {
        return settings;
    }

    protected void subscribe(HomekitCharacteristicType characteristicType,
            HomekitCharacteristicChangeCallback callback) {
        final Optional<HomekitTaggedItem> characteristic = getCharacteristic(characteristicType);
        if (characteristic.isPresent()) {
            getUpdater().subscribe((GenericItem) characteristic.get().getItem(), characteristicType.getTag(), callback);
        } else {
            logger.warn("Missing mandatory characteristic {}", characteristicType);
        }
    }

    protected void unsubscribe(HomekitCharacteristicType characteristicType) {
        final Optional<HomekitTaggedItem> characteristic = getCharacteristic(characteristicType);
        if (characteristic.isPresent()) {
            getUpdater().unsubscribe((GenericItem) characteristic.get().getItem(), characteristicType.getTag());
        } else {
            logger.warn("Missing mandatory characteristic {}", characteristicType);
        }
    }

    protected @Nullable <T extends State> T getStateAs(HomekitCharacteristicType characteristic, Class<T> type) {
        final Optional<HomekitTaggedItem> taggedItem = getCharacteristic(characteristic);
        if (taggedItem.isPresent()) {
            final State state = taggedItem.get().getItem().getStateAs(type);
            if (state != null) {
                return (T) state.as(type);
            }
        }
        logger.warn("State for characteristic {} at accessory {} cannot be retrieved.", characteristic,
                accessory.getId());
        return null;
    }

    @SuppressWarnings("unchecked")
    protected @Nullable <T extends Item> T getItem(HomekitCharacteristicType characteristic, Class<T> type) {
        final Optional<HomekitTaggedItem> taggedItem = getCharacteristic(characteristic);
        if (taggedItem.isPresent()) {
            if (type.isInstance(taggedItem.get().getItem()))
                return (T) taggedItem.get().getItem();
            else
                logger.warn("Unsupported item type for characteristic {} at accessory {}. Expected {}, got {}",
                        characteristic, accessory.getItem().getLabel(), type, taggedItem.get().getItem().getClass());
        } else {
            logger.warn("Mandatory characteristic {} not found at accessory {}. ", characteristic,
                    accessory.getItem().getLabel());

        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getAccessoryConfiguration(String key, @NonNull T defaultValue) {
        final @Nullable Map<String, Object> configuration = accessory.getConfiguration();
        if (configuration != null) {
            Object value = configuration.get(key);
            if (value != null && value.getClass().equals(defaultValue.getClass())) {
                return (T) value;
            }
        }
        return defaultValue;
    }

    protected void addCharacteristic(HomekitTaggedItem characteristic) {
        characteristics.add(characteristic);
    }
}
