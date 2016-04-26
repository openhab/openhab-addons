/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.protocol.ip;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.openhab.binding.mysensors.protocol.MySensorsReader;

public class MySensorIpReader extends MySensorsReader {
    public MySensorIpReader(InputStream inStream, MySensorsIpConnection mysCon) {
        this.mysCon = mysCon;
        this.inStream = inStream;
        reads = new BufferedReader(new InputStreamReader(inStream));
    }
}
