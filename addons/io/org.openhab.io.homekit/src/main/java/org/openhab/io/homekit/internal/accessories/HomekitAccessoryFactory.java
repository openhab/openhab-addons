/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.openhab.io.homekit.internal.HomekitAccessoryType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.openhab.io.homekit.internal.battery.BatteryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitAccessory;
import com.google.common.collect.ImmutableMap;

/**
 * Creates a HomekitAccessory for a given HomekitTaggedItem.
 *
 * @author Andy Lintner - Initial contribution
 */
public class HomekitAccessoryFactory {
    static Logger logger = LoggerFactory.getLogger(HomekitTaggedItem.class);

    public static HomekitAccessory create(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws Exception {
        logger.debug("Constructing {} of accessoryType {}", taggedItem.getName(), taggedItem.getAccessoryType());

        Map<HomekitCharacteristicType, Item> characteristicItems = getCharacteristicItems(taggedItem);

        switch (taggedItem.getAccessoryType()) {
            case LEAK_SENSOR:
                HomekitTaggedItem leakSensorAccessory = getPrimaryAccessory(taggedItem,
                        HomekitAccessoryType.LEAK_SENSOR, itemRegistry).orElseThrow(
                                () -> new Exception("Leak accessory group should have a leak sensor in it"));

                return new HomekitLeakSensorImpl(leakSensorAccessory, itemRegistry, updater,
                        BatteryStatus.getFromCharacteristics(characteristicItems));

            case VALVE:
                return new HomekitValveImpl(taggedItem, itemRegistry, updater);

            case MOTION_SENSOR:
                HomekitTaggedItem motionSensorAccessory = getPrimaryAccessory(taggedItem,
                        HomekitAccessoryType.MOTION_SENSOR, itemRegistry).orElseThrow(
                                () -> new Exception("Leak accessory group should have a leak sensor in it"));

                return new HomekitMotionSensorImpl(motionSensorAccessory, itemRegistry, updater,
                        BatteryStatus.getFromCharacteristics(characteristicItems));

            case LIGHTBULB:
                return new HomekitLightbulbImpl(taggedItem, itemRegistry, updater);

            case DIMMABLE_LIGHTBULB:
                return new HomekitDimmableLightbulbImpl(taggedItem, itemRegistry, updater);

            case COLORFUL_LIGHTBULB:
                return new HomekitColorfulLightbulbImpl(taggedItem, itemRegistry, updater);

            case THERMOSTAT:
                HomekitTaggedItem temperatureAccessory = getPrimaryAccessory(taggedItem,
                        HomekitAccessoryType.TEMPERATURE_SENSOR, itemRegistry)
                                .orElseThrow(() -> new Exception("Thermostats need a CurrentTemperature accessory"));

                return new HomekitThermostatImpl(taggedItem, itemRegistry, updater, settings,
                        temperatureAccessory.getItem(), getCharacteristicItems(taggedItem));

            case SWITCH:
                return new HomekitSwitchImpl(taggedItem, itemRegistry, updater);

            case TEMPERATURE_SENSOR:
                return new HomekitTemperatureSensorImpl(taggedItem, itemRegistry, updater, settings);

            case HUMIDITY_SENSOR:
                return new HomekitHumiditySensorImpl(taggedItem, itemRegistry, updater);

            case CONTACT_SENSOR:
                return new HomekitContactSensorImpl(taggedItem, itemRegistry, updater);

            case BLINDS:
            case WINDOW_COVERING:
                return new HomekitWindowCoveringImpl(taggedItem, itemRegistry, updater);
        }

        throw new Exception("Unknown homekit type: " + taggedItem.getAccessoryType());
    }

    /**
     * Given an accessory group, return the item in the group tagged as an accessory.
     *
     * @param taggedItem    The group item containing our item, or, the accessory item.
     * @param accessoryType The accessory type for which we're looking
     * @return
     */
    private static Optional<HomekitTaggedItem> getPrimaryAccessory(HomekitTaggedItem taggedItem,
            HomekitAccessoryType accessoryType, ItemRegistry itemRegistry) {
        logger.info("{} isGroup? {}", taggedItem.getName(), taggedItem.isGroup(),
                taggedItem.isMemberOfAccessoryGroup());
        if (taggedItem.isGroup()) {
            GroupItem groupItem = (GroupItem) taggedItem.getItem();
            return groupItem.getMembers().stream().filter(item -> item.hasTag(accessoryType.getTag())).findFirst()
                    .map(item -> new HomekitTaggedItem(item, itemRegistry));
        } else if (taggedItem.getAccessoryType() == accessoryType) {
            return Optional.of(taggedItem);
        } else {
            return Optional.empty();
        }
    }

    private static Map<HomekitCharacteristicType, Item> getCharacteristicItems(HomekitTaggedItem taggedItem) {
        if (taggedItem.isGroup()) {
            ImmutableMap.Builder<HomekitCharacteristicType, Item> builder = new ImmutableMap.Builder<>();
            GroupItem groupItem = (GroupItem) taggedItem.getItem();
            groupItem.getMembers().stream().forEach(item -> {
                HomekitCharacteristicType itemType = HomekitTaggedItem.findCharacteristicType(item);
                if (itemType != null) {
                    builder.put(itemType, item);
                }
            });
            return builder.build();
        } else {
            // do nothing; only accessory groups have characteristic items
            return Collections.emptyMap();
        }
    }
}
