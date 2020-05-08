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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.accessories.OutletAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.services.impl.OutletService;

/**
 *
 * @author Eugen Freiter - Initial contribution
 */
public class HomekitOutletImpl extends AbstractHomekitAccessoryImpl implements OutletAccessory {
    private final Logger logger = LoggerFactory.getLogger(HomekitOutletImpl.class);

    private final BooleanItemReader inUseReader;
    private final BooleanItemReader onReader;

    public HomekitOutletImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        this.inUseReader = new BooleanItemReader(getItem(HomekitCharacteristicType.INUSE_STATUS, GenericItem.class),
                OnOffType.ON, OpenClosedType.OPEN);
        this.onReader = new BooleanItemReader(getItem(HomekitCharacteristicType.ON_STATE, GenericItem.class),
                OnOffType.ON, OpenClosedType.OPEN);
        getServices().add(new OutletService(this));
    }

    @Override
    public CompletableFuture<Boolean> getPowerState() {
        return CompletableFuture.completedFuture(this.onReader.getValue() != null && this.onReader.getValue());
    }

    @Override
    public CompletableFuture<Boolean> getOutletInUse() {
        return CompletableFuture.completedFuture(this.inUseReader.getValue() != null && this.onReader.getValue());
    }

    @Override
    public CompletableFuture<Void> setPowerState(final boolean state) throws Exception {
        this.onReader.setValue(state);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribePowerState(final HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.ON_STATE, callback);
    }

    @Override
    public void subscribeOutletInUse(final HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.INUSE_STATUS, callback);
    }

    @Override
    public void unsubscribePowerState() {
        unsubscribe(HomekitCharacteristicType.ON_STATE);
    }

    @Override
    public void unsubscribeOutletInUse() {
        unsubscribe(HomekitCharacteristicType.INUSE_STATUS);
    }
}
