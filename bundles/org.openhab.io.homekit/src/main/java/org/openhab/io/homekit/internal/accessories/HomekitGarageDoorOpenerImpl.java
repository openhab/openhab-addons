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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitException;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.impl.common.ObstructionDetectedCharacteristic;
import io.github.hapjava.characteristics.impl.garagedoor.CurrentDoorStateCharacteristic;
import io.github.hapjava.characteristics.impl.garagedoor.TargetDoorStateCharacteristic;
import io.github.hapjava.services.impl.GarageDoorOpenerService;

/**
 * Implements Garage Door Opener
 *
 * @author Eugen Freiter - Initial contribution
 */
public class HomekitGarageDoorOpenerImpl extends AbstractHomekitAccessoryImpl {
    private final Logger logger = LoggerFactory.getLogger(HomekitGarageDoorOpenerImpl.class);

    public HomekitGarageDoorOpenerImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            List<Characteristic> mandatoryRawCharacteristics, HomekitAccessoryUpdater updater, HomekitSettings settings)
            throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, mandatoryRawCharacteristics, updater, settings);
    }

    @Override
    public void init() throws HomekitException {
        super.init();

        var obstructionDetectedCharacteristic = getCharacteristic(ObstructionDetectedCharacteristic.class).orElseGet(
                () -> new ObstructionDetectedCharacteristic(() -> CompletableFuture.completedFuture(false), (cb) -> {
                }, () -> {
                }));

        addService(new GarageDoorOpenerService(getCharacteristic(CurrentDoorStateCharacteristic.class).get(),
                getCharacteristic(TargetDoorStateCharacteristic.class).get(), obstructionDetectedCharacteristic));
    }
}
