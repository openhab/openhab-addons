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
    private StringItem heatingCoolingModeItem;
    @NonNull
    private NumberItem targetTemperatureItem;

    private Logger logger = LoggerFactory.getLogger(HomekitThermostatImpl.class);

    public HomekitThermostatImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, HomekitSettings settings, Item currentTemperatureItem,
            Map<HomekitCharacteristicType, Item> characteristicItems) throws IncompleteAccessoryException {
        super(taggedItem, itemRegistry, updater, settings, GroupItem.class);
        this.settings = settings;
        this.currentTemperatureItem = (NumberItem) currentTemperatureItem;

        this.heatingCoolingModeItem = Optional
                .ofNullable(characteristicItems.get(HomekitCharacteristicType.HEATING_COOLING_MODE))
                .map(m -> (StringItem) m)
                .orElseThrow(() -> new IncompleteAccessoryException(HomekitCharacteristicType.HEATING_COOLING_MODE));

        Optional<Item> targetTempItem = Optional
                .ofNullable(characteristicItems.get(HomekitCharacteristicType.TARGET_TEMPERATURE));
        if (!targetTempItem.isPresent()) {
            targetTempItem = Optional
                    .ofNullable(characteristicItems.get(HomekitCharacteristicType.OLD_TARGET_TEMPERATURE));
        }

        this.targetTemperatureItem = targetTempItem.map(m -> (NumberItem) m)
                .orElseThrow(() -> new IncompleteAccessoryException(HomekitCharacteristicType.TARGET_TEMPERATURE));

        characteristicItems.entrySet().stream().forEach(entry -> {
            logger.error("Item {} has unrecognized thermostat characteristic: {}", entry.getValue().getName(),
                    entry.getKey());
        });
    }

    @Override
    public CompletableFuture<ThermostatMode> getCurrentMode() {
        State state = heatingCoolingModeItem.getState();
        ThermostatMode mode;

        String stringValue = state.toString();
        if (stringValue.equalsIgnoreCase(settings.getThermostatCoolMode())) {
            mode = ThermostatMode.COOL;
        } else if (stringValue.equalsIgnoreCase(settings.getThermostatHeatMode())) {
            mode = ThermostatMode.HEAT;
        } else if (stringValue.equalsIgnoreCase(settings.getThermostatAutoMode())) {
            mode = ThermostatMode.AUTO;
        } else if (stringValue.equalsIgnoreCase(settings.getThermostatOffMode())) {
            mode = ThermostatMode.OFF;
        } else if (stringValue.equals("UNDEF") || stringValue.equals("NULL")) {
            logger.debug("Heating cooling target mode not available. Relaying value of OFF to Homekit");
            mode = ThermostatMode.OFF;
        } else {
            logger.error("Unrecognized heating cooling target mode: {}. Expected {}, {}, {}, or {} strings in value.",
                    stringValue, settings.getThermostatCoolMode(), settings.getThermostatHeatMode(),
                    settings.getThermostatAutoMode(), settings.getThermostatOffMode());
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
        return getCurrentMode();
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
                modeString = settings.getThermostatAutoMode();
                break;

            case COOL:
                modeString = settings.getThermostatCoolMode();
                break;

            case HEAT:
                modeString = settings.getThermostatHeatMode();
                break;

            case OFF:
                modeString = settings.getThermostatOffMode();
                break;
        }
        heatingCoolingModeItem.send(new StringType(modeString));
    }

    @Override
    public void setTargetTemperature(Double value) throws Exception {
        targetTemperatureItem.send(new DecimalType(BigDecimal.valueOf(convertFromCelsius(value))));
    }

    @Override
    public void subscribeCurrentMode(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(heatingCoolingModeItem, callback);
    }

    @Override
    public void subscribeCurrentTemperature(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(currentTemperatureItem, callback);
    }

    @Override
    public void subscribeTargetMode(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(heatingCoolingModeItem, callback);
    }

    @Override
    public void subscribeTargetTemperature(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(targetTemperatureItem, callback);
    }

    @Override
    public void unsubscribeCurrentMode() {
        getUpdater().unsubscribe(heatingCoolingModeItem);
    }

    @Override
    public void unsubscribeCurrentTemperature() {
        getUpdater().unsubscribe(currentTemperatureItem);
    }

    @Override
    public void unsubscribeTargetMode() {
        getUpdater().unsubscribe(heatingCoolingModeItem);
    }

    @Override
    public void unsubscribeTargetTemperature() {
        getUpdater().unsubscribe(targetTemperatureItem);
    }
}
