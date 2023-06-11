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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.OutletAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.services.impl.OutletService;

/**
 *
 * @author Eugen Freiter - Initial contribution
 */
public class HomekitOutletImpl extends AbstractHomekitAccessoryImpl implements OutletAccessory {
    private final BooleanItemReader inUseReader;
    private final BooleanItemReader onReader;

    public HomekitOutletImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        inUseReader = createBooleanReader(HomekitCharacteristicType.INUSE_STATUS);
        onReader = createBooleanReader(HomekitCharacteristicType.ON_STATE);
        getServices().add(new OutletService(this));
    }

    @Override
    public CompletableFuture<Boolean> getPowerState() {
        return CompletableFuture.completedFuture(onReader.getValue());
    }

    @Override
    public CompletableFuture<Boolean> getOutletInUse() {
        return CompletableFuture.completedFuture(inUseReader.getValue());
    }

    @Override
    public CompletableFuture<Void> setPowerState(boolean state) {
        onReader.setValue(state);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribePowerState(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.ON_STATE, callback);
    }

    @Override
    public void subscribeOutletInUse(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.INUSE_STATUS, callback);
    }

    @Override
    public void unsubscribePowerState() {
        unsubscribe(HomekitCharacteristicType.ON_STATE);
    }

    @Override
    public void unsubscribeOutletInUse() {
        unsubscribe(HomekitCharacteristicType.INUSE_STATUS);
    }
}
