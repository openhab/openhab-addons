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
package org.openhab.binding.intellicenter2.internal.model;

import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.CALIB;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.LISTORD;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.PROBE;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.SNAME;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.SOURCE;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.STATUS;

import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.intellicenter2.internal.protocol.Attribute;
import org.openhab.binding.intellicenter2.internal.protocol.RequestObject;
import org.openhab.binding.intellicenter2.internal.protocol.ResponseObject;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class Sensor extends ResponseModel implements Comparable<Sensor> {

    private final static List<Attribute> REQUEST_ATTRIBUTES = List.of(STATUS, SNAME, LISTORD, SOURCE, PROBE, CALIB);

    public static RequestObject createRefreshRequest(String objectName) {
        return new RequestObject(objectName, REQUEST_ATTRIBUTES);
    }

    Sensor() {
        super(REQUEST_ATTRIBUTES);
    }

    public Sensor(ResponseObject response) {
        super(REQUEST_ATTRIBUTES, response);
    }

    protected Sensor(List<Attribute> requestAttributes) {
        super(requestAttributes);
    }

    protected Sensor(List<Attribute> requestAttributes, ResponseObject response) {
        super(requestAttributes, response);
    }

    public int getListOrder() {
        return getValueAsInt(LISTORD);
    }

    public int getSourceTemperature() {
        return getValueAsInt(SOURCE);
    }

    public int getProbeTemperature() {
        return getValueAsInt(PROBE);
    }

    public int getCalibrationAdjustment() {
        return getValueAsInt(CALIB);
    }

    @Override
    public int compareTo(Sensor that) {
        return new CompareToBuilder().append(getListOrder(), that.getListOrder()).toComparison();
    }
}
