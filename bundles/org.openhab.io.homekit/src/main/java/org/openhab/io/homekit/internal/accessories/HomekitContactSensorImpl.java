/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.ContactSensor;
import com.beowulfe.hap.accessories.properties.ContactState;

/**
 * Implements Contact sensor using an Item that provides an On/Off state.
 *
 * @author Philipp Arndt - Initial contribution
 */
public class HomekitContactSensorImpl extends AbstractHomekitAccessoryImpl<ContactItem> implements ContactSensor {

    public HomekitContactSensorImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater) {
        super(taggedItem, itemRegistry, updater, ContactItem.class);
    }

    @Override
    public CompletableFuture<ContactState> getCurrentState() {
        OpenClosedType state = getItem().getStateAs(OpenClosedType.class);
        return CompletableFuture
                .completedFuture(state == OpenClosedType.CLOSED ? ContactState.DETECTED : ContactState.NOT_DETECTED);
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
