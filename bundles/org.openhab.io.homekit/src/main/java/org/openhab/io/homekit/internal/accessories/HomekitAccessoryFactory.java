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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.items.MetadataKey;
import org.eclipse.smarthome.core.items.MetadataRegistry;
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
public class HomekitAccessoryFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomekitAccessoryFactory.class);
    public final static String METADATA_KEY = "homekit"; // prefix for HomeKit meta information in items.xml

    /** List of mandatory attributes for each accessory type. **/
    private final static Map<HomekitAccessoryType, HomekitCharacteristicType[]> mandatoryCharacteristics =
        new HashMap<HomekitAccessoryType, HomekitCharacteristicType[]>() {{
        put(LEAK_SENSOR,new HomekitCharacteristicType[]{LEAD_DETECTED_STATE});
        put(MOTION_SENSOR,new HomekitCharacteristicType[]{MOTION_DETECTED_STATE});
        put(OCCUPANCY_SENSOR,new HomekitCharacteristicType[]{OCCUPANCY_DETECTED_STATE});
        put(CONTACT_SENSOR,new HomekitCharacteristicType[]{CONTACT_SENSOR_STATE});
        put(SMOKE_SENSOR,new HomekitCharacteristicType[]{SMOKE_DETECTED_STATE});
        put(HUMIDITY_SENSOR,new HomekitCharacteristicType[]{RELATIVE_HUMIDITY});
        put(SWITCH,new HomekitCharacteristicType[]{ON_STATE});
        put(CARBON_DIOXIDE_SENSOR,new HomekitCharacteristicType[]{CARBON_DIOXIDE_DETECTED_STATE});
        put(CARBON_MONOXIDE_SENSOR,new HomekitCharacteristicType[]{CARBON_MONOXIDE_DETECTED_STATE});
        put(WINDOW_COVERING,new HomekitCharacteristicType[]{TARGET_POSITION,CURRENT_POSITION, POSITION_STATE});
        put(LIGHTBULB,new HomekitCharacteristicType[]{ON_STATE});
        put(FAN,new HomekitCharacteristicType[]{ACTIVE_STATUS});
        put(TEMPERATURE_SENSOR,new HomekitCharacteristicType[]{CURRENT_TEMPERATURE});
        put(THERMOSTAT,new HomekitCharacteristicType[]{CURRENT_HEATING_COOLING_STATE,TARGET_HEATING_COOLING_STATE, CURRENT_TEMPERATURE, TARGET_TEMPERATURE});
        put(LOCK,new HomekitCharacteristicType[]{LOCK_CURRENT_STATE,LOCK_TARGET_STATE});
    }};

    /** List of service implementation for each accessory type. **/
    private final static Map<HomekitAccessoryType, Class<? extends AbstractHomekitAccessoryImpl>> serviceImplMap =
        new HashMap<HomekitAccessoryType, Class<? extends AbstractHomekitAccessoryImpl>> (){{
            put(LEAK_SENSOR, HomekitLeakSensorImpl.class);
            put(MOTION_SENSOR, HomekitMotionSensorImpl.class);
            put(OCCUPANCY_SENSOR, HomekitOccupancySensorImpl.class);
            put(CONTACT_SENSOR, HomekitContactSensorImpl.class);
            put(TEMPERATURE_SENSOR, HomekitTemperatureSensorImpl.class);
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

        }};

    /**
     * creates HomeKit accessory for a openhab item.
     * @param taggedItem openhab item tagged as HomeKit item
     * @param itemRegistry openhab item registry, required to find related items
     * @param metadataRegistry openhab metadata registry required to get item meta information
     * @param updater OH HomeKit update class that ensure the status sync between OH item and corresponding HomeKit characteristic.
     * @param settings OH settings
     * @return HomeKit accessory
     * @throws HomekitException exception in case HomeKit accessory could not be created, e.g. due missing mandatory characteristic
     */
    public static HomekitAccessory create(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry, MetadataRegistry metadataRegistry,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws HomekitException {

        LOGGER.debug("Constructing {} of accessoryType {}", taggedItem.getName(), taggedItem.getAccessoryType());
        final List<HomekitTaggedItem> requiredCharacteristics = getMandatoryCharacteristics(taggedItem, metadataRegistry);

        if (requiredCharacteristics.size() != mandatoryCharacteristics.get(taggedItem.getAccessoryType()).length) {
                LOGGER.error("Accessory of type {} must have following characteristics {}. Found only {}", taggedItem.getAccessoryType(), mandatoryCharacteristics.get(taggedItem.getAccessoryType()),requiredCharacteristics);
                throw new HomekitException("Missing mandatory characteristics");
            }
        AbstractHomekitAccessoryImpl<?> accessoryImpl=null;

        try {
            final Class<? extends AbstractHomekitAccessoryImpl> accessoryImplClass = serviceImplMap.get(taggedItem.getAccessoryType());
            if (accessoryImplClass != null) {
                accessoryImpl = accessoryImplClass.getConstructor(HomekitTaggedItem.class, List.class, ItemRegistry.class, HomekitAccessoryUpdater.class, HomekitSettings.class)
                    .newInstance(taggedItem, requiredCharacteristics, itemRegistry, updater, settings);

                addOptionalCharacteristics(getOptionalCharacteristics(taggedItem, metadataRegistry),
                                           accessoryImpl.getPrimaryService(),
                                           updater);
                return accessoryImpl;
            } else {
                LOGGER.error("Unsupported HomeKit type: {}",taggedItem.getAccessoryType());
                throw new HomekitException("Unsupported HomeKit type: " + taggedItem.getAccessoryType());
            }
        }
        catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            LOGGER.error("Cannot instantiate accessory implementation for accessory {}", taggedItem.getAccessoryType(), e);
            throw new HomekitException("Cannot instantiate accessory implementation for accessory " + taggedItem.getAccessoryType());
        }
    }
    /**
     * return HomeKit accessory types for a OH item based on meta data
     * @param item OH item
     * @param metadataRegistry meta data registry
     * @return list of HomeKit accessory types and characteristics.
     */
    public static List<Entry<HomekitAccessoryType, HomekitCharacteristicType>> getAccessoryTypes(Item item, MetadataRegistry metadataRegistry) {
        final List<Entry<HomekitAccessoryType, HomekitCharacteristicType>> accessories = new ArrayList<>();
        Metadata metadata = metadataRegistry.get(new MetadataKey(METADATA_KEY, item.getUID()));
        String[] tags = (metadata != null)?
                        metadata.getValue().split(","):
                        item.getTags().toArray(new String[0]); // fallback to tags

        LOGGER.debug("item {} meta data {}  tags {} ", item.getName(),metadata, tags);

        for (String tag:tags){
            final String[] meta = tag.split("\\.");
            if (HomekitAccessoryType.valueOfTag(meta[0].trim())!=null) {
                final HomekitAccessoryType accessoryType = HomekitAccessoryType.valueOfTag(meta[0].trim());
                if (meta.length>1) {
                    if (HomekitCharacteristicType.valueOfTag(meta[1].trim())!=null) {
                        accessories.add(new SimpleEntry<>
                                            (accessoryType,
                                             HomekitCharacteristicType.valueOfTag(meta[1].trim())));
                    }
                    else
                        LOGGER.error("Unsupported characteristic {}", meta[1]);
                } else
                {
                    accessories.add(new SimpleEntry<>(accessoryType, EMPTY));
                }
            }
        }
        return accessories;
    }

    /**
     * return list of HomeKit relevant groups linked to an accessory
     * @param item OH item
     * @param itemRegistry item registry
     * @param metadataRegistry metadata registry
     * @return list of relevant group items
     */
    public static List<GroupItem> getAccessoryGroups(Item item, ItemRegistry itemRegistry, MetadataRegistry metadataRegistry) {
        return item.getGroupNames().stream().flatMap(name -> {
            Item groupItem = itemRegistry.get(name);
            if (groupItem instanceof GroupItem) {
                return Stream.of((GroupItem) groupItem);
            } else {
                return Stream.empty();
            }
        }).filter(groupItem -> !getAccessoryTypes(groupItem,metadataRegistry).isEmpty()
        ).collect(Collectors.toList());
    }

    /**
     * collect all mandatory characteristics for a given tagged item, e.g. collect all mandatory HomeKit items from a GroupItem
     * @param taggedItem HomeKit tagged item
     * @param metadataRegistry meta data registry
     * @return list of mandatory
     */
    private static List<HomekitTaggedItem> getMandatoryCharacteristics(HomekitTaggedItem taggedItem, MetadataRegistry metadataRegistry) {
        LOGGER.debug("get mandatory characteristics for item {}: isGroup? {}, isMember? {}", taggedItem.getName(), taggedItem.isGroup(), taggedItem.isMemberOfAccessoryGroup());
        List<HomekitTaggedItem> collectedCharacteristics = new ArrayList<>();
        if (taggedItem.isGroup()) {
            for (Item item:((GroupItem) taggedItem.getItem()).getAllMembers()) {
                addMandatoryCharacteristics(taggedItem, collectedCharacteristics, item, metadataRegistry);
            }
        } else  {
            addMandatoryCharacteristics(taggedItem, collectedCharacteristics, taggedItem.getItem(), metadataRegistry);
        }
        return collectedCharacteristics;
    }

    /**
     * add mandatory HomeKit items for a given main item to a list of characteristics
     * @param mainItem main item
     * @param characteristics list of characteristics
     * @param item current item
     * @param metadataRegistry meta date registry
     */
    private static void addMandatoryCharacteristics(HomekitTaggedItem mainItem, List<HomekitTaggedItem> characteristics, Item item, MetadataRegistry metadataRegistry) {
        HomekitCharacteristicType[] requiredChar = mandatoryCharacteristics.get(mainItem.getAccessoryType());
        for (Entry<HomekitAccessoryType, HomekitCharacteristicType> accessory : getAccessoryTypes(item, metadataRegistry)) {
            if (isRootAccessory(accessory))  {
                Arrays.stream(requiredChar).forEach(c ->
                                                         characteristics.add(new HomekitTaggedItem(item, accessory.getKey(), c,
                                                                                                   mainItem.isGroup()?(GroupItem) mainItem.getItem():null)));
            } else {
                if (isMandatoryCharacteristic(mainItem.getAccessoryType(), accessory.getValue()))
                    characteristics.add(
                        new HomekitTaggedItem(item, accessory.getKey(), accessory.getValue(), mainItem.isGroup()?(GroupItem) mainItem.getItem():null));
            }
        }
    }

    /**
     * create optional HomeKit characteristics and add the given HomeKit service.
     * @param characteristics OH items tagged as HomeKit characteristic
     * @param service service the characteristics should be added to
     * @param updater OH HomeKit updater that keeps OH items and HomeKit characteristics in sync
     */
    private static void addOptionalCharacteristics(Map<HomekitCharacteristicType, GenericItem> characteristics, Service service, HomekitAccessoryUpdater updater) {
        characteristics.forEach((type,item) -> {
            try {
                LOGGER.debug("adding optional charateristic: {} for item {}", type, item.getName());
                final Characteristic characteristic = HomekitCharacteristicFactory.createCharacteristic(type, item, updater);
                service.getClass().
                    getMethod("addOptionalCharacteristic", characteristic.getClass()). // find the corresponding add method at service and call it.
                    invoke(service,characteristic);
            }
            catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |HomekitException e) {
                LOGGER.error("Not supported optional HomeKit characteristic. Service type {}, characteristic type {}", service.getType(), type, e);
            }
        });
    }

    /**
     * collect optional HomeKit characteristics for a OH item.
     * @param taggedItem main OH item
     * @param metadataRegistry OH metadata registry
     * @return a map with characteristics and corresponding OH items
     */
    private static Map<HomekitCharacteristicType, GenericItem> getOptionalCharacteristics(HomekitTaggedItem taggedItem, MetadataRegistry metadataRegistry) {
        LOGGER.debug("get optional characteristics for item {}: isGroup? {}, isMember? {}", taggedItem.getName(), taggedItem.isGroup(), taggedItem.isMemberOfAccessoryGroup());
        Map<HomekitCharacteristicType, GenericItem> characteristicItems = new HashMap<>();
        if (taggedItem.isGroup()) {
            GroupItem groupItem = (GroupItem) taggedItem.getItem();
            groupItem.getMembers().forEach(item -> {
                getAccessoryTypes(item, metadataRegistry).stream().
                    filter(c ->  !isRootAccessory(c)
                    ).
                    filter(c -> !isMandatoryCharacteristic(taggedItem.getAccessoryType(), c.getValue())).
                    forEach(
                        characteristic  ->
                            characteristicItems.put(characteristic.getValue(),(GenericItem) item)
                           );
            });
        } else {
            getAccessoryTypes(taggedItem.getItem(),metadataRegistry).stream().
                filter(c -> !isRootAccessory(c)).
                filter(c -> !isMandatoryCharacteristic(taggedItem.getAccessoryType(), c.getValue())).
                forEach(
                    characteristic  ->
                        characteristicItems.put(characteristic.getValue(),(GenericItem) taggedItem.getItem())
                       );
        }
        LOGGER.debug("characteristics for {} = {}",taggedItem.getName(),characteristicItems );
        return Collections.unmodifiableMap(characteristicItems);
    }

    /**
     * return true is characteristic is a mandatory characteristic for the accessory.
     * @param accessory accessory
     * @param characteristic characteristic
     * @return true if characteristic is mandatory, false if not mandatory
     */
    private static boolean isMandatoryCharacteristic(HomekitAccessoryType accessory, HomekitCharacteristicType characteristic) {
        return Arrays.stream(mandatoryCharacteristics.get(accessory)).anyMatch(c -> c.equals(characteristic));
    }

    /**
     * check whether accessory is root accessory, i.e. without characteristic tag.
     * @param accessory accessory
     * @return true if accessory has not characteristic.
     */
    private static boolean isRootAccessory(Entry<HomekitAccessoryType, HomekitCharacteristicType> accessory) {
        return ((accessory.getValue()==null)  || (accessory.getValue()==EMPTY));
    }

}