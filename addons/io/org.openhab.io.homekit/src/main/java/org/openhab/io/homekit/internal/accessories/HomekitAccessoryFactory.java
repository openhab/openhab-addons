/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitAccessory;

/**
 * Creates a HomekitAccessory for a given HomekitTaggedItem.
 *
 * @author Andy Lintner
 */
public class HomekitAccessoryFactory {

    public static HomekitAccessory create(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws Exception {
        switch (taggedItem.getDeviceType()) {
            case LIGHTBULB:
                return new HomekitLightbulbImpl(taggedItem, itemRegistry, updater);

            case DIMMABLE_LIGHTBULB:
                return new HomekitDimmableLightbulbImpl(taggedItem, itemRegistry, updater);

            case COLORFUL_LIGHTBULB:
                return new HomekitColorfulLightbulbImpl(taggedItem, itemRegistry, updater);

            case THERMOSTAT:
                return new HomekitThermostatImpl(taggedItem, itemRegistry, updater, settings);

            case SWITCH:
                return new HomekitSwitchImpl(taggedItem, itemRegistry, updater);

            case TEMPERATURE_SENSOR:
                return new HomekitTemperatureSensorImpl(taggedItem, itemRegistry, updater, settings);

            case HUMIDITY_SENSOR:
                return new HomekitHumiditySensorImpl(taggedItem, itemRegistry, updater);
        }

        throw new Exception("Unknown homekit type: " + taggedItem.getDeviceType());
    }
}
