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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.CURRENT_SLAT_STATE;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitException;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.SlatAccessory;
import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.slat.CurrentSlatStateEnum;
import io.github.hapjava.characteristics.impl.slat.SlatTypeEnum;
import io.github.hapjava.services.impl.SlatService;

/**
 *
 * @author Eugen Freiter - Initial contribution
 */
public class HomekitSlatImpl extends AbstractHomekitAccessoryImpl implements SlatAccessory {
    private static final String CONFIG_TYPE = "type";
    private final Map<CurrentSlatStateEnum, String> currentSlatStateMapping;
    private final SlatTypeEnum slatType;

    public HomekitSlatImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            List<Characteristic> mandatoryRawCharacteristics, HomekitAccessoryUpdater updater,
            HomekitSettings settings) {
        super(taggedItem, mandatoryCharacteristics, mandatoryRawCharacteristics, updater, settings);
        final String slatTypeConfig = getAccessoryConfiguration(CONFIG_TYPE, "horizontal");
        slatType = "horizontal".equalsIgnoreCase(slatTypeConfig) ? SlatTypeEnum.HORIZONTAL : SlatTypeEnum.VERTICAL;
        currentSlatStateMapping = createMapping(CURRENT_SLAT_STATE, CurrentSlatStateEnum.class);
    }

    @Override
    public void init() throws HomekitException {
        super.init();
        addService(new SlatService(this));
    }

    @Override
    public CompletableFuture<CurrentSlatStateEnum> getSlatState() {
        return CompletableFuture.completedFuture(
                getKeyFromMapping(CURRENT_SLAT_STATE, currentSlatStateMapping, CurrentSlatStateEnum.FIXED));
    }

    @Override
    public void subscribeSlatState(final HomekitCharacteristicChangeCallback callback) {
        subscribe(CURRENT_SLAT_STATE, callback);
    }

    @Override
    public void unsubscribeSlatState() {
        unsubscribe(CURRENT_SLAT_STATE);
    }

    @Override
    public CompletableFuture<SlatTypeEnum> getSlatType() {
        return CompletableFuture.completedFuture(slatType);
    }
}
