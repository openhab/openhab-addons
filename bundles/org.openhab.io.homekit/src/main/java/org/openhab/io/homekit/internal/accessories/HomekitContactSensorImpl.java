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

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.openhab.io.homekit.internal.battery.BatteryStatus;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.BatteryStatusAccessory;
import com.beowulfe.hap.accessories.ContactSensor;
import com.beowulfe.hap.accessories.properties.ContactState;

/**
 *
 * @author Philipp Arndt - Initial contribution; Tim Harper - support for battery status
 */
public class HomekitContactSensorImpl extends AbstractHomekitAccessoryImpl<GenericItem>
        implements ContactSensor, BatteryStatusAccessory {
    private BatteryStatus batteryStatus;
    private BooleanItemReader contactSensedReader;

    public HomekitContactSensorImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, BatteryStatus batteryStatus) {
        super(taggedItem, itemRegistry, updater, GenericItem.class);
        this.contactSensedReader = new BooleanItemReader(taggedItem.getItem(), OnOffType.OFF, OpenClosedType.CLOSED);
        this.batteryStatus = batteryStatus;
    }

    @Override
    public CompletableFuture<ContactState> getCurrentState() {
        Boolean contactDetected = contactSensedReader.getValue();
        if (contactDetected == null) {
            // BUG - HAP-java does not currently handle null well here, so we'll default to not detected.
            return CompletableFuture.completedFuture(ContactState.NOT_DETECTED);
        } else if (contactDetected) {
            return CompletableFuture.completedFuture(ContactState.DETECTED);
        } else {
            return CompletableFuture.completedFuture(ContactState.NOT_DETECTED);
        }
    }

    @Override
    public void subscribeContactState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeContactState() {
        getUpdater().unsubscribe(getItem());
    }

    @Override
    public CompletableFuture<Boolean> getLowBatteryState() {
        return CompletableFuture.completedFuture(batteryStatus.isLow());
    }

    @Override
    public void subscribeLowBatteryState(HomekitCharacteristicChangeCallback callback) {
        batteryStatus.subscribe(getUpdater(), callback);
    }

    @Override
    public void unsubscribeLowBatteryState() {
        batteryStatus.unsubscribe(getUpdater());
    }
}
