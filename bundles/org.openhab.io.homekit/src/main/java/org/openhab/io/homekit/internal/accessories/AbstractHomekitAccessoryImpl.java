/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
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
            HomekitAccessoryUpdater updater, HomekitSettings settings) {
        this.characteristics = characteristics;
        this.accessory = accessory;
        this.updater = updater;
        this.services = new ArrayList<>();
        this.settings = settings;
    }

    @NonNullByDefault
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
        return CompletableFuture.completedFuture(accessory.getItem().getName());
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

    @Override
    public Collection<Service> getServices() {
        return this.services;
    }

    protected HomekitAccessoryUpdater getUpdater() {
        return updater;
    }

    protected HomekitSettings getSettings() {
        return settings;
    }

    @NonNullByDefault
    protected void subscribe(HomekitCharacteristicType characteristicType,
            HomekitCharacteristicChangeCallback callback) {
        final Optional<HomekitTaggedItem> characteristic = getCharacteristic(characteristicType);
        if (characteristic.isPresent()) {
            getUpdater().subscribe((GenericItem) characteristic.get().getItem(), characteristicType.getTag(), callback);
        } else {
            logger.warn("Missing mandatory characteristic {}", characteristicType);
        }
    }

    @NonNullByDefault
    protected void unsubscribe(HomekitCharacteristicType characteristicType) {
        final Optional<HomekitTaggedItem> characteristic = getCharacteristic(characteristicType);
        if (characteristic.isPresent()) {
            getUpdater().unsubscribe((GenericItem) characteristic.get().getItem(), characteristicType.getTag());
        } else {
            logger.warn("Missing mandatory characteristic {}", characteristicType);
        }
    }

    protected @Nullable State getState(HomekitCharacteristicType characteristic) {
        final Optional<HomekitTaggedItem> taggedItem = getCharacteristic(characteristic);
        if (taggedItem.isPresent()) {
            return taggedItem.get().getItem().getState();
        }
        logger.debug("State for characteristic {} at accessory {} cannot be retrieved.", characteristic,
                accessory.getName());
        return null;
    }

    protected @Nullable <T extends State> T getStateAs(HomekitCharacteristicType characteristic, Class<T> type) {
        final State state = getState(characteristic);
        if (state != null) {
            return state.as(type);
        }
        return null;
    }

    protected @Nullable Double getStateAsTemperature(HomekitCharacteristicType characteristic) {
        return HomekitCharacteristicFactory.stateAsTemperature(getState(characteristic));
    }

    @NonNullByDefault
    protected <T extends Item> Optional<T> getItem(HomekitCharacteristicType characteristic, Class<T> type) {
        final Optional<HomekitTaggedItem> taggedItem = getCharacteristic(characteristic);
        if (taggedItem.isPresent()) {
            final Item item = taggedItem.get().getItem();
            if (type.isInstance(item)) {
                return Optional.of((T) item);
            } else {
                logger.warn("Unsupported item type for characteristic {} at accessory {}. Expected {}, got {}",
                        characteristic, accessory.getItem().getName(), type, taggedItem.get().getItem().getClass());
            }
        } else {
            logger.warn("Mandatory characteristic {} not found at accessory {}. ", characteristic,
                    accessory.getItem().getName());

        }
        return Optional.empty();
    }

    /**
     * return configuration attached to the root accessory, e.g. groupItem.
     * Note: result will be casted to the type of the default value.
     * The type for number is BigDecimal.
     *
     * @param key configuration key
     * @param defaultValue default value
     * @param <T> expected type
     * @return configuration value
     */
    @NonNullByDefault
    protected <T> T getAccessoryConfiguration(String key, T defaultValue) {
        return accessory.getConfiguration(key, defaultValue);
    }

    /**
     * return configuration attached to the root accessory, e.g. groupItem.
     *
     * @param key configuration key
     * @param defaultValue default value
     * @return configuration value
     */
    @NonNullByDefault
    protected boolean getAccessoryConfigurationAsBoolean(String key, boolean defaultValue) {
        return accessory.getConfigurationAsBoolean(key, defaultValue);
    }

    /**
     * return configuration of the characteristic item, e.g. currentTemperature.
     * Note: result will be casted to the type of the default value.
     * The type for number is BigDecimal.
     *
     * @param characteristicType characteristic type
     * @param key configuration key
     * @param defaultValue default value
     * @param <T> expected type
     * @return configuration value
     */
    @NonNullByDefault
    protected <T> T getAccessoryConfiguration(HomekitCharacteristicType characteristicType, String key,
            T defaultValue) {
        return getCharacteristic(characteristicType)
                .map(homekitTaggedItem -> homekitTaggedItem.getConfiguration(key, defaultValue)).orElse(defaultValue);
    }

    /**
     * update mapping with values from item configuration.
     * it checks for all keys from the mapping whether there is configuration at item with the same key and if yes,
     * replace the value.
     *
     * @param characteristicType characteristicType to identify item
     * @param map mapping to update
     * @param customEnumList list to store custom state enumeration
     */
    @NonNullByDefault
    protected <T> void updateMapping(HomekitCharacteristicType characteristicType, Map<T, String> map,
            @Nullable List<T> customEnumList) {
        getCharacteristic(characteristicType).ifPresent(c -> {
            final Map<String, Object> configuration = c.getConfiguration();
            if (configuration != null) {
                map.forEach((k, current_value) -> {
                    final Object new_value = configuration.get(k.toString());
                    if (new_value instanceof String) {
                        map.put(k, (String) new_value);
                        if (customEnumList != null) {
                            customEnumList.add(k);
                        }
                    }
                });
            }
        });
    }

    @NonNullByDefault
    protected <T> void updateMapping(HomekitCharacteristicType characteristicType, Map<T, String> map) {
        updateMapping(characteristicType, map, null);
    }

    /**
     * takes item state as value and retrieves the key for that value from mapping.
     * e.g. used to map StringItem value to HomeKit Enum
     *
     * @param characteristicType characteristicType to identify item
     * @param mapping mapping
     * @param defaultValue default value if nothing found in mapping
     * @param <T> type of the result derived from
     * @return key for the value
     */
    @NonNullByDefault
    protected <T> T getKeyFromMapping(HomekitCharacteristicType characteristicType, Map<T, String> mapping,
            T defaultValue) {
        final Optional<HomekitTaggedItem> c = getCharacteristic(characteristicType);
        if (c.isPresent()) {
            final State state = c.get().getItem().getState();
            logger.trace("getKeyFromMapping: characteristic {}, state {}, mapping {}", characteristicType.getTag(),
                    state, mapping);
            if (state instanceof StringType) {
                return mapping.entrySet().stream().filter(entry -> state.toString().equalsIgnoreCase(entry.getValue()))
                        .findAny().map(Entry::getKey).orElseGet(() -> {
                            logger.warn(
                                    "Wrong value {} for {} characteristic of the item {}. Expected one of following {}. Returning {}.",
                                    state.toString(), characteristicType.getTag(), c.get().getName(), mapping.values(),
                                    defaultValue);
                            return defaultValue;
                        });
            }
        }
        return defaultValue;
    }

    @NonNullByDefault
    protected void addCharacteristic(HomekitTaggedItem characteristic) {
        characteristics.add(characteristic);
    }

    /**
     * create boolean reader with ON state mapped to trueOnOffValue or trueOpenClosedValue depending of item type
     *
     * @param characteristicType characteristic id
     * @param trueOnOffValue ON value for switch
     * @param trueOpenClosedValue ON value for contact
     * @return boolean read
     * @throws IncompleteAccessoryException
     */
    @NonNullByDefault
    protected BooleanItemReader createBooleanReader(HomekitCharacteristicType characteristicType,
            OnOffType trueOnOffValue, OpenClosedType trueOpenClosedValue) throws IncompleteAccessoryException {
        return new BooleanItemReader(
                getItem(characteristicType, GenericItem.class)
                        .orElseThrow(() -> new IncompleteAccessoryException(characteristicType)),
                trueOnOffValue, trueOpenClosedValue);
    }

    /**
     * create boolean reader for a number item with ON state mapped to the value of the
     * item being above a given threshold
     *
     * @param characteristicType characteristic id
     * @param trueThreshold threshold for true of number item
     * @param invertThreshold result is true if item is less than threshold, instead of more
     * @return boolean read
     * @throws IncompleteAccessoryException
     */
    @NonNullByDefault
    protected BooleanItemReader createBooleanReader(HomekitCharacteristicType characteristicType,
            BigDecimal trueThreshold, boolean invertThreshold) throws IncompleteAccessoryException {
        final HomekitTaggedItem taggedItem = getCharacteristic(characteristicType)
                .orElseThrow(() -> new IncompleteAccessoryException(characteristicType));
        return new BooleanItemReader(taggedItem.getItem(), taggedItem.isInverted() ? OnOffType.OFF : OnOffType.ON,
                taggedItem.isInverted() ? OpenClosedType.CLOSED : OpenClosedType.OPEN, trueThreshold, invertThreshold);
    }

    /**
     * create boolean reader with default ON/OFF mapping considering inverted flag
     *
     * @param characteristicType characteristic id
     * @return boolean reader
     * @throws IncompleteAccessoryException
     */
    @NonNullByDefault
    protected BooleanItemReader createBooleanReader(HomekitCharacteristicType characteristicType)
            throws IncompleteAccessoryException {
        final HomekitTaggedItem taggedItem = getCharacteristic(characteristicType)
                .orElseThrow(() -> new IncompleteAccessoryException(characteristicType));
        return new BooleanItemReader(taggedItem.getItem(), taggedItem.isInverted() ? OnOffType.OFF : OnOffType.ON,
                taggedItem.isInverted() ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
    }
}
