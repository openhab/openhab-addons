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
package org.openhab.io.homekit.internal.battery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;

/**
 *
 * @author Tim Harper - Initial contribution
 */
@NonNullByDefault
public class BatteryLevelStatus implements BatteryStatus {
    private NumberItem batteryLevelItem;

    BatteryLevelStatus(NumberItem batteryLevelItem) {
        this.batteryLevelItem = batteryLevelItem;
    }

    @Override
    public @Nullable Boolean isLow() {
        DecimalType level = batteryLevelItem.getStateAs(DecimalType.class);

        if (level == null) {
            return null;
        } else {
            return level.intValue() < 10;
        }
    }

    @Override
    public void subscribe(HomekitAccessoryUpdater updater, HomekitCharacteristicChangeCallback callback) {
        updater.subscribe(batteryLevelItem, callback);
    }

    @Override
    public void unsubscribe(HomekitAccessoryUpdater updater) {
        updater.unsubscribe(batteryLevelItem);
    }
}
