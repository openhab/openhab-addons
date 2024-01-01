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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.ACTIVE_STATUS;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.FanAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.services.impl.FanService;

/**
 * Implements Fan using an Item that provides an On/Off state
 *
 * @author Eugen Freiter - Initial contribution
 */
class HomekitFanImpl extends AbstractHomekitAccessoryImpl implements FanAccessory {
    private final BooleanItemReader activeReader;

    public HomekitFanImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        activeReader = createBooleanReader(ACTIVE_STATUS);
        this.getServices().add(new FanService(this));
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
    public void subscribeActive(HomekitCharacteristicChangeCallback callback) {
        subscribe(ACTIVE_STATUS, callback);
    }

    @Override
    public void unsubscribeActive() {
        unsubscribe(ACTIVE_STATUS);
    }
}
