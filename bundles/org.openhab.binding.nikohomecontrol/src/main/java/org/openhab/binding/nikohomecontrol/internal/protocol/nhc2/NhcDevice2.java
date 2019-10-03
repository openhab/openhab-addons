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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc2;

import java.util.List;

/**
 * {@link NhcDevice2} represents a Niko Home Control II device. It is used when parsing the device response json and
 * when creating the state update json to send to the Connected Controller.
 *
 * @author Mark Herwege - Initial Contribution
 */
class NhcDevice2 {
    static class NhcProperty {
        // fields for lights
        String status;
        String brightness;
        String aligned;
        String basicState;
        // fields for motors
        String action;
        String position;
        String moving;
        // fields for thermostats and hvac
        String setpointTemperature;
        String program;
        String overruleActive;
        String overruleSetpoint;
        String overruleTime;
        String ecoSave;
        String demand;
        String operationMode;
        String ambientTemperature;
        String protectMode;
        String thermostatOn;
        String hvacOn;
        // fields for fans and ventilation
        String fanSpeed;
        // fields for electricity metering
        String electricalEnergy;
        String electricalPower;
        String reportInstantUsage;
        // fields for access control
        String doorlock;
    }

    static class NhcTrait {
        String macAddress;
        // fields for energyMeters metering
        String channel;
        String meterType;
    }

    static class NhcParameter {
        String locationId;
        String locationName;
        String locationIcon;
        // fields for electricity metering
        String flow;
        String segment;
        String clampType;
        String shortName;
    }

    String name;
    String uuid;
    String technology;
    String identifier;
    String model;
    String type;
    String online;
    List<NhcProperty> properties;
    List<NhcTrait> traits;
    List<NhcParameter> parameters;
}
