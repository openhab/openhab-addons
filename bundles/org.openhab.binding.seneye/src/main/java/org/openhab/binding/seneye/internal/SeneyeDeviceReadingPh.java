/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The result of a seneye readout - The PH water level
 *
 * @author Niko Tanghe - Initial contribution
 */

public class SeneyeDeviceReadingPh {
    public int trend;
    public int critical_in;
    public double avg;
    public boolean status;
    public double curr;
    public SeneyeDeviceReadingAdvice[] advices;
}
