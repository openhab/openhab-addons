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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.CARBON_DIOXIDE_DETECTED_STATE;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitException;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.CarbonDioxideSensorAccessory;
import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.carbondioxidesensor.CarbonDioxideDetectedEnum;
import io.github.hapjava.services.impl.CarbonDioxideSensorService;

/**
 *
 * @author Cody Cutrer - Initial contribution
 */
public class HomekitCarbonDioxideSensorImpl extends AbstractHomekitAccessoryImpl
        implements CarbonDioxideSensorAccessory {
    private final Map<CarbonDioxideDetectedEnum, Object> mapping;

    public HomekitCarbonDioxideSensorImpl(HomekitTaggedItem taggedItem,
            List<HomekitTaggedItem> mandatoryCharacteristics, List<Characteristic> mandatoryRawCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, mandatoryRawCharacteristics, updater, settings);
        mapping = createMapping(CARBON_DIOXIDE_DETECTED_STATE, CarbonDioxideDetectedEnum.class);
    }

    @Override
    public void init() throws HomekitException {
        super.init();
        addService(new CarbonDioxideSensorService(this));
    }

    @Override
    public CompletableFuture<CarbonDioxideDetectedEnum> getCarbonDioxideDetectedState() {
        return CompletableFuture.completedFuture(
                getKeyFromMapping(CARBON_DIOXIDE_DETECTED_STATE, mapping, CarbonDioxideDetectedEnum.NORMAL));
    }

    @Override
    public void subscribeCarbonDioxideDetectedState(HomekitCharacteristicChangeCallback callback) {
        subscribe(CARBON_DIOXIDE_DETECTED_STATE, callback);
    }

    @Override
    public void unsubscribeCarbonDioxideDetectedState() {
        unsubscribe(CARBON_DIOXIDE_DETECTED_STATE);
    }
}
