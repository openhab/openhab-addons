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
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.IrrigationSystemAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.common.ActiveEnum;
import io.github.hapjava.characteristics.impl.common.InUseEnum;
import io.github.hapjava.characteristics.impl.common.ProgramModeEnum;
import io.github.hapjava.characteristics.impl.common.ServiceLabelNamespaceCharacteristic;
import io.github.hapjava.characteristics.impl.common.ServiceLabelNamespaceEnum;
import io.github.hapjava.services.impl.IrrigationSystemService;
import io.github.hapjava.services.impl.ServiceLabelService;

/**
 * Implements an Irrigation System accessory.
 * 
 * To be a complete accessory, the user must configure individual valves linked
 * to this primary service. This class also adds the ServiceLabelService
 * automatically.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault({})
public class HomekitIrrigationSystemImpl extends AbstractHomekitAccessoryImpl implements IrrigationSystemAccessory {
    private Map<InUseEnum, String> inUseMapping;
    private Map<ProgramModeEnum, String> programModeMap;
    private static final String SERVICE_LABEL = "ServiceLabel";

    public HomekitIrrigationSystemImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        inUseMapping = createMapping(HomekitCharacteristicType.INUSE_STATUS, InUseEnum.class);
        programModeMap = HomekitCharacteristicFactory
                .createMapping(getCharacteristic(HomekitCharacteristicType.PROGRAM_MODE).get(), ProgramModeEnum.class);
        getServices().add(new IrrigationSystemService(this));
    }

    @Override
    public void init() {
        String serviceLabelNamespaceConfig = getAccessoryConfiguration(SERVICE_LABEL, "ARABIC_NUMERALS");
        ServiceLabelNamespaceEnum serviceLabelEnum;

        try {
            serviceLabelEnum = ServiceLabelNamespaceEnum.valueOf(serviceLabelNamespaceConfig.toUpperCase());
        } catch (IllegalArgumentException e) {
            serviceLabelEnum = ServiceLabelNamespaceEnum.ARABIC_NUMERALS;
        }
        final var finalEnum = serviceLabelEnum;
        var serviceLabelNamespace = getCharacteristic(ServiceLabelNamespaceCharacteristic.class).orElseGet(
                () -> new ServiceLabelNamespaceCharacteristic(() -> CompletableFuture.completedFuture(finalEnum)));
        getServices().add(new ServiceLabelService(serviceLabelNamespace));
    }

    @Override
    public CompletableFuture<ActiveEnum> getActive() {
        OnOffType state = getStateAs(HomekitCharacteristicType.ACTIVE, OnOffType.class);
        return CompletableFuture.completedFuture(state == OnOffType.ON ? ActiveEnum.ACTIVE : ActiveEnum.INACTIVE);
    }

    @Override
    public CompletableFuture<Void> setActive(ActiveEnum value) {
        getCharacteristic(HomekitCharacteristicType.ACTIVE).ifPresent(tItem -> {
            tItem.send(OnOffType.from(value == ActiveEnum.ACTIVE));
        });
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<InUseEnum> getInUse() {
        return CompletableFuture.completedFuture(
                getKeyFromMapping(HomekitCharacteristicType.INUSE_STATUS, inUseMapping, InUseEnum.NOT_IN_USE));
    }

    @Override
    public CompletableFuture<ProgramModeEnum> getProgramMode() {
        return CompletableFuture.completedFuture(getKeyFromMapping(HomekitCharacteristicType.PROGRAM_MODE,
                programModeMap, ProgramModeEnum.NO_SCHEDULED));
    }

    @Override
    public void subscribeActive(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.ACTIVE, callback);
    }

    @Override
    public void unsubscribeActive() {
        unsubscribe(HomekitCharacteristicType.ACTIVE);
    }

    @Override
    public void subscribeInUse(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.INUSE_STATUS, callback);
    }

    @Override
    public void unsubscribeInUse() {
        unsubscribe(HomekitCharacteristicType.INUSE_STATUS);
    }

    @Override
    public void subscribeProgramMode(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.PROGRAM_MODE, callback);
    }

    @Override
    public void unsubscribeProgramMode() {
        unsubscribe(HomekitCharacteristicType.PROGRAM_MODE);
    }
}
