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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.TemperatureSensorAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.services.impl.TemperatureSensorService;

/**
 * Implements a HomeKit TemperatureSensor using a NumberItem
 *
 * @author Andy Lintner - Initial contribution
 */
class HomekitTemperatureSensorImpl extends AbstractHomekitAccessoryImpl<NumberItem>
    implements TemperatureSensorAccessory {

    public HomekitTemperatureSensorImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem,mandatoryCharacteristics,itemRegistry, updater, settings);
        getServices().add(new TemperatureSensorService(this));
    }

    @Override
    public CompletableFuture<Double> getCurrentTemperature() {
        DecimalType state = getItem().getStateAs(DecimalType.class);
        if (state == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(convertToCelsius(state.doubleValue()));
    }

    @Override
    public void subscribeCurrentTemperature(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeCurrentTemperature() {
        getUpdater().unsubscribe(getItem());
    }

    protected double convertToCelsius(double degrees) {
        if (getSettings().useFahrenheitTemperature) {
            return Math.round((5d / 9d) * (degrees - 32d) * 1000d) / 1000d;
        } else {
            return degrees;
        }
    }

    protected double convertFromCelsius(double degrees) {
        if (getSettings().useFahrenheitTemperature) {
            return Math.round((((9d / 5d) * degrees) + 32d) * 10d) / 10d;
        } else {
            return degrees;
        }
    }
}
