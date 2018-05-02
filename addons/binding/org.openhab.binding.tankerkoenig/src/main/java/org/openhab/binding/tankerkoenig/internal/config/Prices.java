/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tankerkoenig.internal.config;

import java.util.List;

/**
 * The {@link Prices} class is the representing java model for the station specific json result of the tankerkoenig.de
 * api
 *
 * @author Dennis Dollinger - Initial contribution
 */
public class Prices {

    private List<LittleStation> stations;

    public List<LittleStation> getStations() {
        return stations;
    }

    public void setStations(List<LittleStation> stations) {
        this.stations = stations;
    }
}
