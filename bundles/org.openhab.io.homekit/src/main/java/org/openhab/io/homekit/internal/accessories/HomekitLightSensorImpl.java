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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.LIGHT_LEVEL;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitException;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.LightSensorAccessory;
import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.lightsensor.CurrentAmbientLightLevelCharacteristic;
import io.github.hapjava.services.impl.LightSensorService;

/**
 * HomeKit light sensor implementation.
 *
 * @author Eugen Freiter - Initial contribution
 */
public class HomekitLightSensorImpl extends AbstractHomekitAccessoryImpl implements LightSensorAccessory {

    public HomekitLightSensorImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            List<Characteristic> mandatoryRawCharacteristics, HomekitAccessoryUpdater updater,
            HomekitSettings settings) {
        super(taggedItem, mandatoryCharacteristics, mandatoryRawCharacteristics, updater, settings);
    }

    @Override
    public void init() throws HomekitException {
        super.init();
        addService(new LightSensorService(this));
    }

    @Override
    public CompletableFuture<Double> getCurrentAmbientLightLevel() {
        final @Nullable DecimalType state = getStateAs(LIGHT_LEVEL, DecimalType.class);
        return CompletableFuture
                .completedFuture(state != null ? state.doubleValue() : getMinCurrentAmbientLightLevel());
    }

    @Override
    public double getMinCurrentAmbientLightLevel() {
        return getAccessoryConfiguration(HomekitCharacteristicType.LIGHT_LEVEL, HomekitTaggedItem.MIN_VALUE,
                BigDecimal.valueOf(CurrentAmbientLightLevelCharacteristic.DEFAULT_MIN_VALUE)).doubleValue();
    }

    @Override
    public double getMaxCurrentAmbientLightLevel() {
        return getAccessoryConfiguration(HomekitCharacteristicType.LIGHT_LEVEL, HomekitTaggedItem.MAX_VALUE,
                BigDecimal.valueOf(CurrentAmbientLightLevelCharacteristic.DEFAULT_MAX_VALUE)).doubleValue();
    }

    @Override
    public void subscribeCurrentAmbientLightLevel(HomekitCharacteristicChangeCallback callback) {
        subscribe(LIGHT_LEVEL, callback);
    }

    @Override
    public void unsubscribeCurrentAmbientLightLevel() {
        unsubscribe(LIGHT_LEVEL);
    }
}
