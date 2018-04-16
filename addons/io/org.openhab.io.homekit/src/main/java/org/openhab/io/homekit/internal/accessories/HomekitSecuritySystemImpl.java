/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.SecuritySystem;
import com.beowulfe.hap.accessories.properties.CurrentSecuritySystemState;
import com.beowulfe.hap.accessories.properties.SecuritySystemAlarmType;
import com.beowulfe.hap.accessories.properties.TargetSecuritySystemState;

/**
 * Implements a Security System as a GroupedAccessory made up of multiple items:
 * <ul>
 * <li>Current Security System State: String type</li>
 * <li>Target Security System State: String type</li>
 * <li>Alarm Type: Switch type</li>
 * </ul>
 *
 * @author Justin Mutter
 */
class HomekitSecuritySystemImpl extends AbstractHomekitAccessoryImpl<GroupItem>
        implements SecuritySystem, GroupedAccessory {

    private final String groupName;
    private String currentSecuritySystemStateItemName;
    private String targetSecuritySystemStateItemName;
    private String alarmTypeItemName;

    private Logger logger = LoggerFactory.getLogger(HomekitSecuritySystemImpl.class);

    public HomekitSecuritySystemImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater) {
        super(taggedItem, itemRegistry, updater, GroupItem.class);
        this.groupName = taggedItem.getItem().getName();
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public void addCharacteristic(HomekitTaggedItem item) {
        switch (item.getCharacteristicType()) {
            case SECURITY_SYSTEM_CURRENT_STATE:
                currentSecuritySystemStateItemName = item.getItem().getName();
                break;

            case SECURITY_SYSTEM_TARGET_STATE:
                targetSecuritySystemStateItemName = item.getItem().getName();
                break;

            case SECURITY_SYSTEM_ALARM_TYPE:
                alarmTypeItemName = item.getItem().getName();
                break;

            default:
                logger.error("Unrecognized security system characteristic: {}", item.getCharacteristicType().name());
                break;
        }
    }

    @Override
    public boolean isComplete() {
        return currentSecuritySystemStateItemName != null && targetSecuritySystemStateItemName != null
                && alarmTypeItemName != null;
    }

    @Override
    public CompletableFuture<CurrentSecuritySystemState> getCurrentSecuritySystemState() {
        CurrentSecuritySystemState systemState = null;

        if (currentSecuritySystemStateItemName != null) {
            Item item = getItemRegistry().get(currentSecuritySystemStateItemName);

            if (item != null) {
                State itemState = item.getState();
                String stringValue = itemState.toString();

                if (stringValue.equals("stayArm")) {
                    systemState = CurrentSecuritySystemState.STAY_ARM;
                } else if (stringValue.equals("awayArm")) {
                    systemState = CurrentSecuritySystemState.AWAY_ARM;
                } else if (stringValue.equals("nightArm")) {
                    systemState = CurrentSecuritySystemState.NIGHT_ARM;
                } else if (stringValue.equals("disarmed")) {
                    systemState = CurrentSecuritySystemState.DISARMED;
                } else if (stringValue.equals("triggered")) {
                    systemState = CurrentSecuritySystemState.TRIGGERED;
                } else if (stringValue.equals("UNDEF") || stringValue.equals("NULL")) {
                    logger.debug("Current security system state not available. Relaying value of DISARMED to Homekit");
                    systemState = CurrentSecuritySystemState.DISARMED;
                } else {
                    logger.error(
                            "Unrecognized security system state: {}. Expected stayArm, awayArm, nightArm, disarmed, or triggered string.",
                            stringValue);
                    systemState = CurrentSecuritySystemState.DISARMED;
                }
            }
        }

        if (systemState == null) {
            logger.error("Unable to get current security system state");
        } else {
            logger.debug("currentSecuritySystemState: {}", systemState);
        }
        return CompletableFuture.completedFuture(systemState);
    }

    @Override
    public CompletableFuture<TargetSecuritySystemState> getTargetSecuritySystemState() {
        TargetSecuritySystemState systemState = null;

        if (targetSecuritySystemStateItemName != null) {
            Item item = getItemRegistry().get(targetSecuritySystemStateItemName);

            if (item != null) {
                State itemState = item.getState();
                String stringValue = itemState.toString();

                if (stringValue.equals("stayArm")) {
                    systemState = TargetSecuritySystemState.STAY_ARM;
                } else if (stringValue.equals("awayArm")) {
                    systemState = TargetSecuritySystemState.AWAY_ARM;
                } else if (stringValue.equals("nightArm")) {
                    systemState = TargetSecuritySystemState.NIGHT_ARM;
                } else if (stringValue.equals("disarm")) {
                    systemState = TargetSecuritySystemState.DISARMED; // TODO: Update this constant:
                                                                      // https://github.com/beowulfe/HAP-Java/pull/44
                } else if (stringValue.equals("UNDEF") || stringValue.equals("NULL")) {
                    logger.debug("Current security system state not available. Relaying value of DISARMED to Homekit");
                    systemState = TargetSecuritySystemState.DISARMED;
                } else {
                    logger.error(
                            "Unrecognized security system state: {}. Expected stay, away, night, or disarmed strings in value.",
                            stringValue);
                    systemState = TargetSecuritySystemState.DISARMED;
                }
            }
        }

        if (systemState == null) {
            logger.error("Unable to get target security system state");
        } else {
            logger.debug("targetSecuritySystemState: {}", systemState);
        }
        return CompletableFuture.completedFuture(systemState);
    }

    @Override
    public CompletableFuture<SecuritySystemAlarmType> getAlarmTypeState() {
        SecuritySystemAlarmType alarmType = null;

        if (alarmTypeItemName != null) {
            Item item = getItemRegistry().get(alarmTypeItemName);
            if (item != null) {
                State itemState = item.getState();
                String stringValue = itemState.toString();

                if (stringValue.equals("noAlarm")) {
                    alarmType = SecuritySystemAlarmType.CLEARED; // TODO: Update this constant:
                                                                 // https://github.com/beowulfe/HAP-Java/pull/45
                } else if (stringValue.equals("unknown")) {
                    alarmType = SecuritySystemAlarmType.UNKNOWN;
                } else if (stringValue.equals("UNDEF") || stringValue.equals("NULL")) {
                    logger.debug(
                            "Current security system alarm type not available. Relaying value of CLEARED to Homekit");
                    alarmType = SecuritySystemAlarmType.CLEARED;
                } else {
                    logger.error("Unrecognized security system alarm type: {}. Expected noAlarm or unknown",
                            stringValue);
                    alarmType = SecuritySystemAlarmType.CLEARED;
                }
            }
        }
        if (alarmType == null) {
            logger.error("Unable to get alarm type");
        } else {
            logger.debug("alarmType: {}", alarmType);
        }
        return CompletableFuture.completedFuture(alarmType);
    }

    @Override
    public void setTargetSecuritySystemState(TargetSecuritySystemState state) throws Exception {
        String stateString = null;
        switch (state) {
            case STAY_ARM:
                stateString = "stayArm";
                break;
            case NIGHT_ARM:
                stateString = "nightArm";
                break;
            case AWAY_ARM:
                stateString = "awayArm";
                break;
            case DISARMED: // TODO: Update this constant: https://github.com/beowulfe/HAP-Java/pull/44
                stateString = "disarm";
                break;
        }
        logger.debug("Setting targetSecuritySystemState to: {} as per state {}", stateString, state);
        StringItem item = getGenericItem(targetSecuritySystemStateItemName);
        item.send(new StringType(stateString));
    }

    @Override
    public void subscribeCurrentSecuritySystemState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getGenericItem(currentSecuritySystemStateItemName), callback);
    }

    @Override
    public void unsubscribeCurrentSecuritySystemState() {
        getUpdater().unsubscribe(getGenericItem(currentSecuritySystemStateItemName));
    }

    @Override
    public void subscribeTargetSecuritySystemState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getGenericItem(targetSecuritySystemStateItemName), callback);
    }

    @Override
    public void unsubscribeTargetSecuritySystemState() {
        getUpdater().unsubscribe(getGenericItem(targetSecuritySystemStateItemName));
    }

    @Override
    public void subscribeAlarmTypeState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getGenericItem(alarmTypeItemName), callback);
    }

    @Override
    public void unsubscribeAlarmTypeState() {
        getUpdater().unsubscribe(getGenericItem(alarmTypeItemName));
    }

    @SuppressWarnings("unchecked")
    private <T extends GenericItem> T getGenericItem(String name) {
        Item item = getItemRegistry().get(name);
        if (item == null) {
            return null;
        }
        if (!(item instanceof GenericItem)) {
            throw new RuntimeException("Expected GenericItem, found " + item.getClass().getCanonicalName());
        }
        return (T) item;
    }

}
