/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.HumiditySensorAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.services.impl.HumiditySensorService;

/**
 *
 * @author Andy Lintner - Initial contribution
 */
public class HomekitHumiditySensorImpl extends AbstractHomekitAccessoryImpl implements HumiditySensorAccessory {
    private final static String CONFIG_MULTIPLICATOR = "homekitMultiplicator";

    public HomekitHumiditySensorImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        getServices().add(new HumiditySensorService(this));
    }

    @Override
    public CompletableFuture<Double> getCurrentRelativeHumidity() {
        final @Nullable DecimalType state = getStateAs(HomekitCharacteristicType.RELATIVE_HUMIDITY, DecimalType.class);
        if (state != null) {
            BigDecimal multiplicator = getAccessoryConfiguration(CONFIG_MULTIPLICATOR, BigDecimal.valueOf(1.0));
            return CompletableFuture.completedFuture((state.toBigDecimal().multiply(multiplicator)).doubleValue());
        }
        return CompletableFuture.completedFuture(0.0);
    }

    @Override
    public void subscribeCurrentRelativeHumidity(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.RELATIVE_HUMIDITY, callback);
    }

    @Override
    public void unsubscribeCurrentRelativeHumidity() {
        unsubscribe(HomekitCharacteristicType.RELATIVE_HUMIDITY);
    }
}
