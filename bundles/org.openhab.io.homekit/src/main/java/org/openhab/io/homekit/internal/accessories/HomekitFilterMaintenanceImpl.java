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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.FILTER_CHANGE_INDICATION;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitException;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.FilterMaintenanceAccessory;
import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.filtermaintenance.FilterChangeIndicationEnum;
import io.github.hapjava.services.impl.FilterMaintenanceService;

/**
 * Implements filter maintenance indicator
 *
 * @author Eugen Freiter - Initial contribution
 */
public class HomekitFilterMaintenanceImpl extends AbstractHomekitAccessoryImpl implements FilterMaintenanceAccessory {
    private final Map<FilterChangeIndicationEnum, Object> mapping;

    public HomekitFilterMaintenanceImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            List<Characteristic> mandatoryRawCharacteristics, HomekitAccessoryUpdater updater, HomekitSettings settings)
            throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, mandatoryRawCharacteristics, updater, settings);
        mapping = createMapping(FILTER_CHANGE_INDICATION, FilterChangeIndicationEnum.class);
    }

    @Override
    public void init() throws HomekitException {
        super.init();
        addService(new FilterMaintenanceService(this));
    }

    @Override
    public CompletableFuture<FilterChangeIndicationEnum> getFilterChangeIndication() {
        return CompletableFuture.completedFuture(
                getKeyFromMapping(FILTER_CHANGE_INDICATION, mapping, FilterChangeIndicationEnum.NO_CHANGE_NEEDED));
    }

    @Override
    public void subscribeFilterChangeIndication(final HomekitCharacteristicChangeCallback callback) {
        subscribe(FILTER_CHANGE_INDICATION, callback);
    }

    @Override
    public void unsubscribeFilterChangeIndication() {
        unsubscribe(FILTER_CHANGE_INDICATION);
    }
}
