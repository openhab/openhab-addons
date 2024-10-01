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
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitException;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.WindowCoveringAccessory;
import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.windowcovering.PositionStateEnum;
import io.github.hapjava.services.impl.WindowCoveringService;

/**
 *
 * @author epike - Initial contribution
 */
@NonNullByDefault
public class HomekitWindowCoveringImpl extends AbstractHomekitPositionAccessoryImpl implements WindowCoveringAccessory {

    public HomekitWindowCoveringImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            List<Characteristic> mandatoryRawCharacteristics, HomekitAccessoryUpdater updater,
            HomekitSettings settings) {
        super(taggedItem, mandatoryCharacteristics, mandatoryRawCharacteristics, updater, settings);
    }

    @Override
    public void init() throws HomekitException {
        super.init();
        addService(new WindowCoveringService(this));
    }

    @Override
    @NonNullByDefault({})
    public CompletableFuture<Integer> getCurrentPosition() {
        return super.getCurrentPosition();
    }

    @Override
    @NonNullByDefault({})
    public CompletableFuture<PositionStateEnum> getPositionState() {
        return super.getPositionState();
    }

    @Override
    @NonNullByDefault({})
    public CompletableFuture<Integer> getTargetPosition() {
        return super.getTargetPosition();
    }

    @Override
    @NonNullByDefault({})
    public CompletableFuture<Void> setTargetPosition(int value) {
        return super.setTargetPosition(value);
    }

    @Override
    @NonNullByDefault({})
    public void subscribeCurrentPosition(HomekitCharacteristicChangeCallback callback) {
        super.subscribeCurrentPosition(callback);
    }

    @Override
    @NonNullByDefault({})
    public void subscribePositionState(HomekitCharacteristicChangeCallback callback) {
        super.subscribePositionState(callback);
    }

    @Override
    @NonNullByDefault({})
    public void subscribeTargetPosition(HomekitCharacteristicChangeCallback callback) {
        super.subscribeTargetPosition(callback);
    }

    @Override
    @NonNullByDefault({})
    public void unsubscribeCurrentPosition() {
        super.unsubscribeCurrentPosition();
    }

    @Override
    @NonNullByDefault({})
    public void unsubscribePositionState() {
        super.unsubscribePositionState();
    }

    @Override
    @NonNullByDefault({})
    public void unsubscribeTargetPosition() {
        super.unsubscribeTargetPosition();
    }
}
