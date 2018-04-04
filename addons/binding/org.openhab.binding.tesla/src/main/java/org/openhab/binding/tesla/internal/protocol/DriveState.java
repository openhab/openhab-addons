/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tesla.internal.protocol;

/**
 * The {@link DriveState} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Karel Goderis - Initial contribution
 */
public class DriveState {

    public double latitude;
    public double longitude;
    public int heading;
    public int gps_as_of;
    public String shift_state;
    public String speed;

    DriveState() {
    }

}
