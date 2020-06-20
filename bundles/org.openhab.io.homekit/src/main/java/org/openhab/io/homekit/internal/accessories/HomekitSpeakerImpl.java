/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.SpeakerAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.services.impl.SpeakerService;

/**
 * Implements Speaker using an Item that provides an On/Off state for Mute.
 *
 * @author Eugen Freiter - Initial contribution
 */
public class HomekitSpeakerImpl extends AbstractHomekitAccessoryImpl implements SpeakerAccessory {
    private final BooleanItemReader muteReader;

    public HomekitSpeakerImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        this.muteReader = new BooleanItemReader(getItem(HomekitCharacteristicType.MUTE, GenericItem.class),
                OnOffType.ON, OpenClosedType.OPEN);
        getServices().add(new SpeakerService(this));
    }

    @Override
    public CompletableFuture<Boolean> isMuted() {
        return CompletableFuture.completedFuture(this.muteReader.getValue() != null && this.muteReader.getValue());
    }

    @Override
    public CompletableFuture<Void> setMute(final boolean state) {
        this.muteReader.setValue(state);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeMuteState(final HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.MUTE, callback);
    }

    @Override
    public void unsubscribeMuteState() {
        unsubscribe(HomekitCharacteristicType.MUTE);
    }
}
