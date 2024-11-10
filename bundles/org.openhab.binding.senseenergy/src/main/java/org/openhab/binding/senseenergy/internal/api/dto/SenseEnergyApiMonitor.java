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

import java.time.Instant;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SenseEnergyApiMonitor} MonitorDevice dto structure. All fields are documented here for reference, however
 * fields
 * marked as transient are not serialized in order to save processing time.
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyApiMonitor {
    public long id;
    @SerializedName("date_created")
    public Instant dateCreated;
    @SerializedName("time_zone")
    public String timezone;
    @SerializedName("solar_connected")
    public boolean solarConnected;
    @SerializedName("solar_configured")
    public boolean solarConfigured;
    @SerializedName("signal_check_completed_time")
    public Instant signalCheckCompletedTime;
    @SerializedName("ethernet_supported")
    public transient boolean ethernetSupported;
    @SerializedName("power_over_ethernet_supported")
    public transient boolean powerOverEthernetSupported;
    @SerializedName("aux_ignore")
    public transient boolean auxIgnore;
    @SerializedName("aux_port")
    public String auxPort;
    @SerializedName("hardware_type")
    public String hardwareType;
    @SerializedName("zigbee_supported")
    public transient boolean zigbeeSupported;
}

/* @formatter:off
 {
 "checksum": "5E4E0A223C1A8641BF2BF7473CB9F7EAB7B8ED7D",
 "device_data_checksum": "534D88564331E1709A907AD234D9FD3E9DEB3876",
 "monitor_overview": {
     "monitor": {
         "id": xxxx,
         "date_created": "2024-04-01T17:12:45.000Z",
         "serial_number": "N327004101",
             "time_zone": "America/Los_Angeles",
         "solar_connected": true,
         "solar_configured": true,
         "attributes": {
             "id": 117968,
             "name": "",
             "state": "CA",
             "cost": 20.77,
             "sell_back_rate": 20.77,
             "user_set_cost": false,
             "cycle_start": null,
             "basement_type": "No basement",
             "home_size_type": "3250 sq. ft",
             "home_type": "Single family",
             "number_of_occupants": "3",
             "occupancy_type": "Full-time",
             "year_built_type": "1980s",
             "basement_type_key": "basement_type_no_basement",
             "home_size_type_key": "home_size_3250sqft",
             "home_type_key": "home_type_single_family",
             "occupancy_type_key": "occupancy_full_time",
             "year_built_type_key": "year_built_1980s",
             "address": null,
             "city": null,
             "postal_code": "xxxx",
             "electricity_cost": {
                 "id": 6,
                 "location": "California",
                 "abbreviation": "CA",
                 "cost": 20.77,
                 "national_electricity_cost": {
                     "id": 1,
                     "location": "United States",
                     "abbreviation": "US",
                     "cost": 13.31
                 },
                 "national_electricity_cost_id": 1
             },
             "show_cost": true,
             "tou_enabled": false,
             "solar_tou_enabled": false,
             "power_region": "CAISO",
             "to_grid_threshold": null,
             "panel": null,
             "home_info_survey_progress": "COMPLETED",
             "device_survey_progress": "COMPLETED",
             "user_set_sell_back_rate": true
         },
         "signal_check_completed_time": "2024-04-01T17:31:55.000Z",
         "data_sharing": [],
         "ethernet_supported": false,
         "power_over_ethernet_supported": false,
         "aux_ignore": false,
         "aux_port": "solar",
         "hardware_type": "monitor",
         "zigbee_supported": false
     },
     "ndi_enabled": false,
     "local_api_enabled": false,
     "partner_channel": "6-4",
     "partner_tags": [
         "CHANNEL__Amazon"
     ],
     "num_devices": 15,
     "num_named_devices": 14,
     "num_unnamed_devices": 1
 }
 @formatter:on
*/
