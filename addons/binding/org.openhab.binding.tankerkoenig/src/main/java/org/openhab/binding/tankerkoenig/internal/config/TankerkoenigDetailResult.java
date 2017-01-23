/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.tankerkoenig.internal.config;

/***
 * The {@link TankerkoenigDetailResult} class is the representing java model for the json result of the tankerkoenig.de
 * details request
 * Actually not in use. Will be needed for detailed information of gas stations
 *
 * @author Dennis Dollinger
 *
 */
public class TankerkoenigDetailResult {

    private String status;
    private boolean ok;
    private String message;

    private Station[] stations;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Station[] getStations() {
        return stations;
    }

    public void setStations(Station[] stations) {
        this.stations = stations;
    }

}
