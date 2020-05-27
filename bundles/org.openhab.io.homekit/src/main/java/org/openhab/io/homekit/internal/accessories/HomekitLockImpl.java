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
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.Item;
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
 * @author blafois - Initial contribution.
 *
 */
public class HomekitLockImpl extends AbstractHomekitAccessoryImpl implements LockMechanismAccessory {

    public HomekitLockImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        getServices().add(new LockMechanismService(this));
    }

    @Override
    public CompletableFuture<LockCurrentStateEnum> getLockCurrentState() {
        @Nullable
        OnOffType state = getStateAs(HomekitCharacteristicType.LOCK_CURRENT_STATE, OnOffType.class);
        if (state != null) {
            return CompletableFuture.completedFuture(
                    state == OnOffType.OFF ? LockCurrentStateEnum.SECURED : LockCurrentStateEnum.UNSECURED);
        }
        return CompletableFuture.completedFuture(LockCurrentStateEnum.UNKNOWN);
    }

    @Override
    public CompletableFuture<LockTargetStateEnum> getLockTargetState() {
        @Nullable
        OnOffType state = getStateAs(HomekitCharacteristicType.LOCK_TARGET_STATE, OnOffType.class);
        if (state != null) {
            return CompletableFuture.completedFuture(
                    state == OnOffType.OFF ? LockTargetStateEnum.SECURED : LockTargetStateEnum.UNSECURED);
        }
        return CompletableFuture.completedFuture(LockTargetStateEnum.UNSECURED);
        // Apple HAP specification has onyl SECURED and UNSECURED values for lock target state.
        // unknown does not supported for target state.
    }

    @Override
    public CompletableFuture<Void> setLockTargetState(final LockTargetStateEnum state) {
        @Nullable
        Item item = getItem(HomekitCharacteristicType.LOCK_TARGET_STATE, SwitchItem.class);
        if (item != null)
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
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeLockCurrentState(final HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.LOCK_CURRENT_STATE, callback);
    }

    @Override
    public void unsubscribeLockCurrentState() {
        unsubscribe(HomekitCharacteristicType.LOCK_CURRENT_STATE);
    }

    @Override
    public void subscribeLockTargetState(final HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.LOCK_TARGET_STATE, callback);
    }

    @Override
    public void unsubscribeLockTargetState() {
        unsubscribe(HomekitCharacteristicType.LOCK_TARGET_STATE);
    }
}
