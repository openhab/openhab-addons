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
package org.openhab.binding.toyota.internal.dto;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;

public class Vehicle {
    public static String PRODUCTION_DATE = "productionDate";

    public static Type LIST_CLASS = new TypeToken<List<Vehicle>>() {
    }.getType();

    public boolean isEntitled;
    public boolean hasAutomaticMMRegistration;
    public String vehicleId;
    public String vin;
    public String modelName;
    public String modelCode;
    public String productionYear;
    public String imageUrl;
    public String smallImageUrl;
    public String alias;
    public String licensePlate;
    public String exteriorColour;
    public String transmission;
    public String transmissionType;
    public String engine;
    public String fuel;
    public double horsePower;
    public boolean hybrid;
    public boolean owner;
    public String source;
    public boolean isNc;
    public ArrayList<Device> devices;
    public String deliveryCountry;
    public String productionDate;
    public ArrayList<String> features;
    public boolean isOneAppMigrated;
}
