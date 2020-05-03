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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.accessories.ValveAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.common.ActiveEnum;
import io.github.hapjava.characteristics.impl.common.InUseEnum;
import io.github.hapjava.characteristics.impl.valve.ValveTypeEnum;
import io.github.hapjava.services.impl.ValveService;

/**
 *
 * @author Tim Harper - Initial contribution
 * @author Eugen Freiter - timer implementation
 */
public class HomekitValveImpl extends AbstractHomekitAccessoryImpl implements ValveAccessory {
    private final Logger logger = LoggerFactory.getLogger(HomekitValveImpl.class);
    private static final String CONFIG_VALVE_TYPE = "homekitValveType";
    private static final String CONFIG_DEFAULT_DURATION = "homekitDefaultDuration";

    private static final Map<String, ValveTypeEnum> CONFIG_VALVE_TYPE_MAPPING = new HashMap<String, ValveTypeEnum>() {
        {
            put("GENERIC", ValveTypeEnum.GENERIC);
            put("IRRIGATION", ValveTypeEnum.IRRIGATION);
            put("SHOWER", ValveTypeEnum.SHOWER);
            put("FAUCET", ValveTypeEnum.WATER_FAUCET);
        }
    };
    private final BooleanItemReader inUseReader;
    private final BooleanItemReader activeReader;
    private final ScheduledExecutorService timerService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> valveTimer;

    public HomekitValveImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        this.inUseReader = new BooleanItemReader(getItem(HomekitCharacteristicType.INUSE_STATUS, GenericItem.class),
                OnOffType.ON, OpenClosedType.OPEN);
        this.activeReader = new BooleanItemReader(getItem(HomekitCharacteristicType.ACTIVE_STATUS, GenericItem.class),
                OnOffType.ON, OpenClosedType.OPEN);
        getServices().add(new ValveService(this));
    }

    @Override
    public CompletableFuture<ActiveEnum> getValveActive() {
        return CompletableFuture.completedFuture(
                (this.activeReader.getValue() != null && this.activeReader.getValue()) ? ActiveEnum.ACTIVE
                        : ActiveEnum.INACTIVE);
    }

    @Override
    public CompletableFuture<Void> setValveActive(ActiveEnum state) {
        SwitchItem item = getItem(HomekitCharacteristicType.ACTIVE_STATUS, SwitchItem.class);
        if (item != null) {
            item.send(OnOffType.from(state == ActiveEnum.ACTIVE));
            final @Nullable NumberItem durationItem = getItem(HomekitCharacteristicType.DURATION, NumberItem.class);
            final @Nullable NumberItem remainingDurationItem = getItem(HomekitCharacteristicType.REMAINING_DURATION,
                    NumberItem.class);
            if (state == ActiveEnum.ACTIVE) {
                final @Nullable DecimalType durationState = getStateAs(HomekitCharacteristicType.DURATION,
                        DecimalType.class);
                if (durationState != null) {
                    int duration = durationState.intValue() != 0 ? durationState.intValue()
                            : getAccessoryConfiguration(CONFIG_DEFAULT_DURATION, 0);
                    if (duration > 0) {
                        if (durationItem != null) {
                            durationItem.send(new DecimalType(duration));
                        }
                        if (remainingDurationItem != null) {
                            remainingDurationItem.send(new DecimalType(duration));
                        }
                        ScheduledFuture<?> future = valveTimer;
                        if (future != null && !future.isDone()) {
                            future.cancel(true);
                        }
                        valveTimer = timerService.schedule(() -> {
                            logger.trace("valve timer is over. switching off the valve");
                            switchOffValve();
                        }, duration, TimeUnit.SECONDS);
                        logger.trace("started valve timer for {} seconds.", duration);
                    }
                }
            } else {
                ScheduledFuture<?> future = valveTimer;
                if (future != null && !future.isDone()) {
                    future.cancel(true);
                }
                if (remainingDurationItem != null) {
                    remainingDurationItem.send(new DecimalType(0));
                }
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    private void switchOffValve() {
        SwitchItem item = getItem(HomekitCharacteristicType.ACTIVE_STATUS, SwitchItem.class);
        if (item != null) {
            item.send(OnOffType.OFF);
        }
        final @Nullable NumberItem remainingDurationItem = getItem(HomekitCharacteristicType.REMAINING_DURATION,
                NumberItem.class);
        if (remainingDurationItem != null) {
            remainingDurationItem.send(new DecimalType(0));
        }
    }

    @Override
    public void subscribeValveActive(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.ACTIVE_STATUS, callback);
    }

    @Override
    public void unsubscribeValveActive() {
        unsubscribe(HomekitCharacteristicType.ACTIVE_STATUS);
    }

    @Override
    public CompletableFuture<InUseEnum> getValveInUse() {
        return CompletableFuture
                .completedFuture((this.inUseReader.getValue() != null && this.inUseReader.getValue()) ? InUseEnum.IN_USE
                        : InUseEnum.NOT_IN_USE);
    }

    @Override
    public void subscribeValveInUse(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.INUSE_STATUS, callback);
    }

    @Override
    public void unsubscribeValveInUse() {
        unsubscribe(HomekitCharacteristicType.INUSE_STATUS);
    }

    @Override
    public CompletableFuture<ValveTypeEnum> getValveType() {
        final String valveType = getAccessoryConfiguration(CONFIG_VALVE_TYPE, "GENERIC");
        ValveTypeEnum type = CONFIG_VALVE_TYPE_MAPPING.get(valveType.toUpperCase());
        return CompletableFuture.completedFuture(type != null ? type : ValveTypeEnum.GENERIC);
    }

    @Override
    public void subscribeValveType(HomekitCharacteristicChangeCallback callback) {
        // nothing changes here
    }

    @Override
    public void unsubscribeValveType() {
        // nothing changes here
    }
}
