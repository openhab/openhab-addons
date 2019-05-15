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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.properties.ThermostatMode;
import com.beowulfe.hap.accessories.thermostat.BasicThermostat;

/**
 * Implements Thermostat as a GroupedAccessory made up of multiple items:
 * <ul>
 * <li>Cooling Threshold: Decimal type</li>
 * <li>Heating Threshold: Decimal type</li>
 * <li>Auto Threshold: Decimal type</li>
 * <li>Current Temperature: Decimal type</li>
 * <li>Heating/Cooling Mode: String type (see HomekitSettings.thermostat*Mode)</li>
 * </ul>
 *
 * @author Andy Lintner - Initial contribution
 */
class HomekitThermostatImpl extends AbstractTemperatureHomekitAccessoryImpl<GroupItem> implements BasicThermostat {

    private final HomekitSettings settings;
    @NonNull
    private NumberItem currentTemperatureItem;
    @NonNull
    private StringItem targetHeatingCoolingModeItem;

    @NonNull
    private Optional<StringItem> heatingCoolingCurrentModeItem;

    @NonNull
    private NumberItem targetTemperatureItem;

    private Logger logger = LoggerFactory.getLogger(HomekitThermostatImpl.class);

    @SuppressWarnings("deprecation")
    public HomekitThermostatImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, HomekitSettings settings, Item currentTemperatureItem,
            Map<HomekitCharacteristicType, Item> origCharacteristicItems) throws IncompleteAccessoryException {
        super(taggedItem, itemRegistry, updater, settings, GroupItem.class);

        HashMap<HomekitCharacteristicType, Item> characteristicItems = new HashMap<>(origCharacteristicItems);
        this.settings = settings;
        this.currentTemperatureItem = (NumberItem) currentTemperatureItem;

        this.targetHeatingCoolingModeItem = getItemWithDeprecation(characteristicItems,
                HomekitCharacteristicType.TARGET_HEATING_COOLING_MODE,
                HomekitCharacteristicType.OLD_TARGET_HEATING_COOLING_MODE).map(m -> (StringItem) m).orElseThrow(
                        () -> new IncompleteAccessoryException(HomekitCharacteristicType.TARGET_HEATING_COOLING_MODE));

        this.targetTemperatureItem = getItemWithDeprecation(characteristicItems,
                HomekitCharacteristicType.TARGET_TEMPERATURE, HomekitCharacteristicType.OLD_TARGET_TEMPERATURE)
                        .map(item -> (NumberItem) item).orElseThrow(
                                () -> new IncompleteAccessoryException(HomekitCharacteristicType.TARGET_TEMPERATURE));

        this.heatingCoolingCurrentModeItem = Optional
                .ofNullable(characteristicItems.remove(HomekitCharacteristicType.CURRENT_HEATING_COOLING_STATE))
                .map(m -> (StringItem) m);

        characteristicItems.entrySet().stream().forEach(entry -> {
            logger.warn("Item {} has unrecognized thermostat characteristic: {}", entry.getValue().getName(),
                    entry.getKey().getTag());
        });
    }

    private Optional<Item> getItemWithDeprecation(HashMap<HomekitCharacteristicType, Item> characteristicItems,
            HomekitCharacteristicType currentTag, HomekitCharacteristicType deprecatedTag) {
        Optional<Item> targetTempItem = Optional.ofNullable(characteristicItems.remove(currentTag));
        if (!targetTempItem.isPresent()) {
            targetTempItem = Optional.ofNullable(characteristicItems.remove(deprecatedTag));
            targetTempItem.ifPresent(item -> {
                logger.warn("The tag {} has been renamed to {}; please update your things, accordingly",
                        currentTag.getTag(), deprecatedTag.getTag());
            });
        }
        return targetTempItem;
    }

    @Override
    public CompletableFuture<ThermostatMode> getCurrentMode() {
        String stringValue = heatingCoolingCurrentModeItem.map(i -> i.getState().toString())
                .orElseGet(() -> settings.getCurrentModeOff());
        ThermostatMode mode;

        if (stringValue.equalsIgnoreCase(settings.getThermostatCurrentModeCooling())) {
            mode = ThermostatMode.COOL;
        } else if (stringValue.equalsIgnoreCase(settings.getThermostatCurrentModeHeating())) {
            mode = ThermostatMode.HEAT;
        } else if (stringValue.equalsIgnoreCase(settings.getCurrentModeOff())) {
            mode = ThermostatMode.OFF;
        } else if (stringValue.equals("UNDEF") || stringValue.equals("NULL")) {
            logger.debug("Heating cooling target mode not available. Relaying value of OFF to Homekit");
            mode = ThermostatMode.OFF;
        } else {
            logger.error("Unrecognized heatingCoolingCurrentMode: {}. Expected {}, {}, or {} strings in value.",
                    stringValue, settings.getThermostatCurrentModeCooling(), settings.getThermostatCurrentModeHeating(),
                    settings.getCurrentModeOff());
            mode = ThermostatMode.OFF;
        }
        return CompletableFuture.completedFuture(mode);
    }

    @Override
    public CompletableFuture<Double> getCurrentTemperature() {
        DecimalType state = currentTemperatureItem.getStateAs(DecimalType.class);
        if (state == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(convertToCelsius(state.doubleValue()));
    }

    @Override
    public CompletableFuture<ThermostatMode> getTargetMode() {
        State state = targetHeatingCoolingModeItem.getState();
        ThermostatMode mode;

        String stringValue = state.toString();
        if (stringValue.equalsIgnoreCase(settings.getThermostatTargetModeCool())) {
            mode = ThermostatMode.COOL;
        } else if (stringValue.equalsIgnoreCase(settings.getThermostatTargetModeHeat())) {
            mode = ThermostatMode.HEAT;
        } else if (stringValue.equalsIgnoreCase(settings.getThermostatTargetModeAuto())) {
            mode = ThermostatMode.AUTO;
        } else if (stringValue.equalsIgnoreCase(settings.getThermostatTargetModeOff())) {
            mode = ThermostatMode.OFF;
        } else if (stringValue.equals("UNDEF") || stringValue.equals("NULL")) {
            logger.debug("Heating cooling target mode not available. Relaying value of OFF to Homekit");
            mode = ThermostatMode.OFF;
        } else {
            logger.warn("Unrecognized heating cooling target mode: {}. Expected {}, {}, {}, or {} strings in value.",
                    stringValue, settings.getThermostatTargetModeCool(), settings.getThermostatTargetModeHeat(),
                    settings.getThermostatTargetModeAuto(), settings.getThermostatTargetModeOff());
            mode = ThermostatMode.OFF;
        }
        return CompletableFuture.completedFuture(mode);
    }

    @Override
    public CompletableFuture<Double> getTargetTemperature() {
        DecimalType state = targetTemperatureItem.getStateAs(DecimalType.class);
        if (state == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(convertToCelsius(state.doubleValue()));
    }

    @Override
    public void setTargetMode(ThermostatMode mode) throws Exception {
        String modeString = null;
        switch (mode) {
            case AUTO:
                modeString = settings.getThermostatTargetModeAuto();
                break;

            case COOL:
                modeString = settings.getThermostatTargetModeCool();
                break;

            case HEAT:
                modeString = settings.getThermostatTargetModeHeat();
                break;

            case OFF:
                modeString = settings.getThermostatTargetModeOff();
                break;
        }
        targetHeatingCoolingModeItem.send(new StringType(modeString));
    }

    @Override
    public void setTargetTemperature(Double value) throws Exception {
        targetTemperatureItem.send(new DecimalType(BigDecimal.valueOf(convertFromCelsius(value))));
    }

    @Override
    public void subscribeCurrentMode(HomekitCharacteristicChangeCallback callback) {
        heatingCoolingCurrentModeItem.ifPresent(item -> getUpdater().subscribe(item, callback));
    }

    @Override
    public void subscribeCurrentTemperature(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(currentTemperatureItem, callback);
    }

    @Override
    public void subscribeTargetMode(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(targetHeatingCoolingModeItem, callback);
    }

    @Override
    public void subscribeTargetTemperature(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(targetTemperatureItem, callback);
    }

    @Override
    public void unsubscribeCurrentMode() {
        getUpdater().unsubscribe(targetHeatingCoolingModeItem);
    }

    @Override
    public void unsubscribeCurrentTemperature() {
        getUpdater().unsubscribe(currentTemperatureItem);
    }

    @Override
    public void unsubscribeTargetMode() {
        getUpdater().unsubscribe(targetHeatingCoolingModeItem);
    }

    @Override
    public void unsubscribeTargetTemperature() {
        getUpdater().unsubscribe(targetTemperatureItem);
    }
}
