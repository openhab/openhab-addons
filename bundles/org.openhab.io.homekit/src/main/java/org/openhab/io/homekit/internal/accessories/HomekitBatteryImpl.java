/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.BATTERY_CHARGING_STATE;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.BATTERY_LEVEL;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.BATTERY_LOW_STATUS;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.BatteryAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.battery.ChargingStateEnum;
import io.github.hapjava.characteristics.impl.battery.StatusLowBatteryEnum;
import io.github.hapjava.services.impl.BatteryService;

/**
 * Implements battery services for chargeable and non-chargeable batteries
 *
 * @author Eugen Freiter - Initial contribution
 */
public class HomekitBatteryImpl extends AbstractHomekitAccessoryImpl implements BatteryAccessory {
    public static final String BATTERY_TYPE = "chargeable";

    private final BooleanItemReader lowBatteryReader;
    private BooleanItemReader chargingBatteryReader;
    private final boolean isChargeable;
    private final BigDecimal lowThreshold;

    public HomekitBatteryImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        lowThreshold = getAccessoryConfiguration(HomekitCharacteristicType.BATTERY_LOW_STATUS,
                HomekitTaggedItem.BATTERY_LOW_THRESHOLD, BigDecimal.valueOf(20));
        lowBatteryReader = createBooleanReader(BATTERY_LOW_STATUS, lowThreshold, true);
        isChargeable = getAccessoryConfigurationAsBoolean(BATTERY_TYPE, false);
        if (isChargeable) {
            chargingBatteryReader = createBooleanReader(BATTERY_CHARGING_STATE);
        }
        getServices().add(new BatteryService(this));
    }

    @Override
    public CompletableFuture<Integer> getBatteryLevel() {
        final @Nullable DecimalType state = getStateAs(BATTERY_LEVEL, DecimalType.class);
        return CompletableFuture.completedFuture(state != null ? state.intValue() : 0);
    }

    @Override
    public CompletableFuture<StatusLowBatteryEnum> getLowBatteryState() {
        return CompletableFuture
                .completedFuture(lowBatteryReader.getValue() ? StatusLowBatteryEnum.LOW : StatusLowBatteryEnum.NORMAL);
    }

    @Override
    public CompletableFuture<ChargingStateEnum> getChargingState() {
        return CompletableFuture.completedFuture(isChargeable
                ? chargingBatteryReader.getValue() ? ChargingStateEnum.CHARGING : ChargingStateEnum.NOT_CHARGING
                : ChargingStateEnum.NOT_CHARABLE); // the mapping to NOT_CHARABLE is correct, there is a typo in java
                                                   // HAP
    }

    @Override
    public void subscribeBatteryLevel(final HomekitCharacteristicChangeCallback callback) {
        subscribe(BATTERY_LEVEL, callback);
    }

    @Override
    public void subscribeLowBatteryState(final HomekitCharacteristicChangeCallback callback) {
        subscribe(BATTERY_LOW_STATUS, callback);
    }

    @Override
    public void subscribeBatteryChargingState(final HomekitCharacteristicChangeCallback callback) {
        if (isChargeable) {
            subscribe(BATTERY_CHARGING_STATE, callback);
        }
    }

    @Override
    public void unsubscribeBatteryLevel() {
        unsubscribe(BATTERY_LEVEL);
    }

    @Override
    public void unsubscribeLowBatteryState() {
        unsubscribe(BATTERY_LOW_STATUS);
    }

    @Override
    public void unsubscribeBatteryChargingState() {
        if (isChargeable) {
            unsubscribe(BATTERY_CHARGING_STATE);
        }
    }
}
