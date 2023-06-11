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
package org.openhab.binding.wifiled.internal.handler;

/**
 * @author Stefan Endrullis - Initial contribution
 */
public class LEDState {

    public final int state, program, programSpeed;
    public final int red, green, blue, white, white2;

    public LEDState(int state, int program, int programSpeed, int red, int green, int blue, int white, int white2) {
        this.state = state;
        this.program = program;
        this.programSpeed = programSpeed;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.white = white;
        this.white2 = white2;
    }
}
