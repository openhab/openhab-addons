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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitException;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.characteristics.impl.audio.MuteCharacteristic;
import io.github.hapjava.characteristics.impl.audio.VolumeCharacteristic;
import io.github.hapjava.characteristics.impl.common.ActiveCharacteristic;
import io.github.hapjava.characteristics.impl.televisionspeaker.VolumeControlTypeCharacteristic;
import io.github.hapjava.characteristics.impl.televisionspeaker.VolumeControlTypeEnum;
import io.github.hapjava.characteristics.impl.televisionspeaker.VolumeSelectorCharacteristic;
import io.github.hapjava.services.impl.TelevisionSpeakerService;

/**
 * Implements Television Speaker
 * 
 * This is a little different in that we don't implement the accessory interface.
 * This is because several of the "mandatory" characteristics we don't require,
 * and wait until all optional attributes are added and if they don't exist
 * it will create "default" values for them.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault({})
public class HomekitTelevisionSpeakerImpl extends AbstractHomekitAccessoryImpl {

    public HomekitTelevisionSpeakerImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
    }

    @Override
    public void init() throws HomekitException {
        super.init();

        var muteCharacteristic = (MuteCharacteristic) HomekitCharacteristicFactory
                .createCharacteristic(getCharacteristic(HomekitCharacteristicType.MUTE).get(), getUpdater());
        // this characteristic is technically optional, but we provide a default implementation if it's not provided
        var volumeControlTypeCharacteristic = getCharacteristic(VolumeControlTypeCharacteristic.class);
        // optional characteristics
        var volumeCharacteristic = getCharacteristic(VolumeCharacteristic.class);
        var volumeSelectorCharacteristic = getCharacteristic(VolumeSelectorCharacteristic.class);

        if (!volumeControlTypeCharacteristic.isPresent()) {
            VolumeControlTypeEnum type;
            if (volumeCharacteristic.isPresent()) {
                type = VolumeControlTypeEnum.ABSOLUTE;
            } else if (volumeSelectorCharacteristic.isPresent()) {
                type = VolumeControlTypeEnum.RELATIVE;
            } else {
                type = VolumeControlTypeEnum.NONE;
            }
            volumeControlTypeCharacteristic = Optional
                    .of(new VolumeControlTypeCharacteristic(() -> CompletableFuture.completedFuture(type), v -> {
                    }, () -> {
                    }));
        }

        var service = new TelevisionSpeakerService(muteCharacteristic);

        getCharacteristic(ActiveCharacteristic.class).ifPresent(c -> service.addOptionalCharacteristic(c));
        volumeCharacteristic.ifPresent(c -> service.addOptionalCharacteristic(c));
        service.addOptionalCharacteristic(volumeControlTypeCharacteristic.get());

        getServices().add(service);
    }
}
