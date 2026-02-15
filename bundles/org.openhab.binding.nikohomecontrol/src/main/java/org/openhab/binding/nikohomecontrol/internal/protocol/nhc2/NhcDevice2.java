/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc2;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link NhcDevice2} represents a Niko Home Control II device. It is used when parsing the device response json and
 * when creating the state update json to send to the Connected Controller.
 *
 * @author Mark Herwege - Initial Contribution
 * @author Mark Herwege - Add car chargers
 */
@NonNullByDefault
class NhcDevice2 {
    static class NhcProperty {
        // fields for lights
        @Nullable
        String status;
        @Nullable
        String brightness;
        @Nullable
        String aligned;
        @Nullable
        String basicState;

        // fields for motors
        @Nullable
        String action;
        @Nullable
        String position;
        @Nullable
        String moving;

        // fields for thermostats and hvac
        @Nullable
        String setpointTemperature;
        @Nullable
        String program;
        @Nullable
        String overruleActive;
        @Nullable
        String overruleSetpoint;
        @Nullable
        String overruleTime;
        @Nullable
        String ecoSave;
        @Nullable
        String demand;
        @Nullable
        String operationMode;
        @Nullable
        String ambientTemperature;
        @Nullable
        String protectMode;
        @Nullable
        String thermostatOn;
        @Nullable
        String hvacOn;

        // fields for fans and ventilation
        @Nullable
        String fanSpeed;

        // fields for electricity metering
        @Nullable
        String electricalPower;
        @Nullable
        String electricalPowerToGrid;
        @Nullable
        String electricalPowerFromGrid;
        @Nullable
        String electricalMonthlyPeakPowerFromGrid;
        @Nullable
        String electricalPowerProduction;
        @Nullable
        String electricalPowerSelfConsumption;
        @Nullable
        String electricalPowerConsumption;
        @Nullable
        String electricalPowerProductionThresholdExceeded;
        @Nullable
        String reportInstantUsage;
        @Nullable
        String electricalEnergy;
        @Nullable
        String electricalEnergyConsumption;
        @Nullable
        String electricalEnergyToGrid;
        @Nullable
        String electricalEnergyFromGrid;
        @Nullable
        String electricalEnergySelfConsumption;
        @Nullable
        String gasVolume;
        @Nullable
        String waterVolume;

        // fields for access control
        @Nullable
        String doorlock;

        // fields for video devices
        @Nullable
        String ipAddress;
        @Nullable
        String callStatus01;
        @Nullable
        String callStatus02;
        @Nullable
        String callStatus03;
        @Nullable
        String callStatus04;

        // fields for alarms
        @Nullable
        String internalState;
        @Nullable
        String alarmActive;
        @Nullable
        String alarmTriggered;
        @Nullable
        String control;

        // fields for car chargers
        @Nullable
        String chargingStatus;
        @Nullable
        String evStatus;
        @Nullable
        String couplingStatus;
        @Nullable
        String chargingMode;
        @Nullable
        String targetDistance;
        @Nullable
        String targetTime;
        @Nullable
        String boost;
        @Nullable
        String reachableDistance;
        @Nullable
        String nextChargingTime;
    }

    static class NhcTrait {
        @Nullable
        String macAddress;

        // fields for metering
        @Nullable
        String channel;
        @Nullable
        String meterType;

        // fields for car chargers
        @Nullable
        String playerName;
    }

    static class NhcParameter {
        @Nullable
        String locationId;
        @Nullable
        String locationName;
        @Nullable
        String locationIcon;

        // fields for electricity metering
        @Nullable
        String flow;
        @Nullable
        String segment;
        @Nullable
        String clampType;
        @Nullable
        String shortName;

        // fields for access control
        @Nullable
        String buttonId;
        @Nullable
        String ringTone;
        @Nullable
        String declineCallAppliedOnAllDevices;
        @Nullable
        String iconCode;

        // fields for video devices
        @Nullable
        String mjpegUri;
        @Nullable
        String tnUri;
    }

    String name = "";
    String uuid = "";
    String technology = "";
    String identifier = "";
    String model = "";
    String type = "";
    String online = "";
    @Nullable
    List<NhcProperty> properties;
    @Nullable
    List<NhcTrait> traits;
    @Nullable
    List<NhcParameter> parameters;
}
