/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.DimmableLightbulb;

/**
 * Implements DimmableLightBulb using an Item that provides a On/Off and Percent state.
 *
 * @author Andy Lintner
 */
class HomekitDimmableLightbulbImpl extends AbstractHomekitLightbulbImpl<DimmerItem>implements DimmableLightbulb {

    public HomekitDimmableLightbulbImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater) {
        super(taggedItem, itemRegistry, updater, DimmerItem.class);
    }

    @Override
    public CompletableFuture<Integer> getBrightness() {
        State state = getItem().getStateAs(PercentType.class);
        if (state instanceof PercentType) {
            PercentType brightness = (PercentType) state;
            return CompletableFuture.completedFuture(brightness.intValue());
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> setBrightness(Integer value) throws Exception {
        GenericItem item = getItem();
        if (item instanceof DimmerItem) {
            ((DimmerItem) item).send(new PercentType(value));
        } else if (item instanceof GroupItem) {
            ((GroupItem) item).send(new PercentType(value));
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeBrightness(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), "brightness", callback);
    }

    @Override
    public void unsubscribeBrightness() {
        getUpdater().unsubscribe(getItem(), "brightness");
    }

}
