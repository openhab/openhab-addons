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

import static org.openhab.io.homekit.internal.HomekitAccessoryType.*;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.*;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.items.MetadataKey;
import org.eclipse.smarthome.core.items.MetadataRegistry;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.openhab.io.homekit.internal.HomekitAccessoryType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitException;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.accessories.HomekitAccessory;
import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.services.Service;

/**
 * Creates a HomekitAccessory for a given HomekitTaggedItem.
 *
 * @author Andy Lintner - Initial contribution
 * @author Eugen Freiter - refactoring for optional characteristics
 */
@NonNullByDefault
@SuppressWarnings("deprecation")
public class HomekitAccessoryFactory {
    private static final Logger logger = LoggerFactory.getLogger(HomekitAccessoryFactory.class);
    public final static String METADATA_KEY = "homekit"; // prefix for HomeKit meta information in items.xml

    /** List of mandatory attributes for each accessory type. **/
    private final static Map<HomekitAccessoryType, HomekitCharacteristicType[]> MANDATORY_CHARACTERISTICS = new HashMap<HomekitAccessoryType, HomekitCharacteristicType[]>() {
        {
            put(LEAK_SENSOR, new HomekitCharacteristicType[] { LEAK_DETECTED_STATE });
            put(MOTION_SENSOR, new HomekitCharacteristicType[] { MOTION_DETECTED_STATE });
            put(OCCUPANCY_SENSOR, new HomekitCharacteristicType[] { OCCUPANCY_DETECTED_STATE });
            put(CONTACT_SENSOR, new HomekitCharacteristicType[] { CONTACT_SENSOR_STATE });
            put(SMOKE_SENSOR, new HomekitCharacteristicType[] { SMOKE_DETECTED_STATE });
            put(HUMIDITY_SENSOR, new HomekitCharacteristicType[] { RELATIVE_HUMIDITY });
            put(SWITCH, new HomekitCharacteristicType[] { ON_STATE });
            put(CARBON_DIOXIDE_SENSOR, new HomekitCharacteristicType[] { CARBON_DIOXIDE_DETECTED_STATE });
            put(CARBON_MONOXIDE_SENSOR, new HomekitCharacteristicType[] { CARBON_MONOXIDE_DETECTED_STATE });
            put(WINDOW_COVERING, new HomekitCharacteristicType[] { TARGET_POSITION, CURRENT_POSITION, POSITION_STATE });
            put(LIGHTBULB, new HomekitCharacteristicType[] { ON_STATE });
            put(FAN, new HomekitCharacteristicType[] { ACTIVE_STATUS });
            put(TEMPERATURE_SENSOR, new HomekitCharacteristicType[] { CURRENT_TEMPERATURE });
            put(THERMOSTAT, new HomekitCharacteristicType[] { CURRENT_HEATING_COOLING_STATE,
                    TARGET_HEATING_COOLING_STATE, CURRENT_TEMPERATURE, TARGET_TEMPERATURE });
            put(LOCK, new HomekitCharacteristicType[] { LOCK_CURRENT_STATE, LOCK_TARGET_STATE });
            put(VALVE, new HomekitCharacteristicType[] { ACTIVE_STATUS, INUSE_STATUS });
            put(SECURITY_SYSTEM,
                    new HomekitCharacteristicType[] { SECURITY_SYSTEM_CURRENT_STATE, SECURITY_SYSTEM_TARGET_STATE });
            put(OUTLET, new HomekitCharacteristicType[] { ON_STATE, INUSE_STATUS });
            put(SPEAKER, new HomekitCharacteristicType[] { MUTE });
            put(GARAGE_DOOR_OPENER,
                    new HomekitCharacteristicType[] { CURRENT_DOOR_STATE, TARGET_DOOR_STATE, OBSTRUCTION_STATUS });

            // LEGACY
            put(BLINDS, new HomekitCharacteristicType[] { TARGET_POSITION, CURRENT_POSITION, POSITION_STATE });
            put(OLD_HUMIDITY_SENSOR, new HomekitCharacteristicType[] { RELATIVE_HUMIDITY });
            put(OLD_DIMMABLE_LIGHTBULB, new HomekitCharacteristicType[] { ON_STATE });
            put(OLD_COLORFUL_LIGHTBULB, new HomekitCharacteristicType[] { ON_STATE });
        }
    };

    /** List of service implementation for each accessory type. **/
    private final static Map<HomekitAccessoryType, Class<? extends AbstractHomekitAccessoryImpl>> SERVICE_IMPL_MAP = new HashMap<HomekitAccessoryType, Class<? extends AbstractHomekitAccessoryImpl>>() {
        {
            put(LEAK_SENSOR, HomekitLeakSensorImpl.class);
            put(MOTION_SENSOR, HomekitMotionSensorImpl.class);
            put(OCCUPANCY_SENSOR, HomekitOccupancySensorImpl.class);
            put(CONTACT_SENSOR, HomekitContactSensorImpl.class);
            put(SMOKE_SENSOR, HomekitSmokeSensorImpl.class);
            put(HUMIDITY_SENSOR, HomekitHumiditySensorImpl.class);
            put(SWITCH, HomekitSwitchImpl.class);
            put(CARBON_DIOXIDE_SENSOR, HomekitCarbonDioxideSensorImpl.class);
            put(CARBON_MONOXIDE_SENSOR, HomekitCarbonMonoxideSensorImpl.class);
            put(WINDOW_COVERING, HomekitWindowCoveringImpl.class);
            put(LIGHTBULB, HomekitLightbulbImpl.class);
            put(FAN, HomekitFanImpl.class);
            put(TEMPERATURE_SENSOR, HomekitTemperatureSensorImpl.class);
            put(THERMOSTAT, HomekitThermostatImpl.class);
            put(LOCK, HomekitLockImpl.class);
            put(VALVE, HomekitValveImpl.class);
            put(SECURITY_SYSTEM, HomekitSecuritySystemImpl.class);
            put(OUTLET, HomekitOutletImpl.class);
            put(SPEAKER, HomekitSpeakerImpl.class);
            put(GARAGE_DOOR_OPENER, HomekitGarageDoorOpenerImpl.class);
            put(BLINDS, HomekitWindowCoveringImpl.class);
            put(OLD_HUMIDITY_SENSOR, HomekitHumiditySensorImpl.class);
            put(OLD_DIMMABLE_LIGHTBULB, HomekitLightbulbImpl.class);
            put(OLD_COLORFUL_LIGHTBULB, HomekitLightbulbImpl.class);
        }
    };

    /** mapping of legacy attributes to new attributes. **/
    private final static Map<HomekitCharacteristicType, HomekitCharacteristicType> LEGACY_CHARACTERISTICS_MAPPING = new HashMap<HomekitCharacteristicType, HomekitCharacteristicType>() {
        {
            put(OLD_CURRENT_HEATING_COOLING_STATE, CURRENT_HEATING_COOLING_STATE);
            put(OLD_TARGET_HEATING_COOLING_MODE, TARGET_HEATING_COOLING_STATE);
            put(OLD_TARGET_TEMPERATURE, TARGET_TEMPERATURE);
            put(OLD_BATTERY_LOW_STATUS, BATTERY_LOW_STATUS);
            put(VERY_OLD_TARGET_HEATING_COOLING_MODE, CURRENT_HEATING_COOLING_STATE);
        }
    };

    /** list of optional implicit optional characteristics. mainly used for legacy accessory type */
    private final static Map<HomekitAccessoryType, HomekitCharacteristicType[]> IMPLICIT_OPTIONAL_CHARACTERISTICS = new HashMap<HomekitAccessoryType, HomekitCharacteristicType[]>() {
        {
            put(OLD_DIMMABLE_LIGHTBULB, new HomekitCharacteristicType[] { BRIGHTNESS });
            put(OLD_COLORFUL_LIGHTBULB, new HomekitCharacteristicType[] { HUE, SATURATION, BRIGHTNESS });

        }
    };

    /**
     * creates HomeKit accessory for a openhab item.
     * 
     * @param taggedItem openhab item tagged as HomeKit item
     * @param metadataRegistry openhab metadata registry required to get item meta information
     * @param updater OH HomeKit update class that ensure the status sync between OH item and corresponding HomeKit
     *            characteristic.
     * @param settings OH settings
     * @return HomeKit accessory
     * @throws HomekitException exception in case HomeKit accessory could not be created, e.g. due missing mandatory
     *             characteristic
     */
    @SuppressWarnings("null")
    public static HomekitAccessory create(HomekitTaggedItem taggedItem, MetadataRegistry metadataRegistry,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws HomekitException {
        final HomekitAccessoryType accessoryType = taggedItem.getAccessoryType();
        logger.trace("Constructing {} of accessoryType {}", taggedItem.getName(), accessoryType);
        final List<HomekitTaggedItem> requiredCharacteristics = getMandatoryCharacteristics(taggedItem,
                metadataRegistry);
        final HomekitCharacteristicType[] mandatoryCharacteristics = MANDATORY_CHARACTERISTICS.get(accessoryType);
        if ((mandatoryCharacteristics != null) && (requiredCharacteristics.size() < mandatoryCharacteristics.length)) {
            logger.warn("Accessory of type {} must have following characteristics {}. Found only {}", accessoryType,
                    mandatoryCharacteristics, requiredCharacteristics);
            throw new HomekitException("Missing mandatory characteristics");
        }
        AbstractHomekitAccessoryImpl accessoryImpl;

        try {
            @Nullable
            final Class<? extends AbstractHomekitAccessoryImpl> accessoryImplClass = SERVICE_IMPL_MAP
                    .get(accessoryType);
            if (accessoryImplClass != null) {
                accessoryImpl = accessoryImplClass
                        .getConstructor(HomekitTaggedItem.class, List.class, HomekitAccessoryUpdater.class,
                                HomekitSettings.class)
                        .newInstance(taggedItem, requiredCharacteristics, updater, settings);
                addOptionalCharacteristics(accessoryImpl, metadataRegistry);
                return accessoryImpl;
            } else {
                logger.warn("Unsupported HomeKit type: {}", accessoryType);
                throw new HomekitException("Unsupported HomeKit type: " + accessoryType);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException
                | InvocationTargetException e) {
            logger.warn("Cannot instantiate accessory implementation for accessory {}", accessoryType, e);
            throw new HomekitException("Cannot instantiate accessory implementation for accessory " + accessoryType);
        }
    }

    /**
     * return HomeKit accessory types for a OH item based on meta data
     * 
     * @param item OH item
     * @param metadataRegistry meta data registry
     * @return list of HomeKit accessory types and characteristics.
     */
    public static List<Entry<HomekitAccessoryType, HomekitCharacteristicType>> getAccessoryTypes(Item item,
            MetadataRegistry metadataRegistry) {
        final List<Entry<HomekitAccessoryType, HomekitCharacteristicType>> accessories = new ArrayList<>();
        Metadata metadata = metadataRegistry.get(new MetadataKey(METADATA_KEY, item.getUID()));
        boolean legacyMode = metadata == null;
        String[] tags = !legacyMode ? metadata.getValue().split(",") : item.getTags().toArray(new String[0]); // fallback

        logger.trace("item {} meta data {}  tags {} ", item.getName(), metadata, tags);
        for (String tag : tags) {
            final String[] meta = tag.split("\\.");
            Optional<HomekitAccessoryType> accessoryType = HomekitAccessoryType.valueOfTag(meta[0].trim());
            if (accessoryType.isPresent()) { // it accessory, check for characteristic
                HomekitAccessoryType type = accessoryType.get();
                if ((legacyMode) && (type.equals(LIGHTBULB))) { // support old smart logic to convert Lighting to
                                                                // DimmableLighting or ColorfulLighting depending on
                                                                // item type
                    if (item instanceof ColorItem) {
                        type = OLD_COLORFUL_LIGHTBULB;
                    } else if (item instanceof DimmerItem) {
                        type = OLD_DIMMABLE_LIGHTBULB;
                    }
                }
                if (meta.length > 1) {
                    // it has characteristic as well
                    accessories.add(new SimpleEntry<>(type,
                            HomekitCharacteristicType.valueOfTag(meta[1].trim()).orElse(EMPTY)));
                } else {// it has no characteristic
                    accessories.add(new SimpleEntry<>(type, EMPTY));
                }
            } else { // it is no accessory, so, maybe it is a characteristic
                HomekitCharacteristicType.valueOfTag(meta[0].trim())
                        .ifPresent(c -> accessories.add(new SimpleEntry<>(DUMMY, c)));
            }
        }
        return accessories;
    }

    public static @Nullable Map<String, Object> getItemConfiguration(Item item, MetadataRegistry metadataRegistry) {
        Metadata metadata = metadataRegistry.get(new MetadataKey(METADATA_KEY, item.getUID()));
        return metadata != null ? metadata.getConfiguration() : null;
    }

    /**
     * return list of HomeKit relevant groups linked to an accessory
     * 
     * @param item OH item
     * @param itemRegistry item registry
     * @param metadataRegistry metadata registry
     * @return list of relevant group items
     */
    public static List<GroupItem> getAccessoryGroups(Item item, ItemRegistry itemRegistry,
            MetadataRegistry metadataRegistry) {
        return item.getGroupNames().stream().flatMap(name -> {
            Item groupItem = itemRegistry.get(name);
            if (groupItem instanceof GroupItem) {
                return Stream.of((GroupItem) groupItem);
            } else {
                return Stream.empty();
            }
        }).filter(groupItem -> !getAccessoryTypes(groupItem, metadataRegistry).isEmpty()).collect(Collectors.toList());
    }

    /**
     * collect all mandatory characteristics for a given tagged item, e.g. collect all mandatory HomeKit items from a
     * GroupItem
     * 
     * @param taggedItem HomeKit tagged item
     * @param metadataRegistry meta data registry
     * @return list of mandatory
     */
    private static List<HomekitTaggedItem> getMandatoryCharacteristics(HomekitTaggedItem taggedItem,
            MetadataRegistry metadataRegistry) {
        logger.trace("get mandatory characteristics for item {}: isGroup? {}, isMember? {}", taggedItem.getName(),
                taggedItem.isGroup(), taggedItem.isMemberOfAccessoryGroup());
        List<HomekitTaggedItem> collectedCharacteristics = new ArrayList<>();
        if (taggedItem.isGroup()) {
            for (Item item : ((GroupItem) taggedItem.getItem()).getAllMembers()) {
                addMandatoryCharacteristics(taggedItem, collectedCharacteristics, item, metadataRegistry);
            }
        } else {
            addMandatoryCharacteristics(taggedItem, collectedCharacteristics, taggedItem.getItem(), metadataRegistry);
        }
        return collectedCharacteristics;
    }

    /**
     * add mandatory HomeKit items for a given main item to a list of characteristics
     * 
     * @param mainItem main item
     * @param characteristics list of characteristics
     * @param item current item
     * @param metadataRegistry meta date registry
     */
    @SuppressWarnings("null")
    private static void addMandatoryCharacteristics(HomekitTaggedItem mainItem, List<HomekitTaggedItem> characteristics,
            Item item, MetadataRegistry metadataRegistry) {
        HomekitCharacteristicType[] mandatoryCharacteristics = MANDATORY_CHARACTERISTICS
                .get(mainItem.getAccessoryType());
        for (Entry<HomekitAccessoryType, HomekitCharacteristicType> accessory : getAccessoryTypes(item,
                metadataRegistry)) {
            if (isRootAccessory(accessory) && (mandatoryCharacteristics != null)) {
                Arrays.stream(mandatoryCharacteristics)
                        .forEach(c -> characteristics.add(new HomekitTaggedItem(item, accessory.getKey(), c,
                                mainItem.isGroup() ? (GroupItem) mainItem.getItem() : null,
                                HomekitAccessoryFactory.getItemConfiguration(item, metadataRegistry))));
            } else {
                if (isMandatoryCharacteristic(mainItem.getAccessoryType(), legacyCheck(accessory.getValue())))
                    characteristics
                            .add(new HomekitTaggedItem(item, accessory.getKey(), legacyCheck(accessory.getValue()),
                                    mainItem.isGroup() ? (GroupItem) mainItem.getItem() : null,
                                    HomekitAccessoryFactory.getItemConfiguration(item, metadataRegistry)));
            }
        }
    }

    /**
     * add optional characteristic for given accessory.
     * 
     * @param accessory accessory
     * @param metadataRegistry metadata registry
     */
    private static void addOptionalCharacteristics(AbstractHomekitAccessoryImpl accessory,
            MetadataRegistry metadataRegistry) {
        Map<HomekitCharacteristicType, GenericItem> characteristics = getOptionalCharacteristics(
                accessory.getRootAccessory(), metadataRegistry);
        Service service = accessory.getPrimaryService();

        characteristics.forEach((type, item) -> {
            try {
                logger.trace("adding optional characteristic: {} for item {}", type, item.getName());

                final Characteristic characteristic = HomekitCharacteristicFactory.createCharacteristic(type, item,
                        accessory.getUpdater());
                // find the corresponding add method at service and call it.
                service.getClass().getMethod("addOptionalCharacteristic", characteristic.getClass()).invoke(service,
                        characteristic);

                accessory.addCharacteristic(new HomekitTaggedItem(item, accessory.getRootAccessory().getAccessoryType(),
                        type, accessory.getRootAccessory().getRootDeviceGroupItem(),
                        getItemConfiguration(item, metadataRegistry)));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | HomekitException e) {
                logger.warn("Not supported optional HomeKit characteristic. Service type {}, characteristic type {}",
                        service.getType(), type, e);
            }
        });
    }

    /**
     * collect optional HomeKit characteristics for a OH item.
     * 
     * @param taggedItem main OH item
     * @param metadataRegistry OH metadata registry
     * @return a map with characteristics and corresponding OH items
     */
    @SuppressWarnings("null")
    private static Map<HomekitCharacteristicType, GenericItem> getOptionalCharacteristics(HomekitTaggedItem taggedItem,
            MetadataRegistry metadataRegistry) {
        logger.trace("get optional characteristics for item {}: isGroup? {}, isMember? {}", taggedItem.getName(),
                taggedItem.isGroup(), taggedItem.isMemberOfAccessoryGroup());
        Map<HomekitCharacteristicType, GenericItem> characteristicItems = new HashMap<>();
        if (taggedItem.isGroup()) {
            GroupItem groupItem = (GroupItem) taggedItem.getItem();
            groupItem.getMembers().forEach(item -> getAccessoryTypes(item, metadataRegistry).stream()
                    .filter(c -> !isRootAccessory(c))
                    .filter(c -> !isMandatoryCharacteristic(taggedItem.getAccessoryType(), legacyCheck(c.getValue())))
                    .forEach(characteristic -> characteristicItems.put(legacyCheck(characteristic.getValue()),
                            (GenericItem) item)));
        } else {
            getAccessoryTypes(taggedItem.getItem(), metadataRegistry).stream().filter(c -> !isRootAccessory(c))
                    .filter(c -> !isMandatoryCharacteristic(taggedItem.getAccessoryType(), legacyCheck(c.getValue())))
                    .forEach(characteristic -> characteristicItems.put(legacyCheck(characteristic.getValue()),
                            (GenericItem) taggedItem.getItem()));
            final HomekitCharacteristicType[] implicitOptionalCharacteristics = IMPLICIT_OPTIONAL_CHARACTERISTICS
                    .get(taggedItem.getAccessoryType());
            if (implicitOptionalCharacteristics != null) {
                Arrays.stream(implicitOptionalCharacteristics)
                        .filter(c -> !isMandatoryCharacteristic(taggedItem.getAccessoryType(), c))
                        .forEach(characteristic -> characteristicItems.put(legacyCheck(characteristic),
                                (GenericItem) taggedItem.getItem()));
            }
        }
        logger.trace("characteristics for {} = {}", taggedItem.getName(), characteristicItems);
        return Collections.unmodifiableMap(characteristicItems);
    }

    /**
     * return true is characteristic is a mandatory characteristic for the accessory.
     * 
     * @param accessory accessory
     * @param characteristic characteristic
     * @return true if characteristic is mandatory, false if not mandatory
     */
    @SuppressWarnings("null")
    private static boolean isMandatoryCharacteristic(HomekitAccessoryType accessory,
            HomekitCharacteristicType characteristic) {
        return MANDATORY_CHARACTERISTICS.get(accessory) != null
                && Arrays.asList(MANDATORY_CHARACTERISTICS.get(accessory)).contains(characteristic);
    }

    /**
     * check whether accessory is root accessory, i.e. without characteristic tag.
     * 
     * @param accessory accessory
     * @return true if accessory has not characteristic.
     */
    @SuppressWarnings("null")
    private static boolean isRootAccessory(Entry<HomekitAccessoryType, HomekitCharacteristicType> accessory) {
        return ((accessory.getValue() == null) || (accessory.getValue() == EMPTY));
    }

    /**
     * check whether it is legacy characteristic and return new name in such case. otherwise return the input parameter
     * unchangec.
     * 
     * @param characteristicType characteristic to check
     * @return new characteristic type
     */
    @SuppressWarnings("null")
    private static HomekitCharacteristicType legacyCheck(final HomekitCharacteristicType characteristicType) {
        if (LEGACY_CHARACTERISTICS_MAPPING.get(characteristicType) != null)
            return LEGACY_CHARACTERISTICS_MAPPING.get(characteristicType);
        return characteristicType;
    }
}
