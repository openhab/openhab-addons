/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link Vehicle} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Vehicle {
    public static final String ACTIVATED = "ACTIVATED";
    public static final String SUPPORTED = "SUPPORTED";
    public static final String NOT_SUPPORTED = "NOT_SUPPORTED";

    public String vin;
    public String model;
    public String driveTrain;
    public String brand;
    public short yearOfConstruction;
    public String bodytype;
    public String color;
    public boolean statisticsCommunityEnabled;
    public boolean hasAlarmSystem;
    public Dealer dealer;
    public String breakdownNumber;
    public List<String> supportedChargingModes;

    public List<String> activatedServices = new ArrayList<String>();
    public List<String> notActivatedServices = new ArrayList<String>();
    public String vehicleFinder; // ACTIVATED
    public String hornBlow; // ACTIVATED
    public String lightFlash; // ACTIVATED
    public String doorLock; // ACTIVATED
    public String doorUnlock; // ACTIVATED
    public String climateNow; // ACTIVATED
    public String sendPoi; // ACTIVATED

    public List<String> supportedServices = new ArrayList<String>();
    public List<String> notSupportedServices = new ArrayList<String>();
    public String remote360; // SUPPORTED
    public String climateControl; // SUPPORTED
    public String chargeNow; // SUPPORTED
    public String lastDestinations; // SUPPORTED
    public String carCloud; // SUPPORTED
    public String remoteSoftwareUpgrade; // SUPPORTED
}
