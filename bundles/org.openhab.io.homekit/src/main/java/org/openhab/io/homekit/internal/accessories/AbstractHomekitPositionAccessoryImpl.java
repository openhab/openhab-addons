/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.POSITION_STATE;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.TARGET_POSITION;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.windowcovering.PositionStateEnum;

/**
 * Common methods for Door, Window and WindowCovering.
 * 
 * @author Eugen Freiter - Initial contribution
 */
@NonNullByDefault
abstract class AbstractHomekitPositionAccessoryImpl extends AbstractHomekitAccessoryImpl {
    private final Logger logger = LoggerFactory.getLogger(AbstractHomekitPositionAccessoryImpl.class);
    protected int closedPosition;
    protected int openPosition;
    private final Map<PositionStateEnum, String> positionStateMapping;

    public AbstractHomekitPositionAccessoryImpl(HomekitTaggedItem taggedItem,
            List<HomekitTaggedItem> mandatoryCharacteristics, HomekitAccessoryUpdater updater,
            HomekitSettings settings) {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        final boolean inverted = getAccessoryConfigurationAsBoolean(HomekitTaggedItem.INVERTED, true);
        closedPosition = inverted ? 0 : 100;
        openPosition = inverted ? 100 : 0;
        positionStateMapping = new EnumMap<>(PositionStateEnum.class);
        positionStateMapping.put(PositionStateEnum.DECREASING, "DECREASING");
        positionStateMapping.put(PositionStateEnum.INCREASING, "INCREASING");
        positionStateMapping.put(PositionStateEnum.STOPPED, "STOPPED");
        updateMapping(POSITION_STATE, positionStateMapping);
    }

    public CompletableFuture<Integer> getCurrentPosition() {
        return CompletableFuture.completedFuture(convertPositionState(CURRENT_POSITION, openPosition, closedPosition));
    }

    public CompletableFuture<PositionStateEnum> getPositionState() {
        return CompletableFuture
                .completedFuture(getKeyFromMapping(POSITION_STATE, positionStateMapping, PositionStateEnum.STOPPED));
    }

    public CompletableFuture<Integer> getTargetPosition() {
        return CompletableFuture.completedFuture(convertPositionState(TARGET_POSITION, openPosition, closedPosition));
    }

    public CompletableFuture<Void> setTargetPosition(int value) {
        getCharacteristic(TARGET_POSITION).ifPresentOrElse(taggedItem -> {
            final Item item = taggedItem.getItem();
            final int targetPosition = convertPosition(value, openPosition);

            if (item instanceof RollershutterItem) {
                ((RollershutterItem) item).send(new PercentType(targetPosition));
            } else if (item instanceof DimmerItem) {
                ((DimmerItem) item).send(new PercentType(targetPosition));
            } else if (item instanceof NumberItem) {
                ((NumberItem) item).send(new DecimalType(targetPosition));
            } else if (item instanceof GroupItem && ((GroupItem) item).getBaseItem() instanceof RollershutterItem) {
                ((GroupItem) item).send(new PercentType(targetPosition));
            } else if (item instanceof GroupItem && ((GroupItem) item).getBaseItem() instanceof DimmerItem) {
                ((GroupItem) item).send(new PercentType(targetPosition));
            } else if (item instanceof GroupItem && ((GroupItem) item).getBaseItem() instanceof NumberItem) {
                ((GroupItem) item).send(new DecimalType(targetPosition));
            } else {
                logger.warn(
                        "Unsupported item type for characteristic {} at accessory {}. Expected Rollershutter, Dimmer or Number item, got {}",
                        TARGET_POSITION, getName(), item.getClass());
            }
        }, () -> {
            logger.warn("Mandatory characteristic {} not found at accessory {}. ", TARGET_POSITION, getName());
        });
        return CompletableFuture.completedFuture(null);
    }

    public void subscribeCurrentPosition(HomekitCharacteristicChangeCallback callback) {
        subscribe(CURRENT_POSITION, callback);
    }

    public void subscribePositionState(HomekitCharacteristicChangeCallback callback) {
        subscribe(POSITION_STATE, callback);
    }

    public void subscribeTargetPosition(HomekitCharacteristicChangeCallback callback) {
        subscribe(TARGET_POSITION, callback);
    }

    public void unsubscribeCurrentPosition() {
        unsubscribe(CURRENT_POSITION);
    }

    public void unsubscribePositionState() {
        unsubscribe(POSITION_STATE);
    }

    public void unsubscribeTargetPosition() {
        unsubscribe(TARGET_POSITION);
    }

    /**
     * convert/invert position of door/window/blinds.
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
    protected int convertPosition(int value, int openPosition) {
        return Math.abs(openPosition - value);
    }

    protected int convertPositionState(HomekitCharacteristicType type, int openPosition, int closedPosition) {
        @Nullable
        DecimalType value = null;
        final Optional<HomekitTaggedItem> taggedItem = getCharacteristic(type);
        if (taggedItem.isPresent()) {
            final Item item = taggedItem.get().getItem();
            final Item baseItem = taggedItem.get().getBaseItem();
            if (baseItem instanceof RollershutterItem || baseItem instanceof DimmerItem) {
                value = item.getStateAs(PercentType.class);
            } else {
                value = item.getStateAs(DecimalType.class);
            }
        }
        return value != null ? convertPosition(value.intValue(), openPosition) : closedPosition;
    }
}
