/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.tankerkoenig.internal.config;

import java.util.ArrayList;

/**
 * The {@link Prices} class is the representing java model for the station specific json result of the tankerkoenig.de
 * api
 *
 * @author Dennis Dollinger
 *
 */

public class Prices {

    private ArrayList<LittleStation> stations;

    public ArrayList<LittleStation> getStations() {
        return stations;
    }

    public void setStations(ArrayList<LittleStation> stations) {
        this.stations = stations;
    }
}
