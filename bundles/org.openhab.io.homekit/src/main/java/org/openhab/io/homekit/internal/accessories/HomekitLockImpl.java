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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GenericItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
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
    final OnOffType securedState;
    final OnOffType unsecuredState;

    public HomekitLockImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        securedState = taggedItem.isInverted() ? OnOffType.OFF : OnOffType.ON;
        unsecuredState = taggedItem.isInverted() ? OnOffType.ON : OnOffType.OFF;
        getServices().add(new LockMechanismService(this));
    }

    @Override
    public CompletableFuture<LockCurrentStateEnum> getLockCurrentState() {
        final Optional<GenericItem> item = getItem(HomekitCharacteristicType.LOCK_CURRENT_STATE, GenericItem.class);
        LockCurrentStateEnum lockState = LockCurrentStateEnum.UNKNOWN;
        if (item.isPresent()) {
            final State state = item.get().getState();
            if (state instanceof DecimalType) {
                lockState = LockCurrentStateEnum.fromCode(((DecimalType) state).intValue());
            } else if (state instanceof OnOffType) {
                lockState = state.equals(securedState) ? LockCurrentStateEnum.SECURED : LockCurrentStateEnum.UNSECURED;
            }
        }
        return CompletableFuture.completedFuture(lockState);
    }

    @Override
    public CompletableFuture<LockTargetStateEnum> getLockTargetState() {
        final @Nullable OnOffType state = getStateAs(HomekitCharacteristicType.LOCK_TARGET_STATE, OnOffType.class);
        if (state != null) {
            return CompletableFuture.completedFuture(
                    state == securedState ? LockTargetStateEnum.SECURED : LockTargetStateEnum.UNSECURED);
        }
        return CompletableFuture.completedFuture(LockTargetStateEnum.UNSECURED);
        // Apple HAP specification has only SECURED and UNSECURED values for lock target state.
        // unknown does not supported for target state.
    }

    @Override
    public CompletableFuture<Void> setLockTargetState(LockTargetStateEnum state) {
        getItem(HomekitCharacteristicType.LOCK_TARGET_STATE, SwitchItem.class).ifPresent(item -> {
            switch (state) {
                case SECURED:
                    if (item instanceof SwitchItem) {
                        item.send(securedState);
                    }
                    break;
                case UNSECURED:
                    if (item instanceof SwitchItem) {
                        item.send(unsecuredState);
                    }
                    break;
                default:
                    break;
            }
        });
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
