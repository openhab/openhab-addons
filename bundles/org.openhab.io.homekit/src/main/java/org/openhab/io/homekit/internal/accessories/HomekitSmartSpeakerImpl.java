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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.CURRENT_MEDIA_STATE;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.TARGET_MEDIA_STATE;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitException;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.SmartSpeakerAccessory;
import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.television.CurrentMediaStateEnum;
import io.github.hapjava.characteristics.impl.television.TargetMediaStateEnum;
import io.github.hapjava.services.impl.SmartSpeakerService;

/**
 *
 * @author Eugen Freiter - Initial contribution
 */
public class HomekitSmartSpeakerImpl extends AbstractHomekitAccessoryImpl implements SmartSpeakerAccessory {
    private final Map<CurrentMediaStateEnum, Object> currentMediaState;
    private final Map<TargetMediaStateEnum, Object> targetMediaState;

    public HomekitSmartSpeakerImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            List<Characteristic> mandatoryRawCharacteristics, HomekitAccessoryUpdater updater,
            HomekitSettings settings) {
        super(taggedItem, mandatoryCharacteristics, mandatoryRawCharacteristics, updater, settings);
        currentMediaState = createMapping(CURRENT_MEDIA_STATE, CurrentMediaStateEnum.class);
        targetMediaState = createMapping(TARGET_MEDIA_STATE, TargetMediaStateEnum.class);
    }

    @Override
    public void init() throws HomekitException {
        super.init();
        addService(new SmartSpeakerService(this));
    }

    @Override
    public CompletableFuture<CurrentMediaStateEnum> getCurrentMediaState() {
        return CompletableFuture.completedFuture(
                getKeyFromMapping(CURRENT_MEDIA_STATE, currentMediaState, CurrentMediaStateEnum.UNKNOWN));
    }

    @Override
    public void subscribeCurrentMediaState(final HomekitCharacteristicChangeCallback callback) {
        subscribe(CURRENT_MEDIA_STATE, callback);
    }

    @Override
    public void unsubscribeCurrentMediaState() {
        unsubscribe(CURRENT_MEDIA_STATE);
    }

    @Override
    public CompletableFuture<TargetMediaStateEnum> getTargetMediaState() {
        return CompletableFuture
                .completedFuture(getKeyFromMapping(TARGET_MEDIA_STATE, targetMediaState, TargetMediaStateEnum.STOP));
    }

    @Override
    public CompletableFuture<Void> setTargetMediaState(final TargetMediaStateEnum targetState) {
        HomekitCharacteristicFactory.setValueFromEnum(getCharacteristic(TARGET_MEDIA_STATE).get(), targetState,
                targetMediaState);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeTargetMediaState(final HomekitCharacteristicChangeCallback callback) {
        subscribe(TARGET_MEDIA_STATE, callback);
    }

    @Override
    public void unsubscribeTargetMediaState() {
        unsubscribe(TARGET_MEDIA_STATE);
    }
}
