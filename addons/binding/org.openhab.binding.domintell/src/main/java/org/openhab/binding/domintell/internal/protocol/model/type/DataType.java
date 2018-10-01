/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
