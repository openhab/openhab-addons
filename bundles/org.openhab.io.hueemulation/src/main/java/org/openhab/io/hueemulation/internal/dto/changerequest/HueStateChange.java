/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal.dto.changerequest;

import java.util.List;

/**
 * A POST message on a light endpoint will contain this change message body.
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
