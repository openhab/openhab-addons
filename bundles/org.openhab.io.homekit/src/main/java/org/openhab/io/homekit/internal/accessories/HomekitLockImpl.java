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

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.HomekitCharacteristicChangeCallback;
import io.github.hapjava.accessories.LockableLockMechanism;
import io.github.hapjava.accessories.properties.LockMechanismState;

/**
 * Implements the support of Lock accessories, mapping them to OpenHAB Switch type 
 *
 * @author blafois - Support for additional accessory type.
 *
 */
public class HomekitLockImpl extends AbstractHomekitAccessoryImpl<SwitchItem> implements LockableLockMechanism {

    public HomekitLockImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry, HomekitAccessoryUpdater updater) {
        super(taggedItem, itemRegistry, updater, SwitchItem.class);
    }

    @Override
    public CompletableFuture<LockMechanismState> getCurrentMechanismState() {
        OnOffType state = getItem().getStateAs(OnOffType.class);

        if (state == OnOffType.OFF) {
            return CompletableFuture.completedFuture(LockMechanismState.SECURED);
        } else if (state == OnOffType.ON) {
            return CompletableFuture.completedFuture(LockMechanismState.UNSECURED);
        }

        return CompletableFuture.completedFuture(LockMechanismState.UNKNOWN);
    }

    @Override
    public void subscribeCurrentMechanismState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeCurrentMechanismState() {
        getUpdater().unsubscribe(getItem());
    }

    @Override
    public void setTargetMechanismState(LockMechanismState state) throws Exception {
        switch (state) {
            case SECURED:
                // Close the door
                if (getItem() instanceof SwitchItem) {
                    ((SwitchItem) getItem()).send(OnOffType.OFF);
                }
                break;
            case UNSECURED:
                // Open the door
                if (getItem() instanceof SwitchItem) {
                    ((SwitchItem) getItem()).send(OnOffType.ON);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public CompletableFuture<LockMechanismState> getTargetMechanismState() {
        OnOffType state = getItem().getStateAs(OnOffType.class);

        if (state == OnOffType.OFF) {
            return CompletableFuture.completedFuture(LockMechanismState.SECURED);
        } else if (state == OnOffType.ON) {
            return CompletableFuture.completedFuture(LockMechanismState.UNSECURED);
        }

        return CompletableFuture.completedFuture(LockMechanismState.UNKNOWN);
    }

    @Override
    public void subscribeTargetMechanismState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeTargetMechanismState() {
        getUpdater().unsubscribe(getItem());
    }

}
