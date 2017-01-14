/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol.serial;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.openhab.binding.mysensors.internal.protocol.MySensorsReader;

/**
 * Implements the serial reader that receives the messages from the MySensors network.
 *
 * @author Andrea Cioni
 * @author Tim Oberföll
 *
 */
public class MySensorsSerialReader extends MySensorsReader {

    public MySensorsSerialReader(InputStream inStream, MySensorsSerialConnection mysCon) {
        this.mysCon = mysCon;
        this.inStream = inStream;
        reads = new BufferedReader(new InputStreamReader(inStream));
    }
}
