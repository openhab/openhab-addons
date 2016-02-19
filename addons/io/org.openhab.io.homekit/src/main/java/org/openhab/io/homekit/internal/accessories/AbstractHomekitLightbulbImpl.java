/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.Lightbulb;

/**
 * Abstract class implementing a Homekit Lightbulb using a SwitchItem
 *
 * @author Andy Lintner
 */
abstract class AbstractHomekitLightbulbImpl<T extends SwitchItem> extends AbstractHomekitAccessoryImpl<T>
        implements Lightbulb {

    public AbstractHomekitLightbulbImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, Class<T> expectedItemClass) {
        super(taggedItem, itemRegistry, updater, expectedItemClass);
    }

    @Override
    public CompletableFuture<Boolean> getLightbulbPowerState() {
        OnOffType state = (OnOffType) getItem().getStateAs(OnOffType.class);
        return CompletableFuture.completedFuture(state == OnOffType.ON);
    }

    @Override
    public CompletableFuture<Void> setLightbulbPowerState(boolean value) throws Exception {
        getItem().send(value ? OnOffType.ON : OnOffType.OFF);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeLightbulbPowerState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeLightbulbPowerState() {
        getUpdater().unsubscribe(getItem());
    }

}