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

package org.openhab.binding.plugwiseha.internal.api.model.DTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The {@link Location} class is an object model class that
 * mirrors the XML structure provided by the Plugwise Home Automation
 * controller for a Plugwise zone/location.
 * It implements the {@link PlugwiseComparableDate} interface and
 * extends the abstract class {@link PlugwiseBaseModel}.
 * 
 * @author B. van Wetten - Initial contribution
 */
@XStreamAlias("location")
public class Location extends PlugwiseBaseModel implements PlugwiseComparableDate<Location> {

    private String name;
    private String description;
    private String type;
    private String preset;

    @XStreamImplicit(itemFieldName = "appliance")
    private List<String> locationAppliances = new ArrayList<String>();

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

    public String getPreset() {
        return preset;
    }

    public List<String> getLocationAppliances() {
        return locationAppliances;
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

    public Optional<Double> getSetpointTemperature() {
        return this.pointLogs.getThermostatTemperature();
    }

    public int applianceCount() {
        if (this.locationAppliances == null) {
            return 0;
        } else {
            return this.locationAppliances.size();
        }
    }

    public int compareDateWith(Location hasModifiedDate) {
        return this.getModifiedDate().compareTo(hasModifiedDate.getModifiedDate());
    }

    public boolean isOlderThan(Location hasModifiedDate) {
        return compareDateWith(hasModifiedDate) < 0;
    }

    public boolean isNewerThan(Location hasModifiedDate) {
        return this.compareDateWith(hasModifiedDate) > 0;
    }
}
