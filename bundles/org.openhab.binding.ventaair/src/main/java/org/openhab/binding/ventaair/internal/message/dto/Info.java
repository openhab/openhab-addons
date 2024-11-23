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
package org.openhab.binding.ventaair.internal.message.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Part of the {@link DeviceInfoMessage} containing details about the device state
 *
 * @author Stefan Triller - Initial contribution
 *
 */
public class Info {
    @SerializedName(value = "SWDisplay")
    private String swDisplay;

    @SerializedName(value = "SWPower")
    private String swPower;

    @SerializedName(value = "SWTouch")
    private String swTouch;

    @SerializedName(value = "SWWIFI")
    private String swWIFI;

    @SerializedName(value = "CleanMode")
    private boolean cleanMode; // default false?

    @SerializedName(value = "RelState")
    private boolean[] relState; // [true,true,false,false]

    @SerializedName(value = "TimerT")
    private int timerT;

    @SerializedName(value = "OperationT")
    private int operationT;

    @SerializedName(value = "DiscIonT")
    private int discIonT;

    @SerializedName(value = "CleaningT")
    private int cleaningT;

    @SerializedName(value = "FilterT")
    private int filterT;

    @SerializedName(value = "ServiceT")
    private int serviceT;

    @SerializedName(value = "UVCOnT")
    private int uvCOnT;

    @SerializedName(value = "UVCOffT")
    private int uvCOffT;

    @SerializedName(value = "CleaningR")
    private int cleaningR;

    @SerializedName(value = "Warnings")
    private int warnings;

    public String getSwDisplay() {
        return swDisplay;
    }

    public String getSwPower() {
        return swPower;
    }

    public String getSwTouch() {
        return swTouch;
    }

    public String getSwWIFI() {
        return swWIFI;
    }

    public boolean isCleanMode() {
        return cleanMode;
    }

    public boolean[] getRelState() {
        return relState;
    }

    public int getTimerT() {
        return timerT;
    }

    public int getOperationT() {
        return operationT;
    }

    public int getDiscIonT() {
        return discIonT;
    }

    public int getCleaningT() {
        return cleaningT;
    }

    public int getFilterT() {
        return filterT;
    }

    public int getServiceT() {
        return serviceT;
    }

    public int getUvCOnT() {
        return uvCOnT;
    }

    public int getUvCOffT() {
        return uvCOffT;
    }

    public int getCleaningR() {
        return cleaningR;
    }

    public int getWarnings() {
        return warnings;
    }
}
