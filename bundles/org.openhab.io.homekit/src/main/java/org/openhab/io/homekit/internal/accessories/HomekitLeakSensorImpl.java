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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.openhab.io.homekit.internal.battery.BatteryStatus;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.BatteryStatusAccessory;
import com.beowulfe.hap.accessories.LeakSensor;

/**
 *
 * @author Tim Harper - Initial contribution
 */
public class HomekitLeakSensorImpl extends AbstractHomekitAccessoryImpl<GenericItem>
        implements LeakSensor, BatteryStatusAccessory {

    @NonNull
    private BatteryStatus batteryStatus;

    private BooleanItemReader leakDetectedReader;

    public HomekitLeakSensorImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, BatteryStatus batteryStatus) {
        super(taggedItem, itemRegistry, updater, GenericItem.class);

        this.leakDetectedReader = new BooleanItemReader(taggedItem.getItem(), OnOffType.ON, OpenClosedType.OPEN);
        this.batteryStatus = batteryStatus;
    }

    @Override
    public CompletableFuture<Boolean> getLeakDetected() {
        return CompletableFuture.completedFuture(this.leakDetectedReader.getValue());
    }

    @Override
    public void subscribeLeakDetected(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeLeakDetected() {
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
