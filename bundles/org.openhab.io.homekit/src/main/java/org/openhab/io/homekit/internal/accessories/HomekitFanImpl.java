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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.hapjava.accessories.FanAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.services.impl.FanService;

/**
 * Implements Fan using an Item that provides an On/Off state
 *
 * @author Eugen Freiter - Initial contribution
 */
class HomekitFanImpl extends AbstractHomekitAccessoryImpl<SwitchItem> implements FanAccessory {
    private Logger LOGGER = LoggerFactory.getLogger(HomekitFanImpl.class);

    public HomekitFanImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, itemRegistry, updater, settings);
        this.getServices().add(new FanService(this));
    }

    @Override
    public CompletableFuture<Boolean> isActive() {
        OnOffType state = getItem().getStateAs(OnOffType.class);
        return CompletableFuture.completedFuture(state == OnOffType.ON);
    }

    @Override
    public CompletableFuture<Void> setActive(final boolean state) throws Exception {
        GenericItem item = getItem();
        if (item instanceof SwitchItem) {
            ((SwitchItem) item).send(state ? OnOffType.ON : OnOffType.OFF);
        }  else {
            LOGGER.error("Not supporter item type {}", item.getType());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeActive(final HomekitCharacteristicChangeCallback callback) {getUpdater().subscribe(getItem(), callback); }

    @Override
    public void unsubscribeActive() {getUpdater().unsubscribe(getItem()); }
}
