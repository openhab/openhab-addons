/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.semp.internal;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.items.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is representing a SEMP electrical consumer
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class SEMPConsumer {
    private final Logger logger = LoggerFactory.getLogger(SEMPConsumer.class);
    private static final String PREFIX_SEMP = "semp:";
    private static final String PREFIX_SEMP_DEVICE_ID = PREFIX_SEMP + "device_id:";
    private static final String PREFIX_SEMP_DEVICE_VENDOR = PREFIX_SEMP + "device_vendor:";
    private static final String PREFIX_SEMP_DEVICE_TYPE = PREFIX_SEMP + "device_type:";
    private static final String PREFIX_SEMP_SERIAL = PREFIX_SEMP + "device_serial:";
    private static final String PREFIX_SEMP_MAX_POWER = PREFIX_SEMP + "max_power:";
    private static final String PREFIX_SEMP_MIN_OFF_TIME = PREFIX_SEMP + "min_off_time:";
    private static final String PREFIX_SEMP_MIN_ON_TIME = PREFIX_SEMP + "min_on_time:";
    private static final String PREFIX_SEMP_METHOD = PREFIX_SEMP + "method";
    private static final String PREFIX_SEMP_INTER_ALOWED = PREFIX_SEMP + "inter_alowed:";
    private static final String PREFIX_SEMP_EARLIEST_START = PREFIX_SEMP + "earliest_start:";
    private static final String PREFIX_SEMP_LATEST_END = PREFIX_SEMP + "latest_end:";
    private static final String PREFIX_SEMP_MIN_RUNNING_TIME = PREFIX_SEMP + "min_running_time:";
    private static final String PREFIX_SEMP_MAX_RUNNING_TIME = PREFIX_SEMP + "max_running_time:";
    private static final String PREFIX_SEMP_RUNNING_WEEKDAYS = PREFIX_SEMP + "days_of_week:";

    private static final List<String> ALOWED_DEVICE_TYPES = Arrays.asList("AirConditioning", "Charger", "DishWasher",
            "Dryer", "ElectricVehicle", "EVCharger", "Fridge", "Heater", "HeatPump", "Motor", "Pump", "WashingMachine",
            "Other");

    private SEMPIdentification identification = new SEMPIdentification();
    private SEMPCharacteristics characteristics = new SEMPCharacteristics();
    private SEMPCapabilities capabilities = new SEMPCapabilities();
    private SEMPDeviceStatus deviceStatus = new SEMPDeviceStatus();
    private List<SEMPTimeFrame> timeFrames = new ArrayList<SEMPTimeFrame>();
    private List<SEMPDeviceStatus> deviceHistoryStatus = new ArrayList<SEMPDeviceStatus>();
    private List<String> daysOfTheWeek = new ArrayList<String>();
    public boolean hasHistory = false;

    private Item controlItem;
    private Item energyItem;
    private Item groupItem;
    private Item isConnectedItem;
    private Item isListeningItem;

    public SEMPConsumer() {
    }

    public void unsetItems() {
        controlItem = null;
        energyItem = null;
        isConnectedItem = null;
        isListeningItem = null;
    }

    public SEMPIdentification getIdentification() {
        return identification;
    }

    public SEMPCharacteristics getCharacteristics() {
        return characteristics;
    }

    public SEMPCapabilities getCapabilities() {
        return capabilities;
    }

    public SEMPDeviceStatus getDeviceStatus() {
        return deviceStatus;
    }

    public List<SEMPTimeFrame> getTimeFrames() {
        return timeFrames;
    }

    public List<SEMPDeviceStatus> getDeviceHistoryStatus() {
        return deviceHistoryStatus;
    }

    public List<String> getDaysOfTheWeek() {
        return daysOfTheWeek;
    }

    public Item getGroupItem() {
        return groupItem;
    }

    public Item getControlItem() {
        return controlItem;
    }

    public Item getEnergyItem() {
        return energyItem;
    }

    public Item getConnectionItem() {
        return isConnectedItem;
    }

    public Item getListeningItem() {
        return isListeningItem;
    }

    public void setGroupItem(Item groupItem) {
        this.groupItem = groupItem;
    }

    public void setControlItem(Item controlItem) {
        this.controlItem = controlItem;
    }

    public void setEnergyItem(Item energyItem) {
        this.energyItem = energyItem;
    }

    public void setConnectionItem(Item isConnectedItem) {
        this.isConnectedItem = isConnectedItem;
    }

    public void setListeningItem(Item isListeningItem) {
        this.isListeningItem = isListeningItem;
    }

    public boolean checkItemsTags() {
        String groupLabel = groupItem.getLabel();
        if (groupLabel == null) {
            logger.error("{}:Label:No Grouplabel set", groupItem.getName());
            return false;
        } else {
            getIdentification().setDeviceName(groupLabel);
        }
        for (String groupTag : groupItem.getTags()) {
            if (groupTag.startsWith(PREFIX_SEMP_DEVICE_ID)) {
                getIdentification().setDeviceId(groupTag.substring(PREFIX_SEMP_DEVICE_ID.length()));
            }
            if (groupTag.startsWith(PREFIX_SEMP_DEVICE_VENDOR)) {
                getIdentification().setDeviceVendor(groupTag.substring(PREFIX_SEMP_DEVICE_VENDOR.length()));
            }
            if (groupTag.startsWith(PREFIX_SEMP_DEVICE_TYPE)) {
                String devType = groupTag.substring(PREFIX_SEMP_DEVICE_TYPE.length());
                if (ALOWED_DEVICE_TYPES.contains(devType)) {
                    getIdentification().setDeviceType(devType);
                } else {
                    logger.error("{}:{}:Type is not valid", groupItem.getName(), PREFIX_SEMP_MAX_POWER);
                    return false;
                }
            }
            if (groupTag.startsWith(PREFIX_SEMP_SERIAL)) {
                getIdentification().setDeviceSerial(groupTag.substring(PREFIX_SEMP_SERIAL.length()));
            }
            if (groupTag.startsWith(PREFIX_SEMP_MAX_POWER)) {
                try {
                    getCharacteristics().setMaxPowerConsumption(
                            Integer.parseInt(groupTag.substring(PREFIX_SEMP_MAX_POWER.length())));
                } catch (NumberFormatException e) {
                    logger.error("{}:{}:{}", groupItem.getName(), PREFIX_SEMP_MAX_POWER, e.getMessage());
                    return false;
                }
            }
            if (groupTag.startsWith(PREFIX_SEMP_MIN_OFF_TIME)) {
                try {
                    getCharacteristics()
                            .setMinOffTime(Integer.parseInt(groupTag.substring(PREFIX_SEMP_MIN_OFF_TIME.length())));
                } catch (NumberFormatException e) {
                    logger.error("{}:{}:{}", groupItem.getName(), PREFIX_SEMP_MIN_OFF_TIME, e.getMessage());
                    return false;
                }
            }
            if (groupTag.startsWith(PREFIX_SEMP_MIN_ON_TIME)) {
                try {
                    getCharacteristics()
                            .setMinOnTime(Integer.parseInt(groupTag.substring(PREFIX_SEMP_MIN_ON_TIME.length())));
                } catch (NumberFormatException e) {
                    logger.error("{}:{}:{}", groupItem.getName(), PREFIX_SEMP_MIN_ON_TIME, e.getMessage());
                }
            }
            if (groupTag.startsWith(PREFIX_SEMP_METHOD)) {
                getCapabilities().setMethod(groupTag.substring(PREFIX_SEMP_METHOD.length()));
            }
            if (groupTag.startsWith(PREFIX_SEMP_INTER_ALOWED)) {
                try {
                    getCapabilities().setInterruptionsAllowed(
                            Boolean.parseBoolean(groupTag.substring(PREFIX_SEMP_INTER_ALOWED.length())));
                } catch (NumberFormatException e) {
                    logger.error("{}:{}:{}", groupItem.getName(), PREFIX_SEMP_INTER_ALOWED, e.getMessage());
                    return false;
                }
            }
            if (groupTag.startsWith(PREFIX_SEMP_EARLIEST_START)) {
                try {
                    String[] parts = groupTag.substring(PREFIX_SEMP_EARLIEST_START.length()).split(":");
                    for (int i = 0; i < parts.length; i++) {
                        if (getTimeFrames().size() > i) {
                            getTimeFrames().get(i).setEarliestStart(Integer.parseInt(parts[i]) * 60);
                        } else {
                            SEMPTimeFrame timeFrame = new SEMPTimeFrame();
                            timeFrame.setEarliestStart(Integer.parseInt(parts[i]) * 60);
                            getTimeFrames().add(timeFrame);
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.error("{}:{}:{}", groupItem.getName(), PREFIX_SEMP_EARLIEST_START, e.getMessage());
                    return false;
                }
            }
            if (groupTag.startsWith(PREFIX_SEMP_LATEST_END)) {
                try {
                    String[] parts = groupTag.substring(PREFIX_SEMP_LATEST_END.length()).split(":");
                    for (int i = 0; i < parts.length; i++) {
                        if (getTimeFrames().size() > i) {
                            getTimeFrames().get(i).setLatestEnd(Integer.parseInt(parts[i]) * 60);
                        } else {
                            SEMPTimeFrame timeFrame = new SEMPTimeFrame();
                            timeFrame.setLatestEnd(Integer.parseInt(parts[i]) * 60);
                            getTimeFrames().add(timeFrame);
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.error("{}:{}:{}", groupItem.getName(), PREFIX_SEMP_LATEST_END, e.getMessage());
                    return false;
                }
            }
            if (groupTag.startsWith(PREFIX_SEMP_MIN_RUNNING_TIME)) {
                try {
                    String[] parts = groupTag.substring(PREFIX_SEMP_MIN_RUNNING_TIME.length()).split(":");
                    for (int i = 0; i < parts.length; i++) {
                        if (getTimeFrames().size() > i) {
                            getTimeFrames().get(i).setMinRunningTime(Integer.parseInt(parts[i]) * 60);
                        } else {
                            SEMPTimeFrame timeFrame = new SEMPTimeFrame();
                            timeFrame.setMinRunningTime(Integer.parseInt(parts[i]) * 60);
                            getTimeFrames().add(timeFrame);
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.error("{}:{}:{}", groupItem.getName(), PREFIX_SEMP_MIN_RUNNING_TIME, e.getMessage());
                    return false;
                }
            }
            if (groupTag.startsWith(PREFIX_SEMP_MAX_RUNNING_TIME)) {
                try {
                    String[] parts = groupTag.substring(PREFIX_SEMP_MAX_RUNNING_TIME.length()).split(":");
                    for (int i = 0; i < parts.length; i++) {
                        if (getTimeFrames().size() > i) {
                            getTimeFrames().get(i).setMaxRunningTime(Integer.parseInt(parts[i]) * 60);
                        } else {
                            SEMPTimeFrame timeFrame = new SEMPTimeFrame();
                            timeFrame.setMaxRunningTime(Integer.parseInt(parts[i]) * 60);
                            getTimeFrames().add(timeFrame);
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.error("{}:{}:{}", groupItem.getName(), PREFIX_SEMP_MAX_RUNNING_TIME, e.getMessage());
                    return false;
                }
            }
            if (groupTag.startsWith(PREFIX_SEMP_RUNNING_WEEKDAYS)) {
                String[] parts = groupTag.substring(PREFIX_SEMP_RUNNING_WEEKDAYS.length()).split(":");
                for (int i = 0; i < parts.length; i++) {
                    daysOfTheWeek.add(parts[i]);
                }
            }
        }
        return checkTimeFrames();
    }

    public void setDefaultTags() {
        this.getCapabilities().setAbsoluteTimestamps(true);
        this.getCapabilities().setOptionalEnergy(true);
        if (!this.getIdentification().isDeviceIdSet()) {
            int hashVal = groupItem.getName().hashCode();
            if (hashVal < 0) {
                hashVal *= -1;
            }
            if (hashVal < 10000000) {
                hashVal += 10000000;
            }
            this.getIdentification().setDeviceId("F-" + String.valueOf(hashVal).substring(0, 8) + "-112233445566-00");
        }
        if (!this.getIdentification().isDeviceVendorSet()) {
            this.getIdentification().setDeviceVendor("Unknown");
        }
        if (!this.getIdentification().isDeviceTypeSet()) {
            this.getIdentification().setDeviceType("Other");
        }
        if (!this.getCapabilities().isMethodSet()) {
            this.getCapabilities().setMethod("Measurement");
        }
        if (!this.getCapabilities().isInterruptionsAllowedSet()) {
            this.getCapabilities().setInterruptionsAllowed(true);
        }
    }

    private boolean checkTimeFrames() {
        for (int i = getTimeFrames().size() - 1; i >= 0; i--) {
            if (getTimeFrames().get(i).getEarliestStart() < 0 || getTimeFrames().get(i).getEarliestStart() > 86400
                    || getTimeFrames().get(i).getLatestEnd() < 0 || getTimeFrames().get(i).getLatestEnd() > 86400
                    || getTimeFrames().get(i).getMaxRunningTime() < 0
                    || getTimeFrames().get(i).getMaxRunningTime() > 86400
                    || getTimeFrames().get(i).getMinRunningTime() < 0
                    || getTimeFrames().get(i).getMinRunningTime() > 86400) {
                logger.error("Alle time frame values has to be between 0 and 1440 minutes");
                getTimeFrames().clear();
                return false;
            }
            if (getTimeFrames().get(i).getEarliestStart() > getTimeFrames().get(i).getLatestEnd()) {
                logger.error("The earliest start has to be smaller then latest end");
                getTimeFrames().clear();
                return false;
            }
            if (i > 0) {
                if (getTimeFrames().get(i).getEarliestStart() < getTimeFrames().get(i - 1).getLatestEnd()) {
                    logger.error("The end of a time frame has to be smaller or same then the start of the next one");
                    getTimeFrames().clear();
                    return false;
                }
            }
            if (getTimeFrames().get(i).getMinRunningTime() > getTimeFrames().get(i).getMaxRunningTime()) {
                logger.error("The min running time has to be smaller then max running time");
                getTimeFrames().clear();
                return false;
            }
            if (getTimeFrames().get(i).getMinRunningTime() > (getTimeFrames().get(i).getLatestEnd()
                    - getTimeFrames().get(i).getEarliestStart())) {
                logger.error("The min running time has to be smaller then the planing range");
                getTimeFrames().clear();
                return false;
            }
        }
        return true;
    }

    public SEMPTimeFrame getCurrentTimeFrame(long timestamp) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long unixDayTime = startOfToday.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(Instant.now()));
        for (int i = timeFrames.size() - 1; i >= 0; i--) {
            if (timestamp > (timeFrames.get(i).getEarliestStart() + unixDayTime)) {
                return timeFrames.get(i);
            }
        }
        if (timestamp < (timeFrames.get(0).getEarliestStart() + unixDayTime)) {
            return timeFrames.get(timeFrames.size() - 1);
        }
        return null;
    }
}
