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
package org.openhab.binding.ecobee.internal.dto.thermostat;

import java.util.List;

/**
 * The {@link DeviceDTO} represents a device attached to the thermostat. Devices may
 * not be modified remotely, all changes must occur on the thermostat.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class DeviceDTO {

    /*
     * A unique ID for the device.
     */
    public Integer deviceId;

    /*
     * The user supplied device name.
     */
    public String name;

    /*
     * The list of Sensor Objects associated with the device.
     */
    public List<Object> sensors;

    /*
     * The list of Output Objects associated with the device.
     */
    public List<Object> outputs;
}
