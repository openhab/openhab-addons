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
 * The {@link SenseEnergyApiDevice } is the dto for the api sense discovered devices
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyApiDevice {
    public String id;
    public String name;
    public String icon;
    public SenseEnergyApiDeviceTags tags;
}
/* @formatter:off
[
    {
        "id":"0e64ad2b",
        "name":"AC 2",
        "icon":"ac",
        "tags": {
            "Alertable":"true",
            "AlwaysOn":"false",
            "DateCreated":"2024-05-09T04:16:08.000Z",
            "DateFirstUsage":"2024-04-02",
            "DefaultUserDeviceType":"AC",
            "DeployToMonitor":"true",
            "DeviceListAllowed":"true",
            "ModelCreatedVersion":"21",
            "ModelUpdatedVersion":"27",
            "name_useredit":"false",
            "OriginalName":"AC 2",
            "PeerNames": [
                {
                    "Name":"Gas Dryer",
                    "UserDeviceType":"GasDryer",
                    "Percent":97.0,
                    "Icon":"washer",
                    "UserDeviceTypeDisplayString":"Gas Dryer"
                },
                {
                    "Name":"Furnace",
                    "UserDeviceType":"Furnace",
                    "Percent":1.0,
                    "Icon":"heat",
                    "UserDeviceTypeDisplayString":"Furnace"
                },
                {"Name":"Pump","UserDeviceType":"Pump","Percent":1.0,"Icon":"pump","UserDeviceTypeDisplayString":"Pump"},
                {"Name":"Washer","UserDeviceType":"Washer","Percent":1.0,"Icon":"washer","UserDeviceTypeDisplayString":"Washer"}
            ],
            "Pending":"false",
            "Revoked":"false",
            "TimelineAllowed":"true",
            "TimelineDefault":"true",
            "Type":"AC",
            "UserDeletable":"true",
            "UserDeviceType":"AC",
            "UserDeviceTypeDisplayString":"AC",
            "UserEditable":"true",
            "UserEditableMeta":"true",
            "UserMergeable":"true",
            "UserShowInDeviceList":"true"
        }
    },
        {
        "id": "zDcE5VOr",
        "name": "Sense Whole House Proxy Fan",
        "icon": "plug",
        "tags": {
            "Alertable": "false",
            "ControlCapabilities": [
                "OnOff",
                "StandbyThreshold"
            ],
            "DateCreated": "2024-09-22T03:13:45.000Z",
            "DefaultUserDeviceType": "SmartPlug",
            "DeviceListAllowed": "false",
            "DUID": "53:75:31:70:55:53",
            "IntegratedDeviceType": "IntegratedSmartPlug",
            "IntegrationType": "TPLink",
            "name_useredit": "false",
            "OriginalName": "Sense Whole House Proxy Fan",
            "Revoked": "false",
            "SmartPlugModel": "TP-Link Kasa HS110",
            "SSIEnabled": "true",
            "SSIModel": "SelfReporting",
            "TimelineAllowed": "false",
            "TimelineDefault": "false",
            "UserDeletable": "false",
            "UserDeleted": "true",
            "UserDeviceType": "SmartPlug",
            "UserDeviceTypeDisplayString": "Smart Plug",
            "UserEditable": "false",
            "UserEditableMeta": "true",
            "UserMergeable": "false",
            "UserShowInDeviceList": "true",
            "UserVisibleDeviceId": "53:75:31:70:55:53"
        }
    },
    {"id":"2681a800","name":"Toaster oven","icon":"toaster_oven","tags":{"Alertable":"true","AlwaysOn":"false","DateCreated":"2024-04-02T20:03:12.000Z","DateFirstUsage":"2024-04-01","DefaultUserDeviceType":"MysteryHeat","DeployToMonitor":"true","DeviceListAllowed":"true","ModelCreatedVersion":"2","ModelUpdatedVersion":"27","name_useredit":"true","OriginalName":"Heat 1","PeerNames":[{"Name":"Toaster Oven","UserDeviceType":"ToasterOven","Percent":82.0,"Icon":"toaster_oven","UserDeviceTypeDisplayString":"Toaster Oven"},{"Name":"Tea Kettle","UserDeviceType":"TeaKettle","Percent":6.0,"Icon":"kettle","UserDeviceTypeDisplayString":"Tea Kettle"},{"Name":"Printer","UserDeviceType":"Printer","Percent":2.0,"Icon":"printer","UserDeviceTypeDisplayString":"Printer"},{"Name":"Hair Dryer","UserDeviceType":"HairDryer","Percent":2.0,"Icon":"hair_dryer","UserDeviceTypeDisplayString":"Hair Dryer"}],"Pending":"false","Revoked":"false","TimelineAllowed":"true","TimelineDefault":"true","Type":"UnknownHeat","UserDeletable":"true","UserDeviceType":"ToasterOven","UserDeviceTypeDisplayString":"Toaster Oven","UserEditable":"true","UserEditableMeta":"true","UserMergeable":"true","UserShowInDeviceList":"true"}},{"id":"37cec5c4","name":"Furnace","icon":"heat","tags":{"Alertable":"true","AlwaysOn":"false","DateCreated":"2024-04-19T02:12:31.000Z","DateFirstUsage":"2024-04-02","DefaultUserDeviceType":"Furnace","DeployToMonitor":"true","DeviceListAllowed":"true","ModelCreatedVersion":"16","ModelUpdatedVersion":"27","name_useredit":"false","OriginalName":"Furnace","PeerNames":[{"Name":"Furnace","UserDeviceType":"Furnace","Percent":86.0,"Icon":"heat","UserDeviceTypeDisplayString":"Furnace"},{"Name":"Fan","UserDeviceType":"Fan","Percent":7.0,"Icon":"fan","UserDeviceTypeDisplayString":"Fan"},{"Name":"Central Heat","UserDeviceType":"CentralHeat","Percent":5.0,"Icon":"heat","UserDeviceTypeDisplayString":"Central Heat"},{"Name":"AC","UserDeviceType":"AC","Percent":1.0,"Icon":"ac","UserDeviceTypeDisplayString":"AC"}],"Pending":"false","PreselectionIndex":0,"Revoked":"false","TimelineAllowed":"true","TimelineDefault":"false","Type":"Furnace","UserDeletable":"true","UserDeviceType":"Furnace","UserDeviceTypeDisplayString":"Furnace","UserEditable":"true","UserEditableMeta":"true","UserMergeable":"true","UserShowInDeviceList":"true"}},{"id":"57aca87d","name":"Microwave","icon":"microwave","tags":{"Alertable":"true","AlwaysOn":"false","DateCreated":"2024-04-22T18:01:40.000Z","DateFirstUsage":"2024-04-01","DefaultUserDeviceType":"Microwave","DeployToMonitor":"true","DeviceListAllowed":"true","ModelCreatedVersion":"18","ModelUpdatedVersion":"27","name_useredit":"false","OriginalName":"Microwave","PeerNames":[{"Name":"Microwave","UserDeviceType":"Microwave","Percent":100.0,"Icon":"microwave","UserDeviceTypeDisplayString":"Microwave"}],"Pending":"false","PreselectionIndex":0,"Revoked":"false","TimelineAllowed":"true","TimelineDefault":"true","Type":"Microwave","UserDeletable":"true","UserDeviceType":"Microwave","UserDeviceTypeDisplayString":"Microwave","UserEditable":"true","UserEditableMeta":"true","UserMergeable":"true","UserShowInDeviceList":"true"}},{"id":"a6b4aa4d","name":"Backyard Spotlight","icon":"lightbulb","tags":{"Alertable":"true","AlwaysOn":"false","DateCreated":"2024-06-07T12:10:01.000Z","DateFirstUsage":"2024-04-06","DefaultUserDeviceType":"Light","DeployToMonitor":"true","DeviceListAllowed":"true","ModelCreatedVersion":"24","ModelUpdatedVersion":"27","name_useredit":"true","OriginalName":"Light 1","PeerNames":[{"Name":"Light","UserDeviceType":"Light","Percent":87.0,"Icon":"lightbulb","UserDeviceTypeDisplayString":"Light"},{"Name":"TV/Monitor","UserDeviceType":"TV","Percent":7.0,"Icon":"tv","UserDeviceTypeDisplayString":"TV/Monitor"},{"Name":"Appliance Light","UserDeviceType":"ApplianceLight","Percent":3.0,"Icon":"lightbulb","UserDeviceTypeDisplayString":"Appliance Light"},{"Name":"Water Dispenser","UserDeviceType":"WaterDispenser","Percent":1.0,"Icon":"socket","UserDeviceTypeDisplayString":"Water Dispenser"}],"Pending":"false","Revoked":"false","TimelineAllowed":"true","TimelineDefault":"true","Type":"Lighting","UserDeletable":"true","UserDeviceType":"Light","UserDeviceTypeDisplayString":"Light","UserEditable":"true","UserEditableMeta":"true","UserMergeable":"true","UserShowInDeviceList":"true"}},{"id":"a76c2ab2","name":"Garage door","icon":"garage","tags":{"Alertable":"true","AlwaysOn":"false","DateCreated":"2024-04-19T02:12:31.000Z","DateFirstUsage":"2024-04-01","DefaultUserDeviceType":"GarageDoor","DeployToMonitor":"true","DeviceListAllowed":"true","ModelCreatedVersion":"16","ModelUpdatedVersion":"27","name_useredit":"false","OriginalName":"Garage door","PeerNames":[{"Name":"Garage Door","UserDeviceType":"GarageDoor","Percent":99.0,"Icon":"garage","UserDeviceTypeDisplayString":"Garage Door"},{"Name":"Pump","UserDeviceType":"Pump","Percent":1.0,"Icon":"pump","UserDeviceTypeDisplayString":"Pump"}],"Pending":"false","Revoked":"false","TimelineAllowed":"true","TimelineDefault":"true","Type":"GarageDoor","UserDeletable":"true","UserDeviceType":"GarageDoor","UserDeviceTypeDisplayString":"Garage Door","UserEditable":"true","UserEditableMeta":"true","UserMergeable":"true","UserShowInDeviceList":"true"}},{"id":"always_on","name":"Always On","icon":"alwayson","tags":{"DefaultUserDeviceType":"AlwaysOn","DeviceListAllowed":"true","TimelineAllowed":"false","UserDeleted":"false","UserDeviceType":"AlwaysOn","UserDeviceTypeDisplayString":"Always On","UserEditable":"false","UserMergeable":"false"}},{"id":"db3fc2f4","name":"Mystery Device 1","icon":"socket","tags":{"Alertable":"true","AlwaysOn":"false","DateCreated":"2024-04-08T05:41:52.000Z","DateFirstUsage":"2024-04-01","DefaultUserDeviceType":"MysteryDevice","DeployToMonitor":"true","DeviceListAllowed":"true","ModelCreatedVersion":"11","ModelUpdatedVersion":"27","name_useredit":"false","OriginalName":"Mystery Device 1","PeerNames":[{"Name":"Light","UserDeviceType":"Light","Percent":74.0,"Icon":"lightbulb","UserDeviceTypeDisplayString":"Light"},{"Name":"Aquarium","UserDeviceType":"Aquarium","Percent":6.0,"Icon":"aquarium","UserDeviceTypeDisplayString":"Aquarium"},{"Name":"Furnace","UserDeviceType":"Furnace","Percent":2.0,"Icon":"heat","UserDeviceTypeDisplayString":"Furnace"},{"Name":"Circulator Pump","UserDeviceType":"CirculatorPump","Percent":2.0,"Icon":"pump","UserDeviceTypeDisplayString":"Circulator Pump"},{"Name":"Computer","UserDeviceType":"Computer","Percent":2.0,"Icon":"computer","UserDeviceTypeDisplayString":"Computer"},{"Name":"AC","UserDeviceType":"AC","Percent":1.0,"Icon":"ac","UserDeviceTypeDisplayString":"AC"}],"Pending":"false","Revoked":"false","TimelineAllowed":"true","TimelineDefault":"true","Type":"Unknown","UserDeletable":"true","UserDeviceType":"MysteryDevice","UserDeviceTypeDisplayString":"Mystery Device","UserEditable":"true","UserEditableMeta":"true","UserMergeable":"true","UserShowInDeviceList":"true"}},{"id":"e329c895","name":"Wine Cooler","icon":"ac","tags":{"Alertable":"true","AlwaysOn":"false","DateCreated":"2024-04-11T09:28:45.000Z","DateFirstUsage":"2024-04-01","DefaultUserDeviceType":"AC","DeployToMonitor":"true","DeviceListAllowed":"true","ModelCreatedVersion":"12","ModelUpdatedVersion":"27","name_useredit":"true","NameUserGuess":"false","OriginalName":"AC","PeerNames":[{"Name":"Dehumidifier","UserDeviceType":"Dehumidifier","Percent":33.0,"Icon":"dehumidifier","UserDeviceTypeDisplayString":"Dehumidifier"},{"Name":"AC","UserDeviceType":"AC","Percent":22.0,"Icon":"ac","UserDeviceTypeDisplayString":"AC"},{"Name":"Fridge","UserDeviceType":"Fridge","Percent":14.0,"Icon":"fridge","UserDeviceTypeDisplayString":"Fridge"},{"Name":"Freezer","UserDeviceType":"Freezer","Percent":4.0,"Icon":"freezer","UserDeviceTypeDisplayString":"Freezer"},{"Name":"Furnace","UserDeviceType":"Furnace","Percent":4.0,"Icon":"heat","UserDeviceTypeDisplayString":"Furnace"},{"Name":"AC","UserDeviceType":"AC","Percent":3.0,"Icon":"ac","UserDeviceTypeDisplayString":"AC"}],"Pending":"false","Revoked":"false","TimelineAllowed":"true","TimelineDefault":"true","Type":"WindowAC","UserDeletable":"true","UserDeviceType":"AC","UserDeviceTypeDisplayString":"AC","UserEditable":"true","UserEditableMeta":"true","UserMergeable":"true","UserShowBubble":"true","UserShowInDeviceList":"true"}},{"id":"Pgz8OdRP","name":"Oven","icon":"stove","tags":{"Alertable":"true","AlwaysOn":"false","DateCreated":"2024-05-09T04:16:08.000Z","DateFirstUsage":"2024-04-02","DefaultUserDeviceType":"MysteryHeat","DeployToMonitor":"true","DeviceListAllowed":"true","MergedDevices":"0988f51e,fc5b120b","ModelCreatedVersion":"21","name_useredit":"true","OriginalName":"Heat 3","PeerNames":[],"Pending":"false","Revoked":"false","TimelineAllowed":"true","TimelineDefault":"true","Type":"UnknownHeat","UserDeletable":"true","UserDeviceType":"Oven","UserDeviceTypeDisplayString":"Oven","UserEditable":"true","UserEditableMeta":"true","UserMergeable":"true","UserShowInDeviceList":"true","Virtual":"true"},"make":"Decor"},{"id":"solar","name":"Solar","icon":"solar_alt","tags":{"DefaultUserDeviceType":"Solar","DeviceListAllowed":"false","TimelineAllowed":"false","UserDeleted":"false","UserDeviceType":"Solar","UserDeviceTypeDisplayString":"Solar","UserEditable":"false","UserMergeable":"false"}},{"id":"unknown","name":"Other","icon":"home","tags":{"DefaultUserDeviceType":"Unknown","DeviceListAllowed":"true","TimelineAllowed":"false","UserDeleted":"false","UserDeviceType":"Unknown","UserDeviceTypeDisplayString":"Unknown","UserEditable":"false","UserMergeable":"false"}},{"id":"pd-9asJf4FK","name":"Electric Vehicle","icon":"car","monitor_id":869850,"tags":{"DefaultUserDeviceType":"ElectricVehicle","DeviceListAllowed":"true","Stage":"Tracking","UserAdded":"false","UserDeviceType":"ElectricVehicle","UserEditable":"false","UserMergeable":"false"},"type":"ElectricVehicle","count":1,"stage":"Tracking","last_updated":"2024-08-13T00:00:00.000Z","realtime_devices":[],"deleted":null},{"id":"pd-iw0KZlLV","name":"Washer","icon":"washer","monitor_id":869850,"tags":{"DefaultUserDeviceType":"Washer","DeviceListAllowed":"true","Stage":"Inventory","UserAdded":"false","UserDeviceType":"Washer","UserEditable":"false","UserMergeable":"false"},"type":"Washer","count":1,"stage":"Inventory","realtime_devices":[],"deleted":null},{"id":"pd-nWUFUBc0","name":"Hot Tub","icon":"hot_tub","monitor_id":869850,"tags":{"DefaultUserDeviceType":"HotTub","DeviceListAllowed":"true","Stage":"Inventory","UserAdded":"false","UserDeviceType":"HotTub","UserEditable":"false","UserMergeable":"false"},"type":"HotTub","count":1,"stage":"Inventory","realtime_devices":[],"deleted":null},{"id":"pd-QMAly6Xm","name":"Dishwasher","icon":"dishes","monitor_id":869850,"tags":{"DefaultUserDeviceType":"Dishwasher","DeviceListAllowed":"true","Stage":"Inventory","UserAdded":"false","UserDeviceType":"Dishwasher","UserEditable":"false","UserMergeable":"false"},"type":"Dishwasher","count":1,"stage":"Inventory","realtime_devices":[],"deleted":null},{"id":"pd-Rj3WQa1s","name":"Fridges","icon":"fridge","monitor_id":869850,"tags":{"DefaultUserDeviceType":"Fridge","DeviceListAllowed":"true","Stage":"Tracking","UserAdded":"false","UserDeviceType":"Fridge","UserEditable":"false","UserMergeable":"false"},"type":"Fridge","count":2,"stage":"Tracking","last_updated":"2024-08-13T00:00:00.000Z","realtime_devices":[],"deleted":null},{"id":"pd-Z4ZMTVq5","name":"Pool Pump","icon":"pump","monitor_id":869850,"tags":{"DefaultUserDeviceType":"PoolPump","DeviceListAllowed":"true","Stage":"Inventory","UserAdded":"false","UserDeviceType":"PoolPump","UserEditable":"false","UserMergeable":"false"},"type":"PoolPump","count":1,"stage":"Inventory","realtime_devices":[],"deleted":null}]
@formatter:on
*/
