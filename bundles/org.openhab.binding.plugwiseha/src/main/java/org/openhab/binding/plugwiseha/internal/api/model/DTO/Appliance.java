/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
    private Logs pointLogs;

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

    public Logs getPointLogs() {
        if (pointLogs == null) {
            pointLogs = new Logs();
        }
        return pointLogs;
    }

    public ActuatorFunctionalities getActuatorFunctionalities() {
        if (actuatorFunctionalities == null) {
            actuatorFunctionalities = new ActuatorFunctionalities();
        }
        return actuatorFunctionalities;
    }

    public Optional<Double> getTemperature() {
        return this.pointLogs.getTemperature();
    }

    public Optional<String> getTemperatureUnit() {
        return this.pointLogs.getTemperatureUnit();
    }

    public Optional<Double> getSetpointTemperature() {
        return this.pointLogs.getThermostatTemperature();
    }

    public Optional<String> getSetpointTemperatureUnit() {
        return this.pointLogs.getThermostatTemperatureUnit();
    }

    public Optional<Double> getOffsetTemperature() {
        return this.pointLogs.getOffsetTemperature();
    }

    public Optional<String> getOffsetTemperatureUnit() {
        return this.pointLogs.getOffsetTemperatureUnit();
    }

    public Optional<Boolean> getRelayState() {
        return this.pointLogs.getRelayState();
    }

    public Optional<Boolean> getRelayLockState() {
        return this.actuatorFunctionalities.getRelayLockState();
    }

    public Optional<Double> getBatteryLevel() {
        return this.pointLogs.getBatteryLevel();
    }

    public Optional<Double> getPowerUsage() {
        return this.pointLogs.getPowerUsage();
    }

    public Optional<Double> getValvePosition() {
        return this.pointLogs.getValvePosition();
    }

    public Optional<Double> getWaterPressure() {
        return this.pointLogs.getWaterPressure();
    }

    public Optional<Boolean> getCHState() {
        return this.pointLogs.getCHState();
    }

    public Optional<Boolean> getDHWState() {
        return this.pointLogs.getDHWState();
    }

    public boolean isZigbeeDevice() {
        return (this.zigbeeNode instanceof ZigBeeNode);
    }

    public boolean isBatteryOperated() {
        if (this.zigbeeNode instanceof ZigBeeNode) {
            return this.zigbeeNode.getPowerSource().equals("battery") && this.getBatteryLevel().isPresent();
        } else {
            return false;
        }
    }

    @Override
    public int compareDateWith(Appliance hasModifiedDate) {
        if (hasModifiedDate == null) {
            return -1;
        }
        ZonedDateTime compareToDate = this.getModifiedDate();
        ZonedDateTime localcompareFromDate = hasModifiedDate.getModifiedDate();
        if (localcompareFromDate == null) {
            return -1;
        } else if (compareToDate == null) {
            return 1;
        } else {
            return localcompareFromDate.compareTo(compareToDate);
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
