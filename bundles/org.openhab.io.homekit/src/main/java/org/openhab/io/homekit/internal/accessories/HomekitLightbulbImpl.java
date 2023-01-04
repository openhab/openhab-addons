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

import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitCommandType;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.LightbulbAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.services.impl.LightbulbService;

/**
 * Implements Lightbulb using an Item that provides an On/Off state
 *
 * @author Andy Lintner - Initial contribution
 */
class HomekitLightbulbImpl extends AbstractHomekitAccessoryImpl implements LightbulbAccessory {

    public HomekitLightbulbImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        this.getServices().add(new LightbulbService(this));
    }

    @Override
    public CompletableFuture<Boolean> getLightbulbPowerState() {
        OnOffType state = getStateAs(HomekitCharacteristicType.ON_STATE, OnOffType.class);
        return CompletableFuture.completedFuture(state == OnOffType.ON);
    }

    @Override
    public CompletableFuture<Void> setLightbulbPowerState(boolean value) {
        getCharacteristic(HomekitCharacteristicType.ON_STATE).ifPresent(tItem -> {
            final OnOffType onOffState = OnOffType.from(value);
            if (tItem.getBaseItem() instanceof DimmerItem) {
                tItem.sendCommandProxy(HomekitCommandType.ON_COMMAND, onOffState);
            } else if (tItem.getBaseItem() instanceof SwitchItem) {
                tItem.send(onOffState);
            }
        });
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeLightbulbPowerState(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.ON_STATE, callback);
    }

    @Override
    public void unsubscribeLightbulbPowerState() {
        unsubscribe(HomekitCharacteristicType.ON_STATE);
    }
}
