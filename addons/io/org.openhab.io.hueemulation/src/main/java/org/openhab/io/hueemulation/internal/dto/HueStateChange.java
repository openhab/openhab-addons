/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal.dto;

import java.util.List;

/**
 * A POST message on a light entpoint will contain this change message body.
 * Not all fields will be set and always need to be checked.
 *
 * @author David Graeff - Initial contribution
 */
public class HueStateChange {
    public Boolean on;
    public Integer bri;
    public Integer hue;
    public Integer sat;
    public String effect;
    public Integer ct;
    public String alert;
    public List<Double> xy;
    public Integer transitiontime;
    public Integer bri_inc;
    public Integer hue_inc;
    public Integer sat_inc;
    public List<Double> xy_inc;
    public Integer ct_inc;
}
