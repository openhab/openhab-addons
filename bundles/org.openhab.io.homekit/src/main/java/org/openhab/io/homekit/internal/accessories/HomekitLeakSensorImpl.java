/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.LEAK_DETECTED_STATE;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitException;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.LeakSensorAccessory;
import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.leaksensor.LeakDetectedStateEnum;
import io.github.hapjava.services.impl.LeakSensorService;

/**
 *
 * @author Tim Harper - Initial contribution
 */
public class HomekitLeakSensorImpl extends AbstractHomekitAccessoryImpl implements LeakSensorAccessory {
    private final Map<LeakDetectedStateEnum, String> mapping;

    public HomekitLeakSensorImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            List<Characteristic> mandatoryRawCharacteristics, HomekitAccessoryUpdater updater, HomekitSettings settings)
            throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, mandatoryRawCharacteristics, updater, settings);
        mapping = createMapping(LEAK_DETECTED_STATE, LeakDetectedStateEnum.class);
    }

    @Override
    public void init() throws HomekitException {
        super.init();
        addService(new LeakSensorService(this));
    }

    @Override
    public CompletableFuture<LeakDetectedStateEnum> getLeakDetected() {
        return CompletableFuture.completedFuture(
                getKeyFromMapping(LEAK_DETECTED_STATE, mapping, LeakDetectedStateEnum.LEAK_NOT_DETECTED));
    }

    @Override
    public void subscribeLeakDetected(HomekitCharacteristicChangeCallback callback) {
        subscribe(LEAK_DETECTED_STATE, callback);
    }

    @Override
    public void unsubscribeLeakDetected() {
        unsubscribe(LEAK_DETECTED_STATE);
    }
}
