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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;

/**
 *
 * @author Tim Harper - Initial contribution
 */
public class NoBatteryStatus implements BatteryStatus {
    @Override
    public @Nullable Boolean isLow() {
        return false;
    }

    @Override
    public void subscribe(@NonNull HomekitAccessoryUpdater updater,
            @NonNull HomekitCharacteristicChangeCallback callback) {
        // do nothing
    }

    @Override
    public void unsubscribe(@NonNull HomekitAccessoryUpdater updater) {
        // do nothing
    }
}
