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

import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.HTMODE;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.SNAME;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.STATUS;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.intellicenter2.internal.protocol.Attribute;
import org.openhab.binding.intellicenter2.internal.protocol.ResponseObject;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class Heater extends ResponseModel {

    private static final List<Attribute> REQUEST_ATTRIBUTES = List.of(HTMODE, STATUS, SNAME);

    public Heater() {
        super(REQUEST_ATTRIBUTES);
    }

    public Heater(ResponseObject response) {
        super(REQUEST_ATTRIBUTES, response);
    }

    public HeatMode getHeaterMode() {
        return getValueAsEnum(HTMODE, HeatMode.class);
    }

    public String getStatus() {
        return getValueAsString(STATUS);
    }
}
