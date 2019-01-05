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

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.ContactSensor;
import com.beowulfe.hap.accessories.properties.ContactState;

/**
 * Implements Contact sensor using an Item that provides an On/Off state.
 *
 * @author Philipp Arndt
 */
public class HomekitContactSensorImpl extends AbstractHomekitAccessoryImpl<SwitchItem> implements ContactSensor {

    public HomekitContactSensorImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater) {
        super(taggedItem, itemRegistry, updater, SwitchItem.class);
    }

    @Override
    public CompletableFuture<ContactState> getCurrentState() {
        OnOffType state = getItem().getStateAs(OnOffType.class);
        return CompletableFuture
                .completedFuture(state == OnOffType.ON ? ContactState.DETECTED : ContactState.NOT_DETECTED);
    }

    @Override
    public void subscribeContactState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeContactState() {
        getUpdater().unsubscribe(getItem());
    }

}
