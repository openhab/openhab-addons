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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.ON_STATE;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.SwitchAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.services.impl.SwitchService;

/**
 * Implements Switch using an Item that provides an On/Off state.
 *
 * @author Andy Lintner - Initial contribution
 */
public class HomekitSwitchImpl extends AbstractHomekitAccessoryImpl implements SwitchAccessory {
    private final BooleanItemReader onReader;

    public HomekitSwitchImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        onReader = createBooleanReader(ON_STATE);
        getServices().add(new SwitchService(this));
    }

    @Override
    public CompletableFuture<Boolean> getSwitchState() {
        return CompletableFuture.completedFuture(onReader.getValue());
    }

    @Override
    public CompletableFuture<Void> setSwitchState(boolean state) {
        onReader.setValue(state);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeSwitchState(HomekitCharacteristicChangeCallback callback) {
        subscribe(ON_STATE, callback);
    }

    @Override
    public void unsubscribeSwitchState() {
        unsubscribe(ON_STATE);
    }
}
