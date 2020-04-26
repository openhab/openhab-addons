/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.LockMechanismAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.lock.LockCurrentStateEnum;
import io.github.hapjava.characteristics.impl.lock.LockTargetStateEnum;
import io.github.hapjava.services.impl.LockMechanismService;

/**
 * Implements the support of Lock accessories, mapping them to OpenHAB Switch type 
 *
 * @author blafois - Support for additional accessory type.
 *
 */
public class HomekitLockImpl extends AbstractHomekitAccessoryImpl<SwitchItem> implements LockMechanismAccessory {

    public HomekitLockImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics, ItemRegistry itemRegistry, HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, itemRegistry, updater, settings);
        getServices().add(new LockMechanismService(this));
    }


    @Override
    public CompletableFuture<LockCurrentStateEnum> getLockCurrentState() {
        final Optional<HomekitTaggedItem> characteristic = getMandatoryCharacteristic(HomekitCharacteristicType.LOCK_CURRENT_STATE);
        if (characteristic.isPresent()) {
            OnOffType state = characteristic.get().getItem().getStateAs(OnOffType.class);
            if (state == OnOffType.OFF) {
                return CompletableFuture.completedFuture(LockCurrentStateEnum.SECURED);
            } else if (state == OnOffType.ON) {
                return CompletableFuture.completedFuture(LockCurrentStateEnum.UNSECURED);
            }
        }
        return CompletableFuture.completedFuture(LockCurrentStateEnum.UNKNOWN);    }

    @Override
    public CompletableFuture<LockTargetStateEnum> getLockTargetState() {
        final Optional<HomekitTaggedItem> characteristic = getMandatoryCharacteristic(HomekitCharacteristicType.LOCK_TARGET_STATE);
        if (characteristic.isPresent()) {
            OnOffType state = characteristic.get().getItem().getStateAs(OnOffType.class);

            if (state == OnOffType.OFF) {
                return CompletableFuture.completedFuture(LockTargetStateEnum.SECURED);
            }
            else if (state == OnOffType.ON) {
                return CompletableFuture.completedFuture(LockTargetStateEnum.UNSECURED);
            }
        }
        return CompletableFuture.completedFuture(LockTargetStateEnum.UNSECURED);
        // Apple HAP specification has onyl SECURED and UNSECURED values for lock target state.
        // unknown does not supported for target state.
    }

    @Override
    public CompletableFuture<Void> setLockTargetState(final LockTargetStateEnum state) {
        final Optional<HomekitTaggedItem> characteristic = getMandatoryCharacteristic(HomekitCharacteristicType.LOCK_TARGET_STATE);
        if (characteristic.isPresent()) {
            Item item = characteristic.get().getItem();
            switch (state) {
                case SECURED:
                    // Close the door
                    if (item instanceof SwitchItem) {
                        ((SwitchItem) item).send(OnOffType.OFF);
                    }
                    break;
                case UNSECURED:
                    // Open the door
                    if (item instanceof SwitchItem) {
                        ((SwitchItem) item).send(OnOffType.ON);
                    }
                    break;
                default:
                    break;
            }
        }
        return CompletableFuture.completedFuture(null);
    }


    @Override
    public void subscribeLockCurrentState(final HomekitCharacteristicChangeCallback callback) {
        subscribeToCharacteristic(HomekitCharacteristicType.LOCK_CURRENT_STATE, callback);
    }

    @Override
    public void unsubscribeLockCurrentState() {
        unsubscribeFromCharacteristic(HomekitCharacteristicType.LOCK_CURRENT_STATE);
    }

    @Override
    public void subscribeLockTargetState(final HomekitCharacteristicChangeCallback callback) {
        subscribeToCharacteristic(HomekitCharacteristicType.LOCK_TARGET_STATE, callback);
    }

    @Override
    public void unsubscribeLockTargetState() {
        unsubscribeFromCharacteristic(HomekitCharacteristicType.LOCK_TARGET_STATE);
    }

}
