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
import com.beowulfe.hap.accessories.SmokeSensor;
import com.beowulfe.hap.accessories.properties.SmokeDetectedState;

/**
 *
 * @author Cody Cutrer - Initial contribution
 */
public class HomekitSmokeSensorImpl extends AbstractHomekitAccessoryImpl<GenericItem>
        implements SmokeSensor, BatteryStatusAccessory {

    @NonNull
    private BatteryStatus batteryStatus;

    private BooleanItemReader smokeDetectedReader;

    public HomekitSmokeSensorImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, BatteryStatus batteryStatus) {
        super(taggedItem, itemRegistry, updater, GenericItem.class);

        this.smokeDetectedReader = new BooleanItemReader(taggedItem.getItem(), OnOffType.ON, OpenClosedType.OPEN);
        this.batteryStatus = batteryStatus;
    }

    @Override
    public CompletableFuture<SmokeDetectedState> getSmokeDetectedState() {
        Boolean state = this.smokeDetectedReader.getValue();
        if (state == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(state ? SmokeDetectedState.DETECTED : SmokeDetectedState.NOT_DETECTED);
    }

    @Override
    public void subscribeSmokeDetectedState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeSmokeDetectedState() {
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
