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
package org.openhab.binding.plugwiseha.internal.api.model.dto;

import java.time.ZonedDateTime;
import java.util.Optional;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The {@link Appliance} class is an object model class that
 * mirrors the XML structure provided by the Plugwise Home Automation
 * controller for a Plugwise appliance.
 * It implements the {@link PlugwiseComparableDate} interface and
 * extends the abstract class {@link PlugwiseBaseModel}.
 * 
 * @author B. van Wetten - Initial contribution
 * @author Leo Siepel - finish initial contribution
 */
@XStreamAlias("appliance")
public class Appliance extends PlugwiseBaseModel implements PlugwiseComparableDate<Appliance> {

    private String name;
    private String description;
    private String type;
    private String location;

    @XStreamAlias("module")
    private Module module;

    @XStreamAlias("zig_bee_node")
    private ZigBeeNode zigbeeNode;

    @XStreamImplicit(itemFieldName = "point_log", keyFieldName = "type")
    private PointLogs pointLogs;

    @XStreamImplicit(itemFieldName = "cumulative_log", keyFieldName = "type")
    private CumulativeLogs cumulativeLogs;

    @XStreamImplicit(itemFieldName = "actuator_functionality", keyFieldName = "type")
    private ActuatorFunctionalities actuatorFunctionalities;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public ZigBeeNode getZigbeeNode() {
        if (zigbeeNode == null) {
            zigbeeNode = new ZigBeeNode();
        }
        return zigbeeNode;
    }

    public Module getModule() {
        if (module == null) {
            module = new Module();
        }
        return module;
    }

    public PointLogs getPointLogs() {
        if (pointLogs == null) {
            pointLogs = new PointLogs();
        }
        return pointLogs;
    }

    public CumulativeLogs getCumulativeLogs() {
        if (cumulativeLogs == null) {
            cumulativeLogs = new CumulativeLogs();
        }
        return cumulativeLogs;
    }

    public ActuatorFunctionalities getActuatorFunctionalities() {
        if (actuatorFunctionalities == null) {
            actuatorFunctionalities = new ActuatorFunctionalities();
        }
        return actuatorFunctionalities;
    }

    public Optional<Double> getTemperature() {
        return getPointLogs().getTemperature();
    }

    public Optional<String> getTemperatureUnit() {
        return getPointLogs().getTemperatureUnit();
    }

    public Optional<Double> getSetpointTemperature() {
        return getPointLogs().getThermostatTemperature();
    }

    public Optional<String> getSetpointTemperatureUnit() {
        return getPointLogs().getThermostatTemperatureUnit();
    }

    public Optional<Double> getOffsetTemperature() {
        return getPointLogs().getOffsetTemperature();
    }

    public Optional<String> getOffsetTemperatureUnit() {
        return getPointLogs().getOffsetTemperatureUnit();
    }

    public Optional<Boolean> getRelayState() {
        return getPointLogs().getRelayState();
    }

    public Optional<Boolean> getRelayLockState() {
        return getActuatorFunctionalities().getRelayLockState();
    }

    public Optional<Double> getBatteryLevel() {
        return getPointLogs().getBatteryLevel();
    }

    public Optional<Double> getPowerUsage() {
        return getPointLogs().getPowerUsage();
    }

    public Optional<Double> getValvePosition() {
        return getPointLogs().getValvePosition();
    }

    public Optional<Double> getWaterPressure() {
        return getPointLogs().getWaterPressure();
    }

    public Optional<Boolean> getCHState() {
        return getPointLogs().getCHState();
    }

    public Optional<Boolean> getCoolingState() {
        return getPointLogs().getCoolingState();
    }

    public Optional<Double> getIntendedBoilerTemp() {
        return getPointLogs().getIntendedBoilerTemp();
    }

    public Optional<String> getIntendedBoilerTempUnit() {
        return getPointLogs().getIntendedBoilerTempUnit();
    }

    public Optional<Double> getReturnWaterTemp() {
        return getPointLogs().getReturnWaterTemp();
    }

    public Optional<String> getReturnWaterTempUnit() {
        return getPointLogs().getReturnWaterTempUnit();
    }

    public Optional<Boolean> getFlameState() {
        return getPointLogs().getFlameState();
    }

    public Optional<Double> getBurnerFailedStarts() {
        return getCumulativeLogs().getBurnerFailedStarts();
    }

    public Optional<Double> getBurnerStarts() {
        return getCumulativeLogs().getBurnerStarts();
    }

    public Optional<Double> getBurnerOpTime() {
        return getCumulativeLogs().getBurnerOpTime();
    }

    public Optional<Double> getBurnerDHWOPTime() {
        return getCumulativeLogs().getBurnerDHWOPTime();
    }

    public Optional<Double> getBurnerFailedIgnitions() {
        return getCumulativeLogs().getBurnerFailedIgnitions();
    }

    public Optional<Boolean> getIntendedHeatingState() {
        return getPointLogs().getIntendedHeatingState();
    }

    public Optional<Double> getModulationLevel() {
        return getPointLogs().getModulationLevel();
    }

    public Optional<Double> getOTAppFaultCode() {
        return getPointLogs().getOTAppFaultCode();
    }

    public Optional<Double> getDHWTemp() {
        return getPointLogs().getDHWTemp();
    }

    public Optional<String> getDHWTempUnit() {
        return getPointLogs().getDHWTempUnit();
    }

    public Optional<Double> getOTOEMFaultcode() {
        return getPointLogs().getOTOEMFaultcode();
    }

    public Optional<Double> getBoilerTemp() {
        return getPointLogs().getBoilerTemp();
    }

    public Optional<String> getBoilerTempUnit() {
        return getPointLogs().getBoilerTempUnit();
    }

    public Optional<Double> getDHTSetpoint() {
        return getPointLogs().getDHTSetpoint();
    }

    public Optional<String> getDHTSetpointUnit() {
        return getPointLogs().getDHTSetpointUnit();
    }

    public Optional<Double> getMaxBoilerTemp() {
        return getPointLogs().getMaxBoilerTemp();
    }

    public Optional<String> getMaxBoilerTempUnit() {
        return getPointLogs().getMaxBoilerTempUnit();
    }

    public Optional<Boolean> getDHWComfortMode() {
        return getPointLogs().getDHWComfortMode();
    }

    public Optional<Boolean> getDHWState() {
        return getPointLogs().getDHWState();
    }

    public boolean isZigbeeDevice() {
        return (this.zigbeeNode instanceof ZigBeeNode);
    }

    public boolean isBatteryOperated() {
        if (this.zigbeeNode instanceof ZigBeeNode) {
            return "battery".equals(this.zigbeeNode.getPowerSource()) && this.getBatteryLevel().isPresent();
        } else {
            return false;
        }
    }

    @Override
    public int compareDateWith(Appliance compareTo) {
        if (compareTo == null) {
            return -1;
        }
        ZonedDateTime compareToDate = compareTo.getModifiedDate();
        ZonedDateTime compareFromDate = this.getModifiedDate();
        if (compareFromDate == null) {
            return -1;
        } else if (compareToDate == null) {
            return 1;
        } else {
            return compareFromDate.compareTo(compareToDate);
        }
    }

    @Override
    public boolean isNewerThan(Appliance hasModifiedDate) {
        return compareDateWith(hasModifiedDate) > 0;
    }

    @Override
    public boolean isOlderThan(Appliance hasModifiedDate) {
        return compareDateWith(hasModifiedDate) < 0;
    }
}
