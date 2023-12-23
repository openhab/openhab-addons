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

import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.FEATR;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.LISTORD;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.MODE;
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
public class Circuit extends ResponseModel implements Comparable<Circuit> {

    private final static List<Attribute> REQUEST_ATTRIBUTES = List.of(STATUS, MODE, LISTORD, FEATR);

    public static RequestObject createRefreshRequest(String objectName) {
        return new RequestObject(objectName, REQUEST_ATTRIBUTES);
    }

    Circuit() {
        super(REQUEST_ATTRIBUTES);
    }

    public Circuit(ResponseObject response) {
        super(REQUEST_ATTRIBUTES, response);
    }

    protected Circuit(List<Attribute> requestAttributes) {
        super(requestAttributes);
    }

    protected Circuit(List<Attribute> requestAttributes, ResponseObject response) {
        super(requestAttributes, response);
    }

    public boolean isOn() {
        return getValueAsBoolean(STATUS);
    }

    public int getListOrder() {
        return getValueAsInt(LISTORD);
    }

    public String getMode() {
        return getValueAsString(MODE);
    }

    public boolean isFeature() {
        return getValueAsBoolean(FEATR);
    }

    @Override
    public int compareTo(Circuit that) {
        return new CompareToBuilder().append(getListOrder(), that.getListOrder()).toComparison();
    }
}
