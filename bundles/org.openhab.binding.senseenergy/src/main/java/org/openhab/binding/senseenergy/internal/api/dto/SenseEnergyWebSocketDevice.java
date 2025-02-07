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

/**
 * {@link SenseEnergyWebSocketDevice} is dto for the websocket messages. Fields which are commented are not used in the
 * binding, but there for reference.
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyWebSocketDevice {
    public String id;
    public String name;
    // public String icon;
    public float w;
}

/* @formatter:off
{
 "id":"solar",
 "name":"Solar",
 "icon":"solar_alt",
 "tags":{
    "DefaultUserDeviceType":"Solar",
    "DeviceListAllowed":"false",
    "TimelineAllowed":"false",
    "UserDeleted":"false",
    "UserDeviceType":"Solar",
    "UserDeviceTypeDisplayString":"Solar",
    "UserEditable":"false",
    "UserMergeable":"false"
 },
 "attrs":[

 ],
 "w":6104.5957
},
{
    "id": "SgA3CQgg",
    "name": "Garage Freezer",
    "icon": "plug",
    "tags": {
        "Alertable": "true",
        "ControlCapabilities": [
            "OnOff",
            "StandbyThreshold"
        ],
        "DateCreated": "2024-12-02T00:03:45.000Z",
        "DefaultUserDeviceType": "SmartPlug",
        "DeviceListAllowed": "true",
        "DUID": "D8:44:89:30:81:D9",
        "IntegratedDeviceType": "IntegratedSmartPlug",
        "IntegrationType": "TPLink",
        "name_useredit": "false",
        "OriginalName": "Garage Freezer",
        "Revoked": "false",
        "SmartPlugModel": "TP-Link Kasa KP115",
        "SSIEnabled": "true",
        "SSIModel": "SelfReporting",
        "TimelineAllowed": "true",
        "TimelineDefault": "true",
        "UserDeletable": "false",
        "UserDeviceType": "SmartPlug",
        "UserDeviceTypeDisplayString": "Smart Plug",
        "UserEditable": "true",
        "UserEditableMeta": "true",
        "UserMergeable": "false",
        "UserShowInDeviceList": "true",
        "UserVisibleDeviceId": "D8:44:89:30:81:D9"
    },
    "attrs": [],
    "w": 37.227,
    "sd": {
        "w": 37.227,
        "i": 0.574,
        "v": 121.507,
        "e": 4.396
    },
    "ao_w": 1.0,
    "ao_st": false
}

@formatter:on
*/
