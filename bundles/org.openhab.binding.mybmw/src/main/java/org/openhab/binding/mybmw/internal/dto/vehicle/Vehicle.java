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
package org.openhab.binding.mybmw.internal.dto.vehicle;

import org.openhab.binding.mybmw.internal.dto.properties.Properties;
import org.openhab.binding.mybmw.internal.dto.status.Status;

/**
 * The {@link Vehicle} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Vehicle {
    public String vin;// ": "WBY1Z81040V905639",
    public String model;// ": "i3 94 (+ REX)",
    public int year;// ": 2017,
    public String brand;// ": "BMW",
    public String headUnit;// ": "ID5",
    public boolean isLscSupported;// ": true,
    public String driveTrain;// ": "ELECTRIC",
    public String puStep;// ": "0321",
    public String iStep;// ": "I001-21-03-530",
    public String telematicsUnit;// ": "TCB1",
    public String hmiVersion;// ": "ID4",
    public String bodyType;// ": "I01",
    public String a4aType;// ": "USB_ONLY",
    public String exFactoryPUStep;// ": "0717",
    public String exFactoryILevel;// ": "I001-17-07-500"
    public Capabilities capabilities;
    // "connectedDriveServices": [] currently no clue how to resolve,
    public Properties properties;
    public boolean isMappingPending;// ":false,"
    public boolean isMappingUnconfirmed;// ":false,
    public Status status;
    public boolean valid = false;
}
