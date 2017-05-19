package org.openhab.binding.evohome.internal.api.models.v2;

import com.google.gson.annotations.SerializedName;

public class Gateway {
    @SerializedName("locationOwner")
    public LocationOwner LocationOwner;

}

//{
//    "gatewayInfo": {
//      "gatewayId": "453237",
//      "mac": "00D02D4695DE",
//      "crc": "3C52",
//      "isWiFi": false
//    },
//    "temperatureControlSystems": [
//      {
//        "systemId": "478361",
//        "modelType": "EvoTouch",
//        "zones": [
//          {
//            "zoneId": "478360",
//            "modelType": "HeatingZone",
//            "heatSetpointCapabilities": {
//              "maxHeatSetpoint": 35.0000,
//              "minHeatSetpoint": 5.0000,
//              "valueResolution": 0.5,
//              "allowedSetpointModes": [
//                "PermanentOverride",
//                "FollowSchedule",
//                "TemporaryOverride"
//              ],
//              "maxDuration": "1.00:00:00",
//              "timingResolution": "00:10:00"
//            },
//            "scheduleCapabilities": {
//              "maxSwitchpointsPerDay": 6,
//              "minSwitchpointsPerDay": 1,
//              "timingResolution": "00:10:00",
//              "setpointValueResolution": 0.5
//            },
//            "name": "Woonkamer",
//            "zoneType": "RadiatorZone"
//          },
//          {
//            "zoneId": "634951",
//            "modelType": "HeatingZone",
//            "heatSetpointCapabilities": {
//              "maxHeatSetpoint": 35.0000,
//              "minHeatSetpoint": 5.0000,
//              "valueResolution": 0.5,
//              "allowedSetpointModes": [
//                "PermanentOverride",
//                "FollowSchedule",
//                "TemporaryOverride"
//              ],
//              "maxDuration": "1.00:00:00",
//              "timingResolution": "00:10:00"
//            },
//            "scheduleCapabilities": {
//              "maxSwitchpointsPerDay": 6,
//              "minSwitchpointsPerDay": 1,
//              "timingResolution": "00:10:00",
//              "setpointValueResolution": 0.5
//            },
//            "name": "Slaapkamer",
//            "zoneType": "RadiatorZone"
//          },
//          {
//            "zoneId": "634963",
//            "modelType": "HeatingZone",
//            "heatSetpointCapabilities": {
//              "maxHeatSetpoint": 35.0000,
//              "minHeatSetpoint": 5.0000,
//              "valueResolution": 0.5,
//              "allowedSetpointModes": [
//                "PermanentOverride",
//                "FollowSchedule",
//                "TemporaryOverride"
//              ],
//              "maxDuration": "1.00:00:00",
//              "timingResolution": "00:10:00"
//            },
//            "scheduleCapabilities": {
//              "maxSwitchpointsPerDay": 6,
//              "minSwitchpointsPerDay": 1,
//              "timingResolution": "00:10:00",
//              "setpointValueResolution": 0.5
//            },
//            "name": "Babykamer",
//            "zoneType": "RadiatorZone"
//          }
//        ],
//        "allowedSystemModes": [
//          {
//            "systemMode": "Auto",
//            "canBePermanent": true,
//            "canBeTemporary": false
//          },
//          {
//            "systemMode": "AutoWithEco",
//            "canBePermanent": true,
//            "canBeTemporary": true,
//            "maxDuration": "1.00:00:00",
//            "timingResolution": "01:00:00",
//            "timingMode": "Duration"
//          },
//          {
//            "systemMode": "Away",
//            "canBePermanent": true,
//            "canBeTemporary": true,
//            "maxDuration": "99.00:00:00",
//            "timingResolution": "1.00:00:00",
//            "timingMode": "Period"
//          },
//          {
//            "systemMode": "DayOff",
//            "canBePermanent": true,
//            "canBeTemporary": true,
//            "maxDuration": "99.00:00:00",
//            "timingResolution": "1.00:00:00",
//            "timingMode": "Period"
//          },
//          {
//            "systemMode": "HeatingOff",
//            "canBePermanent": true,
//            "canBeTemporary": false
//          },
//          {
//            "systemMode": "Custom",
//            "canBePermanent": true,
//            "canBeTemporary": true,
//            "maxDuration": "99.00:00:00",
//            "timingResolution": "1.00:00:00",
//            "timingMode": "Period"
//          }
//        ]
//      }
//    ]
//  }