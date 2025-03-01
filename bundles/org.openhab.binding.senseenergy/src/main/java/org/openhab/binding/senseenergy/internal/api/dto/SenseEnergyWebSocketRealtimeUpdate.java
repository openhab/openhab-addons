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

package org.openhab.binding.senseenergy.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SenseEnergyWebSocketRealtimeUpdate } dto object for web socket realtime updates. Fields which are not used
 * have been commented out in order to save memory and processor bandwidth in the gson conversion.
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyWebSocketRealtimeUpdate {
    public float[] voltage;
    // public long frame;
    public SenseEnergyWebSocketDevice[] devices;
    // public float defaultCost;
    // public float[] channels;
    public float hz;
    public float w;
    // public float c;
    @SerializedName("solar_w")
    public float solarW;
    @SerializedName("grid_w")
    public float gridW;
    // @SerializedName("solar_c")
    // public float solarC;
    // @SerializedName("solar_pct")
    // public int solarPct;
}

/* @formatter:off
  {
    "payload":{
       "voltage":[
          121.23905944824219,
          121.71208190917969
       ],
       "frame":53187540,
       "devices":[
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
             "id":"unknown",
             "name":"Other",
             "icon":"home",
             "tags":{
                "DefaultUserDeviceType":"Unknown",
                "DeviceListAllowed":"true",
                "TimelineAllowed":"false",
                "UserDeleted":"false",
                "UserDeviceType":"Unknown",
                "UserDeviceTypeDisplayString":"Unknown",
                "UserEditable":"false",
                "UserMergeable":"false"
             },
             "attrs":[

             ],
             "w":550.27637
          },
          {
             "id":"0e64ad2b",
             "name":"AC 2",
             "icon":"ac",
             "tags":{
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
                "PeerNames":[
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
                   {
                      "Name":"Pump",
                      "UserDeviceType":"Pump",
                      "Percent":1.0,
                      "Icon":"pump",
                      "UserDeviceTypeDisplayString":"Pump"
                   },
                   {
                      "Name":"Washer",
                      "UserDeviceType":"Washer",
                      "Percent":1.0,
                      "Icon":"washer",
                      "UserDeviceTypeDisplayString":"Washer"
                   }
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
             },
             "attrs":[

             ],
             "w":498.96848
          },
          {
             "id":"always_on",
             "name":"Always On",
             "icon":"alwayson",
             "tags":{
                "DefaultUserDeviceType":"AlwaysOn",
                "DeviceListAllowed":"true",
                "TimelineAllowed":"false",
                "UserDeleted":"false",
                "UserDeviceType":"AlwaysOn",
                "UserDeviceTypeDisplayString":"Always On",
                "UserEditable":"false",
                "UserMergeable":"false"
             },
             "attrs":[

             ],
             "w":445.0
          }
       ],
       "deltas":[

       ],
       "defaultCost":20.77,
       "channels":[
          810.09912109375,
          684.145751953125
       ],
       "hz":59.98676681518555,
       "w":1494.244873046875,
       "c":31,
       "solar_w":6104.595703125,
       "grid_w":-4610,
       "solar_c":126,
       "_stats":{
          "brcv":1.7236690510805538E9,
          "mrcv":1.723669051122E9,
          "msnd":1.723669051123E9
       },
       "aux":{
          "solar":[
             -3051.75732421875,
             -3052.838134765625
          ]
       },
       "power_flow":{
          "solar":[
             "grid",
             "home"
          ],
          "grid":[

          ]
       },
       "solar_pct":100,
       "d_w":1494,
       "d_solar_w":6105,
       "epoch":1723669048
    },
    "type":"realtime_update"
 })
* @formatter:on
*/
