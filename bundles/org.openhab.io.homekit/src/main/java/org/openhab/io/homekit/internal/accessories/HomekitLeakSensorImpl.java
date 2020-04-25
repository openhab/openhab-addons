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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.hapjava.accessories.LeakSensorAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.leaksensor.LeakDetectedStateEnum;
import io.github.hapjava.services.impl.LeakSensorService;

/**
 *
 * @author Tim Harper - Initial contribution
 */
public class HomekitLeakSensorImpl extends AbstractHomekitAccessoryImpl<GenericItem>
    implements LeakSensorAccessory {
    protected Logger logger = LoggerFactory.getLogger(HomekitLeakSensorImpl.class);

    private final BooleanItemReader leakDetectedReader;

    public HomekitLeakSensorImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics, ItemRegistry itemRegistry, HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, itemRegistry, updater, settings);
        this.leakDetectedReader = new BooleanItemReader(taggedItem.getItem(), OnOffType.ON, OpenClosedType.OPEN);
        getServices().add(new LeakSensorService(this));
    }

    @Override
    public CompletableFuture<LeakDetectedStateEnum> getLeakDetected() {
        return (this.leakDetectedReader.getValue() !=null && this.leakDetectedReader.getValue()) ? CompletableFuture.completedFuture(LeakDetectedStateEnum.LEAK_DETECTED) : CompletableFuture.completedFuture(LeakDetectedStateEnum.LEAK_NOT_DETECTED);
    }

    @Override
    public void subscribeLeakDetected(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeLeakDetected() {
        getUpdater().unsubscribe(getItem());
    }


}
