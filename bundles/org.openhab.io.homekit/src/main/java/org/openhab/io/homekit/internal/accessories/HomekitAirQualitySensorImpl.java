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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.AIR_QUALITY;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.AirQualityAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.airquality.AirQualityEnum;
import io.github.hapjava.services.impl.AirQualityService;

/**
 * Air Quality sensor accessory.
 *
 * @author Eugen Freiter - Initial contribution
 */
public class HomekitAirQualitySensorImpl extends AbstractHomekitAccessoryImpl implements AirQualityAccessory {
    private final Map<AirQualityEnum, String> qualityStateMapping;

    public HomekitAirQualitySensorImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        qualityStateMapping = createMapping(AIR_QUALITY, AirQualityEnum.class);
        getServices().add(new AirQualityService(this));
    }

    @Override
    public CompletableFuture<AirQualityEnum> getAirQuality() {
        return CompletableFuture
                .completedFuture(getKeyFromMapping(AIR_QUALITY, qualityStateMapping, AirQualityEnum.UNKNOWN));
    }

    @Override
    public void subscribeAirQuality(final HomekitCharacteristicChangeCallback callback) {
        subscribe(AIR_QUALITY, callback);
    }

    @Override
    public void unsubscribeAirQuality() {
        unsubscribe(AIR_QUALITY);
    }
}
