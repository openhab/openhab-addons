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
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
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
public class HomekitSwitchImpl extends AbstractHomekitAccessoryImpl<SwitchItem> implements SwitchAccessory {

    public HomekitSwitchImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics, ItemRegistry itemRegistry, HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, itemRegistry, updater, settings);
        getServices().add(new SwitchService(this));
    }

    @Override
    public CompletableFuture<Boolean> getSwitchState() {
        OnOffType state = getItem().getStateAs(OnOffType.class);
        return CompletableFuture.completedFuture(state == OnOffType.ON);
    }

    @Override
    public CompletableFuture<Void> setSwitchState(boolean state) {
        GenericItem item = getItem();
        if (item instanceof SwitchItem) {
            ((SwitchItem) item).send(state ? OnOffType.ON : OnOffType.OFF);
        } else if (item instanceof GroupItem) {
            ((GroupItem) item).send(state ? OnOffType.ON : OnOffType.OFF);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeSwitchState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeSwitchState() {
        getUpdater().unsubscribe(getItem());
    }
}
