/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.seneye.internal;

/**
 * The result of a seneye readout
 *
 * @author Niko Tanghe - Initial contribution
 */

public class SeneyeDeviceReading {
    public SeneyeStatus status;
    public SeneyeDeviceReadingTemperature temperature;
    public SeneyeDeviceReadingPh ph;
    public SeneyeDeviceReadingNh3 nh3;
    public SeneyeDeviceReadingNh4 nh4;
    public SeneyeDeviceReadingO2 o2;
    public SeneyeDeviceReadingLux lux;
    public SeneyeDeviceReadingPar par;
    public SeneyeDeviceReadingKelvin kelvin;
}
