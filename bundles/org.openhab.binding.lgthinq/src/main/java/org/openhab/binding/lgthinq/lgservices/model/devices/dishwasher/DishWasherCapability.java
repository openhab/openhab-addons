/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.AbstractCapability;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDefinition;
import org.openhab.binding.lgthinq.lgservices.model.devices.commons.washers.CourseDefinition;

/**
 * The {@link DishWasherCapability}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class DishWasherCapability extends AbstractCapability<DishWasherCapability> {
    private FeatureDefinition doorState = FeatureDefinition.NULL_DEFINITION;
    private FeatureDefinition state = FeatureDefinition.NULL_DEFINITION;
    private FeatureDefinition processState = FeatureDefinition.NULL_DEFINITION;
    private Map<String, CourseDefinition> courses = new LinkedHashMap<>();

    public FeatureDefinition getProcessState() {
        return processState;
    }

    public void setProcessState(FeatureDefinition processState) {
        this.processState = processState;
    }

    public Map<String, CourseDefinition> getCourses() {
        return courses;
    }

    public void setCourses(Map<String, CourseDefinition> courses) {
        this.courses = courses;
    }

    public FeatureDefinition getStateFeat() {
        return state;
    }

    public FeatureDefinition getDoorStateFeat() {
        return doorState;
    }

    public void setDoorStateFeat(FeatureDefinition doorState) {
        this.doorState = doorState;
    }

    public void setState(FeatureDefinition state) {
        this.state = state;
    }
}
