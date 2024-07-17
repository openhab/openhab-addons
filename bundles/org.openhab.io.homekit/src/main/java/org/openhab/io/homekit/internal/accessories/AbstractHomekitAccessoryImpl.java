/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.State;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitException;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.accessories.HomekitAccessory;
import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.CharacteristicEnum;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.accessoryinformation.FirmwareRevisionCharacteristic;
import io.github.hapjava.characteristics.impl.accessoryinformation.HardwareRevisionCharacteristic;
import io.github.hapjava.characteristics.impl.accessoryinformation.IdentifyCharacteristic;
import io.github.hapjava.characteristics.impl.accessoryinformation.ManufacturerCharacteristic;
import io.github.hapjava.characteristics.impl.accessoryinformation.ModelCharacteristic;
import io.github.hapjava.characteristics.impl.accessoryinformation.SerialNumberCharacteristic;
import io.github.hapjava.characteristics.impl.base.BaseCharacteristic;
import io.github.hapjava.characteristics.impl.common.NameCharacteristic;
import io.github.hapjava.services.Service;
import io.github.hapjava.services.impl.AccessoryInformationService;

/**
 * Abstract class for Homekit Accessory implementations, this provides the
 * accessory metadata using information from the underlying Item.
 *
 * @author Andy Lintner - Initial contribution
 */
public abstract class AbstractHomekitAccessoryImpl implements HomekitAccessory {
    private final Logger logger = LoggerFactory.getLogger(AbstractHomekitAccessoryImpl.class);
    private final List<HomekitTaggedItem> characteristics;
    private final HomekitTaggedItem accessory;
    private final HomekitAccessoryUpdater updater;
    private final HomekitSettings settings;
    private final List<Service> services;
    private final Map<Class<? extends Characteristic>, Characteristic> rawCharacteristics;
    private boolean isLinkedService = false;

    public AbstractHomekitAccessoryImpl(HomekitTaggedItem accessory, List<HomekitTaggedItem> mandatoryCharacteristics,
            List<Characteristic> mandatoryRawCharacteristics, HomekitAccessoryUpdater updater,
            HomekitSettings settings) {
        this.characteristics = mandatoryCharacteristics;
        this.accessory = accessory;
        this.updater = updater;
        this.services = new ArrayList<>();
        this.settings = settings;
        this.rawCharacteristics = new HashMap<>();
        // create raw characteristics for mandatory characteristics
        characteristics.forEach(c -> {
            var rawCharacteristic = HomekitCharacteristicFactory.createNullableCharacteristic(c, updater);
            // not all mandatory characteristics are creatable via HomekitCharacteristicFactory (yet)
            if (rawCharacteristic != null) {
                rawCharacteristics.put(rawCharacteristic.getClass(), rawCharacteristic);
            }
        });
        mandatoryRawCharacteristics.forEach(c -> {
            if (rawCharacteristics.get(c.getClass()) != null) {
                logger.warn(
                        "Accessory {} already has a characteristic of type {}; ignoring additional definition from metadata.",
                        accessory.getName(), c.getClass().getSimpleName());
            } else {
                rawCharacteristics.put(c.getClass(), c);
            }
        });
    }

    /**
     * Gives an accessory an opportunity to populate additional characteristics after all optional
     * charactericteristics have been added.
     * 
     * @throws HomekitException
     */
    public void init() throws HomekitException {
        // initialize the AccessoryInformation Service with defaults if not specified
        if (!rawCharacteristics.containsKey(NameCharacteristic.class)) {
            rawCharacteristics.put(NameCharacteristic.class, new NameCharacteristic(() -> {
                return CompletableFuture.completedFuture(accessory.getItem().getLabel());
            }));
        }

        if (!isLinkedService()) {
            if (!rawCharacteristics.containsKey(IdentifyCharacteristic.class)) {
                rawCharacteristics.put(IdentifyCharacteristic.class, new IdentifyCharacteristic(v -> {
                }));
            }
            if (!rawCharacteristics.containsKey(ManufacturerCharacteristic.class)) {
                rawCharacteristics.put(ManufacturerCharacteristic.class, new ManufacturerCharacteristic(() -> {
                    return CompletableFuture.completedFuture("none");
                }));
            }
            if (!rawCharacteristics.containsKey(ModelCharacteristic.class)) {
                rawCharacteristics.put(ModelCharacteristic.class, new ModelCharacteristic(() -> {
                    return CompletableFuture.completedFuture("none");
                }));
            }
            if (!rawCharacteristics.containsKey(SerialNumberCharacteristic.class)) {
                rawCharacteristics.put(SerialNumberCharacteristic.class, new SerialNumberCharacteristic(() -> {
                    return CompletableFuture.completedFuture(accessory.getItem().getName());
                }));
            }
            if (!rawCharacteristics.containsKey(FirmwareRevisionCharacteristic.class)) {
                rawCharacteristics.put(FirmwareRevisionCharacteristic.class, new FirmwareRevisionCharacteristic(() -> {
                    return CompletableFuture.completedFuture("none");
                }));
            }

            var service = new AccessoryInformationService(getCharacteristic(IdentifyCharacteristic.class).get(),
                    getCharacteristic(ManufacturerCharacteristic.class).get(),
                    getCharacteristic(ModelCharacteristic.class).get(),
                    getCharacteristic(NameCharacteristic.class).get(),
                    getCharacteristic(SerialNumberCharacteristic.class).get(),
                    getCharacteristic(FirmwareRevisionCharacteristic.class).get());

            getCharacteristic(HardwareRevisionCharacteristic.class)
                    .ifPresent(c -> service.addOptionalCharacteristic(c));

            // make sure this is the first service
            services.add(0, service);
        }
    }

    /**
     * @param parentAccessory The primary service to link to.
     * @return If this accessory should be nested as a linked service below a primary service,
     *         rather than as a sibling.
     */
    public boolean isLinkable(HomekitAccessory parentAccessory) {
        return false;
    }

    /**
     * Sets if this accessory is being used as a linked service.
     */
    public void setIsLinkedService(boolean value) {
        isLinkedService = value;
    }

    /**
     * @return If this accessory is being used as a linked service.
     */
    public boolean isLinkedService() {
        return isLinkedService;
    }

    /**
     * @return If this accessory is only valid as a linked service, not as a standalone accessory.
     */
    public boolean isLinkedServiceOnly() {
        return false;
    }

    @NonNullByDefault
    public Optional<HomekitTaggedItem> getCharacteristic(HomekitCharacteristicType type) {
        return characteristics.stream().filter(c -> c.getCharacteristicType() == type).findAny();
    }

    @Override
    public int getId() {
        return accessory.getId();
    }

    @Override
    public CompletableFuture<String> getName() {
        return getCharacteristic(NameCharacteristic.class).get().getValue();
    }

    @Override
    public CompletableFuture<String> getManufacturer() {
        return getCharacteristic(ManufacturerCharacteristic.class).get().getValue();
    }

    @Override
    public CompletableFuture<String> getModel() {
        return getCharacteristic(ModelCharacteristic.class).get().getValue();
    }

    @Override
    public CompletableFuture<String> getSerialNumber() {
        return getCharacteristic(SerialNumberCharacteristic.class).get().getValue();
    }

    @Override
    public CompletableFuture<String> getFirmwareRevision() {
        return getCharacteristic(FirmwareRevisionCharacteristic.class).get().getValue();
    }

    @Override
    public void identify() {
        try {
            getCharacteristic(IdentifyCharacteristic.class).get().setValue(true);
        } catch (Exception e) {
            // ignore
        }
    }

    public HomekitTaggedItem getRootAccessory() {
        return accessory;
    }

    @Override
    public Collection<Service> getServices() {
        return this.services;
    }

    public void addService(Service service) {
        services.add(service);

        var serviceClass = service.getClass();
        rawCharacteristics.values().forEach(characteristic -> {
            // belongs on the accessory information service
            if (characteristic.getClass() == NameCharacteristic.class) {
                return;
            }
            try {
                // if the service supports adding this characteristic as optional, add it!
                serviceClass.getMethod("addOptionalCharacteristic", characteristic.getClass()).invoke(service,
                        characteristic);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // the service doesn't support this optional characteristic; ignore it
            }
        });
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

    @NonNullByDefault
    protected <T extends Enum<T> & CharacteristicEnum> Map<T, String> createMapping(
            HomekitCharacteristicType characteristicType, Class<T> klazz) {
        return createMapping(characteristicType, klazz, null, false);
    }

    @NonNullByDefault
    protected <T extends Enum<T> & CharacteristicEnum> Map<T, String> createMapping(
            HomekitCharacteristicType characteristicType, Class<T> klazz, boolean inverted) {
        return createMapping(characteristicType, klazz, null, inverted);
    }

    @NonNullByDefault
    protected <T extends Enum<T> & CharacteristicEnum> Map<T, String> createMapping(
            HomekitCharacteristicType characteristicType, Class<T> klazz, @Nullable List<T> customEnumList) {
        return createMapping(characteristicType, klazz, customEnumList, false);
    }

    /**
     * create mapping with values from item configuration
     * 
     * @param characteristicType to identify item; must be present
     * @param customEnumList list to store custom state enumeration
     * @param inverted if ON/OFF and OPEN/CLOSED should be inverted by default (inverted on the item will double-invert)
     * @return mapping of enum values to custom string values
     */
    @NonNullByDefault
    protected <T extends Enum<T> & CharacteristicEnum> Map<T, String> createMapping(
            HomekitCharacteristicType characteristicType, Class<T> klazz, @Nullable List<T> customEnumList,
            boolean inverted) {
        HomekitTaggedItem item = getCharacteristic(characteristicType).get();
        return HomekitCharacteristicFactory.createMapping(item, klazz, customEnumList, inverted);
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
    public <T> T getKeyFromMapping(HomekitCharacteristicType characteristicType, Map<T, String> mapping,
            T defaultValue) {
        final Optional<HomekitTaggedItem> c = getCharacteristic(characteristicType);
        if (c.isPresent()) {
            return HomekitCharacteristicFactory.getKeyFromMapping(c.get(), mapping, defaultValue);
        }
        return defaultValue;
    }

    @NonNullByDefault
    protected void addCharacteristic(HomekitTaggedItem item, Characteristic characteristic)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        characteristics.add(item);
        addCharacteristic(characteristic);
    }

    /**
     * If the primary service does not yet exist, it won't be added to it. It's the resposibility
     * of the caller to add characteristics when the primary service is created.
     *
     * @param characteristic
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @NonNullByDefault
    public void addCharacteristic(Characteristic characteristic)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (rawCharacteristics.containsKey(characteristic.getClass())) {
            logger.warn("Accessory {} already has a characteristic of type {}; ignoring additional definition.",
                    accessory.getName(), characteristic.getClass().getSimpleName());
            return;
        }
        rawCharacteristics.put(characteristic.getClass(), characteristic);
        var service = getPrimaryService();
        if (service != null) {
            // find the corresponding add method at service and call it.
            service.getClass().getMethod("addOptionalCharacteristic", characteristic.getClass()).invoke(service,
                    characteristic);
        }
    }

    /**
     * Takes the NameCharacteristic that normally exists on the AccessoryInformationService,
     * and puts it on the primary service.
     */
    public void promoteNameCharacteristic() {
        var characteristic = getCharacteristic(NameCharacteristic.class);
        if (!characteristic.isPresent()) {
            return;
        }

        var service = getPrimaryService();
        if (service != null) {
            try {
                // find the corresponding add method at service and call it.
                service.getClass().getMethod("addOptionalCharacteristic", NameCharacteristic.class).invoke(service,
                        characteristic.get());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // This should never happen; all services should support NameCharacteristic as an optional
                // Characteristic.
                // If HAP-Java defined a service that doesn't support addOptionalCharacteristic(NameCharacteristic),
                // Then it's a bug there, and we're just going to ignore the exception here.
            }
        }
    }

    @NonNullByDefault
    public <T> Optional<T> getCharacteristic(Class<? extends T> klazz) {
        return Optional.ofNullable((T) rawCharacteristics.get(klazz));
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
        return new BooleanItemReader(taggedItem.getItem(), OnOffType.from(!taggedItem.isInverted()),
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
        return new BooleanItemReader(taggedItem.getItem(), OnOffType.from(!taggedItem.isInverted()),
                taggedItem.isInverted() ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
    }

    /**
     * Calculates a string as json of the configuration for this accessory, suitable for seeing
     * if the structure has changed, and building a dummy accessory for it. It is _not_ suitable
     * for actual publishing to by HAP-Java to iOS devices, since all the IIDs will be set to 0.
     * The IIDs will get replaced by actual values by HAP-Java inside of DummyHomekitCharacteristic.
     */
    public String toJson() {
        var builder = Json.createArrayBuilder();
        getServices().forEach(s -> {
            builder.add(serviceToJson(s));
        });
        return builder.build().toString();
    }

    private JsonObjectBuilder serviceToJson(Service service) {
        var serviceBuilder = Json.createObjectBuilder();
        serviceBuilder.add("type", service.getType());
        var characteristics = Json.createArrayBuilder();

        service.getCharacteristics().stream().sorted((l, r) -> l.getClass().getName().compareTo(r.getClass().getName()))
                .forEach(c -> {
                    try {
                        var cJson = c.toJson(0).get();
                        var cBuilder = Json.createObjectBuilder();
                        // Need to copy over everything except the current value, which we instead
                        // reach in and get the default value
                        cJson.forEach((k, v) -> {
                            if ("value".equals(k)) {
                                Object defaultValue = ((BaseCharacteristic) c).getDefault();
                                if (defaultValue instanceof Boolean) {
                                    cBuilder.add("value", (boolean) defaultValue);
                                } else if (defaultValue instanceof Integer) {
                                    cBuilder.add("value", (int) defaultValue);
                                } else if (defaultValue instanceof Double) {
                                    cBuilder.add("value", (double) defaultValue);
                                } else {
                                    cBuilder.add("value", defaultValue.toString());
                                }
                            } else {
                                cBuilder.add(k, v);
                            }
                        });
                        characteristics.add(cBuilder.build());
                    } catch (InterruptedException | ExecutionException e) {
                    }
                });
        serviceBuilder.add("c", characteristics);

        if (!service.getLinkedServices().isEmpty()) {
            var linkedServices = Json.createArrayBuilder();
            service.getLinkedServices().forEach(s -> linkedServices.add(serviceToJson(s)));
            serviceBuilder.add("ls", linkedServices);
        }
        return serviceBuilder;
    }
}
