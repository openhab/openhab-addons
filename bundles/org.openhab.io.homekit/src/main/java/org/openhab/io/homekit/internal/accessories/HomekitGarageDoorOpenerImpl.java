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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.CURRENT_DOOR_STATE;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.OBSTRUCTION_STATUS;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.TARGET_DOOR_STATE;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.accessories.GarageDoorOpenerAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.garagedoor.CurrentDoorStateEnum;
import io.github.hapjava.characteristics.impl.garagedoor.TargetDoorStateEnum;
import io.github.hapjava.services.impl.GarageDoorOpenerService;

/**
 * Implements Garage Door Opener
 *
 * @author Eugen Freiter - Initial contribution
 */
public class HomekitGarageDoorOpenerImpl extends AbstractHomekitAccessoryImpl implements GarageDoorOpenerAccessory {
    private final Logger logger = LoggerFactory.getLogger(HomekitGarageDoorOpenerImpl.class);
    private final BooleanItemReader obstructionReader;
    private final Map<CurrentDoorStateEnum, String> currentDoorStateMapping;
    private final Map<TargetDoorStateEnum, String> targetDoorStateMapping;

    public HomekitGarageDoorOpenerImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        obstructionReader = createBooleanReader(OBSTRUCTION_STATUS);
        currentDoorStateMapping = createMapping(CURRENT_DOOR_STATE, CurrentDoorStateEnum.class, true);
        targetDoorStateMapping = createMapping(TARGET_DOOR_STATE, TargetDoorStateEnum.class, true);

        getServices().add(new GarageDoorOpenerService(this));
    }

    @Override
    public CompletableFuture<CurrentDoorStateEnum> getCurrentDoorState() {
        return CompletableFuture.completedFuture(
                getKeyFromMapping(CURRENT_DOOR_STATE, currentDoorStateMapping, CurrentDoorStateEnum.CLOSED));
    }

    @Override
    public CompletableFuture<TargetDoorStateEnum> getTargetDoorState() {
        return CompletableFuture.completedFuture(
                getKeyFromMapping(TARGET_DOOR_STATE, targetDoorStateMapping, TargetDoorStateEnum.CLOSED));
    }

    @Override
    public CompletableFuture<Boolean> getObstructionDetected() {
        return CompletableFuture.completedFuture(obstructionReader.getValue());
    }

    @Override
    public CompletableFuture<Void> setTargetDoorState(TargetDoorStateEnum targetDoorStateEnum) {
        HomekitCharacteristicFactory.setValueFromEnum(getCharacteristic(TARGET_DOOR_STATE).get(), targetDoorStateEnum,
                targetDoorStateMapping);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeCurrentDoorState(HomekitCharacteristicChangeCallback callback) {
        subscribe(CURRENT_DOOR_STATE, callback);
    }

    @Override
    public void subscribeTargetDoorState(HomekitCharacteristicChangeCallback callback) {
        subscribe(TARGET_DOOR_STATE, callback);
    }

    @Override
    public void subscribeObstructionDetected(HomekitCharacteristicChangeCallback callback) {
        subscribe(OBSTRUCTION_STATUS, callback);
    }

    @Override
    public void unsubscribeCurrentDoorState() {
        unsubscribe(CURRENT_DOOR_STATE);
    }

    @Override
    public void unsubscribeTargetDoorState() {
        unsubscribe(TARGET_DOOR_STATE);
    }

    @Override
    public void unsubscribeObstructionDetected() {
        unsubscribe(OBSTRUCTION_STATUS);
    }
}
