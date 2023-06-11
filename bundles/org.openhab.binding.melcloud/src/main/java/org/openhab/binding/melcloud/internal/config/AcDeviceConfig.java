/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.melcloud.internal.config;

/**
 * Config class for a A.C. device.
 *
 * @author Pauli Anttila - Initial Contribution
 *
 */
public class AcDeviceConfig {

    public Integer deviceID;
    public Integer buildingID;
    public Integer pollingInterval;

    @Override
    public String toString() {
        return "[deviceID=" + deviceID + ", buildingID=" + buildingID + ", pollingInterval=" + pollingInterval + "]";
    }
}
