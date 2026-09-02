/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.ACTIVE;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.CURRENT_AIR_PURIFIER_STATE;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.TARGET_AIR_PURIFIER_STATE;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitException;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.AirPurifierAccessory;
import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.airpurifier.CurrentAirPurifierStateEnum;
import io.github.hapjava.characteristics.impl.airpurifier.TargetAirPurifierStateEnum;
import io.github.hapjava.services.impl.AirPurifierService;

/**
 * Implements Air Purifier
 *
 * @author Cody Cutrer - Initial contribution
 */
public class HomekitAirPurifierImpl extends AbstractHomekitAccessoryImpl implements AirPurifierAccessory {
    private final BooleanItemReader activeReader;
    private final Map<CurrentAirPurifierStateEnum, Object> currentStateMapping;
    private final Map<TargetAirPurifierStateEnum, Object> targetStateMapping;

    public HomekitAirPurifierImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            List<Characteristic> mandatoryRawCharacteristics, HomekitAccessoryUpdater updater, HomekitSettings settings)
            throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, mandatoryRawCharacteristics, updater, settings);
        activeReader = createBooleanReader(ACTIVE);
        currentStateMapping = createMapping(CURRENT_AIR_PURIFIER_STATE, CurrentAirPurifierStateEnum.class);
        targetStateMapping = createMapping(TARGET_AIR_PURIFIER_STATE, TargetAirPurifierStateEnum.class);
    }

    @Override
    public void init() throws HomekitException {
        super.init();
        addService(new AirPurifierService(this));
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
    public CompletableFuture<CurrentAirPurifierStateEnum> getCurrentState() {
        return CompletableFuture.completedFuture(getKeyFromMapping(CURRENT_AIR_PURIFIER_STATE, currentStateMapping,
                CurrentAirPurifierStateEnum.INACTIVE));
    }

    @Override
    public CompletableFuture<TargetAirPurifierStateEnum> getTargetState() {
        return CompletableFuture.completedFuture(
                getKeyFromMapping(TARGET_AIR_PURIFIER_STATE, targetStateMapping, TargetAirPurifierStateEnum.AUTO));
    }

    @Override
    public CompletableFuture<Void> setTargetState(TargetAirPurifierStateEnum state) {
        HomekitCharacteristicFactory.setValueFromEnum(
                getCharacteristic(HomekitCharacteristicType.TARGET_AIR_PURIFIER_STATE).get(), state,
                targetStateMapping);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeActive(HomekitCharacteristicChangeCallback callback) {
        subscribe(ACTIVE, callback);
    }

    @Override
    public void unsubscribeActive() {
        unsubscribe(ACTIVE);
    }

    @Override
    public void subscribeCurrentState(HomekitCharacteristicChangeCallback callback) {
        subscribe(CURRENT_AIR_PURIFIER_STATE, callback);
    }

    @Override
    public void unsubscribeCurrentState() {
        unsubscribe(CURRENT_AIR_PURIFIER_STATE);
    }

    @Override
    public void subscribeTargetState(HomekitCharacteristicChangeCallback callback) {
        subscribe(TARGET_AIR_PURIFIER_STATE, callback);
    }

    @Override
    public void unsubscribeTargetState() {
        unsubscribe(TARGET_AIR_PURIFIER_STATE);
    }
}
