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

import io.github.hapjava.accessories.SwitchAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.services.impl.SwitchService;

/**
 * Implements Switch using an Item that provides an On/Off state.
 *
 * @author Andy Lintner - Initial contribution
 */
public class HomekitSwitchImpl extends AbstractHomekitAccessoryImpl implements SwitchAccessory {

    public HomekitSwitchImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        getServices().add(new SwitchService(this));
    }

    @Override
    public CompletableFuture<Boolean> getSwitchState() {
        OnOffType state = getStateAs(HomekitCharacteristicType.ON_STATE, OnOffType.class);
        return CompletableFuture.completedFuture(state == OnOffType.ON);
    }

    @Override
    public CompletableFuture<Void> setSwitchState(boolean state) {
        GenericItem item = getItem(HomekitCharacteristicType.ON_STATE, GenericItem.class);
        if (item instanceof SwitchItem) {
            ((SwitchItem) item).send(OnOffType.from(state));
        } else if (item instanceof GroupItem) {
            ((GroupItem) item).send(OnOffType.from(state));
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeSwitchState(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.ON_STATE, callback);
    }

    @Override
    public void unsubscribeSwitchState() {
        unsubscribe(HomekitCharacteristicType.ON_STATE);
    }
}
