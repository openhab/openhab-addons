/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.domintell.internal.protocol.model.type;

/**
* The {@link DataType} is the enumeration of possible data types in received messages
*
* @author Gabor Bicskei - Initial contribution
*/
public enum DataType {
    I,  //Inputs
    O,  //Outputs
    D,  //Dimmers
    X,  //DMX
    T,  //Temperature Heating setpoint
    U,  //Temperature Cooling setpoint
    C,  //Infrared Command
    S,  //Sound
    B,  //Button
    P,  //Temp. Plage
    K   //Clocks
}
