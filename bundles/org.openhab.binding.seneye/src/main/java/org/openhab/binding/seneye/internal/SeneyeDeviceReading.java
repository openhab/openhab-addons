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
