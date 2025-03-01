/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.senseenergy.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * {@link SenseEnergyApiGetTrendsDevice }
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyApiGetTrendsDevice {
    public String id;
    public String name;
    public String icon;
    SenseEnergyApiDeviceTags tags;
    @SerializedName("history")
    public float[] historyEnergy;
    @SerializedName("avgw")
    public float averagePower;
    @SerializedName("total_kwh")
    public float totalEnergy;
    @SerializedName("total_cost")
    public float totalCost;
    @SerializedName("pct")
    public float percent;
    @SerializedName("cost_history")
    public float[] historyCost;
}

/* @formatter:off
 *       "devices":[
         {
            "id":"LpzF0vGG",
            "name":"Wine Cooler",
            "icon":"ac",
            "tags":{
               "Alertable":"true",
               "AlwaysOn":"false",
               "DateCreated":"2024-04-11T09:28:45.000Z",
               "DateFirstUsage":"2024-04-01",
               "DefaultUserDeviceType":"AC",
               "DeployToMonitor":"true",
               "DeviceListAllowed":"true",
               "MergedDevices":"db3fc2f4,e329c895",
               "ModelCreatedVersion":"12",
               "ModelUpdatedVersion":"28",
               "name_useredit":"true",
               "NameUserGuess":"false",
               "OriginalName":"AC",
               "PeerNames":[

               ],
               "Pending":"false",
               "Revoked":"false",
               "TimelineAllowed":"true",
               "TimelineDefault":"true",
               "Type":"WindowAC",
               "UserDeletable":"true",
               "UserDeviceType":"AC",
               "UserDeviceTypeDisplayString":"AC",
               "UserEditable":"true",
               "UserEditableMeta":"true",
               "UserMergeable":"true",
               "UserShowBubble":"true",
               "UserShowInDeviceList":"true",
               "Virtual":"true"
            },
            "history":[
               0.37245202,
               0.01909,
               0.020947,
               0.368776,
               0.019066999,
               0.025436,
               0.28254902,
               0.019898001,
               0.019055,
               0.170864,
               0.14370401,
               0.019107,
               0.059858,
               0.296629,
               0.019107,
               0.019064,
               0.345176,
               0.018984,
               0.040691003,
               0.364613,
               0.0,
               0.0,
               0.0,
               0.0
            ],
            "avgw":0.110211134,
            "total_kwh":2.6450672,
            "total_cost":53,
            "pct":14.0,
            "cost_history":[
               8,
               0,
               0,
               8,
               0,
               1,
               6,
               0,
               0,
               4,
               3,
               0,
               1,
               6,
               0,
               0,
               7,
               0,
               1,
               8,
               0,
               0,
               0,
               0
            ]
         },
 * @formatter:on
 */
