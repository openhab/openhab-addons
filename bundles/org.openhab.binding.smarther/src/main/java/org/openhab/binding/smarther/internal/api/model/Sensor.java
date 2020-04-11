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
package org.openhab.binding.smarther.internal.api.model;

import java.util.List;

import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Smarther API Sensor DTO class.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Sensor {

    private List<Measure> measures;

    public List<Measure> getMeasures() {
        return measures;
    }

    public Measure getMeasure(int index) {
        return (measures != null) ? measures.get(index) : null;
    }

    public State toState() {
        return (measures != null && !measures.isEmpty()) ? measures.get(0).toState() : UnDefType.UNDEF;
    }

    @Override
    public String toString() {
        return String.format("measures=%s", measures);
    }

}
