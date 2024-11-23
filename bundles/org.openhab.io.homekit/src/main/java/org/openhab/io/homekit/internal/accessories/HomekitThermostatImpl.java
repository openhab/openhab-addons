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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GenericItem;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitException;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.thermostat.CoolingThresholdTemperatureCharacteristic;
import io.github.hapjava.characteristics.impl.thermostat.CurrentHeatingCoolingStateCharacteristic;
import io.github.hapjava.characteristics.impl.thermostat.CurrentHeatingCoolingStateEnum;
import io.github.hapjava.characteristics.impl.thermostat.CurrentTemperatureCharacteristic;
import io.github.hapjava.characteristics.impl.thermostat.HeatingThresholdTemperatureCharacteristic;
import io.github.hapjava.characteristics.impl.thermostat.TargetHeatingCoolingStateCharacteristic;
import io.github.hapjava.characteristics.impl.thermostat.TargetHeatingCoolingStateEnum;
import io.github.hapjava.characteristics.impl.thermostat.TargetTemperatureCharacteristic;
import io.github.hapjava.characteristics.impl.thermostat.TemperatureDisplayUnitCharacteristic;
import io.github.hapjava.services.impl.ThermostatService;

/**
 * Implements Thermostat as a GroupedAccessory made up of multiple items:
 * <ul>
 * <li>Current Temperature: Number type</li>
 * <li>Target Temperature: Number type</li>
 * <li>Current Heating/Cooling Mode: String type (see HomekitSettings.thermostat*Mode)</li>
 * <li>Target Heating/Cooling Mode: String type (see HomekitSettings.thermostat*Mode)</li>
 * </ul>
 *
 * @author Andy Lintner - Initial contribution
 */
@NonNullByDefault
class HomekitThermostatImpl extends AbstractHomekitAccessoryImpl {
    private final Logger logger = LoggerFactory.getLogger(HomekitThermostatImpl.class);
    private @Nullable HomekitCharacteristicChangeCallback targetTemperatureCallback = null;

    public HomekitThermostatImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            List<Characteristic> mandatoryRawCharacteristics, HomekitAccessoryUpdater updater,
            HomekitSettings settings) {
        super(taggedItem, mandatoryCharacteristics, mandatoryRawCharacteristics, updater, settings);
    }

    @Override
    public void init() throws HomekitException {
        super.init();

        var coolingThresholdTemperatureCharacteristic = getCharacteristic(
                CoolingThresholdTemperatureCharacteristic.class);
        var heatingThresholdTemperatureCharacteristic = getCharacteristic(
                HeatingThresholdTemperatureCharacteristic.class);
        var targetTemperatureCharacteristic = getCharacteristic(TargetTemperatureCharacteristic.class);

        if (!coolingThresholdTemperatureCharacteristic.isPresent()
                && !heatingThresholdTemperatureCharacteristic.isPresent()
                && !targetTemperatureCharacteristic.isPresent()) {
            throw new HomekitException(
                    "Unable to create thermostat; at least one of TargetTemperature, CoolingThresholdTemperature, or HeatingThresholdTemperature is required.");
        }

        var targetHeatingCoolingStateCharacteristic = getCharacteristic(TargetHeatingCoolingStateCharacteristic.class)
                .get();

        // TargetTemperature not provided; simulate by forwarding to HeatingThresholdTemperature and
        // CoolingThresholdTemperature
        // as appropriate
        if (!targetTemperatureCharacteristic.isPresent()) {
            if (Arrays.stream(targetHeatingCoolingStateCharacteristic.getValidValues())
                    .anyMatch(v -> v.equals(TargetHeatingCoolingStateEnum.HEAT))
                    && !heatingThresholdTemperatureCharacteristic.isPresent()) {
                throw new HomekitException(
                        "HeatingThresholdTemperature must be provided if HEAT mode is allowed and TargetTemperature is not provided.");
            }
            if (Arrays.stream(targetHeatingCoolingStateCharacteristic.getValidValues())
                    .anyMatch(v -> v.equals(TargetHeatingCoolingStateEnum.COOL))
                    && !coolingThresholdTemperatureCharacteristic.isPresent()) {
                throw new HomekitException(
                        "CoolingThresholdTemperature must be provided if COOL mode is allowed and TargetTemperature is not provided.");
            }

            double minValue, maxValue, minStep;
            if (coolingThresholdTemperatureCharacteristic.isPresent()
                    && heatingThresholdTemperatureCharacteristic.isPresent()) {
                minValue = Math.min(coolingThresholdTemperatureCharacteristic.get().getMinValue(),
                        heatingThresholdTemperatureCharacteristic.get().getMinValue());
                maxValue = Math.max(coolingThresholdTemperatureCharacteristic.get().getMaxValue(),
                        heatingThresholdTemperatureCharacteristic.get().getMaxValue());
                minStep = Math.min(coolingThresholdTemperatureCharacteristic.get().getMinStep(),
                        heatingThresholdTemperatureCharacteristic.get().getMinStep());
            } else if (coolingThresholdTemperatureCharacteristic.isPresent()) {
                minValue = coolingThresholdTemperatureCharacteristic.get().getMinValue();
                maxValue = coolingThresholdTemperatureCharacteristic.get().getMaxValue();
                minStep = coolingThresholdTemperatureCharacteristic.get().getMinStep();
            } else {
                minValue = heatingThresholdTemperatureCharacteristic.get().getMinValue();
                maxValue = heatingThresholdTemperatureCharacteristic.get().getMaxValue();
                minStep = heatingThresholdTemperatureCharacteristic.get().getMinStep();
            }
            targetTemperatureCharacteristic = Optional
                    .of(new TargetTemperatureCharacteristic(minValue, maxValue, minStep, () -> {
                        // return the value from the characteristic corresponding to the current mode
                        try {
                            switch (targetHeatingCoolingStateCharacteristic.getEnumValue().get()) {
                                case HEAT:
                                    return heatingThresholdTemperatureCharacteristic.get().getValue();
                                case COOL:
                                    return coolingThresholdTemperatureCharacteristic.get().getValue();
                                default:
                                    return CompletableFuture.completedFuture(
                                            (heatingThresholdTemperatureCharacteristic.get().getValue().get()
                                                    + coolingThresholdTemperatureCharacteristic.get().getValue().get())
                                                    / 2);
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            return null;
                        }
                    }, value -> {
                        try {
                            // set the charactestic corresponding to the current mode
                            switch (targetHeatingCoolingStateCharacteristic.getEnumValue().get()) {
                                case HEAT:
                                    heatingThresholdTemperatureCharacteristic.get().setValue(value);
                                    break;
                                case COOL:
                                    coolingThresholdTemperatureCharacteristic.get().setValue(value);
                                    break;
                                default:
                                    // ignore
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            // can't happen, since the futures are synchronous
                        }
                    }, cb -> {
                        targetTemperatureCallback = cb;
                        if (heatingThresholdTemperatureCharacteristic.isPresent()) {
                            getUpdater().subscribe(
                                    (GenericItem) getCharacteristic(HEATING_THRESHOLD_TEMPERATURE).get().getItem(),
                                    TARGET_TEMPERATURE.getTag(), this::thresholdTemperatureChanged);
                        }
                        if (coolingThresholdTemperatureCharacteristic.isPresent()) {
                            getUpdater().subscribe(
                                    (GenericItem) getCharacteristic(COOLING_THRESHOLD_TEMPERATURE).get().getItem(),
                                    TARGET_TEMPERATURE.getTag(), this::thresholdTemperatureChanged);
                        }
                        getUpdater().subscribe(
                                (GenericItem) getCharacteristic(TARGET_HEATING_COOLING_STATE).get().getItem(),
                                TARGET_TEMPERATURE.getTag(), this::thresholdTemperatureChanged);
                    }, () -> {
                        if (heatingThresholdTemperatureCharacteristic.isPresent()) {
                            getUpdater().unsubscribe(
                                    (GenericItem) getCharacteristic(HEATING_THRESHOLD_TEMPERATURE).get().getItem(),
                                    TARGET_TEMPERATURE.getTag());
                        }
                        if (coolingThresholdTemperatureCharacteristic.isPresent()) {
                            getUpdater().unsubscribe(
                                    (GenericItem) getCharacteristic(COOLING_THRESHOLD_TEMPERATURE).get().getItem(),
                                    TARGET_TEMPERATURE.getTag());
                        }
                        getUpdater().unsubscribe(
                                (GenericItem) getCharacteristic(TARGET_HEATING_COOLING_STATE).get().getItem(),
                                TARGET_TEMPERATURE.getTag());
                        targetTemperatureCallback = null;
                    }));
        }

        // These characteristics are technically mandatory, but we provide defaults if they're not provided
        var currentHeatingCoolingStateCharacteristic = getCharacteristic(CurrentHeatingCoolingStateCharacteristic.class)
                .orElseGet(() -> new CurrentHeatingCoolingStateCharacteristic(
                        new CurrentHeatingCoolingStateEnum[] { CurrentHeatingCoolingStateEnum.OFF },
                        () -> CompletableFuture.completedFuture(CurrentHeatingCoolingStateEnum.OFF), (cb) -> {
                        }, () -> {
                        })

                );
        var displayUnitCharacteristic = getCharacteristic(TemperatureDisplayUnitCharacteristic.class)
                .orElseGet(() -> HomekitCharacteristicFactory.createSystemTemperatureDisplayUnitCharacteristic());

        addService(
                new ThermostatService(currentHeatingCoolingStateCharacteristic, targetHeatingCoolingStateCharacteristic,
                        getCharacteristic(CurrentTemperatureCharacteristic.class).get(),
                        targetTemperatureCharacteristic.get(), displayUnitCharacteristic));
    }

    private void thresholdTemperatureChanged() {
        targetTemperatureCallback.changed();
    }
}
