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
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import io.github.hapjava.accessories.ValveAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.common.ActiveEnum;
import io.github.hapjava.characteristics.impl.common.InUseEnum;
import io.github.hapjava.characteristics.impl.valve.ValveTypeEnum;
import io.github.hapjava.services.impl.ValveService;

/**
 *
 * @author Tim Harper - Initial contribution
 */
public class HomekitValveImpl extends AbstractHomekitAccessoryImpl<SwitchItem> implements ValveAccessory {
    public HomekitValveImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics, ItemRegistry itemRegistry, HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, itemRegistry, updater, settings);
        getServices().add(new ValveService(this));
    }

    @Override
    public CompletableFuture<ActiveEnum> getValveActive() {
        OnOffType state = getItem().getStateAs(OnOffType.class);
        if (state == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(state == OnOffType.ON?ActiveEnum.ACTIVE:ActiveEnum.INACTIVE);
    }

    @Override
    public CompletableFuture<Void> setValveActive(ActiveEnum state) throws Exception {
        GenericItem item = getItem();
        if (item instanceof SwitchItem) {
            ((SwitchItem) item).send(state == ActiveEnum.ACTIVE ? OnOffType.ON : OnOffType.OFF);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeValveActive(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeValveActive() {
        getUpdater().unsubscribe(getItem());
    }

    @Override
    public CompletableFuture<InUseEnum> getValveInUse() {
        OnOffType state = getItem().getStateAs(OnOffType.class);
        if (state == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(state == OnOffType.ON?InUseEnum.IN_USE:InUseEnum.NOT_IN_USE);
    }

    @Override
    public void subscribeValveInUse(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), "inUse", callback);
    }

    @Override
    public void unsubscribeValveInUse() {
        getUpdater().unsubscribe(getItem(), "inUse");
    }

    @Override
    public CompletableFuture<ValveTypeEnum> getValveType() {
        // TODO - make this configurable; possibly via additional tags? ValveType:Generic etc
        return CompletableFuture.completedFuture(ValveTypeEnum.GENERIC);
    }

    @Override
    public void subscribeValveType(HomekitCharacteristicChangeCallback callback) {
        // nothing changes here
    }

    @Override
    public void unsubscribeValveType() {
        // nothing changes here
    }
}
