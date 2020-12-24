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

import java.time.ZonedDateTime;
import java.util.Optional;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * The {@link ActuatorFunctionality} class is an object model class that mirrors
 * the XML structure provided by the Plugwise Home Automation controller for the
 * any actuator functionality. It implements the {@link PlugwiseComparableDate}
 * interface and extends the abstract class {@link PlugwiseBaseModel}.
 * 
 * @author B. van Wetten - Initial contribution
 */
@XStreamAlias("actuator_functionality")
public class ActuatorFunctionality extends PlugwiseBaseModel implements PlugwiseComparableDate<ActuatorFunctionality> {

    private String type;
    private String duration;
    private String setpoint;
    private String resolution;
    private String lock;

    @XStreamAlias("lower_bound")
    private String lowerBound;

    @XStreamAlias("upper_bound")
    private String upperBound;

    @XStreamAlias("updated_date")
    private ZonedDateTime updatedDate;

    public String getType() {
        return type;
    }

    public String getDuration() {
        return duration;
    }

    public String getSetpoint() {
        return setpoint;
    }

    public String getResolution() {
        return resolution;
    }

    public String getLowerBound() {
        return lowerBound;
    }

    public String getUpperBound() {
        return upperBound;
    }

    public ZonedDateTime getUpdatedDate() {
        return updatedDate;
    }

    public Optional<String> getRelayLockState() {
        return Optional.ofNullable(lock);
    }

    @Override
    public int compareDateWith(ActuatorFunctionality hasUpdatedDate) {
        return this.getUpdatedDate().compareTo(hasUpdatedDate.getUpdatedDate());
    }

    @Override
    public boolean isOlderThan(ActuatorFunctionality hasUpdatedDate) {
        return this.compareDateWith(hasUpdatedDate) < 0;
    }

    public boolean isNewerThan(ActuatorFunctionality hasUpdatedDate) {
        return this.compareDateWith(hasUpdatedDate) > 0;
    }
}
