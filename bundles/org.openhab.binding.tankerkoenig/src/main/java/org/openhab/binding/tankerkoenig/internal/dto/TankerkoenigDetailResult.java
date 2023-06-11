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
package org.openhab.binding.tankerkoenig.internal.dto;

import org.openhab.binding.tankerkoenig.internal.TankerkoenigBindingConstants;

/***
 * The {@link TankerkoenigDetailResult} class is the representing java model for the json result of the tankerkoenig.de
 * details request
 * Actually used for OpeningTimes only.
 *
 * @author Dennis Dollinger - Initial contribution
 * @author Jürgen Baginski - Initial contribution
 */
public class TankerkoenigDetailResult {

    private boolean ok;
    private boolean wholeDay;
    private OpeningTimes openingTimes;
    private LittleStation littleStation;
    private String message;

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public boolean iswholeDay() {
        return wholeDay;
    }

    public void setwholeDay(boolean WholeDay) {
        this.wholeDay = WholeDay;
    }

    public static TankerkoenigDetailResult emptyResult() {
        TankerkoenigDetailResult emptyResult = new TankerkoenigDetailResult();
        emptyResult.setOk(false);
        emptyResult.setMessage(TankerkoenigBindingConstants.NO_VALID_RESPONSE);
        return emptyResult;
    }

    public LittleStation getLittleStation() {
        return littleStation;
    }

    public void setLittleStation(LittleStation littleStation) {
        this.littleStation = littleStation;
    }

    public OpeningTimes getOpeningTimes() {
        return openingTimes;
    }

    public void setOpeningTimes(OpeningTimes openingTimes) {
        this.openingTimes = openingTimes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
