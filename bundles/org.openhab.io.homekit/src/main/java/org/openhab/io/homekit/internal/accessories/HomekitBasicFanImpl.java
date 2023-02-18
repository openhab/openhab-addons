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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.ON_STATE;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.BasicFanAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.services.impl.BasicFanService;

/**
 * Implements Fan using an Item that provides an On/Off state
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault({})
class HomekitBasicFanImpl extends AbstractHomekitAccessoryImpl implements BasicFanAccessory {
    private final BooleanItemReader onReader;

    public HomekitBasicFanImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        onReader = createBooleanReader(ON_STATE);
        this.getServices().add(new BasicFanService(this));
    }

    @Override
    public CompletableFuture<Boolean> isOn() {
        return CompletableFuture.completedFuture(onReader.getValue());
    }

    @Override
    public CompletableFuture<Void> setOn(boolean state) {
        onReader.setValue(state);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeOn(HomekitCharacteristicChangeCallback callback) {
        subscribe(ON_STATE, callback);
    }

    @Override
    public void unsubscribeOn() {
        unsubscribe(ON_STATE);
    }
}
