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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitException;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.HomekitAccessory;
import io.github.hapjava.characteristics.impl.common.ConfiguredNameCharacteristic;
import io.github.hapjava.characteristics.impl.common.IdentifierCharacteristic;
import io.github.hapjava.characteristics.impl.common.IsConfiguredCharacteristic;
import io.github.hapjava.characteristics.impl.common.IsConfiguredEnum;
import io.github.hapjava.characteristics.impl.common.NameCharacteristic;
import io.github.hapjava.characteristics.impl.inputsource.CurrentVisibilityStateCharacteristic;
import io.github.hapjava.characteristics.impl.inputsource.CurrentVisibilityStateEnum;
import io.github.hapjava.characteristics.impl.inputsource.InputDeviceTypeCharacteristic;
import io.github.hapjava.characteristics.impl.inputsource.InputSourceTypeCharacteristic;
import io.github.hapjava.characteristics.impl.inputsource.InputSourceTypeEnum;
import io.github.hapjava.characteristics.impl.inputsource.TargetVisibilityStateCharacteristic;
import io.github.hapjava.services.impl.InputSourceService;

/**
 * Implements Input Source
 * 
 * This is a little different in that we don't implement the accessory interface.
 * This is because several of the "mandatory" characteristics we don't require,
 * and wait until all optional attributes are added and if they don't exist
 * it will create "default" values for them.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault({})
public class HomekitInputSourceImpl extends AbstractHomekitAccessoryImpl {

    public HomekitInputSourceImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
    }

    @Override
    public void init() throws HomekitException {
        super.init();

        // these charactereristics are technically mandatory, but we provide defaults if they're not provided
        var configuredNameCharacteristic = getCharacteristic(ConfiguredNameCharacteristic.class)
                .orElseGet(() -> new ConfiguredNameCharacteristic(() -> getName(), v -> {
                }, v -> {
                }, () -> {
                }));
        var inputSourceTypeCharacteristic = getCharacteristic(InputSourceTypeCharacteristic.class)
                .orElseGet(() -> new InputSourceTypeCharacteristic(
                        () -> CompletableFuture.completedFuture(InputSourceTypeEnum.OTHER), v -> {
                        }, () -> {
                        }));
        var isConfiguredCharacteristic = getCharacteristic(IsConfiguredCharacteristic.class)
                .orElseGet(() -> new IsConfiguredCharacteristic(
                        () -> CompletableFuture.completedFuture(IsConfiguredEnum.CONFIGURED), v -> {
                        }, v -> {
                        }, () -> {
                        }));
        var currentVisibilityStateCharacteristic = getCharacteristic(CurrentVisibilityStateCharacteristic.class)
                .orElseGet(() -> new CurrentVisibilityStateCharacteristic(
                        () -> CompletableFuture.completedFuture(CurrentVisibilityStateEnum.SHOWN), v -> {
                        }, () -> {
                        }));
        var identifierCharacteristic = getCharacteristic(IdentifierCharacteristic.class)
                .orElseGet(() -> new IdentifierCharacteristic(() -> CompletableFuture.completedFuture(1)));

        var service = new InputSourceService(configuredNameCharacteristic, inputSourceTypeCharacteristic,
                isConfiguredCharacteristic, currentVisibilityStateCharacteristic);

        getCharacteristic(NameCharacteristic.class).ifPresent(c -> service.addOptionalCharacteristic(c));
        service.addOptionalCharacteristic(identifierCharacteristic);
        getCharacteristic(InputDeviceTypeCharacteristic.class).ifPresent(c -> service.addOptionalCharacteristic(c));
        getCharacteristic(TargetVisibilityStateCharacteristic.class)
                .ifPresent(c -> service.addOptionalCharacteristic(c));

        getServices().add(service);
    }

    @Override
    public boolean isLinkable(HomekitAccessory parentAccessory) {
        return parentAccessory instanceof HomekitTelevisionImpl;
    }
}
