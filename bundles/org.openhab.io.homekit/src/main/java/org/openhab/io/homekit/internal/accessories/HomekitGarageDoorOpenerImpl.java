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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
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
    private Logger logger = LoggerFactory.getLogger(HomekitGarageDoorOpenerImpl.class);

    public HomekitGarageDoorOpenerImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        this.getServices().add(new GarageDoorOpenerService(this));
    }

    @Override
    public CompletableFuture<CurrentDoorStateEnum> getCurrentDoorState() {
        final Optional<HomekitTaggedItem> characteristic = getCharacteristic(
                HomekitCharacteristicType.CURRENT_DOOR_STATE);
        final HomekitSettings settings = getSettings();
        String stringValue = settings.doorCurrentStateClosed;
        if (characteristic.isPresent()) {
            stringValue = characteristic.get().getItem().getState().toString();
        } else {
            logger.warn("Missing mandatory characteristic {}", HomekitCharacteristicType.CURRENT_DOOR_STATE);
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
            mode = CurrentDoorStateEnum.SOPPED;
        } else if (stringValue.equals("UNDEF") || stringValue.equals("NULL")) {
            logger.warn("Current door state not available. Relaying value of CLOSED to Homekit");
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
        final Optional<HomekitTaggedItem> characteristic = getCharacteristic(
                HomekitCharacteristicType.TARGET_DOOR_STATE);
        Item item;

        if (characteristic.isPresent()) {
            item = characteristic.get().getItem();
        } else {
            logger.warn("Missing mandatory characteristic {}", HomekitCharacteristicType.TARGET_DOOR_STATE);
            return CompletableFuture.completedFuture(TargetDoorStateEnum.CLOSED);
        }
        TargetDoorStateEnum mode;

        if (item instanceof SwitchItem) {
            mode = item.getState() == OnOffType.ON ? TargetDoorStateEnum.OPEN : TargetDoorStateEnum.CLOSED;
        } else if (item instanceof StringItem) {
            final HomekitSettings settings = getSettings();
            final String stringValue = item.getState().toString();
            if (stringValue.equalsIgnoreCase(settings.doorTargetStateClosed)) {
                mode = TargetDoorStateEnum.CLOSED;
            } else if (stringValue.equalsIgnoreCase(settings.doorTargetStateOpen)) {
                mode = TargetDoorStateEnum.OPEN;
                ;
            } else {
                logger.warn(
                        "Unsupported value {} for {}. Only {} and {} supported. Check HomeKit settings if you want to change the mapping",
                        stringValue, item.getName(), settings.doorTargetStateClosed, settings.doorTargetStateOpen);
                mode = TargetDoorStateEnum.CLOSED;
            }
        } else {
            logger.warn("Unsupported item type {} for {}. Only Switch and String are supported", item.getType(),
                    item.getName());
            mode = TargetDoorStateEnum.CLOSED;
        }
        return CompletableFuture.completedFuture(mode);
    }

    @Override
    public CompletableFuture<Boolean> getObstructionDetected() {
        final @Nullable Item item = getItem(HomekitCharacteristicType.OBSTRUCTION_STATUS, GenericItem.class);
        if (item == null) {
            logger.warn("Missing mandatory characteristic {}", HomekitCharacteristicType.OBSTRUCTION_STATUS);
        }
        return CompletableFuture
                .completedFuture(item.getState() == OnOffType.ON || item.getState() == OpenClosedType.OPEN);
    }

    @Override
    public CompletableFuture<Void> setTargetDoorState(final TargetDoorStateEnum targetDoorStateEnum) {
        final Optional<HomekitTaggedItem> characteristic = getCharacteristic(
                HomekitCharacteristicType.TARGET_DOOR_STATE);
        Item item;
        if (characteristic.isPresent()) {
            item = characteristic.get().getItem();
        } else {
            logger.warn("Missing mandatory characteristic {}", HomekitCharacteristicType.TARGET_DOOR_STATE);
            return CompletableFuture.completedFuture(null);
        }

        if (item instanceof SwitchItem) {
            ((SwitchItem) item).send(OnOffType.from(targetDoorStateEnum == TargetDoorStateEnum.OPEN));
        } else if (item instanceof StringItem) {
            final HomekitSettings settings = getSettings();
            ((StringItem) item)
                    .send(new StringType(targetDoorStateEnum == TargetDoorStateEnum.OPEN ? settings.doorTargetStateOpen
                            : settings.doorTargetStateClosed));
        } else {
            logger.warn("Unsupported item type {} for {}. Only Switch and String are supported", item.getType(),
                    item.getName());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeCurrentDoorState(final HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.CURRENT_DOOR_STATE, callback);
    }

    @Override
    public void subscribeTargetDoorState(final HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.TARGET_DOOR_STATE, callback);
    }

    @Override
    public void subscribeObstructionDetected(final HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.OBSTRUCTION_STATUS, callback);
    }

    @Override
    public void unsubscribeCurrentDoorState() {
        unsubscribe(HomekitCharacteristicType.CURRENT_DOOR_STATE);
    }

    @Override
    public void unsubscribeTargetDoorState() {
        unsubscribe(HomekitCharacteristicType.TARGET_DOOR_STATE);
    }

    @Override
    public void unsubscribeObstructionDetected() {
        unsubscribe(HomekitCharacteristicType.OBSTRUCTION_STATUS);
    }
}
