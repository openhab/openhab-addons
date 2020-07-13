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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.CURRENT_POSITION;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.TARGET_POSITION;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.WindowCoveringAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.windowcovering.PositionStateEnum;
import io.github.hapjava.services.impl.WindowCoveringService;

/**
 *
 * @author epike - Initial contribution
 */
public class HomekitWindowCoveringImpl extends AbstractHomekitAccessoryImpl implements WindowCoveringAccessory {
    private final int closedPosition;
    private final int openPosition;

    public HomekitWindowCoveringImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        final String invertedConfig = getAccessoryConfiguration(HomekitTaggedItem.INVERTED, "true");
        final boolean inverted = invertedConfig.equalsIgnoreCase("yes") || invertedConfig.equalsIgnoreCase("true");
        closedPosition = inverted ? 0 : 100;
        openPosition = inverted ? 100 : 0;
        this.getServices().add(new WindowCoveringService(this));
    }

    @Override
    public CompletableFuture<Integer> getCurrentPosition() {
        return CompletableFuture.completedFuture(convertPositionState(CURRENT_POSITION));
    }

    @Override
    public CompletableFuture<PositionStateEnum> getPositionState() {
        return CompletableFuture.completedFuture(PositionStateEnum.STOPPED);
    }

    @Override
    public CompletableFuture<Integer> getTargetPosition() {
        return CompletableFuture.completedFuture(convertPositionState(TARGET_POSITION));
    }

    @Override
    public CompletableFuture<Void> setTargetPosition(int value) {
        getItem(TARGET_POSITION, RollershutterItem.class)
                .ifPresent(item -> item.send(new PercentType(convertPosition(value))));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeCurrentPosition(HomekitCharacteristicChangeCallback callback) {
        subscribe(CURRENT_POSITION, callback);
    }

    @Override
    public void subscribePositionState(HomekitCharacteristicChangeCallback callback) {
        // Not implemented
    }

    @Override
    public void subscribeTargetPosition(HomekitCharacteristicChangeCallback callback) {
        subscribe(TARGET_POSITION, callback);
    }

    @Override
    public void unsubscribeCurrentPosition() {
        unsubscribe(CURRENT_POSITION);
    }

    @Override
    public void unsubscribePositionState() {
        // Not implemented
    }

    @Override
    public void unsubscribeTargetPosition() {
        unsubscribe(TARGET_POSITION);
    }

    /**
     * openHAB Rollershutter is:
     * - completely open if position is 0%,
     * - completely closed if position is 100%.
     * HomeKit mapping has inverted mapping
     * From Specification: "For blinds/shades/awnings, a value of 0 indicates a position that permits the least light
     * and a value
     * of 100 indicates a position that allows most light.", i.e.
     * HomeKit Blinds is
     * - completely open if position is 100%,
     * - completely closed if position is 0%.
     *
     * As openHAB rollershutter item is typically used for window covering, the binding has by default inverting
     * mapping.
     * One can override this default behaviour with inverted="false/no" flag. in this cases, openHAB item value will be
     * sent to HomeKit with no changes.
     * 
     * @param value source value
     * @return target value
     */
    private int convertPosition(int value) {
        return Math.abs(openPosition - value);
    }

    private int convertPositionState(HomekitCharacteristicType type) {
        final @Nullable DecimalType value = getStateAs(type, PercentType.class);
        return value != null ? convertPosition(value.intValue()) : closedPosition;
    }
}
