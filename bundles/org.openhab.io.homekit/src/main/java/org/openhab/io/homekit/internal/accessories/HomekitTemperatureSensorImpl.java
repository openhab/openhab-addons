/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.TemperatureSensorAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.thermostat.CurrentTemperatureCharacteristic;
import io.github.hapjava.characteristics.impl.thermostat.TargetTemperatureCharacteristic;
import io.github.hapjava.services.impl.TemperatureSensorService;

/**
 * Implements a HomeKit TemperatureSensor using a NumberItem
 *
 * @author Andy Lintner - Initial contribution
 */
class HomekitTemperatureSensorImpl extends AbstractHomekitAccessoryImpl implements TemperatureSensorAccessory {

    public HomekitTemperatureSensorImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        getServices().add(new TemperatureSensorService(this));
    }

    @Override
    public CompletableFuture<Double> getCurrentTemperature() {
        final @Nullable Double state = getStateAsTemperature(HomekitCharacteristicType.CURRENT_TEMPERATURE);
        return CompletableFuture.completedFuture(state != null ? state : getMinCurrentTemperature());
    }

    @Override
    public void subscribeCurrentTemperature(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.CURRENT_TEMPERATURE, callback);
    }

    @Override
    public double getMinCurrentTemperature() {
        // Apple defines default values in Celsius. We need to convert them to Fahrenheit if openHAB is using Fahrenheit
        // convertToCelsius and convertFromCelsius are only converting if useFahrenheit is set to true, so no additional
        // check here needed
        return HomekitCharacteristicFactory.convertToCelsius(
                getAccessoryConfiguration(HomekitCharacteristicType.CURRENT_TEMPERATURE, HomekitTaggedItem.MIN_VALUE,
                        BigDecimal.valueOf(HomekitCharacteristicFactory
                                .convertFromCelsius(CurrentTemperatureCharacteristic.DEFAULT_MIN_VALUE)))
                        .doubleValue());
    }

    @Override
    public double getMaxCurrentTemperature() {
        return HomekitCharacteristicFactory.convertToCelsius(
                getAccessoryConfiguration(HomekitCharacteristicType.CURRENT_TEMPERATURE, HomekitTaggedItem.MAX_VALUE,
                        BigDecimal.valueOf(HomekitCharacteristicFactory
                                .convertFromCelsius(CurrentTemperatureCharacteristic.DEFAULT_MAX_VALUE)))
                        .doubleValue());
    }

    @Override
    public double getMinStepCurrentTemperature() {
        return HomekitCharacteristicFactory.getTemperatureStep(
                getCharacteristic(HomekitCharacteristicType.CURRENT_TEMPERATURE).get(),
                TargetTemperatureCharacteristic.DEFAULT_STEP);
    }

    @Override
    public void unsubscribeCurrentTemperature() {
        unsubscribe(HomekitCharacteristicType.CURRENT_TEMPERATURE);
    }
}
