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
 * The {@link TankerkoenigListResult} class is the representing java model for the json result of the tankerkoenig.de
 * api
 *
 * @author Dennis Dollinger - Initial contribution
 */
public class TankerkoenigListResult {

    private boolean ok;
    private Prices prices;
    private String message;

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public Prices getPrices() {
        return prices;
    }

    public void setPrices(Prices prices) {
        this.prices = prices;
    }

    public static TankerkoenigListResult emptyResult() {
        TankerkoenigListResult emptyResult = new TankerkoenigListResult();
        emptyResult.setOk(false);
        emptyResult.setMessage(TankerkoenigBindingConstants.NO_VALID_RESPONSE);
        return emptyResult;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
