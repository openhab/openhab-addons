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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.ACTIVE_STATUS;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.CURRENT_HEATER_COOLER_STATE;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.TARGET_HEATER_COOLER_STATE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GenericItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitException;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.accessories.HeaterCoolerAccessory;
import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.heatercooler.CurrentHeaterCoolerStateEnum;
import io.github.hapjava.characteristics.impl.heatercooler.TargetHeaterCoolerStateEnum;
import io.github.hapjava.characteristics.impl.thermostat.CurrentTemperatureCharacteristic;
import io.github.hapjava.characteristics.impl.thermostat.TemperatureDisplayUnitCharacteristic;
import io.github.hapjava.services.impl.HeaterCoolerService;

/**
 * Implements Heater Cooler
 *
 * @author Eugen Freiter - Initial contribution
 */

public class HomekitHeaterCoolerImpl extends AbstractHomekitAccessoryImpl implements HeaterCoolerAccessory {
    private final Logger logger = LoggerFactory.getLogger(HomekitHeaterCoolerImpl.class);
    private final BooleanItemReader activeReader;
    private final Map<CurrentHeaterCoolerStateEnum, Object> currentStateMapping;
    private final Map<TargetHeaterCoolerStateEnum, Object> targetStateMapping;

    private final List<CurrentHeaterCoolerStateEnum> customCurrentStateList = new ArrayList<>();
    private final List<TargetHeaterCoolerStateEnum> customTargetStateList = new ArrayList<>();

    public HomekitHeaterCoolerImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            List<Characteristic> mandatoryRawCharacteristics, HomekitAccessoryUpdater updater, HomekitSettings settings)
            throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, mandatoryRawCharacteristics, updater, settings);
        activeReader = new BooleanItemReader(getItem(ACTIVE_STATUS, GenericItem.class)
                .orElseThrow(() -> new IncompleteAccessoryException(ACTIVE_STATUS)), OnOffType.ON, OpenClosedType.OPEN);
        currentStateMapping = createMapping(CURRENT_HEATER_COOLER_STATE, CurrentHeaterCoolerStateEnum.class,
                customCurrentStateList);
        targetStateMapping = createMapping(TARGET_HEATER_COOLER_STATE, TargetHeaterCoolerStateEnum.class,
                customTargetStateList);
    }

    @Override
    public void init() throws HomekitException {
        super.init();
        final HeaterCoolerService service = new HeaterCoolerService(this);

        var temperatureDisplayUnit = getCharacteristic(TemperatureDisplayUnitCharacteristic.class);
        if (temperatureDisplayUnit.isEmpty()) {
            service.addOptionalCharacteristic(
                    HomekitCharacteristicFactory.createSystemTemperatureDisplayUnitCharacteristic());
        }

        addService(service);
    }

    @Override
    public CurrentHeaterCoolerStateEnum[] getCurrentHeaterCoolerStateValidValues() {
        return customCurrentStateList.isEmpty()
                ? currentStateMapping.keySet().toArray(new CurrentHeaterCoolerStateEnum[0])
                : customCurrentStateList.toArray(new CurrentHeaterCoolerStateEnum[0]);
    }

    @Override
    public TargetHeaterCoolerStateEnum[] getTargetHeaterCoolerStateValidValues() {
        return customTargetStateList.isEmpty() ? targetStateMapping.keySet().toArray(new TargetHeaterCoolerStateEnum[0])
                : customTargetStateList.toArray(new TargetHeaterCoolerStateEnum[0]);
    }

    @Override
    public CompletableFuture<Double> getCurrentTemperature() {
        final @Nullable Double state = getStateAsTemperature(HomekitCharacteristicType.CURRENT_TEMPERATURE);
        return CompletableFuture.completedFuture(state != null ? state
                : getAccessoryConfiguration(HomekitCharacteristicType.CURRENT_TEMPERATURE, HomekitTaggedItem.MIN_VALUE,
                        BigDecimal.valueOf(HomekitCharacteristicFactory
                                .convertFromCelsius(CurrentTemperatureCharacteristic.DEFAULT_MIN_VALUE)))
                        .doubleValue());
    }

    @Override
    public CompletableFuture<Boolean> isActive() {
        return CompletableFuture.completedFuture(activeReader.getValue());
    }

    @Override
    public CompletableFuture<Void> setActive(boolean state) {
        activeReader.setValue(state);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<CurrentHeaterCoolerStateEnum> getCurrentHeaterCoolerState() {
        return CompletableFuture.completedFuture(getKeyFromMapping(CURRENT_HEATER_COOLER_STATE, currentStateMapping,
                CurrentHeaterCoolerStateEnum.INACTIVE));
    }

    @Override
    public CompletableFuture<TargetHeaterCoolerStateEnum> getTargetHeaterCoolerState() {
        return CompletableFuture.completedFuture(
                getKeyFromMapping(TARGET_HEATER_COOLER_STATE, targetStateMapping, TargetHeaterCoolerStateEnum.AUTO));
    }

    @Override
    public CompletableFuture<Void> setTargetHeaterCoolerState(TargetHeaterCoolerStateEnum state) {
        HomekitCharacteristicFactory.setValueFromEnum(
                getCharacteristic(HomekitCharacteristicType.TARGET_HEATER_COOLER_STATE).get(), state,
                targetStateMapping);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeCurrentHeaterCoolerState(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.CURRENT_HEATER_COOLER_STATE, callback);
    }

    @Override
    public void unsubscribeCurrentHeaterCoolerState() {
        unsubscribe(HomekitCharacteristicType.CURRENT_HEATER_COOLER_STATE);
    }

    @Override
    public void subscribeTargetHeaterCoolerState(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.TARGET_HEATER_COOLER_STATE, callback);
    }

    @Override
    public void unsubscribeTargetHeaterCoolerState() {
        unsubscribe(HomekitCharacteristicType.TARGET_HEATER_COOLER_STATE);
    }

    @Override
    public void subscribeActive(HomekitCharacteristicChangeCallback callback) {
        subscribe(ACTIVE_STATUS, callback);
    }

    @Override
    public void unsubscribeActive() {
        unsubscribe(ACTIVE_STATUS);
    }

    @Override
    public void subscribeCurrentTemperature(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.CURRENT_TEMPERATURE, callback);
    }

    @Override
    public void unsubscribeCurrentTemperature() {
        unsubscribe(HomekitCharacteristicType.CURRENT_TEMPERATURE);
    }
}
