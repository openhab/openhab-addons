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

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
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
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
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
        GenericItem item = getItem(HomekitCharacteristicType.ON_STATE, GenericItem.class);
        if (item instanceof SwitchItem) {
            ((SwitchItem) item).send(OnOffType.from(value));
        } else if (item instanceof GroupItem) {
            ((GroupItem) item).send(OnOffType.from(value));
        }
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
