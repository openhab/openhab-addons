/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
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
 * @author Andy Lintner
 */
class HomekitThermostatImpl extends AbstractTemperatureHomekitAccessoryImpl<GroupItem>
        implements BasicThermostat, GroupedAccessory {

    private final String groupName;
    private final HomekitSettings settings;
    private String currentTemperatureItemName;
    private String heatingCoolingModeItemName;
    private String targetTemperatureItemName;

    private Logger logger = LoggerFactory.getLogger(HomekitThermostatImpl.class);

    public HomekitThermostatImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, HomekitSettings settings) {
        super(taggedItem, itemRegistry, updater, settings, GroupItem.class);
        this.groupName = taggedItem.getItem().getName();
        this.settings = settings;
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public void addCharacteristic(HomekitTaggedItem item) {
        switch (item.getCharacteristicType()) {
            case CURRENT_TEMPERATURE:
                currentTemperatureItemName = item.getItem().getName();
                break;

            case HEATING_COOLING_MODE:
                heatingCoolingModeItemName = item.getItem().getName();
                break;

            case TARGET_TEMPERATURE:
                targetTemperatureItemName = item.getItem().getName();
                break;

            default:
                logger.error("Unrecognized thermostat characteristic: {}", item.getCharacteristicType().name());
                break;

        }
    }

    @Override
    public boolean isComplete() {
        return targetTemperatureItemName != null && currentTemperatureItemName != null
                && heatingCoolingModeItemName != null;
    }

    @Override
    public CompletableFuture<ThermostatMode> getCurrentMode() {
        Item item = getItemRegistry().get(heatingCoolingModeItemName);
        State state = item.getState();
        ThermostatMode mode;
        if (state != null) {
            String stringValue = state.toString();

            if (stringValue.equals(settings.getThermostatCoolMode())) {
                mode = ThermostatMode.COOL;
            } else if (stringValue.equals(settings.getThermostatHeatMode())) {
                mode = ThermostatMode.HEAT;
            } else if (stringValue.equals(settings.getThermostatAutoMode())) {
                mode = ThermostatMode.AUTO;
            } else if (stringValue.equals(settings.getThermostatOffMode())) {
                mode = ThermostatMode.OFF;
            } else if (stringValue.equals("UNDEF") || stringValue.equals("NULL")) {
                logger.debug("Heating cooling target mode not available. Relaying value of OFF to Homekit");
                mode = ThermostatMode.OFF;
            } else {
                logger.error(
                        "Unrecognized heating cooling target mode: {}. Expected cool, heat, auto, or off strings in value.",
                        stringValue);
                mode = ThermostatMode.OFF;
            }
        } else {
            logger.info("Heating cooling target mode not available. Relaying value of OFF to Homekit");
            mode = ThermostatMode.OFF;
        }
        return CompletableFuture.completedFuture(mode);
    }

    @Override
    public CompletableFuture<Double> getCurrentTemperature() {
        Item item = getItemRegistry().get(currentTemperatureItemName);
        DecimalType state = (DecimalType) item.getStateAs(DecimalType.class);
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
        if (targetTemperatureItemName != null) {
            Item item = getItemRegistry().get(targetTemperatureItemName);
            DecimalType state = (DecimalType) item.getStateAs(DecimalType.class);
            if (state == null) {
                return CompletableFuture.completedFuture(null);
            }
            return CompletableFuture.completedFuture(convertToCelsius(state.doubleValue()));
        } else {
            return CompletableFuture.completedFuture(null);
        }
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
        StringItem item = getGenericItem(heatingCoolingModeItemName);
        item.send(new StringType(modeString));
    }

    @Override
    public void setTargetTemperature(Double value) throws Exception {
        NumberItem item = getGenericItem(targetTemperatureItemName);
        item.send(new DecimalType(BigDecimal.valueOf(convertFromCelsius(value))));
    }

    @Override
    public void subscribeCurrentMode(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getGenericItem(heatingCoolingModeItemName), callback);
    }

    @Override
    public void subscribeCurrentTemperature(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getGenericItem(currentTemperatureItemName), callback);
    }

    @Override
    public void subscribeTargetMode(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getGenericItem(heatingCoolingModeItemName), callback);
    }

    @Override
    public void subscribeTargetTemperature(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getGenericItem(targetTemperatureItemName), callback);
    }

    @Override
    public void unsubscribeCurrentMode() {
        getUpdater().unsubscribe(getGenericItem(heatingCoolingModeItemName));
    }

    @Override
    public void unsubscribeCurrentTemperature() {
        getUpdater().unsubscribe(getGenericItem(currentTemperatureItemName));
    }

    @Override
    public void unsubscribeTargetMode() {
        getUpdater().unsubscribe(getGenericItem(heatingCoolingModeItemName));
    }

    @Override
    public void unsubscribeTargetTemperature() {
        getUpdater().unsubscribe(getGenericItem(targetTemperatureItemName));
    }

    @SuppressWarnings("unchecked")
    private <T extends GenericItem> T getGenericItem(String name) {
        Item item = getItemRegistry().get(name);
        if (item == null) {
            return null;
        }
        if (!(item instanceof GenericItem)) {
            throw new RuntimeException("Expected GenericItem, found " + item.getClass().getCanonicalName());
        }
        return (T) item;
    }

}
