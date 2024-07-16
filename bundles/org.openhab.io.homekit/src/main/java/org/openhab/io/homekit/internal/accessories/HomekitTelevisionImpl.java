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

import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.impl.common.ActiveCharacteristic;
import io.github.hapjava.characteristics.impl.common.ActiveIdentifierCharacteristic;
import io.github.hapjava.characteristics.impl.common.ConfiguredNameCharacteristic;
import io.github.hapjava.characteristics.impl.television.RemoteKeyCharacteristic;
import io.github.hapjava.characteristics.impl.television.SleepDiscoveryModeCharacteristic;
import io.github.hapjava.characteristics.impl.television.SleepDiscoveryModeEnum;
import io.github.hapjava.services.impl.TelevisionService;

/**
 * Implements Television
 * 
 * This is a little different in that we don't implement the accessory interface.
 * This is because several of the "mandatory" characteristics we don't require,
 * and wait until all optional attributes are added and if they don't exist
 * it will create "default" values for them.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault({})
public class HomekitTelevisionImpl extends AbstractHomekitAccessoryImpl {

    public HomekitTelevisionImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            List<Characteristic> mandatoryRawCharacteristics, HomekitAccessoryUpdater updater, HomekitSettings settings)
            throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, mandatoryRawCharacteristics, updater, settings);
    }

    @Override
    public void init() throws HomekitException {
        super.init();

        // these charactereristics are technically mandatory, but we provide defaults if they're not provided
        var activeIdentifierCharacteristic = getCharacteristic(ActiveIdentifierCharacteristic.class)
                .orElseGet(() -> new ActiveIdentifierCharacteristic(() -> CompletableFuture.completedFuture(1), v -> {
                }, v -> {
                }, () -> {
                }));
        var configuredNameCharacteristic = getCharacteristic(ConfiguredNameCharacteristic.class)
                .orElseGet(() -> new ConfiguredNameCharacteristic(() -> getName(), v -> {
                }, v -> {
                }, () -> {
                }));
        var remoteKeyCharacteristic = getCharacteristic(RemoteKeyCharacteristic.class)
                .orElseGet(() -> new RemoteKeyCharacteristic((v) -> {
                }));
        var sleepDiscoveryModeCharacteristic = getCharacteristic(SleepDiscoveryModeCharacteristic.class)
                .orElseGet(() -> new SleepDiscoveryModeCharacteristic(
                        () -> CompletableFuture.completedFuture(SleepDiscoveryModeEnum.ALWAYS_DISCOVERABLE), v -> {
                        }, () -> {
                        }));

        var service = new TelevisionService(getCharacteristic(ActiveCharacteristic.class).get(),
                activeIdentifierCharacteristic, configuredNameCharacteristic, remoteKeyCharacteristic,
                sleepDiscoveryModeCharacteristic);

        addService(service);
    }
}
