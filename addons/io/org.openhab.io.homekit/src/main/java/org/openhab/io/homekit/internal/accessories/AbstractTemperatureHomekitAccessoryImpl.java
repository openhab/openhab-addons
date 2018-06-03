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
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.accessories.TemperatureSensor;
import com.beowulfe.hap.accessories.properties.TemperatureUnit;

/**
 *
 * @author Andy Lintner - Initial contribution
 */
abstract class AbstractTemperatureHomekitAccessoryImpl<T extends GenericItem> extends AbstractHomekitAccessoryImpl<T>
        implements TemperatureSensor {

    private final HomekitSettings settings;

    public AbstractTemperatureHomekitAccessoryImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, HomekitSettings settings, Class<T> expectedItemClass) {
        super(taggedItem, itemRegistry, updater, expectedItemClass);
        this.settings = settings;
    }

    @Override
    public TemperatureUnit getTemperatureUnit() {
        return settings.useFahrenheitTemperature() ? TemperatureUnit.FAHRENHEIT : TemperatureUnit.CELSIUS;
    }

    @Override
    public double getMaximumTemperature() {
        return settings.getMaximumTemperature();
    }

    @Override
    public double getMinimumTemperature() {
        return settings.getMinimumTemperature();
    }

    protected double convertToCelsius(double degrees) {
        if (settings.useFahrenheitTemperature()) {
            return Math.round((5d / 9d) * (degrees - 32d) * 1000d) / 1000d;
        } else {
            return degrees;
        }
    }

    protected double convertFromCelsius(double degrees) {
        if (settings.useFahrenheitTemperature()) {
            return Math.round((((9d / 5d) * degrees) + 32d) * 10d) / 10d;
        } else {
            return degrees;
        }
    }
}
