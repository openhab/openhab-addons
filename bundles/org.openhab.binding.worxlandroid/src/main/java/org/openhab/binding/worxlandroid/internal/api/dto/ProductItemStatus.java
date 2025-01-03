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
package org.openhab.binding.worxlandroid.internal.api.dto;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * The {@link ProductItemStatus} class
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class ProductItemStatus {

    public class Accessories {
        public boolean ultrasonic;
    }

    public class MqttTopics {
        public String commandIn;
        public String commandOut;
    }

    public class SetupLocation {
        public double latitude;
        public double longitude;
    }

    public class AppSettings {
        boolean cellularSetupCompleted;
    }

    public class City {
        public int id;
        public int countryId;
        public String name;
        public double latitude;
        public double longitude;
        public String createdAt;
        public String updatedAt;
    }

    public class Sim {
        public int id;
        public String iccid;
        public String simStatus;
        public boolean pendingActivation;
        public ZonedDateTime contractStartsAt;
        public ZonedDateTime contractEndsAt;
        public ZonedDateTime createdAt;
        public ZonedDateTime updatedAt;
    }

    public class AutoSchedule {
        public int boost;
        public String grassType;
        public boolean irrigation;
        public Map<String, String> nutrition;
        public String soilType;
    }

    public String id;
    public String uuid;
    public int productId;
    public String userId;
    public String serialNumber;
    public String macAddress;
    public String name;
    public boolean locked;
    public String firmwareVersion;
    public boolean firmwareAutoUpgrade;
    public boolean pushNotifications;
    public Sim sim;
    public String pushNotificationsLevel;
    public boolean test;
    public boolean iotRegistered;
    public boolean mqttRegistered;
    public String pinCode;
    public String registeredAt;
    public boolean online;
    public String mqttEndpoint;
    public AppSettings appSettings;
    public int protocol;
    public String pendingRadioLinkValidation;
    public List<String> capabilities;
    public List<String> capabilitiesAvailable;
    public Accessories accessories;
    public MqttTopics mqttTopics;
    public boolean warrantyRegistered;
    public String purchasedAt;
    public String warrantyExpiresAt;
    public SetupLocation setupLocation;
    public City city;
    public ZoneId timeZone;
    public double lawnSize;
    public double lawnPerimeter;
    public AutoSchedule autoScheduleSettings;
    public boolean autoSchedule;
    public boolean improvement;
    public boolean diagnostic;
    public long distanceCovered;
    public long mowerWorkTime;

    public long bladeWorkTime;
    public long bladeWorkTimeReset;
    public ZonedDateTime bladeWorkTimeResetAt;

    public int batteryChargeCycles;
    public int batteryChargeCyclesReset;
    public ZonedDateTime batteryChargeCyclesResetAt;

    public ZonedDateTime createdAt;
    public ZonedDateTime updatedAt;
    public LastStatus lastStatus;
}
