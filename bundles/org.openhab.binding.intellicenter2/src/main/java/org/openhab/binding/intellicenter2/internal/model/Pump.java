/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.GPM;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.LISTORD;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.PWR;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.RPM;
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
public class Pump extends ResponseModel implements Comparable<Pump> {

    private final static List<Attribute> REQUEST_ATTRIBUTES = List.of(STATUS, LISTORD, PWR, GPM, RPM);

    public static RequestObject createRefreshRequest(String objectName) {
        return new RequestObject(objectName, REQUEST_ATTRIBUTES);
    }

    Pump() {
        super(REQUEST_ATTRIBUTES);
    }

    public Pump(ResponseObject response) {
        super(REQUEST_ATTRIBUTES, response);
    }

    protected Pump(List<Attribute> requestAttributes) {
        super(requestAttributes);
    }

    protected Pump(List<Attribute> requestAttributes, ResponseObject response) {
        super(requestAttributes, response);
    }

    public boolean isOn() {
        return getValueAsBoolean(STATUS);
    }

    public int getListOrder() {
        return getValueAsInt(LISTORD);
    }

    public int getGPM() {
        return getValueAsInt(GPM);
    }

    public int getRPM() {
        return getValueAsInt(RPM);
    }

    public int getPowerConsumption() {
        return getValueAsInt(PWR);
    }

    @Override
    public int compareTo(Pump that) {
        return new CompareToBuilder().append(getListOrder(), that.getListOrder()).toComparison();
    }
}
