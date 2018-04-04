/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Hue API device object
 *
 * @author Dan Cunningham
 *
 */
public class HueDevice {
    public HueState state;
    public String type = "Dimmable Light";
    public String name;
    public String modelid = "LWB004";
    public String uniqueid;
    public String manufacturername = "openHAB";
    public String swversion = "66009461";
    public Map<String, String> pointsymbol;

    public HueDevice(HueState state, String name, Integer uniqueid) {
        super();
        this.state = state;
        this.name = name;
        this.uniqueid = uniqueid.toString();
        this.pointsymbol = new HashMap<String, String>();
        for (int i = 1; i < 9; i++) {
            this.pointsymbol.put(String.valueOf(i), "none");
        }
    }

}
