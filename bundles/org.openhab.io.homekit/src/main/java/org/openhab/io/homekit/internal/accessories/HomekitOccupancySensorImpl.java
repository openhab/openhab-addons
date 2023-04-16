/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.OCCUPANCY_DETECTED_STATE;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.OccupancySensorAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.occupancysensor.OccupancyDetectedEnum;
import io.github.hapjava.services.impl.OccupancySensorService;

/**
 *
 * @author Tim Harper - Initial contribution
 */
public class HomekitOccupancySensorImpl extends AbstractHomekitAccessoryImpl implements OccupancySensorAccessory {
    private final Map<OccupancyDetectedEnum, String> mapping;

    public HomekitOccupancySensorImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        mapping = createMapping(OCCUPANCY_DETECTED_STATE, OccupancyDetectedEnum.class);
        getServices().add(new OccupancySensorService(this));
    }

    @Override
    public CompletableFuture<OccupancyDetectedEnum> getOccupancyDetected() {
        return CompletableFuture.completedFuture(
                getKeyFromMapping(OCCUPANCY_DETECTED_STATE, mapping, OccupancyDetectedEnum.NOT_DETECTED));
    }

    @Override
    public void subscribeOccupancyDetected(HomekitCharacteristicChangeCallback callback) {
        subscribe(OCCUPANCY_DETECTED_STATE, callback);
    }

    @Override
    public void unsubscribeOccupancyDetected() {
        unsubscribe(OCCUPANCY_DETECTED_STATE);
    }
}
