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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.FILTER_CHANGE_INDICATION;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.FilterMaintenanceAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.filtermaintenance.FilterChangeIndicationEnum;
import io.github.hapjava.services.impl.FilterMaintenanceService;

/**
 * Implements filter maintenance indicator
 *
 * @author Eugen Freiter - Initial contribution
 */
public class HomekitFilterMaintenanceImpl extends AbstractHomekitAccessoryImpl implements FilterMaintenanceAccessory {
    private BooleanItemReader filterChangeIndication;

    public HomekitFilterMaintenanceImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        filterChangeIndication = createBooleanReader(FILTER_CHANGE_INDICATION);
        getServices().add(new FilterMaintenanceService(this));
    }

    @Override
    public CompletableFuture<FilterChangeIndicationEnum> getFilterChangeIndication() {
        return CompletableFuture
                .completedFuture(filterChangeIndication.getValue() ? FilterChangeIndicationEnum.CHANGE_NEEDED
                        : FilterChangeIndicationEnum.NO_CHANGE_NEEDED);
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
