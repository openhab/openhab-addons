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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.ACTIVE_STATUS;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.INUSE_STATUS;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.REMAINING_DURATION;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GenericItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.RefreshType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitException;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.accessories.HomekitAccessory;
import io.github.hapjava.accessories.ValveAccessory;
import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.common.ActiveEnum;
import io.github.hapjava.characteristics.impl.common.InUseEnum;
import io.github.hapjava.characteristics.impl.valve.RemainingDurationCharacteristic;
import io.github.hapjava.characteristics.impl.valve.ValveTypeEnum;
import io.github.hapjava.services.impl.ValveService;

/**
 *
 * @author Tim Harper - Initial contribution
 * @author Eugen Freiter - timer implementation
 */
public class HomekitValveImpl extends AbstractHomekitAccessoryImpl implements ValveAccessory {
    private final Logger logger = LoggerFactory.getLogger(HomekitValveImpl.class);
    private static final String CONFIG_VALVE_TYPE = "ValveType";
    private static final String CONFIG_VALVE_TYPE_DEPRECATED = "homekitValveType";
    public static final String CONFIG_DEFAULT_DURATION = "homekitDefaultDuration";
    private static final String CONFIG_TIMER = "homekitTimer";

    private static final Map<String, ValveTypeEnum> CONFIG_VALVE_TYPE_MAPPING = new HashMap<>() {
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
    private final boolean homekitTimer;
    private ValveTypeEnum valveType;

    public HomekitValveImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            List<Characteristic> mandatoryRawCharacteristics, HomekitAccessoryUpdater updater, HomekitSettings settings)
            throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, mandatoryRawCharacteristics, updater, settings);
        inUseReader = createBooleanReader(INUSE_STATUS);
        activeReader = createBooleanReader(ACTIVE_STATUS);
        homekitTimer = getAccessoryConfigurationAsBoolean(CONFIG_TIMER, false);
    }

    @Override
    public void init() throws HomekitException {
        super.init();
        ValveService service = new ValveService(this);
        addService(service);

        var remainingDurationCharacteristic = getCharacteristic(RemainingDurationCharacteristic.class);

        if (homekitTimer && remainingDurationCharacteristic.isEmpty()) {
            addRemainingDurationCharacteristic(getRootAccessory(), getUpdater(), service);
        }
        String valveTypeConfig = getAccessoryConfiguration(CONFIG_VALVE_TYPE, "GENERIC");
        valveTypeConfig = getAccessoryConfiguration(CONFIG_VALVE_TYPE_DEPRECATED, valveTypeConfig);
        var valveType = CONFIG_VALVE_TYPE_MAPPING.get(valveTypeConfig.toUpperCase());
        this.valveType = valveType != null ? valveType : ValveTypeEnum.GENERIC;
    }

    private void addRemainingDurationCharacteristic(HomekitTaggedItem taggedItem, HomekitAccessoryUpdater updater,
            ValveService service) {
        logger.trace("addRemainingDurationCharacteristic for {}", taggedItem);
        service.addOptionalCharacteristic(new RemainingDurationCharacteristic(() -> {
            int remainingTime = 0;
            ScheduledFuture<?> future = valveTimer;
            if (future != null && !future.isDone()) {
                remainingTime = Math.toIntExact(future.getDelay(TimeUnit.SECONDS));
            }
            return CompletableFuture.completedFuture(remainingTime);
        }, HomekitCharacteristicFactory.getSubscriber(taggedItem, REMAINING_DURATION, updater),
                HomekitCharacteristicFactory.getUnsubscriber(taggedItem, REMAINING_DURATION, updater)));
    }

    /**
     * return duration set by home app at corresponding OH items. if ot set, then return the default duration from
     * configuration.
     * 
     * @return duraion
     */
    private int getDuration() {
        int duration = 0;
        final @Nullable DecimalType durationState = getStateAs(HomekitCharacteristicType.DURATION, DecimalType.class);
        if (durationState != null) {
            duration = durationState.intValue();
        }
        return duration;
    }

    private void startTimer() {
        int duration = getDuration();
        logger.trace("start timer for duration {}", duration);
        if (duration > 0) {
            stopTimer();
            valveTimer = timerService.schedule(() -> {
                logger.trace("valve timer is over. switching off the valve");
                switchOffValve();
                // let home app refresh the remaining duration, which is 0
                ((GenericItem) getRootAccessory().getItem()).send(RefreshType.REFRESH);
            }, duration, TimeUnit.SECONDS);
            logger.trace("started valve timer for {} seconds.", duration);
        } else {
            logger.debug("valve timer not started as duration = 0");
        }
    }

    private void stopTimer() {
        ScheduledFuture<?> future = valveTimer;
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
    }

    @Override
    public CompletableFuture<ActiveEnum> getValveActive() {
        return CompletableFuture
                .completedFuture(this.activeReader.getValue() ? ActiveEnum.ACTIVE : ActiveEnum.INACTIVE);
    }

    @Override
    public CompletableFuture<Void> setValveActive(ActiveEnum state) {
        getItem(ACTIVE_STATUS, SwitchItem.class).ifPresent(item -> {
            item.send(OnOffType.from(state == ActiveEnum.ACTIVE));
            if (homekitTimer) {
                if ((state == ActiveEnum.ACTIVE)) {
                    startTimer();
                } else {
                    stopTimer();
                }
                // let home app refresh the remaining duration
                ((GenericItem) getRootAccessory().getItem()).send(RefreshType.REFRESH);
            }
        });
        return CompletableFuture.completedFuture(null);
    }

    private void switchOffValve() {
        getItem(ACTIVE_STATUS, SwitchItem.class).ifPresent(item -> item.send(OnOffType.OFF));
    }

    @Override
    public void subscribeValveActive(HomekitCharacteristicChangeCallback callback) {
        subscribe(ACTIVE_STATUS, callback);
    }

    @Override
    public void unsubscribeValveActive() {
        unsubscribe(ACTIVE_STATUS);
    }

    @Override
    public CompletableFuture<InUseEnum> getValveInUse() {
        return CompletableFuture.completedFuture(inUseReader.getValue() ? InUseEnum.IN_USE : InUseEnum.NOT_IN_USE);
    }

    @Override
    public void subscribeValveInUse(HomekitCharacteristicChangeCallback callback) {
        subscribe(INUSE_STATUS, callback);
    }

    @Override
    public void unsubscribeValveInUse() {
        unsubscribe(INUSE_STATUS);
    }

    @Override
    public CompletableFuture<ValveTypeEnum> getValveType() {
        return CompletableFuture.completedFuture(valveType);
    }

    @Override
    public void subscribeValveType(HomekitCharacteristicChangeCallback callback) {
        // nothing changes here
    }

    @Override
    public void unsubscribeValveType() {
        // nothing changes here
    }

    @Override
    public boolean isLinkable(HomekitAccessory parentAccessory) {
        // When part of an irrigation system, the valve type _must_ be irrigation.
        if (parentAccessory instanceof HomekitIrrigationSystemImpl) {
            valveType = ValveTypeEnum.IRRIGATION;
            return true;
        }
        return false;
    }
}
