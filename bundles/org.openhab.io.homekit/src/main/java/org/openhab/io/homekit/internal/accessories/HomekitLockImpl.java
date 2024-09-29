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

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitException;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.LockMechanismAccessory;
import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.lock.LockCurrentStateEnum;
import io.github.hapjava.characteristics.impl.lock.LockTargetStateEnum;
import io.github.hapjava.services.impl.LockMechanismService;

/**
 * Implements the support of Lock accessories, mapping them to OpenHAB Switch type
 *
 * @author blafois - Initial contribution.
 *
 */
public class HomekitLockImpl extends AbstractHomekitAccessoryImpl implements LockMechanismAccessory {
    final Map<LockCurrentStateEnum, Object> currentStateMapping;
    final Map<LockTargetStateEnum, Object> targetStateMapping;

    public HomekitLockImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            List<Characteristic> mandatoryRawCharacteristics, HomekitAccessoryUpdater updater,
            HomekitSettings settings) {
        super(taggedItem, mandatoryCharacteristics, mandatoryRawCharacteristics, updater, settings);

        currentStateMapping = createMapping(HomekitCharacteristicType.LOCK_CURRENT_STATE, LockCurrentStateEnum.class);
        targetStateMapping = createMapping(HomekitCharacteristicType.LOCK_TARGET_STATE, LockTargetStateEnum.class);
    }

    @Override
    public void init() throws HomekitException {
        super.init();
        addService(new LockMechanismService(this));
    }

    @Override
    public CompletableFuture<LockCurrentStateEnum> getLockCurrentState() {
        return CompletableFuture.completedFuture(getKeyFromMapping(HomekitCharacteristicType.LOCK_CURRENT_STATE,
                currentStateMapping, LockCurrentStateEnum.UNKNOWN));
    }

    @Override
    public CompletableFuture<LockTargetStateEnum> getLockTargetState() {
        return CompletableFuture.completedFuture(getKeyFromMapping(HomekitCharacteristicType.LOCK_TARGET_STATE,
                targetStateMapping, LockTargetStateEnum.UNSECURED));
    }

    @Override
    public CompletableFuture<Void> setLockTargetState(LockTargetStateEnum state) {
        HomekitCharacteristicFactory.setValueFromEnum(
                getCharacteristic(HomekitCharacteristicType.LOCK_TARGET_STATE).get(), state, targetStateMapping);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeLockCurrentState(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.LOCK_CURRENT_STATE, callback);
    }

    @Override
    public void unsubscribeLockCurrentState() {
        unsubscribe(HomekitCharacteristicType.LOCK_CURRENT_STATE);
    }

    @Override
    public void subscribeLockTargetState(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.LOCK_TARGET_STATE, callback);
    }

    @Override
    public void unsubscribeLockTargetState() {
        unsubscribe(HomekitCharacteristicType.LOCK_TARGET_STATE);
    }
}
