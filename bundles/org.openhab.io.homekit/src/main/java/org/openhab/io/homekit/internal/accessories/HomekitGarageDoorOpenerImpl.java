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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.CURRENT_DOOR_STATE;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.OBSTRUCTION_STATUS;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.TARGET_DOOR_STATE;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.openhab.core.items.Item;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitOHItemProxy;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.accessories.GarageDoorOpenerAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.garagedoor.CurrentDoorStateEnum;
import io.github.hapjava.characteristics.impl.garagedoor.TargetDoorStateEnum;
import io.github.hapjava.services.impl.GarageDoorOpenerService;

/**
 * Implements Garage Door Opener
 *
 * @author Eugen Freiter - Initial contribution
 */
public class HomekitGarageDoorOpenerImpl extends AbstractHomekitAccessoryImpl implements GarageDoorOpenerAccessory {
    private final Logger logger = LoggerFactory.getLogger(HomekitGarageDoorOpenerImpl.class);
    private final BooleanItemReader obstructionReader;

    public HomekitGarageDoorOpenerImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        obstructionReader = createBooleanReader(OBSTRUCTION_STATUS);
        getServices().add(new GarageDoorOpenerService(this));
    }

    @Override
    public CompletableFuture<CurrentDoorStateEnum> getCurrentDoorState() {
        final Optional<HomekitTaggedItem> characteristic = getCharacteristic(CURRENT_DOOR_STATE);
        final HomekitSettings settings = getSettings();
        String stringValue = settings.doorCurrentStateClosed;
        if (characteristic.isPresent()) {
            stringValue = characteristic.get().getItem().getState().toString();
        } else {
            logger.warn("Missing mandatory characteristic {}", CURRENT_DOOR_STATE);
        }
        CurrentDoorStateEnum mode;

        if (stringValue.equalsIgnoreCase(settings.doorCurrentStateClosed)) {
            mode = CurrentDoorStateEnum.CLOSED;
        } else if (stringValue.equalsIgnoreCase(settings.doorCurrentStateClosing)) {
            mode = CurrentDoorStateEnum.CLOSING;
        } else if (stringValue.equalsIgnoreCase(settings.doorCurrentStateOpen)) {
            mode = CurrentDoorStateEnum.OPEN;
        } else if (stringValue.equalsIgnoreCase(settings.doorCurrentStateOpening)) {
            mode = CurrentDoorStateEnum.OPENING;
        } else if (stringValue.equalsIgnoreCase(settings.doorCurrentStateStopped)) {
            mode = CurrentDoorStateEnum.STOPPED;
        } else if (stringValue.equals("UNDEF") || stringValue.equals("NULL")) {
            logger.warn("Current door state not available. Relaying value of CLOSED to HomeKit");
            mode = CurrentDoorStateEnum.CLOSED;
        } else {
            logger.warn("Unrecognized current door state: {}. Expected {}, {}, {}, {} or {} strings in value.",
                    stringValue, settings.doorCurrentStateClosed, settings.doorCurrentStateClosing,
                    settings.doorCurrentStateOpen, settings.doorCurrentStateOpening, settings.doorCurrentStateStopped);
            mode = CurrentDoorStateEnum.CLOSED;
        }
        return CompletableFuture.completedFuture(mode);
    }

    @Override
    public CompletableFuture<TargetDoorStateEnum> getTargetDoorState() {
        final Optional<HomekitTaggedItem> characteristic = getCharacteristic(TARGET_DOOR_STATE);
        Item item;

        if (characteristic.isPresent()) {
            item = characteristic.get().getItem();
        } else {
            logger.warn("Missing mandatory characteristic {}", TARGET_DOOR_STATE);
            return CompletableFuture.completedFuture(TargetDoorStateEnum.CLOSED);
        }
        TargetDoorStateEnum mode;

        final Item baseItem = HomekitOHItemProxy.getBaseItem(item);
        if (baseItem instanceof SwitchItem) {
            mode = item.getState() == OnOffType.ON ? TargetDoorStateEnum.OPEN : TargetDoorStateEnum.CLOSED;
        } else if (baseItem instanceof StringItem) {
            final HomekitSettings settings = getSettings();
            final String stringValue = item.getState().toString();
            if (stringValue.equalsIgnoreCase(settings.doorTargetStateClosed)) {
                mode = TargetDoorStateEnum.CLOSED;
            } else if (stringValue.equalsIgnoreCase(settings.doorTargetStateOpen)) {
                mode = TargetDoorStateEnum.OPEN;
            } else {
                logger.warn(
                        "Unsupported value {} for {}. Only {} and {} supported. Check HomeKit settings if you want to change the mapping",
                        stringValue, item.getName(), settings.doorTargetStateClosed, settings.doorTargetStateOpen);
                mode = TargetDoorStateEnum.CLOSED;
            }
        } else {
            logger.warn("Unsupported item type {} for {}. Only Switch and String are supported", baseItem.getType(),
                    item.getName());
            mode = TargetDoorStateEnum.CLOSED;
        }
        return CompletableFuture.completedFuture(mode);
    }

    @Override
    public CompletableFuture<Boolean> getObstructionDetected() {
        return CompletableFuture.completedFuture(obstructionReader.getValue());
    }

    @Override
    public CompletableFuture<Void> setTargetDoorState(TargetDoorStateEnum targetDoorStateEnum) {
        final Optional<HomekitTaggedItem> characteristic = getCharacteristic(TARGET_DOOR_STATE);
        final HomekitTaggedItem taggedItem;
        if (characteristic.isPresent()) {
            taggedItem = characteristic.get();
        } else {
            logger.warn("Missing mandatory characteristic {}", TARGET_DOOR_STATE);
            return CompletableFuture.completedFuture(null);
        }

        if (taggedItem.getBaseItem() instanceof SwitchItem) {
            taggedItem.send(OnOffType.from(targetDoorStateEnum == TargetDoorStateEnum.OPEN));
        } else if (taggedItem.getBaseItem() instanceof StringItem) {
            final HomekitSettings settings = getSettings();
            taggedItem
                    .send(new StringType(targetDoorStateEnum == TargetDoorStateEnum.OPEN ? settings.doorTargetStateOpen
                            : settings.doorTargetStateClosed));
        } else {
            logger.warn("Unsupported item type {} for {}. Only Switch and String are supported",
                    taggedItem.getBaseItem().getType(), taggedItem.getName());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeCurrentDoorState(HomekitCharacteristicChangeCallback callback) {
        subscribe(CURRENT_DOOR_STATE, callback);
    }

    @Override
    public void subscribeTargetDoorState(HomekitCharacteristicChangeCallback callback) {
        subscribe(TARGET_DOOR_STATE, callback);
    }

    @Override
    public void subscribeObstructionDetected(HomekitCharacteristicChangeCallback callback) {
        subscribe(OBSTRUCTION_STATUS, callback);
    }

    @Override
    public void unsubscribeCurrentDoorState() {
        unsubscribe(CURRENT_DOOR_STATE);
    }

    @Override
    public void unsubscribeTargetDoorState() {
        unsubscribe(TARGET_DOOR_STATE);
    }

    @Override
    public void unsubscribeObstructionDetected() {
        unsubscribe(OBSTRUCTION_STATUS);
    }
}
