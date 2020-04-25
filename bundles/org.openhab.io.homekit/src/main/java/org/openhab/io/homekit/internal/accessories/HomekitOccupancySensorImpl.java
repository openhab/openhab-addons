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
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
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
public class HomekitOccupancySensorImpl extends AbstractHomekitAccessoryImpl<GenericItem>
    implements OccupancySensorAccessory {
    private final BooleanItemReader occupancySensedReader;

    public HomekitOccupancySensorImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics,itemRegistry, updater, settings);
        this.occupancySensedReader = new BooleanItemReader(taggedItem.getItem(), OnOffType.ON, OpenClosedType.OPEN);
        getServices().add(new OccupancySensorService(this));
    }

    @Override
    public CompletableFuture<OccupancyDetectedEnum> getOccupancyDetected() {
        return
            (this.occupancySensedReader.getValue()!=null && this.occupancySensedReader.getValue())
            ?CompletableFuture.completedFuture(OccupancyDetectedEnum.DETECTED):CompletableFuture.completedFuture(OccupancyDetectedEnum.NOT_DETECTED);
    }

    @Override
    public void subscribeOccupancyDetected(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeOccupancyDetected() {
        getUpdater().unsubscribe(getItem());
    }

}
