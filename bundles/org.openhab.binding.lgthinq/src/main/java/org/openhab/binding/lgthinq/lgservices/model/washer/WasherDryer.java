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
package org.openhab.binding.lgthinq.lgservices.model.washer;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link WasherDryer}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class WasherDryer {
    static final WasherDryer EMPTY_WASHER_DRYER = new WasherDryer();
    @JsonProperty("controlDataType")
    private String controlDataType = "";
    @JsonProperty("controlDataValueLength")
    private int controlDataValueLength;

    public void setControlDataType(String controlDataType) {
        this.controlDataType = controlDataType;
    }

    public String getControlDataType() {
        return controlDataType;
    }

    public void setControlDataValueLength(int controlDataValueLength) {
        this.controlDataValueLength = controlDataValueLength;
    }

    public int getControlDataValueLength() {
        return controlDataValueLength;
    }
}
