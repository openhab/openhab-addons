/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.Service;
import com.beowulfe.hap.accessories.LockableLockMechanism;
import com.beowulfe.hap.accessories.properties.LockMechanismState;
import com.beowulfe.hap.impl.services.LockMechanismService;

/**
 * Implements LockableLockMechanism using an Item that provides an On/Off state.
 *
 * Unfortunately, we cannot use OpenClosedType because ContactItems cannot receive commands.
 *
 * @author Felix Rotthowe
 *
 */
public class HomekitLockImpl extends AbstractHomekitAccessoryImpl<SwitchItem>implements LockableLockMechanism {

    public HomekitLockImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry, HomekitAccessoryUpdater updater) {
        super(taggedItem, itemRegistry, updater, SwitchItem.class);
    }

    @Override
    public CompletableFuture<LockMechanismState> getCurrentMechanismState() {
        OnOffType state = (OnOffType) getItem().getStateAs(OnOffType.class);
        if (state == null || getItem().getState() instanceof UnDefType) {
            return CompletableFuture.completedFuture(LockMechanismState.UNKNOWN);
        }
        return CompletableFuture.completedFuture(
                state.equals(OnOffType.ON) ? LockMechanismState.SECURED : LockMechanismState.UNSECURED);
    }

    @Override
    public void setTargetMechanismState(LockMechanismState state) throws Exception {
        getItem().send(state.equals(LockMechanismState.SECURED) ? OnOffType.ON : OnOffType.OFF);
    }

    @Override
    public CompletableFuture<LockMechanismState> getTargetMechanismState() {
        // TODO: Maybe implement this as a group in combination with a ContactType to
        // differentiate between the current and the target state.
        return getCurrentMechanismState();
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
    public void subscribeTargetMechanismState(HomekitCharacteristicChangeCallback callback) {
        // Not supported
    }

    @Override
    public void unsubscribeTargetMechanismState() {
        // Not supported
    }

    @Override
    public Collection<Service> getServices() {
        return Collections.singleton(new LockMechanismService(this));
    }

}