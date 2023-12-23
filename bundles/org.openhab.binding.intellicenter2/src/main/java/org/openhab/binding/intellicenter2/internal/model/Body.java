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

import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.HITMP;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.HTMODE;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.HTSRC;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.LOTMP;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.LSTTMP;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.MODE;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.SNAME;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.STATUS;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.VOL;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.intellicenter2.internal.protocol.Attribute;
import org.openhab.binding.intellicenter2.internal.protocol.RequestObject;
import org.openhab.binding.intellicenter2.internal.protocol.ResponseObject;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class Body extends ResponseModel {

    public static RequestObject createRefreshRequest(String objectName) {
        return new RequestObject(objectName, REQUEST_ATTRIBUTES);
    }

    private static final List<Attribute> REQUEST_ATTRIBUTES = List.of(MODE, HITMP, LOTMP, LSTTMP, VOL, SNAME, HTSRC,
            HTMODE, STATUS);

    Body() {
        super(REQUEST_ATTRIBUTES);
    }

    public Body(ResponseObject response) {
        super(REQUEST_ATTRIBUTES, response);
    }

    public int getCurrentTemperature() {
        return getValueAsInt(LSTTMP);
    }

    public int getMode() {
        return getValueAsInt(MODE);
    }

    public int getHighTemperature() {
        return getValueAsInt(HITMP);
    }

    public int getTargetTemperature() {
        return getValueAsInt(LOTMP);
    }

    public String getHeaterSource() {
        return getValueAsString(HTSRC);
    }

    public int getVolume() {
        return getValueAsInt(VOL);
    }

    public HeatMode getHeaterMode() {
        return getValueAsEnum(HTMODE, HeatMode.class);
    }

    public boolean isHeating() {
        return getHeaterMode() != HeatMode.OFF;
    }

    public boolean isEnabled() {
        return getValueAsBoolean(STATUS);
    }
}
